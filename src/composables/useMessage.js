import { ref, createApp, h } from 'vue'
import CustomMessage from '../components/CustomMessage.vue'

let messageInstance = null
let messageApp = null

const initMessage = () => {
  if (messageApp) return messageInstance
  
  const container = document.createElement('div')
  document.body.appendChild(container)
  
  messageApp = createApp({
    render() {
      return h(CustomMessage, {
        ref: (el) => {
          messageInstance = el
        }
      })
    }
  })
  
  messageApp.mount(container)
  return messageInstance
}

export const useMessage = () => {
  const show = (message, type = 'info', duration = 3000) => {
    const instance = initMessage()
    return instance?.show(message, type, duration)
  }
  
  const info = (message, duration = 3000) => show(message, 'info', duration)
  const success = (message, duration = 3000) => show(message, 'success', duration)
  const warning = (message, duration = 3000) => show(message, 'warning', duration)
  const error = (message, duration = 3000) => show(message, 'error', duration)
  
  return {
    show,
    info,
    success,
    warning,
    error
  }
}

// 便捷导出，可以直接使用
export const message = {
  info: (msg, duration) => {
    const instance = initMessage()
    return instance?.info(msg, duration)
  },
  success: (msg, duration) => {
    const instance = initMessage()
    return instance?.success(msg, duration)
  },
  warning: (msg, duration) => {
    const instance = initMessage()
    return instance?.warning(msg, duration)
  },
  error: (msg, duration) => {
    const instance = initMessage()
    return instance?.error(msg, duration)
  }
}
