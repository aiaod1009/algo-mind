<template>
  <div class="message-page">
    <div class="message-container">
      <!-- 头部 -->
      <div class="message-header">
        <h2 class="header-title">消息</h2>
        <div class="header-actions">
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#333" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"></circle>
            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
          </svg>
          <svg viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="#333" stroke-width="2"
            stroke-linecap="round" stroke-linejoin="round" style="margin-left: 16px;">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
          </svg>
        </div>
      </div>

      <!-- 分类快捷入口 -->
      <div class="message-categories">
        <div class="category-item">
          <div class="icon-box sys-notice">
            <div class="red-dot"></div>
            <svg viewBox="0 0 24 24" width="28" height="28" fill="#fff">
              <path
                d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z" />
            </svg>
          </div>
          <span>系统通知</span>
        </div>
        <div class="category-item">
          <div class="icon-box likes">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="#fff">
              <path
                d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z" />
            </svg>
          </div>
          <span>赞和收藏</span>
        </div>
        <div class="category-item">
          <div class="icon-box comments">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="#fff">
              <path d="M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18z" />
            </svg>
          </div>
          <span>评论和 @</span>
        </div>
        <div class="category-item">
          <div class="icon-box follows">
            <svg viewBox="0 0 24 24" width="28" height="28" fill="#fff">
              <path
                d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
            </svg>
          </div>
          <span>新增关注</span>
        </div>
      </div>

      <!-- 消息列表 -->
      <div class="message-list">
        <div class="message-item">
          <div class="avatar-wrap">
            <img class="avatar" src="https://api.dicebear.com/7.x/identicon/svg?seed=hua" alt="avatar" />
          </div>
          <div class="msg-content">
            <div class="msg-top">
              <span class="msg-name">花和人品值</span>
            </div>
            <p class="msg-desc">还没有收到小红花，要相信好人有好报</p>
          </div>
        </div>

        <div class="message-item">
          <div class="avatar-wrap">
            <img class="avatar" src="https://api.dicebear.com/7.x/bottts/svg?seed=bot" alt="avatar" />
            <span class="badge">活动</span>
          </div>
          <div class="msg-content">
            <div class="msg-top">
              <div class="name-with-tag">
                <span class="msg-name">创作小助手</span>
                <span class="official-tag">官方</span>
              </div>
              <span class="msg-time">昨天 10:39</span>
            </div>
            <p class="msg-desc">你的简历上写 AI了吗 <span class="emoji">❓</span></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// UI实现
</script>

<style scoped>
.message-page {
  padding: 32px;
  display: flex;
  justify-content: center;
}

.message-container {
  width: 100%;
  max-width: 600px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.message-header {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  padding: 20px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: #333;
}

.header-actions {
  position: absolute;
  right: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.message-categories {
  display: flex;
  justify-content: space-around;
  padding: 24px 20px 32px;
  border-bottom: 8px solid #f7f8fa;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.icon-box {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: transform 0.2s;
}

.category-item:hover .icon-box {
  transform: scale(1.05);
}

.sys-notice {
  background: linear-gradient(135deg, #ffd3a5, #ffb37b);
}

.likes {
  background: linear-gradient(135deg, #d3c4ff, #a88bff);
}

.comments {
  background: linear-gradient(135deg, #a5f3d5, #6ae5b6);
}

.follows {
  background: linear-gradient(135deg, #ffe0b2, #ffcc80);
}

.red-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 10px;
  height: 10px;
  background: #ff4d4f;
  border-radius: 50%;
  border: 2px solid #fff;
}

.category-item span {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.message-list {
  padding: 0 20px;
}

.message-item {
  display: flex;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
}

.message-item:last-child {
  border-bottom: none;
}

.avatar-wrap {
  position: relative;
  margin-right: 16px;
  flex-shrink: 0;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  background: #f5f5f5;
}

.badge {
  position: absolute;
  bottom: -4px;
  left: 50%;
  transform: translateX(-50%);
  background: #ff8c00;
  color: #fff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 8px;
  border: 1px solid #fff;
  white-space: nowrap;
}

.msg-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}

.msg-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.name-with-tag {
  display: flex;
  align-items: center;
  gap: 6px;
}

.msg-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.official-tag {
  font-size: 10px;
  color: #ff4d4f;
  border: 1px solid #ff4d4f;
  padding: 0 4px;
  border-radius: 4px;
  background: #fff2f0;
}

.msg-time {
  font-size: 12px;
  color: #999;
}

.msg-desc {
  margin: 0;
  font-size: 14px;
  color: #666;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.emoji {
  color: #ff4d4f;
  font-weight: bold;
}
</style>
