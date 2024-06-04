import React, { useState, useEffect } from 'react';
import { deleteDoc, collection, getDocs, doc } from 'firebase/firestore';
import { db } from '../DataBase/firebase'; // Assuming you have a similar import for db
import { Table, Button, Space } from 'antd';

const CommentsDataPage = () => {
  const [comments, setComments] = useState([]); // Array to store comments

  const fetchComments = async () => {
    const commentsCollection = collection(db, 'Comments');
    const commentsSnapshot = await getDocs(commentsCollection);
    const fetchedComments = commentsSnapshot.docs.map((doc) => ({
      ...doc.data(),
      id: doc.id,
    }));
    setComments(fetchedComments);
  };

  const deleteComment = async (commentId) => {
    await deleteDoc(doc(db, 'Comments', commentId));
    fetchComments(); // Update comments after deletion
  };

  const columns = [
    {
      title: 'Comment Mark',
      dataIndex: 'commentMark',
      key: 'commentMark',
    },
    {
      title: 'Comment Text',
      dataIndex: 'commentText',
      key: 'commentText',
    },
    {
      title: 'Date & Time',
      dataIndex: 'dateTime',
      key: 'dateTime',
    },
    {
      title: 'Place ID',
      dataIndex: 'placeID',
      key: 'placeID',
    },
    {
      title: 'User ID',
      dataIndex: 'userID',
      key: 'userID',
    },
    {
      title: 'Action',
      dataIndex: '',
      key: 'action',
      render: (comment) => (
        <Space direction="horizontal">
          <Button type="danger" onClick={() => deleteComment(comment.id)}>
            Delete
          </Button>
        </Space>
      ),
    },
  ];

  useEffect(() => {
    fetchComments();
  }, []); // Fetch comments on component mount

  return (
    <div>
      <h2>Manage Comments</h2>
      <Table dataSource={comments} columns={columns} pagination={{ pageSize: 5 }} />
    </div>
  );
};

export default CommentsDataPage;
