
import api from '../api/api';

export const getMyProfile = () => {

  return api.get('/auth/me');
};