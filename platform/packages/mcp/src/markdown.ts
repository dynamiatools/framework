import matter from 'gray-matter';

export interface ParsedMarkdown {
  title: string;
  description?: string;
  body: string;
  headings: string[];
}

export function parseMarkdown(raw: string, fallbackTitle: string): ParsedMarkdown {
  const parsed = matter(raw);
  const data = parsed.data as Record<string, unknown>;
  const titleFromFrontmatter = typeof data.title === 'string' ? data.title.trim() : undefined;
  const description = typeof data.description === 'string' ? data.description.trim() : undefined;
  const body = parsed.content;

  const headings = body
    .split(/\r?\n/)
    .map((line) => line.trim())
    .filter((line) => line.startsWith('#'))
    .map((line) => line.replace(/^#+\s*/, '').trim())
    .filter(Boolean);

  return {
    title: titleFromFrontmatter || fallbackTitle,
    description,
    body,
    headings,
  };
}

export function extractSection(markdownBody: string, heading: string): string | null {
  const lines = markdownBody.split(/\r?\n/);
  const normalizedTarget = heading.trim().toLowerCase();

  let start = -1;
  let startLevel = 0;

  for (let index = 0; index < lines.length; index += 1) {
    const line = lines[index]?.trim() || '';
    const match = /^(#{1,6})\s+(.+)$/.exec(line);
    if (!match) {
      continue;
    }

    const level = match[1].length;
    const text = match[2].trim().toLowerCase();

    if (text === normalizedTarget) {
      start = index;
      startLevel = level;
      break;
    }
  }

  if (start === -1) {
    return null;
  }

  let end = lines.length;
  for (let index = start + 1; index < lines.length; index += 1) {
    const line = lines[index]?.trim() || '';
    const match = /^(#{1,6})\s+(.+)$/.exec(line);
    if (!match) {
      continue;
    }

    const level = match[1].length;
    if (level <= startLevel) {
      end = index;
      break;
    }
  }

  return lines.slice(start, end).join('\n').trim();
}

