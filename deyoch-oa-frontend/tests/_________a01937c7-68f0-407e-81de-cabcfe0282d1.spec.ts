
import { test } from '@playwright/test';
import { expect } from '@playwright/test';

test('公告管理功能测试_2025-12-31', async ({ page, context }) => {
  
    // Navigate to URL
    await page.goto('http://localhost:3000/announcement/list', { waitUntil: 'networkidle' });

    // Click element
    await page.click('.el-button--primary');

    // Fill input field
    await page.fill('.el-input__inner[placeholder="请输入公告标题"]', '测试公告');

    // Fill input field
    await page.fill('input[placeholder="请输入公告标题"]', '测试公告');

    // Click element
    await page.click('text=取消');

    // Fill input field
    await page.fill('.el-input__inner', '系统');

    // Click element
    await page.click('text=搜索');
});