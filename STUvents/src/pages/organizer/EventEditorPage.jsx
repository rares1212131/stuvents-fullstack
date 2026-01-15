

import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Header } from '../../components/layout/Header';
import * as organizerService from '../../services/organizerService';
import * as eventService from '../../services/eventService'; 
import './EventEditorPage.css';

const EVENT_PLACEHOLDER_IMAGE = "/images/placeholder-image.avif";

export function EventEditorPage() {
  const { id } = useParams();
  const isEditMode = Boolean(id);
  const navigate = useNavigate();

  const [eventData, setEventData] = useState({
    name: '',
    description: '',
    eventDateTime: '',
    address: '',
    categoryId: '',
    cityId: '',
    eventImageUrl: null
  });

  const [ticketTypes, setTicketTypes] = useState([
    { name: '', price: '', totalAvailable: '' }
  ]);
  const [selectedFile, setSelectedFile] = useState(null);
  const [previewUrl, setPreviewUrl] = useState('');
  const [categories, setCategories] = useState([]);
  const [cities, setCities] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  useEffect(() => {
    const fetchDropdownData = async () => {
      try {
        const [catsRes, citiesRes] = await Promise.all([
          eventService.getAllCategories(),
          eventService.getAllCities()
        ]);
        setCategories(catsRes.data);
        setCities(citiesRes.data);
      } catch (err) {
        setError('Failed to load required data. Please refresh the page.');
        console.error(err);
      }
    };
    
    const fetchEventData = async () => {
      if (!isEditMode) return;
      try {
        const response = await organizerService.getMyEventById(id);
        const event = response.data;
        const formattedDate = event.eventDateTime ? event.eventDateTime.slice(0, 16) : '';
        
        setEventData({
          name: event.name,
          description: event.description,
          eventDateTime: formattedDate,
          address: event.address,
          categoryId: event.category.id,
          cityId: event.city.id,
          eventImageUrl: event.eventImageUrl
        });

        if (event.ticketTypes && event.ticketTypes.length > 0) {
            setTicketTypes(event.ticketTypes.map(tt => ({
                id: tt.id,
                name: tt.name,
                price: tt.price,
                totalAvailable: tt.totalAvailable
            })));
        } else {
            setTicketTypes([{ id: null, name: '', price: '', totalAvailable: '' }]);
        }
        
      } catch (err) {
        setError('Failed to load event data for editing.');
        console.error(err);
        navigate('/organizer/events');
      }
    };

    fetchDropdownData();
    fetchEventData();
  }, [id, isEditMode, navigate]);

  const handleEventChange = (e) => {
    setEventData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleTicketTypeChange = (index, e) => {
    const updatedTicketTypes = [...ticketTypes];
    updatedTicketTypes[index][e.target.name] = e.target.value;
    setTicketTypes(updatedTicketTypes);
  };

  const addTicketType = () => {
    const lastTicket = ticketTypes[ticketTypes.length - 1];
    if (!lastTicket.name || !lastTicket.price || !lastTicket.totalAvailable) {
      alert("Please complete the current ticket type before adding a new one.");
      return;
    }
    setTicketTypes([...ticketTypes, { id: null, name: '', price: '', totalAvailable: '' }]);
  };

  const removeTicketType = (index) => {
    if (ticketTypes.length <= 1) {
        alert("An event must have at least one ticket type.");
        return;
    }
    setTicketTypes(ticketTypes.filter((_, i) => i !== index));
  };
  
  const handleFileChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setSelectedFile(file);
      setPreviewUrl(URL.createObjectURL(file));
    }
  };
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    const formData = new FormData();

    const eventPayload = {
        ...eventData,
        ticketTypes: ticketTypes.map(tt => ({
            ...tt,
            price: parseFloat(tt.price),
            totalAvailable: parseInt(tt.totalAvailable, 10)
        }))
    };
    
    formData.append('event', new Blob([JSON.stringify(eventPayload)], { type: 'application/json' }));

    if (selectedFile) {
        formData.append('image', selectedFile);
    }

    try {
      if (isEditMode) {
        await organizerService.updateMyEvent(id, formData);
      } else {
        await organizerService.createMyEvent(formData);
      }
      alert(`Event ${isEditMode ? 'updated' : 'created'} successfully!`);
      navigate('/organizer/events');
    } catch (err) {
      setError(err.response?.data?.message || `Failed to ${isEditMode ? 'update' : 'create'} event.`);
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Header />
      <div className="event-editor-page">
        <div className="editor-header">
          <h1>{isEditMode ? 'Edit Event' : 'Create a New Event'}</h1>
          <p>{isEditMode ? 'Update the details for your event below.' : 'Fill in the details to launch your new event.'}</p>
        </div>

        <form onSubmit={handleSubmit} className="event-form">
          
          <div className="form-section">
            <h2>Event Image</h2>
            <div className="form-group">
                <img 
                    src={previewUrl || eventData.eventImageUrl || EVENT_PLACEHOLDER_IMAGE}
                    alt="Event Preview"
                    className="event-image-preview" 
                />
                <label htmlFor="image-upload" className="sr-only">Upload Event Image</label>
                <input 
                    id="image-upload"
                    type="file" 
                    name="image"
                    accept="image/png, image/jpeg, image/webp, image/avif"
                    onChange={handleFileChange}
                />
            </div>
          </div>

          <div className="form-section">
            <h2>Event Details</h2>
            <div className="form-grid">
              <div className="form-group full-width">
                <label htmlFor="name">Event Name</label>
                <input type="text" id="name" name="name" value={eventData.name} onChange={handleEventChange} required />
              </div>
              <div className="form-group full-width">
                <label htmlFor="description">Description</label>
                <textarea id="description" name="description" value={eventData.description} onChange={handleEventChange} required />
              </div>
              <div className="form-group">
                <label htmlFor="eventDateTime">Date and Time</label>
                <input type="datetime-local" id="eventDateTime" name="eventDateTime" value={eventData.eventDateTime} onChange={handleEventChange} required />
              </div>
              <div className="form-group">
                <label htmlFor="address">Address</label>
                <input type="text" id="address" name="address" value={eventData.address} onChange={handleEventChange} required />
              </div>
              <div className="form-group">
                <label htmlFor="categoryId">Category</label>
                <select id="categoryId" name="categoryId" value={eventData.categoryId} onChange={handleEventChange} required>
                  <option value="">Select a Category</option>
                  {categories.map(cat => <option key={cat.id} value={cat.id}>{cat.name}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label htmlFor="cityId">City</label>
                <select id="cityId" name="cityId" value={eventData.cityId} onChange={handleEventChange} required>
                  <option value="">Select a City</option>
                  {cities.map(city => <option key={city.id} value={city.id}>{city.name}</option>)}
                </select>
              </div>
            </div>
          </div>

          <div className="form-section">
            <h2>Ticket Types</h2>
            {ticketTypes.map((ticket, index) => (
              <div key={ticket.id || index} className="ticket-type-row">
                <div className="form-group">
                  <label>Ticket Name</label>
                  <input type="text" name="name" value={ticket.name} onChange={(e) => handleTicketTypeChange(index, e)} required />
                </div>
                <div className="form-group">
                  <label>Price ($)</label>
                  <input type="number" name="price" value={ticket.price} onChange={(e) => handleTicketTypeChange(index, e)} required min="0" step="0.01" />
                </div>
                <div className="form-group">
                  <label>Quantity</label>
                  <input type="number" name="totalAvailable" value={ticket.totalAvailable} onChange={(e) => handleTicketTypeChange(index, e)} required min="1" />
                </div>
                <button 
                  type="button" 
                  onClick={() => removeTicketType(index)} 
                  className="button-secondary button-danger" 
                  disabled={ticketTypes.length <= 1}
                >
                  Remove
                </button>
              </div>
            ))}
            <div className="ticket-type-actions">
              <button type="button" onClick={addTicketType} className="button-secondary dark-bg">
                Add Ticket Type
              </button>
            </div>
          </div>
          
          {error && <p className="error-message">{error}</p>}

          <div className="form-actions">
            <button type="submit" className="button-primary" disabled={loading}>
              {loading ? 'Saving...' : (isEditMode ? 'Save Changes' : 'Create Event')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}