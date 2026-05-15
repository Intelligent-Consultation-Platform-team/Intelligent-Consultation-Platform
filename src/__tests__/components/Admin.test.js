import { mount } from '@vue/test-utils'
import { ElMessage } from 'element-plus'

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

// Admin.vue 在模块顶层读取 localStorage，必须在 import 前设置
localStorage.setItem('role', 'admin')

const { default: Admin } = await import('../../views/user/Admin.vue')

describe('Admin.vue', () => {
  function mountAdmin() {
    return mount(Admin, {
      global: {
        stubs: { Plus: { template: '<span>plus</span>' } },
      },
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    localStorage.setItem('role', 'admin')
  })

  describe('数据渲染', () => {
    it('挂载时加载 mock 管理员数据', async () => {
      const wrapper = mountAdmin()
      await vi.waitFor(() => !wrapper.vm.loading)
      expect(wrapper.vm.admins.length).toBe(2)
      expect(wrapper.vm.admins[0].username).toBe('admin')
    })

    it('canManage 为 true', async () => {
      const wrapper = mountAdmin()
      await vi.waitFor(() => !wrapper.vm.loading)
      expect(wrapper.vm.canManage).toBe(true)
    })
  })

  describe('搜索与过滤', () => {
    it('按用户名过滤', async () => {
      const wrapper = mountAdmin()
      await vi.waitFor(() => !wrapper.vm.loading)
      wrapper.vm.filter.username = 'admin2'
      wrapper.vm.handleSearch()
      await vi.waitFor(() => !wrapper.vm.loading)
      expect(wrapper.vm.admins.every(item => item.username.includes('admin2'))).toBe(true)
    })

    it('重置清空过滤条件', async () => {
      const wrapper = mountAdmin()
      await vi.waitFor(() => !wrapper.vm.loading)
      wrapper.vm.filter.username = 'admin'
      wrapper.vm.resetFilter()
      expect(wrapper.vm.filter.username).toBe('')
    })
  })

  describe('对话框操作', () => {
    it('打开新增对话框', async () => {
      const wrapper = mountAdmin()
      await vi.waitFor(() => !wrapper.vm.loading)
      wrapper.vm.handleAdd()
      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('新增管理员')
    })

    it('打开编辑对话框并填充数据', async () => {
      const wrapper = mountAdmin()
      await vi.waitFor(() => !wrapper.vm.loading)
      const row = wrapper.vm.admins[0]
      wrapper.vm.handleEdit(row)
      expect(wrapper.vm.dialogVisible).toBe(true)
      expect(wrapper.vm.dialogTitle).toBe('编辑管理员')
      expect(wrapper.vm.form.id).toBe(row.id)
    })
  })

  describe('删除', () => {
    it('从列表中删除并显示成功', async () => {
      const wrapper = mountAdmin()
      await vi.waitFor(() => !wrapper.vm.loading)
      const countBefore = wrapper.vm.admins.length
      wrapper.vm.handleDelete(wrapper.vm.admins[0].id)
      expect(wrapper.vm.admins.length).toBe(countBefore - 1)
      expect(ElMessage.success).toHaveBeenCalledWith('删除成功')
    })
  })

  describe('分页', () => {
    it('每页条数限制', async () => {
      const wrapper = mountAdmin()
      await vi.waitFor(() => !wrapper.vm.loading)
      wrapper.vm.pagination.size = 1
      expect(wrapper.vm.pagedAdmins.length).toBe(1)
    })
  })
})
