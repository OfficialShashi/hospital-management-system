
import api from './api'

// Invoice APIs
export const getAllInvoices = async () => {
  const response = await api.get('/invoices')
  return response.data
}

export const getInvoiceById = async (id) => {
  const response = await api.get(`/invoices/${id}`)
  return response.data
}

export const getInvoiceByNumber = async (invoiceNumber) => {
  const response = await api.get(`/invoices/number/${invoiceNumber}`)
  return response.data
}

export const getInvoicesByPatient = async (patientId) => {
  const response = await api.get(`/invoices/patient/${patientId}`)
  return response.data
}

export const createInvoice = async (invoiceData) => {
  const response = await api.post('/invoices', invoiceData)
  return response.data
}

export const updateInvoiceStatus = async (id, status) => {
  const response = await api.put(`/invoices/${id}/status/${status}`)
  return response.data
}

// Payment APIs
export const getAllPayments = async () => {
  const response = await api.get('/payments')
  return response.data
}

export const getPaymentById = async (id) => {
  const response = await api.get(`/payments/${id}`)
  return response.data
}

export const getPaymentsByInvoice = async (invoiceId) => {
  const response = await api.get(`/payments/invoice/${invoiceId}`)
  return response.data
}

export const createPayment = async (paymentData) => {
  const response = await api.post('/payments', paymentData)
  return response.data
}

export const confirmPayment = async (id) => {
  const response = await api.put(`/payments/${id}/confirm`)
  return response.data
}

export const failPayment = async (id) => {
  const response = await api.put(`/payments/${id}/fail`)
  return response.data
}

export const refundPayment = async (id) => {
  const response = await api.put(`/payments/${id}/refund`)
  return response.data
}
