
import { useEffect, useState } from 'react'
import './Patients.css'

import {
    getAllPatients,
    getPatientById,
    getMyDoctorPatients,
    getDoctorPatientById,
    registerPatient,
    updatePatient,
    deactivatePatient,
    reactivatePatient
} from '../services/patientService'

function Patients() {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const roles = user.roles || []
    const userRole = user.role || user.userRole

    const isDoctor = roles.includes('DOCTOR') || userRole === 'DOCTOR'
    const canManagePatients =
        roles.includes('ADMIN') ||
        roles.includes('RECEPTIONIST') ||
        userRole === 'ADMIN' ||
        userRole === 'RECEPTIONIST'

    const [patients, setPatients] = useState([])
    const [searchTerm, setSearchTerm] = useState('')
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')
    const [showForm, setShowForm] = useState(false)
    const [saving, setSaving] = useState(false)
    const [selectedPatient, setSelectedPatient] = useState(null)
    const [detailsLoading, setDetailsLoading] = useState(false)
    const [editingPatient, setEditingPatient] = useState(false)
    const [updating, setUpdating] = useState(false)

    const [editData, setEditData] = useState({
        dateOfBirth: '',
        gender: 'MALE',
        bloodGroup: '',
        address: '',
        emergencyContact: ''
    })

    const [formData, setFormData] = useState({
        fullName: '',
        email: '',
        phone: '',
        password: '',
        dateOfBirth: '',
        gender: 'MALE',
        bloodGroup: '',
        address: '',
        emergencyContact: ''
    })

    useEffect(() => {
        fetchPatients()
    }, [])

    const fetchPatients = async () => {
        try {
            setLoading(true)
            setError('')

            const data = isDoctor
                ? await getMyDoctorPatients()
                : await getAllPatients()

            setPatients(data)
        } catch (err) {
            setError(
                err.response?.data?.message || 'Unable to load patients'
            )
        } finally {
            setLoading(false)
        }
    }

    const filteredPatients = patients.filter(patient => {
        const search = searchTerm.toLowerCase().trim()

        return (
            patient.fullName?.toLowerCase().includes(search) ||
            patient.patientNumber?.toLowerCase().includes(search) ||
            patient.email?.toLowerCase().includes(search) ||
            patient.phone?.toLowerCase().includes(search)
        )
    })

    const handleChange = (e) => {
        const { name, value } = e.target

        setFormData({
            ...formData,
            [name]: value
        })
    }

    const handleSubmit = async (e) => {
        e.preventDefault()

        if (!canManagePatients) return

        try {
            setSaving(true)
            setError('')
            setSuccess('')

            await registerPatient(formData)

            setSuccess('Patient registered successfully!')

            setFormData({
                fullName: '',
                email: '',
                phone: '',
                password: '',
                dateOfBirth: '',
                gender: 'MALE',
                bloodGroup: '',
                address: '',
                emergencyContact: ''
            })

            setShowForm(false)
            await fetchPatients()

        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to register patient'
            )
        } finally {
            setSaving(false)
        }
    }

    const handleViewDetails = async (patientId) => {
        try {
            setDetailsLoading(true)
            setError('')
            setSelectedPatient(null)
            setEditingPatient(false)

            const data = isDoctor
                ? await getDoctorPatientById(patientId)
                : await getPatientById(patientId)

            setSelectedPatient(data)

        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to load patient details'
            )
        } finally {
            setDetailsLoading(false)
        }
    }

    const handleEditPatient = () => {
        if (!canManagePatients || !selectedPatient) return

        setEditData({
            dateOfBirth: selectedPatient.dateOfBirth || '',
            gender: selectedPatient.gender || 'MALE',
            bloodGroup: selectedPatient.bloodGroup || '',
            address: selectedPatient.address || '',
            emergencyContact: selectedPatient.emergencyContact || ''
        })

        setEditingPatient(true)
    }

    const handleEditChange = (e) => {
        const { name, value } = e.target

        setEditData({
            ...editData,
            [name]: value
        })
    }

    const handleUpdatePatient = async (e) => {
        e.preventDefault()

        if (!canManagePatients) return

        try {
            setUpdating(true)
            setError('')
            setSuccess('')

            await updatePatient(selectedPatient.patientId, editData)

            const updatedPatient = await getPatientById(
                selectedPatient.patientId
            )

            setSelectedPatient(updatedPatient)
            setEditingPatient(false)
            setSuccess('Patient details updated successfully!')

            await fetchPatients()

        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to update patient details'
            )
        } finally {
            setUpdating(false)
        }
    }

    const handleTogglePatientStatus = async () => {
        if (!canManagePatients || !selectedPatient) return

        const isActive = selectedPatient.accountStatus === 'ACTIVE'

        const confirmed = window.confirm(
            `Are you sure you want to ${isActive ? 'deactivate' : 'reactivate'} this patient?`
        )

        if (!confirmed) return

        try {
            setError('')
            setSuccess('')

            if (isActive) {
                await deactivatePatient(selectedPatient.patientId)
                setSuccess('Patient deactivated successfully!')
            } else {
                await reactivatePatient(selectedPatient.patientId)
                setSuccess('Patient reactivated successfully!')
            }

            const updatedPatient = await getPatientById(
                selectedPatient.patientId
            )

            setSelectedPatient(updatedPatient)
            await fetchPatients()

        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to update patient status'
            )
        }
    }

    return (
        <div className="patients-page">

            <div className="patients-header">
                <h2>Patient Management</h2>

                {canManagePatients && (
                    <button
                        type="button"
                        className={showForm ? 'cancel-patient-btn' : 'add-patient-btn'}
                        onClick={() => {
                            setShowForm(!showForm)
                            setError('')
                            setSuccess('')
                        }}
                    >
                        {showForm ? 'Cancel' : '+ Add New Patient'}
                    </button>
                )}
            </div>

            {isDoctor && (
                <p>Showing only patients linked to your appointments.</p>
            )}

            {success && <p className="patient-success">{success}</p>}
            {error && <p>{error}</p>}

            {showForm && canManagePatients && (
                <div className="patient-form">

                    <h3>Register New Patient</h3>

                    <form onSubmit={handleSubmit}>

                        <input type="text" name="fullName"
                            placeholder="Full Name"
                            value={formData.fullName}
                            onChange={handleChange} required />

                        <input type="email" name="email"
                            placeholder="Email"
                            value={formData.email}
                            onChange={handleChange} required />

                        <input type="tel" name="phone"
                            placeholder="Phone Number"
                            value={formData.phone}
                            onChange={handleChange} />

                        <input type="password" name="password"
                            placeholder="Password (minimum 8 characters)"
                            value={formData.password}
                            onChange={handleChange}
                            minLength="8" required />

                        <label>Date of Birth</label>
                        <input type="date" name="dateOfBirth"
                            value={formData.dateOfBirth}
                            onChange={handleChange} />

                        <select name="gender" value={formData.gender}
                            onChange={handleChange}>
                            <option value="MALE">Male</option>
                            <option value="FEMALE">Female</option>
                            <option value="OTHER">Other</option>
                        </select>

                        <input type="text" name="bloodGroup"
                            placeholder="Blood Group (e.g. O+)"
                            value={formData.bloodGroup}
                            onChange={handleChange} />

                        <textarea name="address" placeholder="Address"
                            value={formData.address}
                            onChange={handleChange} />

                        <input type="tel" name="emergencyContact"
                            placeholder="Emergency Contact"
                            value={formData.emergencyContact}
                            onChange={handleChange} />

                        <button type="submit" disabled={saving}>
                            {saving ? 'Registering...' : 'Register Patient'}
                        </button>

                    </form>
                </div>
            )}

            <p>Total Patients: {patients.length}</p>

            {!loading && filteredPatients.length > 0 && (
                <div className="patient-profile-grid">

                    {filteredPatients.map((patient, index) => (
                        <div className="patient-profile-card"
                            key={patient.patientId}>

                            <div className="patient-profile-image">
                                <img
                                    src={[
                                        'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400',
                                        'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400',
                                        'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400',
                                        'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=400'
                                    ][index % 4]}
                                    alt="Patient care"
                                />
                            </div>

                            <div className="patient-profile-info">

                                <h3>{patient.fullName}</h3>
                                <p>{patient.patientNumber}</p>

                                <div className="patient-profile-details">
                                    <span>Gender: {patient.gender || 'N/A'}</span>
                                    <span>Blood Group: {patient.bloodGroup || 'N/A'}</span>
                                </div>

                                <div className="patient-profile-footer">
                                    <span>{patient.phone || 'No phone'}</span>

                                    <span className={
                                        patient.accountStatus === 'ACTIVE'
                                            ? 'patient-active'
                                            : 'patient-inactive'
                                    }>
                                        {patient.accountStatus || 'UNKNOWN'}
                                    </span>
                                </div>

                                <button type="button"
                                    className="patient-profile-view"
                                    onClick={() => handleViewDetails(patient.patientId)}>
                                    View Details
                                </button>

                            </div>
                        </div>
                    ))}

                </div>
            )}

            <div className="patient-search-box">

                <input type="text"
                    placeholder="Search by name, patient number, email or phone..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)} />

                <button type="button" onClick={() => setSearchTerm('')}>
                    Clear
                </button>

            </div>

            <p>Showing Patients: {filteredPatients.length}</p>

            {loading ? (
                <p>Loading patients...</p>
            ) : filteredPatients.length === 0 ? (
                <p>No matching patients found.</p>
            ) : (

                <table>
                    <thead>
                        <tr>
                            <th>Patient ID</th>
                            <th>Patient Number</th>
                            <th>Name</th>
                            <th>Email</th>
                            <th>Phone</th>
                            <th>Date of Birth</th>
                            <th>Gender</th>
                            <th>Blood Group</th>
                            <th>Address</th>
                            <th>Emergency Contact</th>
                            <th>Action</th>
                        </tr>
                    </thead>

                    <tbody>
                        {filteredPatients.map(patient => (
                            <tr key={patient.patientId}>
                                <td>{patient.patientId}</td>
                                <td>{patient.patientNumber}</td>
                                <td>{patient.fullName}</td>
                                <td>{patient.email}</td>
                                <td>{patient.phone}</td>
                                <td>{patient.dateOfBirth}</td>
                                <td>{patient.gender}</td>
                                <td>{patient.bloodGroup}</td>
                                <td>{patient.address}</td>
                                <td>{patient.emergencyContact}</td>

                                <td>
                                    <button type="button"
                                        className="view-patient-btn"
                                        onClick={() => handleViewDetails(patient.patientId)}>
                                        View Details
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}

            {detailsLoading && <p>Loading patient details...</p>}

            {selectedPatient && (
                <div className="patient-modal-overlay"
                    onClick={() => {
                        setSelectedPatient(null)
                        setEditingPatient(false)
                    }}>

                    <div className="patient-details patient-modal"
                        onClick={(e) => e.stopPropagation()}>

                        <h3>Patient Details</h3>

                        <div className="patient-status-section">

                            <p>
                                Account Status: {' '}
                                <strong style={{
                                    color: selectedPatient.accountStatus === 'ACTIVE'
                                        ? '#16a34a' : '#dc2626',
                                    fontWeight: '700'
                                }}>
                                    {selectedPatient.accountStatus}
                                </strong>
                            </p>

                            {canManagePatients && (
                                <button type="button"
                                    className={
                                        selectedPatient.accountStatus === 'ACTIVE'
                                            ? 'deactivate-patient-btn'
                                            : 'reactivate-patient-btn'
                                    }
                                    onClick={handleTogglePatientStatus}>
                                    {selectedPatient.accountStatus === 'ACTIVE'
                                        ? 'Deactivate Patient'
                                        : 'Reactivate Patient'}
                                </button>
                            )}

                        </div>

                        {editingPatient && canManagePatients && (
                            <form onSubmit={handleUpdatePatient}
                                className="patient-edit-form">

                                <h4>Edit Patient Information</h4>

                                <label>Date of Birth</label>
                                <input type="date" name="dateOfBirth"
                                    value={editData.dateOfBirth}
                                    onChange={handleEditChange} />

                                <label>Gender</label>
                                <select name="gender" value={editData.gender}
                                    onChange={handleEditChange}>
                                    <option value="MALE">Male</option>
                                    <option value="FEMALE">Female</option>
                                    <option value="OTHER">Other</option>
                                </select>

                                <label>Blood Group</label>
                                <input type="text" name="bloodGroup"
                                    value={editData.bloodGroup}
                                    onChange={handleEditChange} />

                                <label>Address</label>
                                <textarea name="address"
                                    value={editData.address}
                                    onChange={handleEditChange} />

                                <label>Emergency Contact</label>
                                <input type="tel" name="emergencyContact"
                                    value={editData.emergencyContact}
                                    onChange={handleEditChange} />

                                <button type="submit" disabled={updating}>
                                    {updating ? 'Updating...' : 'Update Patient'}
                                </button>

                                <button type="button"
                                    onClick={() => setEditingPatient(false)}>
                                    Cancel Edit
                                </button>

                            </form>
                        )}

                        <div className="patient-information">

                            <p><strong>Patient ID:</strong> {selectedPatient.patientId}</p>
                            <p><strong>Patient Number:</strong> {selectedPatient.patientNumber}</p>
                            <p><strong>Name:</strong> {selectedPatient.fullName}</p>
                            <p><strong>Email:</strong> {selectedPatient.email}</p>
                            <p><strong>Phone:</strong> {selectedPatient.phone}</p>
                            <p><strong>Date of Birth:</strong> {selectedPatient.dateOfBirth}</p>
                            <p><strong>Gender:</strong> {selectedPatient.gender}</p>
                            <p><strong>Blood Group:</strong> {selectedPatient.bloodGroup}</p>
                            <p><strong>Address:</strong> {selectedPatient.address}</p>
                            <p><strong>Emergency Contact:</strong> {selectedPatient.emergencyContact}</p>

                        </div>

                        <div className="patient-modal-actions">

                            {canManagePatients && (
                                <button type="button" onClick={handleEditPatient}>
                                    Edit Patient
                                </button>
                            )}

                            <button type="button"
                                onClick={() => {
                                    setSelectedPatient(null)
                                    setEditingPatient(false)
                                }}>
                                Close Details
                            </button>

                        </div>

                    </div>
                </div>
            )}

        </div>
    )
}

export default Patients
