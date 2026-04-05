<template>
  <div class="avatar-upload">
    <!-- 头像预览区域 -->
    <div class="avatar-preview" @click="triggerFileSelect">
      <img
        v-if="displayUrl"
        :src="displayUrl"
        alt="头像预览"
        :key="displayUrl"
      />
      <div v-else class="placeholder">
        <span class="icon">+</span>
        <span class="text">上传头像</span>
      </div>

      <!-- 悬浮遮罩 -->
      <div class="overlay">
        <span>{{ uploading ? '上传中...' : '更换头像' }}</span>
      </div>

      <!-- 上传进度 -->
      <div v-if="uploading" class="progress-overlay">
        <div class="progress-ring">
          <svg viewBox="0 0 36 36">
            <path
              class="progress-ring-bg"
              d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            />
            <path
              class="progress-ring-fill"
              :stroke-dasharray="`${progress}, 100`"
              d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
            />
          </svg>
          <span class="progress-text">{{ progress }}%</span>
        </div>
      </div>
    </div>

    <!-- 隐藏的文件输入框 -->
    <input
      ref="fileInputRef"
      type="file"
      :accept="acceptFormats"
      hidden
      @change="handleFileChange"
    />

    <!-- 格式提示 -->
    <p class="hint">
      支持 {{ formatLabels }} 格式，文件大小不超过 {{ maxSizeMB }}MB
    </p>

    <!-- 错误提示 -->
    <p v-if="errorMsg" class="error">{{ errorMsg }}</p>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import api from '../api'

// ==================== 类型定义 ====================
// 格式配置表
const FORMAT_MAP = {
  jpg: {
    mime: 'image/jpeg',
    ext: '.jpg,.jpeg',
    label: 'JPG',
    magic: [0xFF, 0xD8, 0xFF]
  },
  png: {
    mime: 'image/png',
    ext: '.png',
    label: 'PNG',
    magic: [0x89, 0x50, 0x4E, 0x47]
  },
  gif: {
    mime: 'image/gif',
    ext: '.gif',
    label: 'GIF',
    magic: [0x47, 0x49, 0x46]
  },
  webp: {
    mime: 'image/webp',
    ext: '.webp',
    label: 'WebP',
    magic: [0x52, 0x49, 0x46, 0x46]  // RIFF
  },
  bmp: {
    mime: 'image/bmp',
    ext: '.bmp',
    label: 'BMP',
    magic: [0x42, 0x4D]
  },
  svg: {
    mime: 'image/svg+xml',
    ext: '.svg',
    label: 'SVG',
    magic: []  // SVG是文本格式，需特殊处理
  }
}

// ==================== Props & Emits ====================
const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  maxSizeMB: {
    type: Number,
    default: 5
  },
  formats: {
    type: Array,
    default: () => ['jpg', 'png', 'gif', 'webp']
  },
  size: {
    type: Number,
    default: 120
  }
})

const emit = defineEmits(['update:modelValue', 'upload-success', 'upload-error'])

// ==================== 状态 ====================
const userStore = useUserStore()
const fileInputRef = ref(null)
const localPreview = ref('')  // 本地预览 URL
const errorMsg = ref('')
const uploading = ref(false)
const progress = ref(0)

// ==================== 计算属性 ====================
// 显示的头像 URL：优先本地预览，其次 store，最后 props
const displayUrl = computed(() => {
  return localPreview.value || userStore.avatar || props.modelValue
})

// 生成 accept 属性字符串
const acceptFormats = computed(() => {
  return props.formats
    .filter(f => FORMAT_MAP[f])
    .map(f => {
      const config = FORMAT_MAP[f]
      return `${config.mime},${config.ext}`
    })
    .join(',')
})

// 格式显示标签
const formatLabels = computed(() => {
  return props.formats
    .filter(f => FORMAT_MAP[f])
    .map(f => FORMAT_MAP[f].label)
    .join(' / ')
})

// 允许的 MIME 类型集合
const allowedMimes = computed(() => {
  return new Set(
    props.formats
      .filter(f => FORMAT_MAP[f])
      .map(f => FORMAT_MAP[f].mime)
  )
})

// ==================== 核心方法 ====================
/** 触发文件选择 */
function triggerFileSelect() {
  if (uploading.value) return
  fileInputRef.value?.click()
}

/** 文件选择处理 */
async function handleFileChange(event) {
  const input = event.target
  const file = input.files?.[0]

  // 重置状态
  errorMsg.value = ''

  if (!file) return

  try {
    // 第一步：基础校验（扩展名 + MIME）
    validateBasic(file)

    // 第二步：文件大小校验
    validateSize(file)

    // 第三步：文件头魔数校验（防止改扩展名伪造）
    await validateMagicBytes(file)

    // 第四步：图片可读性校验
    await validateImageReadable(file)

    // 第五步：生成本地预览（立即显示）
    const preview = await generatePreview(file)
    localPreview.value = preview

    // 第六步：上传文件
    await uploadFile(file)

  } catch (error) {
    errorMsg.value = error.message || '文件处理失败'
    emit('upload-error', errorMsg.value)
    ElMessage.error(errorMsg.value)
  }

  // 重置 input（允许重复选择同一文件）
  input.value = ''
}

