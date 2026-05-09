<template>
  <div class="category-page">
    <div class="page-header">
      <h2>分类管理</h2>
      <button class="btn btn-primary" @click="openModal()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
          <path d="M12 4v16m8-8H4"/>
        </svg>
        新建分类
      </button>
    </div>

    <div class="category-grid">
      <div v-if="categories.length === 0" class="empty-state">
        <svg class="empty-state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"/>
        </svg>
        <div class="empty-state-title">还没有分类</div>
        <div class="empty-state-description">创建分类来整理您的日记</div>
      </div>

      <div v-for="category in categories" :key="category.id" class="category-card">
        <div class="category-color" :style="{ background: category.color }"></div>
        <div class="category-info">
          <div class="category-name">{{ category.name }}</div>
          <div class="category-description">{{ category.description || '暂无描述' }}</div>
          <div class="category-count">{{ category.journalCount || 0 }} 篇日记</div>
        </div>
        <div class="category-actions">
          <button class="btn-icon" @click="openModal(category)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
            </svg>
          </button>
          <button class="btn-icon danger" @click="confirmDelete(category)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
            </svg>
          </button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal" @click.stop>
        <div class="modal-header">
          <h3>{{ editingCategory ? '编辑分类' : '新建分类' }}</h3>
        </div>
        <form @submit.prevent="handleSubmit" class="modal-body">
          <div class="form-group">
            <label class="form-label">名称</label>
            <input type="text" v-model="form.name" class="form-input" required maxlength="50" placeholder="分类名称"/>
          </div>
          <div class="form-group">
            <label class="form-label">描述</label>
            <input type="text" v-model="form.description" class="form-input" maxlength="200" placeholder="分类描述（可选）"/>
          </div>
          <div class="form-group">
            <label class="form-label">颜色</label>
            <div class="color-picker">
              <div v-for="color in colors" :key="color" class="color-option" :class="{ active: form.color === color }" :style="{ background: color }" @click="form.color = color"></div>
            </div>
          </div>
        </form>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" @click="closeModal">取消</button>
          <button class="btn btn-primary" @click="handleSubmit" :disabled="saving">
            {{ saving ? '保存中...' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header"><h3>确认删除</h3></div>
        <div class="modal-body"><p>确定要删除分类 "{{ categoryToDelete?.name }}" 吗？</p></div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showDeleteModal = false">取消</button>
          <button class="btn btn-danger" @click="handleDelete" :disabled="deleting">{{ deleting ? '删除中...' : '确认删除' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useJournalStore } from '@/stores/journal'

const journalStore = useJournalStore()
const categories = computed(() => journalStore.categories)

const showModal = ref(false)
const showDeleteModal = ref(false)
const editingCategory = ref(null)
const categoryToDelete = ref(null)
const saving = ref(false)
const deleting = ref(false)

const colors = ['#6366f1', '#8b5cf6', '#ec4899', '#ef4444', '#f59e0b', '#22c55e', '#06b6d4', '#3b82f6']

const form = reactive({ name: '', description: '', color: '#6366f1' })

function openModal(category = null) {
  editingCategory.value = category
  if (category) {
    form.name = category.name
    form.description = category.description || ''
    form.color = category.color || '#6366f1'
  } else {
    form.name = ''
    form.description = ''
    form.color = '#6366f1'
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingCategory.value = null
}

async function handleSubmit() {
  saving.value = true
  try {
    if (editingCategory.value) {
      await journalStore.updateCategory(editingCategory.value.id, form)
    } else {
      await journalStore.createCategory(form)
    }
    closeModal()
  } finally {
    saving.value = false
  }
}

function confirmDelete(category) {
  categoryToDelete.value = category
  showDeleteModal.value = true
}

async function handleDelete() {
  deleting.value = true
  try {
    await journalStore.deleteCategory(categoryToDelete.value.id)
    showDeleteModal.value = false
  } finally {
    deleting.value = false
  }
}

onMounted(() => { journalStore.fetchCategories() })
</script>

<style scoped>
.category-page { animation: fadeIn var(--transition-normal); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.page-header h2 { font-size: 1.5rem; font-weight: 700; }
.category-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 1rem; }
.category-card { display: flex; align-items: center; gap: 1rem; background: var(--bg-primary); border-radius: var(--radius-xl); padding: 1.25rem; box-shadow: var(--shadow-sm); transition: all var(--transition-fast); }
.category-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.category-color { width: 48px; height: 48px; border-radius: var(--radius-lg); flex-shrink: 0; }
.category-info { flex: 1; min-width: 0; }
.category-name { font-weight: 600; color: var(--text-primary); margin-bottom: 0.25rem; }
.category-description { font-size: 0.875rem; color: var(--text-secondary); margin-bottom: 0.25rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.category-count { font-size: 0.75rem; color: var(--text-tertiary); }
.category-actions { display: flex; gap: 0.25rem; }
.btn-icon { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; background: var(--bg-tertiary); border: none; border-radius: var(--radius-md); cursor: pointer; color: var(--text-secondary); transition: all var(--transition-fast); }
.btn-icon:hover { background: var(--primary-100); color: var(--primary-600); }
.btn-icon.danger:hover { background: #fef2f2; color: var(--error); }
.btn-icon svg { width: 16px; height: 16px; }
.color-picker { display: flex; gap: 0.5rem; flex-wrap: wrap; }
.color-option { width: 32px; height: 32px; border-radius: var(--radius-full); cursor: pointer; transition: all var(--transition-fast); border: 3px solid transparent; }
.color-option.active { border-color: var(--text-primary); transform: scale(1.1); }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: var(--z-modal); }
.modal { background: var(--bg-primary); border-radius: var(--radius-xl); width: 100%; max-width: 420px; box-shadow: var(--shadow-xl); }
.modal-header { padding: 1.25rem 1.5rem; border-bottom: 1px solid var(--border-light); }
.modal-header h3 { font-size: 1.125rem; font-weight: 600; }
.modal-body { padding: 1.5rem; }
.modal-footer { padding: 1rem 1.5rem; display: flex; justify-content: flex-end; gap: 0.75rem; border-top: 1px solid var(--border-light); }
</style>
