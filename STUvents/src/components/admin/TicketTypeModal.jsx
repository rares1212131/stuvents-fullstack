// src/components/admin/TicketTypeModal.jsx

import { useState, useEffect } from 'react';
import api from '../../api/api';
import './TicketTypeModal.css';

export function TicketTypeModal({ eventId, onClose }) {
  const [ticketTypes, setTicketTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // State for the "add new" form
  const [newTicket, setNewTicket] = useState({ name: '', price: '', totalAvailable: '' });

  const fetchTicketTypes = async () => {
    try {
      setLoading(true);
      const response = await api.get(`/admin/events/${eventId}`);
      setTicketTypes(response.data.ticketTypes);
    } catch (err) {
      setError('Failed to load ticket types.', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTicketTypes();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [eventId]);

  const handleAddNew = async (e) => {
    e.preventDefault();
    try {
      await api.post(`/admin/events/${eventId}/ticket-types`, newTicket);
      setNewTicket({ name: '', price: '', totalAvailable: '' }); // Clear form
      fetchTicketTypes(); // Refresh the list
    } catch (err) {
      setError('Failed to add new ticket type. ', err);
    }
  };

  const handleDelete = async (ticketTypeId) => {
    if (!window.confirm("Are you sure you want to delete this ticket type?")) return;
    try {
      await api.delete(`/admin/events/${eventId}/ticket-types/${ticketTypeId}`);
      fetchTicketTypes(); // Refresh the list
    } catch (err) {
      setError('Failed to delete ticket type.', err);
    }
  };
  
  // Note: A full "edit-in-place" feature would add more complexity. 
  // For now, we'll manage via delete and add. A PUT endpoint could be added for editing.

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Manage Ticket Types</h2>
          <button onClick={onClose} className="modal-close-button">&times;</button>
        </div>
        <div className="modal-body">
          {error && <p className="error-message">{error}</p>}
          {loading && <p>Loading...</p>}

          {/* List existing ticket types */}
          <div className="ticket-types-list">
            {ticketTypes.map(tt => (
              <div key={tt.id} className="ticket-type-row">
                <div>{tt.name}</div>
                <div>${tt.price.toFixed(2)}</div>
                <div>{tt.ticketsSold} / {tt.totalAvailable} sold</div>
                <button onClick={() => handleDelete(tt.id)} className="button-secondary button-danger">Delete</button>
              </div>
            ))}
          </div>

          <hr style={{ margin: '2rem 0', borderColor: '#495057' }} />

          {/* Form to add a new ticket type */}
          <form onSubmit={handleAddNew}>
            <h3>Add New Ticket Type</h3>
            <div className="ticket-type-row">
              <div className="form-group">
                <input type="text" placeholder="Ticket Name" value={newTicket.name} onChange={e => setNewTicket({...newTicket, name: e.target.value})} required />
              </div>
              <div className="form-group">
                <input type="number" placeholder="Price" value={newTicket.price} onChange={e => setNewTicket({...newTicket, price: e.target.value})} required min="0" />
              </div>
              <div className="form-group">
                <input type="number" placeholder="Quantity" value={newTicket.totalAvailable} onChange={e => setNewTicket({...newTicket, totalAvailable: e.target.value})} required min="1" />
              </div>
              <button type="submit" className="button-primary">Add</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}