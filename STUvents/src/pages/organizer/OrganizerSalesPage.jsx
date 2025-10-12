// In file: src/pages/OrganizerSalesPage.jsx 

import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
import * as organizerService from '../../services/organizerService'; 
import '../admin/AdminSalesPage.css';

export function OrganizerSalesPage() {
  const { id } = useParams();
  const [eventData, setEventData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchSalesData = async () => {
      try {
        setLoading(true);
        const response = await organizerService.getMyEventById(id);
        setEventData(response.data);
      } catch (err) {
        setError('Failed to load sales data.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchSalesData();
  }, [id]);

  // All calculation logic remains exactly the same
  const totalRevenue = eventData?.ticketTypes.reduce((acc, tt) => acc + (tt.price * tt.ticketsSold), 0) || 0;
  const totalTicketsSold = eventData?.ticketTypes.reduce((acc, tt) => acc + tt.ticketsSold, 0) || 0;
  const totalCapacity = eventData?.ticketTypes.reduce((acc, tt) => acc + tt.totalAvailable, 0) || 0;

  if (loading) return <p className="centered-message">Loading statistics...</p>;
  if (error) return <p className="centered-message error-message">{error}</p>;
  if (!eventData) return null;

  return (
    <div>
      <Header />
      <div className="container admin-sales-page">
        <div className="sales-header">
          <h1>Sales for: {eventData.name}</h1>
          <p>An overview of ticket sales and revenue for this event.</p>
        </div>

        <div className="sales-summary">
          <div className="stat-card">
            <div className="stat-card-title">Total Revenue</div>
            <div className="stat-card-value">${totalRevenue.toFixed(2)}</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-title">Tickets Sold</div>
            <div className="stat-card-value">{totalTicketsSold} / {totalCapacity}</div>
          </div>
        </div>

        <div className="form-section">
          <h2>Sales by Ticket Type</h2>
          <table className="events-list-table">
            <thead>
              <tr>
                <th>Ticket Name</th>
                <th>Price</th>
                <th>Sold / Capacity</th>
                <th>Revenue</th>
              </tr>
            </thead>
            <tbody>
              {eventData.ticketTypes.map(tt => (
                <tr key={tt.id}>
                  <td>{tt.name}</td>
                  <td>${tt.price.toFixed(2)}</td>
                  <td>{tt.ticketsSold} / {tt.totalAvailable}</td>
                  <td>${(tt.price * tt.ticketsSold).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <div className="form-actions">
           <Link to="/organizer/events" className="button-secondary dark-bg">Back to My Events</Link>
        </div>
      </div>
    </div>
  );
}