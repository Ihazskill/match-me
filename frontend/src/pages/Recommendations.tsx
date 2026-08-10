import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";

interface Recommendation { id: number }
interface UserBasic {
  id: number;
  firstName: string;
  lastName: string;
  profilePictureUrl: string | null;
  age: number;
  online: boolean;
}

export default function Recommendations() {
  const [users, setUsers] = useState<UserBasic[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        const response = await api.get<Recommendation[]>("/recommendations");
        const details = await Promise.all(
          response.data.map(async ({ id }) => (await api.get<UserBasic>(`/users/${id}`)).data)
        );
        setUsers(details);
      } catch {
        setError("Unable to load recommendations");
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, []);

  const connect = async (id: number) => {
    await api.post(`/users/${id}/connect`);
    setUsers((previous) => previous.filter((user) => user.id !== id));
  };

  const dismiss = async (id: number) => {
    await api.post(`/users/${id}/dismiss`);
    setUsers((previous) => previous.filter((user) => user.id !== id));
  };

  return (
    <main className="page-shell">
      <h1>Recommendations</h1>
      {loading && <p>Finding strong matches...</p>}
      {error && <p className="error-text">{error}</p>}
      {!loading && !error && users.length === 0 && <p>No strong matches are available right now.</p>}
      <div className="profile-grid">
        {users.map((user) => (
          <article className="profile-card" key={user.id}>
            <Link to={`/users/${user.id}`}>
              {user.profilePictureUrl ? (
                <img src={user.profilePictureUrl} alt={`${user.firstName} ${user.lastName}`} />
              ) : (
                <div className="profile-placeholder" aria-label="No profile picture">👤</div>
              )}
              <h2>{user.firstName} {user.lastName}</h2>
            </Link>
            <p>{user.age} years old · {user.online ? "Online" : "Offline"}</p>
            <div className="button-row">
              <button onClick={() => void connect(user.id)}>Connect</button>
              <button className="secondary" onClick={() => void dismiss(user.id)}>Dismiss</button>
            </div>
          </article>
        ))}
      </div>
    </main>
  );
}