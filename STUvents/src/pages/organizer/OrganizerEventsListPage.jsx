

import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import dayjs from 'dayjs';
import { Header } from '../../components/layout/Header';
import * as organizerService from '../../services/organizerService'; 
import '../admin/AdminEventsListPage.css'; 

export function OrganizerEventsListPage() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const fetchMyEvents = async () => {
    try {
      setLoading(true);
      const response = await organizerService.getMyEvents();
      setEvents(response.data.content);
    } catch (err) {
      setError('Failed to fetch your events.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMyEvents();
  }, []);

  const handleDelete = async (eventId) => {
    if (!window.confirm("Are you sure you want to permanently delete this event? This action cannot be undone.")) {
      return;
    }

    try {
      await organizerService.deleteMyEvent(eventId);
      fetchMyEvents(); 
    } catch (err) {
      setError('Failed to delete event. Please try again.');
      console.error(err);
    }
  };

  return (
    <div>
      <Header />
      <div className="container admin-events-page">
        <div className="admin-events-header">
          <h1>Manage Your Events</h1>
          <Link to="/organizer/events/new" className="button-primary">
            Create New Event
          </Link>
        </div>

        {loading && <p className="centered-message">Loading your events...</p>}
        {error && <p className="centered-message error-message">{error}</p>}

        {!loading && !error && (
          <table className="events-list-table">
            <thead>
              <tr>
                <th>Event Name</th>
                <th>Date</th>
                <th>City</th>
                <th style={{width: '450px'}}>Actions</th> 
              </tr>
            </thead>
            <tbody>
              {events.map(event => (
                <tr key={event.id}>
                  <td>{event.name}</td>
                  <td>{dayjs(event.eventDateTime).format('MMM D, YYYY')}</td>
                  <td>{event.city.name}</td>
                  <td className="event-actions">
                    <Link to={`/organizer/events/${event.id}/stats`} className="button-secondary dark-bg">
                      View Stats
                    </Link>
                    <Link to={`/organizer/events/${event.id}/attendees`} className="button-secondary dark-bg">
                      View Attendees
                    </Link>
                    <Link to={`/organizer/events/edit/${event.id}`} className="button-secondary dark-bg">
                      Edit
                    </Link>
                    <button onClick={() => handleDelete(event.id)} className="button-secondary button-danger">
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}