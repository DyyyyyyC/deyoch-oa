// OA系统编辑和删除功能测试脚本
// 测试所有管理模块的编辑和删除功能
import { test, expect } from '@playwright/test';

// 测试配置
const config = {
  baseURL: 'http://localhost:3001', // 使用当前运行的开发服务器端口
  username: 'admin',
  password: 'admin123',
  timeout: 15000
};

// 登录测试
async function login(page) {
  await page.goto('/login');
  // 等待页面加载完成
  await page.waitForLoadState('networkidle');
  
  // 等待用户名输入框可见（Element Plus组件生成的输入框）
  await page.waitForSelector('.el-input__inner[placeholder="请输入用户名"]', { visible: true });
  await page.fill('.el-input__inner[placeholder="请输入用户名"]', config.username);
  
  // 等待密码输入框可见
  await page.waitForSelector('.el-input__inner[placeholder="请输入密码"]', { visible: true });
  await page.fill('.el-input__inner[placeholder="请输入密码"]', config.password);
  
  // 等待登录按钮可见并点击
  await page.waitForSelector('.el-button--primary.login-btn', { visible: true });
  await page.click('.el-button--primary.login-btn');
  
  // 验证登录成功，等待URL变化
  await page.waitForURL('/dashboard', { timeout: config.timeout * 2 });
  await expect(page).toHaveURL('/dashboard');
}

// 通用编辑功能测试
async function testEditFunction(page, modulePath, moduleName) {
  await page.goto(modulePath);
  await page.waitForLoadState('networkidle');
  
  // 验证页面加载成功
  await expect(page.locator('h2')).toContainText(new RegExp(moduleName));
  
  // 等待表格加载完成
  await page.waitForSelector('.el-table__body tr', { visible: true, timeout: config.timeout });
  
  // 获取表格行数
  const rows = page.locator('.el-table__body tr');
  const rowCount = await rows.count();
  
  if (rowCount > 0) {
    // 点击第一条数据的编辑按钮
    const firstRow = rows.nth(0);
    const editButton = firstRow.locator('.el-button--primary');
    
    if (await editButton.isVisible()) {
      await editButton.click();
      
      // 等待表单弹窗可见
      await page.waitForSelector('.el-dialog', { visible: true, timeout: config.timeout });
      
      // 验证表单元素存在
      await expect(page.locator('.el-dialog')).toBeVisible();
      
      // 关闭弹窗
      await page.click('.el-dialog__footer button:not(.el-button--primary)');
      
      // 验证弹窗关闭
      await expect(page.locator('.el-dialog')).not.toBeVisible();
      return true;
    }
  }
  return false;
}

// 通用删除功能测试
async function testDeleteFunction(page, modulePath, moduleName) {
  await page.goto(modulePath);
  await page.waitForLoadState('networkidle');
  
  // 等待表格加载完成
  await page.waitForSelector('.el-table__body tr', { visible: true, timeout: config.timeout });
  
  // 获取表格行数
  const rows = page.locator('.el-table__body tr');
  const rowCount = await rows.count();
  
  if (rowCount > 0) {
    // 点击第一条数据的删除按钮
    const firstRow = rows.nth(0);
    const deleteButton = firstRow.locator('.el-button--danger');
    
    if (await deleteButton.isVisible()) {
      await deleteButton.click();
      
      // 等待确认对话框可见
      await page.waitForSelector('.el-message-box', { visible: true, timeout: config.timeout });
      
      // 验证对话框存在
      await expect(page.locator('.el-message-box')).toBeVisible();
      
      // 点击取消按钮
      const cancelButton = page.locator('.el-message-box__btns button:last-child');
      await cancelButton.waitFor({ visible: true });
      await cancelButton.click();
      
      // 验证对话框关闭
      await expect(page.locator('.el-message-box')).not.toBeVisible();
      return true;
    }
  }
  return false;
}

// 测试套件
test.describe('OA系统编辑和删除功能测试', () => {
  let page;
  let context;

  // 每个测试前登录
  test.beforeEach(async ({ browser }) => {
    // 创建上下文时配置baseURL
    context = await browser.newContext({
      baseURL: config.baseURL
    });
    page = await context.newPage();
    await login(page);
  });

  // 测试后关闭上下文
  test.afterEach(async () => {
    await context.close();
  });

  // 测试用户管理 - 编辑功能
  test('测试用户管理编辑功能', async () => {
    await testEditFunction(page, '/system/user', '用户管理|User Management');
  });

  // 测试用户管理 - 删除功能
  test('测试用户管理删除功能', async () => {
    await testDeleteFunction(page, '/system/user', '用户管理|User Management');
  });

  // 测试角色管理 - 编辑功能
  test('测试角色管理编辑功能', async () => {
    await testEditFunction(page, '/system/role', '角色管理|Role Management');
  });

  // 测试角色管理 - 删除功能
  test('测试角色管理删除功能', async () => {
    await testDeleteFunction(page, '/system/role', '角色管理|Role Management');
  });

  // 测试权限管理 - 编辑功能
  test('测试权限管理编辑功能', async () => {
    await testEditFunction(page, '/system/permission', '权限管理|Permission Management');
  });

  // 测试权限管理 - 删除功能
  test('测试权限管理删除功能', async () => {
    await testDeleteFunction(page, '/system/permission', '权限管理|Permission Management');
  });

  // 测试公告管理 - 编辑功能
  test('测试公告管理编辑功能', async () => {
    await testEditFunction(page, '/announcement/list', '公告管理|Announcement Management');
  });

  // 测试公告管理 - 删除功能
  test('测试公告管理删除功能', async () => {
    await testDeleteFunction(page, '/announcement/list', '公告管理|Announcement Management');
  });

  // 测试任务管理 - 编辑功能
  test('测试任务管理编辑功能', async () => {
    await testEditFunction(page, '/task/list', '任务管理|Task Management');
  });

  // 测试任务管理 - 删除功能
  test('测试任务管理删除功能', async () => {
    await testDeleteFunction(page, '/task/list', '任务管理|Task Management');
  });

  // 测试日程管理 - 编辑功能
  test('测试日程管理编辑功能', async () => {
    await testEditFunction(page, '/schedule/list', '日程管理|Schedule Management');
  });

  // 测试日程管理 - 删除功能
  test('测试日程管理删除功能', async () => {
    await testDeleteFunction(page, '/schedule/list', '日程管理|Schedule Management');
  });

  // 测试流程定义 - 编辑功能
  test('测试流程定义编辑功能', async () => {
    await testEditFunction(page, '/process/definition', '流程管理|Process Management');
  });

  // 测试流程定义 - 删除功能
  test('测试流程定义删除功能', async () => {
    await testDeleteFunction(page, '/process/definition', '流程管理|Process Management');
  });

  // 测试流程实例 - 编辑功能（跳过，因为流程实例可能是只读的）
  test.skip('测试流程实例编辑功能', async () => {
    await testEditFunction(page, '/process/instance', '流程管理|Process Management');
  });

  // 测试流程实例 - 删除功能
  test('测试流程实例删除功能', async () => {
    await testDeleteFunction(page, '/process/instance', '流程管理|Process Management');
  });
});
