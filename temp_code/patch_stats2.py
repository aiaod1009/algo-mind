# -*- coding: utf-8 -*-
import re

with open('src/views/Profile.vue', 'r', encoding='utf-8') as f:
    content = f.read()

s_start = content.find('const fetchProblemStats = async () => {')
s_end = content.find('}', content.find('catch', s_start)) + 1
s_end = content.find('}', s_end) + 1

new_fetch = '''const fetchProblemStats = async () => {
    try {
      const response = isOtherUser.value ? await api.getUserProfileById(otherUserId.value) : await api.getUserProblemStats()
      if (response.data?.code === 0) {
        const data = response.data.data || {}
        backendStats.value = {
          totalSolved: Number(data.totalSolved || data.solvedProblems || 0),
          weeklyGoal: Number(data.weeklyGoal || currentUser.value?.weeklyGoal || 0),
          followerCount: Number(data.followerCount || 0),
          followingCount: Number(data.followingCount || 0)
        }
      }
    } catch (error) {
      console.error('获取做题统计失败:', error)
    }
}'''

if s_start != -1 and s_end != 0:
    content = content[:s_start] + new_fetch + content[s_end:]

with open('src/views/Profile.vue', 'w', encoding='utf-8') as f:
    f.write(content)
