<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const isPasswordFocused = ref(false)
const isEmailFocused = ref(false)
const activeMode = ref('none')
const mouseOffset = reactive({ x: 0, y: 0 })

const form = reactive({
  email: '',
  password: '',
  remember: true,
})

const syncActiveMode = () => {
  if (isPasswordFocused.value || Boolean(form.password.trim())) {
    activeMode.value = 'password'
    return
  }
  if (isEmailFocused.value || Boolean(form.email.trim())) {
    activeMode.value = 'email'
    return
  }
  activeMode.value = 'none'
}

const onMouseMove = (event) => {
  const centerX = window.innerWidth / 2
  const centerY = window.innerHeight / 2
  mouseOffset.x = event.clientX - centerX
  mouseOffset.y = event.clientY - centerY
}

const clamp = (value, min, max) => Math.max(min, Math.min(max, value))

const characterStyle = (emailShiftX, passwordShiftX) => {
  let finalX = 0

  if (activeMode.value === 'email') {
    finalX = emailShiftX
  } else if (activeMode.value === 'password') {
    finalX = passwordShiftX
  }

  return {
    transform: `translateX(${finalX}px)`,
  }
}

const eyeStyle = (xFactor, yFactor, modeBias = 0) => {
  if (activeMode.value === 'password') {
    return {
      '--pupil-x': '-5.5px',
      '--pupil-y': '0px',
    }
  }

  let modeOffset = 0
  if (activeMode.value === 'email') modeOffset = modeBias

  const eyeX = clamp(mouseOffset.x / xFactor + modeOffset, -5.5, 5.5)
  const eyeY = clamp(mouseOffset.y / yFactor, -3.8, 3.8)
  return {
    '--pupil-x': `${eyeX}px`,
    '--pupil-y': `${eyeY}px`,
  }
}

const normalizeLoginAccount = (value) => {
  const account = String(value || '').trim()
  if (!account) return ''
  if (!account.includes('@')) return account
  return account.split('@')[0]
}

const handleEmailFocus = () => {
  isEmailFocused.value = true
  syncActiveMode()
}

const handleEmailBlur = () => {
  isEmailFocused.value = false
  syncActiveMode()
}

const handleEmailInput = () => {
  syncActiveMode()
}

const handlePasswordFocus = () => {
  isPasswordFocused.value = true
  syncActiveMode()
}

const handlePasswordBlur = () => {
  isPasswordFocused.value = false
  syncActiveMode()
}

const handlePasswordInput = () => {
  syncActiveMode()
}

