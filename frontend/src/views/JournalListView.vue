<template>
  <div class="journal-list-page">
    <!-- Filters -->
    <div class="filters-bar">
      <div class="search-box">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
        </svg>
        <input 
          type="text" 
          v-model="searchKeyword"
          placeholder="搜索日记..."
          @keyup.enter="handleSearch"
        />
        <button v-if="searchKeyword" class="clear-btn" @click="clearSearch">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 18L18 6M6 6l12 12"/>
          </svg>
        </button>
      </div>

      <div class="filter-actions">
        <select v-model="selectedCategory" class="form-select" @change="handleCategoryChange">
          <option :value="null">全部分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">
            {{ cat.name }}
          </option>
        </select>
      </div>
    </div>

    <!-- Journal Grid -->
    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <span>加载中...</span>
    </div>

    <div v-else-if="journals.length === 0" class="empty-state">
      <svg class="empty-state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
      </svg>
      <div class="empty-state-title">{{ searchKeyword ? '没有找到匹配的日记' : '还没有日记' }}</div>
      <div class="empty-state-description">
        {{ searchKeyword ? '试试其他关键词' : '开始写下您的第一篇日记吧' }}
      </div>
      <router-link v-if="!searchKeyword" to="/journals/new" class="btn btn-primary mt-4">写日记</router-link>
    </div>

    <div v-else class="journal-grid">
      <div 
        v-for="journal in journals" 
        :key="journal.id"
        class="journal-card"
        @click="goToJournal(journal.id)"
      >
        <div class="card-header">
          <div class="journal-date">
            <span class="day">{{ formatDay(journal.journalDate) }}</span>
            <span class="month-year">{{ formatMonthYear(journal.journalDate) }}</span>
          </div>
          <div class="journal-actions">
            <button class="btn-icon" @click.stop="editJournal(journal.id)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
              </svg>
            </button>
            <button class="btn-icon danger" @click.stop="confirmDelete(journal)">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
              </svg>
            </button>
          </div>
        </div>

        <h3 class="journal-title">{{ journal.title }}</h3>
        <p class="journal-preview">{{ stripHtml(journal.content) }}</p>

        <div class="card-footer">
          <div class="tags">
            <span v-if="journal.category" class="category-tag" :style="{ background: journal.category.color + '20', color: journal.category.color }">
              {{ journal.category.name }}
            </span>
            <span v-for="tag in journal.tags?.slice(0, 2)" :key="tag.id" class="tag" :style="{ background: tag.color + '20', color: tag.color }">
              {{ tag.name }}
            </span>
          </div>
          <div class="meta">
            <span v-if="journal.mood" class="mood">{{ getMoodEmoji(journal.mood) }}</span>
            <span class="word-count">{{ journal.wordCount }}字</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="pagination.totalPages > 1" class="pagination">
      <button 
        class="btn btn-secondary"
        :disabled="pagination.page === 0"
        @click="goToPage(pagination.page - 1)"
      >
        上一页
      </button>
      <span class="page-info">
        第 {{ pagination.page + 1 }} 页 / 共 {{ pagination.totalPages }} 页
      </span>
      <button 
        class="btn btn-secondary"
        :disabled="pagination.page >= pagination.totalPages - 1"
        @click="goToPage(pagination.page + 1)"
      >
        下一页
      </button>
    </div>

    <!-- Delete Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>确认删除</h3>
        </div>
        <div class="modal-body">
          <p>确定要删除日记 "{{ journalToDelete?.title }}" 吗？此操作无法撤销。</p>
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
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useJournalStore } from '@/stores/journal'
import dayjs from 'dayjs'

const router = useRouter()
const journalStore = useJournalStore()

const searchKeyword = ref('')
const selectedCategory = ref(null)
const showDeleteModal = ref(false)
const journalToDelete = ref(null)
const deleting = ref(false)

const journals = computed(() => journalStore.journals)
const categories = computed(() => journalStore.categories)
const pagination = computed(() => journalStore.pagination)
const loading = computed(() => journalStore.loading)

const moodEmojiMap = {
  happy: '😊',
  sad: '😢',
  neutral: '😐',
  excited: '🎉',
  anxious: '😰',
  calm: '😌',
  angry: '😠',
  tired: '😴'
}

