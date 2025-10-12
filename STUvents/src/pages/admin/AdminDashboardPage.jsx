// This is the new content for the AdminDashboardPage.jsx

import { Link } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
import './AdminDashboardPage.css'; // We can reuse the same hero section CSS

export function AdminDashboardPage() {
  return (
    <div>
      <Header />
      <div className="admin-dashboard-hero">
        <div className="container">
          <h1>Platform Administration</h1>
          <p>
            Welcome, Admin. From this dashboard, you can manage the core
            features of the STUvents platform.
          </p>
          <div className="admin-actions">
            {/* These links point to the new admin-specific pages */}
            <Link to="/admin/applications" className="button-primary">Review Applications</Link>
            <Link to="/admin/users" className="button-secondary">Manage Users</Link>
            <Link to="/admin/manage-filters" className="button-secondary">Manage Filters</Link>
          </div>
        </div>
      </div>
      
      <div className="container" style={{padding: '2rem 0'}}>
          {/* You could add platform-wide statistics here in the future,
              like "Total Users" or "Total Events Created". */}
      </div>
    </div>
  );
}