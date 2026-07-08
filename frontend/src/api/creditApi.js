import { httpClient } from './httpClient';

export const creditApi = {
  listProducts: async () => {
    const res = await httpClient.get('/api/credit-products');
    return res.data;
  },
  pay: async (creditProductId) => {
    const res = await httpClient.post(
      '/api/payments',
      { creditProductId },
      { headers: { 'Idempotency-Key': crypto.randomUUID() } }
    );
    return res.data;
  },
  listTransactions: async ({ type, page = 1, size = 20 } = {}) => {
    const res = await httpClient.get('/api/credit-transactions', { params: { type, page: Math.max(page - 1, 0), size } });
    return { items: res.data, page: res.page };
  },
};
