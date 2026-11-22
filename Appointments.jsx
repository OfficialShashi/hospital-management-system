
import { useEffect, useState } from 'react'
import {
    getAllAppointments,
    getAppointmentsByDoctor,
    bookAppointment,
    rescheduleAppointment,
    cancelAppointment,
    updateAppointmentStatus
} from '../services/appointmentService'
import {
    getAllDoctors,
    getMyDoctorProfile
} from '../services/doctorService'
import { getAllDepartments } from '../services/departmentService'
import { getAllPatients } from '../services/patientService'

function Appointments() {
    const [appointments, setAppointments] = useState([])
    const [searchTerm, setSearchTerm] = useState('')
    const [doctors, setDoctors] = useState([])
    const [departments, setDepartments] = useState([])
    const [patients, setPatients] = useState([])

    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const [showForm, setShowForm] = useState(false)

    const [formData, setFormData] = useState({
        patientId: '',
        departmentName: '',
        doctorId: '',
        appointmentDateTime: '',
        reason: ''
    })

    const [selectedAppointment, setSelectedAppointment] = useState(null)
    const [newDateTime, setNewDateTime] = useState('')
    const [cancellationReason, setCancellationReason] = useState('')
    const [manageMode, setManageMode] = useState('')

    // Logged-in user details
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const roles = user.roles || []
    const role = (user.role || user.userRole || '').replace('ROLE_', '').toUpperCase()

    const isDoctor = roles.includes('DOCTOR') || role === 'DOCTOR'

    const isAdminOrReceptionist =
        roles.includes('ADMIN') ||
        roles.includes('RECEPTIONIST') ||
        role === 'ADMIN' ||
        role === 'RECEPTIONIST'

    useEffect(() => {
        fetchInitialData()
    }, [])

    const fetchInitialData = async () => {
    try {
        setLoading(true)
        setError('')

        let appointmentData = []

        if (isDoctor) {
            // Fetch logged-in doctor's own profile
            const doctorProfile = await getMyDoctorProfile()

            // Get actual doctorId (not userId)
            const doctorId = doctorProfile.doctorId

            // Fetch only this doctor's appointments
            appointmentData = await getAppointmentsByDoctor(doctorId)

        } else {
            appointmentData = await getAllAppointments()
        }

        setAppointments(appointmentData)

        // Only Admin and Receptionist need booking form data
        if (isAdminOrReceptionist) {
            const [
                doctorData,
                departmentData,
                patientData
            ] = await Promise.all([
                getAllDoctors(),
                getAllDepartments(),
                getAllPatients()
            ])

            setDoctors(doctorData)
            setDepartments(departmentData)
            setPatients(patientData)
        }

    } catch (err) {
        setError(
            err.response?.data?.message ||
            err.message ||
            'Unable to load appointment data'
        )
    } finally {
        setLoading(false)
    }
}

    const handleInputChange = (e) => {
        const { name, value } = e.target

        setFormData(prev => ({
            ...prev,
            [name]: value,
            ...(name === 'departmentName' ? { doctorId: '' } : {})
        }))
    }

    const filteredAppointments = appointments.filter(appointment => {
        const search = searchTerm.toLowerCase().trim()

        return (
            appointment.appointmentNumber?.toLowerCase().includes(search) ||
            appointment.patientName?.toLowerCase().includes(search) ||
            appointment.doctorName?.toLowerCase().includes(search) ||
            appointment.departmentName?.toLowerCase().includes(search) ||
            appointment.appointmentStatus?.toLowerCase().includes(search)
        )
    })

    const availableDoctors = doctors.filter(
        doctor =>
            doctor.departmentName === formData.departmentName &&
            doctor.availabilityStatus === 'AVAILABLE'
    )

    const handleBookAppointment = async (e) => {
        e.preventDefault()

        try {
            setError('')
            setSuccess('')

            const bookingData = {
                patientId: Number(formData.patientId),
                doctorId: Number(formData.doctorId),
                appointmentDateTime: formData.appointmentDateTime,
                reason: formData.reason
            }

            await bookAppointment(bookingData)

            setSuccess('Appointment booked successfully!')

            setFormData({
                patientId: '',
                departmentName: '',
                doctorId: '',
                appointmentDateTime: '',
                reason: ''
            })

            setShowForm(false)
            await fetchInitialData()

        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to book appointment'
            )
        }
    }

    const handleReschedule = async () => {
        if (!selectedAppointment || !newDateTime) {
            setError('Please select a new appointment date and time')
            return
        }

        try {
            setError('')
            setSuccess('')

            await rescheduleAppointment(
                selectedAppointment.appointmentId,
                { appointmentDateTime: newDateTime }
            )

            setSuccess('Appointment rescheduled successfully!')
            setSelectedAppointment(null)
            setManageMode('')
            setNewDateTime('')

            await fetchInitialData()

        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to reschedule appointment'
            )
        }
    }

    const handleCancelAppointment = async () => {
        if (!selectedAppointment || !cancellationReason.trim()) {
            setError('Please enter cancellation reason')
            return
        }

        const confirmed = window.confirm(
            'Are you sure you want to cancel this appointment?'
        )

        if (!confirmed) return

        try {
            setError('')
            setSuccess('')

            await cancelAppointment(
                selectedAppointment.appointmentId,
                { cancellationReason }
            )

            setSuccess('Appointment cancelled successfully!')
            setSelectedAppointment(null)
            setManageMode('')
            setCancellationReason('')

            await fetchInitialData()

        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to cancel appointment'
            )
        }
    }

    const handleStatusUpdate = async (appointmentId, status) => {
        try {
            setError('')
            setSuccess('')

            await updateAppointmentStatus(appointmentId, status)

            setSuccess(`Appointment status updated to ${status}`)
            await fetchInitialData()

        } catch (err) {
            setError(
                err.response?.data?.message ||
                'Unable to update appointment status'
            )
        }
    }

    if (loading) {
        return <p>Loading appointments...</p>
    }

    return (
        <div className="appointments-container">

            <div className="appointments-header">
                <div>
                    <h2>Appointment Management</h2>
                    <p>
                        {isDoctor
                            ? 'View and manage your assigned appointments'
                            : 'Manage hospital appointments'}
                    </p>
                </div>

                {isAdminOrReceptionist && (
                    <button
                        className="add-appointment-btn"
                        onClick={() => {
                            setShowForm(!showForm)
                            setError('')
                            setSuccess('')
                        }}
                    >
                        {showForm ? 'Cancel' : '+ Book Appointment'}
                    </button>
                )}
            </div>

            {error && <p className="appointment-error">{error}</p>}
            {success && <p className="appointment-success">{success}</p>}

            {showForm && isAdminOrReceptionist && (
                <form
                    className="appointment-form"
                    onSubmit={handleBookAppointment}
                >
                    <h3>Book New Appointment</h3>

                    <label>Patient</label>
                    <select
                        name="patientId"
                        value={formData.patientId}
                        onChange={handleInputChange}
                        required
                    >
                        <option value="">Select Patient</option>

                        {patients.map(patient => (
                            <option
                                key={patient.patientId}
                                value={patient.patientId}
                            >
                                {patient.fullName}
                            </option>
                        ))}
                    </select>

                    <label>Department</label>
                    <select
                        name="departmentName"
                        value={formData.departmentName}
                        onChange={handleInputChange}
                        required
                    >
                        <option value="">Select Department</option>

                        {departments.map(department => (
                            <option
                                key={department.departmentId}
                                value={department.departmentName}
                            >
                                {department.departmentName}
                            </option>
                        ))}
                    </select>

                    <label>Doctor</label>
                    <select
                        name="doctorId"
                        value={formData.doctorId}
                        onChange={handleInputChange}
                        required
                        disabled={!formData.departmentName}
                    >
                        <option value="">Select Doctor</option>

                        {availableDoctors.map(doctor => (
                            <option
                                key={doctor.doctorId}
                                value={doctor.doctorId}
                            >
                                {doctor.fullName} - {doctor.specialization}
                            </option>
                        ))}
                    </select>

                    <label>Appointment Date & Time</label>
                    <input
                        type="datetime-local"
                        name="appointmentDateTime"
                        value={formData.appointmentDateTime}
                        onChange={handleInputChange}
                        min={new Date(
                            Date.now() - new Date().getTimezoneOffset() * 60000
                        ).toISOString().slice(0, 16)}
                        required
                    />

                    <label>Reason</label>
                    <textarea
                        name="reason"
                        value={formData.reason}
                        onChange={handleInputChange}
                        maxLength={500}
                        placeholder="Enter appointment reason"
                    />

                    <button type="submit" className="submit-appointment-btn">
                        Book Appointment
                    </button>
                </form>
            )}

            <div className="appointment-search-box">
                <input
                    type="text"
                    placeholder="Search by appointment no, patient, doctor, department or status..."
                    value={searchTerm}
                    onChange={e => setSearchTerm(e.target.value)}
                />

                <button
                    type="button"
                    onClick={() => setSearchTerm('')}
                >
                    Clear
                </button>
            </div>

            <p className="appointment-count">
                Showing Appointments: {filteredAppointments.length} / {appointments.length}
            </p>

            <div className="appointments-table-wrapper">
                <table className="appointments-table">
                    <thead>
                        <tr>
                            <th>Appointment No.</th>
                            <th>Patient</th>
                            <th>Doctor</th>
                            <th>Department</th>
                            <th>Date & Time</th>
                            <th>Status</th>
                            <th>Reason</th>
                            <th>Actions</th>
                        </tr>
                    </thead>

                    <tbody>
                        {filteredAppointments.length === 0 ? (
                            <tr>
                                <td colSpan="8">
                                    No matching appointments found
                                </td>
                            </tr>
                        ) : (
                            filteredAppointments.map(appointment => (
                                <tr key={appointment.appointmentId}>
                                    <td>{appointment.appointmentNumber}</td>
                                    <td>{appointment.patientName}</td>
                                    <td>{appointment.doctorName}</td>
                                    <td>{appointment.departmentName}</td>
                                    <td>
                                        {new Date(
                                            appointment.appointmentDateTime
                                        ).toLocaleString()}
                                    </td>
                                    <td>
                                        <span className={`appointment-status ${appointment.appointmentStatus?.toLowerCase()}`}>
                                            {appointment.appointmentStatus}
                                        </span>
                                    </td>
                                    <td>{appointment.reason || '-'}</td>
                                    <td>
                                        <div className="appointment-actions">

                                            <button
                                                type="button"
                                                onClick={() => {
                                                    setSelectedAppointment(appointment)
                                                    setNewDateTime('')
                                                    setCancellationReason('')
                                                    setError('')
                                                    setSuccess('')
                                                }}
                                            >
                                                Manage
                                            </button>

                                            {isDoctor &&
                                                ['BOOKED', 'CONFIRMED'].includes(
                                                    appointment.appointmentStatus
                                                ) && (
                                                    <>
                                                        {appointment.appointmentStatus === 'BOOKED' && (
                                                            <button
                                                                type="button"
                                                                onClick={() =>
                                                                    handleStatusUpdate(
                                                                        appointment.appointmentId,
                                                                        'CONFIRMED'
                                                                    )
                                                                }
                                                            >
                                                                Confirm
                                                            </button>
                                                        )}

                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                handleStatusUpdate(
                                                                    appointment.appointmentId,
                                                                    'COMPLETED'
                                                                )
                                                            }
                                                        >
                                                            Complete
                                                        </button>

                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                handleStatusUpdate(
                                                                    appointment.appointmentId,
                                                                    'NO_SHOW'
                                                                )
                                                            }
                                                        >
                                                            No Show
                                                        </button>
                                                    </>
                                                )}

                                            {isAdminOrReceptionist && (
                                                <>
                                                    {appointment.appointmentStatus === 'BOOKED' && (
                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                handleStatusUpdate(
                                                                    appointment.appointmentId,
                                                                    'CONFIRMED'
                                                                )
                                                            }
                                                        >
                                                            Confirm
                                                        </button>
                                                    )}

                                                    {['BOOKED', 'CONFIRMED', 'CANCELLED'].includes(
                                                        appointment.appointmentStatus
                                                    ) && (
                                                            <button
                                                                type="button"
                                                                onClick={() => {
                                                                    setSelectedAppointment(appointment)
                                                                    setNewDateTime('')
                                                                    setCancellationReason('')
                                                                    setError('')
                                                                    setSuccess('')
                                                                    setManageMode('RESCHEDULE')
                                                                }}
                                                            >
                                                                Reschedule
                                                            </button>
                                                        )}
                                                </>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            </div>

            {selectedAppointment && (
                <div className="appointment-manage-panel">
                    <h3>Manage Appointment</h3>

                    <p>
                        Appointment No.:{' '}
                        <strong>{selectedAppointment.appointmentNumber}</strong>
                    </p>

                    <p>
                        Current Status:{' '}
                        <strong>{selectedAppointment.appointmentStatus}</strong>
                    </p>

                    {!isDoctor && (
                        <div className="appointment-manage-options">
                            <button
                                type="button"
                                onClick={() => {
                                    setManageMode('RESCHEDULE')
                                    setNewDateTime('')
                                    setError('')
                                    setSuccess('')
                                }}
                                disabled={['COMPLETED', 'NO_SHOW'].includes(
                                    selectedAppointment.appointmentStatus
                                )}
                            >
                                Reschedule Appointment
                            </button>

                            <button
                                type="button"
                                onClick={() => {
                                    setManageMode('CANCEL')
                                    setCancellationReason('')
                                    setError('')
                                    setSuccess('')
                                }}
                                disabled={['COMPLETED', 'CANCELLED', 'NO_SHOW'].includes(
                                    selectedAppointment.appointmentStatus
                                )}
                            >
                                Cancel Appointment
                            </button>
                        </div>
                    )}

                    {manageMode === 'RESCHEDULE' && !isDoctor && (
                        <>
                            <label>New Appointment Date & Time</label>

                            <input
                                type="datetime-local"
                                value={newDateTime}
                                onChange={e => setNewDateTime(e.target.value)}
                                min={new Date(
                                    Date.now() - new Date().getTimezoneOffset() * 60000
                                ).toISOString().slice(0, 16)}
                            />

                            <button
                                type="button"
                                onClick={handleReschedule}
                            >
                                Reschedule
                            </button>
                        </>
                    )}

                    {manageMode === 'CANCEL' && !isDoctor && (
                        <>
                            <label>Cancellation Reason</label>

                            <textarea
                                value={cancellationReason}
                                onChange={e => setCancellationReason(e.target.value)}
                                maxLength={500}
                                placeholder="Enter cancellation reason"
                            />

                            <button
                                type="button"
                                className="cancel-appointment-btn"
                                onClick={handleCancelAppointment}
                            >
                                Cancel Appointment
                            </button>
                        </>
                    )}

                    <button
                        type="button"
                        className="close-manage-btn"
                        onClick={() => {
                            setManageMode('')
                            setSelectedAppointment(null)
                            setNewDateTime('')
                            setCancellationReason('')
                            setError('')
                            setSuccess('')
                        }}
                    >
                        Close
                    </button>
                </div>
            )}

        </div>
    )
}

export default Appointments
