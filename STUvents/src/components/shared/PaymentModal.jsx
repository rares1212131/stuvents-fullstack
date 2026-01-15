import './PaymentModal.css'; 

export function PaymentModal({ isOpen, onClose, onConfirm, bookingDetails, isLoading, error }) {

  if (!isOpen) {
    return null;
  }

  if (!bookingDetails) {
    return null;
  }

  const totalCost = (bookingDetails.quantity * bookingDetails.ticket.price).toFixed(2);

  return (
    <div className="modal-overlay">
      <div className="modal-content">
        <div className="modal-header">
          <h2>Confirm Your Purchase</h2>
          <button onClick={onClose} className="close-button">&times;</button>
        </div>
        
        <div className="modal-body">
          <div className="order-summary">
            <p><strong>Event:</strong> {bookingDetails.eventName}</p>
            <p><strong>Ticket:</strong> {bookingDetails.ticket.name}</p>
            <p><strong>Quantity:</strong> {bookingDetails.quantity}</p>
            <p className="total-cost"><strong>Total:</strong> ${totalCost}</p>
          </div>

          <div className="payment-form">
            <h4>Enter Payment Details</h4>
            <p className="mock-notice">(This is a mock form. No real data is needed or stored.)</p>
            
            <div className="form-group">
              <label>Card Number</label>
              <input type="text" placeholder="**** **** **** 1234" readOnly />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Expiry Date</label>
                <input type="text" placeholder="MM/YY" readOnly />
              </div>
              <div className="form-group">
                <label>CVC</label>
                <input type="text" placeholder="123" readOnly />
              </div>
            </div>
          </div>
          {error && <p className="error-message" style={{textAlign: 'center'}}>{error}</p>}
        </div>

        <div className="modal-footer">
          <button onClick={onConfirm} className="button-primary" disabled={isLoading}>
            {isLoading ? 'Processing...' : `Pay $${totalCost}`}
          </button>
        </div>
      </div>
    </div>
  );
}