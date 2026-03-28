<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import ErrorAnalysisDialog from '../components/ErrorAnalysisDialog.vue'
import { useErrorStore } from '../stores/error'

const errorStore = useErrorStore()
const loading = ref(false)
const analysisLoading = ref(false)
const addDialogVisible = ref(false)
const analysisDialogVisible = ref(false)
const analysisText = ref('')

const addForm = reactive({
  question: '',
  userAnswer: '',
  description: '',
})

const loadData = async () => {
  loading.value = true
  try {
    await errorStore.fetchErrors()
  } catch (error) {
    ElMessage.error('错题加载失败')
  } finally {
    loading.value = false
  }
}

const handleAnalyze = async (row) => {
  analysisDialogVisible.value = true
  analysisLoading.value = true
  analysisText.value = ''
  try {
    const result = await errorStore.getAnalysis(row.id, row.description)
    analysisText.value = result
    errorStore.markAnalysis(row.id, result)
  } catch (error) {
    analysisText.value = '分析失败，请稍后重试。'
  } finally {
    analysisLoading.value = false
  }
}

const handleAdd = async () => {
  if (!addForm.question || !addForm.userAnswer || !addForm.description) {
    ElMessage.warning('请完整填写错题信息')
    return
  }

  try {
    await errorStore.addError({
      question: addForm.question,
      userAnswer: addForm.userAnswer,
      description: addForm.description,
      createdAt: new Date().toLocaleString(),
    })
    ElMessage.success('添加成功')
    addDialogVisible.value = false
    addForm.question = ''
    addForm.userAnswer = ''
    addForm.description = ''
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

onMounted(loadData)
</script>

<template>
  <div class="page-container errors-page">
    <div class="title-row">
      <h2 class="section-title">错题本</h2>
      <el-button type="primary" @click="addDialogVisible = true">手动添加错题</el-button>
    </div>

    <el-card class="surface-card" shadow="never">
      <el-table v-loading="loading" :data="errorStore.errors" stripe>
        <el-table-column prop="question" label="题目" min-width="220" />
        <el-table-column prop="userAnswer" label="错误答案" min-width="140" />
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column prop="analysisStatus" label="分析状态" width="120" />
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button link type="primary" @click="handleAnalyze(scope.row)">AI 分析</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="addDialogVisible" title="添加错题" width="560px">
      <el-form label-width="82px">
        <el-form-item label="题目">
          <el-input v-model="addForm.question" />
        </el-form-item>
        <el-form-item label="错误答案">
          <el-input v-model="addForm.userAnswer" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="addForm.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">保存</el-button>
      </template>
    </el-dialog>

    <ErrorAnalysisDialog
      v-model="analysisDialogVisible"
      :loading="analysisLoading"
      :content="analysisText"
    />
  </div>
</template>

<style scoped>
.errors-page {
  padding-bottom: 28px;
}

.title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
