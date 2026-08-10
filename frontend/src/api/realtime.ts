import { Client } from "@stomp/stompjs";

export const MESSAGE_EVENT = "matchme:message";
export const TYPING_EVENT = "matchme:typing";
export const PRESENCE_EVENT = "matchme:presence";
export const READ_EVENT = "matchme:read";

let client: Client | null = null;

export const connectRealtime = () => {
  const token = localStorage.getItem("token");
  if (!token || client?.active) return client;

  client = new Client({
    brokerURL: "ws://localhost:8080/ws",
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 3000,
    onConnect: () => {
      client?.subscribe("/user/queue/messages", (frame) => {
        window.dispatchEvent(new CustomEvent(MESSAGE_EVENT, { detail: JSON.parse(frame.body) }));
      });
      client?.subscribe("/user/queue/typing", (frame) => {
        window.dispatchEvent(new CustomEvent(TYPING_EVENT, { detail: JSON.parse(frame.body) }));
      });
      client?.subscribe("/topic/presence", (frame) => {
        window.dispatchEvent(new CustomEvent(PRESENCE_EVENT, { detail: JSON.parse(frame.body) }));
      });
    },
  });
  client.activate();
  return client;
};

export const disconnectRealtime = () => {
  const activeClient = client;
  client = null;
  return activeClient?.deactivate();
};

export const sendRealtimeMessage = (chatId: number, content: string) => {
  if (!client?.connected) return false;
  client.publish({ destination: "/app/chat.send", body: JSON.stringify({ chatId, content }) });
  return true;
};

export const sendTyping = (chatId: number, typing: boolean) => {
  if (!client?.connected) return;
  client.publish({ destination: "/app/chat.typing", body: JSON.stringify({ chatId, typing }) });
};