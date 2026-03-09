// Domain types matching mybookstore Java entities

export interface Category {
  id: number;
  name: string;
  description?: string;
}

export interface BookCover {
  id: number;
  uuid: string;
  filename: string;
  extension?: string;
}

export type StockStatus = 'IN_STOCK' | 'OUT_STOCK' | 'PREORDER';

export interface Book {
  id: number;
  title: string;
  sinopsys?: string;
  year: number;
  isbn: string;
  category?: Category;
  price?: number;
  salePrice?: number;
  onSale: boolean;
  discount: number;
  stockStatus: StockStatus;
  publishDate?: string;
  bookCover?: BookCover;
}

export interface BookReview {
  id: number;
  user: string;
  comment: string;
  stars: number;
}

export interface Customer {
  id: number;
  name: string;
  email?: string;
}

export interface InvoiceDetail {
  id: number;
  book?: Book;
  quantity: number;
  price: number;
  total: number;
}

export interface Invoice {
  id: number;
  customer?: Customer;
  total: number;
  details?: InvoiceDetail[];
}

