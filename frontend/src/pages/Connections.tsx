import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../api/axios";

interface Connection { id: number }
interface UserBasic {
  id: number;
  firstName: string;
  lastName: string;
  profilePictureUrl: string | null;
  online: boolean;
}

export default function Connections() {
  const navigate = useNavigate();
  const [connections, setConnections] = useState<UserBasic[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const load = async () => {
      try {
        const response = await api.get<Connection[]>("/connections");
        const users = await Promise.all(
          response.data.map(async ({ id }) => (await api.get<UserBasic>(`/users/${id}`)).data)
        );
        setConnections(users);
      } catch {
        setError("Unable to load connections");
      }
    };
    void load();
  }, []);

  const disconnect = async (userId: number) => {
    await api.post(`/connections/${userId}/disconnect`);
    setConnections((previous) => previous.filter((user) => user.id !== userId));
  };

  const message = async (userId: number) => {
    const response = await api.post(`/chats/with/${userId}`);
    navigate(`/chats/${response.data.chatId}`);
  };

  return (
    <main className="page-shell">
      <h1>Connections</h1>
      {error && <p className="error-text">{error}</p>}
      {!error && connections.length === 0 && <p>You have no connections yet.</p>}
      <div className="list-stack">
        {connections.map((user) => (
          <article className="list-row" key={user.id}>
            <Link className="list-row-body" to={`/users/${user.id}`}>
              <strong>{user.firstName} {user.lastName}</strong>
              <span>{user.online ? "Online" : "Offline"}</span>
            </Link>
            <div className="button-row">
              <button onClick={() => void message(user.id)}>Message</button>
              <button className="secondary" onClick={() => void disconnect(user.id)}>Disconnect</button>
            </div>
          </article>
        ))}
      </div>
    </main>
  );
}