import { httpClient } from './httpClient';

const ASSET_TYPE_TO_TYPE = { IMAGE: 'image', VIDEO: 'video', MUSIC: 'music' };

function normalizeLibraryItem(item) {
  return {
    purchaseId: item.purchaseId,
    contentId: item.asset.id,
    title: item.asset.title,
    thumbnail: item.asset.previewUrl,
    type: ASSET_TYPE_TO_TYPE[item.asset.assetType] || 'image',
    deleted: item.asset.deleted,
    price: item.purchasePriceCredit,
    license: item.licenseType,
    purchasedAt: item.purchasedAt,
  };
}

export const purchaseApi = {
  purchase: async (assetId) => {
    const res = await httpClient.post(
      '/api/purchases',
      { assetId },
      { headers: { 'Idempotency-Key': crypto.randomUUID() } }
    );
    return res.data;
  },
  library: async ({ page = 1, size = 20 } = {}) => {
    const res = await httpClient.get('/api/library', { params: { page: Math.max(page - 1, 0), size } });
    return { items: res.data.map(normalizeLibraryItem), page: res.page };
  },
  download: async (assetId) => {
    const res = await httpClient.post('/api/downloads', { assetId });
    return res.data;
  },
};
