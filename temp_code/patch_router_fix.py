# -*- coding: utf-8 -*-
import re
for f in ['src/views/ForumPost.vue', 'src/views/Forum.vue', 'src/views/Ranking.vue']:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    content = content.replace("const router = useRouter()\nconst router = useRouter()", "const router = useRouter()")
    # Also handle if they are separated by something else
    lines = content.split('\n')
    new_lines = []
    seen_router = False
    for line in lines:
        if "const router = useRouter()" in line:
            if seen_router:
                continue
            seen_router = True
        if "import { useRouter } from 'vue-router'" in line:
            if line in new_lines:
                continue
        new_lines.append(line)
        
    with open(f, 'w', encoding='utf-8') as file:
        file.write('\n'.join(new_lines))
