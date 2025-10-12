// This is the new content for OrganizerDashboardPage.jsx

import { Link } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
import '../admin/AdminDashboardPage.css'; // We can reuse the same CSS file for the hero section styling

export function OrganizerDashboardPage() {
  return (
    <div>
      <Header />
      {/* This hero section is styled to look like your mock-up */}
      <div className="admin-dashboard-hero">
        <div className="container">
          <h1>Create, Manage, and Sell Event Tickets with Ease</h1>
          <p>
            Welcome to your Organizer Dashboard. Here you can create new events,
            manage your existing ones, and track your sales.
          </p>
          <div className="admin-actions">
            {/* These links now point to the new organizer routes */}
            <Link to="/organizer/events/new" className="button-primary">Create an Event</Link>
            <Link to="/organizer/events" className="button-secondary">Manage My Events</Link>
          </div>
        </div>
      </div>
      
      <div className="container" style={{padding: '2rem 0'}}>
          {/* Future content like sales stats overview can go here */}
      </div>
    </div>
  );
}