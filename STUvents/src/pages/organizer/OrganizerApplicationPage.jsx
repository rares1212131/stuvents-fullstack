// In file: src/pages/OrganizerApplicationPage.jsx (REFACTORED and COMPLETE)

import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
// import api from '../api/api'; // <-- No longer needed!
import * as organizerService from '../../services/organizerService'; // <-- IMPORT THE SERVICE
import '../auth/AuthForm.css'; // Reusing the auth form styles for a centered layout

export function OrganizerApplicationPage() {
  const [reason, setReason] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    if (!reason.trim()) {
      setError('Please provide a reason for your application.');
      return;
    }

    setLoading(true);
    try {
      // USE THE SERVICE to make the API call
      await organizerService.applyToBeOrganizer(reason);
      alert('Application submitted successfully! Your application is now pending review.');
      navigate('/profile'); 
    } catch (err) {
      if (err.response && err.response.status === 400 && err.response.data.messages) {
        const validationErrors = err.response.data.messages;
        const reasonError = validationErrors.reason; 
        setError(reasonError || 'A validation error occurred.');
      } else {
        const errorMessage = err.response?.data?.message || 'Failed to submit application. You may have already applied.';
        setError(errorMessage);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Header />
      <div className="auth-container">
        <form className="auth-form" onSubmit={handleSubmit} style={{maxWidth: '600px'}}>
          <h2>Become an Organizer</h2>
          <p style={{ color: 'var(--text-color-light)', textAlign: 'center', marginBottom: '1rem' }}>
            Please tell us why you would like to create and manage events on STUvents.
          </p>
          
          {error && <p className="error-message">{error}</p>}

          <div className="form-group">
            <label htmlFor="reason">Your Reason (min. 50 characters)</label>
            <textarea
              id="reason"
              rows="8"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="E.g., I'm a student representative for our faculty and I want to organize several campus-wide events to improve student engagement..."
              required
              style={{ fontFamily: 'var(--font-family-main)', fontSize: '1rem', padding: '0.75rem', border: '1px solid var(--border-color)', borderRadius: '6px' }}
            />
          </div>
          
          <button type="submit" className="button-primary" disabled={loading}>
            {loading ? 'Submitting...' : 'Submit Application'}
          </button>
        </form>
      </div>
    </div>
  );
}