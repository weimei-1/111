import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User, LoginDTO } from '@/types'
import { login as loginApi } from '@/api'

/** 用户状态管理 */
export const useUserStore = defineStore('user', () => {
  // ========== State ==========
  /** JWT Token */
  const token = ref<string>(localStorage.getItem('token') || '')
  /** 当前登录用户信息 */
  let savedUi: User | null = null
  try {
    const raw = localStorage.getItem('userInfo')
    if (raw) savedUi = JSON.parse(raw)
  } catch {}
  const userInfo = ref<User | null>(savedUi)

  // ========== Getters ==========
  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)
  /** 是否为管理员 */
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  // ========== Actions ==========
  /** 登录 */
  const login = async (loginData: LoginDTO): Promise<void> => {
    const res = await loginApi(loginData)
    const { token: newToken, user } = res.data
    token.value = newToken
    userInfo.value = user
    // Token 持久化到 localStorage
    localStorage.setItem('token', newToken)
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  /** 退出登录 */
  const logout = (): void => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  /** 设置Token */
  const setToken = (newToken: string): void => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  /** 获取 Token */
  const getToken = (): string => {
    return token.value
  }

  /** 设置用户信息 */
  const setUserInfo = (user: User): void => {
    userInfo.value = user
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  return {
    // State
    token,
    userInfo,
    // Getters
    isLoggedIn,
    isAdmin,
    // Actions
    login,
    logout,
    setToken,
    getToken,
    setUserInfo
  }
})