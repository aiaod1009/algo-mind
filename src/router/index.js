import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import Levels from '../views/Levels.vue'
import Challenge from '../views/Challenge.vue'
import Errors from '../views/Errors.vue'
import Ranking from '../views/Ranking.vue'
import Profile from '../views/Profile.vue'
import Courses from '../views/Courses.vue'
import Forum from '../views/Forum.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: Login,
    },
    {
      path: '/home',
      name: 'home',
      component: Home,
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
      component: Challenge,
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
      path: '/profile',
      name: 'profile',
      component: Profile,
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to, from, next) => {
  const user = localStorage.getItem('user')
  if (to.meta.requiresAuth && !user) {
    next('/')
    return
  }
  next()
})

export default router
