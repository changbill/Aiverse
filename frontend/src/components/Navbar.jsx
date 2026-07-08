import { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Menu, X, Zap, User, LogOut, Upload as UploadIcon, Package, BarChart, LogIn } from 'lucide-react';
import { useAppStore } from '@/stores/useAppStore';
const LOGO = 'https://cdn.vibe-x.app/apps/850e38c8961e5c6070a133d5/assets/original/logo-0-67018.png';
const navLinks = [
  { to: '/', label: '홈' },
  { to: '/Explore', label: '탐색' },
  { to: '/Credits', label: '크레딧' },
  { to: '/Dashboard', label: '판매 대시보드' },
];
export default function Navbar() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const user = useAppStore((s) => s.user);
  const credits = useAppStore((s) => s.credits);
  const logout = useAppStore((s) => s.logout);
  useEffect(() => {
    setOpen(false);
  }, [location.pathname]);
  const handleLogout = () => {
    logout();
    navigate('/');
  };
  return (
    <header className="sticky top-0 z-50 bg-[#16112E] border-b border-white/10">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-8">
            <Link to="/" className="flex items-center">
              <img src={LOGO} alt="Aiverse" className="h-9 w-auto object-contain" />
            </Link>
            <nav className="hidden lg:flex items-center gap-1">
              {navLinks.map((l) => (
                <Link
                  key={l.to}
                  to={l.to}
                  className="px-3 py-2 rounded-lg text-sm font-medium text-violet-100/80 hover:text-white hover:bg-white/10 transition-colors"
                >
                  {l.label}
                </Link>
              ))}
            </nav>
          </div>
          <div className="hidden lg:flex items-center gap-3">
            {user ? (
              <>
                <Link
                  to="/Credits"
                  className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white/10 text-sm font-semibold text-cyan-300 hover:bg-white/15 transition-colors"
                >
                  <Zap className="w-4 h-4" />
                  {credits.toLocaleString('ko-KR')}
                </Link>
                <Link
                  to="/Upload"
                  className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white text-sm font-semibold hover:opacity-90 transition-opacity"
                >
                  <UploadIcon className="w-4 h-4" /> 등록
                </Link>
                <Link to="/Profile" className="flex items-center">
                  <img
                    src={user.profileUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.nickname}`}
                    alt={user.nickname}
                    className="w-9 h-9 rounded-full border border-white/20 object-cover bg-white/10"
                  />
                </Link>
                <button
                  onClick={handleLogout}
                  className="p-2 rounded-lg text-violet-100/70 hover:text-white hover:bg-white/10 transition-colors"
                  aria-label="로그아웃"
                >
                  <LogOut className="w-5 h-5" />
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/Login"
                  className="px-4 py-2 rounded-lg text-sm font-semibold text-violet-100/90 hover:text-white hover:bg-white/10 transition-colors"
                >
                  로그인
                </Link>
                <Link
                  to="/Register"
                  className="px-4 py-2 rounded-lg bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white text-sm font-semibold hover:opacity-90 transition-opacity"
                >
                  회원가입
                </Link>
              </>
            )}
          </div>
          <button
            onClick={() => setOpen((v) => !v)}
            className="lg:hidden p-2 rounded-lg text-white hover:bg-white/10 transition-colors"
            aria-label="메뉴"
          >
            {open ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
          </button>
        </div>
      </div>
      {open && (
        <div className="lg:hidden border-t border-white/10 bg-[#16112E]">
          <div className="px-4 py-4 space-y-1">
            {user && (
              <div className="flex items-center justify-between px-3 py-3 mb-2 rounded-xl bg-white/5">
                <div className="flex items-center gap-3">
                  <img
                    src={user.profileUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.nickname}`}
                    alt={user.nickname}
                    className="w-10 h-10 rounded-full object-cover bg-white/10"
                  />
                  <div>
                    <p className="text-sm font-semibold text-white">{user.nickname}</p>
                    <p className="text-xs text-cyan-300 flex items-center gap-1">
                      <Zap className="w-3 h-3" /> {credits.toLocaleString('ko-KR')} 크레딧
                    </p>
                  </div>
                </div>
              </div>
            )}
            {navLinks.map((l) => (
              <Link
                key={l.to}
                to={l.to}
                className="block px-3 py-2.5 rounded-lg text-sm font-medium text-violet-100/80 hover:text-white hover:bg-white/10"
              >
                {l.label}
              </Link>
            ))}
            {user ? (
              <>
                <Link to="/Upload" className="flex items-center gap-2 px-3 py-2.5 rounded-lg text-sm font-medium text-violet-100/80 hover:text-white hover:bg-white/10">
                  <UploadIcon className="w-4 h-4" /> 창작물 등록
                </Link>
                <Link to="/Library" className="flex items-center gap-2 px-3 py-2.5 rounded-lg text-sm font-medium text-violet-100/80 hover:text-white hover:bg-white/10">
                  <Package className="w-4 h-4" /> 보관함
                </Link>
                <Link to="/Profile" className="flex items-center gap-2 px-3 py-2.5 rounded-lg text-sm font-medium text-violet-100/80 hover:text-white hover:bg-white/10">
                  <User className="w-4 h-4" /> 프로필
                </Link>
                <button onClick={handleLogout} className="w-full flex items-center gap-2 px-3 py-2.5 rounded-lg text-sm font-medium text-red-300 hover:bg-white/10">
                  <LogOut className="w-4 h-4" /> 로그아웃
                </button>
              </>
            ) : (
              <div className="grid grid-cols-2 gap-2 pt-2">
                <Link to="/Login" className="flex items-center justify-center gap-1 px-4 py-2.5 rounded-lg text-sm font-semibold text-white bg-white/10 hover:bg-white/15">
                  <LogIn className="w-4 h-4" /> 로그인
                </Link>
                <Link to="/Register" className="px-4 py-2.5 rounded-lg bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white text-sm font-semibold text-center">
                  회원가입
                </Link>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
}