
import api from './api'

export const getReportSummary = async () => {
  const response = await api.get('/reports/summary')
  return response.data
}
