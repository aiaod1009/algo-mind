# -*- coding: utf-8 -*-
import re

with open('src/views/Profile.vue', 'r', encoding='utf-8') as f:
    content = f.read()

parts = content.split('<template>')
script_part = parts[0]
template_part = '<template>' + parts[1]

script_part = script_part.replace('currentUser.value?.id', 'userStore.userInfo?.id')

with open('src/views/Profile.vue', 'w', encoding='utf-8') as f:
    f.write(script_part + template_part)
