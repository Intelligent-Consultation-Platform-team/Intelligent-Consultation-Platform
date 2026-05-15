import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import Book from '../../views/appoint/Book.vue'

const { mockGetDepts, mockGetSchedules, mockCreateAppointment } = vi.hoisted(() => ({
  mockGetDepts: vi.fn(),
  mockGetSchedules: vi.fn(),
  mockCreateAppointment: vi.fn(),
}))

vi.mock('../../utils/api', () => ({
  api: {
    departments: { getList: mockGetDepts },
    schedules: { getList: mockGetSchedules },
    appointments: { create: mockCreateAppointment },
  },
}))

vi.mock('element-plus', async (importOriginal) => {
  const actual = await importOriginal()
  return {
    ...actual,
    ElMessage: {
      success: vi.fn(),
      error: vi.fn(),
      warning: vi.fn(),
      info: vi.fn(),
    },
  }
})

describe('Book.vue', () => {
  function mountBook() {
    return mount(Book, {
      global: {
        stubs: { Refresh: { template: '<span>refresh</span>' } },
      },
    })
  }

  beforeEach(() => {
    mockGetDepts.mockClear().mockResolvedValue([])
    mockGetSchedules.mockClear().mockResolvedValue([])
    mockCreateAppointment.mockClear().mockResolvedValue({})
  })

  describe('科室加载', () => {
    it('挂载时加载科室', () => {
      mountBook()
      expect(mockGetDepts).toHaveBeenCalled()
    })

    it('映射科室字段 (deptId/deptName)', async () => {
      mockGetDepts.mockResolvedValue([
        { deptId: 1, deptName: '内科' },
        { id: 2, name: '外科' },
      ])
      const wrapper = mountBook()
      await wrapper.vm.loadData()
      expect(wrapper.vm.departments.length).toBe(2)
      expect(wrapper.vm.departments[0].name).toBe('内科')
    })
  })

  describe('排班规范化', () => {
    it('startTime/endTime 构建时间范围', async () => {
      mockGetSchedules.mockResolvedValue([
        { scheduleId: 1, doctorId: 1, doctorName: '医生A', deptId: 1, deptName: '内科', date: '2026-01-01', startTime: '08:00', endTime: '12:00', maxNumber: 20, remaining: 15 },
      ])
      const wrapper = mountBook()
      // 直接手动调用 loadData 确保数据加载
      await wrapper.vm.loadData()
      expect(wrapper.vm.schedules.length).toBe(1)
      expect(wrapper.vm.schedules[0].timeRange).toBe('08:00 - 12:00')
    })

    it('remaining 为空时回退到 maxNumber', async () => {
      mockGetSchedules.mockResolvedValue([
        { scheduleId: 1, doctorId: 1, doctorName: '医生A', deptId: 1, deptName: '内科', date: '2026-01-01', startTime: '08:00', endTime: '12:00', maxNumber: 10 },
      ])
      const wrapper = mountBook()
      await wrapper.vm.loadData()
      expect(wrapper.vm.schedules.length).toBe(1)
      expect(wrapper.vm.schedules[0].remaining).toBe(10)
    })

    it('加载失败显示错误', async () => {
      mockGetSchedules.mockRejectedValue(new Error('网络错误'))
      const wrapper = mountBook()
      await wrapper.vm.loadData().catch(() => {})
      expect(ElMessage.error).toHaveBeenCalled()
    })
  })

  describe('搜索', () => {
    it('查询时传递过滤参数', async () => {
      const wrapper = mountBook()
      await wrapper.vm.loadData()
      mockGetSchedules.mockClear()
      wrapper.vm.filter.departmentId = 1
      wrapper.vm.filter.date = '2026-01-01'
      await wrapper.vm.handleSearch()
      expect(mockGetSchedules).toHaveBeenCalledWith({
        deptId: 1,
        date: '2026-01-01',
      })
    })
  })

  describe('预约', () => {
    it('打开预约对话框', () => {
      const wrapper = mountBook()
      const row = { id: 1, doctorId: 2, doctorName: 'A', departmentName: '内科', scheduleDate: '2026-01-01', timeRange: '08:00-12:00', remaining: 10 }
      wrapper.vm.handleBook(row)
      expect(wrapper.vm.bookDialogVisible).toBe(true)
      expect(wrapper.vm.selectedSchedule).toEqual(row)
    })

    it('预约成功后刷新数据', async () => {
      mockCreateAppointment.mockResolvedValue({})
      const wrapper = mountBook()
      wrapper.vm.selectedSchedule = { id: 1, doctorId: 2, scheduleDate: '2026-01-01', timeRange: '08:00-12:00' }
      wrapper.vm.bookForm.symptoms = '头疼'
      wrapper.vm.bookFormRef = { validate: () => Promise.resolve() }
      await wrapper.vm.confirmBook()
      expect(mockCreateAppointment).toHaveBeenCalled()
      expect(ElMessage.success).toHaveBeenCalledWith('预约成功，请按时就诊')
      expect(wrapper.vm.bookDialogVisible).toBe(false)
    })

    it('booking 状态中不重复提交', async () => {
      const wrapper = mountBook()
      wrapper.vm.booking = true
      wrapper.vm.selectedSchedule = { id: 1, doctorId: 2 }
      await wrapper.vm.confirmBook()
      expect(mockCreateAppointment).not.toHaveBeenCalled()
    })
  })

  describe('分页', () => {
    it('分页限制每页显示条数', async () => {
      const schedules = Array.from({ length: 15 }, (_, i) => ({
        scheduleId: i + 1, doctorId: 1, doctorName: 'D', deptId: 1, deptName: '内科', date: '2026-01-01', startTime: '08:00', endTime: '12:00', maxNumber: 20, remaining: 10,
      }))
      mockGetSchedules.mockResolvedValue(schedules)
      const wrapper = mountBook()
      await wrapper.vm.loadData()
      expect(wrapper.vm.schedules.length).toBe(15)
      wrapper.vm.pagination.size = 10
      expect(wrapper.vm.paginatedSchedules.length).toBe(10)
    })
  })
})
