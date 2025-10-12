import api from '../api/api';

export const updateUserProfile = (userId, formData) => {
  // The endpoint is from the ProfilePage's handleSubmit function
  return api.put(`/users/${userId}`, formData);
};