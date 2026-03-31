<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  avatar: {
    type: String,
    default: '',
  },
  name: {
    type: String,
    default: '你',
  },
  pendant: {
    type: String,
    default: 'star',
  },
  frame: {
    type: String,
    default: 'neon',
  },
  bubble: {
    type: String,
    default: 'ring',
  },
  size: {
    type: Number,
    default: 100,
  },
})

const pendantConfigs = {
  star: {
    name: '闪烁星星',
    icon: 'star',
    color: '#fbbf24',
    animation: 'twinkle',
  },
  crown: {
    name: '皇冠',
    icon: 'crown',
    color: '#f59e0b',
    animation: 'float',
  },
  flower: {
    name: '樱花',
    icon: 'flower',
    color: '#f472b6',
    animation: 'petal',
  },
  lightning: {
    name: '闪电',
    icon: 'lightning',
    color: '#60a5fa',
    animation: 'flash',
  },
  fire: {
    name: '火焰',
    icon: 'fire',
    color: '#ef4444',
    animation: 'burn',
  },
  diamond: {
    name: '钻石',
    icon: 'diamond',
    color: '#8b5cf6',
    animation: 'sparkle',
  },
  music: {
    name: '音符',
    icon: 'music',
    color: '#10b981',
    animation: 'bounce',
  },
  heart: {
    name: '爱心',
    icon: 'heart',
    color: '#ec4899',
    animation: 'pulse',
  },
  comet: {
    name: '彗星',
    icon: 'comet',
    color: '#06b6d4',
    animation: 'orbit',
  },
  halo: {
    name: '光环',
    icon: 'halo',
    color: '#fcd34d',
    animation: 'rotate',
  },
}

const frameConfigs = {
  gold: {
    name: '金色边框',
    style: 'solid',
    color: '#fbbf24',
  },
  rainbow: {
    name: '彩虹边框',
    style: 'gradient',
    colors: ['#ef4444', '#f59e0b', '#eab308', '#22c55e', '#3b82f6', '#8b5cf6'],
  },
  neon: {
    name: '霓虹边框',
    style: 'glow',
    color: '#4a6f9d',
  },
  pixel: {
    name: '像素边框',
    style: 'pixel',
    color: '#6672cb',
  },
  crystal: {
    name: '水晶边框',
    style: 'crystal',
    color: '#67e8f9',
  },
  flame: {
    name: '火焰边框',
    style: 'flame',
    color: '#f97316',
  },
}

const bubbleConfigs = {
  ring: {
    name: '光环环绕',
    type: 'ring',
    color: 'rgba(74, 111, 157, 0.3)',
  },
  stars: {
    name: '星星环绕',
    type: 'stars',
    color: '#fbbf24',
  },
  particles: {
    name: '粒子环绕',
    type: 'particles',
    color: '#8b5cf6',
  },
  orbit: {
    name: '轨道环绕',
    type: 'orbit',
    color: '#4a6f9d',
  },
  petals: {
    name: '花瓣环绕',
    type: 'petals',
    color: '#f472b6',
  },
}

const bgConfigs = {
  gradient1: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
  gradient2: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
  gradient3: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
  gradient4: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
  gradient5: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
  gradient6: 'linear-gradient(135deg, #d299c2 0%, #fef9d7 100%)',
}

const currentPendant = computed(() => pendantConfigs[props.pendant] || pendantConfigs.star)
const currentFrame = computed(() => frameConfigs[props.frame] || frameConfigs.neon)
const currentBubble = computed(() => bubbleConfigs[props.bubble] || bubbleConfigs.ring)
const currentBg = computed(() => bgConfigs[props.background] || bgConfigs.gradient1)

const containerSize = computed(() => props.size + 40)
const avatarSize = computed(() => props.size)
</script>

