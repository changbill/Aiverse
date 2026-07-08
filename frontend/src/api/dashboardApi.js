import { httpClient } from './httpClient';

export const dashboardApi = {
  getSales: async (period = '30D') => {
    const res = await httpClient.get('/api/dashboard/sales', { params: { period } });
    return res.data;
  },
};
