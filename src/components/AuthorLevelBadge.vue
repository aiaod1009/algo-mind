<script setup>
import { computed } from 'vue'
import { normalizeAuthorLevelProfile } from '../constants/authorLevelThemes'

const props = defineProps({
  profile: {
    type: Object,
    default: null,
  },
  rawLabel: {
    type: String,
    default: '',
  },
  size: {
    type: String,
    default: 'md',
  },
  showScore: {
    type: Boolean,
    default: false,
  },
})

const normalizedProfile = computed(() => normalizeAuthorLevelProfile(props.profile, props.rawLabel))

const badgeStyle = computed(() => ({
  '--badge-primary': normalizedProfile.value.colors?.primary || '#2f9e44',
  '--badge-secondary': normalizedProfile.value.colors?.secondary || '#74c69d',
  '--badge-accent': normalizedProfile.value.colors?.accent || '#d8f3dc',
  '--badge-text': normalizedProfile.value.colors?.text || '#1b4332',
  '--badge-border': normalizedProfile.value.colors?.border || 'rgba(47,158,68,0.28)',
  '--badge-surface': normalizedProfile.value.colors?.surface || 'linear-gradient(135deg, #f8fff9 0%, #def7e8 100%)',
  '--badge-glow': normalizedProfile.value.colors?.glow || '0 0 20px rgba(47,158,68,0.18)',
}))
</script>

<template>
  <span class="author-level-badge" :class="[`size-${size}`, `theme-${normalizedProfile.theme}`]" :style="badgeStyle">
    <span class="badge-halo"></span>
    <span class="badge-sheen"></span>
    <span class="badge-orb"></span>
    <span class="badge-copy">
      <span class="badge-label">{{ normalizedProfile.shortLabel }}</span>
      <span v-if="showScore" class="badge-score">{{ normalizedProfile.score }} pts</span>
    </span>
  </span>
</template>

<style scoped>
.author-level-badge {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 10px;
  overflow: hidden;
  border-radius: 999px;
  border: 1px solid var(--badge-border);
  background: var(--badge-surface);
  color: var(--badge-text);
  box-shadow: var(--badge-glow);
  isolation: isolate;
}

.badge-halo,
.badge-sheen {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.badge-halo {
  opacity: 0.5;
  background:
    radial-gradient(circle at 16% 50%, color-mix(in srgb, var(--badge-secondary) 55%, transparent), transparent 58%),
    radial-gradient(circle at 82% 20%, color-mix(in srgb, var(--badge-primary) 26%, transparent), transparent 50%);
}

.badge-sheen {
  background: linear-gradient(120deg, transparent 0%, rgba(255, 255, 255, 0.58) 48%, transparent 100%);
  transform: translateX(-130%);
}

.badge-orb {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  border-radius: 999px;
  background:
    radial-gradient(circle at 30% 30%, rgba(255, 255, 255, 0.96), transparent 44%),
    linear-gradient(135deg, var(--badge-secondary) 0%, var(--badge-primary) 100%);
  box-shadow:
    inset 0 1px 1px rgba(255, 255, 255, 0.55),
    0 0 0 1px rgba(255, 255, 255, 0.45),
    0 8px 18px color-mix(in srgb, var(--badge-primary) 28%, transparent);
}

.badge-copy {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.badge-label {
  font-weight: 700;
  letter-spacing: 0.01em;
  white-space: nowrap;
}

.badge-score {
  opacity: 0.84;
  font-weight: 600;
}

.size-sm {
  padding: 2px 6px 2px 4px;
  gap: 4px;
}

.size-sm .badge-orb {
  width: 8px;
  height: 8px;
}

.size-sm .badge-label {
  font-size: 9px;
}

.size-sm .badge-score {
  font-size: 8px;
}

.size-md {
  padding: 7px 12px 7px 8px;
}

.size-md .badge-orb {
  width: 14px;
  height: 14px;
}

.size-md .badge-label {
  font-size: 12px;
}

.size-md .badge-score {
  font-size: 11px;
}

.size-lg {
  padding: 6px 10px 6px 6px;
}

.size-lg .badge-orb {
  width: 12px;
  height: 12px;
}

.size-lg .badge-label {
  font-size: 11px;
}

.size-lg .badge-score {
  font-size: 9px;
}

.theme-seed .badge-halo {
  animation: seed-halo 4.8s ease-in-out infinite;
}

.theme-seed .badge-orb {
  animation: seed-orb 3.2s ease-in-out infinite;
}

.theme-seed .badge-sheen {
  animation: seed-shine 4.6s ease-in-out infinite;
}

.theme-flare .badge-halo {
  animation: flare-halo 2.6s ease-in-out infinite;
}

.theme-flare .badge-orb {
  animation: flare-orb 1.8s ease-in-out infinite;
}

.theme-flare .badge-sheen {
  animation: flare-shine 2.4s linear infinite;
}

.theme-tide .badge-halo {
  animation: tide-halo 4s ease-in-out infinite;
}

.theme-tide .badge-orb {
  animation: tide-orb 2.8s ease-in-out infinite;
}

.theme-tide .badge-sheen {
  animation: tide-shine 3.4s ease-in-out infinite;
}

.theme-aurora .badge-halo {
  animation: aurora-halo 5.6s ease-in-out infinite;
}

.theme-aurora .badge-orb {
  animation: aurora-orb 3.2s ease-in-out infinite;
}

.theme-aurora .badge-sheen {
  animation: aurora-shine 2.9s ease-in-out infinite;
}

.theme-solar .badge-halo {
  animation: solar-halo 3s ease-in-out infinite;
}

.theme-solar .badge-orb {
  animation: solar-orb 2.2s ease-in-out infinite;
}

.theme-solar .badge-sheen {
  animation: solar-shine 2.2s linear infinite;
}

.theme-nebula .badge-halo {
  animation: nebula-halo 5.5s ease-in-out infinite;
}

.theme-nebula .badge-orb {
  animation: nebula-orb 3.4s ease-in-out infinite;
}

.theme-nebula .badge-sheen {
  animation: nebula-shine 3.6s ease-in-out infinite;
}

.theme-crown .badge-halo {
  animation: crown-halo 3.2s ease-in-out infinite;
}

.theme-crown .badge-orb {
  animation: crown-orb 2.4s ease-in-out infinite;
}

.theme-crown .badge-sheen {
  animation: crown-shine 2s linear infinite;
}

@keyframes seed-halo {
  0%, 100% { transform: scale(1); opacity: 0.42; }
  50% { transform: scale(1.04); opacity: 0.62; }
}

@keyframes seed-orb {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-1px) rotate(-6deg); }
}

