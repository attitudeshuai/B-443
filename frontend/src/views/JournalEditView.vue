<template>
  <div class="journal-edit-page">
    <form @submit.prevent="handleSubmit" class="journal-form">
      <div class="form-main">
        <div class="form-group">
          <input 
            type="text" 
            v-model="form.title"
            class="title-input"
            placeholder="日记标题..."
            required
            maxlength="200"
          />
        </div>

        <div class="form-group">
          <textarea 
            v-model="form.content"
            class="content-input"
            placeholder="今天发生了什么..."
            rows="15"
          ></textarea>
        </div>
      </div>

      <div class="form-sidebar">
        <div class="sidebar-section">
          <label class="form-label">日期</label>
          <input 
            type="date" 
            v-model="form.journalDate"
            class="form-input"
            required
          />
        </div>

        <div class="sidebar-section">
          <label class="form-label">心情</label>
          <div class="mood-picker">
            <button 
              v-for="(mood, key) in moods" 
              :key="key"
              type="button"
              class="mood-btn"
              :class="{ active: form.mood === key }"
              @click="form.mood = form.mood === key ? '' : key"
              :title="mood.label"
            >
              {{ mood.emoji }}
            </button>
          </div>
        </div>

        <div class="sidebar-section">
          <label class="form-label">天气</label>
          <div class="weather-picker">
            <button 
              v-for="(weather, key) in weathers" 
              :key="key"
              type="button"
              class="weather-btn"
              :class="{ active: form.weather === key }"
              @click="form.weather = form.weather === key ? '' : key"
              :title="weather.label"
            >
              {{ weather.emoji }}
            </button>
          </div>
        </div>

        <div class="sidebar-section">
          <label class="form-label">分类</label>
          <select v-model="form.categoryId" class="form-select">
            <option :value="null">无分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">
              {{ cat.name }}
            </option>
          </select>
        </div>

        <div class="sidebar-section">
          <label class="form-label">标签</label>
          <div class="tags-picker">
            <div 
              v-for="tag in tags" 
              :key="tag.id"
              class="tag-chip"
              :class="{ active: form.tagIds.includes(tag.id) }"
              :style="form.tagIds.includes(tag.id) ? { background: tag.color + '20', color: tag.color, borderColor: tag.color } : {}"
              @click="toggleTag(tag.id)"
            >
              {{ tag.name }}
            </div>
          </div>
          <div v-if="tags.length === 0" class="text-muted text-center">
            <small>暂无标签</small>
          </div>
        </div>

        <div class="sidebar-section">
          <label class="form-label">
            <input type="checkbox" v-model="form.isPrivate" />
            私密日记
          </label>
        </div>

        <div class="form-actions">
          <button type="button" class="btn btn-secondary" @click="goBack">
            取消
          </button>
          <button type="submit" class="btn btn-primary" :disabled="saving">
            <span v-if="saving" class="spinner"></span>
            <span v-else>{{ isEdit ? '保存修改' : '发布日记' }}</span>
          </button>
        </div>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useJournalStore } from '@/stores/journal'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const journalStore = useJournalStore()

const isEdit = computed(() => !!route.params.id)
const saving = ref(false)

const form = reactive({
  title: '',
  content: '',
  mood: '',
  weather: '',
  isPrivate: true,
  journalDate: dayjs().format('YYYY-MM-DD'),
  categoryId: null,
  tagIds: []
})

const categories = computed(() => journalStore.categories)
const tags = computed(() => journalStore.tags)

const moods = {
  happy: { emoji: '😊', label: '开心' },
  sad: { emoji: '😢', label: '难过' },
  neutral: { emoji: '😐', label: '平静' },
  excited: { emoji: '🎉', label: '兴奋' },
  anxious: { emoji: '😰', label: '焦虑' },
  calm: { emoji: '😌', label: '放松' },
  angry: { emoji: '😠', label: '生气' },
  tired: { emoji: '😴', label: '疲惫' }
}

