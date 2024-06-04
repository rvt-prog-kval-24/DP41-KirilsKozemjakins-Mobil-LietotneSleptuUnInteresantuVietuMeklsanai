// App.jsx
import React, { useState, useEffect } from 'react';
import './App.css';
import { Button, Breadcrumb, Layout, Menu } from 'antd';
import { EnvironmentOutlined } from '@ant-design/icons';
import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import { onAuthStateChanged, signOut, getAuth } from 'firebase/auth';
import { collection, query, where, getDocs, updateDoc, serverTimestamp } from 'firebase/firestore'; // Moved import here

import { auth, db } from './components/DataBase/firebase'; // Import auth and db
import LandingPage from './components/LandingPage.jsx';
import MainPage from './components/MainPage.jsx';

import PlacesDataPage from './components/WorkWithData/PlacesDataPage.jsx';
import SuggestionsDataPage from './components/WorkWithData/SuggestionsDataPage.jsx';
import AchievementsDataPage from './components/WorkWithData/AchievementsDataPage.jsx'
import CommentsDataPage from './components/WorkWithData/CommentsDataPage.jsx'
import ProfilePickDataPage from './components/WorkWithData/ProfilePickDataPage.jsx';

import UsersDataPage from './components/WorkWithData/UsersDataPage.jsx';
import AdminsDataPage from './components/WorkWithData/AdminDataPage.jsx';


import withAuth from './components/auth/withAuth'; // Import withAuth HOC
import { clearSession } from './components/auth/session'; // Import session utility

const { Header, Content, Footer } = Layout;

function Navbar({ isLoggedIn }) {
  return (
    <Menu theme="dark" mode="horizontal"  style={{ flex: 1, minWidth: 0 }}>
      <Menu.Item key="1"><Link to="/">Landing Page</Link></Menu.Item>
      {isLoggedIn && (
        <>
          <Menu.Item key="2"><Link to="/main_page">Main Page</Link></Menu.Item>
          <Menu.Item key="3"><Link to="/places_data">Places</Link></Menu.Item>
          <Menu.Item key="4"><Link to="/suggestions_data">Suggestions</Link></Menu.Item>
          <Menu.Item key="5"><Link to="/achievements_data">Achievements</Link></Menu.Item>
          <Menu.Item key="6"><Link to="/comments_data">Users comments</Link></Menu.Item>
          <Menu.Item key="7"><Link to="/prof_pick_data">Profile Pictures</Link></Menu.Item>
          <Menu.Item key="8"><Link to="/users_data">Users</Link></Menu.Item>
          <Menu.Item key="9"><Link to="/admin_data">Admins</Link></Menu.Item>
        </>
      )}
    </Menu>
  );
}

function Logo() {
  return (
    <Link to="/"><EnvironmentOutlined />UndergroundRiga</Link>
  );
}

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      if (user) {
        setIsLoggedIn(true);
      } else {
        setIsLoggedIn(false);
      }
    });

    return () => unsubscribe();
  }, []);

  const handleLogout = async () => {
    try {
      // Sign out from Firebase Auth
      const user = getAuth().currentUser; // Get current user reference (assuming user object is available)

      if (user) {
        const sessionEndTime = serverTimestamp();

        // Update the existing UserSession document with end time
        const userSessionsCollectionRef = collection(db, 'UserSessions');
        const snapshot = await getDocs(query(userSessionsCollectionRef, where('uid', '==', user.uid)));
        
      

        if (snapshot.empty) {
          console.error('User session document not found');
        } else {
          const doc = snapshot.docs[0];
          await updateDoc(doc.ref, { sessionEnd: sessionEndTime });
          console.log('User session document updated');
        }
      } else {
        console.error('No user logged in');
      }

      await signOut(auth);

      clearSession();

      setIsLoggedIn(false);
    } catch (error) {
      console.error('Error logging out:', error);
    }
  };


  return (
    <BrowserRouter>
      <Layout style={{ minHeight: '100vh' }}>
        <Header style={{ position: 'fixed', zIndex: 1, width: '100%', display: 'flex', alignItems: 'center' }}>
          <div style={{ color: 'white' }} className="demo-logo">
            <Logo />
          </div>
          <Navbar isLoggedIn={isLoggedIn} />
          {isLoggedIn && <Button onClick={handleLogout}>Logout</Button>}
        </Header>
        <Content style={{ padding: '0 48px', marginTop: 64 }}>
          <Breadcrumb style={{ margin: '16px 0' }}></Breadcrumb>
          <Routes>
            <Route path="/" element={<LandingPage setIsLoggedIn={setIsLoggedIn} />} />
            <Route path="/main_page"  element={ isLoggedIn ? <MainPage /> : <Navigate to="/" replace />}/>
            <Route path="/places_data" element={isLoggedIn ? <PlacesDataPage /> : <Navigate to="/" replace />}/>
            <Route path="/suggestions_data" element={isLoggedIn ? <SuggestionsDataPage />: <Navigate to="/" replace />}/>
            <Route path="/comments_data" element={isLoggedIn ? <CommentsDataPage />: <Navigate to="/" replace />}/>
            <Route path="/achievements_data" element={isLoggedIn ? <AchievementsDataPage />: <Navigate to="/" replace />}/>
            <Route path="/prof_pick_data" element={isLoggedIn ? <ProfilePickDataPage />: <Navigate to="/" replace />}/>
            <Route path="/users_data" element={isLoggedIn ? <UsersDataPage />: <Navigate to="/" replace />}/>
            <Route path="/admin_data" element={isLoggedIn ? <AdminsDataPage />: <Navigate to="/" replace />}/>


            
          </Routes>
        </Content>
        <Footer style={{ textAlign: 'center' }}>UndergroundRiga ©{new Date().getFullYear()} Created by KirilsK</Footer>
      </Layout>
    </BrowserRouter>
  );
}

export default App;
