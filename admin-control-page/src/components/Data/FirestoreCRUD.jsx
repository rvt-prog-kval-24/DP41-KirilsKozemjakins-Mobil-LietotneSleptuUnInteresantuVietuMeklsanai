import React, { useState, useEffect } from 'react';
import { collection, addDoc, getDocs } from "firebase/firestore";
import { db } from '../DataBase/firebase.jsx'; // Import db from firebase.jsx

const FirestoreCRUD = ({ collectionName, fields, convertTimestampsToStrings }) => {
    const [formData, setFormData] = useState({});
    const [data, setData] = useState([]);

    useEffect(() => {
        fetchPosts();
    }, []);

    const fetchPosts = async () => {
        try {
            const querySnapshot = await getDocs(collection(db, collectionName));
            const newData = querySnapshot.docs.map(doc => {
                const docData = doc.data();
                // Convert timestamp objects to string if required
                if (convertTimestampsToStrings) {
                    const formattedData = Object.keys(docData).reduce((acc, key) => {
                        if (docData[key] instanceof Date) {
                            acc[key] = docData[key].toLocaleString(); // Convert timestamp to string
                        } else {
                            acc[key] = docData[key];
                        }
                        return acc;
                    }, {});
                    return { ...formattedData, id: doc.id };
                }
                return { ...docData, id: doc.id };
            });
            setData(newData);
        } catch (error) {
            console.error("Error fetching documents: ", error);
        }
    };

    const handleChange = e => {
        const { name, value } = e.target;
        setFormData({ ...formData, [name]: value });
    };

    const handleSubmit = async e => {
        e.preventDefault();
        try {
            await addDoc(collection(db, collectionName), formData);
            fetchPosts();
            setFormData({});
        } catch (error) {
            console.error("Error adding document: ", error);
        }
    };

    return (
        <section className="todo-container">
            <div className="todo">
                <h1 className="header">{collectionName}</h1>
                <div>
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
                    <div className="btn-container">
                        <button type="submit" className="btn" onClick={handleSubmit}>
                            Submit
                        </button>
                    </div>
                </div>
                <div className="todo-content">
                    {data.map((item, index) => (
                        <div key={index}>
                            {Object.entries(item).map(([key, value]) => (
                                <p key={key}>
                                    {key}: {value}
                                </p>
                            ))}
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
};

export default FirestoreCRUD;
