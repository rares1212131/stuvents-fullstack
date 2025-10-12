// In file: src/services/adminService.js (CORRECTED)

import api from '../api/api';


export const getAllUsers = () => {
  return api.get('/admin/users');
};


export const updateUserRoles = (userId, roles) => {
  return api.put(`/admin/users/${userId}/roles`, roles);
};

export const getPendingApplications = () => {
  return api.get('/admin/organizer-applications');
};

export const approveApplication = (applicationId) => {
  return api.post(`/admin/organizer-applications/${applicationId}/approve`);
};

export const denyApplication = (applicationId) => {
  return api.post(`/admin/organizer-applications/${applicationId}/deny`);
};

// --- Category Management ---

export const createCategory = (name) => {
  return api.post('/categories', { name });
};

export const updateCategory = (id, name) => {
  return api.put(`/categories/${id}`, { name });
};

export const deleteCategory = (id) => {
  return api.delete(`/categories/${id}`);
};

// --- City Management ---

export const createCity = (name) => {
  return api.post('/cities', { name });
};

export const updateCity = (id, name) => {
  return api.put(`/cities/${id}`, { name });
};

export const deleteCity = (id) => {
  return api.delete(`/cities/${id}`);
};