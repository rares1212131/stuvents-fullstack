import { Routes, Route } from 'react-router-dom';

// --- Core & Public Pages ---
import { HomePage } from './pages/HomePage';
import { LoginPage } from './pages/auth/LoginPage';
import { RegisterPage } from './pages/auth/RegisterPage';
import { EventDetailsPage } from './pages/EventDetailsPage';




// --- General Authenticated User Pages ---
import { MyBookingsPage } from './pages/profile/MyBookingsPage';
import { ProfilePage } from './pages/profile/ProfilePage';
import { OrganizerApplicationPage } from './pages/organizer/OrganizerApplicationPage';

// --- Organizer Pages ---
import { OrganizerDashboardPage } from './pages/organizer/OrganizerDashboardPage';
import { OrganizerEventsListPage } from './pages/organizer/OrganizerEventsListPage';
import { EventEditorPage } from './pages/organizer/EventEditorPage';
import { OrganizerSalesPage } from './pages/organizer/OrganizerSalesPage';
import { OrganizerAtendeesPage } from './pages/organizer/OrganizerAtendeesPage';

// --- Admin Pages ---
import { AdminDashboardPage } from './pages/admin/AdminDashboardPage';
import { AdminApplicationsPages } from './pages/admin/AdminApplicationsPages';
import { AdminUsersListPage } from './pages/admin/AdminUsersListPage';
import { AdminEventsFiltersPage } from './pages/admin/AdminEventsFiltersPage';

// --- Auth Component ---
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { PleaseVerifyPages } from './pages/auth/PleaseVerifyPages';
import { VerificationPage } from './pages/auth/VerificationPage';

import { ForgotPasswordPage } from './pages/auth/ForgotPasswordPage';
import { ResetPasswordPage } from './pages/auth/ResetPasswordPage';

import { OAuth2RedirectHandler } from './pages/auth/OAuth2RedirectHandler';

function App() {
  return (
    <Routes>
      {/* ===================================================================== */}
      {/* == 1. PUBLIC ROUTES (Accessible to everyone)                       == */}
      {/* ===================================================================== */}
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
      <Route path="/events/:id" element={<EventDetailsPage />} />

      <Route path="/please-verify" element={<PleaseVerifyPages />} />
      <Route path="/verify-email" element={<VerificationPage />} />

       <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      {/* ===================================================================== */}
      {/* == 2. AUTHENTICATED USER ROUTES (Requires any login)               == */}
      {/* ===================================================================== */}
      <Route 
        path="/my-bookings"
        element={<ProtectedRoute><MyBookingsPage /></ProtectedRoute>}
      />
      <Route 
        path="/profile"
        element={<ProtectedRoute><ProfilePage /></ProtectedRoute>}
      />
      <Route
        path="/apply-organizer"
        element={<ProtectedRoute><OrganizerApplicationPage /></ProtectedRoute>}
      />

      {/* ===================================================================== */}
      {/* == 3. ORGANIZER ROUTES (Requires ROLE_ORGANIZER or ROLE_ADMIN)     == */}
      {/* ===================================================================== */}
      <Route 
        path="/organizer" 
        element={<ProtectedRoute role="ROLE_ORGANIZER"><OrganizerDashboardPage /></ProtectedRoute>} 
      />
      <Route 
        path="/organizer/events" 
        element={<ProtectedRoute role="ROLE_ORGANIZER"><OrganizerEventsListPage /></ProtectedRoute>} 
      />
      <Route 
        path="/organizer/events/new" 
        element={<ProtectedRoute role="ROLE_ORGANIZER"><EventEditorPage /></ProtectedRoute>} 
      />
      <Route 
        path="/organizer/events/edit/:id" 
        element={<ProtectedRoute role="ROLE_ORGANIZER"><EventEditorPage /></ProtectedRoute>} 
      />
      <Route 
        path="/organizer/events/:id/stats" 
        element={<ProtectedRoute role="ROLE_ORGANIZER"><OrganizerSalesPage /></ProtectedRoute>} 
      />
      <Route 
        path="/organizer/events/:id/attendees" 
        element={<ProtectedRoute role="ROLE_ORGANIZER"><OrganizerAtendeesPage /></ProtectedRoute>} 
      />

      {/* ===================================================================== */}
      {/* == 4. ADMIN ROUTES (Requires ROLE_ADMIN only)                      == */}
      {/* ===================================================================== */}
      <Route 
        path="/admin" 
        element={<ProtectedRoute role="ROLE_ADMIN"><AdminDashboardPage /></ProtectedRoute>} 
      />
      <Route 
        path="/admin/applications" 
        element={<ProtectedRoute role="ROLE_ADMIN"><AdminApplicationsPages /></ProtectedRoute>} 
      />
      <Route 
        path="/admin/users" 
        element={<ProtectedRoute role="ROLE_ADMIN"><AdminUsersListPage /></ProtectedRoute>} 
      />
      <Route 
        path="/admin/manage-filters" 
        element={<ProtectedRoute role="ROLE_ADMIN"><AdminEventsFiltersPage /></ProtectedRoute>} 
      />

    </Routes>
  );
}

export default App;