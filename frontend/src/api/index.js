import axios from 'axios'

const api = axios.create({
  baseURL: import.meta.env.PROD ? '/api' : 'http://localhost:8088/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器：自动附加 token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
api.interceptors.response.use(
  response => response.data,
  error => {
    const message = error.response?.data?.message || '网络错误，请稍后重试'
    return Promise.reject({ message })
  }
)

export default api
