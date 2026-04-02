import { createContext, useContext, useState, useEffect } from 'react';
import { getMeApi } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On every app load — verify session with backend
  useEffect(() => {
    getMeApi()
      .then((userData) => setUser(userData))
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  
  const firstName = user?.displayName?.split(' ')[0] || user?.name?.split(' ')[0] || 'User';
  const login = (tokenValue, userData) => {
    // tokenValue ignored (session-based), just store user in state
    setUser(userData);
  };

  const logout = () => {
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}