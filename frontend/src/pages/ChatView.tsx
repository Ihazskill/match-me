import { FormEvent, useEffect, useRef, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api from "../api/axios";
import {
  connectRealtime,
  MESSAGE_EVENT,
  READ_EVENT,
  sendRealtimeMessage,
  sendTyping,
  TYPING_EVENT,
} from "../api/realtime";

interface ChatMessage {
  id: number;
  chatId: number;
  senderId: number;
  receiverId: number;
  content: string;
  sentAt: string;
  read: boolean;
}

interface MessagePage {
  content: ChatMessage[];
  number: number;
  last: boolean;
}

export default function ChatView() {
  const { chatId } = useParams<{ chatId: string }>();
  const id = Number(chatId);
  const currentUserId = Number(localStorage.getItem("userId"));
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [content, setContent] = useState("");
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [typing, setTypingState] = useState(false);
  const [error, setError] = useState("");
  const typingTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const messageEnd = useRef<HTMLDivElement | null>(null);

  const loadPage = async (pageNumber: number) => {
    const response = await api.get<MessagePage>(`/chats/${id}/messages?page=${pageNumber}&size=30`);
    const chronological = [...response.data.content].reverse();
    setMessages((previous) => pageNumber === 0 ? chronological : [...chronological, ...previous]);
    setPage(pageNumber);
    setHasMore(!response.data.last);
    window.dispatchEvent(new Event(READ_EVENT));
  };

  useEffect(() => {
    connectRealtime();
    void api.get<MessagePage>(`/chats/${id}/messages?page=0&size=30`)
      .then((response) => {
        setMessages([...response.data.content].reverse());
        setPage(0);
        setHasMore(!response.data.last);
        window.dispatchEvent(new Event(READ_EVENT));
      })
      .catch(() => setError("Unable to load this chat"));

    const onMessage = (event: Event) => {
      const message = (event as CustomEvent<ChatMessage>).detail;
      if (message.chatId !== id) return;
      setMessages((previous) => previous.some((item) => item.id === message.id)
        ? previous
        : [...previous, message]);
      if (message.receiverId === currentUserId) {
        void api.get(`/chats/${id}/messages?page=0&size=1`);
        window.dispatchEvent(new Event(READ_EVENT));
      }
    };
    const onTyping = (event: Event) => {
      const detail = (event as CustomEvent<{ chatId: number; typing: boolean }>).detail;
      if (detail.chatId === id) setTypingState(detail.typing);
    };
    window.addEventListener(MESSAGE_EVENT, onMessage);
    window.addEventListener(TYPING_EVENT, onTyping);
    return () => {
      window.removeEventListener(MESSAGE_EVENT, onMessage);
      window.removeEventListener(TYPING_EVENT, onTyping);
      sendTyping(id, false);
      if (typingTimer.current) clearTimeout(typingTimer.current);
    };
  }, [id, currentUserId]);

  const newestMessageId = messages[messages.length - 1]?.id;
  useEffect(() => {
    messageEnd.current?.scrollIntoView({ block: "end" });
  }, [newestMessageId]);

  const submit = async (event: FormEvent) => {
    event.preventDefault();
    const message = content.trim();
    if (!message) return;
    setContent("");
    sendTyping(id, false);
    if (!sendRealtimeMessage(id, message)) {
      await api.post(`/chats/${id}/messages`, { content: message });
    }
  };

  const updateContent = (value: string) => {
    setContent(value);
    sendTyping(id, value.length > 0);
    if (typingTimer.current) clearTimeout(typingTimer.current);
    typingTimer.current = setTimeout(() => sendTyping(id, false), 1500);
  };

  return (
    <main className="chat-shell">
      <header className="chat-toolbar">
        <Link to="/chats">Back to chats</Link>
        {hasMore && <button onClick={() => void loadPage(page + 1)}>Load older messages</button>}
      </header>
      <div className="message-list" aria-live="polite">
        {error && <p className="error-text">{error}</p>}
        {!error && messages.length === 0 && <p className="empty-chat">No messages yet. Say hello.</p>}
        {messages.map((message) => (
          <article className={message.senderId === currentUserId ? "message mine" : "message"} key={message.id}>
            <p>{message.content}</p>
            <time>{new Date(message.sentAt).toLocaleString()}</time>
          </article>
        ))}
        {typing && <p className="typing-status">Typing...</p>}
        <div ref={messageEnd} />
      </div>
      <form className="message-composer" onSubmit={submit}>
        <input
          aria-label="Message"
          value={content}
          onChange={(event) => updateContent(event.target.value)}
          placeholder="Write a message"
          maxLength={4000}
        />
        <button type="submit">Send</button>
      </form>
    </main>
  );
}