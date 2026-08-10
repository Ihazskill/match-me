import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";

interface ConnectionRequest {
  connectionId: number;
  requesterId: number;
  requesterName: string;
  requesterProfilePicture: string | null;
  requestedAt: string;
}

export default function ConnectionRequests() {
  const [requests, setRequests] = useState<ConnectionRequest[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    api.get<ConnectionRequest[]>("/connections/pending")
      .then((response) => setRequests(response.data))
      .catch(() => setError("Unable to load connection requests"));
  }, []);

  const respond = async (connectionId: number, action: "accept" | "reject") => {
    await api.post(`/connections/${connectionId}/${action}`);
    setRequests((previous) => previous.filter((request) => request.connectionId !== connectionId));
  };

  return (
    <main className="page-shell">
      <h1>Connection Requests</h1>
      {error && <p className="error-text">{error}</p>}
      {!error && requests.length === 0 && <p>No pending requests.</p>}
      <div className="list-stack">
        {requests.map((request) => (
          <article className="list-row" key={request.connectionId}>
            <Link className="list-row-body" to={`/users/${request.requesterId}`}>
              <strong>{request.requesterName}</strong>
              <time>{new Date(request.requestedAt).toLocaleString()}</time>
            </Link>
            <div className="button-row">
              <button onClick={() => void respond(request.connectionId, "accept")}>Accept</button>
              <button className="secondary" onClick={() => void respond(request.connectionId, "reject")}>Dismiss</button>
            </div>
          </article>
        ))}
      </div>
    </main>
  );
}