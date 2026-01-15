
import api from '../api/api';


export const getMyApplicationStatus = () => {
  return api.get('/organizer-applications/my-status');
};

export const applyToBeOrganizer = (reason) => {
  return api.post('/organizer-applications', { reason });
};

export const getMyEvents = () => {
  return api.get('/organizer/events/my-events');
};

export const deleteMyEvent = (eventId) => {
  return api.delete(`/organizer/events/${eventId}`);
};

export const getMyEventById = (eventId) => {
  return api.get(`/organizer/events/${eventId}`);
};

export const createMyEvent = (formData) => {
  return api.post('/organizer/events', formData);
};


export const updateMyEvent = (eventId, formData) => {
  return api.put(`/organizer/events/${eventId}`, formData);
};

export const getEventAttendees = (eventId) => {
  return api.get(`/organizer/events/${eventId}/bookings?size=100`);
};