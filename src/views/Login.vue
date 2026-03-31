<script setup>
import { onMounted, onUnmounted, reactive, ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Hide } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import gsap from 'gsap'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const showPassword = ref(false)
const isTyping = ref(false)
const passwordValue = ref('')

const form = reactive({
  email: '',
  password: '',
  remember: true,
})

const containerRef = ref(null)
const mouseRef = reactive({ x: 0, y: 0 })
const rafIdRef = ref(0)

const purpleRef = ref(null)
const blackRef = ref(null)
const yellowRef = ref(null)
const orangeRef = ref(null)

const purpleFaceRef = ref(null)
const blackFaceRef = ref(null)
const yellowFaceRef = ref(null)
const orangeFaceRef = ref(null)

const yellowMouthRef = ref(null)

const purpleBlinkTimerRef = ref(null)
const blackBlinkTimerRef = ref(null)
const purplePeekTimerRef = ref(null)
const lookingTimerRef = ref(null)

const isLookingRef = ref(false)

const isHidingPassword = ref(false)
const isShowingPassword = ref(false)

const quickToRef = ref(null)

const updatePasswordState = () => {
  isHidingPassword.value = passwordValue.value.length > 0 && !showPassword.value
  isShowingPassword.value = passwordValue.value.length > 0 && showPassword.value
}

watch([passwordValue, showPassword], () => {
  updatePasswordState()
})

const calcPos = (el) => {
  const rect = el.getBoundingClientRect()
  const cx = rect.left + rect.width / 2
  const cy = rect.top + rect.height / 3
  const dx = mouseRef.x - cx
  const dy = mouseRef.y - cy
  return {
    faceX: Math.max(-15, Math.min(15, dx / 20)),
    faceY: Math.max(-10, Math.min(10, dy / 30)),
    bodySkew: Math.max(-6, Math.min(6, -dx / 120)),
  }
}

const calcEyePos = (el, maxDist) => {
  const r = el.getBoundingClientRect()
  const cx = r.left + r.width / 2
  const cy = r.top + r.height / 2
  const dx = mouseRef.x - cx
  const dy = mouseRef.y - cy
  const dist = Math.min(Math.sqrt(dx ** 2 + dy ** 2), maxDist)
  const angle = Math.atan2(dy, dx)
  return { x: Math.cos(angle) * dist, y: Math.sin(angle) * dist }
}

const tick = () => {
  const container = containerRef.value
  if (!container) return

  const qt = quickToRef.value
  if (!qt) return

  const typing = isTyping.value
  const hiding = isHidingPassword.value
  const showing = isShowingPassword.value
  const looking = isLookingRef.value

  if (purpleRef.value && !showing) {
    const pp = calcPos(purpleRef.value)
    if (typing || hiding) {
      qt.purpleSkew(pp.bodySkew - 12)
      qt.purpleX(40)
      qt.purpleHeight(440)
    } else {
      qt.purpleSkew(pp.bodySkew)
      qt.purpleX(0)
      qt.purpleHeight(400)
    }
  }

  if (blackRef.value && !showing) {
    const bp = calcPos(blackRef.value)
    if (looking) {
      qt.blackSkew(bp.bodySkew * 1.5 + 10)
      qt.blackX(20)
    } else if (typing || hiding) {
      qt.blackSkew(bp.bodySkew * 1.5)
      qt.blackX(0)
    } else {
      qt.blackSkew(bp.bodySkew)
      qt.blackX(0)
    }
  }

  if (orangeRef.value && !showing) {
    const op = calcPos(orangeRef.value)
    qt.orangeSkew(op.bodySkew)
  }

  if (yellowRef.value && !showing) {
    const yp = calcPos(yellowRef.value)
    qt.yellowSkew(yp.bodySkew)
  }

  if (purpleRef.value && !showing && !looking) {
    const pp = calcPos(purpleRef.value)
    const purpleFaceX = pp.faceX >= 0 ? Math.min(25, pp.faceX * 1.5) : pp.faceX
    qt.purpleFaceLeft(45 + purpleFaceX)
    qt.purpleFaceTop(40 + pp.faceY)
  }

  if (blackRef.value && !showing && !looking) {
    const bp = calcPos(blackRef.value)
    qt.blackFaceLeft(26 + bp.faceX)
    qt.blackFaceTop(32 + bp.faceY)
  }

  if (orangeRef.value && !showing) {
    const op = calcPos(orangeRef.value)
    qt.orangeFaceX(op.faceX)
    qt.orangeFaceY(op.faceY)
  }

  if (yellowRef.value && !showing) {
    const yp = calcPos(yellowRef.value)
    qt.yellowFaceX(yp.faceX)
    qt.yellowFaceY(yp.faceY)
  }

  if (yellowRef.value && !showing) {
    const yp = calcPos(yellowRef.value)
    qt.mouthX(yp.faceX)
    qt.mouthY(yp.faceY)
  }

  if (!showing) {
    const allPupils = container.querySelectorAll('.pupil')
    allPupils.forEach((p) => {
      const el = p
      const maxDist = Number(el.dataset.maxDistance) || 5
      const ePos = calcEyePos(el, maxDist)
      gsap.set(el, { x: ePos.x, y: ePos.y })
    })

    if (!looking) {
      const allEyeballs = container.querySelectorAll('.eyeball')
      allEyeballs.forEach((eb) => {
        const el = eb
        const maxDist = Number(el.dataset.maxDistance) || 10
        const pupil = el.querySelector('.eyeball-pupil')
        if (!pupil) return
        const ePos = calcEyePos(el, maxDist)
        gsap.set(pupil, { x: ePos.x, y: ePos.y })
      })
    }
  }

  rafIdRef.value = requestAnimationFrame(tick)
}

