import React from 'react';
import FirestoreCRUD from './../Data/FirestoreCRUD';

const UsersDataPage = () => {
    const fields = ["username", "email", "password", "DateOfCreation"];

    return <FirestoreCRUD collectionName="Users" fields={fields} convertTimestampsToStrings />;
};

export default UsersDataPage;