<template>
  <div class="avatar-pendant-wrap" :style="{ width: containerSize + 'px', height: containerSize + 'px' }">
    <div class="avatar-bg" :style="{ background: currentBg }">
      <div v-if="currentBubble.type === 'ring'" class="bubble-ring"></div>
      <div v-if="currentBubble.type === 'stars'" class="bubble-stars">
        <span v-for="i in 8" :key="i" class="orbit-star" :style="{ '--i': i }">✦</span>
      </div>
      <div v-if="currentBubble.type === 'particles'" class="bubble-particles">
        <span v-for="i in 12" :key="i" class="particle" :style="{ '--i': i }"></span>
      </div>
      <div v-if="currentBubble.type === 'orbit'" class="bubble-orbit">
        <span class="orbit-dot"></span>
      </div>
      <div v-if="currentBubble.type === 'petals'" class="bubble-petals">
        <span v-for="i in 6" :key="i" class="petal" :style="{ '--i': i }">❀</span>
      </div>

      <div class="avatar-frame" :class="currentFrame.style">
        <div class="avatar-inner">
          <img v-if="avatar" :src="avatar" :alt="name" class="avatar-img" />
          <span v-else class="avatar-text">{{ name.slice(0, 1) }}</span>
        </div>
      </div>

      <div class="pendant-wrap" :class="currentPendant.animation">
        <svg v-if="currentPendant.icon === 'star'" viewBox="0 0 24 24" class="pendant-svg" :style="{ color: currentPendant.color }">
          <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" fill="currentColor"/>
        </svg>
        <svg v-else-if="currentPendant.icon === 'crown'" viewBox="0 0 24 24" class="pendant-svg" :style="{ color: currentPendant.color }">
          <path d="M2 18h20v2H2v-2zm2-3l2-6 4 4 2-8 2 8 4-4 2 6H4z" fill="currentColor"/>
        </svg>
        <svg v-else-if="currentPendant.icon === 'flower'" viewBox="0 0 24 24" class="pendant-svg" :style="{ color: currentPendant.color }">
          <circle cx="12" cy="12" r="3" fill="currentColor"/>
          <circle cx="12" cy="5" r="2.5" fill="currentColor" opacity="0.7"/>
          <circle cx="12" cy="19" r="2.5" fill="currentColor" opacity="0.7"/>
          <circle cx="5" cy="12" r="2.5" fill="currentColor" opacity="0.7"/>
          <circle cx="19" cy="12" r="2.5" fill="currentColor" opacity="0.7"/>
          <circle cx="7" cy="7" r="2" fill="currentColor" opacity="0.5"/>
          <circle cx="17" cy="7" r="2" fill="currentColor" opacity="0.5"/>
          <circle cx="7" cy="17" r="2" fill="currentColor" opacity="0.5"/>
          <circle cx="17" cy="17" r="2" fill="currentColor" opacity="0.5"/>
        </svg>
        <svg v-else-if="currentPendant.icon === 'lightning'" viewBox="0 0 24 24" class="pendant-svg" :style="{ color: currentPendant.color }">
          <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" fill="currentColor"/>
        </svg>
        <svg v-else-if="currentPendant.icon === 'fire'" viewBox="0 0 24 24" class="pendant-svg" :style="{ color: currentPendant.color }">
          <path d="M12 23c-3.866 0-7-3.134-7-7 0-2.5 1.5-4.5 3-6.5s3-4.5 3-7.5c3 3 4.5 5 4.5 7.5 0 1-.5 2-1 3 1.5-1 2.5-2.5 2.5-4 2 2 2.5 4 2.5 5.5 0 5-3.134 9-7 9z" fill="currentColor"/>
        </svg>
        <svg v-else-if="currentPendant.icon === 'diamond'" viewBox="0 0 24 24" class="pendant-svg" :style="{ color: currentPendant.color }">
          <polygon points="12 2 2 9 12 22 22 9 12 2" fill="currentColor"/>
          <polygon points="12 2 6 9 12 22 18 9 12 2" fill="white" opacity="0.3"/>
        </svg>
        <svg v-else-if="currentPendant.icon === 'music'" viewBox="0 0 24 24" class="pendant-svg" :style="{ color: currentPendant.color }">
          <path d="M9 18V5l12-2v13" stroke="currentColor" stroke-width="2" fill="none"/>
          <circle cx="6" cy="18" r="3" fill="currentColor"/>
          <circle cx="18" cy="16" r="3" fill="currentColor"/>
        </svg>
        <svg v-else-if="currentPendant.icon === 'heart'" viewBox="0 0 24 24" class="pendant-svg" :style="{ color: currentPendant.color }">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" fill="currentColor"/>
        </svg>
        <svg v-else-if="currentPendant.icon === 'comet'" viewBox="0 0 24 24" class="pendant-svg" :style="{ color: currentPendant.color }">
          <circle cx="8" cy="16" r="4" fill="currentColor"/>
          <path d="M10 14 L22 2" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          <path d="M12 12 L20 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" opacity="0.5"/>
          <path d="M14 10 L18 6" stroke="currentColor" stroke-width="1" stroke-linecap="round" opacity="0.3"/>
        </svg>
        <svg v-else-if="currentPendant.icon === 'halo'" viewBox="0 0 24 24" class="pendant-svg" :style="{ color: currentPendant.color }">
          <circle cx="12" cy="8" r="6" stroke="currentColor" stroke-width="2" fill="none"/>
          <circle cx="12" cy="8" r="3" fill="currentColor"/>
        </svg>
      </div>
    </div>
  </div>
