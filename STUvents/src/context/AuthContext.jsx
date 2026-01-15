/* eslint-disable react-refresh/only-export-components */

import React, { createContext, useState, useContext, useEffect, useMemo, useCallback } from 'react';
import api from '../api/api';
import { getMyProfile } from '../services/authService';

export const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(() => localStorage.getItem('authToken'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token){
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
      localStorage.setItem('authToken', token);
    } else {
      delete api.defaults.headers.common['Authorization'];
      localStorage.removeItem('authToken');
    }
  }, [token]);
  const fetchUserData = useCallback(async () => {
    if (token) {
      try {
        const response = await getMyProfile();
        setUser(response.data); 
        return response.data;
      } catch (error) {
        console.error("Failed to fetch user:", error);
        setToken(null);
        setUser(null);
        throw error;
      }
    }
    return null;
  }, [token]);

  useEffect(() => {
    const initializeAuth = async () => {
      if (token) {
        await fetchUserData();
      }
      setLoading(false);
    };
    initializeAuth();
  }, [token, fetchUserData]);
  const refreshUser = useCallback(async () => {
    if (token) {
      try {
        const response = await getMyProfile();
        setUser(response.data);
        return response.data;
      } catch (error) {
        console.error("Failed to refresh user:", error);
        throw error;
      }
    }
  }, [token]);

  const login = useCallback(async (jwtResponse) => {
    setToken(jwtResponse.token);
    api.defaults.headers.common['Authorization'] = `Bearer ${jwtResponse.token}`;
    
    try {
        const response = await getMyProfile();
        setUser(response.data);
        return response.data;
    } catch (error) {
        console.error("Failed to fetch user details after login:", error);
        setUser(null);
        setToken(null);
        throw error;
    }
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    setToken(null);
  }, []);

  const updateUser = useCallback((userData) => {
    setUser(userData);
  }, []);

  const value = useMemo(
    () => ({
      user,
      token,
      login,
      logout,
      refreshUser,
      updateUser,
      isAuthenticated: !!user,
    }),
    [user, token, login, logout, refreshUser, updateUser]
  );
  
  if (loading) {
    return <div>Loading Application...</div>;
  }
  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  return useContext(AuthContext);
};