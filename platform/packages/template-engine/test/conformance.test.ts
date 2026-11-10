/// <reference types="node" />
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { describe, expect, it } from 'vitest';
import { DynamiaTemplateEngine } from '../src/index.js';

// The same file the Java engine runs (TemplateConformanceTest in platform/core/templates). Java is the reference.
const FILE = join(__dirname, '../../../contract/fixtures/templates/conformance.json');

interface Case {
  group: string;
  name: string;
  template: string;
  data: Record<string, unknown>;
  locale?: string;
  expected: string;
}

const { cases } = JSON.parse(readFileSync(FILE, 'utf8')) as { cases: Case[] };
const engine = new DynamiaTemplateEngine();

describe('template conformance with the Java engine', () => {
  it('has cases', () => {
    expect(cases.length).toBeGreaterThan(50);
  });

  for (const c of cases) {
    it(`${c.group}: ${c.name}`, () => {
      const data = c.locale ? { ...c.data, locale: c.locale } : c.data;
      expect(engine.evaluate(c.template, data)).toBe(c.expected);
    });
  }
});
