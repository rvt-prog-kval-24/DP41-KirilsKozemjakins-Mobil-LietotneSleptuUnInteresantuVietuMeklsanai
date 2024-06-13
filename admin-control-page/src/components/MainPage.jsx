import React, { useState, useEffect, useCallback } from 'react';
import { getDocs, collection } from 'firebase/firestore';
import { db } from './DataBase/firebase';
import { Card, Statistic, Button } from 'antd';
import { Pie, Bar } from '@ant-design/charts';
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
  const [isInitialFetch, setIsInitialFetch] = useState(true);

  const fetchStats = useCallback(async () => {
    const placesCollection = collection(db, 'Places');
    const suggestionsCollection = collection(db, 'PlacesSuggestions');
    const usersCollection = collection(db, 'Users');

    const placesSnapshot = await getDocs(placesCollection);
    const totalPlaces = placesSnapshot.docs.length;

    const usersSnapshot = await getDocs(usersCollection);
    const totalUsers = usersSnapshot.docs.length;

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

    const suggestionsPerDay = {};
    suggestionsSnapshot.forEach((doc) => {
      const date = doc.data().suggestionDate;
      if (date) {
        const formattedDate = new Date(date).toDateString();
        suggestionsPerDay[formattedDate] =
          suggestionsPerDay[formattedDate] ? suggestionsPerDay[formattedDate] + 1 : 1;
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
    setIsInitialFetch(false);
  }, []);

  useEffect(() => {
    fetchStats();
  }, [fetchStats]);

  const exportToPDF = async () => {
    setIsExporting(true);

    const mainContent = document.getElementById('main-content');

    try {
      const canvas = await html2canvas(mainContent);
      const pdf = new jsPDF({
        orientation: 'landscape',
        unit: 'px',
        format: 'a2',
      });

      const imgData = canvas.toDataURL('image/png');
      pdf.addImage(imgData, 'PNG', 0, 0);
      pdf.save('System_Statistics.pdf');
    } catch (error) {
      console.error('Error exporting to PDF:', error);
    } finally {
      setIsExporting(false);
    }
  };

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
      position: 'bottom',
    },
    meta: {
      value: {
        formatter: (v) => `${v}`,
      },
    },
  };

  const barConfig = {
    data: Object.entries(stats.suggestionsPerDay).map(([date, count]) => ({
      date,
      count,
    })),
    xField: 'date',
    yField: 'count',
    meta: {
      count: {
        formatter: (v) => `${v}`,
      },
    },
  };

  console.log("Pending Suggestions Count after:", stats.pendingSuggestions);

  return (
    <div>
      <h2>System Statistics</h2>
      <Button type="primary" onClick={exportToPDF} disabled={isExporting}>
        {isExporting ? 'Exporting...' : 'Export to PDF'}
      </Button>
      <div id="main-content">
        <div className="stats-grid">
        
          {!isInitialFetch && (
            <div className="chart-container">
              <h3>Statistic on Suggestions</h3>
              <Pie {...pieConfig} />
            </div>
          )}
          <div className="chart-container">
            <h3>Suggestion per day</h3>
            <Bar {...barConfig} />
          </div>
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
