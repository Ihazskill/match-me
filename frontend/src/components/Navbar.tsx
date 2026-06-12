import React from "react";
import { useNavigate, Link } from "react-router-dom";

const Navbar = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    navigate("/login");
  };

  return (
    <nav
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
