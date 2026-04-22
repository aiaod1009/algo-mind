import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import Projects from '../views/Projects.vue'
import Levels from '../views/Levels.vue'
import Errors from '../views/Errors.vue'
import Ranking from '../views/Ranking.vue'
import Profile from '../views/Profile.vue'
import Courses from '../views/Courses.vue'
import Forum from '../views/Forum.vue'
import ForumPost from '../views/ForumPost.vue'
import Register from '../views/Register.vue'
import CodeHistory from '../views/CodeHistory.vue'
import KnowledgeBase from '../views/KnowledgeBase.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: Login,
    },
    {
      path: '/register',
      name: 'register',
      component: Register,
    },
    {
      path: '/home',
      name: 'home',
      component: Home,
      meta: { requiresAuth: true },
    },
    {
      path: '/projects',
      name: 'projects',
      component: Projects,
      meta: { requiresAuth: true },
    },
    {
      path: '/levels',
      name: 'levels',
      component: Levels,
      meta: { requiresAuth: true },
    },
    {
      path: '/challenge/:id',
      name: 'challenge',
      component: () => import('../views/Challenge.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/errors',
      name: 'errors',
      component: Errors,
      meta: { requiresAuth: true },
    },
    {
      path: '/ranking',
      name: 'ranking',
      component: Ranking,
      meta: { requiresAuth: true },
    },
    {
      path: '/courses',
      name: 'courses',
      component: Courses,
      meta: { requiresAuth: true },
    },
    {
      path: '/forum',
      name: 'forum',
      component: Forum,
      meta: { requiresAuth: true },
    },
    {
      path: '/forum/:id',
      name: 'forum-post',
      component: ForumPost,
      meta: { requiresAuth: true },
    },
    {
      path: '/profile',
      name: 'profile',
      component: Profile,
      meta: { requiresAuth: true },
    },
    {
      path: '/code-history',
      name: 'code-history',
      component: CodeHistory,
      meta: { requiresAuth: true },
    },
    {
      path: '/knowledge-base',
      name: 'knowledge-base',
      component: KnowledgeBase,
      meta: { requiresAuth: true },
    },
    {
      path: '/private-message',
      name: 'private-message',
      component: () => import('../views/PrivateMessage.vue'),
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to) => {
  const user = localStorage.getItem('user')
  if (to.meta.requiresAuth && !user) {
    return '/'
  }
})

export default router
