import { useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export function OAuth2RedirectHandler() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();
  useEffect(() => {
    const token = searchParams.get('token');

    if (token) {
      const jwtResponse = { token: token }; 
      login(jwtResponse)
        .then(() => {
          navigate('/');
        })
        .catch(err => {
          console.error("Failed to process OAuth token:", err);
          navigate('/login', { state: { error: "Failed to log in with Google." } });
        });
    } else {
      navigate('/login', { state: { error: "Google login failed. No token received." } });
    }
  }, [searchParams, navigate, login]);
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <h2>Logging you in...</h2>
    </div>
  );
}