const onMove = (e) => {
  mouseRef.x = e.clientX
  mouseRef.y = e.clientY
}

const schedulePurpleBlink = () => {
  const purpleEyeballs = purpleRef.value?.querySelectorAll('.eyeball')
  if (!purpleEyeballs?.length) return

  purpleBlinkTimerRef.value = setTimeout(
    () => {
      purpleEyeballs.forEach((el) => {
        gsap.to(el, { height: 2, duration: 0.08, ease: 'power2.in' })
      })
      setTimeout(() => {
        purpleEyeballs.forEach((el) => {
          gsap.to(el, { height: 18, duration: 0.08, ease: 'power2.out' })
        })
        schedulePurpleBlink()
      }, 150)
    },
    Math.random() * 4000 + 3000
  )
}

const scheduleBlackBlink = () => {
  const blackEyeballs = blackRef.value?.querySelectorAll('.eyeball')
  if (!blackEyeballs?.length) return

  blackBlinkTimerRef.value = setTimeout(
    () => {
      blackEyeballs.forEach((el) => {
        gsap.to(el, { height: 2, duration: 0.08, ease: 'power2.in' })
      })
      setTimeout(() => {
        blackEyeballs.forEach((el) => {
          gsap.to(el, { height: 16, duration: 0.08, ease: 'power2.out' })
        })
        scheduleBlackBlink()
      }, 150)
    },
    Math.random() * 4000 + 3000
  )
}

const applyLookAtEachOther = () => {
  const qt = quickToRef.value
  if (qt) {
    qt.purpleFaceLeft(55)
    qt.purpleFaceTop(65)
    qt.blackFaceLeft(32)
    qt.blackFaceTop(12)
  }
  purpleRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
    gsap.to(p, { x: 3, y: 4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' })
  })
  blackRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
    gsap.to(p, { x: 0, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' })
  })
}

const applyHidingPassword = () => {
  const qt = quickToRef.value
  if (qt) {
    qt.purpleFaceLeft(55)
    qt.purpleFaceTop(65)
  }
}

