import request from './index'

export const messageApi = {
  // 获取指定类型的消息 (sys-notice, likes, comments, follows, chat)
  getMessages: (type) => {
    return request.get(`/api/messages/${type}`)
  },

  // 获取私信联系人列表
  getContacts: () => {
    return request.get('/api/messages/contacts')
  },

  // 标记消息为已读
  markAsRead: (id) => {
    return request.put(`/api/messages/${id}/read`)
  }
}