</template>

<style scoped>
.avatar-pendant-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-bg {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: visible;
}

.bubble-ring {
  position: absolute;
  inset: -8px;
  border: 3px solid rgba(74, 111, 157, 0.3);
  border-radius: 50%;
  animation: ringPulse 2s ease-in-out infinite;
}

@keyframes ringPulse {
  0%, 100% { transform: scale(1); opacity: 0.3; }
  50% { transform: scale(1.08); opacity: 0.6; }
}

.bubble-stars {
  position: absolute;
  inset: -15px;
}

.orbit-star {
  position: absolute;
  font-size: 12px;
  color: #fbbf24;
  animation: orbitSpin 6s linear infinite;
  transform-origin: center center;
  top: 50%;
  left: 50%;
}

.orbit-star:nth-child(1) { --angle: 0deg; }
.orbit-star:nth-child(2) { --angle: 45deg; }
.orbit-star:nth-child(3) { --angle: 90deg; }
.orbit-star:nth-child(4) { --angle: 135deg; }
.orbit-star:nth-child(5) { --angle: 180deg; }
.orbit-star:nth-child(6) { --angle: 225deg; }
.orbit-star:nth-child(7) { --angle: 270deg; }
.orbit-star:nth-child(8) { --angle: 315deg; }

.orbit-star::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  animation: starTwinkle 1s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.1s);
}

@keyframes orbitSpin {
  from { transform: rotate(var(--angle)) translateX(55px) rotate(calc(-1 * var(--angle))); }
  to { transform: rotate(calc(var(--angle) + 360deg)) translateX(55px) rotate(calc(-1 * (var(--angle) + 360deg))); }
}

@keyframes starTwinkle {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.bubble-particles {
  position: absolute;
  inset: -10px;
}

.particle {
  position: absolute;
  width: 6px;
  height: 6px;
  background: #8b5cf6;
  border-radius: 50%;
  top: 50%;
  left: 50%;
  animation: particleFloat 3s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.25s);
}

.particle::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background: inherit;
  border-radius: 50%;
  animation: particleGlow 1.5s ease-in-out infinite;
}

@keyframes particleFloat {
  0%, 100% { transform: rotate(calc(var(--i) * 30deg)) translateX(50px) scale(1); }
  50% { transform: rotate(calc(var(--i) * 30deg + 180deg)) translateX(40px) scale(0.8); }
}

@keyframes particleGlow {
  0%, 100% { box-shadow: 0 0 4px currentColor; }
  50% { box-shadow: 0 0 12px currentColor, 0 0 20px currentColor; }
}

