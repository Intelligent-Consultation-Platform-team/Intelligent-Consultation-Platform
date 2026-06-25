import { test, expect } from '@playwright/test'

/**
 * 我的流程与缴费 E2E。
 * <p>
 * 覆盖患者查看待缴费记录、发起缴费以及完成后的成功提示。
 */
test.describe('journey payment flow', () => {
  test('patient can pay an unpaid consultation from journey page', async ({ page }) => {
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

    await page.route('**/patient/journey', async route => {
      await route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify({ items: [{ appointmentId: 88, consultationId: 66, status: 'unpaid', paymentStatus: 'unpaid', deptName: '内科', doctorName: '张医生', appointmentDate: '2026-06-26', appointmentTime: '09:00', diagnosis: '感冒' }] })
      })
    })

    await page.route('**/consultation/66', async route => {
      await route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, data: { consultationId: 66, appointmentId: 88, amount: 50, deptName: '内科', doctorName: '张医生', diagnosis: '感冒' } })
      })
    })

    await page.route('**/patient/1001/balance', async route => {
      await route.fulfill({
        contentType: 'application/json',
        body: JSON.stringify({ code: 0, data: { balance: 100 } })
      })
    })

    await page.route('**/patient/1001/payment', async route => {
      if (route.request().method() === 'POST') {
        await route.fulfill({
          contentType: 'application/json',
          body: JSON.stringify({ code: 0, data: { paid: true }, message: '缴费成功' })
        })
        return
      }
      await route.continue()
    })

    await page.goto('/appoint/my-journey')
    await expect(page.getByRole('heading', { name: '我的流程' })).toBeVisible()
    await page.getByRole('button', { name: '立即缴费' }).click()
    await page.getByRole('button', { name: '确认支付 ¥50.00' }).click()
    await expect(page.getByText('缴费成功，就诊已完成')).toBeVisible()
  })
})
