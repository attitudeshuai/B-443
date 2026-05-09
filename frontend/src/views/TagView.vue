<template>
  <div class="tag-page">
    <div class="page-header">
      <h2>标签管理</h2>
      <button class="btn btn-primary" @click="openModal()">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="18" height="18">
          <path d="M12 4v16m8-8H4"/>
        </svg>
        新建标签
      </button>
    </div>

    <div class="tag-grid">
      <div v-if="tags.length === 0" class="empty-state">
        <svg class="empty-state-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
        </svg>
        <div class="empty-state-title">还没有标签</div>
        <div class="empty-state-description">创建标签来标记您的日记</div>
      </div>

      <div v-for="tag in tags" :key="tag.id" class="tag-card" :style="{ borderLeftColor: tag.color }">
        <div class="tag-info">
          <div class="tag-name" :style="{ color: tag.color }">{{ tag.name }}</div>
          <div class="tag-count">{{ tag.journalCount || 0 }} 篇日记</div>
        </div>
        <div class="tag-actions">
          <button class="btn-icon" @click="openModal(tag)">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/>
            </svg>
          </button>
          <button class="btn-icon danger" @click="confirmDelete(tag)">
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
        <div class="modal-header"><h3>{{ editingTag ? '编辑标签' : '新建标签' }}</h3></div>
        <form @submit.prevent="handleSubmit" class="modal-body">
          <div class="form-group">
            <label class="form-label">名称</label>
            <input type="text" v-model="form.name" class="form-input" required maxlength="30" placeholder="标签名称"/>
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
          <button class="btn btn-primary" @click="handleSubmit" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
        </div>
      </div>
    </div>

    <!-- Delete Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click="showDeleteModal = false">
      <div class="modal" @click.stop>
        <div class="modal-header"><h3>确认删除</h3></div>
        <div class="modal-body"><p>确定要删除标签 "{{ tagToDelete?.name }}" 吗？</p></div>
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
const tags = computed(() => journalStore.tags)

const showModal = ref(false)
const showDeleteModal = ref(false)
const editingTag = ref(null)
const tagToDelete = ref(null)
const saving = ref(false)
const deleting = ref(false)

const colors = ['#10b981', '#22c55e', '#84cc16', '#06b6d4', '#3b82f6', '#6366f1', '#8b5cf6', '#ec4899', '#ef4444', '#f59e0b']

const form = reactive({ name: '', color: '#10b981' })

function openModal(tag = null) {
  editingTag.value = tag
  if (tag) {
    form.name = tag.name
    form.color = tag.color || '#10b981'
  } else {
    form.name = ''
    form.color = '#10b981'
  }
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editingTag.value = null
}

async function handleSubmit() {
  saving.value = true
  try {
    if (editingTag.value) {
      await journalStore.updateTag(editingTag.value.id, form)
    } else {
      await journalStore.createTag(form)
    }
    closeModal()
  } finally {
    saving.value = false
  }
}

function confirmDelete(tag) {
  tagToDelete.value = tag
  showDeleteModal.value = true
}

async function handleDelete() {
  deleting.value = true
  try {
    await journalStore.deleteTag(tagToDelete.value.id)
    showDeleteModal.value = false
  } finally {
    deleting.value = false
  }
}

onMounted(() => { journalStore.fetchTags() })
</script>

<style scoped>
.tag-page { animation: fadeIn var(--transition-normal); }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; }
.page-header h2 { font-size: 1.5rem; font-weight: 700; }
.tag-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 1rem; }
.tag-card { display: flex; align-items: center; justify-content: space-between; background: var(--bg-primary); border-radius: var(--radius-lg); padding: 1rem 1.25rem; box-shadow: var(--shadow-sm); border-left: 4px solid; transition: all var(--transition-fast); }
.tag-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
.tag-name { font-weight: 600; margin-bottom: 0.25rem; }
.tag-count { font-size: 0.75rem; color: var(--text-tertiary); }
.tag-actions { display: flex; gap: 0.25rem; }
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
