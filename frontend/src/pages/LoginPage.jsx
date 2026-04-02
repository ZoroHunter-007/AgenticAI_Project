import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { loginApi, googleLoginApi, getMeApi } from '../services/api';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async () => {
    if (!email || !password) return setError('Please fill all fields');
    setLoading(true);
    setError('');
    
    try {
      // 1. Hit the Spring Boot login endpoint (which sets the JSESSIONID cookie)
      await loginApi(email, password); 
      
      // 2. Fetch the user's profile data using that new session cookie
      const userData = await getMeApi(); 
      
      // 3. Save the user to AuthContext (pass null for token since we rely on the cookie)
      login(null, userData); 
      
      // 4. Redirect to the Chat page!
      navigate('/chat'); 
      
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        {/* LEFT — Login Form */}
        <div className="auth-form-panel">
          <h2 className="auth-title">Welcome Back 👋</h2>

          {error && <p className="auth-error">{error}</p>}

          <input
            className="auth-input"
            type="email"
            placeholder="Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
          />

          <div className="auth-input-wrapper">
            <input
              className="auth-input"
              type={showPass ? 'text' : 'password'}
              placeholder="Password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
            />
            <button className="eye-btn" onClick={() => setShowPass(!showPass)}>
              {showPass ? '🙈' : '👁️'}
            </button>
          </div>

          <button className="auth-btn-primary" onClick={handleLogin} disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>

          <div className="auth-divider"><span>OR</span></div>

          <button className="auth-btn-google" onClick={googleLoginApi}>
            <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="G" width={20} />
            Sign in with Google
          </button>
        </div>

        {/* RIGHT — Purple Panel */}
        <div className="auth-side-panel">
          <h3>Hello, Friend!</h3>
          <p>Enter your details and start journey with us</p>
          <Link to="/register">
            <button className="auth-btn-outline">Register</button>
          </Link>
        </div>
      </div>
    </div>
  );
}