.bubble-orbit {
  position: absolute;
  inset: -12px;
  border: 2px dashed rgba(74, 111, 157, 0.3);
  border-radius: 50%;
  animation: orbitRotate 8s linear infinite;
}

.orbit-dot {
  position: absolute;
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #4a6f9d 0%, #6672cb 100%);
  border-radius: 50%;
  top: -5px;
  left: 50%;
  transform: translateX(-50%);
  box-shadow: 0 0 10px rgba(74, 111, 157, 0.5);
}

@keyframes orbitRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.bubble-petals {
  position: absolute;
  inset: -10px;
}

.petal {
  position: absolute;
  font-size: 14px;
  color: #f472b6;
  top: 50%;
  left: 50%;
  animation: petalSpin 10s linear infinite;
  transform-origin: center center;
}

.petal:nth-child(1) { --angle: 0deg; }
.petal:nth-child(2) { --angle: 60deg; }
.petal:nth-child(3) { --angle: 120deg; }
.petal:nth-child(4) { --angle: 180deg; }
.petal:nth-child(5) { --angle: 240deg; }
.petal:nth-child(6) { --angle: 300deg; }

@keyframes petalSpin {
  from { transform: rotate(var(--angle)) translateX(55px) rotate(calc(-1 * var(--angle))); }
  to { transform: rotate(calc(var(--angle) + 360deg)) translateX(55px) rotate(calc(-1 * (var(--angle) + 360deg))); }
}

.avatar-frame {
  position: relative;
  border-radius: 50%;
  padding: 4px;
}

.avatar-frame.solid {
  border: 3px solid;
  border-color: v-bind('currentFrame.color');
}

