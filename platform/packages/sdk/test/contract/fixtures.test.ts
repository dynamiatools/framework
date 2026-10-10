/// <reference types="node" />
import { readdirSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';
import {
  ACTION_FLOW_STEP_FIELDS,
  ACTION_FLOW_STEP_TYPES,
  UI_PORTS,
  type ActionExecutionRequest,
  type ActionExecutionResponse,
  type ActionFlowStep,
} from '../../src/index.js';

// The same JSON files the Java tests parse into their classes and write back (FixturesTest in ui-contract-generator).
const FIXTURES = join(__dirname, '../../../../contract/fixtures');

function load<T>(folder: string): [string, T][] {
  return readdirSync(join(FIXTURES, folder))
    .filter((name: string) => name.endsWith('.json'))
    .map((name: string) => [`${folder}/${name}`, JSON.parse(readFileSync(join(FIXTURES, folder, name), 'utf8')) as T]);
}

describe('contract fixtures shared with Java', () => {
  const steps = load<ActionFlowStep>('steps');

  it.each(steps)('%s is a step the generated types describe', (_name, step) => {
    expect(ACTION_FLOW_STEP_TYPES).toContain(step.type);
    for (const key of Object.keys(step)) {
      expect(ACTION_FLOW_STEP_FIELDS as readonly string[]).toContain(key);
    }
    expect(typeof step.flowId).toBe('string');
    expect(JSON.parse(JSON.stringify(step))).toEqual(step);
  });

  it('has a fixture for every type of step the Java protocol knows', () => {
    const covered = new Set(steps.map(([, step]) => step.type));
    expect([...ACTION_FLOW_STEP_TYPES].filter(type => !covered.has(type))).toEqual([]);
  });

  it.each(load<ActionExecutionRequest>('requests'))('%s is an answer with a resume token', (_name, request) => {
    expect(typeof request.resumeToken).toBe('string');
    expect(JSON.parse(JSON.stringify(request))).toEqual(request);
  });

  it.each(load<ActionExecutionResponse>('responses'))('%s is a response whose flow step is valid', (_name, response) => {
    if (response.flow) {
      expect(ACTION_FLOW_STEP_TYPES).toContain(response.flow.type);
    }
    expect(JSON.parse(JSON.stringify(response))).toEqual(response);
  });

  it('only lets a port produce steps that exist', () => {
    for (const port of Object.values(UI_PORTS)) {
      for (const step of port.steps) {
        expect(ACTION_FLOW_STEP_TYPES).toContain(step);
      }
    }
  });

  it('answers each choice and upload with the shape the server reads', () => {
    const choice = load<{ data: unknown }>('requests').find(([name]) => name.includes('choice'))![1];
    const upload = load<{ data: { ref: string }[] }>('requests').find(([name]) => name.includes('upload'))![1];
    expect(choice.data).toEqual(['s3']);
    expect(upload.data[0]).toHaveProperty('ref');
  });
});
