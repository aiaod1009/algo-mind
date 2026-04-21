# -*- coding: utf-8 -*-
import re

with open('src/views/Profile.vue', 'r', encoding='utf-8') as f:
    content = f.read()

parts = content.split('<template>')
script_part = parts[0]
template_part = '<template>' + parts[1]

# In template part, we want to replace userStore.avatar with currentUser.avatar
template_part = template_part.replace('userStore.avatar', 'currentUser?.avatar')

# And userStore.points with currentUser?.points
template_part = template_part.replace('userStore.points', 'currentUser?.points')

# Change the "Edit profile" button to either "Edit Profile" or "Follow" depending on isOtherUser
edit_btn_html = '''
        <button v-if="!isOtherUser" class="edit-profile-btn" @click="openEditModal">
          编辑个人资料
        </button>
        <button v-else class="edit-profile-btn" :class="{ 'following': otherUserInfo?.isFollowing }" @click="handleFollow">
          {{ otherUserInfo?.isFollowing ? '已关注' : '关注' }}
        </button>
'''
template_part = re.sub(r'<button class="edit-profile-btn" @click="openEditModal">\s*编辑个人资料\s*</button>', edit_btn_html, template_part)

# Add Followers & Following to profile-stats
followers_html = '''
          <div class="stat-item" style="cursor: pointer;">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z" />
            </svg>
            <span>{{ isOtherUser ? otherUserInfo.followerCount : backendStats.followerCount || 0 }}</span>
            <span class="stat-label">粉丝</span>
          </div>
          <div class="stat-item" style="cursor: pointer;">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
            </svg>
            <span>{{ isOtherUser ? otherUserInfo.followingCount : backendStats.followingCount || 0 }}</span>
            <span class="stat-label">关注</span>
          </div>
'''
template_part = template_part.replace('<div class="profile-stats">', '<div class="profile-stats">' + followers_html)


with open('src/views/Profile.vue', 'w', encoding='utf-8') as f:
    f.write(script_part + template_part)
