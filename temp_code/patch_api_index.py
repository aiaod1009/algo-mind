# -*- coding: utf-8 -*-
with open('src/api/index.js', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace("api.getUserRanking = userApi.getUserRanking",
'''api.getUserRanking = userApi.getUserRanking
api.getUserProfileById = userApi.getUserProfileById
api.toggleFollow = userApi.toggleFollow
api.getUserHeatmapById = userApi.getUserHeatmapById
api.getUserActivitiesById = userApi.getUserActivitiesById
api.getUserSolvedProblemsById = userApi.getUserSolvedProblemsById''')

with open('src/api/index.js', 'w', encoding='utf-8') as f:
    f.write(content)
