// In file: src/services/authService.js

import api from '../api/api';

export const getMyProfile = () => {
  // We use the specific endpoint from your AuthContext
  return api.get('/auth/me');
};