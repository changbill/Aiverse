import { httpClient } from './httpClient';

export const categoryApi = {
  list: async () => {
    const res = await httpClient.get('/api/categories');
    return res.data;
  },
};
