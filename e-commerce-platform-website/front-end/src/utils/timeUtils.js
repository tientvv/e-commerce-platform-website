/**
 * Utility functions for handling Vietnam timezone
 */

/**
 * Get current time in Vietnam timezone
 */
export function getCurrentVietnamTime() {
  return new Date(new Date().toLocaleString("en-US", {timeZone: "Asia/Ho_Chi_Minh"}))
}

/**
 * Convert date to Vietnam timezone
 */
export function toVietnamTime(date) {
  if (!date) return null
  return new Date(date.toLocaleString("en-US", {timeZone: "Asia/Ho_Chi_Minh"}))
}

/**
 * Format discount time remaining
 */
export function formatDiscountTimeRemaining(endDate) {
  if (!endDate) return ''

  // Lấy thời gian hiện tại (UTC)
  const now = new Date()

  // Lấy thời gian kết thúc (UTC)
  const end = new Date(endDate)

  // Tính khoảng cách thời gian (milliseconds)
  const diffInMs = end.getTime() - now.getTime()
  const diffInSeconds = Math.floor(diffInMs / 1000)

  if (diffInSeconds <= 0) {
    return 'Đã hết hạn'
  }

  // Tính giờ, phút, giây
  const hours = Math.floor(diffInSeconds / 3600)
  const minutes = Math.floor((diffInSeconds % 3600) / 60)
  const seconds = diffInSeconds % 60

  // Format: HH:MM:SS
  const formatNumber = (num) => num.toString().padStart(2, '0')

  return `${formatNumber(hours)}:${formatNumber(minutes)}:${formatNumber(seconds)}`
}

/**
 * Get time remaining in seconds for realtime updates
 */
export function getTimeRemainingInSeconds(endDate) {
  if (!endDate) return 0

  const now = new Date()
  const end = new Date(endDate)
  const diffInMs = end.getTime() - now.getTime()

  return Math.max(0, Math.floor(diffInMs / 1000))
}

/**
 * Format date in Vietnam format
 */
export function formatVietnamDate(date) {
  if (!date) return ''
  const vietnamDate = toVietnamTime(new Date(date))
  return vietnamDate.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

/**
 * Check if discount is active (current time is between start and end date)
 */
export function isDiscountActive(startDate, endDate) {
  if (!startDate || !endDate) return false

  const now = new Date()
  const start = new Date(startDate)
  const end = new Date(endDate)

  return now >= start && now <= end
}
