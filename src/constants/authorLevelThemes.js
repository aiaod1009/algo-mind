const AUTHOR_LEVEL_THEME_MAP = {
  seed: {
    code: 'seed',
    rank: 1,
    title: '青藤新芽',
    shortLabel: '青藤 Lv.1',
    theme: 'seed',
    description: '从零起步，开始建立自己的解题与创作节奏。',
    colors: {
      primary: '#2f9e44',
      secondary: '#74c69d',
      accent: '#d8f3dc',
      text: '#1b4332',
      border: 'rgba(47,158,68,0.28)',
      surface: 'linear-gradient(135deg, rgba(216,243,220,0.95) 0%, rgba(116,198,157,0.18) 100%)',
      glow: '0 0 20px rgba(47,158,68,0.18)',
    },
    effects: {
      animation: 'seed-sway',
      halo: 'leaf-halo',
      particle: 'sprout',
      shine: 'soft-dew',
    },
  },
  flare: {
    code: 'flare',
    rank: 2,
    title: '焰光解题者',
    shortLabel: '焰光 Lv.2',
    theme: 'flare',
    description: '做题稳定输出，开始在社区留下鲜明痕迹。',
    colors: {
      primary: '#f76707',
      secondary: '#ff922b',
      accent: '#fff4e6',
      text: '#7f2704',
      border: 'rgba(247,103,7,0.25)',
      surface: 'linear-gradient(135deg, rgba(255,244,230,0.98) 0%, rgba(255,146,43,0.2) 100%)',
      glow: '0 0 24px rgba(247,103,7,0.22)',
    },
    effects: {
      animation: 'flare-flicker',
      halo: 'ember-ring',
      particle: 'ember',
      shine: 'heat-sweep',
    },
  },
  tide: {
    code: 'tide',
    rank: 3,
    title: '潮汐创作者',
    shortLabel: '潮汐 Lv.3',
    theme: 'tide',
    description: '内容产出与答题成长同步推进，进入持续进阶期。',
    colors: {
      primary: '#0c8599',
      secondary: '#3bc9db',
      accent: '#e3fafc',
      text: '#083344',
      border: 'rgba(12,133,153,0.24)',
      surface: 'linear-gradient(135deg, rgba(227,250,252,0.98) 0%, rgba(59,201,219,0.2) 100%)',
      glow: '0 0 22px rgba(12,133,153,0.18)',
    },
    effects: {
      animation: 'tide-ripple',
      halo: 'wave-ring',
      particle: 'foam',
      shine: 'waterline',
    },
  },
  aurora: {
    code: 'aurora',
    rank: 4,
    title: '极光布道师',
    shortLabel: '极光 Lv.4',
    theme: 'aurora',
    description: '开始具备带动讨论与影响他人的能力。',
    colors: {
      primary: '#7c3aed',
      secondary: '#06b6d4',
      accent: '#f5f3ff',
      text: '#312e81',
      border: 'rgba(124,58,237,0.24)',
      surface: 'linear-gradient(135deg, rgba(245,243,255,0.98) 0%, rgba(6,182,212,0.18) 52%, rgba(124,58,237,0.2) 100%)',
      glow: '0 0 26px rgba(124,58,237,0.22)',
    },
    effects: {
      animation: 'aurora-drift',
      halo: 'polar-veil',
      particle: 'light-ribbon',
      shine: 'spectrum-sweep',
    },
  },
  solar: {
    code: 'solar',
    rank: 5,
    title: '曜金出题官',
    shortLabel: '曜金 Lv.5',
    theme: 'solar',
    description: '不仅能做题，也能用高质量题目和帖子反哺社区。',
    colors: {
      primary: '#d97706',
      secondary: '#fbbf24',
      accent: '#fffbeb',
      text: '#78350f',
      border: 'rgba(217,119,6,0.26)',
      surface: 'linear-gradient(135deg, rgba(255,251,235,0.98) 0%, rgba(251,191,36,0.22) 100%)',
      glow: '0 0 26px rgba(217,119,6,0.24)',
    },
    effects: {
      animation: 'solar-pulse',
      halo: 'sunburst',
      particle: 'spark',
      shine: 'golden-arc',
    },
  },
  nebula: {
    code: 'nebula',
    rank: 6,
    title: '星云策展人',
    shortLabel: '星云 Lv.6',
    theme: 'nebula',
    description: '拥有成熟的内容品味与稳定的社区影响力。',
    colors: {
      primary: '#4338ca',
      secondary: '#db2777',
      accent: '#eef2ff',
      text: '#1e1b4b',
      border: 'rgba(67,56,202,0.24)',
      surface: 'linear-gradient(135deg, rgba(238,242,255,0.98) 0%, rgba(219,39,119,0.18) 50%, rgba(67,56,202,0.22) 100%)',
      glow: '0 0 28px rgba(67,56,202,0.24)',
    },
    effects: {
      animation: 'nebula-float',
      halo: 'cosmic-ring',
      particle: 'stardust',
      shine: 'galaxy-trail',
    },
  },
  crown: {
    code: 'crown',
    rank: 7,
    title: '王冠领航者',
    shortLabel: '王冠 Lv.7',
    theme: 'crown',
    description: '顶级创作者与解题者，用内容与实力定义社区上限。',
    colors: {
      primary: '#991b1b',
      secondary: '#f59e0b',
      accent: '#fff7ed',
      text: '#450a0a',
      border: 'rgba(153,27,27,0.3)',
      surface: 'linear-gradient(135deg, rgba(255,247,237,0.98) 0%, rgba(245,158,11,0.25) 45%, rgba(153,27,27,0.18) 100%)',
      glow: '0 0 30px rgba(153,27,27,0.24)',
    },
    effects: {
      animation: 'crown-rise',
      halo: 'royal-ray',
      particle: 'crown-shard',
      shine: 'victory-sweep',
    },
  },
}

