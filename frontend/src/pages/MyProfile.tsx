import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

const MyProfile = () => {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<any>(null);
  const [bio, setBio] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const profileRes = await api.get("/me/profile");
        const bioRes = await api.get("/me/bio");
        setProfile(profileRes.data);
        setBio(bioRes.data);
      } catch (err: any) {
        setError("Failed to load profile");
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  if (loading) return <div style={{ padding: "20px" }}>Loading...</div>;
  if (error)
    return <div style={{ padding: "20px", color: "red" }}>{error}</div>;

  return (
    <div style={{ maxWidth: "600px", margin: "50px auto", padding: "20px" }}>
      <h2>My Profile</h2>

      {/* Profile Picture */}
      <div style={{ textAlign: "center", marginBottom: "20px" }}>
        {profile?.profilePictureUrl ? (
          <img
            src={profile.profilePictureUrl}
            alt="Profile"
            style={{
              width: "120px",
              height: "120px",
              borderRadius: "50%",
              objectFit: "cover",
            }}
          />
        ) : (
          <div style={{ fontSize: "80px" }}>👤</div>
        )}
      </div>

      {/* Basic Info */}
      <div
        style={{
          marginBottom: "20px",
          padding: "15px",
          backgroundColor: "#f5f5f5",
          borderRadius: "8px",
        }}
      >
        <h3>Basic Info</h3>
        <p>
          <strong>Name:</strong> {profile?.firstName} {profile?.lastName}
        </p>
        <p>
          <strong>Age:</strong> {profile?.age}
        </p>
        <p>
          <strong>About Me:</strong> {profile?.aboutMe}
        </p>
      </div>

      {/* Bio Info */}
      {bio && (
        <div
          style={{
            marginBottom: "20px",
            padding: "15px",
            backgroundColor: "#f5f5f5",
            borderRadius: "8px",
          }}
        >
          <h3>My Bio</h3>
          <p>
            <strong>Interests:</strong> {bio?.interests?.join(", ")}
          </p>
          <p>
            <strong>Hobbies:</strong> {bio?.hobbies?.join(", ")}
          </p>
          <p>
            <strong>Music:</strong> {bio?.musicTaste}
          </p>
          <p>
            <strong>Food:</strong> {bio?.foodPreference}
          </p>
          <p>
            <strong>Travel Style:</strong> {bio?.travelStyle}
          </p>
          <p>
            <strong>Lifestyle:</strong> {bio?.lifestyle}
          </p>
          <p>
            <strong>Personality:</strong> {bio?.personality}
          </p>
          <p>
            <strong>Looking For:</strong> {bio?.lookingFor}
          </p>
        </div>
      )}

      <button
        onClick={() => navigate("/profile/edit")}
        style={{ width: "100%", padding: "10px", marginBottom: "10px" }}
      >
        Edit Profile
      </button>
    </div>
  );
};

export default MyProfile;
