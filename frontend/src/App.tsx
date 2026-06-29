import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import Login from './pages/Login';
import Register from './pages/Register';
import ProfileSetup from './pages/ProfileSetup';
import BioSetup from './pages/BioSetup';
import MyProfile from "./pages/MyProfile";
import EditProfile from "./pages/EditProfile";
import Recommendations from "./pages/Recommendations";
import Connections from "./pages/Connections";
import ConnectionRequests from "./pages/ConnectionRequests";

function App() {
    return (
        <Router>
            <div className="App">
                <Routes>
                    <Route path="/" element={<Navigate to="/login" />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />
                    <Route path="/profile/setup" element={<ProfileSetup />} />
                    <Route path="/bio/setup" element={<BioSetup />} />
                    <Route path="/profile/me" element={<MyProfile />} />
                    <Route path="/profile/edit" element={<EditProfile />} />
                    <Route path="/recommendations" element={<Recommendations />} />
                    <Route path="/connections" element={<Connections />} />
                    <Route path="/connections/requests" element={<ConnectionRequests />} />


                </Routes>
            </div>
        </Router>
    );
}

export default App;