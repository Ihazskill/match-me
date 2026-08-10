import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/axios";

interface Location {
  id: number;
  city: string;
  country: string;
}

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
  const [locationId, setLocationId] = useState("");
  const [locations, setLocations] = useState<Location[]>([]);
  const [latitude, setLatitude] = useState<number | null>(null);
  const [longitude, setLongitude] = useState<number | null>(null);
  const [maxRadiusKm, setMaxRadiusKm] = useState("50");
  const [locationStatus, setLocationStatus] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [basicRes, profileRes, bioRes, locationsRes] = await Promise.all([
          api.get("/me"),
          api.get("/me/profile"),
          api.get("/me/bio"),
          api.get<Location[]>("/locations"),
        ]);
        const p = profileRes.data;
        const b = bioRes.data;
        const availableLocations = locationsRes.data;
        setLocations(availableLocations);
        setFirstName(p.firstName || "");
        setLastName(p.lastName || "");
        setAge(p.age?.toString() || "");
        setAboutMe(p.aboutMe || "");
        setProfilePictureUrl(basicRes.data.profilePictureUrl || "");
        setInterests(b.interests?.join(", ") || "");
        setHobbies(b.hobbies?.join(", ") || "");
        setMusicTaste(b.musicTaste || "");
        setFoodPreference(b.foodPreference || "");
        setTravelStyle(b.travelStyle || "");
        setLifestyle(b.lifestyle || "");
        setPersonality(b.personality || "");
        setLookingFor(b.lookingFor || "");
        setSeekingInterests(b.seekingInterests?.join(", ") || "");
        const currentLocation = availableLocations.find((location) => location.city === b.location);
        setLocationId(currentLocation?.id.toString() || "");
        setLatitude(b.latitude ?? null);
        setLongitude(b.longitude ?? null);
        setMaxRadiusKm(b.maxRadiusKm?.toString() || "50");
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
        latitude,
        longitude,
        maxRadiusKm: latitude !== null ? Number(maxRadiusKm) : null,
      });
      setSuccess("Profile updated successfully!");
      setTimeout(() => navigate("/profile/me"), 1500);
    } catch (err: any) {
      setError(err.response?.data || "Failed to update profile");
    }
  };

  const useCurrentLocation = () => {
    if (!navigator.geolocation) {
      setLocationStatus("Browser location is not available");
      return;
    }
    setLocationStatus("Requesting location...");
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLatitude(position.coords.latitude);
        setLongitude(position.coords.longitude);
        setLocationStatus("Precise location enabled");
      },
      () => setLocationStatus("Location permission was not granted")
    );
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
          <label>Profile Picture</label>
          <br />
          <div style={{ marginTop: "10px", marginBottom: "10px" }}>
            {profilePictureUrl ? (
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
            ) : (
              <div
                style={{
                  width: "80px",
                  height: "80px",
                  borderRadius: "50%",
                  backgroundColor: "#eee",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: "36px",
                }}
              >
                👤
              </div>
            )}
          </div>
          <input
            type="file"
            accept="image/*"
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (!file) return;
              if (file.size > 2 * 1024 * 1024) {
                setError("Profile pictures must be 2 MB or smaller");
                return;
              }
              const reader = new FileReader();
              reader.onloadend = () => {
                setProfilePictureUrl(reader.result as string);
              };
              reader.readAsDataURL(file);
            }}
          />
          {profilePictureUrl && (
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
          <label>Maximum Distance (km)</label>
          <br />
          <input
            type="number"
            min="1"
            max="500"
            value={maxRadiusKm}
            onChange={(e) => setMaxRadiusKm(e.target.value)}
            style={{ width: "100%", padding: "8px" }}
          />
          <div className="button-row" style={{ marginTop: "8px" }}>
            <button type="button" onClick={useCurrentLocation}>Use Current Location</button>
            {latitude !== null && (
              <button
                className="secondary"
                type="button"
                onClick={() => {
                  setLatitude(null);
                  setLongitude(null);
                  setLocationStatus("Using city matching");
                }}
              >
                Clear Precise Location
              </button>
            )}
          </div>
          {locationStatus && <p>{locationStatus}</p>}
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
          <label>Your City</label>
          <br />
          <select
            value={locationId}
            onChange={(e) => setLocationId(e.target.value)}
            required
            style={{ width: "100%", padding: "8px" }}
          >
            <option value="">Select your city...</option>
            {locations.map((location) => (
              <option key={location.id} value={location.id}>
                {location.city}, {location.country}
              </option>
            ))}
          </select>
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
