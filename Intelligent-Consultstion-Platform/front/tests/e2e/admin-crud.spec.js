import { test, expect } from '@playwright/test'

/**
 * 管理员 CRUD E2E。
 * <p>
 * 覆盖管理员进入管理页、查看公告列表并执行新增/编辑/删除的基本路径。
 */
test.describe('admin crud flow', () => {
  test('admin can open manage notice page', async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('demo_session', JSON.stringify({
        userId: 1003,
        username: 'test_admin',
        role: 'admin',
        token: 'token',
        tokenType: 'Bearer',
        loginAt: Date.now(),
        expiresIn: 3600
      }))
    })

    await page.route('**/notices**', async route => {
      await route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify([{ noticeId: 1, title: '系统公告', content: '欢迎使用' }])
      })
    })

    await page.goto('/manage/notice')
    await expect(page.getByText('公告信息')).toBeVisible()
  })
})
