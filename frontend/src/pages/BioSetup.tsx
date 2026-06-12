import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

const BioSetup = () => {
  const navigate = useNavigate();
  const [interests, setInterests] = useState("");
  const [hobbies, setHobbies] = useState("");
  const [musicTaste, setMusicTaste] = useState("");
  const [foodPreference, setFoodPreference] = useState("");
  const [travelStyle, setTravelStyle] = useState("");
  const [lifestyle, setLifestyle] = useState("");
  const [personality, setPersonality] = useState("");
  const [lookingFor, setLookingFor] = useState("");
  const [seekingInterests, setSekingInterests] = useState("");
  const [locationId, setLocationId] = useState("1");
  const [error, setError] = useState("");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    try {
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
      navigate("/recommendations");
    } catch (err: any) {
      setError(err.response?.data || "Failed to save bio");
    }
  };

  return (
    <div style={{ maxWidth: "500px", margin: "50px auto", padding: "20px" }}>
      <h2>Tell Us About Yourself</h2>
      <p>This information is used to find your best matches.</p>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: "15px" }}>
          <label>Interests (comma separated, e.g. music, travel, coding)</label>
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
            placeholder="Describe what kind of connection you're looking for..."
            style={{ width: "100%", padding: "8px" }}
          />
        </div>
        <div style={{ marginBottom: "15px" }}>
          <label>Interests you're seeking in others (comma separated)</label>
          <br />
          <input
            type="text"
            value={seekingInterests}
            onChange={(e) => setSekingInterests(e.target.value)}
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
        <button type="submit" style={{ width: "100%", padding: "10px" }}>
          Finish Setup →
        </button>
      </form>
    </div>
  );
};

export default BioSetup;
