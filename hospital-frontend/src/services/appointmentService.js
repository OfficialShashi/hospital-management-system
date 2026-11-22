import api from './api'

export const getAllAppointments = async () => {
    const response = await api.get('/appointments')
    return response.data
}

export const getAppointmentById = async (id) => {
    const response = await api.get(`/appointments/${id}`)
    return response.data
}

export const getAppointmentsByPatient = async (patientId) => {
    const response = await api.get(`/appointments/patient/${patientId}`)
    return response.data
}

export const getAppointmentsByDoctor = async (doctorId) => {
    const response = await api.get(`/appointments/doctor/${doctorId}`)
    return response.data
}

export const bookAppointment = async (appointmentData) => {
    const response = await api.post('/appointments/book', appointmentData)
    return response.data
}

export const rescheduleAppointment = async (id, appointmentData) => {
    const response = await api.put(
        `/appointments/${id}/reschedule`,
        appointmentData
    )
    return response.data
}

export const cancelAppointment = async (id, cancellationData) => {
    const response = await api.put(
        `/appointments/${id}/cancel`,
        cancellationData
    )
    return response.data
}

export const updateAppointmentStatus = async (id, status) => {
    const response = await api.put(
        `/appointments/${id}/status/${status}`
    )
    return response.data
}