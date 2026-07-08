import { httpClient } from './httpClient';

const ASSET_TYPE_TO_TYPE = { IMAGE: 'image', VIDEO: 'video', MUSIC: 'music' };

const SORT_TO_BACKEND = {
  '-createdAt': 'LATEST',
  '-views': 'POPULAR',
  price: 'PRICE_ASC',
  '-price': 'PRICE_DESC',
};

function normalizeAsset(asset) {
  return {
    id: asset.id,
    title: asset.title,
    description: asset.description,
    type: ASSET_TYPE_TO_TYPE[asset.assetType] || 'image',
    categoryId: asset.categoryId,
    thumbnail: asset.previewUrl,
    price: asset.priceCredit,
    tool: asset.aiTool,
    license: asset.licenseType,
    views: asset.viewCount,
    creatorId: asset.creatorId,
    creatorName: asset.creatorNickname,
    tags: asset.tags || [],
    createdAt: asset.createdAt,
    updatedAt: asset.updatedAt,
  };
}

export const contentApi = {
  list: async ({ page = 1, limit = 20, search, type, categoryId, tag, minPrice, maxPrice, creatorId, sort } = {}) => {
    const res = await httpClient.get('/api/contents', {
      params: {
        page: Math.max(page - 1, 0),
        size: limit,
        search,
        type,
        categoryId,
        tag,
        minPrice,
        maxPrice,
        creatorId,
        sort: SORT_TO_BACKEND[sort] || sort,
      },
    });
    return { items: res.data.map(normalizeAsset), page: res.page };
  },
  get: async (id) => {
    const res = await httpClient.get(`/api/contents/${id}`);
    return normalizeAsset(res.data);
  },
};
