import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { registerApi, googleLoginApi } from '../services/api';

export default function RegisterPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [profilePic, setProfilePic] = useState(null);
  const [showPass, setShowPass] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const [success, setSuccess] = useState(false);

  const handleRegister = async () => {
    if (!name || !email || !password) return setError('Please fill all fields');
    setLoading(true);
    setError('');
    try {
        const formData = new FormData();
        formData.append('displayName', name);
        formData.append('email', email);
        formData.append('password', password);
        if (profilePic) formData.append('profileImage', profilePic);
        await registerApi(formData);
        setSuccess(true);
        setTimeout(() => navigate('/login'), 2000);
    } catch (e) {
        setError(e.message);
    } finally {
        setLoading(false);
    }
};

  return (
    <div className="auth-wrapper">
      <div className="auth-card register-card">
        {/* LEFT — Purple Panel */}
        <div className="auth-side-panel">
          <h3>Welcome Back!</h3>
          <p>To keep connected with us please login</p>
          <Link to="/login">
            <button className="auth-btn-outline">Login</button>
          </Link>
        </div>

        {/* RIGHT — Register Form */}
        <div className="auth-form-panel">
          <h2 className="auth-title">Create Account 🚀</h2>

          {error && <p className="auth-error">{error}</p>}
          {success && (
              <p className="auth-success">
                ✅ Account created! Redirecting to login...
              </p>
            )}

          <input className="auth-input" type="text" placeholder="Full Name"
            value={name} onChange={(e) => setName(e.target.value)} />

          <input className="auth-input" type="email" placeholder="Email"
            value={email} onChange={(e) => setEmail(e.target.value)} />

          <div className="auth-input-wrapper">
            <input className="auth-input" type={showPass ? 'text' : 'password'} placeholder="Password"
              value={password} onChange={(e) => setPassword(e.target.value)} />
            <button className="eye-btn" onClick={() => setShowPass(!showPass)}>
              {showPass ? '🙈' : '👁️'}
            </button>
          </div>

          <div className="auth-file-input">
            <label>Profile Picture (optional)</label>
            <input type="file" accept="image/*" onChange={(e) => setProfilePic(e.target.files[0])} />
          </div>

          <button className="auth-btn-primary" onClick={handleRegister} disabled={loading}>
            {loading ? 'Creating account...' : 'Register'}
          </button>

          <div className="auth-divider"><span>OR</span></div>

          <button className="auth-btn-google" onClick={googleLoginApi}>
            <img src="https://www.gstatic.com/firebasejs/ui/2.0.0/images/auth/google.svg" alt="G" width={20} />
            Continue with Google
          </button>
        </div>
      </div>
    </div>
  );
}
