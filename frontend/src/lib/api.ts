import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add JWT token to requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor to handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear authentication data
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// API methods
export const authApi = {
  login: (username: string, password: string) => api.post('/auth/login', { username, password }),

  register: (username: string, password: string, email: string) =>
    api.post('/auth/register', { username, password, email }),
};

export const expenseApi = {
  getExpenses: (params?: {
    page?: number;
    size?: number;
    sort?: string;
    category?: string;
    merchant?: string;
    bank?: string;
    minAmount?: number;
    maxAmount?: number;
    startDate?: string;
    endDate?: string;
  }) => api.get('/expenses', { params }),

  getExpense: (id: number) => api.get(`/expenses/${id}`),

  createExpense: (expense: {
    amount: number;
    category: string;
    merchant: string;
    date: string;
    bank?: string;
  }) => api.post('/expenses', expense),

  updateExpense: (
    id: number,
    expense: {
      amount: number;
      category: string;
      merchant: string;
      date: string;
      bank?: string;
    }
  ) => api.put(`/expenses/${id}`, expense),

  deleteExpense: (id: number) => api.delete(`/expenses/${id}`),

  exportExpenses: (params?: {
    category?: string;
    merchant?: string;
    bank?: string;
    minAmount?: number;
    maxAmount?: number;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
    sort?: string;
  }) =>
    api.get('/expenses/export', {
      params,
      responseType: 'blob',
    }),
};

export const settingsApi = {
  getCurrentUser: () => api.get('/settings/user'),

  updateEmail: (email: string) => api.put('/settings/email', { email }),

  updatePassword: (currentPassword: string, newPassword: string) =>
    api.put('/settings/password', { currentPassword, newPassword }),
};
