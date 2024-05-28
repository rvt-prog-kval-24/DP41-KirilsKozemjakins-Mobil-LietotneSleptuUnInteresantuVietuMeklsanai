// LandingPage.jsx

import React from 'react';
import { useState } from 'react';
import LoginForm from './auth/LoginForm';
import { message } from 'antd';
import { auth,db } from './DataBase/firebase'; // Import auth
import { signInWithEmailAndPassword } from 'firebase/auth';
import { collection, addDoc, serverTimestamp } from 'firebase/firestore'; // Import Firestore functions

const LandingPage = ({ setIsLoggedIn }) => {
  const [loading, setLoading] = useState(false);

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const userCredential = await signInWithEmailAndPassword(auth, values.email, values.password);

      const userDoc = {
        uid: userCredential.user.uid, // Store user ID
        email: userCredential.user.email, 
        sessionStart: serverTimestamp(),
      };

      const sessionsCollectionRef = collection(db, 'UserSessions');
      await addDoc(sessionsCollectionRef, userDoc);


      message.success('Logged in successfully');
      setIsLoggedIn(true);
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