function formatDay(date) {
  return dayjs(date).format('DD')
}

function formatMonthYear(date) {
  return dayjs(date).format('YYYY年MM月')
}

function stripHtml(html) {
  if (!html) return ''
  const text = html.replace(/<[^>]*>/g, '')
  return text.length > 100 ? text.slice(0, 100) + '...' : text
}

function getMoodEmoji(mood) {
  return moodEmojiMap[mood] || ''
}

function goToJournal(id) {
  router.push(`/journals/${id}`)
}

function editJournal(id) {
  router.push(`/journals/${id}/edit`)
}

function confirmDelete(journal) {
  journalToDelete.value = journal
  showDeleteModal.value = true
}

async function handleDelete() {
  if (!journalToDelete.value) return
  
  deleting.value = true
  try {
    await journalStore.deleteJournal(journalToDelete.value.id)
    showDeleteModal.value = false
    journalToDelete.value = null
  } finally {
    deleting.value = false
  }
}

function handleSearch() {
  if (searchKeyword.value.trim()) {
    journalStore.searchJournals(searchKeyword.value.trim())
  } else {
    journalStore.fetchJournals()
  }
}

function clearSearch() {
  searchKeyword.value = ''
  journalStore.clearFilters()
  journalStore.fetchJournals()
}

function handleCategoryChange() {
  journalStore.setFilter('categoryId', selectedCategory.value)
  journalStore.fetchJournals()
}

function goToPage(page) {
  journalStore.setPage(page)
  journalStore.fetchJournals()
}

onMounted(() => {
  journalStore.fetchJournals()
  journalStore.fetchCategories()
})
</script>

<style scoped>
.journal-list-page {
  animation: fadeIn var(--transition-normal);
}

.filters-bar {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  background: var(--bg-primary);
  border: 1px solid var(--border-medium);
  border-radius: var(--radius-lg);
}

.search-box svg {
  width: 20px;
  height: 20px;
  color: var(--text-tertiary);
}

.search-box input {
  flex: 1;
  border: none;
  background: none;
  font-size: 0.9375rem;
  color: var(--text-primary);
  outline: none;
}

.clear-btn {
  padding: 4px;
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-tertiary);
}

.filter-actions .form-select {
  min-width: 150px;
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

.journal-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
}

.journal-card {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  padding: 1.5rem;
  cursor: pointer;
  transition: all var(--transition-fast);
  box-shadow: var(--shadow-sm);
}

.journal-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.journal-date .day {
  font-size: 2rem;
  font-weight: 700;
  color: var(--primary-600);
  line-height: 1;
}

.journal-date .month-year {
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.journal-actions {
  display: flex;
  gap: 0.25rem;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.journal-card:hover .journal-actions {
  opacity: 1;
}

.btn-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-tertiary);
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.btn-icon:hover {
  background: var(--primary-100);
  color: var(--primary-600);
}

.btn-icon.danger:hover {
  background: #fef2f2;
  color: var(--error);
}

.btn-icon svg {
  width: 16px;
  height: 16px;
}

.journal-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.journal-preview {
  font-size: 0.875rem;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 1rem;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.category-tag, .tag {
  padding: 0.25rem 0.625rem;
  font-size: 0.75rem;
  font-weight: 500;
  border-radius: var(--radius-full);
}

.meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.75rem;
  color: var(--text-tertiary);
}

.mood {
  font-size: 1rem;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border-light);
}

.page-info {
  color: var(--text-secondary);
  font-size: 0.875rem;
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
  animation: fadeIn var(--transition-fast);
}

.modal {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  width: 100%;
  max-width: 420px;
  box-shadow: var(--shadow-xl);
  animation: slideUp var(--transition-normal);
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

.modal-body p {
  color: var(--text-secondary);
}

.modal-footer {
  padding: 1rem 1.5rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  border-top: 1px solid var(--border-light);
}

@media (max-width: 640px) {
  .filters-bar {
    flex-direction: column;
  }
  
  .journal-grid {
    grid-template-columns: 1fr;
  }
}
</style>
