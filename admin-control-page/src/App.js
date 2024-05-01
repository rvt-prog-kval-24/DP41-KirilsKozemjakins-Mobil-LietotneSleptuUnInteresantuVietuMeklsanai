// App.jsx
import React, { useState, useEffect } from 'react';
import './App.css';
import { Breadcrumb, Layout, Menu } from 'antd';
import { EnvironmentOutlined } from '@ant-design/icons';
import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import { onAuthStateChanged } from 'firebase/auth';
import { auth } from './components/DataBase/firebase'; // Import auth
import LandingPage from './components/LandingPage.jsx';
import MainPage from './components/MainPage.jsx';
import PlacesDataPage from './components/WorkWithData/PlacesDataPage.jsx';
import SuggestionsDataPage from './components/WorkWithData/SuggestionsDataPage.jsx';
import UsersDataPage from './components/WorkWithData/UsersDataPage.jsx';
import AdminsDataPage from './components/WorkWithData/AdminDataPage.jsx'; // Correct import path

import TestLogin from './components/TestLogin.jsx'; // Correct import path
import { AuthProvider, useAuth } from './components/auth/authContext.jsx'; // Add imports

const { Header, Content, Footer } = Layout;

function Navbar({ isLoggedIn }) {
  return (
    <Menu theme="dark" mode="horizontal" defaultSelectedKeys={['1']} style={{ flex: 1, minWidth: 0 }}>
      <Menu.Item key="1"><Link to="/">Landing Page</Link></Menu.Item>
      {isLoggedIn && (
        <>
          <Menu.Item key="2"><Link to="/main_page">Main Page</Link></Menu.Item>
          <Menu.Item key="3"><Link to="/places_data">Places</Link></Menu.Item>
          <Menu.Item key="4"><Link to="/suggestions_data">Suggestions</Link></Menu.Item>
          <Menu.Item key="5"><Link to="/users_data">Users</Link></Menu.Item>
          <Menu.Item key="6"><Link to="/admin_data">Admins</Link></Menu.Item>
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

  return (
    <BrowserRouter>
      <Layout style={{ minHeight: '100vh' }}>
        <Header
          style={{
            position: 'fixed',
            zIndex: 1,
            width: '100%',
            display: 'flex',
            alignItems: 'center',
          }}
        >
          <div style={{ color: 'white' }} className="demo-logo">
            <Logo />
          </div>
          <Navbar isLoggedIn={isLoggedIn} />
        </Header>
        <Content style={{ padding: '0 48px', marginTop: 64 }}>
          <Breadcrumb style={{ margin: '16px 0' }}></Breadcrumb>
          <AuthProvider> {/* Wrap with AuthProvider */}
            <Routes>
              <>
                <Route path="/" element={<LandingPage setIsLoggedIn={setIsLoggedIn} />} />
                <Route path="/main_page" element={<MainPage />} />
                <Route path="/places_data" element={<PlacesDataPage />} />
                <Route path="/suggestions_data" element={<SuggestionsDataPage />} />
                <Route path="/users_data" element={<UsersDataPage />} />
                <Route path="/admin_data" element={<AdminsDataPage />} />
                <Route path="/test_log" element={<TestLogin />} />
              </>
            </Routes>
          </AuthProvider> {/* Wrap with AuthProvider */}
        </Content>
        <Footer style={{ textAlign: 'center' }}>
          UndergroundRiga ©{new Date().getFullYear()} Created by KirilsK
        </Footer>
      </Layout>
    </BrowserRouter>
  );
}

export default App;
