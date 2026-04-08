export function getFullFileUrl(path) {
  if (!path) return ''

  if (
    path.startsWith('http://') ||
    path.startsWith('https://') ||
    path.startsWith('data:') ||
    path.startsWith('blob:')
  ) {
    // 兼容历史数据：若数据库里是 localhost 绝对地址，其他设备无法访问，转为同站点相对 API 路径
    try {
      const parsed = new URL(path)
      const isLocalAddress = parsed.hostname === 'localhost' || parsed.hostname === '127.0.0.1'
      const currentHost = window.location.hostname
      const currentIsLocal = currentHost === 'localhost' || currentHost === '127.0.0.1'

      if (isLocalAddress && !currentIsLocal) {
        if (parsed.pathname.startsWith('/api/')) {
          return `${parsed.pathname}${parsed.search}${parsed.hash}`
        }
        const normalizedPath = parsed.pathname.startsWith('/') ? parsed.pathname : `/${parsed.pathname}`
        return `/api${normalizedPath}${parsed.search}${parsed.hash}`
      }
    } catch (error) {
      // 非法 URL 继续按原值返回
    }
    return path
  }

  if (path.startsWith('/api/')) {
    return path
  }

  const normalizedPath = path.startsWith('/') ? path : `/${path}`
  return `/api${normalizedPath}`
}

export function appendTimestamp(url) {
  if (!url) return ''
  const separator = url.includes('?') ? '&' : '?'
  return `${url}${separator}t=${Date.now()}`
}

export function getAvatarUrl(path) {
  return appendTimestamp(getFullFileUrl(path))
}
