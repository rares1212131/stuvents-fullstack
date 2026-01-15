
import { Link } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
import '../admin/AdminDashboardPage.css'; 

export function OrganizerDashboardPage() {
  return (
    <div>
      <Header />
      <div className="admin-dashboard-hero">
        <div className="container">
          <h1>Create, Manage, and Sell Event Tickets with Ease</h1>
          <p>
            Welcome to your Organizer Dashboard. Here you can create new events,
            manage your existing ones, and track your sales.
          </p>
          <div className="admin-actions">
            <Link to="/organizer/events/new" className="button-primary">Create an Event</Link>
            <Link to="/organizer/events" className="button-secondary">Manage My Events</Link>
          </div>
        </div>
      </div>
      
      <div className="container" style={{padding: '2rem 0'}}>
      </div>
    </div>
  );
}