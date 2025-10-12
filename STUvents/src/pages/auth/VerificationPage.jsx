// In file: src/pages/auth/VerificationPage.jsx

import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
import api from '../../api/api';
import './AuthForm.css';

export function VerificationPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token');

  const [verificationStatus, setVerificationStatus] = useState('verifying'); // 'verifying', 'success', 'error'
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (!token) {
      setVerificationStatus('error');
      setErrorMessage('No verification token found. The link may be invalid.');
      return;
    }

    const verifyToken = async () => {
      try {
        await api.get(`/auth/verify-email?token=${token}`);
        setVerificationStatus('success');
      } catch (err) {
        setVerificationStatus('error');

        // ★★★ THIS IS THE FIX ★★★
        // This ensures we always set a STRING as the error message.
        if (typeof err.response?.data === 'string') {
          setErrorMessage(err.response.data);
        } else {
          setErrorMessage('An unknown error occurred. The token might be expired or invalid.');
        }
      }
    };
    

    verifyToken();
  }, [token]); // The effect runs once when the component mounts and gets the token.

  const renderContent = () => {
    switch (verificationStatus) {
      case 'verifying':
        return <h2>Verifying your account, please wait...</h2>;
      case 'success':
        return (
          <>
            <h2>Account Verified!</h2>
            <p style={{ margin: '1rem 0' }}>Your email has been successfully verified.</p>
            <Link to="/login" className="button-primary">Proceed to Login</Link>
          </>
        );
      case 'error':
        return (
          <>
            <h2>Verification Failed</h2>
            <p className="error-message">{errorMessage}</p>
            <p style={{ marginTop: '1rem' }}>Please try registering again or contact support.</p>
          </>
        );
      default:
        return null;
    }
  };

  return (
    <div>
      <Header />
      <div className="auth-container">
        <div className="auth-form" style={{ textAlign: 'center' }}>
          {renderContent()}
        </div>
      </div>
    </div>
  );
}