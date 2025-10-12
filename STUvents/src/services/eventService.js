// In file: src/services/eventService.js

import api from '../api/api';

export const getInitialEvents = () => {
  return api.get('/events?page=0&size=9&sort=eventDateTime');
};

export const getEventsByCity = (cityName) => {
  return api.get(`/events?page=0&size=100&sort=eventDateTime&cityName=${cityName}`);
};


export const getAllCategories = () => {
  return api.get('/categories');
};

export const getAllCities = () => {
  return api.get('/cities');
};

export const getEventById = (eventId) => {
  return api.get(`/events/${eventId}`);
};