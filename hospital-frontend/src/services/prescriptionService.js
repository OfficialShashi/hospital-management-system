
import api from "./api";

const prescriptionService = {
    getAllPrescriptions: async () => {
        const response = await api.get("/prescriptions");
        return response.data;
    },

    getPrescriptionById: async (id) => {
        const response = await api.get(`/prescriptions/${id}`);
        return response.data;
    },

    getPrescriptionByNumber: async (number) => {
        const response = await api.get(
            `/prescriptions/number/${encodeURIComponent(number)}`
        );
        return response.data;
    },

    getPrescriptionsByPatient: async (patientId) => {
        const response = await api.get(
            `/prescriptions/patient/${patientId}`
        );
        return response.data;
    },

    getPrescriptionsByDoctor: async (doctorId) => {
        const response = await api.get(
            `/prescriptions/doctor/${doctorId}`
        );
        return response.data;
    },

    getPrescriptionItems: async (prescriptionId) => {
        const response = await api.get(
            `/prescriptions/${prescriptionId}/items`
        );
        return response.data;
    },

    createPrescription: async (prescriptionData) => {
        const response = await api.post(
            "/prescriptions",
            prescriptionData
        );
        return response.data;
    },

    updatePrescriptionStatus: async (id, status) => {
        const response = await api.put(
            `/prescriptions/${id}/status/${status}`
        );
        return response.data;
    }
};

export default prescriptionService;
