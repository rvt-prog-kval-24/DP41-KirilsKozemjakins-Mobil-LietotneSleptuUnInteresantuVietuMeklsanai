// AddPlaceForm.jsx
import React, { useState, useEffect, useRef } from 'react';
import { addDoc, collection, getDocs, getDoc, doc } from 'firebase/firestore';
import { db, storage, auth } from '../DataBase/firebase';
import { getDownloadURL, ref, uploadBytesResumable } from 'firebase/storage';
import { LoadScript, GoogleMap, Marker, useGoogleMap } from '@react-google-maps/api';
import googleMapAPI from '../keys/keys.jsx';
import { Select, Form, Input, Button, Space } from 'antd';

const AddPlaceForm = ({ fields }) => {
  const [formData, setFormData] = useState({});
  const [address, setAddress] = useState('');
  const [latLng, setLatLng] = useState(null);
  const [photo, setPhoto] = useState(null);
  const [photoURL, setPhotoURL] = useState('');
  const [tags, setTags] = useState([]);
  const [allTags, setAllTags] = useState([]);
  const [existingPlaces, setExistingPlaces] = useState([]);
  const [isAddressEditable, setIsAddressEditable] = useState(false);
  const mapRef = useRef(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleTagChange = async (value) => {
    try {
      const tagDocRef = doc(db, 'Tags', value);
      const tagDocSnapshot = await getDoc(tagDocRef);
      if (tagDocSnapshot.exists()) {
        const tagName = tagDocSnapshot.data().name;
        console.log('Selected Tag Name:', tagName);
        setTags(tagName);
      } else {
        console.log('Tag not found');
      }
    } catch (error) {
      console.error('Error fetching tag name:', error);
    }
  };

  const handleAddressClick = () => {
    setIsAddressEditable(true);
  };

  const handleAddressChange = (e) => {
    setAddress(e.target.value);
  };

  const handlePhotoChange = (e) => {
    if (e.target.files[0]) {
      setPhoto(e.target.files[0]);
    }
  };

  const handleAddPlace = async () => {
    try {
      let posX = '';
      let posY = '';
      if (latLng) {
        posX = latLng.lat().toString();
        posY = latLng.lng().toString();
      }

      const user = auth.currentUser;
      const userID = user ? user.uid : '';

      let photoURL = '';
      if (photo) {
        const storageRef = ref(storage, `places/${photo.name}`);
        const uploadTask = uploadBytesResumable(storageRef, photo);
        await uploadTask;
        photoURL = await getDownloadURL(uploadTask.snapshot.ref);
      }

      await addDoc(collection(db, 'Places'), {
        ...formData,
        PosX: posX,
        PosY: posY,
        photoURL,
        userID,
        tags,
      });
      setFormData({});
      setAddress('');
      setLatLng(null);
      setPhoto(null);
      setPhotoURL('');
      setTags([]);
      setIsAddressEditable(false);
      fetchExistingPlaces(); // Refresh the places after adding a new one
    } catch (error) {
      console.error('Error adding place:', error);
    }
  };

  const convertAddressToLatLng = async () => {
    try {
      const response = await fetch(`https://maps.googleapis.com/maps/api/geocode/json?address=${address}&key=${googleMapAPI}`);
      const data = await response.json();
      const location = data.results[0].geometry.location;
      setLatLng(new window.google.maps.LatLng(location.lat, location.lng));
    } catch (error) {
      console.error('Error converting address to LatLng:', error);
    }
  };

  const handleMapClick = (event) => {
    setLatLng(event.latLng);
  };

  const fetchExistingPlaces = async () => {
    try {
      const placesCollection = collection(db, 'Places');
      const placesSnapshot = await getDocs(placesCollection);
      const places = placesSnapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      }));
      setExistingPlaces(places);
    } catch (error) {
      console.error('Error fetching existing places:', error);
    }
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
    
    useEffect(() => {
    const fetchTags = async () => {
    const tagsCollection = collection(db, 'Tags');
    const tagsSnapshot = await getDocs(tagsCollection);
    const fetchedTags = tagsSnapshot.docs.map((doc) => ({
    value: doc.id,
    label: doc.data().name,
    }));
    setAllTags(fetchedTags);
    };
  
    fetchTags();
    fetchExistingPlaces();
    }, []);
    
    return (
    <div>
    <h2>Add Place</h2>
    <Form layout="vertical">
    {fields.map((field) => (
    <Form.Item key={field} label={field}>
    <Input name={field} placeholder={`Enter ${field}`} value={formData[field] || ''} onChange={handleChange} />
    </Form.Item>
    ))}
        <Form.Item label="Tags">
      <Select
        mode="single"
        allowClear
        style={{ width: '100%' }}
        placeholder="Select tags"
        options={allTags}
        onChange={handleTagChange}
        value={tags}
      />
    </Form.Item>

    <Space direction="vertical">
      <Form.Item>
        <Input disabled={!isAddressEditable} value={address} onChange={handleAddressChange} placeholder="Enter Address" />
      </Form.Item>
      <Button type="primary" onClick={handleAddressClick} disabled={isAddressEditable}>
        Input Address
      </Button>
    </Space>

    <Form.Item label="Upload">
      <Input type="file" onChange={handlePhotoChange} />
    </Form.Item>

    <Button type="primary" onClick={handleAddPlace}>
      Add Place
    </Button>
  </Form>

  {latLng && (
    <div>
      <p>Selected Location:</p>
      <p>Latitude: {latLng.lat()}</p>
      <p>Longitude: {latLng.lng()}</p>
    </div>
  )}
  <div>
    <h3>Map</h3>
    <div id="map" style={{ height: '400px', width: '100%' }}>
      <LoadScript googleMapsApiKey={googleMapAPI}>
        <GoogleMap mapContainerStyle={{ height: '100%', width: '100%' }} options={mapOptions}>
          <MapComponent />
          {latLng && <Marker position={latLng} />}
          {existingPlaces.map((place) => (
            <Marker
              key={place.id}
              position={{ lat: parseFloat(place.PosX), lng: parseFloat(place.PosY) }}
              title={place.name}
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

export default AddPlaceForm;


