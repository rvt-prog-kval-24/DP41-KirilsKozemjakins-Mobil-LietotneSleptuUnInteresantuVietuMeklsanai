// LandingPage.jsx

import React from 'react';
import { useState } from 'react';
import { message } from 'antd';
import { auth } from "./DataBase/firebase"; // Import auth from the correct path
import { signInWithEmailAndPassword } from "firebase/auth"; // Import signInWithEmailAndPassword
import LoginForm from './auth/LoginForm';

const LandingPage = () => {
  const [loading, setLoading] = useState(false);

  const onFinish = async (values) => {
    setLoading(true);
    try {
      const userCredential = await signInWithEmailAndPassword(auth, values.email, values.password);
      console.log('Success:', userCredential.user);
      message.success('Logged in successfully');
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