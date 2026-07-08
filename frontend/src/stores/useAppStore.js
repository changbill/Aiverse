import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '@/api/authApi';
import { setAccessTokenProvider, setUnauthorizedHandler } from '@/api/httpClient';

export const useAppStore = create(
  persist(
    (set, get) => ({
      user: null,
      userId: null,
      accessToken: null,
      isAuthReady: false,
      credits: 0,
      myUploads: [],
      setAccessToken: (accessToken) => set({ accessToken }),
      setSession: (accessToken, user) =>
        set((state) => {
          const sameUser = state.userId === user?.id;
          return {
            accessToken,
            user,
            userId: user?.id ?? null,
            isAuthReady: true,
            credits: sameUser ? state.credits : (Number(user?.creditBalance) || 0),
            myUploads: sameUser ? state.myUploads : [],
          };
        }),
      clearSession: () => set({ accessToken: null, user: null, userId: null, isAuthReady: true }),
      updateUser: (patch) =>
        set((state) => ({ user: state.user ? { ...state.user, ...patch } : state.user })),
      logout: async () => {
        try {
          await authApi.logout();
        } catch (e) {
          console.error(e);
        } finally {
          get().clearSession();
        }
      },
      setCredits: (credits) => set({ credits }),
      addUpload: (content) => set((state) => ({ myUploads: [content, ...state.myUploads] })),
    }),
    {
      name: 'aiverse-store',
      partialize: (state) => {
        const { accessToken, isAuthReady, user, ...rest } = state;
        return rest;
      },
    }
  )
);

setAccessTokenProvider(() => useAppStore.getState().accessToken);
setUnauthorizedHandler(restoreSession);

export async function restoreSession() {
  try {
    const { data } = await authApi.reissue();
    useAppStore.getState().setAccessToken(data.accessToken);
    const me = await authApi.me();
    useAppStore.getState().setSession(data.accessToken, me.data);
    return true;
  } catch {
    useAppStore.getState().clearSession();
    return false;
  }
}