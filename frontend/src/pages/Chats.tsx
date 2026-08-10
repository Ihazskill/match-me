import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";
import { connectRealtime, MESSAGE_EVENT, PRESENCE_EVENT, READ_EVENT } from "../api/realtime";

export interface ChatPreview {
  chatId: number;
  otherUserId: number;
  otherUserName: string;
  otherUserProfilePicture: string | null;
  lastMessage: string | null;
  lastMessageAt: string;
  unreadCount: number;
  otherUserOnline: boolean;
}

export default function Chats() {
  const [chats, setChats] = useState<ChatPreview[]>([]);
  const [error, setError] = useState("");

  const load = async () => {
    try {
      const response = await api.get<ChatPreview[]>("/chats");
      setChats(response.data);
      setError("");
    } catch {
      setError("Unable to load chats");
    }
  };

  useEffect(() => {
    connectRealtime();
    void load();
    const reload = () => void load();
    window.addEventListener(MESSAGE_EVENT, reload);
    window.addEventListener(READ_EVENT, reload);
    window.addEventListener(PRESENCE_EVENT, reload);
    return () => {
      window.removeEventListener(MESSAGE_EVENT, reload);
      window.removeEventListener(READ_EVENT, reload);
      window.removeEventListener(PRESENCE_EVENT, reload);
    };
  }, []);

  return (
    <main className="page-shell">
      <h1>Chats</h1>
      {error && <p className="error-text">{error}</p>}
      {!error && chats.length === 0 && <p>No chats yet. Open a connection to start one.</p>}
      <div className="list-stack">
        {chats.map((chat) => (
          <Link className="list-row" key={chat.chatId} to={`/chats/${chat.chatId}`}>
            <div className="avatar">
              {chat.otherUserProfilePicture ? (
                <img src={chat.otherUserProfilePicture} alt="" />
              ) : (
                <span aria-hidden="true">👤</span>
              )}
              <i className={chat.otherUserOnline ? "presence online" : "presence"} />
            </div>
            <div className="list-row-body">
              <strong>{chat.otherUserName}</strong>
              <span>{chat.lastMessage || "Start a conversation"}</span>
            </div>
            <div className="list-row-meta">
              <time>{new Date(chat.lastMessageAt).toLocaleString()}</time>
              {chat.unreadCount > 0 && <b className="unread-badge">{chat.unreadCount}</b>}
            </div>
          </Link>
        ))}
      </div>
    </main>
  );
}