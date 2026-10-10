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
  const uploads: File[] = [];
  const jobStatuses: { state: string; current: number; max: number }[] = [];
  const client = {
    jobs: {
      status: vi.fn(async (id: string) => ({ id, title: 'Moving', message: '', ...(jobStatuses.length > 1 ? jobStatuses.shift()! : jobStatuses[0]!) })),
    },
    transfers: {
      upload: vi.fn(async (file: File) => {
        uploads.push(file);
        return { ref: `ref-${uploads.length}`, name: file.name, contentType: file.type, size: file.size };
      }),
    },
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
  return { client: client as never, calls, uploads, jobStatuses };
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
  it('sends the picked files to the transfers endpoint and answers with their references only', async () => {
    const upload = flowResponse(step({ type: 'UPLOAD', data: { accept: '.json', multiple: false, maxFileSize: 1000 } }));
    const done = flowResponse(step({ type: 'DONE' }));
    const { client, calls, uploads } = fakeClient({ main: [upload, done] });
    const file = new File(['{}'], 'report.json', { type: 'application/json' });
    const pickFiles = vi.fn(async () => [file]);

    await runActionFlow(client, action, {}, handlers({ pickFiles }));

    expect(pickFiles).toHaveBeenCalledWith({ accept: '.json', multiple: false });
    expect(uploads).toEqual([file]);
    expect(calls.main[1]!.data).toEqual([{ ref: 'ref-1' }]);
    expect(calls.main[1]!.resumeToken).toBe('tok');
  });

  it('answers with an empty list when the user cancels the picker', async () => {
    const { client, calls, uploads } = fakeClient({
      main: [flowResponse(step({ type: 'UPLOAD', data: { multiple: true } })), flowResponse(step({ type: 'DONE' }))],
    });

    await runActionFlow(client, action, {}, handlers({ pickFiles: async () => null }));

    expect(calls.main[1]!.data).toEqual([]);
    expect(uploads).toHaveLength(0);
  });

  it('refuses, before sending anything, files that break the limits of the step', async () => {
    const data = { accept: '.json', multiple: true, maxFiles: 1, maxFileSize: 5 };
    const pick = (files: File[]) => handlers({ pickFiles: async () => files });
    const exec = (files: File[]) => {
      const { client, uploads } = fakeClient({ main: [flowResponse(step({ type: 'UPLOAD', data })), flowResponse(step({ type: 'DONE' }))] });
      return { run: runActionFlow(client, action, {}, pick(files)), uploads };
    };

    const tooBig = exec([new File(['0123456789'], 'a.json')]);
    await expect(tooBig.run).rejects.toThrow(/limit/);
    const wrongType = exec([new File(['x'], 'a.exe')]);
    await expect(wrongType.run).rejects.toThrow(/accepted type/);
    const tooMany = exec([new File(['x'], 'a.json'), new File(['x'], 'b.json')]);
    await expect(tooMany.run).rejects.toThrow(/At most 1/);
    expect(tooBig.uploads.length + wrongType.uploads.length + tooMany.uploads.length).toBe(0);
  });

  it('gives params.downloads of the final response to saveFile with the client', async () => {
    const file = { name: 'out.txt', contentType: 'text/plain', size: 5, url: '/api/app/transfers/abc' };
    const final = { ...flowResponse(step({ type: 'DONE' })), params: { downloads: [file] } };
    const { client } = fakeClient({ main: [final] });
    const saveFile = vi.fn();

    await runActionFlow(client, action, {}, handlers({ saveFile }));

    expect(saveFile).toHaveBeenCalledWith(file, client);
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
  it('answers a CHOICE step with the keys chosen', async () => {
    const choice = flowResponse(step({ type: 'CHOICE', title: 'Storage', data: { options: [{ key: 'l', label: 'LOCAL' }, { key: 's', label: 'S3' }], multiple: false } }));
    const { client, calls } = fakeClient({ main: [choice, flowResponse(step({ type: 'DONE' }))] });
    const choose = vi.fn(async () => ['s']);

    await runActionFlow(client, action, {}, handlers({ choose }));

    expect(choose).toHaveBeenCalledWith({ title: 'Storage', options: [{ key: 'l', label: 'LOCAL' }, { key: 's', label: 'S3' }], multiple: false });
    expect(calls.main[1]!.data).toEqual(['s']);
  });

  it('answers an empty list when the user cancels the choice', async () => {
    const { client, calls } = fakeClient({
      main: [flowResponse(step({ type: 'CHOICE', data: { options: [{ key: 'a', label: 'A' }], multiple: true } })), flowResponse(step({ type: 'DONE' }))],
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

describe('runActionFlow VIEW', () => {
  it('shows the view read only and acknowledges it', async () => {
    const view = flowResponse(step({ type: 'VIEW', viewDescriptor: 'form', viewClass: 'x.Sale', data: { total: 5 }, title: 'Sale 1' }));
    const { client: base, calls } = fakeClient({ main: [view, flowResponse(step({ type: 'DONE' }))] });
    const client = {
      ...(base as object),
      metadata: { getEntityView: vi.fn(async () => ({ id: 'form', fields: [], view: 'form' })), getEntity: vi.fn(async () => null) },
    } as never;
    const showFormDialog = vi.fn(async () => ({}));

    await runActionFlow(client, action, { dataType: 'x.Sale' }, handlers({ showFormDialog }), 'x.Sale');

    expect(showFormDialog).toHaveBeenCalledWith(expect.objectContaining({ title: 'Sale 1', readonly: true }));
    expect(calls.main[1]!.data).toBe(true);
  });
});

describe('runActionFlow VIEW size hints', () => {
  it('hands the width and height of the server to the dialog', async () => {
    const view = flowResponse(step({ type: 'VIEW', viewDescriptor: 'form', viewClass: 'x.Sale', data: {}, hints: { width: '60%', height: '400px' } }));
    const { client: base } = fakeClient({ main: [view, flowResponse(step({ type: 'DONE' }))] });
    const client = { ...(base as object), metadata: { getEntityView: vi.fn(async () => ({ id: 'form', fields: [], view: 'form' })), getEntity: vi.fn(async () => null) } } as never;
    const showFormDialog = vi.fn(async () => ({}));

    await runActionFlow(client, action, {}, handlers({ showFormDialog }), 'x.Sale');

    expect(showFormDialog).toHaveBeenCalledWith(expect.objectContaining({ width: '60%', height: '400px', readonly: true }));
  });
});

describe('runActionFlow DIALOG shown again after a validation error', () => {
  it('hands the message and the field errors to the form so the user sees what failed', async () => {
    const reopened = flowResponse(step({
      type: 'DIALOG', viewDescriptor: 'form', viewClass: 'x.Person', data: { name: '' },
      message: 'Name is required', messageType: 'ERROR', fieldErrors: { name: 'Name is required' },
    }));
    const { client: base, calls } = fakeClient({ main: [reopened, flowResponse(step({ type: 'DONE' }))] });
    const getEntityView = vi.fn(async () => ({ id: 'form', fields: [], view: 'form' }));
    const client = { ...(base as object), metadata: { getEntityView, getEntity: vi.fn(async () => null) } } as never;
    const showFormDialog = vi.fn(async (options: { view: { errors: { value: Record<string, string> }; values: { value: Record<string, unknown> } } }) => {
      expect(options.view.errors.value).toEqual({ name: 'Name is required' });
      expect(options.view.values.value).toEqual({ name: '' });
      return { name: 'Ana' };
    });

    await runActionFlow(client, action, {}, handlers({ showFormDialog: showFormDialog as never }), 'x.Person');

    expect(showFormDialog).toHaveBeenCalledWith(expect.objectContaining({ message: 'Name is required', messageType: 'ERROR' }));
    expect(calls.main[1]!.data).toEqual({ name: 'Ana' });
  });
});

describe('runActionFlow PROGRESS', () => {
  it('follows the job until it stops and answers with its id and state', async () => {
    const progress = flowResponse(step({ type: 'PROGRESS', title: 'Moving', data: { title: 'Moving', jobId: 'job-1' } }));
    const { client, calls, jobStatuses } = fakeClient({ main: [progress, flowResponse(step({ type: 'DONE' }))] });
    jobStatuses.push({ state: 'RUNNING', current: 1, max: 4 }, { state: 'RUNNING', current: 3, max: 4 }, { state: 'DONE', current: 4, max: 4 });
    const shown: string[] = [];

    await runActionFlow(client, action, {}, handlers({
      progressIntervalMs: 0,
      showProgress: status => shown.push(`${status.state} ${status.current}/${status.max}`),
    }));

    expect(shown).toEqual(['RUNNING 1/4', 'RUNNING 3/4', 'DONE 4/4']);
    expect(calls.main[1]!.data).toEqual({ jobId: 'job-1', state: 'DONE' });
    expect(calls.main[1]!.resumeToken).toBe('tok');
  });

  it('answers FAILED when the job failed so the action can handle the error', async () => {
    const progress = flowResponse(step({ type: 'PROGRESS', data: { jobId: 'job-2' } }));
    const { client, calls, jobStatuses } = fakeClient({ main: [progress, flowResponse(step({ type: 'DONE' }))] });
    jobStatuses.push({ state: 'FAILED', current: 0, max: 0 });

    await runActionFlow(client, action, {}, handlers({ progressIntervalMs: 0 }));

    expect(calls.main[1]!.data).toEqual({ jobId: 'job-2', state: 'FAILED' });
  });

  it('fails when the step carries no job', async () => {
    const { client } = fakeClient({ main: [flowResponse(step({ type: 'PROGRESS', data: {} }))] });

    await expect(runActionFlow(client, action, {}, handlers())).rejects.toThrow(/jobId/);
  });
});
