// AdminsPage.jsx

import React from 'react';
import FirestoreCRUD from './Data/FirestoreCRUD';

const AdminsPage = () => {
    const fields = ["adminname", "password", "dateOfCreation"];

    return <FirestoreCRUD collectionName="Admins" fields={fields} />;
};

export default AdminsPage;