/** 基础格式校验 */
function validateBasic(file) {
  // 检查 MIME 类型
  if (!allowedMimes.value.has(file.type)) {
    // SVG 可能被识别为其他 MIME
    const isSvgFile = file.name.toLowerCase().endsWith('.svg')
    if (!(isSvgFile && props.formats.includes('svg'))) {
      throw new Error(
        `不支持的文件格式，请上传 ${formatLabels.value} 格式`
      )
    }
  }

  // 检查文件扩展名
  const ext = file.name.split('.').pop()?.toLowerCase() || ''
  const allowedExts = props.formats.flatMap(f => {
    const config = FORMAT_MAP[f]
    return config ? config.ext.replace(/\./g, '').split(',') : []
  })

  if (!allowedExts.includes(ext)) {
    throw new Error(`不支持的文件扩展名 ".${ext}"`)
  }
}

/** 文件大小校验 */
function validateSize(file) {
  const maxBytes = props.maxSizeMB * 1024 * 1024
  if (file.size > maxBytes) {
    const actualMB = (file.size / 1024 / 1024).toFixed(2)
    throw new Error(
      `文件大小 ${actualMB}MB 超过限制 ${props.maxSizeMB}MB`
    )
  }
  if (file.size === 0) {
    throw new Error('文件为空，请重新选择')
  }
}

/** 文件头魔数校验（防伪造） */
async function validateMagicBytes(file) {
  // SVG 是文本格式，单独处理
  if (file.name.toLowerCase().endsWith('.svg')) {
    return validateSvgContent(file)
  }

  const format = props.formats.find(f => {
    const config = FORMAT_MAP[f]
    return config && config.mime === file.type
  })

  if (!format) return

  const config = FORMAT_MAP[format]
  if (config.magic.length === 0) return

  const header = await readFileHeader(file, config.magic.length)

  const isValid = config.magic.every(
    (byte, index) => header[index] === byte
  )

  // WebP 需要额外检查第 8-11 字节
  if (format === 'webp' && isValid) {
    const extHeader = await readFileHeader(file, 12)
    const webpSign = [0x57, 0x45, 0x42, 0x50] // "WEBP"
    const isWebP = webpSign.every(
      (byte, index) => extHeader[index + 8] === byte
    )
    if (!isWebP) {
      throw new Error('文件内容不是有效的 WebP 格式')
    }
  }

  if (!isValid) {
    throw new Error('文件内容与扩展名不匹配，可能是伪造文件')
  }
}

/** 读取文件头部字节 */
function readFileHeader(file, byteCount) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      resolve(new Uint8Array(reader.result))
    }
    reader.onerror = () => reject(new Error('文件读取失败'))
    reader.readAsArrayBuffer(file.slice(0, byteCount))
  })
}

/** SVG 内容校验 */
async function validateSvgContent(file) {
  const text = await file.text()
  if (!text.includes('<svg') || !text.includes('</svg>')) {
    throw new Error('文件内容不是有效的 SVG 格式')
  }
  // 安全检查：SVG 中不应包含 script
  if (text.includes('<script')) {
    throw new Error('SVG 文件包含脚本，已拒绝上传')
  }
}

/** 图片可读性校验 */
function validateImageReadable(file) {
  // SVG 跳过此检查
  if (file.type === 'image/svg+xml') return Promise.resolve()

  return new Promise((resolve, reject) => {
    const img = new Image()
    const url = URL.createObjectURL(file)

    img.onload = () => {
      URL.revokeObjectURL(url)

      // 检查图片尺寸是否合理
      if (img.width < 10 || img.height < 10) {
        reject(new Error('图片尺寸过小，最小 10x10 像素'))
        return
      }
      if (img.width > 4096 || img.height > 4096) {
        reject(new Error('图片尺寸过大，最大 4096x4096 像素'))
        return
      }

      resolve()
    }

    img.onerror = () => {
      URL.revokeObjectURL(url)
      reject(new Error('图片文件已损坏，无法读取'))
    }

    img.src = url
  })
}

/** 生成本地预览 */
function generatePreview(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(new Error('预览生成失败'))
    reader.readAsDataURL(file)
  })
}

/** 获取 token */
function getToken() {
  const userRaw = localStorage.getItem('user')
  if (userRaw) {
    try {
      return JSON.parse(userRaw)?.token
    } catch (e) {
      console.warn('解析用户 token 失败:', e)
    }
  }
  return null
}

/** 使用项目 api 实例上传（备用方法） */
async function uploadWithApi(file) {
  const formData = new FormData()
  formData.append('file', file)

  // 使用项目配置的 api 实例，它已经包含了 baseURL 和拦截器
  const response = await api.post('/users/me/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 30000,
    onUploadProgress: (e) => {
      if (e.lengthComputable) {
        progress.value = Math.round((e.loaded / e.total) * 100)
      }
    }
  })

  return response.data?.data
}

