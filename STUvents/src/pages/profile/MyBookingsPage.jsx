// In file: src/pages/MyBookingsPage.jsx (REFACTORED and COMPLETE)

import { useState, useEffect } from 'react';
import dayjs from 'dayjs';
import * as bookingService from '../../services/bookingService'; // <-- IMPORT THE SERVICE
import { Header } from '../../components/layout/Header';
import './MyBookingsPage.css';

export function MyBookingsPage() {
  const [bookings, setBookings] = useState([]);
  
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    setLoading(true);

    const fetchMyBookings = async () => {
      try {
        const response = await bookingService.getMyBookings(page);
        
        setBookings(prevBookings => [...prevBookings, ...response.data.content]);
        setHasMore(!response.data.last);

      } catch (err) {
        setError('Could not fetch your bookings. Please try logging in again.');
        console.error("Failed to fetch bookings:", err);
      } finally {
        setLoading(false);
      }
    };

    if (hasMore) {
      fetchMyBookings();
    } else {
      setLoading(false);
    }
  // We disable the exhaustive-deps warning because we intentionally want this
  // to run ONLY when the page number changes.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const handleLoadMore = () => {
    setPage(prevPage => prevPage + 1);
  };

  return (
    <div className="my-bookings-page">
      <Header />
      <div className="bookings-hero">
        <div className="container">
          <h1>Your Tickets</h1>
          <p>
            A history of all your purchased event tickets. Thank you for using STUvents!
          </p>
        </div>
      </div>

      <div className="container bookings-list-container">
        {loading && page === 0 && <p className="centered-message">Loading your tickets...</p>}
        
        {error && <p className="centered-message error-message">{error}</p>}
        
        {!(loading && page === 0) && !error && (
          <>
            <div className="bookings-list">
              {bookings.length > 0 ? (
                bookings.map(booking => (
                  <div key={booking.id} className="booking-card">
                    <div className="booking-event-details">
                      <h3>{booking.event.name}</h3>
                      <p>{dayjs(booking.event.eventDateTime).format('dddd, MMMM D, YYYY')}</p>
                    </div>
                    <div className="booking-ticket-details">
                      <span className="ticket-name">{booking.ticketType.name}</span>
                      <span className="ticket-price">${booking.ticketType.price.toFixed(2)}</span>
                    </div>
                  </div>
                ))
              ) : (
                <p className="centered-message">You have not purchased any tickets yet.</p>
              )}
            </div>

            {hasMore && (
              <div style={{ textAlign: 'center', marginTop: '2rem' }}>
                <button 
                  onClick={handleLoadMore} 
                  className="button-primary"
                  disabled={loading}
                >
                  {loading ? 'Loading...' : 'Load More'}
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}