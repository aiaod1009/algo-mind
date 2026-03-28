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
const containerRef = ref(null)
const containerRect = reactive({ x: 0, y: 0, width: 550, height: 400 })

// 平滑动画状态
const smoothState = reactive({
  // 柱子倾斜角度（使用skew）
  skewX: 0,
  targetSkewX: 0,
  // 每个眼睛的独立偏移
  eyes: {
    'purple-eye-1': { x: 0, y: 0, targetX: 0, targetY: 0 },
    'purple-eye-2': { x: 0, y: 0, targetX: 0, targetY: 0 },
    'black-eye-1': { x: 0, y: 0, targetX: 0, targetY: 0 },
    'black-eye-2': { x: 0, y: 0, targetX: 0, targetY: 0 },
    'orange-eye-1': { x: 0, y: 0, targetX: 0, targetY: 0 },
    'orange-eye-2': { x: 0, y: 0, targetX: 0, targetY: 0 },
    'yellow-eye-1': { x: 0, y: 0, targetX: 0, targetY: 0 },
    'yellow-eye-2': { x: 0, y: 0, targetX: 0, targetY: 0 },
  },
  // 黄色柱子面部偏移
  faceOffsetX: 0,
  faceOffsetY: 0,
  targetFaceOffsetX: 0,
  targetFaceOffsetY: 0,
})

let animationFrameId = null
const lerpFactor = 0.12 // 插值因子

const form = reactive({
  email: '',
  password: '',
  remember: true,
})

// 眼睛配置（与原始HTML一致）
const eyeConfigs = {
  'purple-eye-1': { maxDistance: 5, elementX: 0, elementY: 0 },
  'purple-eye-2': { maxDistance: 5, elementX: 0, elementY: 0 },
  'black-eye-1': { maxDistance: 4, elementX: 0, elementY: 0 },
  'black-eye-2': { maxDistance: 4, elementX: 0, elementY: 0 },
  'orange-eye-1': { maxDistance: 5, elementX: 0, elementY: 0 },
  'orange-eye-2': { maxDistance: 5, elementX: 0, elementY: 0 },
  'yellow-eye-1': { maxDistance: 5, elementX: 0, elementY: 0 },
  'yellow-eye-2': { maxDistance: 5, elementX: 0, elementY: 0 },
}

// 更新容器位置信息
const updateContainerRect = () => {
  if (containerRef.value) {
    const rect = containerRef.value.getBoundingClientRect()
    containerRect.x = rect.left
    containerRect.y = rect.top
    containerRect.width = rect.width
    containerRect.height = rect.height
    updateEyePositions()
  }
}

// 更新每个眼睛在屏幕上的绝对位置
const updateEyePositions = () => {
  const eyeIds = Object.keys(eyeConfigs)
  eyeIds.forEach(id => {
    const el = document.getElementById(id)
    if (el) {
      const rect = el.getBoundingClientRect()
      eyeConfigs[id].elementX = rect.left + rect.width / 2
      eyeConfigs[id].elementY = rect.top + rect.height / 2
    }
  })
}

// 线性插值函数
const lerp = (start, end, factor) => start + (end - start) * factor

// 计算单个眼睛的瞳孔偏移
const calculateEyeOffset = (eyeId, mouseX, mouseY) => {
  const config = eyeConfigs[eyeId]
  const dx = mouseX - config.elementX
  const dy = mouseY - config.elementY
  const distance = Math.sqrt(dx * dx + dy * dy)
  const angle = Math.atan2(dy, dx)

  // 根据距离计算移动量，越远移动越多但有上限
  const moveDistance = Math.min(distance * 0.08, config.maxDistance)

  return {
    x: Math.cos(angle) * moveDistance,
    y: Math.sin(angle) * moveDistance,
  }
}

// 动画循环
const animate = () => {
  // 平滑插值柱子倾斜
  smoothState.skewX = lerp(smoothState.skewX, smoothState.targetSkewX, lerpFactor)

  // 平滑插值每个眼睛的偏移
  Object.keys(smoothState.eyes).forEach(eyeId => {
    const eye = smoothState.eyes[eyeId]
    eye.x = lerp(eye.x, eye.targetX, lerpFactor)
    eye.y = lerp(eye.y, eye.targetY, lerpFactor)

    // 更新DOM
    const el = document.getElementById(eyeId)
    if (el) {
      el.style.transform = `translate(${eye.x}px, ${eye.y}px)`
    }
  })

  // 平滑插值黄色柱子面部偏移
  smoothState.faceOffsetX = lerp(smoothState.faceOffsetX, smoothState.targetFaceOffsetX, lerpFactor)
  smoothState.faceOffsetY = lerp(smoothState.faceOffsetY, smoothState.targetFaceOffsetY, lerpFactor)

  animationFrameId = requestAnimationFrame(animate)
}

const onMouseMove = (event) => {
  updateContainerRect()

  // 计算鼠标相对于容器中心的位置
  const centerX = containerRect.x + containerRect.width / 2
  const centerY = containerRect.y + containerRect.height / 2

  mouseOffset.x = event.clientX - centerX
  mouseOffset.y = event.clientY - centerY

  // 计算柱子倾斜角度（基于鼠标水平位置）
  // 原始HTML使用 skew(-6deg, 0deg) 作为基准
  const baseSkew = -6
  const maxSkewOffset = 8
  const skewOffset = (mouseOffset.x / window.innerWidth) * maxSkewOffset
  smoothState.targetSkewX = baseSkew + skewOffset

  // 计算每个眼睛的目标偏移
  Object.keys(smoothState.eyes).forEach(eyeId => {
    const offset = calculateEyeOffset(eyeId, event.clientX, event.clientY)
    smoothState.eyes[eyeId].targetX = offset.x
    smoothState.eyes[eyeId].targetY = offset.y
  })

  // 计算黄色柱子面部偏移（与原始HTML一致）
  // 原始：transform: translate(15px, -7.4306px) 等
  const faceMoveFactor = 0.03
  smoothState.targetFaceOffsetX = Math.min(Math.max(mouseOffset.x * faceMoveFactor, 0), 20)
  smoothState.targetFaceOffsetY = Math.min(Math.max(mouseOffset.y * faceMoveFactor * 0.5, -10), 0)
}

