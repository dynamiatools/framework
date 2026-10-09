import { describe, expect, it, vi } from 'vitest';
import type { ActionExecutionRequest, ActionExecutionResponse, ActionFlowStep, ActionMetadata } from '@dynamia-tools/sdk';
import { runActionFlow, type FlowStepHandlers } from './runActionFlow.js';

const action = { id: 'main' } as ActionMetadata;

function step(partial: Partial<ActionFlowStep> & Pick<ActionFlowStep, 'type'>): ActionFlowStep {
  return { flowId: 'f1', resumeToken: 'tok', ...partial };
}

function flowResponse(s: ActionFlowStep): ActionExecutionResponse {
  return { status: s.type === 'DONE' ? 'SUCCESS' : 'PENDING', statusCode: 200, flow: s } as ActionExecutionResponse;
}

function fakeClient(opts: {
  main: ActionExecutionResponse[];
  global?: Record<string, ActionExecutionResponse[]>;
  entity?: Record<string, ActionExecutionResponse[]>;
}) {
  const calls = { main: [] as ActionExecutionRequest[], global: [] as [string, ActionExecutionRequest][], entity: [] as [string, string, ActionExecutionRequest][] };
  const client = {
    actions: {
      execute: vi.fn(async (_a: ActionMetadata, req: ActionExecutionRequest) => {
        calls.main.push(req);
        return opts.main.shift()!;
      }),
      executeGlobal: vi.fn(async (id: string, req: ActionExecutionRequest) => {
        calls.global.push([id, req]);
        return opts.global![id]!.shift()!;
      }),
      executeEntity: vi.fn(async (cls: string, id: string, req: ActionExecutionRequest) => {
        calls.entity.push([cls, id, req]);
        return opts.entity![`${cls}/${id}`]!.shift()!;
      }),
    },
  };
  return { client: client as never, calls };
}

function handlers(overrides: Partial<FlowStepHandlers> = {}): FlowStepHandlers {
  return { confirm: vi.fn(async () => true), showToast: vi.fn(() => 't'), ...overrides };
}

describe('runActionFlow REDIRECT (experimental)', () => {
  it('navigates and ends the flow without resuming', async () => {
    const redirect = flowResponse(step({ type: 'REDIRECT', data: { url: '/books/1', awaitReturn: false } }));
    const { client, calls } = fakeClient({ main: [redirect] });
    const navigate = vi.fn();

    const result = await runActionFlow(client, action, {}, handlers({ navigate }));

    expect(navigate).toHaveBeenCalledWith('/books/1');
    expect(calls.main).toHaveLength(1);
    expect(result.flow?.type).toBe('REDIRECT');
  });

  it('rejects awaitReturn=true before navigating', async () => {
    const { client } = fakeClient({ main: [flowResponse(step({ type: 'REDIRECT', data: { url: '/x', awaitReturn: true } }))] });
    const navigate = vi.fn();

    await expect(runActionFlow(client, action, {}, handlers({ navigate }))).rejects.toThrow(/awaitReturn/);
    expect(navigate).not.toHaveBeenCalled();
  });

  it.each(['javascript:alert(1)', 'data:text/html,<script>1</script>', 'JaVaScRiPt:alert(1)'])('refuses unsafe url %s', async (url) => {
    const { client } = fakeClient({ main: [flowResponse(step({ type: 'REDIRECT', data: { url } }))] });
    const navigate = vi.fn();

    await expect(runActionFlow(client, action, {}, handlers({ navigate }))).rejects.toThrow(/refused/);
    expect(navigate).not.toHaveBeenCalled();
  });

  it('allows absolute https and relative urls', async () => {
    for (const url of ['https://example.com/a', '/a/b', 'a/b']) {
      const { client } = fakeClient({ main: [flowResponse(step({ type: 'REDIRECT', data: { url } }))] });
      const navigate = vi.fn();
      await runActionFlow(client, action, {}, handlers({ navigate }));
      expect(navigate).toHaveBeenCalledWith(url);
    }
  });

  it('fails when data.url is missing', async () => {
    const { client } = fakeClient({ main: [flowResponse(step({ type: 'REDIRECT' }))] });
    await expect(runActionFlow(client, action, {}, handlers({ navigate: vi.fn() }))).rejects.toThrow(/data\.url/);
  });
});

