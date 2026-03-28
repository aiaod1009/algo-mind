<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: 'AI 分析结果',
  },
  content: {
    type: String,
    default: '',
  },
  loading: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})
</script>

<template>
  <el-dialog v-model="visible" :title="title" width="620px">
    <el-skeleton :loading="loading" animated :rows="6">
      <div class="analysis-content">{{ content || '暂无分析结果' }}</div>
    </el-skeleton>
    <template #footer>
      <el-button type="primary" @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.analysis-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: var(--text-main);
}
</style>
