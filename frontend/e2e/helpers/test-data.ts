/**
 * Test data helpers for Playwright integration tests
 * These helpers manage test user creation and cleanup
 */

import { APIRequestContext } from '@playwright/test';

export interface TestUser {
  username: string;
  email: string;
  password: string;
  token?: string;
}

export interface TestExpense {
  id?: number;
  amount: number;
  category: string;
  merchant: string;
  date: string;
  bank?: string;
}

const API_BASE_URL = process.env.VITE_API_BASE_URL || 'http://localhost:8080';

/**
 * Generate a unique test username based on timestamp and random string
 */
export function generateTestUsername(): string {
  const timestamp = Date.now();
  const random = Math.random().toString(36).substring(2, 8);
  return `testuser_${timestamp}_${random}`;
}

/**
 * Register a new test user
 */
export async function registerTestUser(
  request: APIRequestContext,
  username?: string,
  password: string = 'TestPass123!',
  email?: string
): Promise<TestUser> {
  const testUsername = username || generateTestUsername();
  const testEmail = email || `${testUsername}@test.com`;

  const response = await request.post(`${API_BASE_URL}/auth/register`, {
    data: {
      username: testUsername,
      password: password,
      email: testEmail,
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to register test user: ${response.status()} ${body}`);
  }

  const data = await response.json();

  return {
    username: testUsername,
    email: testEmail,
    password: password,
    token: data.token,
  };
}

/**
 * Login as a test user
 */
export async function loginTestUser(
  request: APIRequestContext,
  username: string,
  password: string
): Promise<TestUser> {
  const response = await request.post(`${API_BASE_URL}/auth/login`, {
    data: {
      username,
      password,
    },
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to login test user: ${response.status()} ${body}`);
  }

  const data = await response.json();

  return {
    username,
    email: '',
    password,
    token: data.token,
  };
}

/**
 * Create a test expense for a user
 */
export async function createTestExpense(
  request: APIRequestContext,
  token: string,
  expense: Partial<TestExpense>
): Promise<TestExpense> {
  const expenseData: TestExpense = {
    amount: expense.amount || 50.0,
    category: expense.category || 'Food',
    merchant: expense.merchant || 'Test Merchant',
    date: expense.date || new Date().toISOString().split('T')[0],
    bank: expense.bank,
  };

  const response = await request.post(`${API_BASE_URL}/expenses`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    data: expenseData,
  });

  if (!response.ok()) {
    const body = await response.text();
    throw new Error(`Failed to create test expense: ${response.status()} ${body}`);
  }

  return await response.json();
}

/**
 * Delete all expenses for a user
 */
export async function deleteAllExpenses(
  request: APIRequestContext,
  token: string
): Promise<void> {
  // Get all expenses
  const response = await request.get(`${API_BASE_URL}/expenses?size=1000`, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  if (!response.ok()) {
    console.warn('Failed to fetch expenses for cleanup');
    return;
  }

  const data = await response.json();
  const expenses = data.content || [];

  // Delete each expense
  for (const expense of expenses) {
    await request.delete(`${API_BASE_URL}/expenses/${expense.id}`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
  }
}

/**
 * Create multiple test expenses
 */
export async function createMultipleTestExpenses(
  request: APIRequestContext,
  token: string,
  count: number
): Promise<TestExpense[]> {
  const expenses: TestExpense[] = [];
  const categories = ['Food', 'Transport', 'Entertainment', 'Shopping', 'Bills'];
  const merchants = ['Walmart', 'Amazon', 'Starbucks', 'Shell', 'Netflix'];

  for (let i = 0; i < count; i++) {
    const date = new Date();
    date.setDate(date.getDate() - i);

    const expense = await createTestExpense(request, token, {
      amount: Math.round((Math.random() * 100 + 10) * 100) / 100,
      category: categories[i % categories.length],
      merchant: merchants[i % merchants.length],
      date: date.toISOString().split('T')[0],
    });

    expenses.push(expense);
  }

  return expenses;
}
