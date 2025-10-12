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
  }, [token]);//it updates the api's token so that it can make calls to restricted parts of the application

  //create a separate async function to get the current user data because it has to be used in many places, callBack because the function have to be saved and not created a new one at every render even if the user has not changed, because it will encounter an infinite loop.
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
  //use effect to fetch the user's data which has in the dependency array the token and the function created, every variable and function used has to be in the dependency array.

  //function to refresh the data of a user mostly used in data profile change, but with delay
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
   //auth context to avoid prop drilling
  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => {
  return useContext(AuthContext);
};