describe('runActionFlow CALL (experimental)', () => {
  it('runs the named global action and resumes the flow with its response as the answer', async () => {
    const { client, calls } = fakeClient({
      main: [
        flowResponse(step({ type: 'CALL', flowId: 'outer', resumeToken: 'outer-tok', data: { action: 'lookup', q: 'abc' } })),
        flowResponse(step({ type: 'DONE', data: 'ok' })),
      ],
      global: { lookup: [flowResponse(step({ type: 'DONE', data: { id: 7 } }))] },
    });

    const result = await runActionFlow(client, action, { params: { a: 1 } }, handlers());

    expect(calls.global).toEqual([['lookup', { data: { q: 'abc' } }]]);
    expect(calls.main[1]).toMatchObject({
      params: { a: 1 },
      flowId: 'outer',
      resumeToken: 'outer-tok',
      data: { status: 'SUCCESS', statusCode: 200 },
    });
    expect((calls.main[1]!.data as Record<string, unknown>)['flow']).toBeUndefined();
    expect(result.flow?.data).toBe('ok');
  });

  it('routes to the entity endpoint when data.className is given', async () => {
    const { client, calls } = fakeClient({
      main: [
        flowResponse(step({ type: 'CALL', data: { action: 'edit', className: 'com.acme.Book' } })),
        flowResponse(step({ type: 'DONE' })),
      ],
      entity: { 'com.acme.Book/edit': [flowResponse(step({ type: 'DONE' }))] },
    });

    await runActionFlow(client, action, {}, handlers());

    expect(calls.entity).toEqual([['com.acme.Book', 'edit', { dataType: 'com.acme.Book' }]]);
    expect(calls.global).toHaveLength(0);
  });

  it('drives a nested flow (with its own steps) to completion before resuming', async () => {
    const confirm = vi.fn(async () => true);
    const { client, calls } = fakeClient({
      main: [
        flowResponse(step({ type: 'CALL', data: { action: 'nested' } })),
        flowResponse(step({ type: 'DONE' })),
      ],
      global: {
        nested: [
          flowResponse(step({ type: 'CONFIRM', flowId: 'inner', resumeToken: 'inner-tok', message: 'sure?' })),
          flowResponse(step({ type: 'DONE', data: 'nested-done' })),
        ],
      },
    });

    await runActionFlow(client, action, {}, handlers({ confirm }));

    expect(confirm).toHaveBeenCalledOnce();
    expect(calls.global[1]![1]).toMatchObject({ flowId: 'inner', resumeToken: 'inner-tok', data: true });
  });

  it('stops the outer flow when the nested flow redirects', async () => {
    const { client, calls } = fakeClient({
      main: [flowResponse(step({ type: 'CALL', data: { action: 'nested' } }))],
      global: { nested: [flowResponse(step({ type: 'REDIRECT', data: { url: '/done' } }))] },
    });
    const navigate = vi.fn();

    const result = await runActionFlow(client, action, {}, handlers({ navigate }));

    expect(navigate).toHaveBeenCalledWith('/done');
    expect(calls.main).toHaveLength(1);
    expect(result.flow?.type).toBe('REDIRECT');
  });

  it('fails fast on runaway CALL recursion', async () => {
    const loop = () => flowResponse(step({ type: 'CALL', data: { action: 'loop' } }));
    const { client } = fakeClient({
      main: [loop()],
      global: { loop: Array.from({ length: 20 }, loop) },
    });

    await expect(runActionFlow(client, action, {}, handlers())).rejects.toThrow(/nesting deeper/);
  });

  it('fails when data.action is missing', async () => {
    const { client } = fakeClient({ main: [flowResponse(step({ type: 'CALL', data: {} }))] });
    await expect(runActionFlow(client, action, {}, handlers())).rejects.toThrow(/data\.action/);
  });
});

