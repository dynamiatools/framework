import path from 'node:path';
import { describe, expect, it } from 'vitest';
import { DocumentationCatalog } from '../src/catalog.js';
const ROOT = path.resolve(__dirname, '../../../../../');
describe('DocumentationCatalog', () => {
    it('loads docs from website and framework sources', async () => {
        const catalog = await DocumentationCatalog.fromWorkspace({ rootDir: ROOT });
        const websiteDocs = catalog.list({ source: 'website', limit: 500 });
        const frameworkDocs = catalog.list({ source: 'framework', limit: 500 });
        expect(websiteDocs.length).toBeGreaterThan(0);
        expect(frameworkDocs.length).toBeGreaterThan(0);
    });
    it('finds spanish docs and searches by keywords', async () => {
        const catalog = await DocumentationCatalog.fromWorkspace({ rootDir: ROOT });
        const spanishDocs = catalog.list({ source: 'website', locale: 'es', limit: 500 });
        expect(spanishDocs.length).toBeGreaterThan(0);
        const hits = catalog.search({ query: 'CrudPage module', source: 'website', locale: 'en', limit: 5 });
        expect(hits.length).toBeGreaterThan(0);
        expect(hits[0]?.score).toBeGreaterThan(0);
    });
    it('extracts a section by heading', async () => {
        const catalog = await DocumentationCatalog.fromWorkspace({ rootDir: ROOT });
        const doc = catalog.list({ source: 'website', locale: 'en', limit: 50 }).find((item) => item.id.includes('getting-started'));
        expect(doc).toBeDefined();
        const section = catalog.getSection(doc.id, 'Installation');
        expect(section).toBeTruthy();
        expect(section).toContain('Installation');
    });
});
//# sourceMappingURL=catalog.test.js.map