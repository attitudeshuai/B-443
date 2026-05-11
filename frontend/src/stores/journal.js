import { defineStore } from 'pinia'
import { ref } from 'vue'
import { journalService, categoryService, tagService, dashboardService } from '@/services'

export const useJournalStore = defineStore('journal', () => {
  // State
  const journals = ref([])
  const currentJournal = ref(null)
  const categories = ref([])
  const tags = ref([])
  const dashboardStats = ref(null)
  const pagination = ref({
    page: 0,
    size: 10,
    totalPages: 0,
    totalElements: 0
  })
  const loading = ref(false)
  const filters = ref({
    categoryId: null,
    tagId: null,
    keyword: ''
  })

  // Actions
  async function fetchJournals(params = {}) {
    loading.value = true
    try {
      const response = await journalService.getList({
        page: pagination.value.page,
        size: pagination.value.size,
        categoryId: filters.value.categoryId,
        ...params
      })
      journals.value = response.data.content
      pagination.value = {
        page: response.data.number,
        size: response.data.size,
        totalPages: response.data.totalPages,
        totalElements: response.data.totalElements
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchJournalById(id) {
    loading.value = true
    try {
      const response = await journalService.getById(id)
      currentJournal.value = response.data
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function createJournal(data) {
    loading.value = true
    try {
      const response = await journalService.create(data)
      journals.value.unshift(response.data)
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function updateJournal(id, data) {
    loading.value = true
    try {
      const response = await journalService.update(id, data)
      const index = journals.value.findIndex(j => j.id === Number(id))
      if (index !== -1) {
        journals.value[index] = response.data
      }
      currentJournal.value = response.data
      return response.data
    } finally {
      loading.value = false
    }
  }

  async function deleteJournal(id) {
    loading.value = true
    try {
      await journalService.delete(id)
      journals.value = journals.value.filter(j => j.id !== Number(id))
    } finally {
      loading.value = false
    }
  }

  async function searchJournals(keyword) {
    loading.value = true
    try {
      const response = await journalService.search(keyword, {
        page: 0,
        size: pagination.value.size
      })
      journals.value = response.data.content
      pagination.value = {
        page: response.data.number,
        size: response.data.size,
        totalPages: response.data.totalPages,
        totalElements: response.data.totalElements
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchCategories() {
    try {
      const response = await categoryService.getList()
      categories.value = response.data
    } catch (error) {
      console.error('获取分类失败:', error)
    }
  }

  async function createCategory(data) {
    const response = await categoryService.create(data)
    categories.value.push(response.data)
    return response.data
  }

  async function updateCategory(id, data) {
    const response = await categoryService.update(id, data)
    const index = categories.value.findIndex(c => c.id === id)
    if (index !== -1) {
      categories.value[index] = response.data
    }
    return response.data
  }

  async function deleteCategory(id) {
    await categoryService.delete(id)
    categories.value = categories.value.filter(c => c.id !== id)
  }

  async function fetchTags() {
    try {
      const response = await tagService.getList()
      tags.value = response.data
    } catch (error) {
      console.error('获取标签失败:', error)
    }
  }

  async function createTag(data) {
    const response = await tagService.create(data)
    tags.value.push(response.data)
    return response.data
  }

  async function updateTag(id, data) {
    const response = await tagService.update(id, data)
    const index = tags.value.findIndex(t => t.id === id)
    if (index !== -1) {
      tags.value[index] = response.data
    }
    return response.data
  }

  async function deleteTag(id) {
    await tagService.delete(id)
    tags.value = tags.value.filter(t => t.id !== id)
  }

  async function fetchDashboardStats() {
    loading.value = true
    try {
      const response = await dashboardService.getStats()
      dashboardStats.value = response.data
    } finally {
      loading.value = false
    }
  }

  function setPage(page) {
    pagination.value.page = page
  }

  function setFilter(key, value) {
    filters.value[key] = value
    pagination.value.page = 0
  }

  function clearFilters() {
    filters.value = {
      categoryId: null,
      tagId: null,
      keyword: ''
    }
    pagination.value.page = 0
  }

  return {
    journals,
    currentJournal,
    categories,
    tags,
    dashboardStats,
    pagination,
    loading,
    filters,
    fetchJournals,
    fetchJournalById,
    createJournal,
    updateJournal,
    deleteJournal,
    searchJournals,
    fetchCategories,
    createCategory,
    updateCategory,
    deleteCategory,
    fetchTags,
    createTag,
    updateTag,
    deleteTag,
    fetchDashboardStats,
    setPage,
    setFilter,
    clearFilters
  }
})
