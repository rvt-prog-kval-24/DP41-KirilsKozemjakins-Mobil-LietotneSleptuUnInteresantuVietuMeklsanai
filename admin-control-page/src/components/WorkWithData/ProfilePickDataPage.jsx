//ProfilePickData.jsx

import React, { useState, useEffect } from 'react';
import { addDoc, collection, getDocs, doc, updateDoc, deleteDoc } from 'firebase/firestore';
import { db, storage } from '../DataBase/firebase';
import { getDownloadURL, ref, uploadBytesResumable } from 'firebase/storage';
import { Form, Input, Button, Table, Image } from 'antd';

const ProfilePicForm = () => {
  const [profilePics, setProfilePics] = useState([]); // Array to store profile pictures
  const [formData, setFormData] = useState({}); // State for form data
  const [photo, setPhoto] = useState(null); // State for uploaded photo
  const [photoURL, setPhotoURL] = useState(''); // State for photo URL
  const [isEditing, setIsEditing] = useState(false); // Flag for editing mode
  const [editingProfilePicId, setEditingProfilePicId] = useState(null); // ID of profile picture being edited

  const handleChange = (e) => {
    const { name, value } = e.target;
    // Convert Price to integer if it's a valid number
    if (name === 'Price') {
      setFormData({ ...formData, [name]: parseInt(value) || '' });
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const handlePhotoChange = (e) => {
    if (e.target.files[0]) {
      setPhoto(e.target.files[0]);
    }
  };

  const handleAddProfilePic = async () => {
    try {
      let photoURL = '';
      if (photo) {
        const storageRef = ref(storage, `profilePictures/${photo.name}`);
        const uploadTask = uploadBytesResumable(storageRef, photo);
        await uploadTask;
        photoURL = await getDownloadURL(uploadTask.snapshot.ref);
      }

      const picData = {
        Title: formData.Title,
        ProfPickURL: photoURL,
        Price: formData.Price || 0, // Ensure Price is an integer
      };

      if (isEditing) {
        await updateDoc(doc(db, 'ProfPictures', editingProfilePicId), picData);
      } else {
        await addDoc(collection(db, 'ProfPictures'), picData);
      }

      setFormData({});
      setPhoto(null);
      setPhotoURL('');
      setIsEditing(false);
      setEditingProfilePicId(null);
      fetchProfilePics();
    } catch (error) {
      console.error('Error adding profile picture:', error);
    }
  };

  const fetchProfilePics = async () => {
    const picsCollection = collection(db, 'ProfPictures');
    const picsSnapshot = await getDocs(picsCollection);
    const fetchedPics = picsSnapshot.docs.map((doc) => ({
      ...doc.data(),
      id: doc.id,
    }));
    setProfilePics(fetchedPics);
  };

  const editProfilePic = (picId) => {
    const picToEdit = profilePics.find((pic) => pic.id === picId);
    setFormData({ Title: picToEdit.Title, Price: picToEdit.Price });
    setPhotoURL(picToEdit.ProfPickURL);
    setIsEditing(true);
    setEditingProfilePicId(picId);
  };

  const deleteProfilePic = async (picId) => {
    await deleteDoc(doc(db, 'ProfPictures', picId));
    fetchProfilePics();
  };

  const columns = [
    {
      title: 'Profile Picture',
      dataIndex: 'ProfPickURL',
      key: 'ProfPickURL',
      render: (ProfPickURL) => (ProfPickURL && <Image width={50} src={ProfPickURL} alt="Profile Picture" />),
    },
    {
      title: 'Title',
      dataIndex: 'Title',
      key: 'Title',
    },
    {
      title: 'Price',
      dataIndex: 'Price',
      key: 'Price',
    },
    {
      title: 'Action',
      dataIndex: '',
      key: 'action',
      render: (pic) => (
        <div>
          <Button type="primary" onClick={() => editProfilePic(pic.id)}>
            Edit
          </Button>
          <Button type="danger" onClick={() => deleteProfilePic(pic.id)} style={{ marginLeft: '8px' }}>
            Delete
          </Button>
        </div>
      ),
    },
  ];

  useEffect(() => {
    fetchProfilePics();
  }, []); // Fetch profile pictures on component mount

  return (
    <div>
      <h2>Manage Profile Pictures</h2>
      <Table dataSource={profilePics} columns={columns} pagination={{ pageSize: 5 }} />
      <br />
      <h2>{isEditing ? 'Edit Profile Picture' : 'Add Profile Picture'}</h2>
      <Form layout="vertical">
        <Form.Item label="Title">
          <Input name="Title" placeholder="Enter Title" value={formData.Title || ''} onChange={handleChange} />
        </Form.Item>
        <Form.Item label="Price">
          <Input type="number" name="Price" placeholder="Enter Price" value={formData.Price || ''} onChange={handleChange} />
        </Form.Item>
        <Form.Item label="Upload Picture">
          <Input type="file" onChange={handlePhotoChange} />
          {photoURL && <Image src={photoURL} alt="Profile Picture Preview" style={{ width: '100px', height: 'auto', marginTop: '10px' }} />}
        </Form.Item>
        <Button type="primary" onClick={handleAddProfilePic}>
          {isEditing ? 'Update Profile Picture' : 'Add Profile Picture'}
        </Button>
      </Form>
    </div>
  );
};

export default ProfilePicForm;
