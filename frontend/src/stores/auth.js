import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authService } from '@/services'

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref(null)
  const user = ref(null)
  const loading = ref(false)

  // Getters
  const isAuthenticated = computed(() => !!token.value)

  // Actions
  async function login(credentials) {
    loading.value = true
    try {
      const response = await authService.login(credentials)
      token.value = response.data.token
      user.value = response.data.user
      return response
    } finally {
      loading.value = false
    }
  }

  async function register(data) {
    loading.value = true
    try {
      const response = await authService.register(data)
      token.value = response.data.token
      user.value = response.data.user
      return response
    } finally {
      loading.value = false
    }
  }

  async function fetchCurrentUser() {
    if (!token.value) return
    
    loading.value = true
    try {
      const response = await authService.getCurrentUser()
      user.value = response.data
    } catch (error) {
      // Token may be invalid
      logout()
    } finally {
      loading.value = false
    }
  }

  async function updateProfile(data) {
    loading.value = true
    try {
      const response = await authService.updateProfile(data)
      user.value = response.data
      return response
    } finally {
      loading.value = false
    }
  }

  function logout() {
    token.value = null
    user.value = null
  }

  return {
    token,
    user,
    loading,
    isAuthenticated,
    login,
    register,
    fetchCurrentUser,
    updateProfile,
    logout
  }
}, {
  persist: {
    paths: ['token']
  }
})
