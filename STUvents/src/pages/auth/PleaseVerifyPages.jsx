
import { useLocation } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
import './AuthForm.css'; 

export function PleaseVerifyPages() {
  const location = useLocation();
  const email = location.state?.email;

  return (
    <div>
      <Header />
      <div className="auth-container">
        <div className="auth-form" style={{ textAlign: 'center' }}>
          <h2>Thank You For Registering!</h2>
          <p style={{ margin: '1rem 0' }}>
            We've sent a verification link to your email address:
          </p>
          {email ? (
            <p><strong>{email}</strong></p>
          ) : (
            <p>Please check your inbox.</p>
          )}
          <p style={{ marginTop: '1rem' }}>
            Click the link in the email to activate your account.
          </p>
        </div>
      </div>
    </div>
  );
}