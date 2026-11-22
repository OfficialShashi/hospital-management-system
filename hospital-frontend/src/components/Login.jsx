
import { useState } from 'react'
import { loginUser } from '../services/authService'
import './Login.css'

function Login({ onLogin }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const data = await loginUser(email, password)

      localStorage.setItem('token', data.token)
      localStorage.setItem('user', JSON.stringify(data))

      onLogin(data)
    } catch (err) {
      setError(
        err.response?.data?.message || 'Invalid email or password'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-container">
      {/* Hospital Background Image */}
      <img
        src="/hospital-login-bg.png"
        alt=""
        className="login-background-image"
      />

      {/* Background Overlay */}
      <div className="login-overlay"></div>

      {/* Login Popup */}
      <form className="login-form" onSubmit={handleSubmit}>
        <div className="login-logo">🏥</div>

        <h1>HMS Login</h1>

        <p className="login-subtitle">
          Hospital Management System
        </p>

        <div className="login-input-group">
          <label>Email Address</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Enter your email"
            required
          />
        </div>

        <div className="login-input-group">
          <label>Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter your password"
            required
          />
        </div>

        {error && <p className="login-error">{error}</p>}

        <button
          type="submit"
          className="login-submit-btn"
          disabled={loading}
        >
          {loading ? 'Logging in...' : 'Login'}
        </button>

        <p className="login-security">
          🔒 Secure Hospital Management Portal
        </p>
      </form>
    </div>
  )
}

export default Login
