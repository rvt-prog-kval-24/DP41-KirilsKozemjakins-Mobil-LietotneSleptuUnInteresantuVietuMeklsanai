//withAuth.jsx

import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from './authContext'; // Import AuthContext

const withAuth = (WrappedComponent) => (props) => {
  const { isLoggedIn } = useContext(AuthContext); // Access isLoggedIn from context

  if (!isLoggedIn) {
    return <Navigate to="/login" replace />; // Redirect to login if not logged in
  }

  return <WrappedComponent {...props} />;
};

export default withAuth;

