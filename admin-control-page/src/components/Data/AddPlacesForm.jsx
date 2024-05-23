import React, { useState, useRef } from 'react';
import { addDoc, collection } from 'firebase/firestore';
import { db } from '../DataBase/firebase';
import { GoogleMap, Marker, useGoogleMap } from '@react-google-maps/api';

const AddPlaceForm = ({ fields }) => {
    const [formData, setFormData] = useState({});
    const [address, setAddress] = useState('');
    const [latLng, setLatLng] = useState(null);
    const mapRef = useRef(null);

    const handleChange = e => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleAddressChange = e => {
        setAddress(e.target.value);
    };

    const handleAddPlace = async () => {
        try {
            let posX = '';
            let posY = '';
            if (latLng) {
                posX = latLng.lat().toString();
                posY = latLng.lng().toString();
            }
            await addDoc(collection(db, 'Places'), { ...formData, PosX: posX, PosY: posY });
            setFormData({});
            setAddress('');
            setLatLng(null);
        } catch (error) {
            console.error('Error adding place:', error);
        }
    };

    const convertAddressToLatLng = async () => {
        try {
            const response = await fetch(`https://maps.googleapis.com/maps/api/geocode/json?address=${address}&key=AIzaSyBtQ-lmc3Z6wOE6e-TMYFk-f-6crzCkgtA`);
            const data = await response.json();
            const location = data.results[0].geometry.location;
            setLatLng(new window.google.maps.LatLng(location.lat, location.lng));
        } catch (error) {
            console.error('Error converting address to LatLng:', error);
        }
    };

    const handleMapClick = event => {
        setLatLng(event.latLng);
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
            <h2>Add Place</h2>
            {fields.map(field => (
                <div key={field}>
                    <input
                        type="text"
                        name={field}
                        placeholder={`Enter ${field}`}
                        value={formData[field] || ''}
                        onChange={handleChange}
                    />
                </div>
            ))}
            <div>
                <input
                    type="text"
                    placeholder="Enter Address"
                    value={address}
                    onChange={handleAddressChange}
                />
                <button onClick={convertAddressToLatLng}>Convert</button>
            </div>
            <button onClick={handleAddPlace}>Add Place</button>
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
                    <GoogleMap mapContainerStyle={{ height: '100%', width: '100%' }} options={mapOptions}>
                        <MapComponent />
                        {latLng && <Marker position={latLng} />}
                    </GoogleMap>
                </div>
                <p>Click on the map to select location</p>
            </div>
        </div>
    );
};

export default AddPlaceForm;
