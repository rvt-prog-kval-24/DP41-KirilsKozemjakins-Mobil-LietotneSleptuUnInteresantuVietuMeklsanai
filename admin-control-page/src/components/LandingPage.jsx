// LandingPage.jsx

import React from 'react';
import { useState } from 'react';
import { message } from 'antd';
import { auth, db } from './DataBase/firebase'; // Import auth and db
import { signInWithEmailAndPassword } from 'firebase/auth'; // Import signInWithEmailAndPassword
import LoginForm from './auth/LoginForm';
import { collection, query, where, getDocs } from 'firebase/firestore'; // Import Firestore functions

const LandingPage = ({ setIsLoggedIn }) => {
  const [loading, setLoading] = useState(false);

  const onFinish = async (values) => {
    setLoading(true);
    try {
      // Check if the user's email exists in the "Admins" collection
      const adminsRef = collection(db, 'Admins');
      const adminQuery = query(adminsRef, where('email', '==', values.email));
      const adminSnapshot = await getDocs(adminQuery);
      if (!adminSnapshot.empty) {
        // If the user's email exists in the "Admins" collection, proceed with sign-in
        const userCredential = await signInWithEmailAndPassword(auth, values.email, values.password);
        console.log('Success:', userCredential.user);
        message.success('Logged in successfully');
        setIsLoggedIn(true); // Set isLoggedIn to true
      } else {
        // If the user's email does not exist in the "Admins" collection, show an error message
        message.error('You are not authorized to log in.');
      }
    } catch (error) {
      console.error('Failed:', error.message);
      message.error(error.message);
    } finally {
      setLoading(false);
    }
  };

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
      <LoginForm onFinish={onFinish} onFinishFailed={onFinishFailed} />
    </div>
  );
};

export default LandingPage;
