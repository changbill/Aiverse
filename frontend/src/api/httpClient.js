const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export class ApiError extends Error {
  constructor({ code, message, fieldErrors, requestId, status }) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.fieldErrors = fieldErrors ?? [];
    this.requestId = requestId;
    this.status = status;
  }
}

let accessTokenProvider = () => null;
let unauthorizedHandler = null;

export function setAccessTokenProvider(provider) {
  accessTokenProvider = provider;
}

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

async function request(path, { method = 'GET', body, params, skipAuth = false, retryOn401 = true } = {}) {
  const url = new URL(path, API_BASE_URL);
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        url.searchParams.set(key, value);
      }
    });
  }

  const headers = { 'Content-Type': 'application/json' };
  const token = !skipAuth ? accessTokenProvider() : null;
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    method,
    headers,
    credentials: 'include',
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (response.status === 204) {
    return null;
  }

  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    if (response.status === 401 && !skipAuth && retryOn401 && unauthorizedHandler) {
      const recovered = await unauthorizedHandler();
      if (recovered) {
        return request(path, { method, body, params, skipAuth, retryOn401: false });
      }
    }
    throw new ApiError({
      code: payload?.code ?? 'UNKNOWN_ERROR',
      message: payload?.message ?? '알 수 없는 오류가 발생했습니다.',
      fieldErrors: payload?.fieldErrors,
      requestId: payload?.requestId,
      status: response.status,
    });
  }

  return payload;
}

export const httpClient = {
  get: (path, options) => request(path, { ...options, method: 'GET' }),
  post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
  put: (path, body, options) => request(path, { ...options, method: 'PUT', body }),
  delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
};
