/**
 * Smoke Test - Basic connectivity and setup verification
 * This test ensures the test infrastructure is working correctly
 */

import { test, expect } from '@playwright/test';

test.describe('Smoke Tests', () => {
  test('should load the frontend application', async ({ page }) => {
    await page.goto('/');
    
    // Should redirect to login or dashboard
    await expect(page).toHaveURL(/\/(login|dashboard)/);
  });

  test('should connect to backend API', async ({ request }) => {
    // Check if backend health endpoint is accessible
    const response = await request.get('http://localhost:8080/actuator/health');
    
    expect(response.ok()).toBeTruthy();
    
    const data = await response.json();
    expect(data.status).toBe('UP');
  });

  test('should be able to register a new user', async ({ page, request }) => {
    const username = `smoketest_${Date.now()}`;
    const password = 'SmokeTest123!';
    const email = `${username}@test.com`;

    const response = await request.post('http://localhost:8080/auth/register', {
      data: {
        username,
        password,
        email,
      },
    });

    expect(response.ok()).toBeTruthy();
    
    const data = await response.json();
    expect(data.token).toBeTruthy();
    expect(data.username).toBe(username);
  });
});
