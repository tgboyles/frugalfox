// API Response Types

export type Expense = {
  id: number;
  amount: number;
  category: string;
  merchant: string;
  date: string;
  bank?: string;
  createdAt: string;
  updatedAt: string;
};

export type PageableResponse<T> = {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
};

export type User = {
  id: number;
  username: string;
  createdAt: string;
  updatedAt: string;
};

export type AuthResponse = {
  token: string;
  username: string;
};

export type ErrorResponse = {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
};

export type UserResponse = {
  id: number;
  username: string;
  email: string;
  createdAt: string;
  updatedAt: string;
};

export type MessageResponse = {
  message: string;
};

export type ImportResult = {
  totalRows: number;
  successfulImports: number;
  failedImports: number;
  errors: string[];
};
