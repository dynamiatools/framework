import { describe, it, expect, beforeEach, vi } from 'vitest';
import type { ActionFlowStep } from '@dynamia-tools/sdk';
import {
  FlowStepRendererRegistry,
  registerFlowStepRenderer,
} from '../../src/actions/FlowStepRenderer.js';

// Helper: minimal CUSTOM ActionFlowStep
function customStep(component?: string): ActionFlowStep {
  return {
    flowId: 'flow-1',
    type: 'CUSTOM',
    data: component !== undefined ? { component } : undefined,
  };
}

describe('FlowStepRendererRegistry', () => {
  beforeEach(() => FlowStepRendererRegistry.clear());

  it('resolves a registered renderer by step.data.component (exact)', () => {
    const renderer = vi.fn();
    registerFlowStepRenderer('bookCoverPicker', renderer);
    expect(FlowStepRendererRegistry.resolve(customStep('bookCoverPicker'))).toBe(renderer);
  });

  it('resolves case-insensitively', () => {
    const renderer = vi.fn();
    registerFlowStepRenderer('bookCoverPicker', renderer);
    expect(FlowStepRendererRegistry.resolve(customStep('BOOKCOVERPICKER'))).toBe(renderer);
  });

  it('returns null for an unregistered component', () => {
    expect(FlowStepRendererRegistry.resolve(customStep('unknown'))).toBeNull();
  });

  it('returns null when the step carries no data.component', () => {
    expect(FlowStepRendererRegistry.resolve(customStep())).toBeNull();
  });

  it('calls the resolved renderer with the step and returns its result', async () => {
    const renderer = vi.fn().mockResolvedValue({ coverUrl: 'http://example.com/cover.png' });
    registerFlowStepRenderer('bookCoverPicker', renderer);
    const step = customStep('bookCoverPicker');

    const result = await FlowStepRendererRegistry.resolve(step)!(step);

    expect(renderer).toHaveBeenCalledWith(step);
    expect(result).toEqual({ coverUrl: 'http://example.com/cover.png' });
  });
});