const weathers = {
  sunny: { emoji: '☀️', label: '晴天' },
  cloudy: { emoji: '☁️', label: '多云' },
  rainy: { emoji: '🌧️', label: '下雨' },
  snowy: { emoji: '❄️', label: '下雪' },
  windy: { emoji: '💨', label: '刮风' },
  foggy: { emoji: '🌫️', label: '有雾' }
}

function toggleTag(tagId) {
  const index = form.tagIds.indexOf(tagId)
  if (index === -1) {
    form.tagIds.push(tagId)
  } else {
    form.tagIds.splice(index, 1)
  }
}

function goBack() {
  router.back()
}

async function handleSubmit() {
  saving.value = true
  
  try {
    const data = {
      title: form.title,
      content: form.content,
      mood: form.mood || null,
      weather: form.weather || null,
      isPrivate: form.isPrivate,
      journalDate: form.journalDate,
      categoryId: form.categoryId,
      tagIds: form.tagIds
    }
    
    if (isEdit.value) {
      await journalStore.updateJournal(route.params.id, data)
    } else {
      await journalStore.createJournal(data)
    }
    
    router.push('/journals')
  } catch (error) {
    console.error('保存失败:', error)
  } finally {
    saving.value = false
  }
}

async function loadJournal() {
  if (!isEdit.value) return
  
  try {
    const journal = await journalStore.fetchJournalById(route.params.id)
    form.title = journal.title
    form.content = journal.content || ''
    form.mood = journal.mood || ''
    form.weather = journal.weather || ''
    form.isPrivate = journal.isPrivate
    form.journalDate = journal.journalDate
    form.categoryId = journal.category?.id || null
    form.tagIds = journal.tags?.map(t => t.id) || []
  } catch (error) {
    console.error('加载日记失败:', error)
    router.push('/journals')
  }
}

onMounted(() => {
  journalStore.fetchCategories()
  journalStore.fetchTags()
  loadJournal()
})
</script>

<style scoped>
.journal-edit-page {
  animation: fadeIn var(--transition-normal);
}

.journal-form {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 2rem;
  min-height: calc(100vh - 200px);
}

.form-main {
  background: var(--bg-primary);
  border-radius: var(--radius-xl);
  padding: 2rem;
  box-shadow: var(--shadow-sm);
}

.title-input {
  width: 100%;
  border: none;
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-primary);
  padding: 0;
  margin-bottom: 1.5rem;
  outline: none;
}

.title-input::placeholder {
  color: var(--text-tertiary);
}

.content-input {
  width: 100%;
  border: none;
  font-size: 1rem;
  line-height: 1.8;
  color: var(--text-primary);
  resize: none;
  outline: none;
  min-height: 400px;
}

.content-input::placeholder {
  color: var(--text-tertiary);
}

.form-sidebar {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.sidebar-section {
  background: var(--bg-primary);
  border-radius: var(--radius-lg);
  padding: 1.25rem;
  box-shadow: var(--shadow-xs);
}

.sidebar-section .form-label {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.sidebar-section .form-label input[type="checkbox"] {
  width: 16px;
  height: 16px;
}

.mood-picker, .weather-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.mood-btn, .weather-btn {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.25rem;
  background: var(--bg-tertiary);
  border: 2px solid transparent;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.mood-btn:hover, .weather-btn:hover {
  transform: scale(1.1);
}

.mood-btn.active, .weather-btn.active {
  background: var(--primary-100);
  border-color: var(--primary-500);
}

.tags-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-chip {
  padding: 0.375rem 0.75rem;
  font-size: 0.8125rem;
  background: var(--bg-tertiary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.tag-chip:hover {
  border-color: var(--primary-400);
}

.tag-chip.active {
  border-width: 1.5px;
}

.form-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: auto;
  padding-top: 1rem;
}

.form-actions .btn {
  flex: 1;
}

@media (max-width: 900px) {
  .journal-form {
    grid-template-columns: 1fr;
  }
  
  .form-sidebar {
    order: -1;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 1rem;
  }
  
  .form-actions {
    grid-column: span 2;
  }
}

@media (max-width: 640px) {
  .form-sidebar {
    grid-template-columns: 1fr;
  }
  
  .form-actions {
    grid-column: span 1;
  }
}
</style>
