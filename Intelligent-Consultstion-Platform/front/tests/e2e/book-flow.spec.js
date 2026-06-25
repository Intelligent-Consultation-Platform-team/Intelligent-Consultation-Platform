import { test, expect } from '@playwright/test'

/**
 * 预约挂号 E2E。
 * <p>
 * 覆盖患者进入预约页、选择排班、填写症状并提交预约的完整流程。
 */
test.describe('appointment booking flow', () => {
  test('patient can open booking page and submit appointment', async ({ page }) => {
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

    await page.route('**/departments', async route => {
      await route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify([{ deptId: 1, deptName: '内科' }])
      })
    })

    await page.route('**/schedules**', async route => {
      await route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify([{ scheduleId: 10, deptName: '内科', doctorName: '张医生', title: '主任医师', dayOfWeek: '5', startTime: '09:00', endTime: '12:00', availableSlots: 3 }])
      })
    })

    await page.route('**/appointment', async route => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          contentType: 'application/json',
          body: JSON.stringify({ code: 0, data: { appointmentId: 9001 }, message: '预约成功' })
        })
        return
      }
      await route.continue()
    })

    await page.goto('/appoint/book')
    await expect(page.getByRole('heading', { name: '预约挂号' })).toBeVisible()
    await page.getByRole('button', { name: '预约' }).click()
    await page.getByLabel('症状描述').fill('头痛')
    await page.getByRole('button', { name: '确认预约' }).click()
    await expect(page.getByText('预约成功！请按时就诊')).toBeVisible()
  })
})
