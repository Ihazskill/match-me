import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import Login from './pages/Login';
import Register from './pages/Register';
import ProfileSetup from './pages/ProfileSetup';
import BioSetup from './pages/BioSetup';
import MyProfile from './pages/MyProfile';
import EditProfile from './pages/EditProfile';
import Recommendations from './pages/Recommendations';
import Connections from './pages/Connections';
import ConnectionRequests from './pages/ConnectionRequests';
import UserProfileView from './pages/UserProfileView';
import Chats from './pages/Chats';
import ChatView from './pages/ChatView';
import Navbar from './components/Navbar';

const isLoggedIn = () => {
  return localStorage.getItem('token') !== null;
};

const ProtectedRoute = ({
  children,
  allowIncomplete = false,
}: {
  children: React.ReactElement;
  allowIncomplete?: boolean;
}) => {
  if (!isLoggedIn()) {
    return <Navigate to="/login" />;
  }
  if (!allowIncomplete && localStorage.getItem('profileCompleted') === 'false') {
    return <Navigate to="/profile/setup" />;
  }
  return (
    <>
      <Navbar />
      {children}
    </>
  );
};

const PublicRoute = ({ children }: { children: React.ReactElement }) => {
  if (!isLoggedIn()) return children;
  return <Navigate to={localStorage.getItem('profileCompleted') === 'false'
    ? '/profile/setup'
    : '/recommendations'} />;
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />
        <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
        <Route
          path="/profile/setup"
          element={
            <ProtectedRoute allowIncomplete>
              <ProfileSetup />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile/bio"
          element={
            <ProtectedRoute allowIncomplete>
              <BioSetup />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile/me"
          element={
            <ProtectedRoute>
              <MyProfile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile/edit"
          element={
            <ProtectedRoute>
              <EditProfile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/users/:id"
          element={
            <ProtectedRoute>
              <UserProfileView />
            </ProtectedRoute>
          }
        />
        <Route
          path="/recommendations"
          element={
            <ProtectedRoute>
              <Recommendations />
            </ProtectedRoute>
          }
        />
        <Route
          path="/connections"
          element={
            <ProtectedRoute>
              <Connections />
            </ProtectedRoute>
          }
        />
        <Route
          path="/connections/requests"
          element={
            <ProtectedRoute>
              <ConnectionRequests />
            </ProtectedRoute>
          }
        />
        <Route
          path="/chats"
          element={
            <ProtectedRoute>
              <Chats />
            </ProtectedRoute>
          }
        />
        <Route
          path="/chats/:chatId"
          element={
            <ProtectedRoute>
              <ChatView />
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/login" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