// 获取柱子倾斜样式
const getColumnStyle = (factor = 1) => {
  return {
    transform: `skew(${smoothState.skewX * factor}deg, 0deg)`,
  }
}

// 获取黄色柱子面部偏移样式
const getYellowFaceStyle = () => {
  return {
    transform: `translate(${smoothState.faceOffsetX}px, ${smoothState.faceOffsetY}px)`,
  }
}

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
  window.addEventListener('resize', updateContainerRect)
  updateContainerRect()
  // 启动动画循环
  animate()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('resize', updateContainerRect)
  // 停止动画循环
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
})
</script>

<template>
  <div class="login-page">
    <section class="left-panel">
      <div ref="containerRef" class="characters-stage">
        <!-- 紫色柱子 -->
        <div
          class="character purple"
          :style="getColumnStyle(1)"
        >
          <div class="eyes-container">
            <div class="eyeball">
              <div id="purple-eye-1" class="eyeball-pupil"></div>
            </div>
            <div class="eyeball">
              <div id="purple-eye-2" class="eyeball-pupil"></div>
            </div>
          </div>
        </div>

        <!-- 黑色柱子 -->
        <div
          class="character black"
          :style="getColumnStyle(1)"
        >
          <div class="eyes-container">
            <div class="eyeball small">
              <div id="black-eye-1" class="eyeball-pupil small"></div>
            </div>
            <div class="eyeball small">
              <div id="black-eye-2" class="eyeball-pupil small"></div>
            </div>
          </div>
        </div>

        <!-- 橙色柱子 -->
        <div
          class="character orange"
          :style="getColumnStyle(1)"
        >
          <div class="eyes-container orange-eyes" :style="getYellowFaceStyle()">
            <div class="pupil" id="orange-eye-1"></div>
            <div class="pupil" id="orange-eye-2"></div>
          </div>
        </div>

        <!-- 黄色柱子 -->
        <div
          class="character yellow"
          :style="getColumnStyle(1)"
        >
          <div 
            class="face-container"
            :style="getYellowFaceStyle()"
          >
            <div class="eyes-container yellow-eyes">
              <div class="pupil" id="yellow-eye-1"></div>
              <div class="pupil" id="yellow-eye-2"></div>
            </div>
            <div class="mouth"></div>
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
    display: flex;
    align-items: center;
    justify-content: center;

    .characters-stage {
      position: relative;
      width: 550px;
      height: 400px;
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

// 角色基础样式
.character {
  position: absolute;
  bottom: 0;
  transform-origin: center bottom;
  will-change: transform;
  transition: transform 0.1s ease-out;

  .eyes-container {
    position: absolute;
    display: flex;
    gap: 32px;

    .eyeball {
      width: 18px;
      height: 18px;
      border-radius: 50%;
      background-color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      will-change: transform;

      &.small {
        width: 16px;
        height: 16px;
      }

      .eyeball-pupil {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        background-color: rgb(45, 45, 45);
        will-change: transform;

        &.small {
          width: 6px;
          height: 6px;
        }
      }
    }

    .pupil {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      background-color: rgb(45, 45, 45);
      will-change: transform;
    }

    &.orange-eyes {
      gap: 32px;
    }

    &.yellow-eyes {
      gap: 24px;
    }
  }
}

// 紫色柱子
.purple {
  left: 70px;
  width: 180px;
  height: 400px;
  background-color: rgb(108, 63, 245);
  border-radius: 10px 10px 0px 0px;
  z-index: 1;

  .eyes-container {
    left: 67px;
    top: 36px;
  }
}

// 黑色柱子
.black {
  left: 240px;
  width: 120px;
  height: 310px;
  background-color: rgb(45, 45, 45);
  border-radius: 8px 8px 0px 0px;
  z-index: 2;

  .eyes-container {
    left: 41px;
    top: 26px;
    gap: 24px;
  }
}

// 橙色柱子
.orange {
  left: 0px;
  width: 240px;
  height: 200px;
  background-color: rgb(255, 155, 107);
  border-radius: 120px 120px 0px 0px;
  z-index: 3;

  .eyes-container {
    left: 82px;
    top: 90px;
  }
}

// 黄色柱子
.yellow {
  left: 310px;
  width: 140px;
  height: 230px;
  background-color: rgb(232, 215, 84);
  border-radius: 70px 70px 0px 0px;
  z-index: 4;

  .face-container {
    position: absolute;
    left: 52px;
    top: 40px;
    transition: transform 0.1s ease-out;

    .eyes-container {
      position: relative;
      left: 0;
      top: 0;
    }

    .mouth {
      position: absolute;
      width: 80px;
      height: 4px;
      background-color: rgb(45, 45, 45);
      border-radius: 999px;
      left: -12px;
      top: 48px;
    }
  }
}

@media (max-width: 980px) {
  .login-page {
    grid-template-columns: 1fr;

    .left-panel {
      min-height: 380px;
      padding: 20px;

      .characters-stage {
        transform: scale(0.75);
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

@media (max-width: 600px) {
  .login-page {
    .left-panel {
      min-height: 300px;

      .characters-stage {
        transform: scale(0.55);
      }
    }
  }
}
</style>
