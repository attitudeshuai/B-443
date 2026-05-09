<template>
  <div class="dashboard">
    <!-- Stats Cards -->
    <div class="stats-grid" v-if="stats">
      <div class="stat-card">
        <div class="stat-icon journals">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalJournals || 0 }}</div>
          <div class="stat-label">日记总数</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon words">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ formatNumber(stats.totalWords || 0) }}</div>
          <div class="stat-label">总字数</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon streak">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M17.657 18.657A8 8 0 016.343 7.343S7 9 9 10c0-2 .5-5 2.986-7C14 5 16.09 5.777 17.656 7.343A7.975 7.975 0 0120 13a7.975 7.975 0 01-2.343 5.657z"/>
            <path d="M9.879 16.121A3 3 0 1012.015 11L11 14H9c0 .768.293 1.536.879 2.121z"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.currentStreak || 0 }}</div>
          <div class="stat-label">连续天数</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon categories">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
          </svg>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ (stats.totalCategories || 0) + (stats.totalTags || 0) }}</div>
          <div class="stat-label">分类与标签</div>
        </div>
      </div>
    </div>

    <div class="dashboard-content">
      <!-- Recent Journals -->
      <div class="card recent-journals">
        <div class="card-header">
          <h3>最近日记</h3>
          <router-link to="/journals" class="btn btn-ghost btn-sm">查看全部</router-link>
        </div>
        <div class="card-body">
          <div v-if="loading" class="loading-state">
            <div class="spinner"></div>
            <span>加载中...</span>
          </div>
          <div v-else-if="!stats?.recentJournals?.length" class="empty-state">
            <svg class="empty-state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
            </svg>
            <div class="empty-state-title">还没有日记</div>
            <div class="empty-state-description">开始写下您的第一篇日记吧</div>
            <router-link to="/journals/new" class="btn btn-primary mt-4">写日记</router-link>
          </div>
          <div v-else class="journal-list">
            <router-link 
              v-for="journal in stats.recentJournals" 
              :key="journal.id"
              :to="`/journals/${journal.id}`"
              class="journal-item"
            >
              <div class="journal-date">
                <div class="date-day">{{ formatDay(journal.journalDate) }}</div>
                <div class="date-month">{{ formatMonth(journal.journalDate) }}</div>
              </div>
              <div class="journal-content">
                <div class="journal-title">{{ journal.title }}</div>
                <div class="journal-preview">{{ stripHtml(journal.content) }}</div>
                <div class="journal-meta">
                  <span v-if="journal.mood" class="mood">{{ getMoodLabel(journal.mood) }}</span>
                  <span class="word-count">{{ journal.wordCount }} 字</span>
                </div>
              </div>
            </router-link>
          </div>
        </div>
      </div>

      <!-- Mood Stats -->
      <div class="card mood-stats">
        <div class="card-header">
          <h3>心情统计</h3>
        </div>
        <div class="card-body">
          <div v-if="!stats?.moodStats || Object.keys(stats.moodStats).length === 0" class="empty-state">
            <div class="empty-state-description">暂无心情数据</div>
          </div>
          <div v-else class="mood-chart">
            <div 
              v-for="(count, mood) in stats.moodStats" 
              :key="mood"
              class="mood-bar"
            >
              <div class="mood-label">{{ getMoodLabel(mood) }}</div>
              <div class="mood-progress">
                <div 
                  class="mood-fill" 
                  :style="{ width: getMoodPercentage(count) + '%', background: getMoodColor(mood) }"
                ></div>
              </div>
              <div class="mood-count">{{ count }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useJournalStore } from '@/stores/journal'
import dayjs from 'dayjs'

const journalStore = useJournalStore()

const stats = computed(() => journalStore.dashboardStats)
const loading = computed(() => journalStore.loading)

const moodMap = {
  happy: { label: '开心', color: '#22c55e' },
  sad: { label: '难过', color: '#64748b' },
  neutral: { label: '平静', color: '#3b82f6' },
  excited: { label: '兴奋', color: '#f59e0b' },
  anxious: { label: '焦虑', color: '#ef4444' },
  calm: { label: '放松', color: '#06b6d4' },
  angry: { label: '生气', color: '#dc2626' },
  tired: { label: '疲惫', color: '#8b5cf6' }
}

function formatNumber(num) {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toLocaleString()
}

function formatDay(date) {
  return dayjs(date).format('DD')
}

function formatMonth(date) {
  return dayjs(date).format('MM月')
}

function stripHtml(html) {
  if (!html) return ''
  const text = html.replace(/<[^>]*>/g, '')
  return text.length > 80 ? text.slice(0, 80) + '...' : text
}

function getMoodLabel(mood) {
  return moodMap[mood]?.label || mood
}

function getMoodColor(mood) {
  return moodMap[mood]?.color || '#6b7280'
}

function getMoodPercentage(count) {
  if (!stats.value?.moodStats) return 0
  const total = Object.values(stats.value.moodStats).reduce((a, b) => a + b, 0)
  return total > 0 ? (count / total) * 100 : 0
}

onMounted(() => {
  journalStore.fetchDashboardStats()
})
</script>

<style scoped>
.dashboard {
  animation: fadeIn var(--transition-normal);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-icon svg {
  width: 26px;
  height: 26px;
  color: white;
}

.stat-icon.journals {
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
}

.stat-icon.words {
  background: linear-gradient(135deg, #22c55e, #10b981);
}

.stat-icon.streak {
  background: linear-gradient(135deg, #f59e0b, #ef4444);
}

.stat-icon.categories {
  background: linear-gradient(135deg, #06b6d4, #3b82f6);
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.dashboard-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  color: var(--text-secondary);
  gap: 1rem;
}

.journal-list {
  display: flex;
  flex-direction: column;
}

.journal-item {
  display: flex;
  gap: 1rem;
  padding: 1rem 0;
  border-bottom: 1px solid var(--border-light);
  text-decoration: none;
  transition: all var(--transition-fast);
}

.journal-item:last-child {
  border-bottom: none;
}

.journal-item:hover {
  background: var(--bg-secondary);
  margin: 0 -1.5rem;
  padding: 1rem 1.5rem;
  border-radius: var(--radius-lg);
}

.journal-date {
  width: 50px;
  text-align: center;
  flex-shrink: 0;
}

.date-day {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--primary-600);
}

.date-month {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.journal-content {
  flex: 1;
  min-width: 0;
}

.journal-title {
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.25rem;
}

.journal-preview {
  font-size: 0.875rem;
  color: var(--text-secondary);
  margin-bottom: 0.5rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.journal-meta {
  display: flex;
  gap: 0.75rem;
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.mood {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.mood-chart {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.mood-bar {
  display: grid;
  grid-template-columns: 60px 1fr 40px;
  align-items: center;
  gap: 0.75rem;
}

.mood-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.mood-progress {
  height: 8px;
  background: var(--bg-tertiary);
  border-radius: 4px;
  overflow: hidden;
}

.mood-fill {
  height: 100%;
  border-radius: 4px;
  transition: width var(--transition-slow);
}

.mood-count {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--text-primary);
  text-align: right;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .dashboard-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
