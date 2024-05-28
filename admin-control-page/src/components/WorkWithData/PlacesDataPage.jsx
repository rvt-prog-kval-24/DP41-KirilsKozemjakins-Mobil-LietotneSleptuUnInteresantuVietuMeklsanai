import React, { useState, useEffect, useRef } from 'react';
import { addDoc, collection, getDocs, doc, updateDoc, deleteDoc } from 'firebase/firestore';
import { db, storage } from '../DataBase/firebase';
import { getDownloadURL, ref, uploadBytesResumable } from 'firebase/storage';
import { LoadScript, GoogleMap, Marker, useGoogleMap } from '@react-google-maps/api';
import googleMapAPI from '../keys/keys.jsx';
import { Select, Form, Input, Button, Space, Table, Image } from 'antd';

const PlaceForm = () => {
  const [places, setPlaces] = useState([]); // Array to store places
  const [formData, setFormData] = useState({}); // State for form data
  const [photo, setPhoto] = useState(null); // State for uploaded photo
  const [photoURL, setPhotoURL] = useState(''); // State for photo URL
  const [isEditing, setIsEditing] = useState(false); // Flag for editing mode
  const [editingPlaceId, setEditingPlaceId] = useState(null); // ID of place being edited
  const [latLng, setLatLng] = useState(null); // State for selected LatLng
  const [tags, setTags] = useState([]); // Array to store tags
  const mapRef = useRef(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handlePhotoChange = (e) => {
    if (e.target.files[0]) {
      setPhoto(e.target.files[0]);
    }
  };

  const handleAddPlace = async () => {
    try {
      let photoURL = '';
      if (photo) {
        const storageRef = ref(storage, `places/${photo.name}`);
        const uploadTask = uploadBytesResumable(storageRef, photo);
        await uploadTask;
        photoURL = await getDownloadURL(uploadTask.snapshot.ref);
      }
  
      const placeData = {
        ...formData,
        photoURL,
        PosX: latLng ? latLng.lat().toString() : '',
        PosY: latLng ? latLng.lng().toString() : '',
        tags,
      };
  
      if (isEditing) {
        await updateDoc(doc(db, 'Places', editingPlaceId), placeData);
      } else {
        await addDoc(collection(db, 'Places'), placeData);
      }
      
      // Clear form data, photo, and photoURL after successfully adding place
      setFormData({});
      setPhoto(null);
      setPhotoURL('');
      
      setIsEditing(false);
      setEditingPlaceId(null);
      fetchPlaces();
    } catch (error) {
      console.error('Error adding place:', error);
    }
  };
  

  const fetchPlaces = async () => {
    const placesCollection = collection(db, 'Places');
    const placesSnapshot = await getDocs(placesCollection);
    const fetchedPlaces = placesSnapshot.docs.map((doc) => ({
      ...doc.data(),
      id: doc.id,
    }));
    setPlaces(fetchedPlaces);
  };

  const editPlace = (placeId) => {
    const place = places.find((p) => p.id === placeId);
    setFormData({ ...place });
    setPhotoURL(place.photoURL);
    setIsEditing(true);
    setEditingPlaceId(placeId);
    setTags(place.tags);
    // Convert PosX and PosY to google.maps.LatLng
    setLatLng(new window.google.maps.LatLng(parseFloat(place.PosX), parseFloat(place.PosY)));
  };

  const deletePlace = async (placeId) => {
    await deleteDoc(doc(db, 'Places', placeId));
    fetchPlaces();
  };

  const handleMapClick = (event) => {
    setLatLng(event.latLng);
  };

  const columns = [
    {
      title: 'Place Name',
      dataIndex: 'PlaceName',
      key: 'PlaceName',
    },
    {
      title: 'Description',
      dataIndex: 'Description',
      key: 'Description',
    },
    {
      title: 'Latitude',
      dataIndex: 'PosX',
      key: 'PosX',
    },
    {
      title: 'Longitude',
      dataIndex: 'PosY',
      key: 'PosY',
    },
    {
      title: 'Tags',
      dataIndex: 'tags',
      key: 'tags',
    },
    {
      title: 'Photo',
      dataIndex: 'photoURL',
      key: 'photoURL',
      render: (photoURL) => (photoURL && <Image width={50} src={photoURL} alt="Place Preview" />),
    },
    {
      title: 'Action',
      dataIndex: '',
      key: 'action',
      render: (place) => (
        <Space direction="horizontal">
          <Button type="primary" onClick={() => editPlace(place.id)}>
            Edit
          </Button>
          <Button type="danger" onClick={() => deletePlace(place.id)}>
            Delete
          </Button>
        </Space>
      ),
    },
  ];

  useEffect(() => {
    fetchPlaces();
    fetchTags(); // Fetch existing tags
  }, []);

  const fetchTags = async () => {
    const tagsCollection = collection(db, 'Tags');
    const tagsSnapshot = await getDocs(tagsCollection);
    const fetchedTags = tagsSnapshot.docs.map((doc) => ({
      id: doc.id,
      name: doc.data().name,
    }));
    setTags(fetchedTags);
  };

  const mapOptions = {
    center: latLng || { lat: 56.9496, lng: 24.1052 },
    zoom: 15,
  };

  const MapComponent = () => {
    const map = useGoogleMap();
    mapRef.current = map;
    window.google.maps.event.addListener(map, 'click', handleMapClick);
    return null;
  };

  return (
    <div>
      <h2>Manage Places</h2>
      <Table dataSource={places} columns={columns} pagination={{ pageSize: 5 }} />
      <br />
      <h2>{isEditing ? 'Edit Place' : 'Add Place'}</h2>
      <Form layout="vertical">
        <Form.Item label="Place Name">
          <Input name="PlaceName" placeholder="Enter Place Name" value={formData.PlaceName || ''} onChange={handleChange} />
        </Form.Item>
        <Form.Item label="Description">
          <Input name="Description" placeholder="Enter Place Description" value={formData.Description || ''} onChange={handleChange} />
        </Form.Item>
        <Form.Item label="Tags">
            <Select
                mode="single"
                placeholder="Select Tags"
                value={tags}
                onChange={setTags}
            >
                {Array.isArray(tags) && tags.map(tag => (
                <Select.Option key={tag.id} value={tag.name}>
                    {tag.name}
                </Select.Option>
                ))}
            </Select>
        </Form.Item>

        <Form.Item label="Upload Photo (optional)">
          <Input type="file" onChange={handlePhotoChange} />
          {photoURL && <img src={photoURL} alt="Place Preview" style={{ width: '100px', height: 'auto', marginTop: '10px' }} />}
        </Form.Item>
        <Button type="primary" onClick={handleAddPlace}>
          {isEditing ? 'Update Place' : 'Add Place'}
        </Button>
      </Form>
      <div>
        <h3>Map</h3>
        <div id="map" style={{ height: '400px', width: '100%' }}>
          <LoadScript googleMapsApiKey={googleMapAPI}>
            <GoogleMap mapContainerStyle={{ height: '100%', width: '100%' }} options={mapOptions}>
              <MapComponent />
              {latLng && <Marker position={latLng} />}
              {places.map((place) => (
                <Marker
                  key={place.id}
                  position={{ lat: parseFloat(place.PosX), lng: parseFloat(place.PosY) }}
                  title={place.PlaceName}
                />
              ))}
            </GoogleMap>
          </LoadScript>
        </div>
        <p>Click on the map to select location</p>
      </div>
    </div>
  );
};

export default PlaceForm;
