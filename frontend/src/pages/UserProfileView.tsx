import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../api/axios";

interface UserBasic {
  id: number;
  firstName: string;
  lastName: string;
  profilePictureUrl: string | null;
  age: number;
  online: boolean;
}

interface UserProfile {
  id: number;
  firstName: string;
  lastName: string;
  aboutMe: string;
  age: number;
}

interface UserBio {
  id: number;
  interests: string[];
  hobbies: string[];
  musicTaste: string;
  foodPreference: string;
  travelStyle: string;
  lifestyle: string;
  personality: string;
  location: string;
  lookingFor: string;
  seekingInterests: string[];
  seekingLocation: string;
}

const UserProfileView: React.FC = () => {
  const { id } = useParams<{ id: string }>();

  const [basic, setBasic] = useState<UserBasic | null>(null);
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [bio, setBio] = useState<UserBio | null>(null);
  const [loading, setLoading] = useState(true);
  const [notFound, setNotFound] = useState(false);

  useEffect(() => {
    if (!id) return;

    const fetchUser = async () => {
      setLoading(true);
      setNotFound(false);
      try {
        const [basicRes, profileRes, bioRes] = await Promise.all([
          api.get(`/users/${id}`),
          api.get(`/users/${id}/profile`),
          api.get(`/users/${id}/bio`),
        ]);
        setBasic(basicRes.data);
        setProfile(profileRes.data);
        setBio(bioRes.data);
      } catch (err: any) {
        if (err.response?.status === 404) {
          setNotFound(true);
        } else {
          console.error("Failed to load user profile", err);
        }
      } finally {
        setLoading(false);
      }
    };

    fetchUser();
  }, [id]);

  if (loading) return <div style={{ padding: 20 }}>Loading profile...</div>;

  if (notFound) {
    return (
      <div style={{ padding: 20 }}>
        <h2>Profile not available</h2>
        <p>
          You can only view profiles you're recommended, connected with, or have
          a pending request with.
        </p>
      </div>
    );
  }

  if (!basic || !profile || !bio) return null;

  return (
    <div style={{ padding: 20, maxWidth: 600, margin: "0 auto" }}>
      <div style={{ textAlign: "center" }}>
        <div style={{ position: "relative", display: "inline-block" }}>
          {basic.profilePictureUrl ? (
            <img
              src={basic.profilePictureUrl}
              alt={`${basic.firstName} ${basic.lastName}`}
              style={{
                width: 150,
                height: 150,
                borderRadius: "50%",
                objectFit: "cover",
              }}
            />
          ) : (
            <div
              style={{
                width: 150,
                height: 150,
                borderRadius: "50%",
                backgroundColor: "#eee",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                fontSize: 60,
              }}
            >
              👤
            </div>
          )}
          <span
            style={{
              position: "absolute",
              bottom: "10px",
              right: "10px",
              width: "22px",
              height: "22px",
              borderRadius: "50%",
              backgroundColor: basic.online ? "#2ecc71" : "#bbb",
              border: "3px solid white",
            }}
            title={basic.online ? "Online" : "Offline"}
          />
        </div>
        <h2>
          {basic.firstName} {basic.lastName}
        </h2>
        <p style={{ color: "#666" }}>{basic.age} years old</p>
      </div>

      <h3>About</h3>
      <p>{profile.aboutMe}</p>

      <h3>Details</h3>
      <ul>
        <li>
          <strong>Location:</strong> {bio.location}
        </li>
        <li>
          <strong>Interests:</strong> {bio.interests?.join(", ")}
        </li>
        <li>
          <strong>Hobbies:</strong> {bio.hobbies?.join(", ")}
        </li>
        <li>
          <strong>Music taste:</strong> {bio.musicTaste}
        </li>
        <li>
          <strong>Food preference:</strong> {bio.foodPreference}
        </li>
        <li>
          <strong>Travel style:</strong> {bio.travelStyle}
        </li>
        <li>
          <strong>Lifestyle:</strong> {bio.lifestyle}
        </li>
        <li>
          <strong>Personality:</strong> {bio.personality}
        </li>
        <li>
          <strong>Looking for:</strong> {bio.lookingFor}
        </li>
      </ul>
    </div>
  );
};

export default UserProfileView;
