// src/pages/LoginPage.jsx (FIXED)

import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import api from '../../api/api';
import { Header } from '../../components/layout/Header';
import './AuthForm.css';

export function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const from = location.state?.from?.pathname || "/";

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/auth/login', { email, password });
      
      // Wait for the login function to completely finish
      // The login function now returns the user data when complete
      await login(response.data);
      
      // Add a small delay to ensure all state updates are processed
      setTimeout(() => {
        navigate(from, { replace: true });
      }, 100);

    } catch (err) {
      console.error('Login error:', err);
      setError('Failed to log in. Please check your credentials.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Header />
      <div className="auth-container">
        <form className="auth-form" onSubmit={handleSubmit}>
          <h2>Log In</h2>
          {error && <p className="error-message">{error}</p>}
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>
          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          <button type="submit" className="button-primary" disabled={loading}>
            {loading ? 'Logging In...' : 'Log In'}
          </button>
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', margin: '1.5rem 0' }}>
            <hr style={{ flexGrow: 1, borderTop: '1px solid var(--border-color)' }} />
            <span style={{ color: 'var(--text-color-light)' }}>OR</span>
            <hr style={{ flexGrow: 1, borderTop: '1px solid var(--border-color)' }} />
          </div>

          <a href={`${import.meta.env.VITE_API_URL}/oauth2/authorization/google`} className="button-secondary" style={{ width: '100%' }}>
  {/* ... */}
  Log In with Google
</a>
          {/* ★★★ END OF NEW CODE ★★★ */}
          <p className="auth-switch-text">
            Don't have an account? <Link to="/register">Sign Up</Link>
          </p>
          <p className="auth-switch-text" style={{ marginTop: '1rem' }}>
            <Link to="/forgot-password">Forgot Password?</Link>
          </p>
        </form>
      </div>
    </div>
  );
}