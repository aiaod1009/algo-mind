<template>
  <div class="message-page page-container">
    <div class="message-container glass-panel">
      <!-- 侧边栏：分类与联系人 -->
      <div class="message-sidebar">
        <div class="sidebar-header">
          <h2 class="header-title">消息中心</h2>
        </div>

        <div class="message-categories">
          <div class="category-item active">
            <div class="icon-box sys-notice">
              <div class="red-dot"></div>
              <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff">
                <path
                  d="M12 22c1.1 0 2-.9 2-2h-4c0 1.1.9 2 2 2zm6-6v-5c0-3.07-1.64-5.64-4.5-6.32V4c0-.83-.67-1.5-1.5-1.5s-1.5.67-1.5 1.5v.68C7.63 5.36 6 7.92 6 11v5l-2 2v1h16v-1l-2-2z" />
              </svg>
            </div>
            <span class="category-name">系统通知</span>
          </div>
          <div class="category-item">
            <div class="icon-box likes">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff">
                <path
                  d="M1 21h4V9H1v12zm22-11c0-1.1-.9-2-2-2h-6.31l.95-4.57.03-.32c0-.41-.17-.79-.44-1.06L14.17 1 7.59 7.59C7.22 7.95 7 8.45 7 9v10c0 1.1.9 2 2 2h9c.83 0 1.54-.5 1.84-1.22l3.02-7.05c.09-.23.14-.47.14-.73v-2z" />
              </svg>
            </div>
            <span class="category-name">赞和收藏</span>
          </div>
          <div class="category-item">
            <div class="icon-box comments">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff">
                <path d="M21.99 4c0-1.1-.89-2-1.99-2H4c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h14l4 4-.01-18z" />
              </svg>
            </div>
            <span class="category-name">评论和 @</span>
          </div>
          <div class="category-item">
            <div class="icon-box follows">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="#fff">
                <path
                  d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
              </svg>
            </div>
            <span class="category-name">新增关注</span>
          </div>
        </div>

        <div class="sidebar-divider">
          <span>近期私信</span>
        </div>

        <div class="contact-list">
          <div class="contact-item">
            <div class="avatar-wrap">
              <img class="avatar" src="https://api.dicebear.com/7.x/bottts/svg?seed=bot" alt="avatar" />
              <span class="badge">官方</span>
            </div>
            <div class="contact-info">
              <div class="contact-top">
                <span class="contact-name">创作小助手</span>
                <span class="contact-time">昨天</span>
              </div>
              <p class="contact-desc">你的简历上写 AI了吗 <span class="emoji">❓</span></p>
            </div>
          </div>

          <div class="contact-item">
            <div class="avatar-wrap">
              <img class="avatar" src="https://api.dicebear.com/7.x/identicon/svg?seed=hua" alt="avatar" />
            </div>
            <div class="contact-info">
              <div class="contact-top">
                <span class="contact-name">花和人品值</span>
                <span class="contact-time">10:39</span>
              </div>
              <p class="contact-desc">还没有收到小红花，要相信好人有好报</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：消息内容列表 -->
      <div class="message-content-area">
        <div class="content-header">
          <h2 class="content-title">系统通知</h2>
          <div class="header-actions">
            <button class="action-btn" title="标记已读">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round">
                <polyline points="20 6 9 17 4 12"></polyline>
              </svg>
            </button>
            <button class="action-btn" title="消息设置">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2"
                stroke-linecap="round" stroke-linejoin="round">
                <circle cx="12" cy="12" r="3"></circle>
                <path
                  d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z">
                </path>
              </svg>
            </button>
          </div>
        </div>

        <div class="system-msg-list">
          <div class="sys-msg-item">
            <div class="sys-msg-time">昨天 10:39</div>
            <div class="sys-msg-card">
              <h3 class="sys-msg-title">算法挑战赛获奖通知</h3>
              <p class="sys-msg-detail">恭喜你在上周的「春季算法挑战赛」中获得第 3 名！你的积分奖励已经发放，请前往个人中心查看。</p>
              <div class="sys-msg-link">查看详情 <svg viewBox="0 0 24 24" width="16" height="16" fill="none"
                  stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="9 18 15 12 9 6"></polyline>
                </svg></div>
            </div>
          </div>

          <div class="sys-msg-item">
            <div class="sys-msg-time">前天 14:20</div>
            <div class="sys-msg-card">
              <h3 class="sys-msg-title">系统升级维护通知</h3>
              <p class="sys-msg-detail">尊敬的用户，为了提供更好的体验，我们将于本周五凌晨 02:00 - 04:00 进行系统升级维护。期间部分功能可能无法使用，给您带来的不便敬请谅解。</p>
            </div>
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
  padding: 24px;
  height: calc(100vh - 72px);
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
}

