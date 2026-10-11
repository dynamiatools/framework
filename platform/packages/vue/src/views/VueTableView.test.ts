import { describe, expect, it } from 'vitest';
import type { ViewDescriptor } from '@dynamia-tools/sdk';
import { VueTableView } from './VueTableView.js';

const descriptor: ViewDescriptor = {
  id: 'movie-table',
  beanClass: 'Movie',
  view: 'table',
  fields: [
    { name: 'title', fieldClass: 'java.lang.String', visible: true },
    { name: 'year', fieldClass: 'int', visible: true },
    { name: 'secret', fieldClass: 'java.lang.String', visible: false },
  ],
  params: {},
};

describe('VueTableView', () => {
  it('exposes the resolved columns through the TableView API once initialised', async () => {
    const view = new VueTableView(descriptor, null);
    expect(view.getResolvedColumns()).toEqual([]);

    await view.initialize();

    expect(view.getResolvedColumns().map((c) => c.name)).toEqual(['title', 'year']);
    expect(view.getResolvedColumns()).toBe(view.columns.value);
  });
});
