import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import WelcomeView from '../components/chat/WelcomeView';
import ActiveChatView from '../components/chat/ActiveChatView';
import { getChatHistoryApi, deleteChatSessionApi } from '../services/api.js'; // ✅ add import

export default function ChatPage() {
  const { user } = useAuth();
  const [chatStarted, setChatStarted] = useState(false);
  const [messages, setMessages] = useState([]);
  const [sessionId, setSessionId] = useState(null);
  const [sessions, setSessions] = useState([]);
  const [loadingSessions, setLoadingSessions] = useState(true);

  useEffect(() => { loadSessions(); }, []);

  const loadSessions = async () => {
    try {
      const data = await getChatHistoryApi();
      setSessions(data);
    } catch (e) {
      console.error('Failed to load sessions:', e);
    } finally {
      setLoadingSessions(false);
    }
  };

  const handleFirstMessage = (msg, aiReply, newSessionId) => {
    setMessages([
      { role: 'user', content: msg },
      { role: 'assistant', content: aiReply },
    ]);
    setSessionId(newSessionId);
    setChatStarted(true);
    loadSessions();
  };

  const handleNewMessage = (userMsg) => {
    setMessages((prev) => [...prev, { role: 'user', content: userMsg }]);
  };

  const handleAiReply = (aiReply) => {
    setMessages((prev) => [...prev, { role: 'assistant', content: aiReply }]);
    loadSessions();
  };

  const handleSelectSession = (session, msgs) => {
    const normalized = msgs.map(m => ({
      role: m.role === 'USER' ? 'user' : 'assistant',
      content: m.content,
    }));
    setMessages(normalized);
    setSessionId(session.sessionId || session.id);
    setChatStarted(true);
  };

  const handleNewChat = () => {
    setMessages([]);
    setSessionId(null);
    setChatStarted(false);
  };

  // ✅ Fixed — uses correct scope variables
  const handleDeleteSession = async (id) => {
    try {
      await deleteChatSessionApi(id);
      if (id === sessionId) handleNewChat(); // ✅ sessionId not activeSessionId
      loadSessions(); // ✅ refresh sidebar
    } catch (e) {
      console.error('Delete failed:', e);
    }
  };

  return chatStarted ? (
    <ActiveChatView
      user={user}
      messages={messages}
      sessionId={sessionId}
      sessions={sessions}
      onNewMessage={handleNewMessage}
      onAiReply={handleAiReply}
      onSelectSession={handleSelectSession}
      onNewChat={handleNewChat}
      loadingSessions={loadingSessions}
      onDeleteSession={handleDeleteSession} // ✅ pass the function not loadSessions
    />
  ) : (
    <WelcomeView
      user={user}
      sessions={sessions}
      onFirstMessage={handleFirstMessage}
      onSelectSession={handleSelectSession}
      onNewChat={handleNewChat}
      loadingSessions={loadingSessions}
      onDeleteSession={handleDeleteSession} // ✅ pass to WelcomeView too
    />
  );
}