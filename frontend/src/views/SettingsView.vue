<template>
  <div class="settings-page">
    <div class="settings-card card">
      <div class="card-header"><h3>个人信息</h3></div>
      <div class="card-body">
        <div class="profile-section">
          <div class="avatar-wrapper">
            <div class="avatar">{{ user?.nickname?.charAt(0) || user?.username?.charAt(0) || 'U' }}</div>
          </div>
          <div class="profile-info">
            <div class="username">{{ user?.username }}</div>
            <div class="email">{{ user?.email }}</div>
            <div class="join-date">加入于 {{ formatDate(user?.createdAt) }}</div>
          </div>
        </div>

        <form @submit.prevent="handleUpdateProfile" class="profile-form">
          <div class="form-group">
            <label class="form-label">昵称</label>
            <input type="text" v-model="form.nickname" class="form-input" placeholder="您的昵称"/>
          </div>
          <div class="form-group">
            <label class="form-label">个人简介</label>
            <textarea v-model="form.bio" class="form-input" rows="3" placeholder="介绍一下自己..."></textarea>
          </div>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </form>
      </div>
    </div>

    <div class="settings-card card">
      <div class="card-header"><h3>账户安全</h3></div>
      <div class="card-body">
        <p class="text-secondary">如需修改密码，请联系管理员。</p>
      </div>
    </div>

    <div class="settings-card card">
      <div class="card-header"><h3>关于</h3></div>
      <div class="card-body">
        <div class="about-info">
          <p><strong>个人日记</strong> v1.0.0</p>
          <p class="text-secondary">一个简洁优雅的个人日记记录应用，帮助您记录生活中的每一个精彩瞬间。</p>
          <p class="text-muted mt-2">© 2026 Personal Journal. Built with Vue.js & Spring Boot.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import dayjs from 'dayjs'

const authStore = useAuthStore()
const user = computed(() => authStore.user)
const saving = ref(false)

const form = reactive({ nickname: '', bio: '' })

function formatDate(date) {
  return date ? dayjs(date).format('YYYY年MM月DD日') : '-'
}

async function handleUpdateProfile() {
  saving.value = true
  try {
    await authStore.updateProfile({ nickname: form.nickname, bio: form.bio })
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (user.value) {
    form.nickname = user.value.nickname || ''
    form.bio = user.value.bio || ''
  }
})
</script>

<style scoped>
.settings-page { max-width: 600px; animation: fadeIn var(--transition-normal); }
.settings-card { margin-bottom: 1.5rem; }
.card-header h3 { font-size: 1.125rem; font-weight: 600; }
.profile-section { display: flex; align-items: center; gap: 1.5rem; margin-bottom: 2rem; padding-bottom: 2rem; border-bottom: 1px solid var(--border-light); }
.avatar { width: 80px; height: 80px; border-radius: 50%; background: linear-gradient(135deg, var(--primary-500), var(--accent-500)); color: white; display: flex; align-items: center; justify-content: center; font-size: 2rem; font-weight: 700; }
.username { font-size: 1.25rem; font-weight: 600; color: var(--text-primary); }
.email { font-size: 0.875rem; color: var(--text-secondary); margin-top: 0.25rem; }
.join-date { font-size: 0.75rem; color: var(--text-tertiary); margin-top: 0.5rem; }
.profile-form .btn { margin-top: 0.5rem; }
.about-info p { margin-bottom: 0.5rem; }
</style>
