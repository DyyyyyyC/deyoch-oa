
import { test } from '@playwright/test';
import { expect } from '@playwright/test';

test('管理功能测试_2025-12-31', async ({ page, context }) => {
  
    // Navigate to URL
    await page.goto('http://localhost:3000/login', { waitUntil: 'networkidle' });

    // Fill input field
    await page.fill('.el-input__inner[placeholder="请输入用户名"]', 'admin');

    // Click element
    await page.click('text=用户管理');

    // Click element
    await page.click('.el-button--primary');

    // Fill input field
    await page.fill('input[placeholder="请输入用户名"]', 'testuser');

    // Fill input field
    await page.fill('input[placeholder="请输入密码"]', 'test123456');

    // Fill input field
    await page.fill('input[placeholder="请输入昵称"]', '测试用户');

    // Fill input field
    await page.fill('input[placeholder="请输入邮箱"]', 'test@example.com');

    // Fill input field
    await page.fill('input[placeholder="请输入电话"]', '13800138005');

    // Select option
    await page.selectOption('select', '2');

    // Click element
    await page.click('.el-select__input');

    // Click element
    await page.click('text=取消');

    // Click element
    await page.click('.el-table__body tr:first-child .el-button--primary');

    // Click element
    await page.click('text=取消');

    // Click element
    await page.click('.el-table__body tr:first-child .el-button--danger');

    // Click element
    await page.click('.el-message-box__btns button:last-child');
});