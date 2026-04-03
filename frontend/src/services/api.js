const BASE_URL = 'http://localhost:8081';

const getHeaders = () => {
  return {
    'Content-Type': 'application/json',
  };
};

// ─── AUTH ────────────────────────────────────────────────────────────────────

export const loginApi = async (email, password) => {
  const res = await fetch(`${BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: getHeaders(),
    credentials: 'include', // ✅ CRITICAL: Tells browser to save the login cookie
    body: JSON.stringify({ email, password }),
  });
  if (!res.ok) throw new Error((await res.json()).message || 'Login failed');
  return res.json(); 
};

export const logoutApi = async () => {
  await fetch(`${BASE_URL}/api/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  });
};

// ✅ NEW: Fetch the user details using the session cookie
export const getMeApi = async () => {
  const res = await fetch(`${BASE_URL}/api/auth/me`, {
    method: 'GET',
    headers: getHeaders(),
    credentials: 'include', 
  });
  if (!res.ok) throw new Error('Failed to fetch user');
  return res.json();
};

export const registerApi = async (formData) => {
  const res = await fetch(`${BASE_URL}/api/auth/register`, {
    method: 'POST',
    credentials: 'include',
    body: formData,
  });
  if (!res.ok) throw new Error((await res.json()).message || 'Registration failed');
   return res.text();
};

export const googleLoginApi = () => {
  window.location.href = `${BASE_URL}/oauth2/authorization/google`;
};

// ─── CHAT ────────────────────────────────────────────────────────────────────

export const sendMessageApi = async (message, sessionId) => {
  const res = await fetch(`${BASE_URL}/api/ai`, { // ✅ Change this from /api/chat/message
    method: 'POST',
    headers: getHeaders(),
    credentials: 'include',
    body: JSON.stringify({ message, sessionId }),
  });
  if (!res.ok) throw new Error('Failed to send message');
  return res.json(); 
};
// Change this line in api.js
export const getChatHistoryApi = async () => { // ✅ Added 'Api' to the end
  const res = await fetch(`${BASE_URL}/api/chats`, {
    method: 'GET',
    headers: getHeaders(),
    credentials: 'include',
  });
  if (!res.ok) throw new Error('Failed to fetch history');
  return res.json();
};

export const getSessionMessagesApi = async (sessionId) => {
  const res = await fetch(`${BASE_URL}/api/chats/${sessionId}/messages`, { // Adjusted to match your Spring controller
    method: 'GET',
    headers: getHeaders(),
    credentials: 'include', // ✅ CRITICAL
  });
  if (!res.ok) throw new Error('Failed to fetch messages');
  return res.json();
};
export const deleteChatSessionApi = async (sessionId) => {
  const res = await fetch(`${BASE_URL}/api/chats/${sessionId}`, {
    method: 'DELETE',
    credentials: 'include',
  });
  if (!res.ok) throw new Error('Failed to delete session');
};