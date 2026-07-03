import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Mail, User as UserIcon, Eye, EyeOff, Loader2, Check } from 'lucide-react';
import { Auth } from '@/api/entities';
import { useAppStore } from '@/stores/useAppStore';
const LOGO = 'https://cdn.vibe-x.app/apps/850e38c8961e5c6070a133d5/assets/original/logo-0-67018.png';
const validatePassword = (pw) => {
  if (pw.length < 8) return '비밀번호는 8자 이상이어야 합니다';
  if (!/[0-9]/.test(pw)) return '숫자를 1개 이상 포함해야 합니다';
  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pw)) return '특수문자를 1개 이상 포함해야 합니다';
  return null;
};
export default function Register() {
  const navigate = useNavigate();
  const loginUser = useAppStore((s) => s.loginUser);
  const [form, setForm] = useState({ name: '', email: '', password: '', passwordConfirm: '' });
  const [touched, setTouched] = useState({});
  const [showPw, setShowPw] = useState(false);
  const [loading, setLoading] = useState(false);
  const [serverError, setServerError] = useState('');
  const setField = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  const markTouched = (key) => setTouched((t) => ({ ...t, [key]: true }));
  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email);
  const errors = {
    name: form.name.trim().length < 2 ? '이름을 2자 이상 입력해주세요' : null,
    email: !emailValid ? '올바른 이메일 형식이 아닙니다' : null,
    password: validatePassword(form.password),
    passwordConfirm: form.password !== form.passwordConfirm ? '비밀번호가 일치하지 않습니다' : null,
  };
  const isValid = !errors.name && !errors.email && !errors.password && !errors.passwordConfirm &&
    form.name && form.email && form.password && form.passwordConfirm;
  const handleSubmit = async () => {
    setTouched({ name: true, email: true, password: true, passwordConfirm: true });
    if (!isValid) return;
    setLoading(true);
    setServerError('');
    try {
      await Auth.register({ email: form.email, password: form.password, name: form.name });
      // auto-login
      const res = await Auth.login({ email: form.email, password: form.password });
      const data = res.data.data;
      localStorage.setItem('access_token', data.token);
      localStorage.setItem('user', JSON.stringify(data.user));
      loginUser(data.user);
      navigate('/');
    } catch (err) {
      console.error(err);
      setServerError('회원가입에 실패했습니다. 이미 사용 중인 이메일일 수 있어요.');
    } finally {
      setLoading(false);
    }
  };
  const inputCls = (key) =>
    `w-full px-4 py-3 rounded-xl border outline-none transition-colors focus:ring-2 ${
      touched[key] && errors[key]
        ? 'border-red-500 focus:border-red-500 focus:ring-red-200'
        : 'border-slate-300 focus:border-[#6D28D9] focus:ring-violet-200'
    }`;
  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-950 via-violet-900 to-cyan-900 overflow-hidden relative px-4 py-12">
      <div className="absolute -top-24 -right-24 w-96 h-96 rounded-full bg-fuchsia-500/25 blur-3xl" />
      <div className="absolute bottom-0 -left-20 w-80 h-80 rounded-full bg-cyan-500/25 blur-3xl" />
      <div className="relative z-10 w-full max-w-md">
        <div className="text-center mb-8">
          <Link to="/" className="inline-block">
            <img src={LOGO} alt="Aiverse" className="h-11 w-auto object-contain mx-auto" />
          </Link>
          <h1 className="font-display text-2xl font-bold text-white mt-6">Aiverse 시작하기</h1>
          <p className="text-violet-100/70 mt-2">계정을 만들고 AI 창작물을 등록·구매하세요.</p>
        </div>
        <div role="form" aria-label="회원가입" className="bg-white rounded-3xl p-8 shadow-2xl">
          {serverError && (
            <div className="mb-5 p-3 rounded-xl text-sm font-medium bg-red-50 text-red-700 border border-red-200">
              {serverError}
            </div>
          )}
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">이름</label>
              <div className="relative">
                <UserIcon className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                <input
                  value={form.name}
                  onChange={(e) => setField('name', e.target.value)}
                  onBlur={() => markTouched('name')}
                  placeholder="이름"
                  className={`${inputCls('name')} pl-12`}
                />
              </div>
              {touched.name && errors.name && <p className="text-xs text-red-500 mt-1">{errors.name}</p>}
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">이메일</label>
              <div className="relative">
                <Mail className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                <input
                  type="email"
                  value={form.email}
                  onChange={(e) => setField('email', e.target.value)}
                  onBlur={() => markTouched('email')}
                  placeholder="이메일 주소"
                  className={`${inputCls('email')} pl-12`}
                />
              </div>
              {touched.email && errors.email && <p className="text-xs text-red-500 mt-1">{errors.email}</p>}
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">비밀번호</label>
              <div className="relative">
                <input
                  type={showPw ? 'text' : 'password'}
                  value={form.password}
                  onChange={(e) => setField('password', e.target.value)}
                  onBlur={() => markTouched('password')}
                  placeholder="8자 이상, 숫자/특수문자 포함"
                  className={`${inputCls('password')} pr-12`}
                />
                <button
                  onClick={() => setShowPw((v) => !v)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                  aria-label="비밀번호 표시"
                >
                  {showPw ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
              {touched.password && errors.password && <p className="text-xs text-red-500 mt-1">{errors.password}</p>}
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">비밀번호 확인</label>
              <input
                type={showPw ? 'text' : 'password'}
                value={form.passwordConfirm}
                onChange={(e) => setField('passwordConfirm', e.target.value)}
                onBlur={() => markTouched('passwordConfirm')}
                onKeyDown={(e) => { if (e.key === 'Enter' && !e.nativeEvent.isComposing) handleSubmit(); }}
                placeholder="비밀번호를 다시 입력해 주세요"
                className={inputCls('passwordConfirm')}
              />
              {touched.passwordConfirm && errors.passwordConfirm && <p className="text-xs text-red-500 mt-1">{errors.passwordConfirm}</p>}
            </div>
          </div>
          <button
            onClick={handleSubmit}
            disabled={loading || !isValid}
            className="w-full mt-6 inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white font-semibold hover:opacity-90 transition-opacity disabled:opacity-50 active:scale-95"
          >
            {loading ? <><Loader2 className="w-5 h-5 animate-spin" /> 가입 중...</> : <><Check className="w-5 h-5" /> 회원가입</>}
          </button>
          <p className="text-center text-sm text-slate-500 mt-6">
            이미 계정이 있으신가요?{' '}
            <Link to="/Login" className="font-semibold text-[#6D28D9] hover:underline">로그인</Link>
          </p>
        </div>
      </div>
    </div>
  );
}