# -*- coding: utf-8 -*-
import re

def process_file(filepath, replacements):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. ensure vue-router is imported
    if "from 'vue-router'" not in content:
        content = re.sub(
            r"import \{([^}]+)\} from 'vue'",
            r"import {\1} from 'vue'\nimport { useRouter } from 'vue-router'",
            content, count=1
        )
    
    # 2. ensure goToProfile is added
    if "const goToProfile =" not in content:
        router_logic = '''
const router = useRouter()
const goToProfile = (userId) => {
  if (userId) router.push({ path: '/profile', query: { userId: userId } })
}
'''
        # insert after first store or api import
        content = re.sub(r'(const \w+Store = use\w+Store\(\))', r'\1' + router_logic, content, count=1)

    # 3. Apply custom replacements
    for old, new in replacements:
        content = content.replace(old, new)
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

# Ranking.vue
process_file('src/views/Ranking.vue', [
    ('<div class="podium-card"', '<div class="podium-card" @click.stop="goToProfile(user.userId)" style="cursor: pointer;"'),
    ('<div class="list-item"', '<div class="list-item" @click.stop="goToProfile(user.userId)" style="cursor: pointer;"')
])

# Forum.vue
process_file('src/views/Forum.vue', [
    ('<div class="head-row">', '<div class="head-row" @click.stop="goToProfile(item.authorId)" style="cursor: pointer;">'),
    ('<div class="hot-comment-header">', '<div class="hot-comment-header" @click.stop="goToProfile(postHotComments[item.id][hotCommentIndex[item.id] || 0]?.authorId)" style="cursor: pointer;">')
])

# ForumPost.vue
process_file('src/views/ForumPost.vue', [
    ('<div class="author-section">', '<div class="author-section" @click.stop="goToProfile(post.authorId)" style="cursor: pointer;">'),
    ('<div class="comment-author-info">', '<div class="comment-author-info" @click.stop="goToProfile(comment.authorId)" style="cursor: pointer;">')
])

