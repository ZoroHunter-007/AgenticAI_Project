import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { sendMessageApi, getSessionMessagesApi } from '../../services/api';
import MessageBubble from './MessageBubble';

export default function ActiveChatView({
  user, messages, sessionId, sessions,
  onNewMessage, onAiReply, onSelectSession, onNewChat, loadingSessions
}) {
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [activeSessionId, setActiveSessionId] = useState(sessionId);
  const [showScrollBtn, setShowScrollBtn] = useState(false); // ✅ NEW
  const messagesEndRef = useRef(null);
  const messagesContainerRef = useRef(null); // ✅ NEW
  const { logout } = useAuth();
  const navigate = useNavigate();

  // Auto scroll only if user is near bottom
  useEffect(() => {
    const container = messagesContainerRef.current;
    if (!container) return;
    const isNearBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 150;
    if (isNearBottom) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  // ✅ Show/hide scroll button based on scroll position
  const handleScroll = () => {
    const container = messagesContainerRef.current;
    if (!container) return;
    const distanceFromBottom = container.scrollHeight - container.scrollTop - container.clientHeight;
    setShowScrollBtn(distanceFromBottom > 200);
  };

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  const handleSend = async () => {
    const text = input.trim();
    if (!text || loading) return;
    setInput('');
    setLoading(true);
    onNewMessage(text);
    try {
      const data = await sendMessageApi(text, activeSessionId);
      if (data.sessionId) setActiveSessionId(data.sessionId);
      onAiReply(data.reply);
    } catch (e) {
      onAiReply('Gemini API error. Please try again later.');
    } finally {
      setLoading(false);
    }
  };

 const handleSelectSession = async (s) => {
  try {
   
    const id = s.sessionId || s.id || s.chatId;
    const msgs = await getSessionMessagesApi(id);
    setActiveSessionId(id);
    onSelectSession(s, msgs);
  } catch (e) {
    console.error('Failed to load session', e);
  }
};

  const handleLogout = async () => {
  try {
    await logout();
  } catch (e) {
    // ignore — backend session cleared regardless
  } finally {
    logout(); // ✅ always clear frontend state
    navigate('/login'); // ✅ always redirect
  }
};
const handleDeleteSession = async (id) => {
  try {
    await deleteChatSessionApi(id);
    // If deleted session is currently active → go to welcome
    if (id === activeSessionId) onNewChat();
    // Refresh sessions list
    loadSessions(); 
  } catch (e) {
    console.error('Delete failed:', e);
  }
};

  const firstName = user?.displayName?.split(' ')[0] || user?.name?.split(' ')[0] || 'User';

  return (
    <div className="chat-layout">
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="sidebar-logo">🤖 Agentic AI</div>
          <button className="new-chat-btn" onClick={onNewChat}>+ New Chat</button>
        </div>
        <div className="sidebar-section-label">Your Chats</div>
        <div className="sidebar-sessions">
          {loadingSessions ? (
            <p className="sidebar-empty">Loading...</p>
          ) : sessions.length === 0 ? (
            <p className="sidebar-empty">No chats yet</p>
          ) : (
           sessions.map((s, index) => (
            <div key={s.sessionId || s.id || index}
              className={`session-item ${(s.sessionId || s.id) === activeSessionId ? 'active' : ''}`}
              onClick={() => handleSelectSession(s)}>
              <span className="session-title">{s.title || 'New Chat'}</span>
              <button
                className="delete-session-btn"
                onClick={async (e) => {
                  e.stopPropagation(); // ✅ don't trigger session click
                  await handleDeleteSession(s.sessionId || s.id);
                }}
              >
                ✕
              </button>
            </div>
          ))
          )}
        </div>
        <div className="sidebar-user">
         {(user?.profileImage || user?.profilePicture) 
            ? <img src={user.profileImage || user.profilePicture} alt="avatar" className="user-avatar" />
            : <div className="user-avatar-placeholder">{firstName[0]}</div>
          }
          <div>
            <div className="user-name">Welcome {user?.name}</div>
            <button className="logout-btn" onClick={handleLogout}>Logout</button>
          </div>
        </div>
      </aside>

      {/* Chat Area */}
      <main className="chat-main">
        {/* ✅ Add ref and onScroll here */}
        <div
          className="messages-container"
          ref={messagesContainerRef}
          onScroll={handleScroll}
        >
          {messages.map((msg, i) => (
            <MessageBubble key={i} role={msg.role} content={msg.content} />
          ))}
          {loading && (
            <div className="message-row assistant-row">
              <div className="assistant-icon">🤖</div>
              <div className="message-bubble assistant-bubble typing">
                <span className="dot" /><span className="dot" /><span className="dot" />
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        {/* ✅ Scroll to bottom button */}
        {showScrollBtn && (
          <button className="scroll-to-bottom" onClick={scrollToBottom}>
            ↓
          </button>
        )}

        {/* Input */}
        <div className="chat-input-wrapper">
          <div className="chat-input-box">
            <textarea
              className="chat-textarea"
              placeholder="Message Agentic AI..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); }
              }}
              rows={1}
              disabled={loading}
            />
            <button className="send-btn" onClick={handleSend} disabled={!input.trim() || loading}>▶</button>
          </div>
        </div>
      </main>
    </div>
  );
}