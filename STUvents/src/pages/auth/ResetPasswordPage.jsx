// In file: src/pages/auth/ResetPasswordPage.jsx

import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
import api from '../../api/api';
import './AuthForm.css';

export function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');
  const navigate = useNavigate();

  const [formData, setFormData] = useState({ newPassword: '', confirmPassword: '' });
  const [passwordError, setPasswordError] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // Real-time validation for the password fields
  useEffect(() => {
    if (formData.newPassword || formData.confirmPassword) {
      if (formData.newPassword.length > 0 && formData.newPassword.length < 8) {
        setPasswordError('Password must be at least 8 characters long.');
      } else if (formData.newPassword !== formData.confirmPassword && formData.confirmPassword.length > 0) {
        setPasswordError('Passwords do not match.');
      } else {
        setPasswordError('');
      }
    }
  }, [formData.newPassword, formData.confirmPassword]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (passwordError) {
      setError('Please fix the password errors.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      await api.post('/auth/reset-password', {
        token: token,
        newPassword: formData.newPassword,
      });
      // On success, redirect to login with a success message
      navigate('/login', { state: { message: 'Password reset successfully! Please log in.' } });
    } catch (err) {
      setError(err.response?.data || 'Failed to reset password. The link may be invalid or expired.');
    } finally {
      setLoading(false);
    }
  };

  // If the user somehow gets here without a token, show an error.
  if (!token) {
    return (
        <div>
            <Header />
            <div className="auth-container"><p className="error-message">Invalid password reset link. No token found.</p></div>
        </div>
    );
  }

  return (
    <div>
      <Header />
      <div className="auth-container">
        <form className="auth-form" onSubmit={handleSubmit}>
          <h2>Reset Your Password</h2>
          <p style={{ color: 'var(--text-color-light)', textAlign: 'center', marginBottom: '1rem' }}>
            Enter your new password below.
          </p>

          {error && <p className="error-message">{error}</p>}

          <div className="form-group">
            <label htmlFor="newPassword">New Password</label>
            <input id="newPassword" name="newPassword" type="password" onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm New Password</label>
            <input id="confirmPassword" name="confirmPassword" type="password" onChange={handleChange} required />
          </div>

          {passwordError && <p className="error-message" style={{backgroundColor: 'transparent', color: '#dc3545', border: 'none', padding: 0, textAlign: 'left'}}>{passwordError}</p>}
          
          <button type="submit" className="button-primary" disabled={loading || !!passwordError}>
            {loading ? 'Resetting...' : 'Reset Password'}
          </button>
        </form>
      </div>
    </div>
  );
}