const handleLogin = async () => {
  if (!form.email || !form.password) {
    ElMessage.warning('请输入邮箱和密码')
    return
  }

  const account = normalizeLoginAccount(form.email)
  if (!account) {
    ElMessage.warning('请输入有效邮箱')
    return
  }

  loading.value = true
  try {
    const success = await userStore.login(account, form.password)
    if (!success) {
      ElMessage.error('邮箱或密码错误')
      return
    }
    ElMessage.success('登录成功')
    router.push('/home')
  } catch (error) {
    ElMessage.error('登录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleForgetPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}

const handleRegister = () => {
  ElMessage.info('注册入口开发中，当前可使用演示账号登录')
}

onMounted(() => {
  window.addEventListener('mousemove', onMouseMove)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
})
</script>

<template>
  <div class="login-page">
    <section class="left-panel">
      <div class="characters-stage">
        <div class="character purple" :style="characterStyle(36, -24)">
          <div class="eyes">
            <span class="eye" :style="eyeStyle(120, 170, 1.8)"><i class="pupil"></i></span>
            <span class="eye" :style="eyeStyle(120, 170, 1.8)"><i class="pupil"></i></span>
          </div>
        </div>

        <div class="character black" :style="characterStyle(0, -16)">
          <div class="eyes">
            <span class="eye" :style="eyeStyle(132, 180, 1.1)"><i class="pupil"></i></span>
            <span class="eye" :style="eyeStyle(132, 180, 1.1)"><i class="pupil"></i></span>
          </div>
        </div>

        <div class="character yellow" :style="characterStyle(0, -14)">
          <div class="eyes">
            <span class="eye" :style="eyeStyle(145, 190, 1)"><i class="pupil"></i></span>
            <span class="eye" :style="eyeStyle(145, 190, 1)"><i class="pupil"></i></span>
          </div>
          <div class="mouth"></div>
        </div>

        <div class="character orange" :style="characterStyle(0, -18)">
          <div class="eyes">
            <span class="eye" :style="eyeStyle(150, 200, 0.8)"><i class="pupil"></i></span>
            <span class="eye" :style="eyeStyle(150, 200, 0.8)"><i class="pupil"></i></span>
          </div>
        </div>
      </div>
    </section>

    <section class="right-panel">
      <div class="form-wrap">
        <h1>Welcome back!</h1>
        <p class="subtitle">Please enter your details</p>

        <el-form :model="form" @keyup.enter="handleLogin">
          <el-form-item label="Email" label-position="top">
            <el-input v-model="form.email" placeholder="you@example.com" size="large" @focus="handleEmailFocus"
              @blur="handleEmailBlur" @input="handleEmailInput" />
          </el-form-item>

          <el-form-item label="Password" label-position="top">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large"
              @focus="handlePasswordFocus" @blur="handlePasswordBlur" @input="handlePasswordInput" />
          </el-form-item>

          <div class="helper-row">
            <el-checkbox v-model="form.remember">Remember for 30 days</el-checkbox>
            <a href="#" class="link" @click.prevent="handleForgetPassword">Forgot password?</a>
          </div>

          <el-button type="primary" size="large" class="action-btn" :loading="loading" @click="handleLogin">
            Log in
          </el-button>
        </el-form>

        <p class="register-row">
          Don't have an account?
          <a href="#" class="register-link" @click.prevent="handleRegister">Sign Up</a>
        </p>
        <p class="demo-hint">演示账号：admin@example.com / 123456</p>
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 48% 52%;
  background: #eef2f6;

  .left-panel {
    position: relative;
    overflow: hidden;
    background: radial-gradient(circle at 18% 20%, #5a667a 0%, #465267 45%, #3f4b60 100%);

    .characters-stage {
      position: absolute;
      width: 520px;
      max-width: calc(100% - 42px);
      height: 420px;
      left: 50%;
      top: 50%;
      transform: translate(-50%, -50%);

      &::after {
        content: '';
        position: absolute;
        left: 14px;
        right: 14px;
        bottom: 0;
        height: 14px;
        border-radius: 999px;
        background: rgba(18, 25, 39, 0.25);
        filter: blur(8px);
      }
    }
  }

  .right-panel {
    display: grid;
    place-items: center;
    background: linear-gradient(180deg, #f8fafc 0%, #f3f6fa 100%);
    border-left: 1px solid #d4dde8;

    .form-wrap {
      width: 430px;
      max-width: calc(100% - 56px);

      h1 {
        font-size: 48px;
        line-height: 1.1;
        color: #1f2f44;
        font-weight: 800;
      }

      .subtitle {
        margin-top: 8px;
        margin-bottom: 34px;
        color: #667789;
      }

      :deep(.el-form-item__label) {
        color: #2f425b;
        font-weight: 600;
      }

      :deep(.el-input__wrapper) {
        border-radius: 999px;
        min-height: 46px;
        background: #f6f9fc;
      }

      .helper-row {
        margin-top: 4px;
        margin-bottom: 18px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        color: #5f7286;
      }

      .link,
      .register-link {
        color: #3f5d85;
        font-weight: 600;
      }

      .action-btn {
        width: 100%;
        border-radius: 999px;
        min-height: 48px;
        border: none;
        background: linear-gradient(90deg, #557faf 0%, #4a6f9d 100%);

        &:hover {
          background: linear-gradient(90deg, #4f77a5 0%, #436994 100%);
        }
      }

      .register-row {
        margin-top: 24px;
        text-align: center;
        color: #68717e;
      }

      .demo-hint {
        margin-top: 8px;
        text-align: center;
        font-size: 12px;
        color: #8b93a0;
      }
    }
  }
}

.character {
  position: absolute;
  transform-origin: bottom center;
  transition: transform 0.3s ease;

  .eyes {
    display: flex;
    gap: 20px;
    align-items: center;

    .eye {
      width: 22px;
      height: 22px;
      border-radius: 50%;
      background: #f5f7fa;
      display: grid;
      place-items: center;
      box-shadow: 0 1px 1px rgba(0, 0, 0, 0.12) inset;

      .pupil {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #111;
        transform: translate(var(--pupil-x, 0), var(--pupil-y, 0));
        transition: transform 0.2s ease;
      }
    }
  }
}

.purple {
  z-index: 1;
  left: 92px;
  bottom: 0;
  width: 176px;
  height: 330px;
  border-radius: 12px;
  background: #6672cb;

  .eyes {
    position: absolute;
    top: 42px;
    left: 50%;
    transform: translateX(-50%);
  }
}

.black {
  z-index: 2;
  left: 250px;
  bottom: 0;
  width: 126px;
  height: 260px;
  border-radius: 10px;
  background: #303b4c;

  .eyes {
    position: absolute;
    top: 38px;
    left: 50%;
    transform: translateX(-50%);
  }
}

.yellow {
  z-index: 3;
  left: 328px;
  bottom: 0;
  width: 162px;
  height: 210px;
  border-radius: 82px 82px 10px 10px;
  background: #c9b26d;

  .eyes {
    position: absolute;
    top: 42px;
    left: 50%;
    transform: translateX(-50%);
    gap: 18px;
  }

  .mouth {
    position: absolute;
    width: 52px;
    height: 4px;
    background: #20222b;
    border-radius: 999px;
    left: 50%;
    top: 110px;
    transform: translateX(-50%);
  }
}

.orange {
  z-index: 4;
  left: 26px;
  bottom: 0;
  width: 268px;
  height: 170px;
  border-radius: 135px 135px 0 0;
  background: #d88966;

  .eyes {
    position: absolute;
    top: 76px;
    left: 50%;
    transform: translateX(-50%);
  }
}

@media (max-width: 980px) {
  .login-page {
    grid-template-columns: 1fr;

    .left-panel {
      min-height: 380px;

      .characters-stage {
        top: auto;
        bottom: 20px;
        left: 50%;
        transform: translateX(-50%);
        width: 420px;
        height: 320px;
      }

      .purple {
        left: 70px;
        width: 138px;
        height: 250px;
      }

      .black {
        left: 185px;
        width: 100px;
        height: 206px;
      }

      .yellow {
        left: 246px;
        width: 132px;
        height: 172px;
      }

      .orange {
        left: 14px;
        width: 216px;
        height: 136px;
      }
    }

    .right-panel {
      padding: 24px 0 38px;

      .form-wrap {
        h1 {
          font-size: 36px;
        }
      }
    }
  }
}
</style>
