// PlacesDataPage.jsx

import "../App.css";
import React from 'react';
import FirestoreCRUD from './Data/FirestoreCRUD';

const PlacesDataPage = () => {
    const fields = ["PlaceName", "Description", "Tag", "PosX", "PosY", "AuthorID"];

    return <FirestoreCRUD collectionName="Places" fields={fields} />;
};

export default PlacesDataPage;
