// src/components/EventCard.jsx
import { Link } from 'react-router-dom';
import dayjs from 'dayjs';
import './EventCard.css';

const CARD_PLACEHOLDER_IMAGE = "/images/placeholder-image.avif";
export function EventCard({ event, onMouseEnter, onMouseLeave }) {
  return (
    <Link 
      to={`/events/${event.id}`} 
      className="event-card-v2"
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
    >
     <img
        src={event.eventImageUrl || CARD_PLACEHOLDER_IMAGE}
        alt={event.name}
        className="event-card-image"
      />
      <div className="event-card-content">
        <h3 className="event-card-title">{event.name.toUpperCase()}</h3>

        <div className="event-card-info">
          {/* Info Item 1: Category */}
          <div className="info-item">
            <img src="/icons/category-icon.svg" alt="Category" className="info-icon" />
            <span className="info-text">{event.category.name.toUpperCase()}</span>
          </div>

          {/* Info Item 2: City */}
          <div className="info-item">
            <img src="/icons/location-icon.svg" alt="Location" className="info-icon" />
            <span className="info-text">{event.city.name.toUpperCase()}</span>
          </div>

          {/* Info Item 3: Date */}
          <div className="info-item">
            <img src="/icons/calendar-icon.svg" alt="Date" className="info-icon" />
            <span className="info-text">{dayjs(event.eventDateTime).format('MMMM DD').toUpperCase()}</span>
          </div>
        </div>
      </div>
    </Link>
  );
}