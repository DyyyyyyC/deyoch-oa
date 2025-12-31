
import { test } from '@playwright/test';
import { expect } from '@playwright/test';

test('OA系统测试_2025-12-31', async ({ page, context }) => {
  
    // Navigate to URL
    await page.goto('http://localhost:3000', { waitUntil: 'networkidle' });

    // Fill input field
    await page.fill('.el-input__inner[placeholder="请输入用户名"]', 'admin');

    // Fill input field
    await page.fill('.el-input__inner[placeholder="请输入密码"]', 'admin123');

    // Click element
    await page.click('.el-button--primary.login-btn');

    // Wait for response
    const dashboard-responseResponse = page.waitForResponse('http://localhost:3000/dashboard');

    // Click element
    await page.click('text=流程管理');

    // Click element
    await page.click('text=流程定义');

    // Wait for response
    const process-definition-responseResponse = page.waitForResponse('http://localhost:3000/process/definition');

    // Click element
    await page.click('text=流程实例管理');

    // Click element
    await page.click('text=流程实例');
});