.avatar-frame.gradient {
  border: 3px solid transparent;
  background: linear-gradient(white, white) padding-box, 
    linear-gradient(45deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box;
  animation: gradientShift 3s linear infinite;
}

@keyframes gradientShift {
  0% { background: linear-gradient(white, white) padding-box, linear-gradient(0deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box; }
  25% { background: linear-gradient(white, white) padding-box, linear-gradient(90deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box; }
  50% { background: linear-gradient(white, white) padding-box, linear-gradient(180deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box; }
  75% { background: linear-gradient(white, white) padding-box, linear-gradient(270deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box; }
  100% { background: linear-gradient(white, white) padding-box, linear-gradient(360deg, #ef4444, #f59e0b, #eab308, #22c55e, #3b82f6, #8b5cf6) border-box; }
}

.avatar-frame.glow {
  border: 2px solid #4a6f9d;
  box-shadow: 0 0 15px rgba(74, 111, 157, 0.5), 
              0 0 30px rgba(74, 111, 157, 0.3),
              inset 0 0 15px rgba(74, 111, 157, 0.1);
  animation: glowPulse 2s ease-in-out infinite;
}

@keyframes glowPulse {
  0%, 100% { 
    box-shadow: 0 0 15px rgba(74, 111, 157, 0.5), 
                0 0 30px rgba(74, 111, 157, 0.3),
                inset 0 0 15px rgba(74, 111, 157, 0.1);
  }
  50% { 
    box-shadow: 0 0 25px rgba(74, 111, 157, 0.7), 
                0 0 50px rgba(74, 111, 157, 0.4),
                inset 0 0 20px rgba(74, 111, 157, 0.15);
  }
}

.avatar-frame.pixel {
  border: 4px solid;
  border-color: v-bind('currentFrame.color');
  border-image: repeating-linear-gradient(
    90deg,
    v-bind('currentFrame.color') 0px,
    v-bind('currentFrame.color') 4px,
    transparent 4px,
    transparent 8px
  ) 4;
  border-radius: 0;
}

.avatar-frame.crystal {
  border: 3px solid rgba(103, 232, 249, 0.5);
  background: linear-gradient(135deg, rgba(255,255,255,0.3) 0%, rgba(103, 232, 249, 0.1) 50%, rgba(255,255,255,0.3) 100%);
  box-shadow: 0 0 20px rgba(103, 232, 249, 0.3),
              inset 0 0 20px rgba(255, 255, 255, 0.2);
  animation: crystalShimmer 3s ease-in-out infinite;
}

@keyframes crystalShimmer {
  0%, 100% { 
    box-shadow: 0 0 20px rgba(103, 232, 249, 0.3), inset 0 0 20px rgba(255, 255, 255, 0.2);
    filter: brightness(1);
  }
  50% { 
    box-shadow: 0 0 30px rgba(103, 232, 249, 0.5), inset 0 0 25px rgba(255, 255, 255, 0.3);
    filter: brightness(1.1);
  }
}

.avatar-frame.flame {
  border: 3px solid transparent;
  background: linear-gradient(white, white) padding-box, 
    linear-gradient(180deg, #fbbf24 0%, #f97316 50%, #ef4444 100%) border-box;
  box-shadow: 0 0 20px rgba(249, 115, 22, 0.4);
  animation: flameFlicker 0.5s ease-in-out infinite alternate;
}

@keyframes flameFlicker {
  0% { 
    box-shadow: 0 0 15px rgba(249, 115, 22, 0.4), 0 -5px 20px rgba(251, 191, 36, 0.3);
    transform: scale(1);
  }
  100% { 
    box-shadow: 0 0 25px rgba(249, 115, 22, 0.6), 0 -8px 30px rgba(251, 191, 36, 0.4);
    transform: scale(1.02);
  }
}

.avatar-inner {
  width: v-bind('avatarSize + "px"');
  height: v-bind('avatarSize + "px"');
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f4f8 0%, #e8ecf4 100%);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-text {
  font-size: calc(v-bind('avatarSize') * 0.4px);
  font-weight: 700;
  color: #4a6f9d;
}

.pendant-wrap {
  position: absolute;
  top: -5px;
  right: -5px;
  width: 32px;
  height: 32px;
  z-index: 10;
}

.pendant-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
}

.pendant-wrap.twinkle {
  animation: twinkle 1.5s ease-in-out infinite;
}

@keyframes twinkle {
  0%, 100% { transform: scale(1) rotate(0deg); opacity: 1; }
  50% { transform: scale(1.2) rotate(15deg); opacity: 0.8; }
}

.pendant-wrap.float {
  animation: float 2s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-8px) rotate(10deg); }
}

.pendant-wrap.petal {
  animation: petalFall 3s ease-in-out infinite;
}

@keyframes petalFall {
  0% { transform: rotate(0deg) translateY(0); opacity: 1; }
  50% { transform: rotate(180deg) translateY(5px); opacity: 0.8; }
  100% { transform: rotate(360deg) translateY(0); opacity: 1; }
}

.pendant-wrap.flash {
  animation: flash 0.8s ease-in-out infinite;
}

@keyframes flash {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.9); }
}

.pendant-wrap.burn {
  animation: burn 0.4s ease-in-out infinite alternate;
}

@keyframes burn {
  0% { transform: scale(1) rotate(-3deg); filter: brightness(1); }
  100% { transform: scale(1.1) rotate(3deg); filter: brightness(1.2); }
}

.pendant-wrap.sparkle {
  animation: sparkle 1s ease-in-out infinite;
}

@keyframes sparkle {
  0%, 100% { filter: brightness(1) drop-shadow(0 0 5px currentColor); }
  50% { filter: brightness(1.5) drop-shadow(0 0 15px currentColor); }
}

.pendant-wrap.bounce {
  animation: bounce 0.6s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-10px) scale(1.1); }
}

.pendant-wrap.pulse {
  animation: pendantPulse 1s ease-in-out infinite;
}

@keyframes pendantPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.3); }
}

.pendant-wrap.orbit {
  animation: pendantOrbit 3s linear infinite;
  transform-origin: -40px 40px;
}

@keyframes pendantOrbit {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.pendant-wrap.rotate {
  animation: pendantRotate 4s linear infinite;
}

@keyframes pendantRotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