const codeByLegacyRank = {
  1: 'seed',
  2: 'flare',
  3: 'tide',
  4: 'aurora',
  5: 'solar',
  6: 'nebula',
  7: 'crown',
  8: 'crown',
  9: 'crown',
}

const inferCodeFromLabel = (rawLabel = '') => {
  const normalized = String(rawLabel || '').trim().toLowerCase()
  if (!normalized) return 'seed'
  if (AUTHOR_LEVEL_THEME_MAP[normalized]) return normalized

  const match = normalized.match(/lv\.?\s*(\d+)/i)
  if (match) {
    return codeByLegacyRank[Number(match[1])] || 'seed'
  }

  if (normalized.includes('王冠')) return 'crown'
  if (normalized.includes('星云')) return 'nebula'
  if (normalized.includes('曜金')) return 'solar'
  if (normalized.includes('极光')) return 'aurora'
  if (normalized.includes('潮汐')) return 'tide'
  if (normalized.includes('焰光')) return 'flare'
  return 'seed'
}

export const normalizeAuthorLevelProfile = (profile, rawLabel = '') => {
  const fallbackCode = inferCodeFromLabel(profile?.code || rawLabel)
  const theme = AUTHOR_LEVEL_THEME_MAP[fallbackCode] || AUTHOR_LEVEL_THEME_MAP.seed

  return {
    ...theme,
    ...profile,
    code: profile?.code || theme.code,
    rank: Number(profile?.rank || theme.rank || 1),
    title: profile?.title || theme.title,
    shortLabel: profile?.shortLabel || (rawLabel ? String(rawLabel).trim() : theme.shortLabel),
    theme: profile?.theme || theme.theme,
    description: profile?.description || theme.description,
    score: Number(profile?.score || 0),
    progressPercent: Number(profile?.progressPercent || 0),
    currentLevelMinScore: Number(profile?.currentLevelMinScore ?? 0),
    currentLevelMaxScore: Number(profile?.currentLevelMaxScore ?? 0),
    nextLevelMinScore: profile?.nextLevelMinScore ?? null,
    colors: {
      ...theme.colors,
      ...(profile?.colors || {}),
    },
    effects: {
      ...theme.effects,
      ...(profile?.effects || {}),
    },
    breakdown: profile?.breakdown || { total: Number(profile?.score || 0) },
  }
}

export { AUTHOR_LEVEL_THEME_MAP }
