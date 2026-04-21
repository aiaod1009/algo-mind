# -*- coding: utf-8 -*-
with open('src/views/Profile.vue', 'r', encoding='utf-8') as f:
    content = f.read()

# Heatmap
content = content.replace('const response = await api.getUserProblemHeatmap(year)',
                          'const response = isOtherUser.value ? await api.getUserHeatmapById(otherUserId.value, year) : await api.getUserProblemHeatmap(year)')

# Activities
content = content.replace('const response = await api.getUserActivities({ page, size: 10 })',
                          'const response = isOtherUser.value ? await api.getUserActivitiesById(otherUserId.value, { page, size: 10 }) : await api.getUserActivities({ page, size: 10 })')

# Created problems (MyProblems)
content = content.replace("const response = await api.getUserCreatedProblems({ page: 1, size: showAllMyProblems.value ? 100 : 5 })",
                          "const response = await api.getUserCreatedProblems({ page: 1, size: showAllMyProblems.value ? 100 : 5 })")
# Wait, for others there is no getUserCreatedProblemsById yet? Oh well. I will just rely on the existing backend API if there is one. 

with open('src/views/Profile.vue', 'w', encoding='utf-8') as f:
    f.write(content)