/** 使用 XMLHttpRequest 上传 */
async function uploadWithXHR(file) {
  const formData = new FormData()
  formData.append('file', file)

  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest()

    // 设置超时时间（30秒）
    xhr.timeout = 30000

    // 上传进度
    xhr.upload.onprogress = (e) => {
      if (e.lengthComputable) {
        progress.value = Math.round((e.loaded / e.total) * 100)
      }
    }

    xhr.onload = () => {
      if (xhr.status === 200) {
        try {
          const res = JSON.parse(xhr.responseText)
          if (res.code === 0 || res.code === 200) {
            resolve(res.data)
          } else {
            reject(new Error(res.message || '上传失败'))
          }
        } catch {
          reject(new Error('服务器返回格式错误'))
        }
      } else if (xhr.status === 0) {
        // status 0 通常是网络错误或跨域问题
        reject(new Error('无法连接到服务器，请检查网络或后端服务是否运行'))
      } else if (xhr.status === 401) {
        reject(new Error('登录已过期，请重新登录'))
      } else if (xhr.status === 413) {
        reject(new Error('文件过大，请上传更小的文件'))
      } else {
        reject(new Error(`上传失败，状态码: ${xhr.status}`))
      }
    }

    xhr.onerror = () => {
      console.error('XHR Error - 可能的原因：')
      console.error('1. 后端服务未启动或无法访问')
      console.error('2. 跨域(CORS)配置问题')
      console.error('3. 网络连接中断')
      reject(new Error('网络错误：无法连接到服务器'))
    }

    xhr.ontimeout = () => reject(new Error('上传超时，请检查网络或稍后重试'))

    // 使用相对路径，让浏览器自动处理同源策略
    // 这样不会触发额外的 CORS 检查
    const uploadUrl = '/api/users/me/avatar'
    console.log('上传头像到:', uploadUrl)
    xhr.open('POST', uploadUrl)

    // 获取 token
    const token = getToken()
    if (token) {
      xhr.setRequestHeader('Authorization', `Bearer ${token}`)
    } else {
      console.warn('未找到用户 token，上传可能因未授权而失败')
    }

    xhr.send(formData)
  })
}

/** 上传文件到服务器 */
async function uploadFile(file) {
  uploading.value = true
  progress.value = 0

  try {
    let result = null

    // 尝试使用 XHR 上传，失败时回退到 api 实例
    try {
      result = await uploadWithXHR(file)
    } catch (xhrError) {
      console.warn('XHR 上传失败，尝试使用 api 实例:', xhrError.message)
      result = await uploadWithApi(file)
    }

    // 处理返回的头像 URL（原始路径）
    const avatarPath = result.avatarUrl || result.url
    if (!avatarPath) {
      throw new Error('服务器未返回头像路径')
    }

    // ★★★ 关键：通过 store 更新头像，只存原始路径 ★★★
    userStore.updateAvatar(avatarPath)

    // 清除本地预览（现在用服务器返回的URL了）
    if (localPreview.value) {
      URL.revokeObjectURL(localPreview.value)
      localPreview.value = ''
    }

    emit('update:modelValue', avatarPath)
    emit('upload-success', { url: avatarPath, fileName: file.name })
    ElMessage.success('头像上传成功')

  } catch (error) {
    // 上传失败，清除本地预览，恢复旧头像
    if (localPreview.value) {
      URL.revokeObjectURL(localPreview.value)
      localPreview.value = ''
    }

    // 详细的错误日志
    console.error('头像上传失败:', error)
    if (error.response) {
      // axios 错误
      console.error('响应状态:', error.response.status)
      console.error('响应数据:', error.response.data)
    }

    throw error
  } finally {
    uploading.value = false
    progress.value = 0
  }
}
</script>

<style scoped>
.avatar-upload {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.avatar-preview {
  position: relative;
  width: v-bind('size + "px"');
  height: v-bind('size + "px"');
  border-radius: 50%;
  border: 2px dashed #d0d7de;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f6f8fa;
}

.avatar-preview:hover {
  border-color: #0969da;
  box-shadow: 0 0 0 3px rgba(9, 105, 218, 0.1);
}

.avatar-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #8c959f;
}

.placeholder .icon {
  font-size: 32px;
  line-height: 1;
  font-weight: 300;
}

.placeholder .text {
  font-size: 12px;
  margin-top: 4px;
}

.overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 13px;
  font-weight: 500;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.avatar-preview:hover .overlay {
  opacity: 1;
}

.progress-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-ring {
  position: relative;
  width: 50px;
  height: 50px;
}

.progress-ring svg {
  transform: rotate(-90deg);
  width: 100%;
  height: 100%;
}

.progress-ring-bg {
  fill: none;
  stroke: #e1e4e8;
  stroke-width: 3;
}

.progress-ring-fill {
  fill: none;
  stroke: #0969da;
  stroke-width: 3;
  stroke-linecap: round;
  transition: stroke-dasharray 0.3s ease;
}

.progress-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 11px;
  font-weight: 600;
  color: #0969da;
}

.hint {
  font-size: 12px;
  color: #656d76;
  margin: 0;
  text-align: center;
}

.error {
  font-size: 12px;
  color: #cf222e;
  margin: 0;
  text-align: center;
}
</style>
