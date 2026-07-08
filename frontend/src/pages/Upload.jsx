import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Image as ImageIcon, Video, Music, Upload as UploadIcon, Check, Loader2, X, Info, FileCheck } from 'lucide-react';
import { contentApi, TYPE_TO_ASSET_TYPE } from '@/api/contentApi';
import { categoryApi } from '@/api/categoryApi';
import { fileApi } from '@/api/fileApi';
import { useAppStore } from '@/stores/useAppStore';
import confetti from 'canvas-confetti';
const typeOptions = [
  { value: 'image', label: '이미지', Icon: ImageIcon },
  { value: 'video', label: '영상', Icon: Video },
  { value: 'music', label: '음악', Icon: Music },
];
const licenseOptions = [
  { value: 'PERSONAL', label: '개인 이용' },
  { value: 'COMMERCIAL', label: '상업적 이용 가능' },
];
const COVER_LIMIT = { maxSize: 10 * 1024 * 1024, accept: 'image/jpeg,image/png,image/webp' };
const ORIGINAL_LIMITS = {
  image: { maxSize: 50 * 1024 * 1024, accept: 'image/jpeg,image/png,image/webp', hint: 'JPEG/PNG/WebP, 최대 50MB' },
  video: { maxSize: 1024 * 1024 * 1024, accept: 'video/mp4,video/webm', hint: 'MP4/WebM, 최대 1GB' },
  music: { maxSize: 200 * 1024 * 1024, accept: 'audio/mpeg,audio/wav,audio/flac', hint: 'MP3/WAV/FLAC, 최대 200MB' },
};
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
    license: 'PERSONAL',
  });
  const [coverObjectKey, setCoverObjectKey] = useState('');
  const [coverPreviewUrl, setCoverPreviewUrl] = useState('');
  const [uploadingCover, setUploadingCover] = useState(false);
  const [originalFile, setOriginalFile] = useState(null);
  const [uploadingOriginal, setUploadingOriginal] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errors, setErrors] = useState({});
  const [notice, setNotice] = useState(null);
  useEffect(() => {
    if (!user) navigate('/Login', { replace: true });
  }, [user, navigate]);
  useEffect(() => {
    (async () => {
      try {
        const list = await categoryApi.list();
        setCategories(list);
        if (list[0]) setForm((f) => ({ ...f, categoryId: list[0].id }));
      } catch (e) {
        console.error(e);
      }
    })();
  }, []);
  if (!user) return null;
  const setField = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  const onTypeChange = (type) => {
    setField('type', type);
    setOriginalFile(null);
  };
  const onCoverChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    if (file.size > COVER_LIMIT.maxSize) {
      setNotice({ type: 'error', message: '미리보기 이미지는 10MB를 초과할 수 없어요.' });
      return;
    }
    setUploadingCover(true);
    setNotice(null);
    try {
      const { data } = await fileApi.requestUploadUrl({
        purpose: 'COVER',
        assetType: TYPE_TO_ASSET_TYPE[form.type],
        fileName: file.name,
        contentType: file.type,
        fileSize: file.size,
      });
      await fileApi.uploadToStorage(data.uploadUrl, file);
      setCoverObjectKey(data.objectKey);
      setCoverPreviewUrl(URL.createObjectURL(file));
    } catch (err) {
      console.error(err);
      setNotice({ type: 'error', message: '이미지 업로드에 실패했어요. 다시 시도해주세요.' });
    } finally {
      setUploadingCover(false);
    }
  };
  const onOriginalChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const limit = ORIGINAL_LIMITS[form.type];
    if (file.size > limit.maxSize) {
      setNotice({ type: 'error', message: `원본 파일 크기가 허용 범위를 초과했어요 (${limit.hint}).` });
      return;
    }
    setUploadingOriginal(true);
    setNotice(null);
    try {
      const { data } = await fileApi.requestUploadUrl({
        purpose: 'ORIGINAL',
        assetType: TYPE_TO_ASSET_TYPE[form.type],
        fileName: file.name,
        contentType: file.type,
        fileSize: file.size,
      });
      await fileApi.uploadToStorage(data.uploadUrl, file);
      setOriginalFile({ objectKey: data.objectKey, fileName: file.name, contentType: file.type, fileSize: file.size });
    } catch (err) {
      console.error(err);
      setNotice({ type: 'error', message: '원본 파일 업로드에 실패했어요. 다시 시도해주세요.' });
    } finally {
      setUploadingOriginal(false);
    }
  };
  const validate = () => {
    const e = {};
    if (!form.title.trim()) e.title = '제목을 입력해주세요';
    if (!form.description.trim()) e.description = '설명을 입력해주세요';
    if (!form.categoryId) e.categoryId = '카테고리를 선택해주세요';
    if (form.price === '' || Number(form.price) <= 0) e.price = '올바른 가격을 입력해주세요';
    if (!coverObjectKey) e.cover = '미리보기 이미지를 업로드해주세요';
    if (!originalFile) e.original = '원본 파일을 업로드해주세요';
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
        description: form.description.trim(),
        assetType: TYPE_TO_ASSET_TYPE[form.type],
        categoryId: Number(form.categoryId),
        previewObjectKey: coverObjectKey,
        originalObjectKey: originalFile.objectKey,
        originalFilename: originalFile.fileName,
        contentType: originalFile.contentType,
        fileSize: originalFile.fileSize,
        priceCredit: Number(form.price),
        aiTool: form.tool.trim(),
        licenseType: form.license,
        tags: form.tags.split(',').map((t) => t.trim()).filter(Boolean),
      };
      const created = await contentApi.create(payload);
      addUpload(created);
      confetti({ particleCount: 100, spread: 70, origin: { y: 0.6 }, colors: ['#6D28D9', '#0891B2', '#e879f9'] });
      navigate('/Dashboard');
    } catch (err) {
      console.error(err);
      setNotice({ type: 'error', message: err.message || '등록에 실패했어요. 잠시 후 다시 시도해주세요.' });
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
          {/* type */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-2">유형</label>
            <div className="grid grid-cols-3 gap-3">
              {typeOptions.map((t) => (
                <button
                  key={t.value}
                  onClick={() => onTypeChange(t.value)}
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
          {/* cover */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-2">미리보기 커버 이미지</label>
            {coverPreviewUrl ? (
              <div className="relative w-full aspect-[4/3] rounded-2xl overflow-hidden border border-violet-100">
                <img src={coverPreviewUrl} alt="preview" className="w-full h-full object-cover" />
                <button
                  onClick={() => { setCoverObjectKey(''); setCoverPreviewUrl(''); }}
                  className="absolute top-3 right-3 p-1.5 rounded-full bg-black/50 text-white hover:bg-black/70"
                  aria-label="이미지 제거"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <label className={`flex flex-col items-center justify-center w-full aspect-[4/3] rounded-2xl border-2 border-dashed bg-violet-50/50 cursor-pointer hover:bg-violet-50 transition-colors ${errors.cover ? 'border-red-400' : 'border-violet-200'}`}>
                {uploadingCover ? (
                  <Loader2 className="w-8 h-8 text-[#6D28D9] animate-spin" />
                ) : (
                  <>
                    <UploadIcon className="w-8 h-8 text-[#6D28D9]" />
                    <span className="mt-2 text-sm text-slate-500">클릭하여 커버 이미지 업로드 (JPEG/PNG/WebP, 최대 10MB)</span>
                  </>
                )}
                <input type="file" accept={COVER_LIMIT.accept} className="hidden" onChange={onCoverChange} disabled={uploadingCover} />
              </label>
            )}
            {errors.cover && <p className="text-xs text-red-500 mt-1">{errors.cover}</p>}
          </div>
          {/* original */}
          <div>
            <label className="block text-sm font-semibold text-slate-700 mb-2">원본 파일</label>
            {originalFile ? (
              <div className={`flex items-center justify-between gap-3 p-4 rounded-xl border ${errors.original ? 'border-red-400' : 'border-violet-100'} bg-violet-50/50`}>
                <span className="inline-flex items-center gap-2 text-sm text-slate-700 min-w-0">
                  <FileCheck className="w-5 h-5 text-[#6D28D9] flex-shrink-0" />
                  <span className="truncate">{originalFile.fileName}</span>
                </span>
                <button
                  onClick={() => setOriginalFile(null)}
                  className="p-1.5 rounded-full text-slate-400 hover:text-slate-600 hover:bg-white flex-shrink-0"
                  aria-label="원본 파일 제거"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
            ) : (
              <label className={`flex flex-col items-center justify-center w-full py-8 rounded-2xl border-2 border-dashed bg-violet-50/50 cursor-pointer hover:bg-violet-50 transition-colors ${errors.original ? 'border-red-400' : 'border-violet-200'}`}>
                {uploadingOriginal ? (
                  <Loader2 className="w-8 h-8 text-[#6D28D9] animate-spin" />
                ) : (
                  <>
                    <UploadIcon className="w-8 h-8 text-[#6D28D9]" />
                    <span className="mt-2 text-sm text-slate-500">클릭하여 원본 파일 업로드 ({ORIGINAL_LIMITS[form.type].hint})</span>
                  </>
                )}
                <input type="file" accept={ORIGINAL_LIMITS[form.type].accept} className="hidden" onChange={onOriginalChange} disabled={uploadingOriginal} />
              </label>
            )}
            {errors.original && <p className="text-xs text-red-500 mt-1">{errors.original}</p>}
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
                min="1"
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
                  <option key={l.value} value={l.value}>{l.label}</option>
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
            disabled={submitting || uploadingCover || uploadingOriginal}
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
