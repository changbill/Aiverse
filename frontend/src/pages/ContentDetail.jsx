import { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import {
  Image, Video, Music, Eye, Heart, ArrowLeft, Check, Zap, CreditCard, Package,
  Award, Info, Loader2, User as UserIcon,
} from 'lucide-react';
import { Content } from '@/api/entities';
import { useAppStore } from '@/stores/useAppStore';
import ContentCard from '@/components/ContentCard';
import confetti from 'canvas-confetti';
const typeMeta = {
  image: { label: '이미지', Icon: Image },
  video: { label: '영상', Icon: Video },
  music: { label: '음악', Icon: Music },
};
export default function ContentDetail() {
  const { slug } = useParams();
  const navigate = useNavigate();
  const [content, setContent] = useState(null);
  const [related, setRelated] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState(null);
  const user = useAppStore((s) => s.user);
  const credits = useAppStore((s) => s.credits);
  const purchaseContent = useAppStore((s) => s.purchaseContent);
  const isPurchased = useAppStore((s) => s.isPurchased);
  useEffect(() => {
    window.scrollTo(0, 0);
    setNotice(null);
    (async () => {
      setLoading(true);
      try {
        const res = await Content.get(slug);
        const c = res.data;
        setContent(c);
        if (c?.categoryId) {
          const rel = await Content.paging({ page: 1, limit: 4, filter: { search: '', categoryId: c.categoryId } });
          setRelated((rel.data.data || []).filter((x) => x.id !== c.id).slice(0, 3));
        }
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    })();
  }, [slug]);
  const purchased = content ? isPurchased(content.id) : false;
  const handlePurchase = () => {
    if (!user) {
      navigate('/Login');
      return;
    }
    const result = purchaseContent(content);
    if (result.success) {
      setNotice({ type: 'success', message: '구매 완료! 보관함에서 확인하세요.' });
      confetti({ particleCount: 90, spread: 70, origin: { y: 0.6 }, colors: ['#6D28D9', '#0891B2', '#e879f9'] });
    } else {
      setNotice({ type: 'error', message: result.message });
    }
  };
  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10 animate-pulse">
          <div className="aspect-[4/3] bg-slate-200 rounded-3xl" />
          <div className="space-y-4">
            <div className="h-8 bg-slate-200 rounded w-3/4" />
            <div className="h-4 bg-slate-100 rounded w-1/2" />
            <div className="h-24 bg-slate-100 rounded" />
          </div>
        </div>
      </div>
    );
  }
  if (!content) {
    return (
      <div className="text-center py-24">
        <Info className="w-12 h-12 text-slate-300 mx-auto mb-4" />
        <h2 className="text-xl font-semibold text-slate-900">콘텐츠를 찾을 수 없어요</h2>
        <Link to="/Explore" className="inline-block mt-5 px-6 py-3 rounded-lg bg-[#6D28D9] text-white font-semibold hover:bg-[#5b21b6] transition-colors">
          탐색으로 돌아가기
        </Link>
      </div>
    );
  }
  const meta = typeMeta[content.type] || typeMeta.image;
  const Icon = meta.Icon;
  const notEnough = credits < content.price;
  return (
    <div className="bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <Link to="/Explore" className="inline-flex items-center gap-1.5 text-sm font-medium text-slate-500 hover:text-[#6D28D9] transition-colors mb-6">
          <ArrowLeft className="w-4 h-4" /> 탐색으로 돌아가기
        </Link>
        {notice && (
          <div className={`mb-6 p-4 rounded-xl text-sm font-medium ${notice.type === 'success' ? 'bg-green-50 text-green-800 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'}`}>
            {notice.message}
          </div>
        )}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10">
          {/* preview */}
          <div>
            <div className="relative rounded-3xl overflow-hidden border border-violet-100 bg-slate-100 aspect-[4/3]">
              {content.thumbnail ? (
                <img src={content.thumbnail} alt={content.title} className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#6D28D9] to-[#0891B2]">
                  <Icon className="w-16 h-16 text-white" />
                </div>
              )}
              {(content.type === 'video' || content.type === 'music') && (
                <div className="absolute inset-0 flex items-center justify-center bg-black/25">
                  <div className="flex flex-col items-center text-white">
                    <div className="w-16 h-16 rounded-full bg-white/20 backdrop-blur flex items-center justify-center">
                      <Icon className="w-8 h-8" />
                    </div>
                    <span className="mt-3 text-sm font-medium bg-black/40 px-3 py-1 rounded-full">
                      {content.type === 'video' ? '영상 미리보기' : '오디오 미리보기'}
                    </span>
                  </div>
                </div>
              )}
              <span className="absolute top-4 left-4 inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white/90 backdrop-blur text-sm font-semibold text-[#6D28D9]">
                <Icon className="w-4 h-4" /> {meta.label}
              </span>
            </div>
          </div>
          {/* info */}
          <div>
            <h1 className="font-display text-3xl md:text-4xl font-bold text-slate-900 leading-tight">{content.title}</h1>
            <div className="flex items-center gap-4 mt-4 text-sm text-slate-500">
              <span className="inline-flex items-center gap-1.5">
                <UserIcon className="w-4 h-4" /> {content.creatorName}
              </span>
              <span className="inline-flex items-center gap-1"><Eye className="w-4 h-4" /> {Number(content.views || 0).toLocaleString('ko-KR')}</span>
              <span className="inline-flex items-center gap-1"><Heart className="w-4 h-4" /> {Number(content.likes || 0).toLocaleString('ko-KR')}</span>
            </div>
            <p className="text-slate-600 leading-relaxed mt-6">{content.description}</p>
            {content.tags && content.tags.length > 0 && (
              <div className="flex flex-wrap gap-2 mt-6">
                {content.tags.map((t) => (
                  <span key={t} className="px-3 py-1 rounded-full bg-violet-50 text-[#6D28D9] text-xs font-medium">#{t}</span>
                ))}
              </div>
            )}
            <div className="grid grid-cols-2 gap-4 mt-8">
              <div className="p-4 rounded-2xl bg-stone-50 border border-slate-100">
                <p className="text-xs text-slate-400">생성 도구</p>
                <p className="font-semibold text-slate-900 mt-1">{content.tool || '-'}</p>
              </div>
              <div className="p-4 rounded-2xl bg-stone-50 border border-slate-100">
                <p className="text-xs text-slate-400">라이선스</p>
                <p className="font-semibold text-slate-900 mt-1 inline-flex items-center gap-1">
                  <Award className="w-4 h-4 text-[#0891B2]" /> {content.license || '개인 이용'}
                </p>
              </div>
            </div>
            {/* purchase box */}
            <div className="mt-8 p-6 rounded-3xl bg-gradient-to-br from-indigo-950 via-violet-900 to-cyan-900 text-white">
              <div className="flex items-end justify-between">
                <div>
                  <p className="text-violet-200/70 text-sm">가격</p>
                  <p className="font-display text-3xl font-bold mt-1">
                    {Number(content.price).toLocaleString('ko-KR')} <span className="text-base font-normal text-violet-200/70">크레딧</span>
                  </p>
                </div>
                {user && (
                  <div className="text-right">
                    <p className="text-violet-200/70 text-xs">보유 크레딧</p>
                    <p className="text-cyan-300 font-semibold inline-flex items-center gap-1">
                      <Zap className="w-4 h-4" /> {credits.toLocaleString('ko-KR')}
                    </p>
                  </div>
                )}
              </div>
              <div className="mt-5">
                {purchased ? (
                  <Link
                    to="/Library"
                    className="w-full inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-white text-[#6D28D9] font-semibold hover:bg-violet-50 transition-colors"
                  >
                    <Package className="w-5 h-5" /> 보관함에서 확인
                  </Link>
                ) : !user ? (
                  <button
                    onClick={() => navigate('/Login')}
                    className="w-full inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-white text-[#6D28D9] font-semibold hover:bg-violet-50 transition-colors active:scale-95"
                  >
                    로그인하고 구매하기
                  </button>
                ) : notEnough ? (
                  <Link
                    to="/Credits"
                    className="w-full inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-amber-400 text-slate-900 font-semibold hover:bg-amber-300 transition-colors"
                  >
                    <CreditCard className="w-5 h-5" /> 크레딧이 부족해요 · 충전하기
                  </Link>
                ) : (
                  <button
                    onClick={handlePurchase}
                    className="w-full inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-white text-[#6D28D9] font-semibold hover:bg-violet-50 transition-colors active:scale-95"
                  >
                    <Check className="w-5 h-5" /> 크레딧으로 구매하기
                  </button>
                )}
              </div>
              <p className="text-violet-200/60 text-xs mt-3 text-center">구매 시 라이선스 범위 내에서 다운로드 및 사용이 가능합니다.</p>
            </div>
          </div>
        </div>
        {/* related */}
        {related.length > 0 && (
          <div className="mt-16">
            <h2 className="font-display text-2xl font-bold text-slate-900 mb-6">비슷한 창작물</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {related.map((c) => (
                <ContentCard key={c.id} content={c} />
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}