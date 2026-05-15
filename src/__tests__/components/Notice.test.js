import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'
import { api } from '../../utils/api'
import Notice from '../../views/manage/Notice.vue'

vi.mock('../../utils/api', () => ({
  api: {
    notices: { getList: vi.fn() },
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

describe('Notice.vue', () => {
  function mountNotice() {
    return mount(Notice, {
      global: {
        stubs: { Plus: { template: '<span>plus</span>' } },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('数据加载', () => {
    it('挂载时加载公告列表', () => {
      api.notices.getList.mockResolvedValue([])
      mountNotice()
      expect(api.notices.getList).toHaveBeenCalled()
    })

    it('正常渲染公告数据', async () => {
      api.notices.getList.mockResolvedValue([
        { noticeId: 1, title: '公告1', content: '内容1', status: 'active', createdAt: '2026-01-01T00:00:00' },
      ])
      const wrapper = mountNotice()
      await vi.waitFor(() => !wrapper.vm.loading)
      expect(wrapper.vm.notices.length).toBe(1)
      expect(wrapper.vm.notices[0].title).toBe('公告1')
      expect(wrapper.vm.notices[0].statusText).toBe('启用')
    })

    it('加载失败时显示错误提示', async () => {
      api.notices.getList.mockRejectedValue(new Error('网络错误'))
      mountNotice()
      await vi.waitFor(() => expect(ElMessage.error).toHaveBeenCalled())
    })

    it('noticeId 为空时回退到 id', async () => {
      api.notices.getList.mockResolvedValue([
        { id: 99, title: 'x', content: 'x', status: 'active', createdAt: '' },
      ])
      const wrapper = mountNotice()
      await vi.waitFor(() => !wrapper.vm.loading)
      expect(wrapper.vm.notices[0].id).toBe(99)
    })
  })

  describe('搜索与过滤', () => {
    it('按标题过滤', async () => {
      api.notices.getList.mockResolvedValue([
        { noticeId: 1, title: '系统公告', content: 'c', status: 'active', createdAt: '' },
        { noticeId: 2, title: '其他通知', content: 'c', status: 'active', createdAt: '' },
      ])
      const wrapper = mountNotice()
      await vi.waitFor(() => !wrapper.vm.loading)
      wrapper.vm.filter.title = '系统'
      expect(wrapper.vm.filteredNotices.length).toBe(1)
    })

    it('查询时重置分页', async () => {
      api.notices.getList.mockResolvedValue([])
      const wrapper = mountNotice()
      wrapper.vm.pagination.current = 5
      wrapper.vm.handleSearch()
      expect(wrapper.vm.pagination.current).toBe(1)
    })

    it('重置清空过滤条件和分页', async () => {
      api.notices.getList.mockResolvedValue([])
      const wrapper = mountNotice()
      wrapper.vm.filter.title = 'test'
      wrapper.vm.resetFilter()
      expect(wrapper.vm.filter.title).toBe('')
      expect(wrapper.vm.pagination.current).toBe(1)
    })
  })

  describe('对话框操作', () => {
    it('打开新增对话框', async () => {
      api.notices.getList.mockResolvedValue([])
      const wrapper = mountNotice()
      wrapper.vm.handleAdd()
      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('新增公告')
      expect(wrapper.vm.form.id).toBe('')
    })

    it('打开编辑对话框并填充数据', async () => {
      api.notices.getList.mockResolvedValue([])
      const wrapper = mountNotice()
      wrapper.vm.handleEdit({ id: 1, title: '编辑标题', content: '编辑内容' })
      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('编辑公告')
      expect(wrapper.vm.form.id).toBe(1)
    })

    it('删除弹出 warning', async () => {
      api.notices.getList.mockResolvedValue([])
      const wrapper = mountNotice()
      wrapper.vm.handleDelete(1)
      expect(ElMessage.warning).toHaveBeenCalledWith('当前后端暂未提供删除接口')
    })
  })
})
