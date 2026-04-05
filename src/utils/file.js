export function getFullFileUrl(path) {
  if (!path) return ''

  if (path.startsWith('http://') || path.startsWith('https://')) {
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
