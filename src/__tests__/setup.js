import { config } from '@vue/test-utils'
import { vi, beforeEach, afterEach } from 'vitest'

// 全局浅桩化 Element Plus 组件
config.global.stubs = {
  'el-card': true,
  'el-button': true,
  'el-input': true,
  'el-form': true,
  'el-form-item': true,
  'el-tabs': true,
  'el-tab-pane': true,
  'el-table': true,
  'el-table-column': true,
  'el-select': true,
  'el-option': true,
  'el-date-picker': true,
  'el-dialog': true,
  'el-menu': true,
  'el-menu-item': true,
  'el-sub-menu': true,
  'el-tag': true,
  'el-pagination': true,
  'el-skeleton': true,
  'el-empty': true,
  'el-row': true,
  'el-col': true,
  'el-container': true,
  'el-aside': true,
  'el-header': true,
  'el-main': true,
  'el-divider': true,
  'el-icon': true,
  'el-image': true,
  'el-switch': true,
  'el-descriptions': true,
  'el-descriptions-item': true,
  'el-statistic': true,
  'el-checkbox-group': true,
  'el-checkbox': true,
  'el-popconfirm': true,
  'el-avatar': true,
  'el-tooltip': true,
  'el-breadcrumb': true,
  'el-breadcrumb-item': true,
  'el-dropdown': true,
  'el-dropdown-menu': true,
  'el-dropdown-item': true,
  'el-input-number': true,
  transition: true,
  'router-view': true,
  'router-link': true,
}

// 每个测试前清理 localStorage
beforeEach(() => {
  localStorage.clear()
})

// 每个测试后重置所有 mock
afterEach(() => {
  vi.restoreAllMocks()
})
