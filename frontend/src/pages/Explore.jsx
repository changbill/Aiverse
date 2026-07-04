import { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Search, SortAsc, Grid, Image, Video, Music, Loader2 } from 'lucide-react';
import { Content, Category } from '@/api/entities';
import ContentCard from '@/components/ContentCard';
const typeTabs = [
  { value: 'all', label: '전체', Icon: Grid },
  { value: 'image', label: '이미지', Icon: Image },
  { value: 'video', label: '영상', Icon: Video },
  { value: 'music', label: '음악', Icon: Music },
];
const sortOptions = [
  { value: '-createdAt', label: '최신순' },
  { value: '-views', label: '인기순' },
  { value: 'price', label: '낮은 가격순' },
  { value: '-price', label: '높은 가격순' },
];
export default function Explore() {
  const [searchParams] = useSearchParams();
  const [items, setItems] = useState([]);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [categories, setCategories] = useState([]);
  const [searchInput, setSearchInput] = useState('');
  const [search, setSearch] = useState('');
  const [type, setType] = useState(searchParams.get('type') || 'all');
  const [categoryId, setCategoryId] = useState(searchParams.get('category') || 'all');
  const [sort, setSort] = useState('-createdAt');
  const isComposingRef = useRef(false);
  const loaderRef = useRef(null);
  useEffect(() => {
    (async () => {
      try {
        const res = await Category.paging({ page: 1, limit: 50, filter: { search: '' } });
        setCategories(res.data.data || []);
      } catch (e) {
        console.error(e);
      }
    })();
  }, []);
  const buildFilter = () => {
    const f = { search };
    if (type !== 'all') f.type = type;
    if (categoryId !== 'all') f.categoryId = categoryId;
    return f;
  };
  const fetchData = async (pageNum, reset = false) => {
    setLoading(true);
    try {
      const res = await Content.paging({ page: pageNum, limit: 12, filter: buildFilter(), sort });
      const data = res.data.data || [];
      setItems((prev) => (reset ? data : [...prev, ...data]));
      setHasMore(pageNum < res.data.totalPages);
      setPage(pageNum);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };
  // debounce search input (IME-safe)
  useEffect(() => {
    if (isComposingRef.current) return;
    const timer = setTimeout(() => setSearch(searchInput), 300);
    return () => clearTimeout(timer);
  }, [searchInput]);
  // reset + fetch page 1 on filter change
  useEffect(() => {
    setItems([]);
    setHasMore(true);
    setPage(1);
    fetchData(1, true);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [search, type, categoryId, sort]);
  // append on page change (scroll)
  useEffect(() => {
    if (page > 1) fetchData(page, false);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);
  // infinite scroll observer
  useEffect(() => {
    if (!hasMore || loading) return;
    const el = loaderRef.current;
    if (!el) return;
    const obs = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) setPage((p) => p + 1);
      },
      { rootMargin: '200px' }
    );
    obs.observe(el);
    return () => obs.disconnect();
  }, [hasMore, loading]);
  return (
    <div className="bg-stone-50 min-h-screen">
      {/* mini hero */}
      <div className="bg-gradient-to-br from-indigo-950 via-violet-900 to-cyan-900 overflow-hidden relative">
        <div className="absolute -top-20 -right-16 w-72 h-72 rounded-full bg-cyan-500/20 blur-3xl" />
        <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pt-12 pb-10">
          <h1 className="font-display text-3xl md:text-4xl font-bold text-white">창작물 탐색</h1>
          <p className="text-violet-100/80 mt-2">유형 · 카테고리 · 태그로 원하는 AI 창작물을 찾아보세요.</p>
          <div className="mt-6 relative max-w-xl">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
            <input
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              onCompositionStart={() => { isComposingRef.current = true; }}
              onCompositionEnd={(e) => { isComposingRef.current = false; setSearchInput(e.currentTarget.value); }}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.nativeEvent.isComposing && !isComposingRef.current) {
                  setSearch(searchInput);
                }
              }}
              placeholder="제목, 태그로 검색..."
              className="w-full pl-12 pr-4 py-3.5 rounded-xl bg-white/95 backdrop-blur border border-white/20 text-slate-900 placeholder-slate-400 outline-none focus:ring-2 focus:ring-cyan-400"
            />
          </div>
        </div>
      </div>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* controls */}
        <div className="flex flex-col gap-4 mb-8">
          <div className="flex flex-wrap items-center gap-2">
            {typeTabs.map((t) => (
              <button
                key={t.value}
                onClick={() => setType(t.value)}
                className={`inline-flex items-center gap-1.5 px-4 py-2 rounded-full text-sm font-semibold transition-colors ${
                  type === t.value
                    ? 'bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white'
                    : 'bg-white text-slate-600 border border-violet-100 hover:bg-violet-50'
                }`}
              >
                <t.Icon className="w-4 h-4" /> {t.label}
              </button>
            ))}
          </div>
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex flex-wrap items-center gap-2">
              <button
                onClick={() => setCategoryId('all')}
                className={`px-3 py-1.5 rounded-full text-xs font-medium transition-colors ${
                  categoryId === 'all' ? 'bg-[#16112E] text-white' : 'bg-white text-slate-500 border border-slate-200 hover:bg-slate-50'
                }`}
              >
                모든 카테고리
              </button>
              {categories.map((cat) => (
                <button
                  key={cat.id}
                  onClick={() => setCategoryId(cat.id)}
                  className={`px-3 py-1.5 rounded-full text-xs font-medium transition-colors ${
                    categoryId === cat.id ? 'bg-[#16112E] text-white' : 'bg-white text-slate-500 border border-slate-200 hover:bg-slate-50'
                  }`}
                >
                  {cat.name}
                </button>
              ))}
            </div>
            <div className="relative flex items-center gap-2 text-sm text-slate-500">
              <SortAsc className="w-4 h-4" />
              <select
                value={sort}
                onChange={(e) => setSort(e.target.value)}
                className="bg-white border border-slate-200 rounded-lg px-3 py-2 text-slate-700 font-medium outline-none focus:ring-2 focus:ring-violet-200 cursor-pointer"
              >
                {sortOptions.map((o) => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>
          </div>
        </div>
        {/* grid */}
        {loading && items.length === 0 ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {[...Array(8)].map((_, i) => (
              <div key={i} className="bg-white rounded-2xl border border-violet-100 overflow-hidden animate-pulse">
                <div className="aspect-[4/3] bg-slate-200" />
                <div className="p-4 space-y-2">
                  <div className="h-4 bg-slate-200 rounded w-3/4" />
                  <div className="h-3 bg-slate-100 rounded w-1/2" />
                </div>
              </div>
            ))}
          </div>
        ) : items.length === 0 ? (
          <div className="text-center py-20">
            <Search className="w-12 h-12 text-slate-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-slate-900">검색 결과가 없어요</h3>
            <p className="text-slate-500 mt-1">다른 키워드나 필터로 다시 시도해보세요.</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {items.map((c) => (
                <ContentCard key={c.id} content={c} />
              ))}
            </div>
            {hasMore && (
              <div ref={loaderRef} className="flex justify-center py-10">
                {loading && <Loader2 className="w-6 h-6 text-[#6D28D9] animate-spin" />}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}