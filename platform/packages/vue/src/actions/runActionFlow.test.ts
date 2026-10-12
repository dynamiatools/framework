import { describe, expect, it, vi } from 'vitest';
import type { ActionExecutionRequest, ActionExecutionResponse, ActionFlowStep, ActionMetadata } from '@dynamia-tools/sdk';
import { VueFormView } from '../views/VueFormView.js';
import { runActionFlow, type FlowStepHandlers } from './runActionFlow.js';

// The loop is tested in @dynamia-tools/ui-core (test/flow/runActionFlow.test.ts). Here: what is Vue specific.

const action = { id: 'main' } as ActionMetadata;

function step(partial: Partial<ActionFlowStep> & Pick<ActionFlowStep, 'type'>): ActionFlowStep {
  return { flowId: 'f1', resumeToken: 'tok', ...partial };
}

function flowResponse(s: ActionFlowStep): ActionExecutionResponse {
  return { status: s.type === 'DONE' ? 'SUCCESS' : 'PENDING', statusCode: 200, flow: s } as ActionExecutionResponse;
}

function clientFor(responses: ActionExecutionResponse[]) {
  const calls: ActionExecutionRequest[] = [];
  const client = {
    actions: {
      execute: vi.fn(async (_a: ActionMetadata, req: ActionExecutionRequest) => {
        calls.push(req);
        return responses.shift()!;
      }),
    },
    metadata: {
      getEntityView: vi.fn(async () => ({ id: 'form', fields: [], view: 'form' })),
      getEntity: vi.fn(async () => null),
    },
  };
  return { client: client as never, calls };
}

function handlers(overrides: Partial<FlowStepHandlers> = {}): FlowStepHandlers {
  return { confirm: vi.fn(async () => true), showToast: vi.fn(() => 't'), ...overrides };
}

describe('runActionFlow in Vue', () => {
  it('turns a DIALOG step into a VueFormView prefilled with the values and the field errors', async () => {
    const reopened = flowResponse(step({
      type: 'DIALOG', viewDescriptor: 'form', viewClass: 'x.Person', title: 'New person', data: { name: '' },
      message: 'Name is required', messageType: 'ERROR', fieldErrors: { name: 'Name is required' },
    }));
    const { client, calls } = clientFor([reopened, flowResponse(step({ type: 'DONE' }))]);
    const showFormDialog = vi.fn(async (options: { view: VueFormView }) => {
      expect(options.view).toBeInstanceOf(VueFormView);
      expect(options.view.errors.value).toEqual({ name: 'Name is required' });
      expect(options.view.values.value).toEqual({ name: '' });
      return { name: 'Ana' };
    });

    await runActionFlow(client, action, {}, handlers({ showFormDialog }), 'x.Person');

    expect(showFormDialog).toHaveBeenCalledWith(expect.objectContaining({
      title: 'New person', message: 'Name is required', messageType: 'ERROR',
    }));
    expect(calls[1]!.data).toEqual({ name: 'Ana' });
  });

  it('shows a VIEW step read only with the size the server hinted, and acknowledges it', async () => {
    const view = flowResponse(step({ type: 'VIEW', viewDescriptor: 'form', viewClass: 'x.Sale', data: {}, hints: { width: '60%', height: '400px' } }));
    const { client, calls } = clientFor([view, flowResponse(step({ type: 'DONE' }))]);
    const showFormDialog = vi.fn(async () => ({}));

    await runActionFlow(client, action, {}, handlers({ showFormDialog }), 'x.Sale');

    expect(showFormDialog).toHaveBeenCalledWith(expect.objectContaining({ width: '60%', height: '400px', readonly: true }));
    expect(calls[1]!.data).toBe(true);
  });

  it('fails clearly when a DIALOG step has no showFormDialog handler', async () => {
    const { client } = clientFor([flowResponse(step({ type: 'DIALOG', viewDescriptor: 'form', viewClass: 'x.Person' }))]);

    await expect(runActionFlow(client, action, {}, handlers(), 'x.Person')).rejects.toThrow(/showForm/);
  });

  it('delegates the rest of the protocol to ui-core', async () => {
    const { client, calls } = clientFor([flowResponse(step({ type: 'CONFIRM', message: 'Sure?' })), flowResponse(step({ type: 'DONE' }))]);
    const confirm = vi.fn(async () => true);

    await runActionFlow(client, action, {}, handlers({ confirm }));

    expect(confirm).toHaveBeenCalledWith({ message: 'Sure?' });
    expect(calls[1]).toMatchObject({ data: true, resumeToken: 'tok' });
  });
});
