<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const loading = ref(false)

const form = reactive({
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const handleRegister = async () => {
  const email = String(form.email || '').trim()
  const password = String(form.password || '')
  const confirmPassword = String(form.confirmPassword || '')

  if (!email || !password) {
    ElMessage.warning('请输入邮箱和密码')
    return
  }
  if (password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  if (password !== confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  loading.value = true
  try {
    const res = await api.post('/register', {
      name: String(form.name || '').trim(),
      email,
      password,
      confirmPassword,
    })

    if (res.data?.code !== 0) {
      ElMessage.error(res.data?.message || '注册失败')
      return
    }

    ElMessage.success('注册成功，请登录')
    router.push('/')
  } catch (error) {
    const serverMessage = error?.response?.data?.message || error?.message || '注册失败，请稍后重试'
    ElMessage.error(serverMessage)
  } finally {
    loading.value = false
  }
}

const goLogin = () => {
  router.push('/')
}
</script>

<template>
  <div class="register-page">
    <div class="register-card">
      <div class="hug-icon">🤗</div>
      <h1>Join Algo Mind</h1>
      <p class="subtitle">Join the community of machine learners!</p>

      <el-form :model="form" @keyup.enter="handleRegister">
        <el-form-item label="Nickname" label-position="top">
          <el-input v-model="form.name" placeholder="Your nickname (optional)" size="large" />
        </el-form-item>

        <el-form-item label="Email Address" label-position="top">
          <el-input v-model="form.email" placeholder="Email Address" size="large" />
          <p class="hint">Hint: Use your email to sign in next time.</p>
        </el-form-item>

        <el-form-item label="Password" label-position="top">
          <el-input v-model="form.password" type="password" placeholder="Password" show-password size="large" />
        </el-form-item>

        <el-form-item label="Confirm Password" label-position="top">
          <el-input v-model="form.confirmPassword" type="password" placeholder="Confirm Password" show-password
            size="large" />
        </el-form-item>

        <el-button class="next-btn" :loading="loading" @click="handleRegister">
          {{ loading ? 'Registering...' : 'Next' }}
        </el-button>
      </el-form>

      <p class="login-row">
        Already have an account?
        <a href="#" @click.prevent="goLogin">Log in</a>
      </p>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: radial-gradient(circle at 20% 10%, #dce2f6 0%, #ececf0 45%, #f5f3f1 100%);
  padding: 24px;
}

.register-card {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border: 1px solid #d9dee6;
  border-radius: 20px;
  box-shadow: 0 14px 40px rgba(52, 61, 78, 0.18);
  padding: 30px 24px 26px;
  position: relative;
}

.hug-icon {
  position: absolute;
  top: -34px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 56px;
  line-height: 1;
}

h1 {
  margin-top: 16px;
  font-size: 44px;
  line-height: 1.05;
  font-weight: 800;
  color: #0f172a;
}

.subtitle {
  margin-top: 8px;
  margin-bottom: 20px;
  color: #5f6b7d;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #1f2937;
}

:deep(.el-input__wrapper) {
  border-radius: 12px;
  min-height: 46px;
  box-shadow: 0 0 0 1px #d6dce6 inset;
}

.hint {
  margin-top: 8px;
  margin-bottom: 0;
  color: #7b8797;
  font-size: 13px;
  line-height: 1.35;
}

.next-btn {
  width: 100%;
  min-height: 46px;
  margin-top: 6px;
  border-radius: 12px;
  border: 1px solid #d6dce6;
  background: linear-gradient(180deg, #f7f9fc 0%, #edf1f6 100%);
  color: #1f3047;
  font-size: 18px;
  font-weight: 600;
}

.next-btn:hover {
  border-color: #9bb6df;
  color: #102238;
}

.login-row {
  margin-top: 16px;
  text-align: center;
  color: #5f6b7d;
}

.login-row a {
  color: #233c68;
  font-weight: 600;
}
</style>
