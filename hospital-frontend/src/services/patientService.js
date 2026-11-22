import api from './api'

export const getAllPatients = async () => {
  const response = await api.get('/patients')
  return response.data
}

export const getPatientById = async (id) => {
  const response = await api.get(`/patients/${id}`)
  return response.data
}
export const registerPatient = async (patientData) => {
  const response = await api.post('/patients/register', patientData)
  return response.data
}
export const updatePatient = async (id, patientData) => {
  const response = await api.put(`/patients/${id}`, patientData)
  return response.data
}

export const deactivatePatient = async (id) => {
  const response = await api.put(`/patients/${id}/deactivate`)
  return response.data
}

export const reactivatePatient = async (id) => {
  const response = await api.put(`/patients/${id}/reactivate`)
  return response.data
}

// Get patients linked to logged-in doctor
export const getMyDoctorPatients = async () => {
  const response = await api.get('/patients/doctor/my-patients')
  return response.data
}

// Get individual patient linked to logged-in doctor
export const getDoctorPatientById = async (id) => {
  const response = await api.get(`/patients/doctor/my-patients/${id}`)
  return response.data
}