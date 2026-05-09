import api from './api'

export const authService = {
  login(credentials) {
    return api.post('/auth/login', credentials)
  },
  
  register(data) {
    return api.post('/auth/register', data)
  },
  
  getCurrentUser() {
    return api.get('/auth/me')
  },
  
  updateProfile(data) {
    const params = new URLSearchParams()
    if (data.nickname) params.append('nickname', data.nickname)
    if (data.bio) params.append('bio', data.bio)
    if (data.avatar) params.append('avatar', data.avatar)
    return api.put(`/auth/profile?${params.toString()}`)
  }
}

export const journalService = {
  getList(params = {}) {
    return api.get('/journals', { params })
  },
  
  getById(id) {
    return api.get(`/journals/${id}`)
  },
  
  create(data) {
    return api.post('/journals', data)
  },
  
  update(id, data) {
    return api.put(`/journals/${id}`, data)
  },
  
  delete(id) {
    return api.delete(`/journals/${id}`)
  },
  
  search(keyword, params = {}) {
    return api.get('/journals/search', { params: { keyword, ...params } })
  },
  
  getByTag(tagId, params = {}) {
    return api.get(`/journals/by-tag/${tagId}`, { params })
  },
  
  getByDateRange(startDate, endDate) {
    return api.get('/journals/by-date', { params: { startDate, endDate } })
  },
  
  getRecent() {
    return api.get('/journals/recent')
  }
}

export const categoryService = {
  getList() {
    return api.get('/categories')
  },
  
  getById(id) {
    return api.get(`/categories/${id}`)
  },
  
  create(data) {
    return api.post('/categories', data)
  },
  
  update(id, data) {
    return api.put(`/categories/${id}`, data)
  },
  
  delete(id) {
    return api.delete(`/categories/${id}`)
  }
}

export const tagService = {
  getList() {
    return api.get('/tags')
  },
  
  getById(id) {
    return api.get(`/tags/${id}`)
  },
  
  create(data) {
    return api.post('/tags', data)
  },
  
  update(id, data) {
    return api.put(`/tags/${id}`, data)
  },
  
  delete(id) {
    return api.delete(`/tags/${id}`)
  }
}

export const dashboardService = {
  getStats() {
    return api.get('/dashboard/stats')
  }
}
