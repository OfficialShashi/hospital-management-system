
import api from './api'

// Get all medical records
export const getAllMedicalRecords = async () => {
    const response = await api.get('/medical-records')
    return response.data
}

// Get medical record by ID
export const getMedicalRecordById = async (id) => {
    const response = await api.get(`/medical-records/${id}`)
    return response.data
}

// Get records by patient
export const getRecordsByPatient = async (patientId) => {
    const response = await api.get(`/medical-records/patient/${patientId}`)
    return response.data
}

// Get records by doctor
export const getRecordsByDoctor = async (doctorId) => {
    const response = await api.get(`/medical-records/doctor/${doctorId}`)
    return response.data
}

// Get records by appointment
export const getRecordsByAppointment = async (appointmentId) => {
    const response = await api.get(`/medical-records/appointment/${appointmentId}`)
    return response.data
}

// Create medical record
export const createMedicalRecord = async (recordData) => {
    const response = await api.post('/medical-records', recordData)
    return response.data
}
