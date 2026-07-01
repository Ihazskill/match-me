import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

const EditProfile = () => {
  const navigate = useNavigate();
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [age, setAge] = useState("");
  const [aboutMe, setAboutMe] = useState("");
  const [profilePictureUrl, setProfilePictureUrl] = useState("");
  const [interests, setInterests] = useState("");
  const [hobbies, setHobbies] = useState("");
  const [musicTaste, setMusicTaste] = useState("");
  const [foodPreference, setFoodPreference] = useState("");
  const [travelStyle, setTravelStyle] = useState("");
  const [lifestyle, setLifestyle] = useState("");
  const [personality, setPersonality] = useState("");
  const [lookingFor, setLookingFor] = useState("");
  const [seekingInterests, setSeekingInterests] = useState("");
  const [locationId, setLocationId] = useState("1");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const profileRes = await api.get("/me/profile");
        const bioRes = await api.get("/me/bio");
        const p = profileRes.data;
        const b = bioRes.data;
        setFirstName(p.firstName || "");
        setLastName(p.lastName || "");
        setAge(p.age?.toString() || "");
        setAboutMe(p.aboutMe || "");
        setProfilePictureUrl(p.profilePictureUrl || "");
        setInterests(b.interests?.join(", ") || "");
        setHobbies(b.hobbies?.join(", ") || "");
        setMusicTaste(b.musicTaste || "");
        setFoodPreference(b.foodPreference || "");
        setTravelStyle(b.travelStyle || "");
        setLifestyle(b.lifestyle || "");
        setPersonality(b.personality || "");
        setLookingFor(b.lookingFor || "");
        setSeekingInterests(b.seekingInterests?.join(", ") || "");
      } catch (err: any) {
        setError("Failed to load profile data");
      }
    };
    fetchData();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    try {
      await api.post("/profile", {
        firstName,
        lastName,
        age: parseInt(age),
        aboutMe,
        profilePictureUrl,
      });
      await api.post("/profile/bio", {
        interests: interests
          .split(",")
          .map((s) => s.trim())
          .filter(Boolean),
        hobbies: hobbies
          .split(",")
          .map((s) => s.trim())
          .filter(Boolean),
        musicTaste,
        foodPreference,
        travelStyle,
        lifestyle,
        personality,
        lookingFor,
        seekingInterests: seekingInterests
          .split(",")
          .map((s) => s.trim())
          .filter(Boolean),
        locationId: parseInt(locationId),
      });
      setSuccess("Profile updated successfully!");
      setTimeout(() => navigate("/profile/me"), 1500);
    } catch (err: any) {
      setError(err.response?.data || "Failed to update profile");
    }
  };

  return (
    <div style={{ maxWidth: "500px", margin: "50px auto", padding: "20px" }}>
      <h2>Edit Profile</h2>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: "15px" }}>
          <label>First Name</label>
          <br />
          <input
            type="text"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          />
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Last Name</label>
          <br />
          <input
            type="text"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          />
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Age</label>
          <br />
          <input
            type="number"
            value={age}
            onChange={(e) => setAge(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          />
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>About Me</label>
          <br />
          <textarea
            value={aboutMe}
            onChange={(e) => setAboutMe(e.target.value)}
            rows={4}
            style={{ width: "100%", padding: "8px" }}
          />
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Profile Picture URL</label>
          <br />
          <input
            type="text"
            value={profilePictureUrl}
            onChange={(e) => setProfilePictureUrl(e.target.value)}
            style={{ width: "100%", padding: "8px" }}
          />
          {profilePictureUrl && (
            <div style={{ marginTop: "10px" }}>
              <img
                src={profilePictureUrl}
                alt="Preview"
                style={{
                  width: "80px",
                  height: "80px",
                  borderRadius: "50%",
                  objectFit: "cover",
                }}
              />
              <button
                type="button"
                onClick={() => setProfilePictureUrl("")}
                style={{
                  marginLeft: "10px",
                  padding: "5px 10px",
                  backgroundColor: "#e74c3c",
                  color: "white",
                  border: "none",
                  borderRadius: "4px",
                  cursor: "pointer",
                }}
              >
                Remove Picture
              </button>
            </div>
          )}
        </div>
        <hr />
        <h3>Bio Information</h3>
        <div style={{ marginBottom: "15px" }}>
          <label>Interests (comma separated)</label>
          <br />
          <input
            type="text"
            value={interests}
            onChange={(e) => setInterests(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          />
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Hobbies (comma separated)</label>
          <br />
          <input
            type="text"
            value={hobbies}
            onChange={(e) => setHobbies(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          />
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Music Taste</label>
          <br />
          <select
            value={musicTaste}
            onChange={(e) => setMusicTaste(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          >
            <option value="">Select...</option>
            <option value="pop">Pop</option>
            <option value="rock">Rock</option>
            <option value="hiphop">Hip-Hop</option>
            <option value="jazz">Jazz</option>
            <option value="classical">Classical</option>
            <option value="electronic">Electronic</option>
            <option value="other">Other</option>
          </select>
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Food Preference</label>
          <br />
          <select
            value={foodPreference}
            onChange={(e) => setFoodPreference(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          >
            <option value="">Select...</option>
            <option value="vegan">Vegan</option>
            <option value="vegetarian">Vegetarian</option>
            <option value="meat">Meat Eater</option>
            <option value="seafood">Seafood Lover</option>
            <option value="anything">Eat Anything</option>
          </select>
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Travel Style</label>
          <br />
          <select
            value={travelStyle}
            onChange={(e) => setTravelStyle(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          >
            <option value="">Select...</option>
            <option value="adventurer">Adventurer</option>
            <option value="relaxed">Relaxed/Beach</option>
            <option value="cultural">Cultural Explorer</option>
            <option value="homebody">Prefer Staying Home</option>
          </select>
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Lifestyle</label>
          <br />
          <select
            value={lifestyle}
            onChange={(e) => setLifestyle(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          >
            <option value="">Select...</option>
            <option value="active">Active</option>
            <option value="balanced">Balanced</option>
            <option value="relaxed">Relaxed</option>
            <option value="workaholic">Workaholic</option>
          </select>
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Personality</label>
          <br />
          <select
            value={personality}
            onChange={(e) => setPersonality(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          >
            <option value="">Select...</option>
            <option value="introvert">Introvert</option>
            <option value="extrovert">Extrovert</option>
            <option value="ambivert">Ambivert</option>
          </select>
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Looking For</label>
          <br />
          <textarea
            value={lookingFor}
            onChange={(e) => setLookingFor(e.target.value)}
            rows={3}
            style={{ width: "100%", padding: "8px" }}
          />
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Seeking Interests (comma separated)</label>
          <br />
          <input
            type="text"
            value={seekingInterests}
            onChange={(e) => setSeekingInterests(e.target.value)}
            style={{ width: "100%", padding: "8px" }}
          />
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Location ID</label>
          <br />
          <input
            type="number"
            value={locationId}
            onChange={(e) => setLocationId(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          />
        </div>
        {error && <p style={{ color: "red" }}>{error}</p>}
        {success && <p style={{ color: "green" }}>{success}</p>}
        <button type="submit" style={{ width: "100%", padding: "10px" }}>
          Save Changes
        </button>
      </form>
    </div>
  );
};

export default EditProfile;
