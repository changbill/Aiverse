import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, Eye, EyeOff, Loader2, LogIn } from 'lucide-react';
import { Auth } from '@/api/entities';
import { useAppStore } from '@/stores/useAppStore';
const LOGO = 'https://cdn.vibe-x.app/apps/850e38c8961e5c6070a133d5/assets/original/logo-0-67018.png';
export default function Login() {
  const navigate = useNavigate();
  const loginUser = useAppStore((s) => s.loginUser);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const handleSubmit = async () => {
    if (!email || !password) {
      setError('이메일과 비밀번호를 입력해주세요.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      const res = await Auth.login({ email, password });
      const data = res.data.data;
      const user = data.user;
      const token = data.token;
      localStorage.setItem('access_token', token);
      localStorage.setItem('user', JSON.stringify(user));
      loginUser(user);
      navigate('/');
    } catch (err) {
      console.error(err);
      setError('이메일 또는 비밀번호가 올바르지 않습니다.');
    } finally {
      setLoading(false);
    }
  };
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-950 via-violet-900 to-cyan-900 overflow-hidden relative px-4 py-12">
      <div className="absolute -top-24 -left-24 w-96 h-96 rounded-full bg-violet-500/30 blur-3xl" />
      <div className="absolute bottom-0 -right-20 w-80 h-80 rounded-full bg-cyan-500/25 blur-3xl" />
      <div className="relative z-10 w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-block">
            <img src={LOGO} alt="Aiverse" className="h-11 w-auto object-contain mx-auto" />
          </Link>
          <h1 className="font-display text-2xl font-bold text-white mt-6">다시 오신 것을 환영해요</h1>
          <p className="text-violet-100/70 mt-2">계정에 로그인하고 창작물을 만나보세요.</p>
        </div>
        <div role="form" aria-label="로그인" className="bg-white rounded-3xl p-8 shadow-2xl">
          {error && (
            <div className="mb-5 p-3 rounded-xl text-sm font-medium bg-red-50 text-red-700 border border-red-200">
              {error}
            </div>
          )}
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">이메일</label>
              <div className="relative">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter' && !e.nativeEvent.isComposing) handleSubmit(); }}
                  placeholder="이메일 주소를 입력해 주세요"
                  className="w-full pl-12 pr-4 py-3 rounded-xl border border-slate-300 focus:border-[#6D28D9] focus:ring-2 focus:ring-violet-200 outline-none transition-colors"
                />
              </div>
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">비밀번호</label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  onKeyDown={(e) => { if (e.key === 'Enter' && !e.nativeEvent.isComposing) handleSubmit(); }}
                  placeholder="비밀번호를 입력해 주세요"
                  className="w-full pl-4 pr-12 py-3 rounded-xl border border-slate-300 focus:border-[#6D28D9] focus:ring-2 focus:ring-violet-200 outline-none transition-colors"
                />
                <button
                  onClick={() => setShowPw((v) => !v)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                  aria-label="비밀번호 표시"
                >
                  {showPw ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
            </div>
          </div>
          <button
            onClick={handleSubmit}
            disabled={loading}
            className="w-full mt-6 inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white font-semibold hover:opacity-90 transition-opacity disabled:opacity-60 active:scale-95"
          >
            {loading ? <><Loader2 className="w-5 h-5 animate-spin" /> 로그인 중...</> : <><LogIn className="w-5 h-5" /> 로그인</>}
          </button>
          <p className="text-center text-sm text-slate-500 mt-6">
            아직 계정이 없으신가요?{' '}
            <Link to="/Register" className="font-semibold text-[#6D28D9] hover:underline">회원가입</Link>
          </p>
        </div>
      </div>
    </div>
  );
}