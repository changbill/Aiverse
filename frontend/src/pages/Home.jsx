import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Image, Video, Music, ArrowRight, Search, CreditCard, Package, Upload as UploadIcon,
  Star, Zap, TrendingUp, Award, Check,
} from 'lucide-react';
import { contentApi } from '@/api/contentApi';
import ContentCard from '@/components/ContentCard';
import Reveal from '@/components/Reveal';
const HERO = 'https://cdn.vibe-x.app/apps/850e38c8961e5c6070a133d5/assets/original/hero-1-67018.png';
const PROD1 = 'https://cdn.vibe-x.app/apps/850e38c8961e5c6070a133d5/assets/original/product-3-67018.png';
const PROD2 = 'https://cdn.vibe-x.app/apps/850e38c8961e5c6070a133d5/assets/original/product-5-67018.png';
const contentTypes = [
  { type: 'image', label: '이미지', Icon: Image, desc: '컨셉아트 · 일러스트 · 포트레이트' },
  { type: 'video', label: '영상', Icon: Video, desc: '루프 · 모션 · 시네마틱' },
  { type: 'music', label: '음악', Icon: Music, desc: '앰비언트 · 로파이 · 사운드' },
];
const steps = [
  { Icon: Search, title: '탐색', desc: '유형·카테고리·태그로 원하는 AI 창작물을 찾아보세요.' },
  { Icon: CreditCard, title: '크레딧 충전', desc: '결제 성공 시 크레딧이 즉시 충전됩니다.' },
  { Icon: Zap, title: '구매', desc: '보유 크레딧으로 콘텐츠를 구매하면 접근 권한이 생성됩니다.' },
  { Icon: Package, title: '보관 · 다운로드', desc: '구매 보관함에서 라이선스와 함께 다운로드하세요.' },
];
const creators = [
  { name: 'Admin', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', works: 2, sales: 46, specialty: '판타지 · 인물' },
  { name: '박지호', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=jiho', works: 1, sales: 52, specialty: '앰비언트 사운드' },
  { name: '정예준', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=yejun', works: 1, sales: 41, specialty: '로파이 · 음악' },
  { name: '이서연', avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=seoyeon', works: 1, sales: 18, specialty: 'AI 영상' },
];
const stats = [
  { value: '2,400+', label: '누적 거래 크레딧' },
  { value: '6', label: '등록 창작물' },
  { value: '5', label: '활동 창작자' },
  { value: '99%', label: '라이선스 명시율' },
];
export default function Home() {
  const [trending, setTrending] = useState([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    (async () => {
      setLoading(true);
      try {
        const res = await contentApi.list({ page: 1, limit: 6, sort: '-views' });
        setTrending(res.items);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    })();
  }, []);
  return (
    <div>
      {/* ===== HERO (gradient-float) ===== */}
      <section className="relative overflow-hidden bg-gradient-to-br from-indigo-950 via-violet-900 to-cyan-900">
        <div className="absolute -top-24 -left-24 w-96 h-96 rounded-full bg-violet-500/30 blur-3xl animate-pulse" />
        <div className="absolute top-40 -right-24 w-80 h-80 rounded-full bg-cyan-500/25 blur-3xl" />
        <div className="absolute bottom-0 left-1/3 w-72 h-72 rounded-full bg-fuchsia-500/20 blur-3xl" />
        <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-14 pb-20 md:pt-20 md:pb-28 flex flex-col md:flex-row items-center gap-12">
          <div className="w-full md:w-1/2 text-center md:text-left">
            <span className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full bg-white/10 border border-white/15 text-xs font-semibold text-cyan-200 backdrop-blur">
              <Star className="w-3.5 h-3.5" /> AI 디지털 콘텐츠 마켓
            </span>
            <h1 className="font-display text-4xl md:text-5xl lg:text-6xl font-bold text-white leading-tight mt-6">
              AI가 만든 창작물,<br />
              <span className="bg-gradient-to-r from-fuchsia-300 to-cyan-300 bg-clip-text text-transparent">성운처럼 무한히</span> 확장되다
            </h1>
            <p className="text-lg text-violet-100/85 mt-6 leading-relaxed max-w-xl mx-auto md:mx-0">
              이미지 · 영상 · 음악을 등록하고, 크레딧으로 자유롭게 거래하세요. 창작자는 판매하고, 구매자는 필요한 창작물을 빠르게 찾습니다.
            </p>
            <div className="mt-8 flex flex-col sm:flex-row gap-4 justify-center md:justify-start">
              <Link
                to="/Explore"
                className="inline-flex items-center justify-center gap-2 px-8 py-4 rounded-xl bg-white text-[#6D28D9] font-semibold text-lg hover:bg-violet-50 transition-colors active:scale-95"
              >
                <Search className="w-5 h-5" /> 탐색 시작하기
              </Link>
              <Link
                to="/Upload"
                className="inline-flex items-center justify-center gap-2 px-8 py-4 rounded-xl bg-transparent border-2 border-white/40 text-white font-semibold text-lg hover:bg-white/10 transition-colors active:scale-95"
              >
                <UploadIcon className="w-5 h-5" /> 창작물 등록
              </Link>
            </div>
            <div className="mt-10 flex items-center gap-6 justify-center md:justify-start text-violet-100/70 text-sm">
              <span className="inline-flex items-center gap-1.5"><Check className="w-4 h-4 text-cyan-300" /> 라이선스 명시</span>
              <span className="inline-flex items-center gap-1.5"><Check className="w-4 h-4 text-cyan-300" /> 크레딧 결제</span>
              <span className="inline-flex items-center gap-1.5"><Check className="w-4 h-4 text-cyan-300" /> 즉시 다운로드</span>
            </div>
          </div>
          <div className="w-full md:w-1/2 relative">
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: 20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              transition={{ duration: 0.7 }}
              className="rounded-3xl overflow-hidden border border-white/15 shadow-2xl shadow-black/40"
            >
              <img src={HERO} alt="Aiverse" className="w-full h-full object-cover" />
            </motion.div>
            <motion.img
              src={PROD1}
              alt=""
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.7, delay: 0.2 }}
              className="hidden sm:block absolute -bottom-6 -left-6 w-32 h-32 rounded-2xl object-cover border-2 border-white/20 shadow-xl"
            />
            <motion.img
              src={PROD2}
              alt=""
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.7, delay: 0.35 }}
              className="hidden sm:block absolute -top-6 -right-6 w-28 h-28 rounded-2xl object-cover border-2 border-white/20 shadow-xl"
            />
          </div>
        </div>
      </section>
      {/* ===== TrendingCreations ===== */}
      <section className="bg-white py-16 md:py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <Reveal className="flex items-end justify-between gap-4 mb-10">
            <div>
              <span className="inline-flex items-center gap-1.5 text-sm font-semibold text-[#0891B2]">
                <TrendingUp className="w-4 h-4" /> 인기 창작물
              </span>
              <h2 className="font-display text-3xl md:text-4xl font-bold text-slate-900 mt-2">지금 가장 많이 본 작품</h2>
            </div>
            <Link to="/Explore" className="hidden sm:inline-flex items-center gap-1 text-sm font-semibold text-[#6D28D9] hover:gap-2 transition-all">
              전체 보기 <ArrowRight className="w-4 h-4" />
            </Link>
          </Reveal>
          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {[...Array(6)].map((_, i) => (
                <div key={i} className="bg-white rounded-2xl border border-violet-100 overflow-hidden animate-pulse">
                  <div className="aspect-[4/3] bg-slate-200" />
                  <div className="p-4 space-y-2">
                    <div className="h-4 bg-slate-200 rounded w-3/4" />
                    <div className="h-3 bg-slate-100 rounded w-1/2" />
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {trending.map((c, i) => (
                <Reveal key={c.id} delay={i * 0.05}>
                  <ContentCard content={c} />
                </Reveal>
              ))}
            </div>
          )}
        </div>
      </section>
      {/* ===== ContentTypes ===== */}
      <section className="bg-stone-50 py-16 md:py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <Reveal className="text-center max-w-2xl mx-auto mb-12">
            <h2 className="font-display text-3xl md:text-4xl font-bold text-slate-900">유형별로 탐색하기</h2>
            <p className="text-slate-500 mt-3 text-lg">이미지, 영상, 음악 세 가지 유형의 AI 창작물을 만나보세요.</p>
          </Reveal>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {contentTypes.map((t, i) => (
              <Reveal key={t.type} delay={i * 0.08}>
                <Link
                  to={`/Explore?type=${t.type}`}
                  className="group block bg-white rounded-3xl p-8 border border-violet-100 hover:shadow-xl hover:shadow-violet-200/50 hover:-translate-y-1 transition-all duration-300"
                >
                  <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#6D28D9] to-[#0891B2] flex items-center justify-center">
                    <t.Icon className="w-8 h-8 text-white" />
                  </div>
                  <h3 className="font-display text-2xl font-bold text-slate-900 mt-6">{t.label}</h3>
                  <p className="text-slate-500 mt-2">{t.desc}</p>
                  <span className="inline-flex items-center gap-1 text-[#6D28D9] font-semibold mt-5 group-hover:gap-2 transition-all">
                    둘러보기 <ArrowRight className="w-4 h-4" />
                  </span>
                </Link>
              </Reveal>
            ))}
          </div>
        </div>
      </section>
      {/* ===== HowItWorks ===== */}
      <section className="bg-white py-16 md:py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <Reveal className="max-w-2xl mb-14">
            <h2 className="font-display text-3xl md:text-4xl font-bold text-slate-900">이용 방법</h2>
            <p className="text-slate-500 mt-3 text-lg">탐색부터 다운로드까지, 네 단계로 완성됩니다.</p>
          </Reveal>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
            {steps.map((s, i) => (
              <Reveal key={s.title} delay={i * 0.08}>
                <div className="relative">
                  <span className="font-display text-5xl font-bold text-violet-100">0{i + 1}</span>
                  <div className="w-12 h-12 rounded-xl bg-violet-50 flex items-center justify-center mt-3 mb-4">
                    <s.Icon className="w-6 h-6 text-[#6D28D9]" />
                  </div>
                  <h3 className="font-display text-lg font-bold text-slate-900">{s.title}</h3>
                  <p className="text-slate-500 mt-2 text-sm leading-relaxed">{s.desc}</p>
                </div>
              </Reveal>
            ))}
          </div>
        </div>
      </section>
      {/* ===== CreatorSpotlight ===== */}
      <section className="bg-stone-50 py-16 md:py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <Reveal className="flex items-end justify-between gap-4 mb-10">
            <div>
              <span className="inline-flex items-center gap-1.5 text-sm font-semibold text-[#0891B2]">
                <Award className="w-4 h-4" /> 크리에이터
              </span>
              <h2 className="font-display text-3xl md:text-4xl font-bold text-slate-900 mt-2">주목받는 창작자</h2>
            </div>
          </Reveal>
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-6">
            {creators.map((c, i) => (
              <Reveal key={c.name} delay={i * 0.06}>
                <div className="bg-white rounded-2xl border border-violet-100 p-6 text-center hover:shadow-lg hover:shadow-violet-200/40 transition-shadow">
                  <img src={c.avatar} alt={c.name} className="w-16 h-16 rounded-full mx-auto bg-violet-50 object-cover" />
                  <h3 className="font-display font-bold text-slate-900 mt-4">{c.name}</h3>
                  <p className="text-xs text-slate-500 mt-1">{c.specialty}</p>
                  <div className="flex items-center justify-center gap-4 mt-4 pt-4 border-t border-slate-100 text-sm">
                    <div>
                      <p className="font-bold text-[#6D28D9]">{c.works}</p>
                      <p className="text-xs text-slate-400">작품</p>
                    </div>
                    <div className="w-px h-8 bg-slate-100" />
                    <div>
                      <p className="font-bold text-[#0891B2]">{c.sales}</p>
                      <p className="text-xs text-slate-400">판매</p>
                    </div>
                  </div>
                </div>
              </Reveal>
            ))}
          </div>
        </div>
      </section>
      {/* ===== Stats (dark cosmic band) ===== */}
      <section className="relative overflow-hidden bg-gradient-to-r from-indigo-950 via-violet-900 to-cyan-900 py-16 md:py-20">
        <div className="absolute -top-16 right-1/4 w-72 h-72 rounded-full bg-fuchsia-500/20 blur-3xl" />
        <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8 text-center">
            {stats.map((s, i) => (
              <Reveal key={s.label} delay={i * 0.06}>
                <p className="font-display text-4xl md:text-5xl font-bold bg-gradient-to-r from-fuchsia-300 to-cyan-300 bg-clip-text text-transparent">
                  {s.value}
                </p>
                <p className="text-violet-100/70 mt-2 text-sm md:text-base">{s.label}</p>
              </Reveal>
            ))}
          </div>
        </div>
      </section>
      {/* ===== CTA ===== */}
      <section className="bg-white py-16 md:py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <Reveal>
            <div className="relative overflow-hidden rounded-3xl bg-gradient-to-br from-[#6D28D9] to-[#0891B2] px-8 py-14 md:px-16 md:py-20 text-center">
              <div className="absolute -top-10 -left-10 w-56 h-56 rounded-full bg-white/10 blur-3xl" />
              <div className="absolute -bottom-10 -right-10 w-56 h-56 rounded-full bg-white/10 blur-3xl" />
              <div className="relative z-10 max-w-2xl mx-auto">
                <h2 className="font-display text-3xl md:text-4xl font-bold text-white">지금 나의 AI 창작물을 자산으로</h2>
                <p className="text-violet-100/90 mt-4 text-lg">
                  등록하고, 판매하고, 성장하세요. 몇 분이면 첫 창작물을 마켓에 올릴 수 있습니다.
                </p>
                <div className="mt-8 flex flex-col sm:flex-row gap-4 justify-center">
                  <Link to="/Register" className="inline-flex items-center justify-center gap-2 px-8 py-4 rounded-xl bg-white text-[#6D28D9] font-semibold text-lg hover:bg-violet-50 transition-colors active:scale-95">
                    무료로 시작하기
                  </Link>
                  <Link to="/Explore" className="inline-flex items-center justify-center gap-2 px-8 py-4 rounded-xl bg-white/10 border-2 border-white/40 text-white font-semibold text-lg hover:bg-white/20 transition-colors active:scale-95">
                    창작물 둘러보기
                  </Link>
                </div>
              </div>
            </div>
          </Reveal>
        </div>
      </section>
    </div>
  );
}