describe('runActionFlow UPLOAD and downloads', () => {
  it('asks for files on an UPLOAD step and answers with them', async () => {
    const upload = flowResponse(step({ type: 'UPLOAD', data: { accept: '.json', multiple: false } }));
    const done = flowResponse(step({ type: 'DONE' }));
    const { client, calls } = fakeClient({ main: [upload, done] });
    const file = { name: 'report.json', contentType: 'application/json', content: 'e30=' };
    const pickFiles = vi.fn(async () => [file]);

    await runActionFlow(client, action, {}, handlers({ pickFiles }));

    expect(pickFiles).toHaveBeenCalledWith({ accept: '.json', multiple: false });
    expect(calls.main[1]!.data).toEqual([file]);
    expect(calls.main[1]!.resumeToken).toBe('tok');
  });

  it('answers with an empty list when the user cancels the picker', async () => {
    const { client, calls } = fakeClient({
      main: [flowResponse(step({ type: 'UPLOAD', data: { multiple: true } })), flowResponse(step({ type: 'DONE' }))],
    });

    await runActionFlow(client, action, {}, handlers({ pickFiles: async () => null }));

    expect(calls.main[1]!.data).toEqual([]);
  });

  it('gives params.downloads of the final response to saveFile', async () => {
    const file = { name: 'out.txt', contentType: 'text/plain', content: 'aGVsbG8=' };
    const final = { ...flowResponse(step({ type: 'DONE' })), params: { downloads: [file] } };
    const { client } = fakeClient({ main: [final] });
    const saveFile = vi.fn();

    await runActionFlow(client, action, {}, handlers({ saveFile }));

    expect(saveFile).toHaveBeenCalledWith(file);
  });
});

describe('runActionFlow DIALOG view class', () => {
  it('fetches the view of step.viewClass instead of the entity of the request', async () => {
    const dialog = flowResponse(step({ type: 'DIALOG', viewDescriptor: 'form', viewClass: 'x.AccountPayment', data: { amount: 5 } }));
    const done = flowResponse(step({ type: 'DONE' }));
    const { client: base, calls } = fakeClient({ main: [dialog, done] });
    const getEntityView = vi.fn(async () => ({ id: 'form', fields: [], view: 'form' }));
    const getEntity = vi.fn(async () => null);
    const client = { ...(base as object), metadata: { getEntityView, getEntity } } as never;
    const showFormDialog = vi.fn(async () => ({ amount: 10 }));

    await runActionFlow(client, action, { dataType: 'x.Account' }, handlers({ showFormDialog }), 'x.Account');

    expect(getEntityView).toHaveBeenCalledWith('x.AccountPayment', 'form');
    expect(calls.main[1]!.data).toEqual({ amount: 10 });
  });
});

describe('runActionFlow CHOICE and new window redirects', () => {
  it('answers a CHOICE step with the positions chosen', async () => {
    const choice = flowResponse(step({ type: 'CHOICE', title: 'Storage', data: { options: ['LOCAL', 'S3'], multiple: false } }));
    const { client, calls } = fakeClient({ main: [choice, flowResponse(step({ type: 'DONE' }))] });
    const choose = vi.fn(async () => [1]);

    await runActionFlow(client, action, {}, handlers({ choose }));

    expect(choose).toHaveBeenCalledWith({ title: 'Storage', options: ['LOCAL', 'S3'], multiple: false });
    expect(calls.main[1]!.data).toEqual([1]);
  });

  it('answers an empty list when the user cancels the choice', async () => {
    const { client, calls } = fakeClient({
      main: [flowResponse(step({ type: 'CHOICE', data: { options: ['A'], multiple: true } })), flowResponse(step({ type: 'DONE' }))],
    });

    await runActionFlow(client, action, {}, handlers({ choose: async () => null }));

    expect(calls.main[1]!.data).toEqual([]);
  });

  it('fails clearly when there is no choose handler', async () => {
    const { client } = fakeClient({ main: [flowResponse(step({ type: 'CHOICE', data: { options: [] } }))] });
    await expect(runActionFlow(client, action, {}, handlers())).rejects.toThrow(/choose/);
  });

  it('tells the navigate handler to open a new window', async () => {
    const redirect = flowResponse(step({ type: 'REDIRECT', data: { url: '/files/1/download', awaitReturn: false, newWindow: true } }));
    const { client } = fakeClient({ main: [redirect] });
    const navigate = vi.fn();

    await runActionFlow(client, action, {}, handlers({ navigate }));

    expect(navigate).toHaveBeenCalledWith('/files/1/download', { newWindow: true });
  });
});
