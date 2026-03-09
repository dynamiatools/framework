import { client } from './client';
import type { Book, BookReview, Category } from './types';
import type { CrudListResult, CrudQueryParams } from '@dynamia-tools/sdk';

export async function getBooks(params?: CrudQueryParams): Promise<CrudListResult<Book>> {
  return client.crud<Book>('library/books').findAll(params);
}

export async function getBook(id: number | string): Promise<Book> {
  return client.crud<Book>('library/books').findById(id);
}

export async function getCategories(): Promise<CrudListResult<Category>> {
  return client.crud<Category>('library/categories').findAll();
}

export async function getBookReviews(bookId: number | string): Promise<BookReview[]> {
  const result = await client.crud<BookReview>('library/books')
    .findAll({ 'book.id': bookId, pageSize: 50 })
    .catch(() => ({ content: [] as BookReview[], total: 0, page: 1, pageSize: 50, totalPages: 1 }));
  return result.content;
}

export function getCoverUrl(book: Book): string | null {
  if (!book.bookCover?.uuid || !book.bookCover?.filename) return null;
  return client.files.getUrl(book.bookCover.filename, book.bookCover.uuid);
}

export function formatPrice(price?: number, onSale?: boolean, salePrice?: number): string {
  if (!price) return 'Free';
  if (onSale && salePrice) {
    return `$${salePrice.toFixed(2)}`;
  }
  return `$${price.toFixed(2)}`;
}

export function stockLabel(status: Book['stockStatus']): { label: string; color: string } {
  switch (status) {
    case 'IN_STOCK':  return { label: 'Available',  color: 'green'  };
    case 'OUT_STOCK': return { label: 'Out of Stock', color: 'red'  };
    case 'PREORDER':  return { label: 'Pre-order',  color: 'amber' };
    default:          return { label: status,        color: 'gray'  };
  }
}

