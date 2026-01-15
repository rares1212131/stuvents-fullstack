
import { useState, useEffect } from 'react'; 
import { Link, useNavigate } from 'react-router-dom';
import api from '../../api/api'; 
import { Header } from '../../components/layout/Header';
import './AuthForm.css';

export function RegisterPage() {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    confirmPassword: '', 
  });

  const [passwordError, setPasswordError] = useState('');

  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  useEffect(() => {
    setError(''); 
    
    if (formData.password || formData.confirmPassword) { 
      if (formData.password.length > 0 && formData.password.length < 8) {
        setPasswordError('Password must be at least 8 characters long.');
      } else if (formData.password !== formData.confirmPassword && formData.confirmPassword.length > 0) {
        setPasswordError('Passwords do not match.');
      } else {
        setPasswordError(''); 
      }
    } else {
      setPasswordError(''); 
    }
  }, [formData.password, formData.confirmPassword]);

  const handleSubmit = async (e) => {
    e.preventDefault();

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
      const { firstName, lastName, email, password } = formData;
      await api.post('/auth/register', { firstName, lastName, email, password });
      
      navigate('/please-verify', { state: { email: formData.email } });
    } catch (err) {
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

          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm Password</label>
            <input id="confirmPassword" name="confirmPassword" type="password" onChange={handleChange} required />
          </div>

          {passwordError && <p className="error-message" style={{backgroundColor: 'transparent', color: '#dc3545', border: 'none', padding: 0, textAlign: 'left'}}>{passwordError}</p>}
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