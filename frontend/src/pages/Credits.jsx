import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Zap, Check, CreditCard, Star, Loader2, ArrowRight } from 'lucide-react';
import { useAppStore } from '@/stores/useAppStore';
import { creditApi } from '@/api/creditApi';
import confetti from 'canvas-confetti';
export default function Credits() {
  const navigate = useNavigate();
  const user = useAppStore((s) => s.user);
  const credits = useAppStore((s) => s.credits);
  const setCredits = useAppStore((s) => s.setCredits);
  const [packages, setPackages] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [selected, setSelected] = useState(null);
  const [processing, setProcessing] = useState(false);
  const [notice, setNotice] = useState(null);
  useEffect(() => {
    (async () => {
      try {
        const products = await creditApi.listProducts();
        setPackages(products);
        if (products[0]) {
          const recommended = products.find((p) => p.code === 'PLUS') || products[0];
          setSelected(recommended.id);
        }
      } catch (e) {
        console.error(e);
      }
    })();
  }, []);
  useEffect(() => {
    if (!user) return;
    (async () => {
      try {
        const res = await creditApi.listTransactions({ page: 1, size: 10 });
        setTransactions(res.items);
      } catch (e) {
        console.error(e);
      }
    })();
  }, [user]);
  const handleCharge = async () => {
    if (!user) {
      navigate('/Login');
      return;
    }
    const pkg = packages.find((p) => p.id === selected);
    if (!pkg) return;
    setProcessing(true);
    setNotice(null);
    try {
      const result = await creditApi.pay(pkg.id);
      setCredits(result.creditBalance);
      const res = await creditApi.listTransactions({ page: 1, size: 10 });
      setTransactions(res.items);
      setNotice({ type: 'success', message: `${result.grantedCredit.toLocaleString('ko-KR')} 크레딧이 충전되었습니다!` });
      confetti({ particleCount: 120, spread: 80, origin: { y: 0.6 }, colors: ['#6D28D9', '#0891B2', '#e879f9'] });
    } catch (err) {
      console.error(err);
      setNotice({ type: 'error', message: err.message || '결제에 실패했어요. 잠시 후 다시 시도해주세요.' });
    } finally {
      setProcessing(false);
    }
  };
  return (
    <div className="bg-stone-50 min-h-screen">
      <div className="bg-gradient-to-br from-indigo-950 via-violet-900 to-cyan-900 overflow-hidden relative">
        <div className="absolute -top-16 right-1/4 w-72 h-72 rounded-full bg-fuchsia-500/20 blur-3xl" />
        <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-14 pb-12">
          <h1 className="font-display text-3xl md:text-4xl font-bold text-white">크레딧 충전</h1>
          <p className="text-violet-100/80 mt-2">크레딧으로 원하는 AI 창작물을 자유롭게 구매하세요.</p>
          {user && (
            <div className="mt-6 inline-flex items-center gap-2 px-5 py-3 rounded-2xl bg-white/10 border border-white/15 backdrop-blur">
              <Zap className="w-5 h-5 text-cyan-300" />
              <span className="text-white font-medium">보유 크레딧</span>
              <span className="font-display text-2xl font-bold text-cyan-300 ml-2">{credits.toLocaleString('ko-KR')}</span>
            </div>
          )}
        </div>
      </div>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        {notice && (
          <div className={`mb-6 p-4 rounded-xl text-sm font-medium ${notice.type === 'success' ? 'bg-green-50 text-green-800 border border-green-200' : 'bg-red-50 text-red-700 border border-red-200'}`}>
            {notice.message}
          </div>
        )}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          <div className="lg:col-span-2">
            <h2 className="font-display text-xl font-bold text-slate-900 mb-5">패키지 선택</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {packages.map((pkg) => {
                const active = selected === pkg.id;
                const popular = pkg.code === 'PLUS';
                return (
                  <button
                    key={pkg.id}
                    onClick={() => setSelected(pkg.id)}
                    className={`relative text-left p-6 rounded-2xl border-2 transition-all ${
                      active ? 'border-[#6D28D9] bg-white shadow-lg shadow-violet-200/50' : 'border-violet-100 bg-white hover:border-violet-200'
                    }`}
                  >
                    {popular && (
                      <span className="absolute top-4 right-4 inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white text-xs font-semibold">
                        <Star className="w-3 h-3" /> 인기
                      </span>
                    )}
                    <p className="font-semibold text-slate-500">{pkg.name}</p>
                    <p className="font-display text-3xl font-bold text-slate-900 mt-2">
                      {pkg.creditAmount.toLocaleString('ko-KR')}
                      <span className="text-base font-normal text-slate-400 ml-1">크레딧</span>
                    </p>
                    {pkg.bonusCredit > 0 && (
                      <p className="text-sm text-[#0891B2] font-medium mt-1">+{pkg.bonusCredit} 보너스 크레딧</p>
                    )}
                    <p className="text-slate-700 font-semibold mt-4">₩{pkg.price.toLocaleString('ko-KR')}</p>
                    {active && (
                      <div className="absolute top-4 left-4 w-6 h-6 rounded-full bg-[#6D28D9] flex items-center justify-center">
                        <Check className="w-4 h-4 text-white" />
                      </div>
                    )}
                  </button>
                );
              })}
            </div>
          </div>
          <div className="lg:col-span-1">
            <div className="sticky top-24 bg-white rounded-2xl border border-violet-100 p-6 shadow-lg shadow-violet-100/40">
              <h3 className="font-display font-bold text-slate-900">결제 요약</h3>
              {(() => {
                const pkg = packages.find((p) => p.id === selected);
                if (!pkg) return null;
                const total = pkg.creditAmount + pkg.bonusCredit;
                return (
                  <div className="mt-4 space-y-3 text-sm">
                    <div className="flex justify-between text-slate-500">
                      <span>{pkg.name} 패키지</span>
                      <span>{pkg.creditAmount.toLocaleString('ko-KR')} 크레딧</span>
                    </div>
                    {pkg.bonusCredit > 0 && (
                      <div className="flex justify-between text-[#0891B2]">
                        <span>보너스</span>
                        <span>+{pkg.bonusCredit} 크레딧</span>
                      </div>
                    )}
                    <div className="border-t border-slate-100 pt-3 flex justify-between font-semibold text-slate-900">
                      <span>충전 크레딧</span>
                      <span className="text-[#6D28D9]">{total.toLocaleString('ko-KR')}</span>
                    </div>
                    <div className="flex justify-between font-semibold text-slate-900">
                      <span>결제 금액</span>
                      <span>₩{pkg.price.toLocaleString('ko-KR')}</span>
                    </div>
                  </div>
                );
              })()}
              <button
                onClick={handleCharge}
                disabled={processing || !selected}
                className="w-full mt-6 inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white font-semibold hover:opacity-90 transition-opacity disabled:opacity-60 active:scale-95"
              >
                {processing ? (
                  <><Loader2 className="w-5 h-5 animate-spin" /> 결제 처리 중...</>
                ) : (
                  <><CreditCard className="w-5 h-5" /> {user ? '결제하기' : '로그인 후 결제'}</>
                )}
              </button>
              <p className="text-xs text-slate-400 mt-3 text-center">데모용 목업 결제입니다. 실제 청구는 발생하지 않습니다.</p>
            </div>
          </div>
        </div>
        {/* transaction history */}
        {user && transactions.length > 0 && (
          <div className="mt-12">
            <h2 className="font-display text-xl font-bold text-slate-900 mb-5">크레딧 내역</h2>
            <div className="bg-white rounded-2xl border border-violet-100 divide-y divide-slate-100">
              {transactions.map((t) => (
                <div key={t.id} className="flex items-center justify-between px-5 py-4">
                  <div className="flex items-center gap-3">
                    <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${t.type === 'CHARGE' ? 'bg-cyan-50 text-[#0891B2]' : 'bg-violet-50 text-[#6D28D9]'}`}>
                      {t.type === 'CHARGE' ? <Zap className="w-4 h-4" /> : <ArrowRight className="w-4 h-4" />}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-slate-900">{t.reason}</p>
                      <p className="text-xs text-slate-400">{new Date(t.createdAt).toLocaleString('ko-KR')}</p>
                    </div>
                  </div>
                  <span className={`font-semibold ${t.amount > 0 ? 'text-[#0891B2]' : 'text-slate-500'}`}>
                    {t.amount > 0 ? '+' : ''}{t.amount.toLocaleString('ko-KR')}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
