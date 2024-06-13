// AdminsPage.jsx

import React, { useState, useEffect } from 'react';
import { Select, Form, Input, Button, Space, Table, Popconfirm, message } from 'antd';
import { addDoc, collection, getDocs, doc, deleteDoc } from 'firebase/firestore';
import { db } from '../DataBase/firebase'; // Assuming you have a separate file for Firebase configuration

const AdminsPage = () => {
  const [users, setUsers] = useState([]); // Array to store users
  const [admins, setAdmins] = useState([]); // Array to store admins
  const [selectedUser, setSelectedUser] = useState(null); // Selected user for adding admin
  const [editingAdminId, setEditingAdminId] = useState(null); // ID of admin being edited

  const fetchUsers = async () => {
    const usersCollection = collection(db, 'Users'); // Assuming your users are stored in a 'Users' collection
    const usersSnapshot = await getDocs(usersCollection);
    const fetchedUsers = usersSnapshot.docs.map((doc) => ({
      ...doc.data(),
      id: doc.id,
    }));
    setUsers(fetchedUsers);
  };

  const fetchAdmins = async () => {
    const adminsCollection = collection(db, 'Admins'); // Assuming your admins are stored in an 'Admins' collection
    const adminsSnapshot = await getDocs(adminsCollection);
    const fetchedAdmins = adminsSnapshot.docs.map((doc) => ({
      ...doc.data(),
      id: doc.id,
    }));
    setAdmins(fetchedAdmins);
  };

  useEffect(() => {
    // Fetch users and admins on component mount
    fetchUsers();
    fetchAdmins();
  }, []);

  const handleUserSelect = (value) => {
    setSelectedUser(value);
  };

  const handleAddAdmin = async () => {
    if (!selectedUser) {
      return alert('Please select a user to add as admin');
    }

    try {
      await addDoc(collection(db, 'Admins'), { userId: selectedUser });
      setSelectedUser(null);
      fetchAdmins(); // Update admins list after adding
    } catch (error) {
      console.error('Error adding admin:', error);
    }
  };

  const handleEditAdmin = (adminId) => {
    setEditingAdminId(adminId);
  };

  const handleDeleteAdmin = async (adminId) => {
    try {
      await deleteDoc(doc(db, 'Admins', adminId));
      setEditingAdminId(null);
      fetchAdmins(); // Update admins list after deleting
      message.success('Admin deleted successfully');
    } catch (error) {
      console.error('Error deleting admin:', error);
      message.error('Failed to delete admin');
    }
  };

  const columns = [
    {
      title: 'Email',
      dataIndex: 'email', // Assuming you have an 'email' field in your Users collection
      key: 'email',
    },
    {
      title: 'Admin',
      dataIndex: 'id',
      key: 'id',
      render: (adminId) => (
        <Space direction="horizontal">
          {editingAdminId === adminId ? (
            <Button type="primary" size="small" disabled>
              Editing
            </Button>
          ) : (
            <>
              <Button type="link" onClick={() => handleEditAdmin(adminId)}>
                Edit
              </Button>
              <Popconfirm
                title="Are you sure you want to delete this admin?"
                onConfirm={() => handleDeleteAdmin(adminId)}
                okText="Yes"
                cancelText="No"
              >
                <Button type="link" danger>
                  Delete
                </Button>
              </Popconfirm>
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2>Manage Admins</h2>
      <Table dataSource={admins} columns={columns} pagination={{ pageSize: 5 }} />
      <br />

      <h2>Add Admin</h2>
      <Form layout="vertical">
        <Form.Item label="Select User">
          <Select
            showSearch
            placeholder="Select a user"
            optionFilterProp="children"
            onChange={handleUserSelect}
            filterOption={(input, option) =>
              option.children.toLowerCase().indexOf(input.toLowerCase()) >= 0
            }
          >
            {users.map((user) => (
              <Select.Option key={user.id} value={user.id}>
                {user.email}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Button type="primary" onClick={handleAddAdmin} disabled={!selectedUser}>
          Add Admin
        </Button>
      </Form>
    </div>
  );
};

export default AdminsPage;
