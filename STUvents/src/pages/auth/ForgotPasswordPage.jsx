// In file: src/pages/auth/ForgotPasswordPage.jsx

import { useState } from 'react';
import { Header } from '../../components/layout/Header';
import api from '../../api/api';
import './AuthForm.css';

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    try {
      // Send the email as a plain text string in the request body
      // We need to specify the Content-Type header for this to work
      const response = await api.post('/auth/forgot-password', email, {
        headers: {
          'Content-Type': 'text/plain'
        }
      });
      setMessage(response.data);
    } catch (err) {
      // Even on error, we show a generic message for security
      setMessage('If an account with that email exists, a password reset link has been sent.', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Header />
      <div className="auth-container">
        <form className="auth-form" onSubmit={handleSubmit}>
          <h2>Forgot Password</h2>
          <p style={{ color: 'var(--text-color-light)', textAlign: 'center', marginBottom: '1rem' }}>
            Enter your email address and we'll send you a link to reset your password.
          </p>

          {/* This part of the form is only shown BEFORE a message is set */}
          {!message && (
            <>
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
              <button type="submit" className="button-primary" disabled={loading}>
                {loading ? 'Sending...' : 'Send Reset Link'}
              </button>
            </>
          )}

          {/* This part is shown AFTER the user submits the form */}
          {message && (
            <div className="message success" style={{ backgroundColor: '#e9f7ef', color: '#155724', border: '1px solid #c3e6cb' }}>
              <p>{message}</p>
            </div>
          )}
        </form>
      </div>
    </div>
  );
}