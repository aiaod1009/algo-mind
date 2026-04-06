export const withNonBlockingAuth = (config = {}) => ({
  ...config,
  preserveSessionOn401: true,
  suppressAuthMessage: true,
})