@keyframes seed-shine {
  0%, 55%, 100% { transform: translateX(-140%); opacity: 0; }
  14%, 38% { transform: translateX(130%); opacity: 1; }
}

@keyframes flare-halo {
  0%, 100% { opacity: 0.45; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.08); }
}

@keyframes flare-orb {
  0%, 100% { transform: scale(1); }
  40% { transform: scale(1.1) rotate(-4deg); }
  72% { transform: scale(0.95); }
}

@keyframes flare-shine {
  0% { transform: translateX(-130%); opacity: 0; }
  18% { opacity: 0.95; }
  44% { transform: translateX(120%); opacity: 0; }
  100% { transform: translateX(120%); opacity: 0; }
}

@keyframes tide-halo {
  0%, 100% { transform: scaleX(1); opacity: 0.46; }
  50% { transform: scaleX(1.06); opacity: 0.68; }
}

@keyframes tide-orb {
  0%, 100% { transform: translateX(0); }
  50% { transform: translateX(1px) translateY(-1px); }
}

@keyframes tide-shine {
  0%, 100% { transform: translateX(-140%) skewX(-10deg); opacity: 0; }
  30%, 56% { transform: translateX(140%) skewX(-10deg); opacity: 0.95; }
}

@keyframes aurora-halo {
  0%, 100% { opacity: 0.38; filter: hue-rotate(0deg); }
  50% { opacity: 0.8; filter: hue-rotate(18deg); }
}

@keyframes aurora-orb {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-1px) scale(1.08); }
}

@keyframes aurora-shine {
  0%, 100% { transform: translateX(-140%) rotate(0deg); opacity: 0; }
  40% { opacity: 1; }
  60% { transform: translateX(130%) rotate(2deg); opacity: 0.1; }
}

@keyframes solar-halo {
  0%, 100% { opacity: 0.42; box-shadow: none; }
  50% { opacity: 0.9; box-shadow: inset 0 0 0 1px rgba(255,255,255,0.2); }
}

@keyframes solar-orb {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.12); }
}

@keyframes solar-shine {
  0% { transform: translateX(-130%) scaleX(0.8); opacity: 0; }
  20% { opacity: 1; }
  42% { transform: translateX(125%) scaleX(1.15); opacity: 0; }
  100% { transform: translateX(125%) scaleX(1.15); opacity: 0; }
}

@keyframes nebula-halo {
  0%, 100% { opacity: 0.36; filter: blur(0); }
  50% { opacity: 0.82; filter: blur(1px); }
}

@keyframes nebula-orb {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-1px) rotate(10deg); }
}

@keyframes nebula-shine {
  0%, 100% { transform: translateX(-145%) skewX(-12deg); opacity: 0; }
  36%, 62% { transform: translateX(145%) skewX(-12deg); opacity: 0.95; }
}

@keyframes crown-halo {
  0%, 100% { opacity: 0.48; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.08); }
}

@keyframes crown-orb {
  0%, 100% { transform: scale(1) rotate(0deg); }
  50% { transform: scale(1.14) rotate(-8deg); }
}

@keyframes crown-shine {
  0% { transform: translateX(-135%) skewX(-16deg); opacity: 0; }
  15% { opacity: 1; }
  34% { transform: translateX(135%) skewX(-16deg); opacity: 0; }
  100% { transform: translateX(135%) skewX(-16deg); opacity: 0; }
}
</style>
