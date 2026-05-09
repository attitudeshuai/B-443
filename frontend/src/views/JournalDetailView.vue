<template>
  <div class="journal-detail-page" v-if="journal">
    <div class="journal-header">
      <div class="journal-meta">
        <div class="journal-date">
          {{ formatDate(journal.journalDate) }}
        </div>
        <div class="journal-info">
          <span v-if="journal.mood" class="mood">{{ getMoodEmoji(journal.mood) }} {{ getMoodLabel(journal.mood) }}</span>
          <span v-if="journal.weather" class="weather">{{ getWeatherEmoji(journal.weather) }}</span>
          <span class="word-count">{{ journal.wordCount }} 字</span>
        </div>
      </div>
      <div class="journal-actions">
        <router-link :to="`/journals/${journal.id}/edit`" class="btn btn-secondary">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
            <path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
          </svg>
          编辑
        </router-link>
        <button class="btn btn-danger" @click="confirmDelete">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
            <path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
          </svg>
          删除
        </button>
      </div>
    </div>

    <article class="journal-content card">
      <h1 class="journal-title">{{ journal.title }}</h1>
      
      <div class="tags-row" v-if="journal.category || journal.tags?.length">
        <span v-if="journal.category" class="category-tag" :style="{ background: journal.category.color + '20', color: journal.category.color }">
          {{ journal.category.name }}
        </span>
        <span v-for="tag in journal.tags" :key="tag.id" class="tag" :style="{ background: tag.color + '20', color: tag.color }">
          {{ tag.name }}
        </span>
      </div>

      <div class="content-body" v-html="journal.content || '<p class=\'text-muted\'>暂无内容</p>'"></div>
    </article>

    <!-- Delete Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>确认删除</h3>
        </div>
        <div class="modal-body">
          <p>确定要删除这篇日记吗？此操作无法撤销。</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showDeleteModal = false">取消</button>
          <button class="btn btn-danger" @click="handleDelete" :disabled="deleting">
            {{ deleting ? '删除中...' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="loading-state">
    <div class="spinner"></div>
    <span>加载中...</span>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useJournalStore } from '@/stores/journal'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const journalStore = useJournalStore()

const showDeleteModal = ref(false)
const deleting = ref(false)

const journal = computed(() => journalStore.currentJournal)

const moodMap = {
  happy: { emoji: '😊', label: '开心' },
  sad: { emoji: '😢', label: '难过' },
  neutral: { emoji: '😐', label: '平静' },
  excited: { emoji: '🎉', label: '兴奋' },
  anxious: { emoji: '😰', label: '焦虑' },
  calm: { emoji: '😌', label: '放松' },
  angry: { emoji: '😠', label: '生气' },
  tired: { emoji: '😴', label: '疲惫' }
}

const weatherMap = {
  sunny: '☀️',
  cloudy: '☁️',
  rainy: '🌧️',
  snowy: '❄️',
  windy: '💨',
  foggy: '🌫️'
}

function formatDate(date) {
  return dayjs(date).format('YYYY年MM月DD日 dddd')
}

function getMoodEmoji(mood) {
  return moodMap[mood]?.emoji || ''
}

function getMoodLabel(mood) {
  return moodMap[mood]?.label || mood
}

function getWeatherEmoji(weather) {
  return weatherMap[weather] || ''
}

function confirmDelete() {
  showDeleteModal.value = true
}

async function handleDelete() {
  deleting.value = true
  try {
    await journalStore.deleteJournal(journal.value.id)
    router.push('/journals')
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  journalStore.fetchJournalById(route.params.id)
})
</script>

<style scoped>
.journal-detail-page {
  max-width: 800px;
  margin: 0 auto;
  animation: fadeIn var(--transition-normal);
}

.journal-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1.5rem;
}

.journal-date {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.journal-info {
  display: flex;
  gap: 1rem;
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.mood {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.journal-actions {
  display: flex;
  gap: 0.75rem;
}

.journal-content {
  padding: 2.5rem;
}

.journal-title {
  font-size: 2rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 1rem;
  line-height: 1.3;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid var(--border-light);
}

.category-tag, .tag {
  padding: 0.25rem 0.75rem;
  font-size: 0.8125rem;
  font-weight: 500;
  border-radius: var(--radius-full);
}

.content-body {
  font-size: 1.0625rem;
  line-height: 1.9;
  color: var(--text-primary);
}

.content-body :deep(p) {
  margin-bottom: 1.25rem;
}

.content-body :deep(h1),
.content-body :deep(h2),
.content-body :deep(h3) {
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 5rem;
  color: var(--text-secondary);
  gap: 1rem;
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: var(--z-modal);
}

.modal {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 420px;
  box-shadow: var(--shadow-xl);
}

.modal-header {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--border-light);
}

.modal-header h3 {
  font-size: 1.125rem;
  font-weight: 600;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  border-top: 1px solid var(--border-light);
}
</style>
