import { Link } from 'react-router-dom';
import { Instagram, Twitter, Youtube, Mail } from 'lucide-react';
const LOGO = 'https://cdn.vibe-x.app/apps/850e38c8961e5c6070a133d5/assets/original/logo-0-67018.png';
export default function Footer() {
  return (
    <footer className="bg-[#0D0A1F] text-slate-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="md:col-span-2">
            <img src={LOGO} alt="Aiverse" className="h-9 w-auto object-contain" />
            <p className="mt-4 text-sm text-slate-400 leading-relaxed max-w-sm">
              AI로 생성한 이미지 · 영상 · 음악을 등록하고, 크레딧으로 자유롭게 거래하는 AI 디지털 콘텐츠 마켓입니다.
            </p>
            <div className="flex items-center gap-3 mt-5">
              <a href="#" className="p-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors" aria-label="Instagram"><Instagram className="w-4 h-4" /></a>
              <a href="#" className="p-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors" aria-label="Twitter"><Twitter className="w-4 h-4" /></a>
              <a href="#" className="p-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors" aria-label="Youtube"><Youtube className="w-4 h-4" /></a>
              <a href="#" className="p-2 rounded-lg bg-white/5 hover:bg-white/10 transition-colors" aria-label="Mail"><Mail className="w-4 h-4" /></a>
            </div>
          </div>
          <div className="hidden md:block">
            <h4 className="font-display font-semibold text-white mb-4">둘러보기</h4>
            <ul className="space-y-2 text-sm text-slate-400">
              <li><Link to="/Explore" className="hover:text-white transition-colors">창작물 탐색</Link></li>
              <li><Link to="/Credits" className="hover:text-white transition-colors">크레딧 충전</Link></li>
              <li><Link to="/Upload" className="hover:text-white transition-colors">창작물 등록</Link></li>
              <li><Link to="/Dashboard" className="hover:text-white transition-colors">판매 대시보드</Link></li>
            </ul>
          </div>
          <div className="hidden md:block">
            <h4 className="font-display font-semibold text-white mb-4">회원</h4>
            <ul className="space-y-2 text-sm text-slate-400">
              <li><Link to="/Library" className="hover:text-white transition-colors">구매 보관함</Link></li>
              <li><Link to="/Profile" className="hover:text-white transition-colors">내 프로필</Link></li>
              <li><Link to="/Login" className="hover:text-white transition-colors">로그인</Link></li>
              <li><Link to="/Register" className="hover:text-white transition-colors">회원가입</Link></li>
            </ul>
          </div>
        </div>
        <div className="mt-10 pt-6 border-t border-white/10 flex flex-col sm:flex-row items-center justify-between gap-3">
          <p className="text-xs text-slate-500">© 2026 Aiverse. All rights reserved.</p>
          <p className="text-xs text-slate-500">본 서비스의 결제/거래는 데모용 목업으로 동작합니다.</p>
        </div>
      </div>
    </footer>
  );
}