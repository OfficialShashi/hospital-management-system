
import api from './api'

// ==================== MEDICINE APIs ====================

export const getAllMedicines = async () => {
    const response = await api.get('/medicines')
    return response.data
}

export const getMedicineById = async (id) => {
    const response = await api.get(`/medicines/${id}`)
    return response.data
}

export const getMedicineByCode = async (code) => {
    const response = await api.get(`/medicines/code/${encodeURIComponent(code)}`)
    return response.data
}

export const searchMedicines = async (name) => {
    const response = await api.get('/medicines/search', {
        params: { name },
    })
    return response.data
}

export const getMedicinesByStatus = async (status) => {
    const response = await api.get(`/medicines/status/${status}`)
    return response.data
}

export const getMedicinesByCategory = async (category) => {
    const response = await api.get(
        `/medicines/category/${encodeURIComponent(category)}`
    )
    return response.data
}

export const createMedicine = async (medicineData) => {
    const response = await api.post('/medicines', medicineData)
    return response.data
}

export const updateMedicine = async (id, medicineData) => {
    const response = await api.put(`/medicines/${id}`, medicineData)
    return response.data
}

export const updateMedicineStatus = async (id, status) => {
    const response = await api.put(`/medicines/${id}/status/${status}`)
    return response.data
}
