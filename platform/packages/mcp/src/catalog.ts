import { promises as fs } from 'node:fs';
import path from 'node:path';
import { extractSection, parseMarkdown } from './markdown.js';
import type { DocRecord, DocSourceType, IndexOptions, SearchHit, SearchOptions } from './types.js';

const DOC_EXTENSIONS = new Set(['.md', '.mdx']);

interface RawDocSource {
  type: DocSourceType;
  rootPath: string;
  isFile: boolean;
}

export class DocumentationCatalog {
  private readonly records = new Map<string, DocRecord>();

  static async fromWorkspace(options: IndexOptions): Promise<DocumentationCatalog> {
    const catalog = new DocumentationCatalog();
    await catalog.load(options.rootDir);
    return catalog;
  }

  list(filters?: { source?: DocSourceType; locale?: string; limit?: number; offset?: number }): DocRecord[] {
    const source = filters?.source;
    const locale = filters?.locale;
    const limit = Math.max(1, Math.min(200, filters?.limit ?? 100));
    const offset = Math.max(0, filters?.offset ?? 0);

    return Array.from(this.records.values())
      .filter((doc) => (source ? doc.source === source : true))
      .filter((doc) => (locale ? doc.locale === locale : true))
      .sort((a, b) => a.id.localeCompare(b.id))
      .slice(offset, offset + limit);
  }

  get(id: string): DocRecord | undefined {
    return this.records.get(id);
  }

  getSection(id: string, heading: string): string | null {
    const record = this.records.get(id);
    if (!record) {
      return null;
    }

    return extractSection(record.content, heading);
  }

  search(options: SearchOptions): SearchHit[] {
    const source = options.source;
    const locale = options.locale;
    const query = options.query.trim().toLowerCase();
    const limit = Math.max(1, Math.min(50, options.limit ?? 8));

    if (!query) {
      return [];
    }

    const terms = query.split(/\s+/).filter(Boolean);

    const hits: SearchHit[] = [];
    for (const doc of this.records.values()) {
      if (source && doc.source !== source) {
        continue;
      }
      if (locale && doc.locale !== locale) {
        continue;
      }

      const haystack = `${doc.title}\n${doc.description ?? ''}\n${doc.content}`.toLowerCase();
      let score = 0;

      for (const term of terms) {
        const titleWeight = countOccurrences(doc.title.toLowerCase(), term) * 10;
        const descriptionWeight = countOccurrences((doc.description ?? '').toLowerCase(), term) * 4;
        const contentWeight = countOccurrences(haystack, term);
        score += titleWeight + descriptionWeight + contentWeight;
      }

      if (score === 0) {
        continue;
      }

      hits.push({
        id: doc.id,
        source: doc.source,
        locale: doc.locale,
        title: doc.title,
        relativePath: doc.relativePath,
        score,
        excerpt: buildExcerpt(doc.content, terms[0]),
      });
    }

    return hits.sort((a, b) => b.score - a.score).slice(0, limit);
  }

  private async load(rootDir: string): Promise<void> {
    const sources = buildSources(rootDir);

    for (const source of sources) {
      if (!(await exists(source.rootPath))) {
        continue;
      }

      if (source.isFile) {
        const entry = await this.loadFile(source.type, source.rootPath, path.dirname(source.rootPath));
        if (entry) {
          this.records.set(entry.id, entry);
        }
        continue;
      }

      const files = await walkFiles(source.rootPath);
      for (const file of files) {
        if (!DOC_EXTENSIONS.has(path.extname(file).toLowerCase())) {
          continue;
        }

        const entry = await this.loadFile(source.type, file, source.rootPath);
        if (entry) {
          this.records.set(entry.id, entry);
        }
      }
    }
  }

  private async loadFile(
    sourceType: DocSourceType,
    absolutePath: string,
    sourceRootPath: string,
  ): Promise<DocRecord | null> {
    const stat = await fs.stat(absolutePath);
    if (!stat.isFile()) {
      return null;
    }

    const raw = await fs.readFile(absolutePath, 'utf8');
    const relativePath = path.relative(sourceRootPath, absolutePath).replaceAll(path.sep, '/');

    const locale = inferLocale(sourceType, relativePath);
    const fallbackTitle = path.basename(absolutePath).replace(/\.(md|mdx)$/i, '');
    const parsed = parseMarkdown(raw, fallbackTitle);

    const slug = relativePath.replace(/\.(md|mdx)$/i, '');
    const id = `${sourceType}:${locale}:${slug}`;

    return {
      id,
      source: sourceType,
      locale,
      title: parsed.title,
      description: parsed.description,
      relativePath,
      absolutePath,
      updatedAt: stat.mtime.toISOString(),
      content: parsed.body,
      headings: parsed.headings,
    };
  }
}

function buildSources(rootDir: string): RawDocSource[] {
  return [
    {
      type: 'website',
      rootPath: path.join(rootDir, 'website', 'src', 'content', 'docs'),
      isFile: false,
    },
    {
      type: 'framework',
      rootPath: path.join(rootDir, 'framework', 'docs'),
      isFile: false,
    },
    {
      type: 'buckie',
      rootPath: path.join(rootDir, 'buckie', 'buckie-node-js', 'README.md'),
      isFile: true,
    },
    {
      type: 'buckie',
      rootPath: path.join(rootDir, 'buckie', 'buckie-php', 'README.md'),
      isFile: true,
    },
  ];
}

function inferLocale(sourceType: DocSourceType, relativePath: string): string {
  if (sourceType === 'framework' || sourceType === 'buckie') {
    return 'en';
  }

  const firstSegment = relativePath.split('/')[0]?.toLowerCase() || '';
  return firstSegment === 'es' ? 'es' : 'en';
}

async function walkFiles(dir: string): Promise<string[]> {
  const dirents = await fs.readdir(dir, { withFileTypes: true });
  const files: string[] = [];

  for (const entry of dirents) {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      files.push(...(await walkFiles(fullPath)));
      continue;
    }

    files.push(fullPath);
  }

  return files;
}

async function exists(targetPath: string): Promise<boolean> {
  try {
    await fs.access(targetPath);
    return true;
  } catch {
    return false;
  }
}

function countOccurrences(text: string, term: string): number {
  if (!term) {
    return 0;
  }

  let count = 0;
  let start = 0;

  while (true) {
    const foundAt = text.indexOf(term, start);
    if (foundAt === -1) {
      return count;
    }

    count += 1;
    start = foundAt + term.length;
  }
}

function buildExcerpt(markdownBody: string, firstTerm: string | undefined): string {
  const plain = markdownBody.replace(/[#*_`>\-]/g, ' ').replace(/\s+/g, ' ').trim();
  if (!plain) {
    return '';
  }

  if (!firstTerm) {
    return plain.slice(0, 240);
  }

  const lowered = plain.toLowerCase();
  const index = lowered.indexOf(firstTerm.toLowerCase());
  if (index === -1) {
    return plain.slice(0, 240);
  }

  const start = Math.max(0, index - 80);
  const end = Math.min(plain.length, index + 160);
  return plain.slice(start, end).trim();
}

