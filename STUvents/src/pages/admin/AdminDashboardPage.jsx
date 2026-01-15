
import { Link } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
import './AdminDashboardPage.css'; 

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
            <Link to="/admin/applications" className="button-primary">Review Applications</Link>
            <Link to="/admin/users" className="button-secondary">Manage Users</Link>
            <Link to="/admin/manage-filters" className="button-secondary">Manage Filters</Link>
          </div>
        </div>
      </div>
      
      <div className="container" style={{padding: '2rem 0'}}>
      </div>
    </div>
  );
}