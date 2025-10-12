// In file: src/pages/OrganizerAtendeesPage.jsx (REFACTORED and COMPLETE)

import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import dayjs from 'dayjs';
import { Header } from '../../components/layout/Header';
// import api from '../api/api'; // <-- No longer needed!
import * as organizerService from '../../services/organizerService'; // <-- IMPORT THE SERVICE
import '../admin/AdminEventsListPage.css'; // Reusing styles

export function OrganizerAtendeesPage() {
  const { id } = useParams();
  const [attendees, setAttendees] = useState([]);
  const [eventName, setEventName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchAttendees = async () => {
      try {
        setLoading(true);
        // We can use Promise.all to make both calls concurrently for better performance.
        const [eventRes, attendeesRes] = await Promise.all([
            organizerService.getMyEventById(id),      // <-- USE SERVICE
            organizerService.getEventAttendees(id) // <-- USE SERVICE
        ]);
        
        setEventName(eventRes.data.name);
        setAttendees(attendeesRes.data.content);
      } catch (err) {
        setError('Failed to fetch attendee data.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchAttendees();
  }, [id]);

  return (
    <div>
      <Header />
      <div className="container admin-events-page">
        <div className="admin-events-header">
          <div>
            <h1>Attendee List</h1>
            <p style={{ color: '#6c757d', marginTop: '0.5rem' }}>For event: {eventName}</p>
          </div>
          <Link to="/organizer/events" className="button-secondary dark-bg">
            Back to My Events
          </Link>
        </div>

        {loading && <p className="centered-message">Loading attendees...</p>}
        {error && <p className="centered-message error-message">{error}</p>}

        {!loading && !error && (
          <table className="events-list-table">
            <thead>
              <tr>
                <th>User Name</th>
                <th>Email</th>
                <th>Ticket Type</th>
                <th>Booking Date</th>
              </tr>
            </thead>
            <tbody>
              {attendees.map(booking => (
                <tr key={booking.id}>
                  <td>{booking.user.firstName} {booking.user.lastName}</td>
                  <td>{booking.user.email}</td>
                  <td>{booking.ticketType.name}</td>
                  <td>{dayjs(booking.bookingDateTime).format('MMM D, YYYY h:mm A')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}