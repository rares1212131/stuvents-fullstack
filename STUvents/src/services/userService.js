import api from '../api/api';

export const updateUserProfile = (userId, formData) => {
  return api.put(`/users/${userId}`, formData);
};