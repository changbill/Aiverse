import { Link } from 'react-router-dom';
import { Image, Video, Music, Eye, Heart } from 'lucide-react';
const typeMeta = {
  image: { label: '이미지', Icon: Image },
  video: { label: '영상', Icon: Video },
  music: { label: '음악', Icon: Music },
};
export default function ContentCard({ content }) {
  const meta = typeMeta[content.type] || typeMeta.image;
  const Icon = meta.Icon;
  return (
    <Link
      to={`/content/${content.slug}`}
      className="group block bg-white rounded-2xl border border-violet-100 overflow-hidden transition-all duration-300 hover:shadow-xl hover:shadow-violet-200/50 hover:scale-[1.02]"
    >
      <div className="relative aspect-[4/3] overflow-hidden bg-slate-100">
        {content.thumbnail ? (
          <img
            src={content.thumbnail}
            alt={content.title}
            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#6D28D9] to-[#0891B2]">
            <Icon className="w-10 h-10 text-white" />
          </div>
        )}
        <div className="absolute inset-0 bg-gradient-to-t from-black/45 via-transparent to-transparent" />
        <span className="absolute top-3 left-3 inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-white/90 backdrop-blur text-xs font-semibold text-[#6D28D9]">
          <Icon className="w-3.5 h-3.5" /> {meta.label}
        </span>
      </div>
      <div className="p-4">
        <h3 className="font-semibold text-slate-900 line-clamp-1">{content.title}</h3>
        <p className="text-sm text-slate-500 mt-1 line-clamp-1">{content.creatorName}</p>
        <div className="flex items-center justify-between mt-3">
          <span className="font-display font-bold text-[#6D28D9]">
            {Number(content.price || 0).toLocaleString('ko-KR')}
            <span className="text-xs text-slate-400 font-normal ml-1">크레딧</span>
          </span>
          <div className="flex items-center gap-3 text-xs text-slate-400">
            <span className="inline-flex items-center gap-1">
              <Eye className="w-3.5 h-3.5" />
              {Number(content.views || 0).toLocaleString('ko-KR')}
            </span>
            <span className="inline-flex items-center gap-1">
              <Heart className="w-3.5 h-3.5" />
              {Number(content.likes || 0).toLocaleString('ko-KR')}
            </span>
          </div>
        </div>
      </div>
    </Link>
  );
}