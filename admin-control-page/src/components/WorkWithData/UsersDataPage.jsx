import React, { useState, useEffect } from 'react';
import { collection, getDocs, doc, updateDoc, deleteDoc, getDoc } from 'firebase/firestore';
import { db } from '../DataBase/firebase';
import { Table, Button, Space, Modal, Form, Input, Image } from 'antd';

const UsersDataPage = () => {
  const [users, setUsers] = useState([]); // State to store user data
  const [isDeleteModalVisible, setIsDeleteModalVisible] = useState(false); // State for delete confirmation modal
  const [isEditing, setIsEditing] = useState(false); // Flag for editing mode
  const [editingUserId, setEditingUserId] = useState(null); // ID of user being edited
  const [deletingUserId, setDeletingUserId] = useState(null); // ID of user being deleted
  const [formData, setFormData] = useState({}); // State for form data

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    const usersCollection = collection(db, 'Users');
    const usersSnapshot = await getDocs(usersCollection);
    const usersData = await Promise.all(usersSnapshot.docs.map(async (doc) => {
      const userData = doc.data();
      const profilePictureUrl = await fetchProfilePictureUrl(userData.CurrentPickID);
      return {
        ...userData,
        id: doc.id,
        profilePictureUrl,
      };
    }));
    setUsers(usersData);
  };

  const fetchProfilePictureUrl = async (currentPickID) => {
    if (!currentPickID) return null;
    const profilePictureDoc = await getDoc(doc(db, 'ProfPictures', currentPickID));
    return profilePictureDoc.exists() ? profilePictureDoc.data().ProfPickURL : null;
  };

  const handleDeleteUser = async () => {
    await deleteDoc(doc(db, 'Users', deletingUserId));
    setIsDeleteModalVisible(false);
    setDeletingUserId(null);
    fetchUsers();
  };

  const handleEditUser = (userId) => {
    const user = users.find((u) => u.id === userId);
    setFormData({ ...user });
    setIsEditing(true);
    setEditingUserId(userId);
  };

  const handleUpdateUser = async () => {
    await updateDoc(doc(db, 'Users', editingUserId), formData);
    setIsEditing(false);
    setEditingUserId(null);
    setFormData({});
    fetchUsers();
  };

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: 'Email',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: 'Date of Creation',
      dataIndex: 'dateOfCreation',
      key: 'dateOfCreation',
    },
    {
      title: 'Current Picture',
      dataIndex: 'profilePictureUrl',
      key: 'profilePictureUrl',
      render: (url) => url ? <Image width={50} src={url} /> : 'No Picture',
    },
    {
      title: 'Action',
      dataIndex: '',
      key: 'action',
      render: (user) => (
        <Space direction="horizontal">
          <Button type="primary" onClick={() => handleEditUser(user.id)}>
            Edit
          </Button>
          <Button type="danger" onClick={() => {
            setDeletingUserId(user.id);
            setIsDeleteModalVisible(true);
          }}>
            Delete
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2>Users Data</h2>
      <Table dataSource={users} columns={columns} pagination={{ pageSize: 5 }} />
      <br />
      {isEditing && (
        <div>
          <h2>Edit User</h2>
          <Form layout="vertical">
            <Form.Item label="Name">
              <Input
                name="name"
                placeholder="Enter Name"
                value={formData.name || ''}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              />
            </Form.Item>
            <Form.Item label="Email">
              <Input
                name="email"
                placeholder="Enter Email"
                value={formData.email || ''}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </Form.Item>
            <Button type="primary" onClick={handleUpdateUser}>
              Update User
            </Button>
          </Form>
        </div>
      )}
      <Modal
        title="Confirm Delete"
        visible={isDeleteModalVisible}
        onOk={handleDeleteUser}
        onCancel={() => setIsDeleteModalVisible(false)}
      >
        <p>Are you sure you want to delete this user?</p>
      </Modal>
    </div>
  );
};

export default UsersDataPage;
