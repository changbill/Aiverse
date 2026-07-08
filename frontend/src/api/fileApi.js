import { httpClient } from './httpClient';

export const fileApi = {
  requestUploadUrl: (payload) => httpClient.post('/api/files/upload', payload),
  uploadToStorage: async (uploadUrl, file) => {
    const res = await fetch(uploadUrl, {
      method: 'PUT',
      headers: { 'Content-Type': file.type },
      body: file,
    });
    if (!res.ok) {
      throw new Error('파일 업로드에 실패했습니다.');
    }
  },
};
