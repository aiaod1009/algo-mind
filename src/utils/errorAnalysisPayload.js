const toStringList = (value) => {
  if (Array.isArray(value)) {
    return value
      .filter((item) => item != null)
      .map((item) => String(item).trim())
      .filter(Boolean)
  }

  if (typeof value === 'string') {
    const text = value.trim()
    if (!text) return []

    try {
      const parsed = JSON.parse(text)
      if (Array.isArray(parsed)) {
        return parsed.map((item) => String(item).trim()).filter(Boolean)
      }
    } catch (error) {
      // Ignore JSON parse error and fallback to delimiter split.
    }

    return text
      .split(/[\n,，、]/)
      .map((item) => item.trim())
      .filter(Boolean)
  }

  if (value == null) return []
  return [String(value).trim()].filter(Boolean)
}

const normalizeQuestionType = (value) => {
  const type = String(value || '').trim().toLowerCase()
  if (type === 'multiple' || type === 'multiple_choice') {
    return 'multi'
  }
  if (type === 'single_choice') {
    return 'single'
  }
  if (['single', 'multi', 'judge', 'fill', 'code'].includes(type)) {
    return type
  }
  return 'single'
}

const normalizeUserAnswerPayload = (errorItem = {}) => {
  const userAnswers = toStringList(errorItem.userAnswer)
  const questionType = normalizeQuestionType(errorItem.levelType || errorItem.type)

  return {
    questionType,
    userAnswers,
    userAnswer: questionType === 'multi'
      ? userAnswers.join(', ')
      : (userAnswers[0] || String(errorItem.userAnswer || '').trim()),
  }
}

export {
  normalizeQuestionType,
  normalizeUserAnswerPayload,
  toStringList,
}
