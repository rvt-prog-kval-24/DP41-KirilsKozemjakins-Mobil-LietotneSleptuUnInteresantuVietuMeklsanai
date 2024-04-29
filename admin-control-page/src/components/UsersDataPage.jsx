// UsersDataPage.jsx

import "../App.css";
import React from 'react';
import FirestoreCRUD from './Data/FirestoreCRUD';

const UsersDataPage = () => {
    const fields = ["username", "password", "email"];

    return <FirestoreCRUD collectionName="Users" fields={fields} />;
};

export default UsersDataPage;
