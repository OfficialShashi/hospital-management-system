
import { useEffect, useState } from 'react'

import {
    getAllMedicalRecords,
    getMedicalRecordById,
    createMedicalRecord
} from '../services/medicalRecordService'

import { getAllPatients } from '../services/patientService'
import { getAllDoctors } from '../services/doctorService'

function MedicalRecords() {
    const [records, setRecords] = useState([])
    const [patients, setPatients] = useState([])
    const [doctors, setDoctors] = useState([])

    const [searchTerm, setSearchTerm] = useState('')
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)

    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const [showForm, setShowForm] = useState(false)
    const [selectedRecord, setSelectedRecord] = useState(null)
    const [detailsLoading, setDetailsLoading] = useState(false)

    const initialForm = {
        patientId: '',
        doctorId: '',
        appointmentId: '',
        diagnosis: '',
        symptoms: '',
        treatment: '',
        doctorNotes: ''
    }

    const [formData, setFormData] = useState(initialForm)

    useEffect(() => {
        loadInitialData()
    }, [])

    const loadInitialData = async () => {
        try {
            setLoading(true)
            setError('')

            const [recordData, patientData, doctorData] =
                await Promise.all([
                    getAllMedicalRecords(),
                    getAllPatients(),
                    getAllDoctors()
                ])

            setRecords(Array.isArray(recordData) ? recordData : [])
            setPatients(Array.isArray(patientData) ? patientData : [])
            setDoctors(Array.isArray(doctorData) ? doctorData : [])

        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to load medical records'
            )
        } finally {
            setLoading(false)
        }
    }

    const getPatientName = (patient) => {
        return (
            patient?.fullName ||
            patient?.user?.fullName ||
            'N/A'
        )
    }

    const getDoctorName = (doctor) => {
        return (
            doctor?.fullName ||
            doctor?.user?.fullName ||
            'N/A'
        )
    }

    const getPatientNumber = (patient) => {
        return patient?.patientNumber || 'N/A'
    }

    const getRecordDate = (date) => {
        if (!date) return 'N/A'

        return new Date(date).toLocaleString('en-IN', {
            dateStyle: 'medium',
            timeStyle: 'short'
        })
    }

    const filteredRecords = records.filter((record) => {
        const search = searchTerm.toLowerCase().trim()

        return (
            String(record.recordId || '').includes(search) ||
            getPatientName(record.patient).toLowerCase().includes(search) ||
            getDoctorName(record.doctor).toLowerCase().includes(search) ||
            record.diagnosis?.toLowerCase().includes(search)
        )
    })

    const handleChange = (e) => {
        const { name, value } = e.target

        setFormData((previous) => ({
            ...previous,
            [name]: value
        }))
    }

    const handleSubmit = async (e) => {
        e.preventDefault()

        try {
            setSaving(true)
            setError('')
            setSuccess('')

            const payload = {
                patientId: Number(formData.patientId),
                doctorId: Number(formData.doctorId),
                appointmentId: formData.appointmentId
                    ? Number(formData.appointmentId)
                    : null,
                diagnosis: formData.diagnosis.trim(),
                symptoms: formData.symptoms.trim(),
                treatment: formData.treatment.trim(),
                doctorNotes: formData.doctorNotes.trim()
            }

            await createMedicalRecord(payload)

            setSuccess('Medical record created successfully!')
            setFormData(initialForm)
            setShowForm(false)

            const updatedRecords = await getAllMedicalRecords()
            setRecords(updatedRecords)

        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                'Unable to create medical record'
            )
        } finally {
            setSaving(false)
        }
    }

    const handleViewDetails = async (recordId) => {
        try {
            setDetailsLoading(true)
            setError('')
            setSelectedRecord(null)

            const data = await getMedicalRecordById(recordId)

            setSelectedRecord(data)

        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to load medical record details'
            )
        } finally {
            setDetailsLoading(false)
        }
    }

    return (
        <div className="medical-records-page">

            <div className="medical-records-header">
                <div>
                    <h2>Medical Records</h2>
                    <p>
                        Manage patient diagnoses, treatments and medical history.
                    </p>
                </div>

                <button
                    type="button"
                    className="medical-add-button"
                    onClick={() => {
                        setShowForm(!showForm)
                        setError('')
                        setSuccess('')
                        setSelectedRecord(null)
                    }}
                >
                    {showForm ? 'Cancel' : '+ Add Medical Record'}
                </button>
            </div>

            {error && (
                <p className="medical-error">{error}</p>
            )}

            {success && (
                <p className="medical-success">{success}</p>
            )}

            <div className="medical-stats">
                <div className="medical-stat-card">
                    <span>Total Medical Records</span>
                    <h3>{records.length}</h3>
                </div>

                <div className="medical-stat-card">
                    <span>Registered Patients</span>
                    <h3>{patients.length}</h3>
                </div>

                <div className="medical-stat-card">
                    <span>Registered Doctors</span>
                    <h3>{doctors.length}</h3>
                </div>
            </div>

            {showForm && (
                <div className="medical-form-card">

                    <h3>Create Medical Record</h3>

                    <form onSubmit={handleSubmit}>

                        <div className="medical-form-grid">

                            <div>
                                <label>Patient *</label>

                                <select
                                    name="patientId"
                                    value={formData.patientId}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="">
                                        Select Patient
                                    </option>

                                    {patients
                                        .filter(patient =>
                                            !patient.accountStatus ||
                                            patient.accountStatus === 'ACTIVE'
                                        )
                                        .map(patient => (
                                            <option
                                                key={patient.patientId}
                                                value={patient.patientId}
                                            >
                                                {getPatientName(patient)} - {getPatientNumber(patient)}
                                            </option>
                                        ))}
                                </select>
                            </div>

                            <div>
                                <label>Doctor *</label>

                                <select
                                    name="doctorId"
                                    value={formData.doctorId}
                                    onChange={handleChange}
                                    required
                                >
                                    <option value="">
                                        Select Doctor
                                    </option>

                                    {doctors
                                        .filter(doctor =>
                                            !doctor.accountStatus ||
                                            doctor.accountStatus === 'ACTIVE'
                                        )
                                        .map(doctor => (
                                            <option
                                                key={doctor.doctorId}
                                                value={doctor.doctorId}
                                            >
                                                {getDoctorName(doctor)}
                                            </option>
                                        ))}
                                </select>
                            </div>

                            <div>
                                <label>Appointment ID (Optional)</label>

                                <input
                                    type="number"
                                    name="appointmentId"
                                    placeholder="Enter Appointment ID"
                                    value={formData.appointmentId}
                                    onChange={handleChange}
                                    min="1"
                                />
                            </div>

                            <div>
                                <label>Diagnosis *</label>

                                <input
                                    type="text"
                                    name="diagnosis"
                                    placeholder="Enter diagnosis"
                                    value={formData.diagnosis}
                                    onChange={handleChange}
                                    maxLength="500"
                                    required
                                />
                            </div>

                            <div className="medical-full-width">
                                <label>Symptoms</label>

                                <textarea
                                    name="symptoms"
                                    placeholder="Describe patient symptoms..."
                                    value={formData.symptoms}
                                    onChange={handleChange}
                                    rows="3"
                                />
                            </div>

                            <div className="medical-full-width">
                                <label>Treatment</label>

                                <textarea
                                    name="treatment"
                                    placeholder="Describe treatment and medications..."
                                    value={formData.treatment}
                                    onChange={handleChange}
                                    rows="3"
                                />
                            </div>

                            <div className="medical-full-width">
                                <label>Doctor Notes</label>

                                <textarea
                                    name="doctorNotes"
                                    placeholder="Additional notes from doctor..."
                                    value={formData.doctorNotes}
                                    onChange={handleChange}
                                    rows="3"
                                />
                            </div>

                        </div>

                        <div className="medical-form-actions">

                            <button
                                type="submit"
                                className="medical-add-button"
                                disabled={saving}
                            >
                                {saving
                                    ? 'Saving...'
                                    : 'Save Medical Record'}
                            </button>

                            <button
                                type="button"
                                className="medical-cancel-button"
                                onClick={() => {
                                    setShowForm(false)
                                    setFormData(initialForm)
                                }}
                            >
                                Cancel
                            </button>

                        </div>

                    </form>
                </div>
            )}

            <div className="medical-search-container">

                <input
                    type="text"
                    placeholder="Search by record ID, patient, doctor or diagnosis..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />

                <button
                    type="button"
                    onClick={() => setSearchTerm('')}
                >
                    Clear
                </button>

                <span>
                    Total Records: {filteredRecords.length}
                </span>

            </div>

            {detailsLoading && (
                <p>Loading medical record details...</p>
            )}

            {selectedRecord && (
                <div className="medical-details-card">

                    <div className="medical-details-header">
                        <h3>Medical Record Details</h3>

                        <button
                            type="button"
                            onClick={() => setSelectedRecord(null)}
                        >
                            Close
                        </button>
                    </div>

                    <div className="medical-details-grid">

                        <p>
                            <strong>Record ID:</strong>{' '}
                            {selectedRecord.recordId}
                        </p>

                        <p>
                            <strong>Patient:</strong>{' '}
                            {getPatientName(selectedRecord.patient)}
                        </p>

                        <p>
                            <strong>Patient Number:</strong>{' '}
                            {getPatientNumber(selectedRecord.patient)}
                        </p>

                        <p>
                            <strong>Doctor:</strong>{' '}
                            {getDoctorName(selectedRecord.doctor)}
                        </p>

                        <p>
                            <strong>Appointment ID:</strong>{' '}
                            {selectedRecord.appointment?.appointmentId || 'N/A'}
                        </p>

                        <p>
                            <strong>Record Date:</strong>{' '}
                            {getRecordDate(selectedRecord.recordDate)}
                        </p>

                        <p>
                            <strong>Diagnosis:</strong>{' '}
                            {selectedRecord.diagnosis}
                        </p>

                        <p>
                            <strong>Symptoms:</strong>{' '}
                            {selectedRecord.symptoms || 'N/A'}
                        </p>

                        <p>
                            <strong>Treatment:</strong>{' '}
                            {selectedRecord.treatment || 'N/A'}
                        </p>

                        <p>
                            <strong>Doctor Notes:</strong>{' '}
                            {selectedRecord.doctorNotes || 'N/A'}
                        </p>

                    </div>
                </div>
            )}

            {loading ? (
                <p>Loading medical records...</p>
            ) : (
                <div className="medical-table-container">

                    <table className="medical-records-table">

                        <thead>
                            <tr>
                                <th>Record ID</th>
                                <th>Patient Name</th>
                                <th>Patient Number</th>
                                <th>Doctor</th>
                                <th>Diagnosis</th>
                                <th>Record Date</th>
                                <th>Action</th>
                            </tr>
                        </thead>

                        <tbody>

                            {filteredRecords.length > 0 ? (
                                filteredRecords.map(record => (
                                    <tr key={record.recordId}>

                                        <td>{record.recordId}</td>

                                        <td>
                                            {getPatientName(record.patient)}
                                        </td>

                                        <td>
                                            {getPatientNumber(record.patient)}
                                        </td>

                                        <td>
                                            {getDoctorName(record.doctor)}
                                        </td>

                                        <td>{record.diagnosis}</td>

                                        <td>
                                            {getRecordDate(record.recordDate)}
                                        </td>

                                        <td>
                                            <button
                                                type="button"
                                                className="medical-view-button"
                                                onClick={() =>
                                                    handleViewDetails(record.recordId)
                                                }
                                            >
                                                View Details
                                            </button>
                                        </td>

                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="7">
                                        No medical records found.
                                    </td>
                                </tr>
                            )}

                        </tbody>

                    </table>

                </div>
            )}

        </div>
    )
}

export default MedicalRecords
