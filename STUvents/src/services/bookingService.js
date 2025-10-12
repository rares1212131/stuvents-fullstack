
import api from '../api/api';

export const createBooking = (ticketTypeId, quantity) => {
  return api.post('/bookings', { ticketTypeId, quantity });
};

export const getMyBookings = (page = 0, size = 10) => {
  return api.get(`/bookings/my-bookings?page=${page}&size=${size}`);
};