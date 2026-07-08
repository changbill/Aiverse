import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Image, Video, Music, Download, Award, Calendar, Package, Loader2 } from 'lucide-react';
import { useAppStore } from '@/stores/useAppStore';
import { purchaseApi } from '@/api/purchaseApi';
const typeMeta = {
  image: { label: '이미지', Icon: Image },
  video: { label: '영상', Icon: Video },
  music: { label: '음악', Icon: Music },
};
const licenseLabel = { PERSONAL: '개인 이용', COMMERCIAL: '상업적 이용 가능' };
export default function Library() {
  const navigate = useNavigate();
  const user = useAppStore((s) => s.user);
  const [purchases, setPurchases] = useState([]);
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(null);
  const [notice, setNotice] = useState(null);
  useEffect(() => {
    if (!user) navigate('/Login', { replace: true });
  }, [user, navigate]);
  useEffect(() => {
    if (!user) return;
    (async () => {
      setLoading(true);
      try {
        const res = await purchaseApi.library({ page: 1, size: 100 });
        setPurchases(res.items);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    })();
  }, [user]);
  if (!user) return null;
  const handleDownload = async (item) => {
    setDownloading(item.contentId);
    setNotice(null);
    try {
      const { downloadUrl } = await purchaseApi.download(item.contentId);
      window.open(downloadUrl, '_blank', 'noopener,noreferrer');
    } catch (e) {
      console.error(e);
      setNotice(e.message || '다운로드 링크 발급에 실패했어요.');
    } finally {
      setDownloading(null);
    }
  };
  return (
    <div className="bg-stone-50 min-h-screen py-10">
      <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center gap-3">
          <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-[#6D28D9] to-[#0891B2] flex items-center justify-center">
            <Package className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="font-display text-3xl font-bold text-slate-900">구매 보관함</h1>
            <p className="text-slate-500">구매한 창작물을 확인하고 다운로드하세요.</p>
          </div>
        </div>
        {notice && (
          <div className="mt-6 p-4 rounded-xl text-sm font-medium bg-amber-50 text-amber-800 border border-amber-200">
            {notice}
          </div>
        )}
        {loading ? (
          <div className="mt-12 flex justify-center py-16">
            <Loader2 className="w-6 h-6 text-[#6D28D9] animate-spin" />
          </div>
        ) : purchases.length === 0 ? (
          <div className="mt-12 text-center py-16 bg-white rounded-3xl border border-violet-100">
            <Package className="w-14 h-14 text-slate-200 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-slate-900">아직 구매한 창작물이 없어요</h3>
            <p className="text-slate-500 mt-1">마음에 드는 AI 창작물을 찾아 구매해보세요.</p>
            <Link to="/Explore" className="inline-block mt-5 px-6 py-3 rounded-lg bg-[#6D28D9] text-white font-semibold hover:bg-[#5b21b6] transition-colors">
              창작물 탐색하기
            </Link>
          </div>
        ) : (
          <div className="mt-8 space-y-4">
            {purchases.map((item) => {
              const meta = typeMeta[item.type] || typeMeta.image;
              const Icon = meta.Icon;
              return (
                <div key={item.purchaseId} className="bg-white rounded-2xl border border-violet-100 p-4 flex flex-col sm:flex-row gap-4 hover:shadow-lg hover:shadow-violet-100/50 transition-shadow">
                  <Link to={`/content/${item.contentId}`} className="relative w-full sm:w-40 h-32 flex-shrink-0 rounded-xl overflow-hidden bg-slate-100">
                    {item.thumbnail ? (
                      <img src={item.thumbnail} alt={item.title} className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#6D28D9] to-[#0891B2]">
                        <Icon className="w-8 h-8 text-white" />
                      </div>
                    )}
                    <span className="absolute top-2 left-2 inline-flex items-center gap-1 px-2 py-0.5 rounded-full bg-white/90 text-xs font-semibold text-[#6D28D9]">
                      <Icon className="w-3 h-3" /> {meta.label}
                    </span>
                    {item.deleted && (
                      <span className="absolute bottom-2 left-2 px-2 py-0.5 rounded-full bg-black/60 text-white text-xs">삭제된 콘텐츠</span>
                    )}
                  </Link>
                  <div className="flex-1 flex flex-col justify-between">
                    <div>
                      <Link to={`/content/${item.contentId}`} className="font-display font-bold text-slate-900 hover:text-[#6D28D9] transition-colors">
                        {item.title}
                      </Link>
                      <div className="flex flex-wrap items-center gap-x-4 gap-y-1 mt-3 text-xs text-slate-500">
                        <span className="inline-flex items-center gap-1"><Award className="w-3.5 h-3.5 text-[#0891B2]" /> {licenseLabel[item.license] || item.license}</span>
                        <span className="inline-flex items-center gap-1"><Calendar className="w-3.5 h-3.5" /> {new Date(item.purchasedAt).toLocaleDateString('ko-KR')}</span>
                        <span className="text-[#6D28D9] font-semibold">{item.price.toLocaleString('ko-KR')} 크레딧</span>
                      </div>
                    </div>
                    <div className="mt-4">
                      <button
                        onClick={() => handleDownload(item)}
                        disabled={downloading === item.contentId}
                        className="inline-flex items-center gap-2 px-5 py-2.5 rounded-lg bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white text-sm font-semibold hover:opacity-90 transition-opacity disabled:opacity-60 active:scale-95"
                      >
                        {downloading === item.contentId ? (
                          <><Loader2 className="w-4 h-4 animate-spin" /> 준비 중...</>
                        ) : (
                          <><Download className="w-4 h-4" /> 다운로드</>
                        )}
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
