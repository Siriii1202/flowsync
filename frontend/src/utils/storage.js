/**
 * sessionStorage 工具模块
 *
 * 什么是 sessionStorage？
 * 它是浏览器提供的一个"临时小仓库"，
 * 数据在关闭浏览器标签页后自动清空。
 * 我们用它来存储当前登录用户的信息和登录凭证。
 *
 * 为什么要用 sessionStorage 而不是 localStorage？
 * sessionStorage 在用户关闭页面后自动清除，
 * 更安全，适合存储登录状态这类临时数据。
 */

const USER_KEY = 'flowsync_user'
const TOKEN_KEY = 'flowsync_token'

// 保存用户信息到 sessionStorage
export function setUser(user) {
  // JSON.stringify 将 JavaScript 对象转换成字符串才能存储
  sessionStorage.setItem(USER_KEY, JSON.stringify(user))
}

// 从 sessionStorage 获取用户信息
export function getUser() {
  // JSON.parse 将字符串还原成 JavaScript 对象
  const str = sessionStorage.getItem(USER_KEY)
  return str ? JSON.parse(str) : null
}

// 清除用户信息（退出登录时调用）
export function removeUser() {
  sessionStorage.removeItem(USER_KEY)
}

// 获取登录凭证（Token）
export function getToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

// 保存登录凭证
export function setToken(token) {
  sessionStorage.setItem(TOKEN_KEY, token)
}
