import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function OAuth2Success() {
  const navigate = useNavigate();
  const { login } = useAuth();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const name = params.get('name');
    const email = params.get('email');
    const profilePicture = params.get('profilePicture');

    if (token) {
      login(token, { name, email, profilePicture });
      navigate('/chat', { replace: true });
    } else {
      navigate('/login', { replace: true });
    }
  }, []);

  return (
    <div className="loading-screen">
      <div className="loader" />
      <p style={{ color: '#a78bfa', marginTop: '1rem', fontFamily: 'sans-serif' }}>
        Signing you in...
      </p>
    </div>
  );
}
