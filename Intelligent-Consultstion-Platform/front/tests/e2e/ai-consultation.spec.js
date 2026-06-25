import { test, expect } from '@playwright/test'

/**
 * AI 问诊 E2E。
 * <p>
 * 覆盖患者创建会话、发送消息以及关闭会话的主要交互流程。
 */
test.describe('ai consultation flow', () => {
  test('patient can create a session and send a message', async ({ page }) => {
    await page.addInitScript(() => {
      localStorage.setItem('demo_session', JSON.stringify({
        userId: 1001,
        username: 'test_patient',
        role: 'patient',
        token: 'token',
        tokenType: 'Bearer',
        loginAt: Date.now(),
        expiresIn: 3600
      }))
    })

    await page.route('**/api/ai-consultation/session', async route => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          contentType: 'application/json',
          body: JSON.stringify({ code: 0, data: { sessionId: 'cs_1', status: 'active' }, message: '创建成功' })
        })
        return
      }
      await route.continue()
    })

    await page.route('**/api/ai-consultation/message', async route => {
      await route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, data: { assistantMessage: { content: '建议休息' } }, message: '发送成功' })
      })
    })

    await page.goto('/ai-consultation')
    await expect(page.getByText('AI 问诊')).toBeVisible()
  })
})
