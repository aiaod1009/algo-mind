# -*- coding: utf-8 -*-
import re

with open('src/views/Profile.vue', 'r', encoding='utf-8') as f:
    content = f.read()

# Make fetchProblemStats fetch other user's stats if isOtherUser
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

content = re.sub(r'const fetchProblemStats = async \(\) => \{.+?catch \(error\) \{\s*console\.error.+?\}\n\s*\}', new_fetch, content, flags=re.DOTALL)

with open('src/views/Profile.vue', 'w', encoding='utf-8') as f:
    f.write(content)
