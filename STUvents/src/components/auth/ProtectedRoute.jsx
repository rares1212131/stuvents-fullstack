import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';


export function ProtectedRoute({ children, role }) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  // --- Check 1: Is the user logged in at all? ---
  if (!isAuthenticated) {
    // If not, redirect them to the login page.
    // We save the page they were trying to access in 'state.from'.
    // This allows us to redirect them back to that page after they successfully log in.
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  // --- Check 2: If a specific role is required, does the user have it? ---
  // The 'role' prop is passed from the route definition in App.jsx
  if (role && !user?.roles?.includes(role)) {
    // If the user is logged in but doesn't have the required role,
    // send them back to the homepage. You could also create a dedicated
    // "Access Denied" page for this scenario.
    return <Navigate to="/" replace />;
  }

  // --- All checks passed! ---
  // If the user is authenticated and has the correct role (if required),
  // render the actual page component they were trying to access.
  return children;
}