const applyShowPassword = () => {
  const qt = quickToRef.value
  if (qt) {
    qt.purpleSkew(0)
    qt.blackSkew(0)
    qt.orangeSkew(0)
    qt.yellowSkew(0)
    qt.purpleX(0)
    qt.blackX(0)
    qt.purpleHeight(400)

    qt.purpleFaceLeft(20)
    qt.purpleFaceTop(35)
    qt.blackFaceLeft(10)
    qt.blackFaceTop(28)
    qt.orangeFaceX(50 - 82)
    qt.orangeFaceY(85 - 90)
    qt.yellowFaceX(20 - 52)
    qt.yellowFaceY(35 - 40)
    qt.mouthX(10 - 40)
    qt.mouthY(0)
  }

  purpleRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
    gsap.to(p, { x: -4, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' })
  })
  blackRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
    gsap.to(p, { x: -4, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' })
  })
  orangeRef.value?.querySelectorAll('.pupil').forEach((p) => {
    gsap.to(p, { x: -5, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' })
  })
  yellowRef.value?.querySelectorAll('.pupil').forEach((p) => {
    gsap.to(p, { x: -5, y: -4, duration: 0.3, ease: 'power2.out', overwrite: 'auto' })
  })
}

const schedulePurplePeek = () => {
  if (!isShowingPassword.value || passwordValue.value.length <= 0) {
    clearTimeout(purplePeekTimerRef.value)
    return
  }

  const purpleEyePupils = purpleRef.value?.querySelectorAll('.eyeball-pupil')
  if (!purpleEyePupils?.length) return

  purplePeekTimerRef.value = setTimeout(
    () => {
      purpleEyePupils.forEach((p) => {
        gsap.to(p, {
          x: 4,
          y: 5,
          duration: 0.3,
          ease: 'power2.out',
          overwrite: 'auto',
        })
      })
      const qt = quickToRef.value
      if (qt) {
        qt.purpleFaceLeft(20)
        qt.purpleFaceTop(35)
      }

      setTimeout(() => {
        purpleEyePupils.forEach((p) => {
          gsap.to(p, {
            x: -4,
            y: -4,
            duration: 0.3,
            ease: 'power2.out',
            overwrite: 'auto',
          })
        })
        schedulePurplePeek()
      }, 800)
    },
    Math.random() * 3000 + 2000
  )
}

watch(isTyping, (newVal) => {
  if (newVal && !isShowingPassword.value) {
    isLookingRef.value = true
    applyLookAtEachOther()

    clearTimeout(lookingTimerRef.value)
    lookingTimerRef.value = setTimeout(() => {
      isLookingRef.value = false
      purpleRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
        gsap.killTweensOf(p)
      })
      blackRef.value?.querySelectorAll('.eyeball-pupil').forEach((p) => {
        gsap.killTweensOf(p)
      })
    }, 800)
  } else {
    clearTimeout(lookingTimerRef.value)
    isLookingRef.value = false
  }
})

watch([isHidingPassword, isShowingPassword], () => {
  if (isShowingPassword.value) {
    applyShowPassword()
    schedulePurplePeek()
  } else {
    clearTimeout(purplePeekTimerRef.value)
    if (isHidingPassword.value) {
      applyHidingPassword()
    }
  }
})

const normalizeLoginAccount = (value) => {
  const account = String(value || '').trim()
  if (!account) return ''
  if (!account.includes('@')) return account
  return account.split('@')[0]
}

const handleEmailFocus = () => {
  isTyping.value = true
}

const handleEmailBlur = () => {
  isTyping.value = false
}

const handlePasswordInput = (value) => {
  passwordValue.value = value
}

