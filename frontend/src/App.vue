<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

onMounted(() => {
  // 检查并恢复登录状态
  if (authStore.token) {
    authStore.fetchCurrentUser()
  }
})
</script>

<style>
#app {
  min-height: 100vh;
}
</style>
