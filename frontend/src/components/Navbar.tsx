import React, { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/axios";
import { connectRealtime, disconnectRealtime, MESSAGE_EVENT, READ_EVENT } from "../api/realtime";

const Navbar = () => {
  const navigate = useNavigate();
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    connectRealtime();
    const loadUnread = async () => {
      try {
        const response = await api.get("/chats");
        setUnreadCount(response.data.reduce(
          (total: number, chat: { unreadCount: number }) => total + chat.unreadCount,
          0
        ));
      } catch {
        setUnreadCount(0);
      }
    };
    void loadUnread();
    const refresh = () => void loadUnread();
    window.addEventListener(MESSAGE_EVENT, refresh);
    window.addEventListener(READ_EVENT, refresh);
    return () => {
      window.removeEventListener(MESSAGE_EVENT, refresh);
      window.removeEventListener(READ_EVENT, refresh);
    };
  }, []);

  const handleLogout = async () => {
    try {
      await api.post("/auth/signout");
    } catch (err) {
      // even if this fails, still log the user out locally
      console.error("Failed to update online status on logout", err);
    }
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("profileCompleted");
    await disconnectRealtime();
    navigate("/login");
  };
  return (
    <nav className="navbar"
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        padding: "10px 20px",
        backgroundColor: "#333",
        color: "white",
      }}
    >
      <div style={{ display: "flex", gap: "20px" }}>
        <Link
          to="/recommendations"
          style={{ color: "white", textDecoration: "none" }}
        >
          Recommendations
        </Link>
        <Link
          to="/connections"
          style={{ color: "white", textDecoration: "none" }}
        >
          Connections
        </Link>
        <Link to="/connections/requests" style={{ color: "white", textDecoration: "none" }}>
          Requests
        </Link>
        <Link to="/chats" style={{ color: "white", textDecoration: "none" }}>
          Chats {unreadCount > 0 && <span className="unread-badge">{unreadCount}</span>}
        </Link>
        <Link
          to="/profile/me"
          style={{ color: "white", textDecoration: "none" }}
        >
          My Profile
        </Link>
      </div>
      <button
        onClick={handleLogout}
        style={{
          padding: "8px 16px",
          backgroundColor: "#e74c3c",
          color: "white",
          border: "none",
          borderRadius: "4px",
          cursor: "pointer",
        }}
      >
        Logout
      </button>
    </nav>
  );
};
export default Navbar;
