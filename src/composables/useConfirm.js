import { ref, createApp, h } from 'vue'
import CustomConfirmDialog from '../components/CustomConfirmDialog.vue'

let dialogInstance = null
let dialogApp = null

export const useConfirm = () => {
  const showConfirm = async (options) => {
    // 如果已存在对话框，先销毁
    if (dialogApp) {
      dialogApp.unmount()
      dialogApp = null
      dialogInstance = null
    }
    
    // 创建容器
    const container = document.createElement('div')
    document.body.appendChild(container)
    
    // 创建 Promise
    return new Promise((resolve, reject) => {
      // 创建应用实例
      dialogApp = createApp({
        render() {
          return h(CustomConfirmDialog, {
            ref: (el) => {
              if (el) {
                dialogInstance = el
                // 显示对话框
                el.show(options).then(resolve).catch(reject).finally(() => {
                  // 清理
                  setTimeout(() => {
                    dialogApp?.unmount()
                    document.body.removeChild(container)
                    dialogApp = null
                    dialogInstance = null
                  }, 300)
                })
              }
            }
          })
        }
      })
      
      dialogApp.mount(container)
    })
  }
  
  return {
    showConfirm
  }
}

// 便捷方法
export const confirmInfo = (message, title = '提示', options = {}) => {
  const { showConfirm } = useConfirm()
  return showConfirm({ message, title, type: 'info', ...options })
}

export const confirmSuccess = (message, title = '成功', options = {}) => {
  const { showConfirm } = useConfirm()
  return showConfirm({ message, title, type: 'success', ...options })
}

export const confirmWarning = (message, title = '警告', options = {}) => {
  const { showConfirm } = useConfirm()
  return showConfirm({ message, title, type: 'warning', ...options })
}

export const confirmError = (message, title = '错误', options = {}) => {
  const { showConfirm } = useConfirm()
  return showConfirm({ message, title, type: 'error', ...options })
}
