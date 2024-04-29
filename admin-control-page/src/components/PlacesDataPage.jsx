// PlacesDataPage.jsx

import "../App.css";
import React from 'react';
import AddPlaceForm from './Data/AddPlacesForm.jsx';

const PlacesDataPage = () => {
    const fields = ['PlaceName', 'Description', 'Tag', 'AuthorID'];

    return <AddPlaceForm fields={fields} />;
};

export default PlacesDataPage;
