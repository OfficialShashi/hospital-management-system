
import { useState, useEffect } from 'react'
import Login from './components/Login'
import Doctors from './components/Doctors'
import Patients from './components/Patients'
import Appointments from './components/Appointments'
import Laboratory from './components/Laboratory'
import Pharmacy from './components/Pharmacy'
import MedicalRecords from './components/MedicalRecords'
import Prescriptions from './components/Prescriptions'
import Billing from './components/Billing'
import Reports from './components/Reports'
import { getDashboardData } from './services/dashboardService'
import './App.css'


function App() {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user')
    const token = localStorage.getItem('token')

    return token && savedUser ? JSON.parse(savedUser) : null
  })

  const [activePage, setActivePage] = useState('Dashboard')
  const [dashboard, setDashboard] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!user) return

    const fetchDashboard = async () => {
      try {
        setLoading(true)
        setError('')

        const data = await getDashboardData()
        setDashboard(data)
      } catch (err) {
        setError(
          err.response?.data?.message || 'Unable to load dashboard data'
        )
      } finally {
        setLoading(false)
      }
    }

    fetchDashboard()
  }, [user])

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    setUser(null)
    setDashboard(null)
    setActivePage('Dashboard')
  }

  if (!user) {
    return <Login onLogin={setUser} />
  }

  const statistics = [
    { title: 'Total Patients', value: dashboard?.totalPatients },
    { title: 'Total Doctors', value: dashboard?.totalDoctors },
    { title: 'Total Appointments', value: dashboard?.totalAppointments },
    { title: "Today's Appointments", value: dashboard?.todayAppointments },
    { title: 'Total Prescriptions', value: dashboard?.totalPrescriptions },
    { title: 'Laboratory Tests', value: dashboard?.totalLabTests },
    { title: 'Total Medicines', value: dashboard?.totalMedicines },
    { title: 'Total Invoices', value: dashboard?.totalInvoices },
    { title: 'Total Payments', value: dashboard?.totalPayments }
  ]

  const menuItems = [
    { name: 'Dashboard', icon: '📊' },
    { name: 'Doctors', icon: '👨‍⚕️' },
    { name: 'Patients', icon: '🧑' },
    { name: 'Appointments', icon: '📅' },
    { name: 'Medical Records', icon: '📋' },
    { name: 'Prescriptions', icon: '💊' },
    { name: 'Laboratory', icon: '🧪' },
    { name: 'Pharmacy', icon: '💉' },
    { name: 'Billing', icon: '💰' },
    { name: 'Reports', icon: '📈' }
  ]

  return (
    <div className="app-container">
      <aside className="sidebar">
        <h2>🏥 HMS</h2>
        <p className="sidebar-subtitle">Hospital Management</p>

        <nav>
          {menuItems.map((item) => (
            <a
              key={item.name}
              href="#"
              className={activePage === item.name ? 'active' : ''}
              onClick={(e) => {
                e.preventDefault()
                setActivePage(item.name)
              }}
            >
              {item.icon} {item.name}
            </a>
          ))}
        </nav>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <h2>{activePage}</h2>
            <p>Welcome to Hospital Management System</p>
          </div>

          <div className="admin-profile">
            <div className="admin-avatar">
              {user.fullName?.charAt(0).toUpperCase()}
            </div>

            <span>{user.fullName}</span>
            <button onClick={handleLogout}>Logout</button>
          </div>
        </header>

        {activePage === 'Dashboard' && (
          <section className="dashboard-content">
            <div className="dashboard-hero">
              <div className="dashboard-hero-content">
                <span className="hero-tag">WELCOME TO HMS</span>
                <h1>Smart Healthcare Management</h1>
                <p>
                  Manage doctors, patients, appointments and hospital operations
                  from one place.
                </p>
              </div>              
            </div>
            <h2>Overview</h2>

            <p className="section-description">
              Here's what's happening in your hospital today.
            </p>

            {loading && <p>Loading dashboard data...</p>}
            {error && <p className="login-error">{error}</p>}

            <div className="stats-grid">
              {statistics.map((item) => (
                <div className="stat-card" key={item.title}>
                  <span>{item.title}</span>
                  <h2>{loading ? '...' : item.value ?? '--'}</h2>
                </div>
              ))}
            </div>
          </section>
        )}

        {activePage === 'Doctors' && <Doctors />}
        {activePage === 'Patients' && <Patients />}
        {activePage === 'Appointments' && <Appointments />}
        {activePage === 'Medical Records' && <MedicalRecords />}
        {activePage === 'Prescriptions' && <Prescriptions />}
        {activePage === 'Laboratory' && <Laboratory />}
        {activePage === 'Pharmacy' && <Pharmacy />}
        {activePage === 'Billing' && <Billing />}
        {activePage === 'Reports' && <Reports />}

        {![
          'Dashboard',
          'Doctors',
          'Patients',
          'Appointments',
          'Medical Records',
          'Prescriptions',
          'Laboratory',
          'Pharmacy',
          'Billing',
          'Reports'
        ].includes(activePage) && (
            <section className="dashboard-content">
              <h2>{activePage}</h2>
              <p className="section-description">
                This module will be developed next.
              </p>
            </section>
          )}
      </main>
    </div>
  )
}

export default App
