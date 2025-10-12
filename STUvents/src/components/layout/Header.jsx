// This is the new, updated content for Header.jsx

import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom'; // <<< Import useLocation
import { useAuth } from '../../context/AuthContext';
import './Header.css';

const DEFAULT_AVATAR = "/icons/default-avatar.svg";

// <<< The component now accepts an onLogoClick prop
export function Header({ onLogoClick }) { 
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation(); // <<< Get the current page location
  const [dropdownOpen, setDropdownOpen] = useState(false);

  const isOrganizer = user?.roles?.includes('ROLE_ORGANIZER');
  const isAdmin = user?.roles?.includes('ROLE_ADMIN');

  const handleLogout = () => {
    logout();
    setDropdownOpen(false);
    navigate('/');
  };

  // <<< This is the new logic for the logo click
  const handleLogoClick = (e) => {
    // Check if we are already on the homepage and if the onLogoClick function was provided
    if (location.pathname === '/' && onLogoClick) {
      e.preventDefault(); // Prevent the default link navigation
      onLogoClick(); // Call the reset function from HomePage
    }
    // If we are on any other page, the Link will navigate to "/" normally.
  };

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownOpen && !event.target.closest('.profile-menu')) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [dropdownOpen]);

  return (
    <header className="main-header">
      <div className="container">
        {/* The Link now has an onClick handler */}
        <Link to="/" className="logo" onClick={handleLogoClick}>STUvents</Link>
        <nav className="main-nav">
          {isAuthenticated ? (
            <div className="profile-menu">
              <img
                src={user.profilePictureUrl || DEFAULT_AVATAR}
                alt="My Profile"
                className="avatar-image"
                onClick={() => setDropdownOpen(prev => !prev)}
              />

              {dropdownOpen && (
                <div className="dropdown-menu">
                  <Link to="/profile" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                    My Profile
                  </Link>
                  <Link to="/my-bookings" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                    My Bookings
                  </Link>

                  {(isOrganizer || isAdmin) && (
                    <Link to="/organizer" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                      Organizer Dashboard
                    </Link>
                  )}

                  {isAdmin && (
                    <Link to="/admin" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                      Admin Dashboard
                    </Link>
                  )}
                  
                  {!isOrganizer && !isAdmin && (
                    <>
                      <div style={{ height: '1px', backgroundColor: 'var(--border-color)', margin: '0.5rem 0' }} />
                      <Link to="/apply-organizer" className="dropdown-item" onClick={() => setDropdownOpen(false)}>
                        Become an Organizer
                      </Link>
                    </>
                  )}
                  
                  <div style={{ height: '1px', backgroundColor: 'var(--border-color)', margin: '0.5rem 0' }} />
                  <button onClick={handleLogout} className="dropdown-logout-button">
                    Log Out
                  </button>
                </div>
              )}
            </div>
          ) : (
            <>
              <Link to="/login" className="nav-button">Log In</Link>
              <Link to="/register" className="nav-button signup">Sign Up</Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}