// In file: src/pages/EventDetailsPage.jsx (REFACTORED and COMPLETE)

import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import dayjs from 'dayjs';

import * as eventService from '../services/eventService';
import * as bookingService from '../services/bookingService';

import { Header } from '../components/layout/Header';
import { TicketPurchaseCard } from '../components/events/TicketPurchaseCard';
import { PaymentModal } from '../components/shared/PaymentModal';
import './EventDetailsPage.css';

function InfoDetail({ icon, text }) {
  return (
    <div className="info-item">
      <img src={`/icons/${icon}.svg`} alt="" className="info-icon" />
      <span className="info-text">{text}</span>
    </div>
  );
}

export function EventDetailsPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // State for the modal and booking process
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [bookingDetails, setBookingDetails] = useState(null);
  const [isBooking, setIsBooking] = useState(false);
  const [bookingError, setBookingError] = useState('');

  useEffect(() => {
    const fetchEvent = async () => {
      try {
        setLoading(true);
        // USE THE SERVICE to fetch the event by its ID from the URL
        const response = await eventService.getEventById(id);
        setEvent(response.data);
      } catch (err) {
        setError('Failed to load event details. Please try again later.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchEvent();
  }, [id]);

  // This function is passed down to the TicketPurchaseCard.
  // It prepares the data for the modal.
  const handlePurchaseInitiate = (ticket, quantity) => {
    setBookingDetails({
      eventName: event.name,
      ticket: ticket,
      quantity: quantity
    });
    setBookingError('');
    setIsModalOpen(true);
  };

  const confirmBooking = async () => {
    setIsBooking(true);
    setBookingError('');
    try {
      await bookingService.createBooking(
        bookingDetails.ticket.id,
        bookingDetails.quantity
      );
      alert(`Successfully purchased ${bookingDetails.quantity} ticket(s)! You will now be redirected to your bookings page.`);
      setIsModalOpen(false);
      navigate('/my-bookings');
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Your purchase could not be completed. Please try again.';
      setBookingError(errorMessage);
    } finally {
      setIsBooking(false);
    }
  };

  if (loading) {
    return (
      <div>
        <Header />
        <p className="centered-message">Loading Event...</p>
      </div>
    );
  }

  if (error || !event) {
    return (
      <div>
        <Header />
        <p className="centered-message error-message">{error || 'Event not found.'}</p>
      </div>
    );
  }

  return (
    <div>
      <Header />
      <main className="container event-details-page">
        <div className="event-details-layout">
          {/* Left Column */}
          <div className="event-info-column">
            <h1>{event.name}</h1>
            <p className="event-description">{event.description}</p>
            <div className="event-meta-info">
              <InfoDetail icon="calendar-icon.svg" text={dayjs(event.eventDateTime).format('dddd, MMMM D, YYYY [at] h:mm A')} />
              <InfoDetail icon="category-icon.svg" text={event.category.name} />
              <InfoDetail icon="location-icon.svg" text={`${event.address}, ${event.city.name}`} />
            </div>
          </div>
          <div className="event-purchase-column">
            <img 
                src={event.eventImageUrl || "/images/placeholder-image.avif"} 
                alt={event.name} 
                className="event-image"
            />
            <TicketPurchaseCard 
              ticketTypes={event.ticketTypes} 
              onPurchaseInitiate={handlePurchaseInitiate} 
            />
          </div>
        </div>
      </main>
      <PaymentModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onConfirm={confirmBooking}
        bookingDetails={bookingDetails}
        isLoading={isBooking}
        error={bookingError}
      />
    </div>
  );
}