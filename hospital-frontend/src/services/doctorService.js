
import api from './api'

export const getAllDoctors = async () => {
  const response = await api.get('/doctors')
  return response.data
}

export const getDoctorById = async (id) => {
  const response = await api.get(`/doctors/${id}`)
  return response.data
}

export const updateDoctorAvailability = async (id, status) => {
  const response = await api.put(`/doctors/${id}/availability/${status}`)
  return response.data
}
export const createDoctor = async (doctorData) => {
  const response = await api.post('/doctors', doctorData)
  return response.data
}

export const getMyDoctorProfile = async () => {
  const response = await api.get('/doctors/me')
  return response.data
}