import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Register from "./pages/Register";
import Login from "./pages/Login";
import ProfileSetup from "./pages/ProfileSetup";
import BioSetup from "./pages/BioSetup";
import MyProfile from "./pages/MyProfile";
import EditProfile from "./pages/EditProfile";
import Navbar from "./components/Navbar";

const isLoggedIn = () => {
  return localStorage.getItem("token") !== null;
};

const ProtectedRoute = ({ children }: { children: React.ReactElement }) => {
  if (!isLoggedIn()) {
    return <Navigate to="/login" />;
  }
  return (
    <>
      <Navbar />
      {children}
    </>
  );
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route
          path="/profile/setup"
          element={
            <ProtectedRoute>
              <ProfileSetup />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile/bio"
          element={
            <ProtectedRoute>
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
          path="/recommendations"
          element={
            <ProtectedRoute>
              <div style={{ padding: "20px" }}>Recommendations coming soon</div>
            </ProtectedRoute>
          }
        />
        <Route
          path="/connections"
          element={
            <ProtectedRoute>
              <div style={{ padding: "20px" }}>Connections coming soon</div>
            </ProtectedRoute>
          }
        />
        <Route path="/" element={<Navigate to="/login" />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
