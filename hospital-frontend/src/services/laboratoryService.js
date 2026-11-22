
import api from './api'

// ==================== LAB TEST APIs ====================

export const getAllLabTests = async () => {
  const response = await api.get('/lab-tests')
  return response.data
}

export const getLabTestById = async (testId) => {
  const response = await api.get(`/lab-tests/${testId}`)
  return response.data
}

export const getLabTestByCode = async (testCode) => {
  const response = await api.get(`/lab-tests/code/${encodeURIComponent(testCode)}`)
  return response.data
}

export const searchLabTests = async (name) => {
  const response = await api.get('/lab-tests/search', {
    params: { name },
  })
  return response.data
}

export const getLabTestsByStatus = async (status) => {
  const response = await api.get(`/lab-tests/status/${status}`)
  return response.data
}

export const getLabTestsByCategory = async (category) => {
  const response = await api.get(`/lab-tests/category/${encodeURIComponent(category)}`)
  return response.data
}

export const createLabTest = async (testData) => {
  const response = await api.post('/lab-tests', testData)
  return response.data
}

export const updateLabTest = async (testId, testData) => {
  const response = await api.put(`/lab-tests/${testId}`, testData)
  return response.data
}

export const updateLabTestStatus = async (testId, status) => {
  const response = await api.put(`/lab-tests/${testId}/status/${status}`)
  return response.data
}

// ==================== LAB ORDER APIs ====================

export const getAllLabOrders = async () => {
  const response = await api.get('/lab-orders')
  return response.data
}

export const getLabOrderById = async (orderId) => {
  const response = await api.get(`/lab-orders/${orderId}`)
  return response.data
}

export const getLabOrderByNumber = async (orderNumber) => {
  const response = await api.get(`/lab-orders/number/${encodeURIComponent(orderNumber)}`)
  return response.data
}

export const getLabOrdersByPatient = async (patientId) => {
  const response = await api.get(`/lab-orders/patient/${patientId}`)
  return response.data
}

export const getLabOrdersByDoctor = async (doctorId) => {
  const response = await api.get(`/lab-orders/doctor/${doctorId}`)
  return response.data
}

export const getLabOrdersByStatus = async (status) => {
  const response = await api.get(`/lab-orders/status/${status}`)
  return response.data
}

export const createLabOrder = async (orderData) => {
  const response = await api.post('/lab-orders', orderData)
  return response.data
}

export const collectLabSample = async (itemId) => {
  const response = await api.put(`/lab-orders/items/${itemId}/collect-sample`)
  return response.data
}

export const startLabProcessing = async (itemId) => {
  const response = await api.put(`/lab-orders/items/${itemId}/start-processing`)
  return response.data
}

export const completeLabTest = async (itemId, resultData) => {
  const response = await api.put(`/lab-orders/items/${itemId}/complete`, resultData)
  return response.data
}

export const cancelLabOrder = async (orderId) => {
  const response = await api.put(`/lab-orders/${orderId}/cancel`)
  return response.data
}

// ==================== DOCTOR LAB ORDER APIs ====================

// Get Lab Orders for Logged-in Doctor
export const getMyLabOrders = async () => {
    const response = await api.get('/lab-orders/doctor/my-orders')
    return response.data
}

// Create Lab Order by Doctor
export const createLabOrderByDoctor = async (orderData) => {
    const response = await api.post('/lab-orders/doctor', orderData)
    return response.data
}