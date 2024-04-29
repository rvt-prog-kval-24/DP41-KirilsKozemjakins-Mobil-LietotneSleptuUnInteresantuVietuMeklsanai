// PlacesSuggestionsPage.jsx

import React from 'react';
import FirestoreCRUD from './Data/FirestoreCRUD';

const PlacesSuggestionsPage = () => {
    const fields = ["PlaceName", "Description", "userId", "Tag", "PosX", "PosY"];

    return <FirestoreCRUD collectionName="PlacesSuggestions" fields={fields} />;
};

export default PlacesSuggestionsPage;