const handleTogglePassword = () => {
  showPassword.value = !showPassword.value
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

onMounted(async () => {
  await nextTick()

  if (
    !purpleRef.value ||
    !blackRef.value ||
    !orangeRef.value ||
    !yellowRef.value ||
    !purpleFaceRef.value ||
    !blackFaceRef.value ||
    !orangeFaceRef.value ||
    !yellowFaceRef.value ||
    !yellowMouthRef.value
  )
    return

  quickToRef.value = {
    purpleSkew: gsap.quickTo(purpleRef.value, 'skewX', {
      duration: 0.3,
      ease: 'power2.out',
    }),
    blackSkew: gsap.quickTo(blackRef.value, 'skewX', {
      duration: 0.3,
      ease: 'power2.out',
    }),
    orangeSkew: gsap.quickTo(orangeRef.value, 'skewX', {
      duration: 0.3,
      ease: 'power2.out',
    }),
    yellowSkew: gsap.quickTo(yellowRef.value, 'skewX', {
      duration: 0.3,
      ease: 'power2.out',
    }),
    purpleX: gsap.quickTo(purpleRef.value, 'x', { duration: 0.3, ease: 'power2.out' }),
    blackX: gsap.quickTo(blackRef.value, 'x', { duration: 0.3, ease: 'power2.out' }),
    purpleHeight: gsap.quickTo(purpleRef.value, 'height', {
      duration: 0.3,
      ease: 'power2.out',
    }),
    purpleFaceLeft: gsap.quickTo(purpleFaceRef.value, 'left', {
      duration: 0.3,
      ease: 'power2.out',
    }),
    purpleFaceTop: gsap.quickTo(purpleFaceRef.value, 'top', {
      duration: 0.3,
      ease: 'power2.out',
    }),
    blackFaceLeft: gsap.quickTo(blackFaceRef.value, 'left', {
      duration: 0.3,
      ease: 'power2.out',
    }),
    blackFaceTop: gsap.quickTo(blackFaceRef.value, 'top', {
      duration: 0.3,
      ease: 'power2.out',
    }),
    orangeFaceX: gsap.quickTo(orangeFaceRef.value, 'x', {
      duration: 0.2,
      ease: 'power2.out',
    }),
    orangeFaceY: gsap.quickTo(orangeFaceRef.value, 'y', {
      duration: 0.2,
      ease: 'power2.out',
    }),
    yellowFaceX: gsap.quickTo(yellowFaceRef.value, 'x', {
      duration: 0.2,
      ease: 'power2.out',
    }),
    yellowFaceY: gsap.quickTo(yellowFaceRef.value, 'y', {
      duration: 0.2,
      ease: 'power2.out',
    }),
    mouthX: gsap.quickTo(yellowMouthRef.value, 'x', {
      duration: 0.2,
      ease: 'power2.out',
    }),
    mouthY: gsap.quickTo(yellowMouthRef.value, 'y', {
      duration: 0.2,
      ease: 'power2.out',
    }),
  }

  gsap.set('.pupil', { x: 0, y: 0 })
  gsap.set('.eyeball-pupil', { x: 0, y: 0 })

  window.addEventListener('mousemove', onMove, { passive: true })
  rafIdRef.value = requestAnimationFrame(tick)

  schedulePurpleBlink()
  scheduleBlackBlink()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onMove)
  cancelAnimationFrame(rafIdRef.value)
  clearTimeout(purpleBlinkTimerRef.value)
  clearTimeout(blackBlinkTimerRef.value)
  clearTimeout(purplePeekTimerRef.value)
  clearTimeout(lookingTimerRef.value)
})
</script>

