export type DocSourceType = 'website' | 'framework' | 'buckie';

export interface DocRecord {
  id: string;
  source: DocSourceType;
  locale: string;
  title: string;
  description?: string;
  relativePath: string;
  absolutePath: string;
  updatedAt: string;
  content: string;
  headings: string[];
}

export interface IndexOptions {
  rootDir: string;
}

export interface SearchOptions {
  query: string;
  source?: DocSourceType;
  locale?: string;
  limit?: number;
}

export interface SearchHit {
  id: string;
  source: DocSourceType;
  locale: string;
  title: string;
  relativePath: string;
  score: number;
  excerpt: string;
}

