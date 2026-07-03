import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Image as ImageIcon, Video, Music, Upload as UploadIcon, Check, Loader2, X, Info } from 'lucide-react';
import { Content, Category } from '@/api/entities';
import { vibex } from '@/api/vibexClient';
import { useAppStore } from '@/stores/useAppStore';
import confetti from 'canvas-confetti';
const typeOptions = [
  { value: 'image', label: '이미지', Icon: ImageIcon },
  { value: 'video', label: '영상', Icon: Video },
  { value: 'music', label: '음악', Icon: Music },
];
const licenseOptions = ['개인 이용', '상업적 이용 가능', '확장 라이선스'];
function slugify(str) {
  return String(str)
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9가-힣\s-]/g, '')
    .replace(/\s+/g, '-')
    .slice(0, 60) + '-' + Math.random().toString(36).slice(2, 6);
}
export default function Upload() {
  const navigate = useNavigate();
  const user = useAppStore((s) => s.user);
  const addUpload = useAppStore((s) => s.addUpload);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({
    title: '',
    description: '',
    type: 'image',
    categoryId: '',
    tags: '',
    price: '',
    tool: '',
    license: '개인 이용',
  });
  const [thumbnail, setThumbnail] = useState('');
  const [uploading, setUploading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errors, setErrors] = useState({});
  const [notice, setNotice] = useState(null);
  useEffect(() => {
    if (!user) navigate('/Login', { replace: true });
  }, [user, navigate]);
  useEffect(() => {
    (async () => {
      try {
        const res = await Category.paging({ page: 1, limit: 50, filter: { search: '' } });
        const list = res.data.data || [];
        setCategories(list);
        if (list[0]) setForm((f) => ({ ...f, categoryId: list[0].id }));
      } catch (e) {
        console.error(e);
      }
    })();
  }, []);
  if (!user) return null;
  const setField = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  const onFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setUploading(true);
    try {
      const result = await vibex.integrations.Core.UploadFile({ file, folder: 'images' });
      const url = result?.data?.file_url;
      if (url) setThumbnail(url);
    } catch (err) {
      console.error(err);
      setNotice({ type: 'error', message: '이미지 업로드에 실패했어요. 다시 시도해주세요.' });
    } finally {
      setUploading(false);
    }
  };
  const validate = () => {
    const e = {};
    if (!form.title.trim()) e.title = '제목을 입력해주세요';
    if (!form.description.trim()) e.description = '설명을 입력해주세요';
    if (!form.categoryId) e.categoryId = '카테고리를 선택해주세요';
    if (form.price === '' || Number(form.price) < 0) e.price = '올바른 가격을 입력해주세요';
    setErrors(e);
    return Object.keys(e).length === 0;
  };
  const handleSubmit = async () => {
    if (!validate()) return;
    setSubmitting(true);
    setNotice(null);
    try {
      const payload = {
        title: form.title.trim(),
        slug: slugify(form.title),
        description: form.description.trim(),
        type: form.type,
        categoryId: form.categoryId,
        tags: form.tags.split(',').map((t) => t.trim()).filter(Boolean),
        price: Number(form.price),
        tool: form.tool.trim(),
        license: form.license,
        thumbnail: thumbnail || '',
        creatorId: user.id,
        creatorName: user.name,
        sales: 0,
        views: 0,
        likes: 0,
        featured: false,
        status: 'published',
        createdAt: new Date().toISOString(),
      };
      const res = await Content.create(payload);
      const created = res?.data || payload;
      addUpload(created);
      confetti({ particleCount: 100, spread: 70, origin: { y: 0.6 }, colors: ['#6D28D9', '#0891B2', '#e879f9'] });
      navigate('/Dashboard');
    } catch (err) {
      console.error(err);
      setNotice({ type: 'error', message: '등록에 실패했어요. 잠시 후 다시 시도해주세요.' });
    } finally {
      setSubmitting(false);
    }
  };
  const inputCls = 'w-full px-4 py-3 rounded-xl border border-slate-300 focus:border-[#6D28D9] focus:ring-2 focus:ring-violet-200 outline-none transition-colors';
  return (
    <div className="bg-stone-50 min-h-screen py-10">
      <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8">
        <h1 className="font-display text-3xl font-bold text-slate-900">창작물 등록</h1>
        <p className="text-slate-500 mt-2">AI로 만든 창작물의 정보를 입력하고 마켓에 등록하세요.</p>
        {notice && (
          <div className="mt-6 p-4 rounded-xl text-sm font-medium bg-red-50 text-red-700 border border-red-200">
            {notice.message}
          </div>
        )}
        <div role="form" aria-label="창작물 등록" className="mt-8 bg-white rounded-3xl border border-violet-100 p-6 md:p-8 space-y-6 shadow-lg shadow-violet-100/40">
          {/* thumbnail */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-2">대표 이미지 / 썸네일</label>
            {thumbnail ? (
              <div className="relative w-full aspect-[4/3] rounded-2xl overflow-hidden border border-violet-100">
                <img src={thumbnail} alt="preview" className="w-full h-full object-cover" />
                <button
                  onClick={() => setThumbnail('')}
                  className="absolute top-3 right-3 p-1.5 rounded-full bg-black/50 text-white hover:bg-black/70"
                  aria-label="이미지 제거"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <label className="flex flex-col items-center justify-center w-full aspect-[4/3] rounded-2xl border-2 border-dashed border-violet-200 bg-violet-50/50 cursor-pointer hover:bg-violet-50 transition-colors">
                {uploading ? (
                  <Loader2 className="w-8 h-8 text-[#6D28D9] animate-spin" />
                ) : (
                  <>
                    <UploadIcon className="w-8 h-8 text-[#6D28D9]" />
                    <span className="mt-2 text-sm text-slate-500">클릭하여 이미지 업로드</span>
                  </>
                )}
                <input type="file" accept="image/*" className="hidden" onChange={onFileChange} disabled={uploading} />
              </label>
            )}
          </div>
          {/* type */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-2">유형</label>
            <div className="grid grid-cols-3 gap-3">
              {typeOptions.map((t) => (
                <button
                  key={t.value}
                  onClick={() => setField('type', t.value)}
                  className={`flex flex-col items-center gap-2 py-4 rounded-xl border-2 transition-all ${
                    form.type === t.value ? 'border-[#6D28D9] bg-violet-50 text-[#6D28D9]' : 'border-slate-200 text-slate-500 hover:border-violet-200'
                  }`}
                >
                  <t.Icon className="w-6 h-6" />
                  <span className="text-sm font-medium">{t.label}</span>
                </button>
              ))}
            </div>
          </div>
          {/* title */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-2">제목</label>
            <input
              value={form.title}
              onChange={(e) => setField('title', e.target.value)}
              placeholder="예: Neon Nebula Dreamscape"
              className={`${inputCls} ${errors.title ? 'border-red-500' : ''}`}
            />
            {errors.title && <p className="text-xs text-red-500 mt-1">{errors.title}</p>}
          </div>
          {/* description */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-2">설명</label>
            <textarea
              value={form.description}
              onChange={(e) => setField('description', e.target.value)}
              rows={4}
              placeholder="창작물에 대한 설명을 입력하세요"
              className={`${inputCls} resize-none ${errors.description ? 'border-red-500' : ''}`}
            />
            {errors.description && <p className="text-xs text-red-500 mt-1">{errors.description}</p>}
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            {/* category */}
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">카테고리</label>
              <select
                value={form.categoryId}
                onChange={(e) => setField('categoryId', e.target.value)}
                className={`${inputCls} cursor-pointer ${errors.categoryId ? 'border-red-500' : ''}`}
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
              {errors.categoryId && <p className="text-xs text-red-500 mt-1">{errors.categoryId}</p>}
            </div>
            {/* price */}
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">가격 (크레딧)</label>
              <input
                type="number"
                min="0"
                value={form.price}
                onChange={(e) => setField('price', e.target.value)}
                placeholder="예: 120"
                className={`${inputCls} ${errors.price ? 'border-red-500' : ''}`}
              />
              {errors.price && <p className="text-xs text-red-500 mt-1">{errors.price}</p>}
            </div>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            {/* tool */}
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">생성 도구</label>
              <input
                value={form.tool}
                onChange={(e) => setField('tool', e.target.value)}
                placeholder="예: Midjourney v6"
                className={inputCls}
              />
            </div>
            {/* license */}
            <div>
              <label className="block text-sm font-semibold text-slate-700 mb-2">라이선스</label>
              <select
                value={form.license}
                onChange={(e) => setField('license', e.target.value)}
                className={`${inputCls} cursor-pointer`}
              >
                {licenseOptions.map((l) => (
                  <option key={l} value={l}>{l}</option>
                ))}
              </select>
            </div>
          </div>
          {/* tags */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-2">태그</label>
            <input
              value={form.tags}
              onChange={(e) => setField('tags', e.target.value)}
              placeholder="쉼표로 구분 (예: nebula, cosmic, 4k)"
              className={inputCls}
            />
          </div>
          <div className="flex items-start gap-2 p-3 rounded-xl bg-violet-50 text-[#6D28D9] text-xs">
            <Info className="w-4 h-4 flex-shrink-0 mt-0.5" />
            <span>등록된 창작물은 판매 대시보드에서 판매 현황을 확인할 수 있어요.</span>
          </div>
          <button
            onClick={handleSubmit}
            disabled={submitting}
            className="w-full inline-flex items-center justify-center gap-2 px-6 py-4 rounded-xl bg-gradient-to-r from-[#6D28D9] to-[#0891B2] text-white font-semibold text-lg hover:opacity-90 transition-opacity disabled:opacity-60 active:scale-95"
          >
            {submitting ? (
              <><Loader2 className="w-5 h-5 animate-spin" /> 등록 중...</>
            ) : (
              <><Check className="w-5 h-5" /> 창작물 등록하기</>
            )}
          </button>
        </div>
      </div>
    </div>
  );
}