<template>
  <div class="login-page">
    <section class="left-panel">
      <div ref="containerRef" class="characters-stage">
        <!-- 紫色柱子 -->
        <div
          ref="purpleRef"
          class="character purple"
        >
          <div
            ref="purpleFaceRef"
            class="eyes-container"
          >
            <div class="eyeball" data-max-distance="5">
              <div class="eyeball-pupil"></div>
            </div>
            <div class="eyeball" data-max-distance="5">
              <div class="eyeball-pupil"></div>
            </div>
          </div>
        </div>

        <!-- 黑色柱子 -->
        <div
          ref="blackRef"
          class="character black"
        >
          <div
            ref="blackFaceRef"
            class="eyes-container"
          >
            <div class="eyeball small" data-max-distance="4">
              <div class="eyeball-pupil small"></div>
            </div>
            <div class="eyeball small" data-max-distance="4">
              <div class="eyeball-pupil small"></div>
            </div>
          </div>
        </div>

        <!-- 橙色柱子 -->
        <div
          ref="orangeRef"
          class="character orange"
        >
          <div
            ref="orangeFaceRef"
            class="eyes-container orange-eyes"
          >
            <div class="pupil" data-max-distance="5"></div>
            <div class="pupil" data-max-distance="5"></div>
          </div>
        </div>

        <!-- 黄色柱子 -->
        <div
          ref="yellowRef"
          class="character yellow"
        >
          <div
            ref="yellowFaceRef"
            class="eyes-container yellow-eyes"
          >
            <div class="pupil" data-max-distance="5"></div>
            <div class="pupil" data-max-distance="5"></div>
          </div>
          <div ref="yellowMouthRef" class="mouth"></div>
        </div>
      </div>
    </section>

    <section class="right-panel">
      <div class="form-wrap">
        <h1>Welcome back!</h1>
        <p class="subtitle">Please enter your details</p>

        <el-form :model="form" @keyup.enter="handleLogin">
          <el-form-item label="Email" label-position="top">
            <el-input
              v-model="form.email"
              placeholder="you@example.com"
              size="large"
              @focus="handleEmailFocus"
              @blur="handleEmailBlur"
            />
          </el-form-item>

          <el-form-item label="Password" label-position="top">
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              size="large"
              @input="handlePasswordInput"
            >
              <template #suffix>
                <span class="eye-toggle" @click="handleTogglePassword">
                  <el-icon v-if="showPassword"><View /></el-icon>
                  <el-icon v-else><Hide /></el-icon>
                </span>
              </template>
            </el-input>
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
    background: linear-gradient(145deg, #0f172a 0%, #1e3a8a 50%, #1e40af 100%);
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

      .eye-toggle {
        cursor: pointer;
        color: #6b7280;
        display: flex;
        align-items: center;
        transition: color 0.2s;

        &:hover {
          color: #374151;
        }
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
        background: #0c1559;
        transform-style: preserve-3d;
        transform: perspective(500px) translateZ(0);
        transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
        box-shadow: 0 4px 0 #070c33, 0 6px 20px rgba(12, 21, 89, 0.4);
        position: relative;

        &:hover {
          background: #1a237e;
          transform: perspective(500px) translateZ(8px) translateY(-2px);
          box-shadow: 0 8px 0 #070c33, 0 12px 30px rgba(12, 21, 89, 0.5);
        }

        &:active {
          transform: perspective(500px) translateZ(-4px) translateY(4px);
          box-shadow: 0 2px 0 #070c33, 0 2px 10px rgba(12, 21, 89, 0.4);
        }

        &::before {
          content: '';
          position: absolute;
          inset: 0;
          border-radius: 999px;
          background: linear-gradient(135deg, rgba(255,255,255,0.15) 0%, transparent 50%);
          pointer-events: none;
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
  bottom: 0;
  transform-origin: bottom center;
  will-change: transform;

  .eyes-container {
    position: absolute;
    display: flex;
    gap: 32px;
    will-change: left, top;

    .eyeball {
      width: 18px;
      height: 18px;
      border-radius: 50%;
      background-color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      will-change: height;

      &.small {
        width: 16px;
        height: 16px;
      }

      .eyeball-pupil {
        width: 7px;
        height: 7px;
        border-radius: 50%;
        background-color: #2D2D2D;
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
      background-color: #2D2D2D;
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

.purple {
  left: 70px;
  width: 180px;
  height: 400px;
  background-color: #6C3FF5;
  border-radius: 10px 10px 0 0;
  z-index: 1;

  .eyes-container {
    left: 45px;
    top: 40px;
  }
}

.black {
  left: 240px;
  width: 120px;
  height: 310px;
  background-color: #2D2D2D;
  border-radius: 8px 8px 0 0;
  z-index: 2;

  .eyes-container {
    left: 26px;
    top: 32px;
    gap: 24px;
  }
}

.orange {
  left: 0;
  width: 240px;
  height: 200px;
  background-color: #FF9B6B;
  border-radius: 120px 120px 0 0;
  z-index: 3;

  .eyes-container {
    left: 82px;
    top: 90px;
  }
}

.yellow {
  left: 310px;
  width: 140px;
  height: 230px;
  background-color: #E8D754;
  border-radius: 70px 70px 0 0;
  z-index: 4;

  .eyes-container.yellow-eyes {
    position: absolute;
    left: 52px;
    top: 40px;
    gap: 24px;
  }

  .mouth {
    position: absolute;
    width: 80px;
    height: 4px;
    background-color: #2D2D2D;
    border-radius: 999px;
    left: 40px;
    top: 88px;
    will-change: transform;
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
