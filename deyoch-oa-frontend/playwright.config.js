// playwright.config.js
import { defineConfig } from '@playwright/test';

export default defineConfig({
  // 超时设置
  timeout: 30000,
  expect: {
    timeout: 10000
  },
  // 测试文件配置
  testDir: './tests',
  // 浏览器配置
  projects: [
    {
      name: 'chromium',
      use: {
        channel: 'chrome',
        // 视口大小
        viewport: { width: 1920, height: 1080 },
        // 关闭Headless模式以便观察测试过程
        headless: false,
        // 捕获视频和截图
        video: 'on-first-retry',
        screenshot: 'only-on-failure',
      },
    },
  ],
  // 报告配置
  reporter: [['html', { open: 'never' }]],
});
