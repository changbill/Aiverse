import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Package, TrendingUp, Zap, Upload as UploadIcon, BarChart, Loader2,
} from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { dashboardApi } from '@/api/dashboardApi';
import { useAppStore } from '@/stores/useAppStore';
const periodOptions = [
  { value: '7D', label: '최근 7일' },
  { value: '30D', label: '최근 30일' },
  { value: 'ALL', label: '전체' },
];
export default function Dashboard() {
  const navigate = useNavigate();
  const user = useAppStore((s) => s.user);
  const [period, setPeriod] = useState('30D');
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    if (!user) navigate('/Login', { replace: true });
  }, [user, navigate]);
  useEffect(() => {
    if (!user) return;
    (async () => {
      setLoading(true);
      try {
        const res = await dashboardApi.getSales(period);
        setData(res);
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    })();
  }, [user, period]);
  if (!user) return null;
  const totals = data?.totals || { assetCount: 0, totalSales: 0, totalRevenueCredit: 0 };
  const series = data?.series || [];
  const items = data?.items || [];
  const stats = [
    { label: '등록 창작물', value: totals.assetCount, Icon: Package, color: 'from-[#6D28D9] to-[#7c3aed]' },
    { label: '누적 판매 횟수', value: totals.totalSales, Icon: TrendingUp, color: 'from-[#0891B2] to-[#06b6d4]' },
    { label: '누적 판매 크레딧', value: totals.totalRevenueCredit, Icon: Zap, color: 'from-fuchsia-600 to-[#6D28D9]' },
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
        {/* period selector */}
        <div className="flex flex-wrap items-center gap-2 mt-6">
          {periodOptions.map((p) => (
            <button
              key={p.value}
              onClick={() => setPeriod(p.value)}
              className={`px-4 py-2 rounded-full text-sm font-semibold transition-colors ${
                period === p.value
                  ? 'bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white'
                  : 'bg-white text-slate-600 border border-violet-100 hover:bg-violet-50'
              }`}
            >
              {p.label}
            </button>
          ))}
        </div>
        {loading ? (
          <div className="bg-white rounded-2xl border border-violet-100 p-10 flex justify-center mt-8">
            <Loader2 className="w-6 h-6 text-[#6D28D9] animate-spin" />
          </div>
        ) : (
          <>
            {/* stat cards */}
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 mt-6">
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
            {/* sales trend */}
            {series.length > 0 && (
              <div className="mt-10 bg-white rounded-2xl border border-violet-100 p-6">
                <h2 className="font-display text-xl font-bold text-slate-900 mb-4 flex items-center gap-2">
                  <TrendingUp className="w-5 h-5 text-[#6D28D9]" /> 판매 추이
                </h2>
                <div className="h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={series}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                      <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                      <YAxis tick={{ fontSize: 12 }} allowDecimals={false} />
                      <Tooltip />
                      <Line type="monotone" dataKey="salesCount" name="판매 횟수" stroke="#6D28D9" strokeWidth={2} dot={false} />
                      <Line type="monotone" dataKey="revenueCredit" name="판매 크레딧" stroke="#0891B2" strokeWidth={2} dot={false} />
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </div>
            )}
            {/* top items */}
            <div className="mt-10">
              <h2 className="font-display text-xl font-bold text-slate-900 mb-4 flex items-center gap-2">
                <BarChart className="w-5 h-5 text-[#6D28D9]" /> 판매 상위 콘텐츠
              </h2>
              {items.length === 0 ? (
                <div className="bg-white rounded-3xl border border-violet-100 text-center py-16">
                  <Package className="w-14 h-14 text-slate-200 mx-auto mb-4" />
                  <h3 className="text-lg font-semibold text-slate-900">아직 판매 내역이 없어요</h3>
                  <p className="text-slate-500 mt-1">첫 창작물을 등록하고 판매를 시작해보세요.</p>
                  <Link to="/Upload" className="inline-block mt-5 px-6 py-3 rounded-lg bg-[#6D28D9] text-white font-semibold hover:bg-[#5b21b6] transition-colors">
                    창작물 등록하기
                  </Link>
                </div>
              ) : (
                <div className="bg-white rounded-2xl border border-violet-100 overflow-hidden">
                  <div className="hidden md:grid grid-cols-12 gap-4 px-5 py-3 bg-stone-50 text-xs font-semibold text-slate-400 uppercase">
                    <div className="col-span-8">창작물</div>
                    <div className="col-span-2 text-right">판매</div>
                    <div className="col-span-2 text-right">판매 크레딧</div>
                  </div>
                  <div className="divide-y divide-slate-100">
                    {items.map((item) => (
                      <Link
                        key={item.assetId}
                        to={`/content/${item.assetId}`}
                        className="grid grid-cols-2 md:grid-cols-12 gap-4 px-5 py-4 items-center hover:bg-violet-50/40 transition-colors"
                      >
                        <div className="col-span-2 md:col-span-8 min-w-0">
                          <p className="font-medium text-slate-900 truncate">{item.title}</p>
                        </div>
                        <div className="md:col-span-2 text-left md:text-right">
                          <span className="md:hidden text-xs text-slate-400">판매 </span>
                          <span className="text-slate-700">{item.salesCount.toLocaleString('ko-KR')}</span>
                        </div>
                        <div className="md:col-span-2 text-left md:text-right">
                          <span className="md:hidden text-xs text-slate-400">판매 크레딧 </span>
                          <span className="font-semibold text-[#0891B2]">{item.revenueCredit.toLocaleString('ko-KR')}</span>
                        </div>
                      </Link>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
