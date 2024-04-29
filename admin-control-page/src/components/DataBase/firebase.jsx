//firebase.jsx

// Import the functions you need from the SDKs you need
import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
import { getFirestore } from 'firebase/firestore';
// TODO: Add SDKs for Firebase products that you want to use
// https://firebase.google.com/docs/web/setup#available-libraries

// Your web app's Firebase configuration
// For Firebase JS SDK v7.20.0 and later, measurementId is optional
const firebaseConfig = {
  apiKey: "AIzaSyBU0oUlkzfxy3cLzQf9IWOeat6gDOyCyfU",
  authDomain: "undergroundriga.firebaseapp.com",
  databaseURL: "https://undergroundriga-default-rtdb.europe-west1.firebasedatabase.app",
  projectId: "undergroundriga",
  storageBucket: "undergroundriga.appspot.com",
  messagingSenderId: "1086149395268",
  appId: "1:1086149395268:web:75951b49b5fb408a068d11",
  measurementId: "G-STPHV6MXRK"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);
const db = getFirestore(app); // Add this line

export { db };