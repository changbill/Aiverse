import { httpClient } from './httpClient';

export const authApi = {
  register: (payload) => httpClient.post('/api/auth/register', payload, { skipAuth: true }),
  login: (payload) => httpClient.post('/api/auth/login', payload, { skipAuth: true }),
  reissue: () => httpClient.post('/api/auth/reissue', undefined, { skipAuth: true, retryOn401: false }),
  logout: () => httpClient.post('/api/auth/logout', undefined, { skipAuth: true }),
  me: () => httpClient.get('/api/auth/me'),
  updateMe: (payload) => httpClient.put('/api/auth/me', payload),
};
