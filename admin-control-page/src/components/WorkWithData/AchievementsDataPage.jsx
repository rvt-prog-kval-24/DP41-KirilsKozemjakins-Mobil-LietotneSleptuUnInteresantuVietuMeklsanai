import React, { useState, useEffect, useRef } from 'react';
import { addDoc, collection, getDocs, doc, updateDoc, deleteDoc } from 'firebase/firestore';
import { db, storage, auth } from '../DataBase/firebase';
import { getDownloadURL, ref, uploadBytesResumable } from 'firebase/storage';
import { Select, Form, Input, Button, Space, Table, Image } from 'antd';

const AchievementForm = () => {
  const [achievements, setAchievements] = useState([]); // Array to store achievements
  const [formData, setFormData] = useState({}); // State for form data
  const [photo, setPhoto] = useState(null); // State for uploaded photo
  const [photoURL, setPhotoURL] = useState(''); // State for photo URL
  const [isEditing, setIsEditing] = useState(false); // Flag for editing mode
  const [editingAchievementId, setEditingAchievementId] = useState(null); // ID of achievement being edited
  const mapRef = useRef(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    // Handle integer input for Reward field
    if (name === 'Reward') {
      setFormData({ ...formData, [name]: parseInt(value) || 0 }); // Parse to integer or set to 0
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const handlePhotoChange = (e) => {
    if (e.target.files[0]) {
      setPhoto(e.target.files[0]);
    }
  };

  const handleAddAchievement = async () => {
    try {
      let photoURL = '';
      if (photo) {
        const storageRef = ref(storage, `achievements/${photo.name}`);
        const uploadTask = uploadBytesResumable(storageRef, photo);
        await uploadTask;
        photoURL = await getDownloadURL(uploadTask.snapshot.ref);
      } else if (isEditing && formData.photoURL) { // Check if editing and existing photoURL
        photoURL = formData.photoURL; // Use existing photoURL from formData
      }

      const achievementData = {
        ...formData,
        photoURL,
      };

      if (isEditing) {
        await updateDoc(doc(db, 'Achievements', editingAchievementId), achievementData);
      } else {
        await addDoc(collection(db, 'Achievements'), achievementData);
      }
      setFormData({});
      setPhoto(null);
      setPhotoURL('');
      setIsEditing(false);
      setEditingAchievementId(null);
      fetchAchievements();
    } catch (error) {
      console.error('Error adding achievement:', error);
    }
  };

  const fetchAchievements = async () => {
    const achievementsCollection = collection(db, 'Achievements');
    const achievementsSnapshot = await getDocs(achievementsCollection);
    const fetchedAchievements = achievementsSnapshot.docs.map((doc) => ({
      ...doc.data(),
      id: doc.id,
    }));
    setAchievements(fetchedAchievements);
  };

  const editAchievement = (achievementId) => {
    setFormData({ ...achievements.find((a) => a.id === achievementId) });
    setPhotoURL(achievements.find((a) => a.id === achievementId).photoURL);
    setIsEditing(true);
    setEditingAchievementId(achievementId);
  };

  const deleteAchievement = async (achievementId) => {
    await deleteDoc(doc(db, 'Achievements', achievementId));
    fetchAchievements();
  };

  const columns = [
    {
      title: 'Title',
      dataIndex: 'Title',
      key: 'Title',
    },
    {
      title: 'Description',
      dataIndex: 'Description',
      key: 'Description',
    },
    {
      title: 'Required Value',
      dataIndex: 'RequiredValue',
      key: 'RequiredValue',
    },
    {
      title: 'Condition Variable',
      dataIndex: 'ConditionVariable',
      key: 'ConditionVariable',
    },
    {
      title: 'Reward',
      dataIndex: 'Reward',
      key: 'Reward',
    },
    {
      title: 'Photo',
      dataIndex: 'photoURL',
      key: 'photoURL',
      render: (photoURL) => (photoURL && <Image width={50} src={photoURL} alt="Achievement Preview" />), // Display image in table cell
    },
    {
      title: 'Action',
      dataIndex: '',
      key: 'action',
      render: (achievement) => (
        <Space direction="horizontal">
          <Button type="primary" onClick={() => editAchievement(achievement.id)}>
            Edit
          </Button>
          <Button type="danger" onClick={() => deleteAchievement(achievement.id)}>
            Delete
          </Button>
        </Space>
      ),
    },
  ];

  useEffect(() => {
    fetchAchievements();
  }, []); // Fetch achievements on component mount

  return (
    <div>
      <h2>Manage Achievements</h2>
      <Table dataSource={achievements} columns={columns} pagination={{ pageSize: 5 }} />
      <br />
      <h2>{isEditing ? 'Edit Achievement' : 'Add Achievement'}</h2>
      <Form layout="vertical">
        <Form.Item label="Title">
          <Input name="Title" placeholder="Enter Achievement Title" value={formData.Title || ''} onChange={handleChange} />
        </Form.Item>
        <Form.Item label="Description">
          <Input name="Description" placeholder="Enter Achievement Description" value={formData.Description || ''} onChange={handleChange} />
        </Form.Item>
        <Form.Item label="Condition Variable">
          <Input name="ConditionVariable" placeholder="Enter Condition Variable" value={formData.ConditionVariable || ''} onChange={handleChange} />
        </Form.Item>
        <Form.Item label="Required Value">
          <Input type="number" placeholder="Enter Required Value" value={formData.RequiredValue || 0} onChange={handleChange} />
        </Form.Item>
        <Form.Item label="Reward">
          <Input type="number" name="Reward" placeholder="Enter Reward Points" value={formData.Reward || 0} onChange={handleChange} />
        </Form.Item>
        <Form.Item label="Upload Photo (optional)">
          <Input type="file" onChange={handlePhotoChange} />
          {photoURL && <img src={photoURL} alt="Achievement Preview" style={{ width: '100px', height: 'auto', marginTop: '10px' }} />}
        </Form.Item>
        <Button type="primary" onClick={handleAddAchievement}>
          {isEditing ? 'Update Achievement' : 'Add Achievement'}
        </Button>
      </Form>
    </div>
  );
};

export default AchievementForm;
