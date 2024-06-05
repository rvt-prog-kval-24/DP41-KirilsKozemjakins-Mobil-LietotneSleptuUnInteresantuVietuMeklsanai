import React, { useState, useEffect } from 'react';
import { getDocs, collection, query, where } from 'firebase/firestore';
import { db } from './DataBase/firebase'; // Assuming you have a similar import for db
import { Card, Statistic, Button } from 'antd'; // Using Ant Design for cards, statistics, and button
import { Pie, Bar } from '@ant-design/charts'; // Import Pie and Bar chart components
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

const MainPage = () => {
  const [stats, setStats] = useState({
    totalPlaces: 0,
    totalSuggestions: 0,
    pendingSuggestions: 0,
    positiveSuggestions: 0,
    negativeSuggestions: 0,
    totalUsers: 0,
    suggestionsPerDay: {},
  });

  const [isExporting, setIsExporting] = useState(false);

  const fetchStats = async () => {
    const placesCollection = collection(db, 'Places');
    const suggestionsCollection = collection(db, 'PlacesSuggestions');
    const usersCollection = collection(db, 'Users');

    // Get total places
    const placesSnapshot = await getDocs(placesCollection);
    const totalPlaces = placesSnapshot.docs.length;

    const usersSnapshot = await getDocs(usersCollection);
    const totalUsers = usersSnapshot.docs.length;

    // Get total suggestions and count by response status
    const suggestionsSnapshot = await getDocs(suggestionsCollection);
    const totalSuggestions = suggestionsSnapshot.docs.length;
    const pendingSuggestions = suggestionsSnapshot.docs.filter(
      (doc) => doc.data().respond === 'pending'
    ).length;
    const positiveSuggestions = suggestionsSnapshot.docs.filter(
      (doc) => doc.data().respond === 'positive'
    ).length;
    const negativeSuggestions = suggestionsSnapshot.docs.filter(
      (doc) => doc.data().respond === 'negative'
    ).length;

    // Get suggestions per day (assuming a `date` field)
    const suggestionsPerDay = {};
    suggestionsSnapshot.forEach((doc) => {
      const date = doc.data().suggestionDate; // Get the date field
      if (date) {
        const formattedDate = new Date(date).toDateString();
        suggestionsPerDay[formattedDate] = suggestionsPerDay[formattedDate] ? suggestionsPerDay[formattedDate] + 1 : 1;
      }
    });

    setStats({
      totalPlaces,
      totalSuggestions,
      pendingSuggestions,
      positiveSuggestions,
      negativeSuggestions,
      totalUsers,
      suggestionsPerDay,
    });
  };

  useEffect(() => {
    fetchStats();
  }, []);

  const exportToPDF = async () => {
    setIsExporting(true); // Set loading state

    const mainContent = document.getElementById('main-content'); // Target the content area

    try {
      const canvas = await html2canvas(mainContent); // Generate canvas image

      const pdf = new jsPDF({
        orientation: 'landscape', // Adjust if needed
        unit: 'px',
        format: 'a2', // Adjust paper size if needed
      });

      const imgData = canvas.toDataURL('image/png'); // Convert canvas to base64 image data
      pdf.addImage(imgData, 'PNG', 0, 0); // Add image to PDF

      pdf.save('System_Statistics.pdf'); // Download PDF with filename
    } catch (error) {
      console.error('Error exporting to PDF:', error);
      // Handle errors appropriately (e.g., display an error message to the user)
    } finally {
      setIsExporting(false); // Reset loading state
    }
  };

  // Pie chart config
 const pieConfig = {
    data: [
     { type: 'Pending', value: stats.pendingSuggestions },
     { type: 'Positive', value: stats.positiveSuggestions },
     { type: 'Negative', value: stats.negativeSuggestions },
    ],
    angleField: 'value',
    colorField: 'type',
    seriesField: 'type',
    legend: {
     position: 'bottom', // Position legend at the bottom
    },
    meta: {
     value: {
      formatter: (v) => `${v}`, // Format value labels without decimals
     },
    },
   };
   console.log("Pending Suggestions Count after:", stats.pendingSuggestions);
  
  
  
   // Bar chart config
   const barConfig = {
    data: Object.entries(stats.suggestionsPerDay).map(([date, count]) => ({ date, count })),
    xField: 'date',
    yField: 'count',
    meta: {
     count: {
      formatter: (v) => `${v}`, // Format value labels without decimals
     },
    },
   };
  

  return (
    <div>
      <h2>System Statistics</h2>
      <Button type="primary" onClick={exportToPDF} disabled={isExporting}>
        {isExporting ? 'Exporting...' : 'Export to PDF'}
      </Button>
      <div id="main-content">
        <div className="stats-grid">
          <div className="chart-container">
            <Pie {...pieConfig} />
          </div>
          <div className="chart-container">
            <Bar {...barConfig} />
          
          </div>
          {/* Other statistic cards */}
          <Card>
            <Statistic title="Total Places" value={stats.totalPlaces} />
          </Card>
          <Card>
            <Statistic title="Total Suggestions" value={stats.totalSuggestions} />
          </Card>
          <Card>
            <Statistic title="Total Users" value={stats.totalUsers} />
          </Card>
        </div>
      </div>
    </div>
  );
};

export default MainPage;
