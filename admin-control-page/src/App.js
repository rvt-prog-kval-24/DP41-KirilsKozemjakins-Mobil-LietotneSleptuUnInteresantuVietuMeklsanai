import React, { } from 'react';
import './App.css';
import { Breadcrumb, Layout, Menu } from 'antd';
import { EnvironmentOutlined } from '@ant-design/icons';
  
import {BrowserRouter, Routes, Route, Link} from 'react-router-dom';

import LandingPage from "./components/LandingPage.jsx"
import MainPage from "./components/MainPage.jsx"
import PlacesDataPage from "./components/WorkWithData/PlacesDataPage.jsx"
import SuggestionsDataPage from "./components/WorkWithData/SuggestionsDataPage.jsx"
import UsersDataPage from "./components/WorkWithData/UsersDataPage.jsx"
import AdminsDataPage from "./components/WorkWithData/UsersDataPage.jsx"



const { Header, Content, Footer } = Layout;

function Navbar() {
  return (
    <Menu theme="dark" mode="horizontal" defaultSelectedKeys={['1']} style={{ flex: 1, minWidth: 0 }}>
      <Menu.Item key="1"><Link to="/">Landing Page</Link></Menu.Item>
      <Menu.Item key="2"><Link to="/main">Main Page</Link></Menu.Item>
      <Menu.Item key="3"><Link to="/places_data">Places</Link></Menu.Item>
      <Menu.Item key="4"><Link to="/suggestions_data">Suggestions</Link></Menu.Item>
      <Menu.Item key="5"><Link to="/users_data">Users</Link></Menu.Item>
      <Menu.Item key="5"><Link to="/admin_data">Admins</Link></Menu.Item>
    </Menu>
  );
}

  
function App() {
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
          <EnvironmentOutlined />UndergroundRiga
          </div>
          <Navbar />
        </Header>
        <Content style={{ padding: '0 48px', marginTop: 64 }}>
          <Breadcrumb style={{ margin: '16px 0' }}>
           
          </Breadcrumb>
          <Routes>

            <Route path="/" element={<LandingPage />} />
            <Route path="/main" element={<MainPage />} />
            <Route path="/places_data" element={<PlacesDataPage />} />
            <Route path="/suggestions_data" element={<SuggestionsDataPage />} />
            <Route path="/users_data" element={<UsersDataPage />} />
            <Route path="/admins_data" element={<AdminsDataPage />} />

            </Routes>
        </Content>
        <Footer style={{ textAlign: 'center' }}>
          UndergroundRiga ©{new Date().getFullYear()} Created by KirilsK
        </Footer>
      </Layout>
    </BrowserRouter>
  );
}
   
export default App;