import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { sendMessageApi, getSessionMessagesApi } from '../../services/api';

const QUICK_PROMPTS = [
  { icon: '⚡', label: 'What can you do?' },
  { icon: '💡', label: 'Help me think' },
  { icon: '🔍', label: 'Search something' },
  { icon: '🛠️', label: 'Build a plan' },
  { icon: '📝', label: 'Write for me' },
];

export default function WelcomeView({ user, sessions, onFirstMessage, onSelectSession, onNewChat, loadingSessions, onDeleteSession }) {
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleSend = async (msg) => {
    const text = msg || input.trim();
    if (!text || loading) return;
    setLoading(true);
    try {
      const data = await sendMessageApi(text, null);
      onFirstMessage(text, data.reply, data.sessionId);
    } catch (e) {
      onFirstMessage(text, 'Gemini API error. Please try again later.', null);
    } finally {
      setLoading(false);
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
  
const firstName = user?.displayName?.split(' ')[0] || user?.name?.split(' ')[0] || 'User';

  return (
    <div className="welcome-layout">
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
                  className="session-item"  // ✅ no active class check in WelcomeView
                  onClick={async () => {
                    const id = s.sessionId || s.id;
                    const msgs = await getSessionMessagesApi(id);
                    onSelectSession(s, msgs);
                  }}>
                  <span className="session-title">{s.title || 'New Chat'}</span>
                  <button
                    className="delete-session-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      onDeleteSession(s.sessionId || s.id); // ✅ just call prop directly
                    }}
                  >
                    ✕
                  </button>
                </div>
              ))
          )}
        </div>
        <div className="sidebar-user">
          {user?.profilePicture
            ? <img src={user.profilePicture} alt="avatar" className="user-avatar" />
            : <div className="user-avatar-placeholder">{firstName[0]}</div>
          }
          <div>
            <div className="user-name">Welcome {user?.name}</div>
            <button className="logout-btn" onClick={handleLogout}>Logout</button>
          </div>
        </div>
      </aside>

      {/* Welcome Center */}
      <main className="welcome-main">
        <div className="welcome-content">
          <div className="welcome-heading">
            <span className="jarvis-icon">🤖</span>
            <h1>Welcome, {firstName}</h1>
          </div>

          <div className="welcome-input-box">
            <textarea
              className="welcome-textarea"
              placeholder="How can I help you today?"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); }
              }}
              rows={2}
              disabled={loading}
            />
            <div className="welcome-input-footer">
              <button className="send-btn" onClick={() => handleSend()} disabled={!input.trim() || loading}>
                {loading ? '...' : '▶'}
              </button>
            </div>
          </div>

          <div className="quick-chips">
            {QUICK_PROMPTS.map((p) => (
              <button key={p.label} className="quick-chip" onClick={() => handleSend(p.label)} disabled={loading}>
                {p.icon} {p.label}
              </button>
            ))}
          </div>
        </div>
      </main>
    </div>
  );
}
