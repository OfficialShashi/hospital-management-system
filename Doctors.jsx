import { useEffect, useState } from 'react'
import './Doctors.css'
import {
  getAllDoctors,
  getDoctorById,
  updateDoctorAvailability,
  createDoctor
} from '../services/doctorService'

import { getAllDepartments } from '../services/departmentService'

function Doctors() {
  const [doctors, setDoctors] = useState([])
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedDoctor, setSelectedDoctor] = useState(null)
  const [detailLoading, setDetailLoading] = useState(false)
  const [detailError, setDetailError] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [updatingDoctorId, setUpdatingDoctorId] = useState(null)
  const [updateMessage, setUpdateMessage] = useState('')

  const [showAddDoctorForm, setShowAddDoctorForm] = useState(false)
  const [departments, setDepartments] = useState([])
  const [newDoctor, setNewDoctor] = useState({
    fullName: '',
    email: '',
    phone: '',
    password: '',
    licenseNumber: '',
    specialization: '',
    qualification: '',
    experienceYears: '',
    consultationFee: '',
    departmentId: ''
  })

  useEffect(() => {
    const fetchDoctors = async () => {
      try {
        setLoading(true)
        setError('')

        const data = await getAllDoctors()
        setDoctors(data)
      } catch (err) {
        setError(
          err.response?.data?.message || 'Unable to load doctors'
        )
      } finally {
        setLoading(false)
      }
    }

    fetchDoctors()
  }, [])

  useEffect(() => {
    const fetchDepartments = async () => {
      try {
        const data = await getAllDepartments()
        setDepartments(data)
      } catch (err) {
        console.error('Unable to load departments:', err)
      }
    }

    fetchDepartments()
  }, [])

  const handleViewDetails = async (doctorId) => {
    try {
      setDetailLoading(true)
      setDetailError('')
      setSelectedDoctor(null)

      const data = await getDoctorById(doctorId)
      setSelectedDoctor(data)
    } catch (err) {
      setDetailError(
        err.response?.data?.message || 'Unable to load doctor details'
      )
    } finally {
      setDetailLoading(false)
    }
  }

  const handleDoctorInputChange = (e) => {
    const { name, value } = e.target

    setNewDoctor((previous) => ({
      ...previous,
      [name]: value
    }))
  }
  const handleAddDoctor = async (e) => {
    e.preventDefault()

    try {
      setError('')
      setUpdateMessage('')

      const doctorData = {
        ...newDoctor,
        experienceYears: Number(newDoctor.experienceYears),
        consultationFee: Number(newDoctor.consultationFee),
        departmentId: Number(newDoctor.departmentId)
      }

      await createDoctor(doctorData)

      setUpdateMessage('New doctor added successfully.')
      setShowAddDoctorForm(false)

      setNewDoctor({
        fullName: '',
        email: '',
        phone: '',
        password: '',
        licenseNumber: '',
        specialization: '',
        qualification: '',
        experienceYears: '',
        consultationFee: '',
        departmentId: ''
      })

      const data = await getAllDoctors()
      setDoctors(data)

    } catch (err) {
      setError(err.response?.data?.message || 'Unable to add doctor')
    }
  }

  const handleUpdateAvailability = async (doctor) => {
    const newStatus =
      doctor.availabilityStatus === 'AVAILABLE'
        ? 'UNAVAILABLE'
        : 'AVAILABLE'

    try {
      setUpdatingDoctorId(doctor.doctorId)
      setUpdateMessage('')
      setError('')

      await updateDoctorAvailability(doctor.doctorId, newStatus)

      setDoctors((previousDoctors) =>
        previousDoctors.map((item) =>
          item.doctorId === doctor.doctorId
            ? { ...item, availabilityStatus: newStatus }
            : item
        )
      )

      setSelectedDoctor((previousDoctor) =>
        previousDoctor?.doctorId === doctor.doctorId
          ? { ...previousDoctor, availabilityStatus: newStatus }
          : previousDoctor
      )

      setUpdateMessage(
        `${doctor.fullName} availability updated to ${newStatus}.`
      )
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Unable to update doctor availability'
      )
    } finally {
      setUpdatingDoctorId(null)
    }
  }

  const filteredDoctors = doctors.filter((doctor) => {
    const search = searchTerm.toLowerCase()

    return (
      doctor.fullName?.toLowerCase().includes(search) ||
      doctor.email?.toLowerCase().includes(search) ||
      doctor.specialization?.toLowerCase().includes(search)
    )
  })

  return (
    <section className="dashboard-content">
      <div className="doctor-page-header">
        <h2>Doctors Management</h2>

        <button
          className="add-doctor-button"
          onClick={() => setShowAddDoctorForm(true)}
        >
          + Add New Doctor
        </button>
      </div>

      <p className="section-description">
        View and manage registered doctors in the hospital.
      </p>

      {!loading && !error && (
        <div className="doctor-search-container">
          <input
            type="text"
            className="doctor-search-input"
            placeholder="Search by doctor name, email or specialization..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />

          <span className="doctor-count">
            Total Doctors: {filteredDoctors.length}
          </span>
        </div>
      )}
      {showAddDoctorForm && (
        <div className="doctor-form-card">
          <div className="doctor-details-header">
            <h3>Add New Doctor</h3>
            <button onClick={() => setShowAddDoctorForm(false)}>
              Close
            </button>
          </div>

          <form onSubmit={handleAddDoctor}>
            <div className="doctor-form-grid">
              <input
                type="text"
                name="fullName"
                placeholder="Doctor Full Name"
                value={newDoctor.fullName}
                onChange={handleDoctorInputChange}
                required
              />

              <input
                type="email"
                name="email"
                placeholder="Email Address"
                value={newDoctor.email}
                onChange={handleDoctorInputChange}
                required
              />

              <input
                type="text"
                name="phone"
                placeholder="Phone Number"
                value={newDoctor.phone}
                onChange={handleDoctorInputChange}
                required
              />

              <input
                type="password"
                name="password"
                placeholder="Password"
                value={newDoctor.password}
                onChange={handleDoctorInputChange}
                required
              />
              <input
                type="text"
                name="licenseNumber"
                placeholder="License Number"
                value={newDoctor.licenseNumber}
                onChange={handleDoctorInputChange}
                required
              />

              <input
                type="text"
                name="specialization"
                placeholder="Specialization"
                value={newDoctor.specialization}
                onChange={handleDoctorInputChange}
                required
              />

              <input
                type="text"
                name="qualification"
                placeholder="Qualification"
                value={newDoctor.qualification}
                onChange={handleDoctorInputChange}
                required
              />

              <input
                type="number"
                name="experienceYears"
                placeholder="Experience (Years)"
                value={newDoctor.experienceYears}
                onChange={handleDoctorInputChange}
                min="0"
                required
              />
              <input
                type="number"
                name="consultationFee"
                placeholder="Consultation Fee (₹)"
                value={newDoctor.consultationFee}
                onChange={handleDoctorInputChange}
                min="0"
                required
              />
              <select
                name="departmentId"
                value={newDoctor.departmentId}
                onChange={handleDoctorInputChange}
                required
              >
                <option value="">Select Department</option>

                {departments
                  .filter((department) => department.status === 'ACTIVE')
                  .map((department) => (
                    <option
                      key={department.departmentId}
                      value={department.departmentId}
                    >
                      {department.departmentName}
                    </option>
                  ))}
              </select>
            </div>
            <div className="doctor-form-actions">
              <button type="submit" className="add-doctor-button">
                Add Doctor
              </button>

              <button
                type="button"
                onClick={() => setShowAddDoctorForm(false)}>
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}
      {loading && <p>Loading doctors...</p>}
      {error && <p className="login-error">{error}</p>}

      {updateMessage && (
        <p className="doctor-update-success">{updateMessage}</p>
      )}

      {detailLoading && <p>Loading doctor details...</p>}
      {detailError && <p className="login-error">{detailError}</p>}

      {selectedDoctor && (
        <div className="doctor-details-card">
          <div className="doctor-details-header">
            <h3>Doctor Details</h3>
            <button onClick={() => setSelectedDoctor(null)}>
              Close
            </button>
          </div>

          <div className="doctor-details-grid">
            <p><strong>Doctor ID:</strong> {selectedDoctor.doctorId}</p>
            <p><strong>User ID:</strong> {selectedDoctor.userId}</p>
            <p><strong>Name:</strong> {selectedDoctor.fullName}</p>
            <p><strong>Email:</strong> {selectedDoctor.email}</p>
            <p><strong>License Number:</strong> {selectedDoctor.licenseNumber}</p>
            <p><strong>Specialization:</strong> {selectedDoctor.specialization}</p>
            <p><strong>Qualification:</strong> {selectedDoctor.qualification}</p>
            <p><strong>Experience:</strong> {selectedDoctor.experienceYears} Years</p>
            <p><strong>Consultation Fee:</strong> ₹{selectedDoctor.consultationFee}</p>
            <p><strong>Department:</strong> {selectedDoctor.departmentName}</p>
            <p><strong>Availability:</strong> {selectedDoctor.availabilityStatus}</p>
          </div>
        </div>
      )}
      {!loading && !error && (
        <div className="doctor-profile-grid">
          {filteredDoctors.map((doctor, index) => (
            <div className="doctor-profile-card" key={doctor.doctorId}>
              <div className="doctor-profile-image">
                <img
                  src={
                    [
                      'https://images.unsplash.com/photo-1612349317150-e413f6a5b16d?w=400',
                      'https://images.unsplash.com/photo-1559839734-2b71ea197ec2?w=400',
                      'https://images.unsplash.com/photo-1622253692010-333f2da6031d?w=400',
                      'https://images.unsplash.com/photo-1594824476967-48c8b964273f?w=400'
                    ][index % 4]
                  }
                  alt={doctor.fullName}
                />
              </div>

              <div className="doctor-profile-info">
                <h3>{doctor.fullName}</h3>
                <p>{doctor.specialization}</p>
                <span>{doctor.departmentName}</span>

                <div className="doctor-profile-footer">
                  <span>
                    {doctor.experienceYears} Years Experience
                  </span>

                  <span className={
                    doctor.availabilityStatus === 'AVAILABLE'
                      ? 'availability-available'
                      : 'availability-unavailable'
                  }>
                    {doctor.availabilityStatus}
                  </span>
                </div>

                <button
                  className="doctor-profile-view"
                  onClick={() => handleViewDetails(doctor.doctorId)}
                >
                  View Details
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {!loading && !error && (
        <div className="doctor-table-container">
          <table className="doctor-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Doctor Name</th>
                <th>Email</th>
                <th>Specialization</th>
                <th>Department</th>
                <th>Experience</th>
                <th>Consultation Fee</th>
                <th>Availability</th>
                <th>Action</th>
              </tr>
            </thead>

            <tbody>
              {filteredDoctors.length > 0 ? (
                filteredDoctors.map((doctor) => (
                  <tr key={doctor.doctorId}>
                    <td>{doctor.doctorId}</td>

                    <td>
                      <button
                        className="doctor-name-button"
                        onClick={() => handleViewDetails(doctor.doctorId)}
                      >
                        {doctor.fullName}
                      </button>
                    </td>

                    <td>{doctor.email}</td>
                    <td>{doctor.specialization}</td>
                    <td>{doctor.departmentName}</td>
                    <td>{doctor.experienceYears} Years</td>
                    <td>₹{doctor.consultationFee}</td>

                    <td>
                      <span
                        className={`availability-badge ${doctor.availabilityStatus === 'AVAILABLE'
                            ? 'availability-available'
                            : 'availability-unavailable'
                          }`}
                      >
                        {doctor.availabilityStatus}
                      </span>
                    </td>

                    <td>
                      <button
                        className="doctor-availability-button"
                        disabled={updatingDoctorId === doctor.doctorId}
                        onClick={() => handleUpdateAvailability(doctor)}
                      >
                        {updatingDoctorId === doctor.doctorId
                          ? 'Updating...'
                          : doctor.availabilityStatus === 'AVAILABLE'
                            ? 'Mark Unavailable'
                            : 'Mark Available'}
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="9">No doctors found.</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}

export default Doctors