.glass-panel {
  background: var(--card-bg, rgba(255, 255, 255, 0.6));
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 20px;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.08);
}

.message-container {
  width: 100%;
  max-width: 1000px;
  height: 100%;
  display: flex;
  overflow: hidden;
}

/* 侧边栏 */
.message-sidebar {
  width: 280px;
  border-right: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.3);
  flex-shrink: 0;
}

.sidebar-header {
  padding: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
}

.header-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: var(--text-title, #1e293b);
  letter-spacing: 0.5px;
}

.message-categories {
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.category-item:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: translateX(4px);
}

.category-item.active {
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

.icon-box {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-right: 16px;
}

.sys-notice {
  background: linear-gradient(135deg, #ff9a9e, #fecfef);
}

.likes {
  background: linear-gradient(135deg, #a18cd1, #fbc2eb);
}

.comments {
  background: linear-gradient(135deg, #84fab0, #8fd3f4);
}

.follows {
  background: linear-gradient(135deg, #fccb90, #d57eeb);
}

.red-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #ff4d4f;
  border-radius: 50%;
  border: 2px solid #fff;
}

.category-name {
  font-size: 15px;
  color: var(--text-main, #334155);
  font-weight: 500;
}

.sidebar-divider {
  padding: 16px 24px 8px;
  font-size: 13px;
  color: var(--text-sub, #94a3b8);
  font-weight: 600;
  letter-spacing: 1px;
}

.contact-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contact-item {
  display: flex;
  padding: 12px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.contact-item:hover {
  background: rgba(255, 255, 255, 0.5);
}

.avatar-wrap {
  position: relative;
  margin-right: 14px;
  flex-shrink: 0;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  background: #f1f5f9;
  border: 2px solid #fff;
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

.contact-info {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.contact-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.contact-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-title, #1e293b);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.contact-time {
  font-size: 12px;
  color: var(--text-sub, #94a3b8);
}

.contact-desc {
  margin: 0;
  font-size: 13px;
  color: var(--text-main, #64748b);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.emoji {
  color: #ff4d4f;
  font-weight: bold;
}

/* 右侧内容区 */
.message-content-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.4);
}

.content-header {
  padding: 24px 32px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.2);
}

.content-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: var(--text-title, #1e293b);
}

.header-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-sub, #64748b);
  transition: all 0.2s;
}

.action-btn:hover {
  background: #fff;
  color: var(--brand-blue, #3b82f6);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.system-msg-list {
  flex: 1;
  padding: 32px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.sys-msg-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.sys-msg-time {
  padding: 4px 12px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.04);
  color: var(--text-sub, #64748b);
  font-size: 12px;
  font-weight: 500;
}

.sys-msg-card {
  width: 100%;
  max-width: 600px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.5);
  transition: transform 0.2s;
}

.sys-msg-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.05);
}

.sys-msg-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-title, #1e293b);
}

.sys-msg-detail {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-main, #475569);
}

.sys-msg-link {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--brand-blue, #3b82f6);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.sys-msg-link:hover {
  opacity: 0.8;
}

/* 适配移动端 */
@media (max-width: 768px) {
  .message-page {
    padding: 16px;
  }

  .message-sidebar {
    width: 80px;
  }

  .category-name,
  .contact-info,
  .sidebar-divider,
  .header-title {
    display: none;
  }

  .icon-box,
  .avatar-wrap {
    margin-right: 0;
  }

  .category-item,
  .contact-item {
    justify-content: center;
  }

  .sys-msg-card {
    padding: 16px;
  }
}
</style>
