import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export function TicketPurchaseCard({ ticketTypes, onPurchaseInitiate }) {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  const [selection, setSelection] = useState({ ticketId: null, quantity: '' });

  const handleTicketSelect = (ticketId, maxQuantity) => {

    if (ticketId !== selection.ticketId && maxQuantity > 0) {
      setSelection({ ticketId: ticketId, quantity: 1 });
    }
  };

  const handleQuantityChange = (e, maxQuantity) => {
    const value = e.target.value;
    if (value === '') {
      setSelection({ ...selection, quantity: '' });
      return;
    }
    const newQuantity = parseInt(value, 10);
    if (!isNaN(newQuantity) && newQuantity >= 1 && newQuantity <= maxQuantity) {
      setSelection({ ...selection, quantity: newQuantity });
    }
  };

  const handlePurchase = () => {
    const quantityToPurchase = parseInt(selection.quantity, 10);
    if (isNaN(quantityToPurchase) || quantityToPurchase < 1) {
      alert("Please enter a valid quantity.");
      return;
    }

    const selectedTicket = ticketTypes.find(t => t.id === selection.ticketId);
    onPurchaseInitiate(selectedTicket, quantityToPurchase);
  };

  const formatAvailability = (status) => {
    return status.replace('_', ' ').toUpperCase();
  };

  return (
    <div className="ticket-selection-card">
      <h3>Select Your Ticket</h3>
      <div className="ticket-options">
        {ticketTypes.map((ticket) => {
          const isSelected = selection.ticketId === ticket.id;
          return (
            <div
              key={ticket.id}
              className={`ticket-option ${isSelected ? 'selected' : ''} ${ticket.availability === 'SOLD_OUT' ? 'disabled' : ''}`}
              onClick={() => handleTicketSelect(ticket.id, ticket.maxPurchaseQuantity)}
            >
              <div>
                <div className="ticket-name">{ticket.name}</div>
                <div className="ticket-price">${ticket.price.toFixed(2)}</div>
              </div>
              {isSelected && (
                <div className="quantity-selector">
                  <label htmlFor="quantity">Qty:</label>
                  <input
                    type="number"
                    id="quantity"
                    min="1"
                    max={ticket.maxPurchaseQuantity}
                    value={selection.quantity}
                    onChange={(e) => handleQuantityChange(e, ticket.maxPurchaseQuantity)}
                    onClick={(e) => e.stopPropagation()} 
                    autoFocus
                  />
                </div>
              )}
              <span className={`availability-badge ${ticket.availability}`}>
                {formatAvailability(ticket.availability)}
              </span>
            </div>
          );
        })}
      </div>    
      {isAuthenticated ? (
        <button
          className="button-primary"
          style={{ width: '100%' }}
          onClick={handlePurchase}
          disabled={!selection.ticketId || !selection.quantity || selection.quantity < 1}
        >
          Purchase Ticket(s)
        </button>
      ) : (
        <Link to="/login" state={{ from: location }} className="button-primary" style={{ width: '100%', textAlign: 'center' }}>
          Log In to Purchase
        </Link>
      )}
    </div>
  );
}