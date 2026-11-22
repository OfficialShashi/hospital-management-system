
import { useEffect, useState } from 'react'
import prescriptionService from '../services/prescriptionService'
import { getAllPatients } from '../services/patientService'
import { getAllDoctors } from '../services/doctorService'
import {
    getAllAppointments,
    getAppointmentsByDoctor
} from '../services/appointmentService'

function Prescriptions() {
    const [prescriptions, setPrescriptions] = useState([])
    const [patients, setPatients] = useState([])
    const [doctors, setDoctors] = useState([])
    const [appointments, setAppointments] = useState([])

    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const [search, setSearch] = useState('')
    const [selectedPrescription, setSelectedPrescription] = useState(null)
    const [prescriptionItems, setPrescriptionItems] = useState([])
    const [showForm, setShowForm] = useState(false)

    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const roles = user.roles || []
    const isDoctor = roles.includes('DOCTOR')
    const isAdmin = roles.includes('ADMIN')

    const [form, setForm] = useState({
        patientId: '',
        doctorId: '',
        appointmentId: '',
        diagnosis: '',
        instructions: '',
        items: [
            {
                medicineName: '',
                dosage: '',
                route: 'Oral',
                frequency: '',
                durationDays: '',
                quantity: '',
                instructions: ''
            }
        ]
    })

    const loadPrescriptions = async () => {
        try {
            setLoading(true)
            setError('')

            const data = await prescriptionService.getAllPrescriptions()
            setPrescriptions(Array.isArray(data) ? data : [])
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to load prescriptions.'
            )
        } finally {
            setLoading(false)
        }
    }

    const loadFormData = async () => {
        try {
            const doctorData = await getAllDoctors()
            const doctorList = Array.isArray(doctorData) ? doctorData : []
            setDoctors(doctorList)

            let patientData = []
            try {
                patientData = await getAllPatients()
                setPatients(Array.isArray(patientData) ? patientData : [])
            } catch (err) {
                console.error('Unable to load patients:', err)
            }

            let loggedInDoctor = null

            if (isDoctor) {
                loggedInDoctor = doctorList.find(
                    doctor => Number(doctor.userId) === Number(user.userId)
                )

                if (!loggedInDoctor) {
                    throw new Error(
                        'Logged-in Doctor profile could not be found.'
                    )
                }

                setForm(prev => ({
                    ...prev,
                    doctorId: String(loggedInDoctor.doctorId)
                }))

                const appointmentData = await getAppointmentsByDoctor(
                    loggedInDoctor.doctorId
                )

                setAppointments(
                    (Array.isArray(appointmentData) ? appointmentData : [])
                        .filter(item => item.appointmentStatus === 'COMPLETED')
                )
            } else if (isAdmin) {
                const appointmentData = await getAllAppointments()

                setAppointments(
                    (Array.isArray(appointmentData) ? appointmentData : [])
                        .filter(item => item.appointmentStatus === 'COMPLETED')
                )
            }
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.message ||
                'Unable to load prescription form data.'
            )
        }
    }

    useEffect(() => {
        loadPrescriptions()
    }, [])

    useEffect(() => {
        if (showForm) {
            loadFormData()
        }
    }, [showForm])

    const filteredPrescriptions = prescriptions.filter(item => {
        const query = search.toLowerCase()

        return (
            item.prescriptionNumber?.toLowerCase().includes(query) ||
            item.patientName?.toLowerCase().includes(query) ||
            item.doctorName?.toLowerCase().includes(query) ||
            item.diagnosis?.toLowerCase().includes(query) ||
            item.status?.toLowerCase().includes(query)
        )
    })

    const handleFormChange = (e) => {
        const { name, value } = e.target

        setForm(prev => ({
            ...prev,
            [name]: value
        }))
    }

    const handleItemChange = (index, field, value) => {
        setForm(prev => ({
            ...prev,
            items: prev.items.map((item, i) =>
                i === index ? { ...item, [field]: value } : item
            )
        }))
    }

    const addMedicine = () => {
        setForm(prev => ({
            ...prev,
            items: [
                ...prev.items,
                {
                    medicineName: '',
                    dosage: '',
                    route: 'Oral',
                    frequency: '',
                    durationDays: '',
                    quantity: '',
                    instructions: ''
                }
            ]
        }))
    }

    const removeMedicine = (index) => {
        setForm(prev => ({
            ...prev,
            items: prev.items.filter((_, i) => i !== index)
        }))
    }

    const handlePatientChange = (e) => {
        const patientId = e.target.value

        setForm(prev => ({
            ...prev,
            patientId,
            appointmentId: ''
        }))
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        setSuccess('')

        if (!form.items.length) {
            setError('At least one medicine is required.')
            return
        }

        const selectedAppointment = appointments.find(
            item => String(item.appointmentId) === String(form.appointmentId)
        )

        if (form.appointmentId && !selectedAppointment) {
            setError('Please select a valid completed appointment.')
            return
        }

        if (
            selectedAppointment &&
            Number(selectedAppointment.patientId) !== Number(form.patientId)
        ) {
            setError('Selected appointment does not belong to this patient.')
            return
        }

        if (
            selectedAppointment &&
            Number(selectedAppointment.doctorId) !== Number(form.doctorId)
        ) {
            setError('Selected appointment does not belong to this doctor.')
            return
        }

        const payload = {
            patientId: Number(form.patientId),
            doctorId: Number(form.doctorId),
            appointmentId: form.appointmentId
                ? Number(form.appointmentId)
                : null,
            diagnosis: form.diagnosis.trim(),
            instructions: form.instructions.trim(),
            items: form.items.map(item => ({
                medicineName: item.medicineName.trim(),
                dosage: item.dosage.trim(),
                route: item.route,
                frequency: item.frequency.trim(),
                durationDays: Number(item.durationDays),
                quantity: Number(item.quantity),
                instructions: item.instructions.trim()
            }))
        }

        try {
            setSaving(true)

            await prescriptionService.createPrescription(payload)

            setSuccess('Prescription created successfully.')

            setForm({
                patientId: '',
                doctorId: isDoctor ? form.doctorId : '',
                appointmentId: '',
                diagnosis: '',
                instructions: '',
                items: [
                    {
                        medicineName: '',
                        dosage: '',
                        route: 'Oral',
                        frequency: '',
                        durationDays: '',
                        quantity: '',
                        instructions: ''
                    }
                ]
            })

            setShowForm(false)
            await loadPrescriptions()
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to create prescription.'
            )
        } finally {
            setSaving(false)
        }
    }

    const handleView = async (prescription) => {
        try {
            setError('')
            const items = await prescriptionService.getPrescriptionItems(
                prescription.prescriptionId
            )

            setSelectedPrescription(prescription)
            setPrescriptionItems(Array.isArray(items) ? items : [])
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to load prescription details.'
            )
        }
    }

    const handleStatusChange = async (id, status) => {
        try {
            setError('')
            setSuccess('')

            await prescriptionService.updatePrescriptionStatus(id, status)

            setSuccess('Prescription status updated successfully.')
            await loadPrescriptions()

            if (selectedPrescription?.prescriptionId === id) {
                const updated = await prescriptionService.getPrescriptionById(id)
                setSelectedPrescription(updated)
            }
        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to update prescription status.'
            )
        }
    }

    const closeDetails = () => {
        setSelectedPrescription(null)
        setPrescriptionItems([])
    }

    return (
        <section className="dashboard-content prescription-module">
            <div className="prescription-header">
                <div>
                    <h2>Prescription Management</h2>
                    <p className="section-description">
                        Create, search and manage patient prescriptions.
                    </p>
                </div>

                {isDoctor && (
                    <button
                        className="prescription-primary-btn"
                        onClick={() => {
                            setError('')
                            setSuccess('')
                            setShowForm(true)
                        }}
                    >
                        + Create Prescription
                    </button>
                )}
            </div>

            {error && <div className="prescription-alert error">{error}</div>}
            {success && <div className="prescription-alert success">{success}</div>}

            <div className="prescription-summary">
                <div className="prescription-stat-card">
                    <span>Total Prescriptions</span>
                    <strong>{prescriptions.length}</strong>
                </div>
                <div className="prescription-stat-card">
                    <span>Active</span>
                    <strong>
                        {prescriptions.filter(p => p.status === 'ACTIVE').length}
                    </strong>
                </div>
                <div className="prescription-stat-card">
                    <span>Dispensed</span>
                    <strong>
                        {prescriptions.filter(p => p.status === 'DISPENSED').length}
                    </strong>
                </div>
            </div>

            <div className="prescription-toolbar">
                <input
                    type="text"
                    placeholder="Search prescription, patient, doctor or diagnosis..."
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                />
                <button onClick={loadPrescriptions}>Refresh</button>
            </div>

            {loading ? (
                <p>Loading prescriptions...</p>
            ) : (
                <div className="prescription-table-wrapper">
                    <table className="prescription-table">
                        <thead>
                            <tr>
                                <th>Prescription No.</th>
                                <th>Patient</th>
                                <th>Doctor</th>
                                <th>Diagnosis</th>
                                <th>Date</th>
                                <th>Status</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredPrescriptions.length === 0 ? (
                                <tr>
                                    <td colSpan="7">No prescriptions found.</td>
                                </tr>
                            ) : (
                                filteredPrescriptions.map(item => (
                                    <tr key={item.prescriptionId}>
                                        <td>{item.prescriptionNumber}</td>
                                        <td>{item.patientName}</td>
                                        <td>{item.doctorName}</td>
                                        <td>{item.diagnosis || '-'}</td>
                                        <td>
                                            {item.prescriptionDate
                                                ? new Date(item.prescriptionDate).toLocaleString()
                                                : '-'}
                                        </td>
                                        <td>
                                            <span className={`prescription-status ${item.status?.toLowerCase()}`}>
                                                {item.status}
                                            </span>
                                        </td>
                                        <td>
                                            <button
                                                className="prescription-view-btn"
                                                onClick={() => handleView(item)}
                                            >
                                                View
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            )}

            {showForm && (
                <div className="prescription-modal-overlay">
                    <div className="prescription-modal">
                        <div className="prescription-modal-header">
                            <h2>Create Prescription</h2>
                            <button onClick={() => setShowForm(false)}>×</button>
                        </div>

                        <form onSubmit={handleSubmit}>
                            <div className="prescription-form-grid">
                                <div>
                                    <label>Patient *</label>
                                    <select
                                        name="patientId"
                                        value={form.patientId}
                                        onChange={handlePatientChange}
                                        required
                                    >
                                        <option value="">Select Patient</option>
                                        {patients
                                            .filter(p => p.accountStatus === 'ACTIVE')
                                            .map(patient => (
                                                <option
                                                    key={patient.patientId}
                                                    value={patient.patientId}
                                                >
                                                    {patient.fullName} ({patient.patientNumber})
                                                </option>
                                            ))}
                                    </select>
                                </div>

                                <div>
                                    <label>Doctor *</label>
                                    <select
                                        name="doctorId"
                                        value={form.doctorId}
                                        onChange={handleFormChange}
                                        required
                                        disabled={isDoctor}
                                    >
                                        <option value="">Select Doctor</option>
                                        {doctors.map(doctor => (
                                            <option
                                                key={doctor.doctorId}
                                                value={doctor.doctorId}
                                            >
                                                {doctor.fullName} - {doctor.specialization}
                                            </option>
                                        ))}
                                    </select>
                                </div>

                                <div>
                                    <label>Completed Appointment (Optional)</label>
                                    <select
                                        name="appointmentId"
                                        value={form.appointmentId}
                                        onChange={handleFormChange}
                                    >
                                        <option value="">No Appointment Selected</option>
                                        {appointments
                                            .filter(item =>
                                                !form.patientId ||
                                                Number(item.patientId) === Number(form.patientId)
                                            )
                                            .filter(item =>
                                                !form.doctorId ||
                                                Number(item.doctorId) === Number(form.doctorId)
                                            )
                                            .map(item => (
                                                <option
                                                    key={item.appointmentId}
                                                    value={item.appointmentId}
                                                >
                                                    {item.appointmentNumber} - {item.patientName} - {new Date(item.appointmentDateTime).toLocaleString()}
                                                </option>
                                            ))}
                                    </select>
                                </div>

                                <div>
                                    <label>Diagnosis</label>
                                    <input
                                        name="diagnosis"
                                        value={form.diagnosis}
                                        onChange={handleFormChange}
                                        maxLength="500"
                                        placeholder="Enter diagnosis"
                                    />
                                </div>

                                <div className="prescription-full-width">
                                    <label>General Instructions</label>
                                    <textarea
                                        name="instructions"
                                        value={form.instructions}
                                        onChange={handleFormChange}
                                        placeholder="Enter general instructions"
                                    />
                                </div>
                            </div>

                            <div className="prescription-items-heading">
                                <h3>Medicines</h3>
                                <button type="button" onClick={addMedicine}>
                                    + Add Medicine
                                </button>
                            </div>

                            {form.items.map((item, index) => (
                                <div className="prescription-medicine-card" key={index}>
                                    <div className="prescription-medicine-heading">
                                        <h4>Medicine {index + 1}</h4>
                                        {form.items.length > 1 && (
                                            <button
                                                type="button"
                                                onClick={() => removeMedicine(index)}
                                            >
                                                Remove
                                            </button>
                                        )}
                                    </div>

                                    <div className="prescription-form-grid">
                                        <div>
                                            <label>Medicine Name *</label>
                                            <input
                                                value={item.medicineName}
                                                onChange={e => handleItemChange(index, 'medicineName', e.target.value)}
                                                maxLength="200"
                                                required
                                            />
                                        </div>
                                        <div>
                                            <label>Dosage *</label>
                                            <input
                                                value={item.dosage}
                                                onChange={e => handleItemChange(index, 'dosage', e.target.value)}
                                                placeholder="500 mg"
                                                maxLength="100"
                                                required
                                            />
                                        </div>
                                        <div>
                                            <label>Route</label>
                                            <select
                                                value={item.route}
                                                onChange={e => handleItemChange(index, 'route', e.target.value)}
                                            >
                                                <option value="Oral">Oral</option>
                                                <option value="Topical">Topical</option>
                                                <option value="Injection">Injection</option>
                                                <option value="Inhalation">Inhalation</option>
                                                <option value="Other">Other</option>
                                            </select>
                                        </div>
                                        <div>
                                            <label>Frequency *</label>
                                            <input
                                                value={item.frequency}
                                                onChange={e => handleItemChange(index, 'frequency', e.target.value)}
                                                placeholder="Twice a day"
                                                maxLength="100"
                                                required
                                            />
                                        </div>
                                        <div>
                                            <label>Duration (Days) *</label>
                                            <input
                                                type="number"
                                                min="1"
                                                value={item.durationDays}
                                                onChange={e => handleItemChange(index, 'durationDays', e.target.value)}
                                                required
                                            />
                                        </div>
                                        <div>
                                            <label>Quantity *</label>
                                            <input
                                                type="number"
                                                min="1"
                                                value={item.quantity}
                                                onChange={e => handleItemChange(index, 'quantity', e.target.value)}
                                                required
                                            />
                                        </div>
                                        <div className="prescription-full-width">
                                            <label>Medicine Instructions</label>
                                            <textarea
                                                value={item.instructions}
                                                onChange={e => handleItemChange(index, 'instructions', e.target.value)}
                                                placeholder="Take after meals"
                                            />
                                        </div>
                                    </div>
                                </div>
                            ))}

                            <div className="prescription-form-actions">
                                <button
                                    type="button"
                                    className="prescription-cancel-btn"
                                    onClick={() => setShowForm(false)}
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    className="prescription-primary-btn"
                                    disabled={saving}
                                >
                                    {saving ? 'Creating...' : 'Create Prescription'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {selectedPrescription && (
                <div className="prescription-modal-overlay">
                    <div className="prescription-modal prescription-details-modal">
                        <div className="prescription-modal-header">
                            <h2>Prescription Details</h2>
                            <button onClick={closeDetails}>×</button>
                        </div>

                        <div className="prescription-detail-grid">
                            <p><strong>Prescription No:</strong> {selectedPrescription.prescriptionNumber}</p>
                            <p><strong>Patient:</strong> {selectedPrescription.patientName}</p>
                            <p><strong>Doctor:</strong> {selectedPrescription.doctorName}</p>
                            <p><strong>Diagnosis:</strong> {selectedPrescription.diagnosis || '-'}</p>
                            <p><strong>Date:</strong> {selectedPrescription.prescriptionDate ? new Date(selectedPrescription.prescriptionDate).toLocaleString() : '-'}</p>
                            <p><strong>Status:</strong> {selectedPrescription.status}</p>
                            <p className="prescription-full-width">
                                <strong>Instructions:</strong> {selectedPrescription.instructions || '-'}
                            </p>
                        </div>

                        <h3>Prescribed Medicines</h3>

                        <div className="prescription-table-wrapper">
                            <table className="prescription-table">
                                <thead>
                                    <tr>
                                        <th>Medicine</th>
                                        <th>Dosage</th>
                                        <th>Route</th>
                                        <th>Frequency</th>
                                        <th>Duration</th>
                                        <th>Quantity</th>
                                        <th>Instructions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {prescriptionItems.map(item => (
                                        <tr key={item.itemId}>
                                            <td>{item.medicineName}</td>
                                            <td>{item.dosage}</td>
                                            <td>{item.route || '-'}</td>
                                            <td>{item.frequency}</td>
                                            <td>{item.durationDays} days</td>
                                            <td>{item.quantity}</td>
                                            <td>{item.instructions || '-'}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>

                        <div className="prescription-form-actions">
                            {(isAdmin || roles.includes('PHARMACIST')) &&
                                !['DISPENSED', 'CANCELLED'].includes(selectedPrescription.status) && (
                                    <select
                                        defaultValue=""
                                        onChange={e => {
                                            if (e.target.value) {
                                                handleStatusChange(
                                                    selectedPrescription.prescriptionId,
                                                    e.target.value
                                                )
                                                e.target.value = ''
                                            }
                                        }}
                                    >
                                        <option value="">Update Status</option>
                                        {selectedPrescription.status === 'ACTIVE' && (
                                            <>
                                                <option value="PARTIALLY_DISPENSED">Partially Dispensed</option>
                                                <option value="DISPENSED">Dispensed</option>
                                                <option value="CANCELLED">Cancelled</option>
                                            </>
                                        )}
                                        {selectedPrescription.status === 'PARTIALLY_DISPENSED' && (
                                            <option value="DISPENSED">Dispensed</option>
                                        )}
                                    </select>
                                )}

                            <button
                                className="prescription-cancel-btn"
                                onClick={closeDetails}
                            >
                                Close
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </section>
    )
}

export default Prescriptions
