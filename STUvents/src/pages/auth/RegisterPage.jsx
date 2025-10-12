// In file: src/pages/RegisterPage.jsx
// (Correct version WITHOUT using a service)

import { useState, useEffect } from 'react'; // <-- 1. Import useEffect
import { Link, useNavigate } from 'react-router-dom';
import api from '../../api/api'; // <-- We are still using this
import { Header } from '../../components/layout/Header';
import './AuthForm.css';

export function RegisterPage() {
  // 2. Add confirmPassword to our form state
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '', // <-- New field
  });

  // 3. We'll have a dedicated error state for password issues
  const [passwordError, setPasswordError] = useState('');

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  // 4. This useEffect will run every time the user types in either password field
  useEffect(() => {
    // We clear the main form error whenever the password changes
    setError(''); 
    
    if (formData.password || formData.confirmPassword) { // Check if either field has been touched
      if (formData.password.length > 0 && formData.password.length < 8) {
        setPasswordError('Password must be at least 8 characters long.');
      } else if (formData.password !== formData.confirmPassword && formData.confirmPassword.length > 0) {
        setPasswordError('Passwords do not match.');
      } else {
        setPasswordError(''); // Clear the error if everything is okay
      }
    } else {
      setPasswordError(''); // Don't show an error if fields are empty
    }
  }, [formData.password, formData.confirmPassword]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    // 5. Final check before submitting to the API
    if (passwordError) {
      setError('Please fix the errors with your password.');
      return;
    }
    if (formData.password.length < 8) {
        setError('Password must be at least 8 characters long.');
        return;
    }

    setError('');
    setLoading(true);
    try {
      // The logic here remains the same, using api.post directly.
      // We explicitly DO NOT send confirmPassword to the backend.
      const { firstName, lastName, email, password } = formData;
      await api.post('/auth/register', { firstName, lastName, email, password });
      
      navigate('/please-verify', { state: { email: formData.email } });
    } catch (err) {
      // The error handling is also the same.
      setError(err.response?.data?.message || 'Registration failed. The email might already be in use.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Header />
      <div className="auth-container">
        <form className="auth-form" onSubmit={handleSubmit}>
          <h2>Create Account</h2>
          {error && <p className="error-message">{error}</p>}
          
          <div className="form-group">
            <label htmlFor="firstName">First Name</label>
            <input id="firstName" name="firstName" type="text" onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label htmlFor="lastName">Last Name</label>
            <input id="lastName" name="lastName" type="text" onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input id="email" name="email" type="email" onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input id="password" name="password" type="password" onChange={handleChange} required />
          </div>

          {/* 6. Add the new "Confirm Password" input field */}
          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <input id="confirmPassword" name="confirmPassword" type="password" onChange={handleChange} required />
          </div>

          {/* 7. Display the real-time password error message */}
          {passwordError && <p className="error-message" style={{backgroundColor: 'transparent', color: '#dc3545', border: 'none', padding: 0, textAlign: 'left'}}>{passwordError}</p>}
          
          {/* 8. Disable the button if there's a password error or we are loading */}
          <button type="submit" className="button-primary" disabled={loading || !!passwordError}>
            {loading ? 'Creating Account...' : 'Sign Up'}
          </button>
          
          <p className="auth-switch-text">
            Already have an account? <Link to="/login">Log In</Link>
          </p>
        </form>
      </div>
    </div>
  );
}