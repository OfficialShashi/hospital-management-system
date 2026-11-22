
import { useEffect, useState } from 'react'
import { getReportSummary } from '../services/reportService'
import './Reports.css'
function Reports() {
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadReport = async () => {
    try {
      setLoading(true)
      setError('')

      const data = await getReportSummary()
      setReport(data)
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Unable to load reports.'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadReport()
  }, [])

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      maximumFractionDigits: 2,
    }).format(amount ?? 0)
  }

  const reportCards = report
    ? [
        {
          title: 'Total Patients',
          value: report.totalPatients,
        },
        {
          title: 'Total Doctors',
          value: report.totalDoctors,
        },
        {
          title: 'Total Appointments',
          value: report.totalAppointments,
        },
        {
          title: 'Completed Appointments',
          value: report.completedAppointments,
        },
        {
          title: 'Cancelled Appointments',
          value: report.cancelledAppointments,
        },
        {
          title: 'Total Invoices',
          value: report.totalInvoices,
        },
        {
          title: 'Total Revenue',
          value: formatCurrency(report.totalRevenue),
        },
      ]
    : []

  if (loading) {
    return <div className="reports-page">Loading reports...</div>
  }

  if (error) {
    return (
      <div className="reports-page">
        <p className="reports-error">{error}</p>
        <button onClick={loadReport}>Retry</button>
      </div>
    )
  }

  return (
    <div className="reports-page">
      <div className="reports-header">
        <div>
          <h2>Reports</h2>
          <p>Hospital summary and performance overview</p>
        </div>

        <button onClick={loadReport}>
          Refresh Report
        </button>
      </div>

      <div className="reports-grid">
        {reportCards.map((card) => (
          <div className="report-card" key={card.title}>
            <h3>{card.title}</h3>
            <p>{card.value}</p>
          </div>
        ))}
      </div>
    </div>
  )
}

export default Reports
