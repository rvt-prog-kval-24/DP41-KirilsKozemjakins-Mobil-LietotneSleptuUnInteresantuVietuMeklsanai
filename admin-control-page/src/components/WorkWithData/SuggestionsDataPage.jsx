// PlacesSuggestionsPage.jsx

import React, { useEffect, useState } from 'react';
import { db, storage } from '../DataBase/firebase'; // Assuming you have storage initialized in firebase.js
import { collection, getDocs, doc, updateDoc, addDoc } from 'firebase/firestore';
import { Card, Space, Button } from 'antd';

const PlacesSuggestionsPage = () => {
  const [suggestions, setSuggestions] = useState([]);
  const suggestionsCollection = collection(db, 'PlacesSuggestions');

  useEffect(() => {
    const fetchSuggestions = async () => {
      const querySnapshot = await getDocs(suggestionsCollection);
      const suggestionsList = querySnapshot.docs.map(doc => ({ id: doc.id, ...doc.data() }));
      setSuggestions(suggestionsList);
    };

    fetchSuggestions();
  }, []);

  const handleConfirm = async (suggestion) => {
    const placesCollection = collection(db, 'Places');
    const suggestionDoc = doc(suggestionsCollection, suggestion.id);
  
    try {
      // Ensure all required fields are available
      const {
        PlaceName = '',
        Description = '',
        tags = '',
        PosX = '',
        PosY = '',
        SuggestionDate = new Date().toISOString(),
        imageUrl = '', // Updated from photoURL
        UserId = '', // Added UserId field
      } = suggestion;
  
      // Upload image to Firebase Storage if imageUrl is available

  
      // Add suggestion data to the Places table
      await addDoc(placesCollection, {
        PlaceName,
        Description,
        tags,
        PosX,
        PosY,
        SuggestionDate,
        photoURL: imageUrl, // Use imageUrlInDatabase instead of imageUrl
        UserId, // Include UserId
        // Add any additional fields you want to save to the Places table
      });
  
      // Update suggestion document in the PlacesSuggestions collection
      await updateDoc(suggestionDoc, {
        respond: 'positive',
      });
  
      // Remove the confirmed suggestion from the suggestions state
      setSuggestions(suggestions.filter(item => item.id !== suggestion.id));
    } catch (error) {
      console.error('Error confirming suggestion:', error);
    }
  };
  

  const handleReject = async (suggestion) => {
    const suggestionDoc = doc(suggestionsCollection, suggestion.id);

    try {
      // Update suggestion document in the PlacesSuggestions collection
      await updateDoc(suggestionDoc, {
        respond: 'negative',
      });

      // Remove the rejected suggestion from the suggestions state
      setSuggestions(suggestions.filter(item => item.id !== suggestion.id));
    } catch (error) {
      console.error('Error rejecting suggestion:', error);
    }
  };

  return (
    <Space direction="vertical" size={16}>
      {suggestions.map(suggestion => (
        <Card
          key={suggestion.id}
          title={suggestion.PlaceName}
          extra={<span>{new Date(suggestion.SuggestionDate).toLocaleDateString()}</span>}
          style={{ width: 300 }}
        >
          <p>{suggestion.Description}</p>
          <p><b>Tag:</b> {suggestion.Tag}</p>
          <p><b>Location:</b> ({suggestion.PosX}, {suggestion.PosY})</p>
          {suggestion.imageUrl && (
            <img src={suggestion.imageUrl} alt="Suggestion" style={{ width: '100%', height: 'auto' }} />
          )}
          <p><b>Suggested by:</b> {suggestion.UserId}</p>
          <Button type="primary" onClick={() => handleConfirm(suggestion)} style={{ marginRight: 8 }}>
            Confirm
          </Button>
          <Button danger onClick={() => handleReject(suggestion)}>
            Reject
          </Button>
        </Card>
      ))}
    </Space>
  );
};

export default PlacesSuggestionsPage;
