import type { ApiVariable, EmailTemplatesApi, MigrationReport, TemplateData } from './api.js';
import { createBlock, emptyDesign } from './model/defaults.js';
import { insert, updateProps } from './model/tree.js';
import { serializeDesign } from './model/validate.js';
import type { Design } from './model/types.js';

/** A design with every kind of block, used by the demo page and the docs. */
export function sampleDesign(): Design {
  let d = emptyDesign();
  const root = (i: number) => ({ parentId: null, slot: 'children', index: i }) as const;
  d = insert(d, root(0), createBlock('header', { title: 'Invoice {{invoice.number}}' }));
  const columns = createBlock('columns');
  d = insert(d, root(1), columns);
  const [left, right] = columns.children ?? [];
  d = insert(d, { parentId: left!.id, slot: 'children', index: 0 }, createBlock('text', { html: '<p>Hello <strong>${customer.name}</strong>,</p><p>Here is your invoice for {{total|currency:0}}.</p>' }));
  d = insert(d, { parentId: right!.id, slot: 'children', index: 0 }, createBlock('image', { src: 'https://placehold.co/260x120/1a73e8/ffffff?text=Logo', alt: 'Logo' }));
  const lines = createBlock('repeat', { source: 'lines', alias: 'line' });
  d = insert(d, root(2), lines);
  d = insert(d, { parentId: lines.id, slot: 'children', index: 0 }, createBlock('text', { html: '<p>{{loop.number}}. {{line.name}} — {{line.price|currency:0}}</p>', padding: '2px 24px' }));
  const paid = createBlock('condition', { expression: 'paid' });
  d = insert(d, root(3), paid);
  d = insert(d, { parentId: paid.id, slot: 'children', index: 0 }, createBlock('text', { html: '<p><strong>Paid, thank you!</strong></p>' }));
  d = insert(d, { parentId: paid.id, slot: 'elseChildren', index: 0 }, createBlock('button', { text: 'Pay now', href: '{{payUrl}}' }));
  d = insert(d, root(4), createBlock('divider'));
  d = insert(d, root(5), createBlock('footer', { html: '<p>Company name · 123 Main St · <a href="{{unsubscribeUrl}}">Unsubscribe</a></p>' }));
  return updateProps(d, d.blocks[0]!.id, { backgroundColor: '#eaf1fe' });
}

const VARIABLES: ApiVariable[] = [
  { name: 'invoice.number', label: 'Invoice number', sample: 'F-1042' },
  { name: 'customer.name', label: 'Customer name', sample: 'Ada Lovelace' },
  { name: 'total', label: 'Total', sample: 1234567 },
  { name: 'paid', label: 'Already paid', sample: false },
  { name: 'payUrl', label: 'Payment link', sample: 'https://pay.example.com/F-1042' },
  { name: 'unsubscribeUrl', label: 'Unsubscribe link', sample: 'https://example.com/unsubscribe' },
  { name: 'lines', label: 'Invoice lines', sample: [{ name: 'Design', price: 800000 }, { name: 'Hosting', price: 434567 }] },
];

/**
 * An in-memory template API so the editor can be tried without a server (`?demo=1` in the standalone page). With
 * `velocity` the template starts as a Velocity one, to try the migration.
 */
export function createDemoApi(options: { velocity?: boolean } = {}): EmailTemplatesApi {
  let template: TemplateData = {
    id: 1,
    name: 'Invoice',
    subject: 'Invoice {{invoice.number}} for ${customer.name}',
    engine: options.velocity ? 'VELOCITY' : 'DYNAMIA',
    escapeValues: true,
    design: options.velocity ? null : serializeDesign(sampleDesign()),
    content: options.velocity ? '<html><body><p>Hello $customer.name</p></body></html>' : '<p/>',
    hasBackup: false,
    parentId: null,
  };
  const wait = <T>(value: T): Promise<T> => new Promise((resolve) => setTimeout(() => resolve(value), 120));
  return {
    get: () => wait({ ...template }),
    variables: () => wait(VARIABLES),
    saveDesign: (_id, body) => {
      template = { ...template, design: body.design, content: body.content, subject: body.subject ?? template.subject };
      return wait({ ...template });
    },
    testSend: () => wait({ sent: true, message: null }),
    migrate: (_id, dryRun) => {
      const report: MigrationReport = { alreadyMigrated: false, portable: true, applied: !dryRun, converted: {}, issues: [], warnings: ['Applications that evaluate the content themselves will not understand a migrated template.'] };
      if (!dryRun) {
        template = { ...template, engine: 'DYNAMIA', escapeValues: false, hasBackup: true, design: serializeDesign({ ...emptyDesign(), blocks: [{ id: 'html-1', type: 'html', props: { html: template.content, fullDocument: true } }] }) };
      }
      return wait(report);
    },
    revert: () => {
      template = { ...template, engine: 'VELOCITY', hasBackup: false, design: null };
      return wait({ reverted: true, designEdited: false, reason: null });
    },
  };
}
