// authContext.jsx
import React, { createContext, useState, useEffect } from 'react';
import { onAuthStateChanged } from 'firebase/auth'; // Import onAuthStateChanged
import { auth, db } from '../DataBase/firebase'; // Import auth and db

export const AuthContext = createContext(null);

const AuthProvider = ({ children }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false); // Initialize isLoggedIn to false

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      setIsLoggedIn(!!user); // Set isLoggedIn based on user object presence
    });

    return () => unsubscribe();
  }, []);

  return (
    <AuthContext.Provider value={{ isLoggedIn, setIsLoggedIn }}>
      {children}
    </AuthContext.Provider>
  );
};

export default AuthProvider;
