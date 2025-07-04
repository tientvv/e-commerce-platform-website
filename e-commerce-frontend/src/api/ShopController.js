import axios from 'axios'

export const getAllRegistrations = async () => {
  try {
    const response = await axios.get('/api/shops/registrations')
    return response.data
  } catch (error) {
    console.error('Could not get the list of registrations:', error)
    throw error
  }
}

export const processRegistration = async (userId, status, reason = null) => {
  try {
    const response = await axios.put('/api/shops/sales-registration', {
      userId,
      status,
      reason,
    })
    return response.data
  } catch (error) {
    console.error('Lỗi khi xử lý đơn đăng ký:', error)
    throw error
  }
}

export const deleteRegistration = async (userId) => {
  try {
    const response = await axios.delete(`/api/shops/registrations/${userId}`)
    return response.data
  } catch (error) {
    console.error('Could not delete the registration:', error)
    throw error
  }
}
