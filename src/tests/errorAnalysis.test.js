import assert from 'node:assert/strict'

import {
  normalizeQuestionType,
  normalizeUserAnswerPayload,
  toStringList,
} from '../utils/errorAnalysisPayload.js'

const run = () => {
  assert.deepEqual(toStringList(['A', ' B ', '', null]), ['A', 'B'])
  assert.deepEqual(toStringList('A, B, C'), ['A', 'B', 'C'])

  assert.equal(normalizeQuestionType('single'), 'single')
  assert.equal(normalizeQuestionType('multi'), 'multi')
  assert.equal(normalizeQuestionType('multiple_choice'), 'multi')
  assert.equal(normalizeQuestionType('single_choice'), 'single')
  assert.equal(normalizeQuestionType('judge'), 'judge')
  assert.equal(normalizeQuestionType(''), 'single')

  assert.deepEqual(
    normalizeUserAnswerPayload({
      levelType: 'single',
      userAnswer: ['B'],
    }),
    {
      questionType: 'single',
      userAnswers: ['B'],
      userAnswer: 'B',
    },
  )

  assert.deepEqual(
    normalizeUserAnswerPayload({
      levelType: 'multi',
      userAnswer: ['A', 'C'],
    }),
    {
      questionType: 'multi',
      userAnswers: ['A', 'C'],
      userAnswer: 'A, C',
    },
  )

  assert.deepEqual(
    normalizeUserAnswerPayload({
      type: 'multiple_choice',
      userAnswer: 'A,C',
    }),
    {
      questionType: 'multi',
      userAnswers: ['A', 'C'],
      userAnswer: 'A, C',
    },
  )
}

run()
console.log('errorAnalysis tests passed')
