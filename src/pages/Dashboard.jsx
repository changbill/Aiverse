import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Package, TrendingUp, Zap, Upload as UploadIcon, Image, Video, Music, BarChart, Loader2,
} from 'lucide-react';
import { Content } from '@/api/entities';
import { useAppStore } from '@/stores/useAppStore';
const typeMeta = {
  image: { label: '이미지', Icon: Image },
  video: { label: '영상', Icon: Video },
  music: { label: '음악', Icon: Music },
};
export default function Dashboard() {
  const navigate = useNavigate();
  const user = useAppStore((s) => s.user);
  const myUploads = useAppStore((s) => s.myUploads);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    if (!user) navigate('/Login', { replace: true });
  }, [user, navigate]);
  useEffect(() => {
    if (!user) return;
    (async () => {
      setLoading(true);
      try {
        const res = await Content.paging({ page: 1, limit: 100, filter: { search: '', creatorId: user.id }, sort: '-createdAt' });
        const fetched = res.data.data || [];
        // merge local uploads not yet reflected from API
        const ids = new Set(fetched.map((c) => c.id));
        const localOnly = myUploads.filter((u) => u.id && !ids.has(u.id));
        setItems([...localOnly, ...fetched]);
      } catch (e) {
        console.error(e);
        setItems(myUploads);
      } finally {
        setLoading(false);
      }
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);
  if (!user) return null;
  const totalContents = items.length;
  const totalSales = items.reduce((sum, c) => sum + Number(c.sales || 0), 0);
  const totalRevenue = items.reduce((sum, c) => sum + Number(c.sales || 0) * Number(c.price || 0), 0);
  const stats = [
    { label: '등록 창작물', value: totalContents, Icon: Package, color: 'from-[#6D28D9] to-[#7c3aed]' },
    { label: '누적 판매 횟수', value: totalSales, Icon: TrendingUp, color: 'from-[#0891B2] to-[#06b6d4]' },
    { label: '누적 판매 크레딧', value: totalRevenue, Icon: Zap, color: 'from-fuchsia-600 to-[#6D28D9]' },
  ];
  return (
    <div className="bg-stone-50 min-h-screen py-10">
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div>
            <h1 className="font-display text-3xl font-bold text-slate-900">판매 대시보드</h1>
            <p className="text-slate-500 mt-1">등록한 창작물의 판매 현황을 한눈에 확인하세요.</p>
          </div>
          <Link to="/Upload" className="inline-flex items-center gap-2 px-5 py-3 rounded-xl bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white font-semibold hover:opacity-90 transition-opacity">
            <UploadIcon className="w-5 h-5" /> 새 창작물 등록
          </Link>
        </div>
        {/* stat cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mt-8">
          {stats.map((s) => (
            <div key={s.label} className="bg-white rounded-2xl border border-violet-100 p-6">
              <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${s.color} flex items-center justify-center`}>
                <s.Icon className="w-6 h-6 text-white" />
              </div>
              <p className="font-display text-3xl font-bold text-slate-900 mt-4">{s.value.toLocaleString('ko-KR')}</p>
              <p className="text-slate-500 text-sm mt-1">{s.label}</p>
            </div>
          ))}
        </div>
        {/* content table */}
        <div className="mt-10">
          <h2 className="font-display text-xl font-bold text-slate-900 mb-4 flex items-center gap-2">
            <BarChart className="w-5 h-5 text-[#6D28D9]" /> 내 창작물
          </h2>
          {loading ? (
            <div className="bg-white rounded-2xl border border-violet-100 p-10 flex justify-center">
              <Loader2 className="w-6 h-6 text-[#6D28D9] animate-spin" />
            </div>
          ) : items.length === 0 ? (
            <div className="bg-white rounded-3xl border border-violet-100 text-center py-16">
              <Package className="w-14 h-14 text-slate-200 mx-auto mb-4" />
              <h3 className="text-lg font-semibold text-slate-900">아직 등록한 창작물이 없어요</h3>
              <p className="text-slate-500 mt-1">첫 창작물을 등록하고 판매를 시작해보세요.</p>
              <Link to="/Upload" className="inline-block mt-5 px-6 py-3 rounded-lg bg-[#6D28D9] text-white font-semibold hover:bg-[#5b21b6] transition-colors">
                창작물 등록하기
              </Link>
            </div>
          ) : (
            <div className="bg-white rounded-2xl border border-violet-100 overflow-hidden">
              <div className="hidden md:grid grid-cols-12 gap-4 px-5 py-3 bg-stone-50 text-xs font-semibold text-slate-400 uppercase">
                <div className="col-span-6">창작물</div>
                <div className="col-span-2 text-right">가격</div>
                <div className="col-span-2 text-right">판매</div>
                <div className="col-span-2 text-right">판매 크레딧</div>
              </div>
              <div className="divide-y divide-slate-100">
                {items.map((c) => {
                  const meta = typeMeta[c.type] || typeMeta.image;
                  const Icon = meta.Icon;
                  return (
                    <div key={c.id || c.slug} className="grid grid-cols-2 md:grid-cols-12 gap-4 px-5 py-4 items-center">
                      <div className="col-span-2 md:col-span-6 flex items-center gap-3 min-w-0">
                        <div className="w-12 h-12 rounded-lg overflow-hidden bg-slate-100 flex-shrink-0">
                          {c.thumbnail ? (
                            <img src={c.thumbnail} alt={c.title} className="w-full h-full object-cover" />
                          ) : (
                            <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#6D28D9] to-[#0891B2]">
                              <Icon className="w-5 h-5 text-white" />
                            </div>
                          )}
                        </div>
                        <div className="min-w-0">
                          <p className="font-medium text-slate-900 truncate">{c.title}</p>
                          <p className="text-xs text-slate-400 inline-flex items-center gap-1"><Icon className="w-3 h-3" /> {meta.label}</p>
                        </div>
                      </div>
                      <div className="md:col-span-2 text-left md:text-right">
                        <span className="md:hidden text-xs text-slate-400">가격 </span>
                        <span className="font-semibold text-[#6D28D9]">{Number(c.price || 0).toLocaleString('ko-KR')}</span>
                      </div>
                      <div className="md:col-span-2 text-left md:text-right">
                        <span className="md:hidden text-xs text-slate-400">판매 </span>
                        <span className="text-slate-700">{Number(c.sales || 0).toLocaleString('ko-KR')}</span>
                      </div>
                      <div className="md:col-span-2 text-left md:text-right">
                        <span className="md:hidden text-xs text-slate-400">판매 크레딧 </span>
                        <span className="font-semibold text-[#0891B2]">{(Number(c.sales || 0) * Number(c.price || 0)).toLocaleString('ko-KR')}</span>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}