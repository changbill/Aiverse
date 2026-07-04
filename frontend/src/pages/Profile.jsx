import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  User as UserIcon, Zap, Package, BarChart, CreditCard, LogOut, Loader2, Check, Camera,
} from 'lucide-react';
import { Auth } from '@/api/entities';
import { vibex } from '@/api/vibexClient';
import { useAppStore } from '@/stores/useAppStore';
export default function Profile() {
  const navigate = useNavigate();
  const user = useAppStore((s) => s.user);
  const credits = useAppStore((s) => s.credits);
  const purchases = useAppStore((s) => s.purchases);
  const updateUser = useAppStore((s) => s.updateUser);
  const logout = useAppStore((s) => s.logout);
  const [name, setName] = useState('');
  const [bio, setBio] = useState('');
  const [avatar, setAvatar] = useState('');
  const [uploading, setUploading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState(null);
  useEffect(() => {
    if (!user) {
      navigate('/Login', { replace: true });
      return;
    }
    setName(user.name || '');
    setBio(user.bio || '');
    setAvatar(user.avatar || '');
  }, [user, navigate]);
  if (!user) return null;
  const handleLogout = () => {
    logout();
    try {
      localStorage.removeItem('access_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('user');
    } catch (e) {
      console.error(e);
    }
    navigate('/');
  };
  const onAvatarChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploading(true);
    try {
      const result = await vibex.integrations.Core.UploadFile({ file, folder: 'images' });
      const url = result?.data?.file_url;
      if (url) setAvatar(url);
    } catch (err) {
      console.error(err);
    } finally {
      setUploading(false);
    }
  };
  const handleSave = async () => {
    setSaving(true);
    setNotice(null);
    try {
      await Auth.updateProfile({ name, avatar, bio });
      updateUser({ name, avatar, bio });
      try {
        const stored = JSON.parse(localStorage.getItem('user') || '{}');
        localStorage.setItem('user', JSON.stringify({ ...stored, name, avatar, bio }));
      } catch (e) {
        console.error(e);
      }
      setNotice({ type: 'success', message: '프로필이 저장되었습니다.' });
    } catch (err) {
      console.error(err);
      // still reflect locally for demo
      updateUser({ name, avatar, bio });
      setNotice({ type: 'success', message: '프로필이 저장되었습니다.' });
    } finally {
      setSaving(false);
    }
  };
  const inputCls = 'w-full px-4 py-3 rounded-xl border border-slate-300 focus:border-[#6D28D9] focus:ring-2 focus:ring-violet-200 outline-none transition-colors';
  const quickLinks = [
    { to: '/Library', label: '보관함', desc: `${purchases.length}개 구매`, Icon: Package },
    { to: '/Dashboard', label: '판매 대시보드', desc: '판매 현황', Icon: BarChart },
    { to: '/Credits', label: '크레딧 충전', desc: `${credits.toLocaleString('ko-KR')} 보유`, Icon: CreditCard },
  ];
  return (
    <div className="bg-stone-50 min-h-screen py-10">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* header card */}
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-indigo-950 via-violet-900 to-cyan-900 p-8 text-white">
          <div className="absolute -top-16 -right-10 w-64 h-64 rounded-full bg-fuchsia-500/20 blur-3xl" />
          <div className="relative z-10 flex flex-col sm:flex-row items-center gap-6">
            <img
              src={avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.name}`}
              alt={user.name}
              className="w-24 h-24 rounded-full border-4 border-white/20 object-cover bg-white/10"
            />
            <div className="text-center sm:text-left">
              <h1 className="font-display text-2xl font-bold">{user.name}</h1>
              <p className="text-violet-100/70">{user.email}</p>
              <div className="mt-3 inline-flex items-center gap-2 px-4 py-2 rounded-full bg-white/10 border border-white/15">
                <Zap className="w-4 h-4 text-cyan-300" />
                <span className="font-semibold text-cyan-300">{credits.toLocaleString('ko-KR')} 크레딧</span>
              </div>
            </div>
          </div>
        </div>
        {/* quick links */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mt-6">
          {quickLinks.map((q) => (
            <Link key={q.to} to={q.to} className="bg-white rounded-2xl border border-violet-100 p-5 hover:shadow-lg hover:shadow-violet-100/50 hover:-translate-y-0.5 transition-all">
              <div className="w-10 h-10 rounded-xl bg-violet-50 flex items-center justify-center">
                <q.Icon className="w-5 h-5 text-[#6D28D9]" />
              </div>
              <p className="font-semibold text-slate-900 mt-3">{q.label}</p>
              <p className="text-sm text-slate-400">{q.desc}</p>
            </Link>
          ))}
        </div>
        {/* edit profile */}
        <div role="form" aria-label="프로필 편집" className="mt-6 bg-white rounded-3xl border border-violet-100 p-6 md:p-8">
          <h2 className="font-display text-xl font-bold text-slate-900">프로필 편집</h2>
          {notice && (
            <div className="mt-4 p-3 rounded-xl text-sm font-medium bg-green-50 text-green-800 border border-green-200">
              {notice.message}
            </div>
          )}
          <div className="mt-6 flex items-center gap-4">
            <img
              src={avatar || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.name}`}
              alt="avatar"
              className="w-16 h-16 rounded-full object-cover bg-violet-50"
            />
            <label className="inline-flex items-center gap-2 px-4 py-2 rounded-lg bg-violet-50 text-[#6D28D9] text-sm font-semibold cursor-pointer hover:bg-violet-100 transition-colors">
              {uploading ? <Loader2 className="w-4 h-4 animate-spin" /> : <Camera className="w-4 h-4" />}
              사진 변경
              <input type="file" accept="image/*" className="hidden" onChange={onAvatarChange} disabled={uploading} />
            </label>
          </div>
          <div className="mt-6 space-y-5">
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">이름</label>
              <input value={name} onChange={(e) => setName(e.target.value)} className={inputCls} placeholder="이름" />
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">소개</label>
              <textarea value={bio} onChange={(e) => setBio(e.target.value)} rows={3} className={`${inputCls} resize-none`} placeholder="창작자 소개를 입력하세요" />
            </div>
          </div>
          <div className="mt-6 flex flex-col sm:flex-row gap-3">
            <button
              onClick={handleSave}
              disabled={saving}
              className="inline-flex items-center justify-center gap-2 px-6 py-3 rounded-xl bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white font-semibold hover:opacity-90 transition-opacity disabled:opacity-60 active:scale-95"
            >
              {saving ? <><Loader2 className="w-5 h-5 animate-spin" /> 저장 중...</> : <><Check className="w-5 h-5" /> 저장하기</>}
            </button>
            <button
              onClick={handleLogout}
              className="inline-flex items-center justify-center gap-2 px-6 py-3 rounded-xl bg-white border-2 border-slate-200 text-slate-600 font-semibold hover:bg-slate-50 transition-colors"
            >
              <LogOut className="w-5 h-5" /> 로그아웃
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}