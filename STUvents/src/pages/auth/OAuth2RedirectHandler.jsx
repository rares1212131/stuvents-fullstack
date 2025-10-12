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
      // We have a token. We need to tell our AuthContext about this new login.
      // We can create a "fake" JWTResponse object that our login function expects.
      const jwtResponse = { token: token }; 
      login(jwtResponse)
        .then(() => {
          // After the login state is updated, go to the homepage.
          navigate('/');
        })
        .catch(err => {
          console.error("Failed to process OAuth token:", err);
          // If something goes wrong, send the user to the login page with an error.
          navigate('/login', { state: { error: "Failed to log in with Google." } });
        });
    } else {
      // No token found, this is an error.
      navigate('/login', { state: { error: "Google login failed. No token received." } });
    }
  // We add navigate and login to the dependency array to satisfy the linter.
  }, [searchParams, navigate, login]);

  // This component will only be on screen for a moment, so we can just show a loading message.
  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <h2>Logging you in...</h2>
    </div>
  );
}