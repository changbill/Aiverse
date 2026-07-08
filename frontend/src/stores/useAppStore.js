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
      purchases: [],
      myUploads: [],
      transactions: [],
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
            purchases: sameUser ? state.purchases : [],
            myUploads: sameUser ? state.myUploads : [],
            transactions: sameUser ? state.transactions : [],
          };
        }),
      clearSession: () => set({ accessToken: null, user: null, userId: null, isAuthReady: true }),
      loginUser: (user) =>
        set((state) => {
          const sameUser = state.userId === user.id;
          return {
            user,
            userId: user.id,
            credits: sameUser ? state.credits : (Number(user.credits) || 1000),
            purchases: sameUser ? state.purchases : [],
            myUploads: sameUser ? state.myUploads : [],
            transactions: sameUser ? state.transactions : [],
          };
        }),
      updateUser: (patch) =>
        set((state) => ({ user: state.user ? { ...state.user, ...patch } : state.user })),
      logout: () => set({ user: null }),
      addCredits: (amount, label) =>
        set((state) => {
          const balance = state.credits + amount;
          return {
            credits: balance,
            transactions: [
              {
                type: 'charge',
                amount,
                balance,
                description: label || '크레딧 충전',
                createdAt: new Date().toISOString(),
              },
              ...state.transactions,
            ],
          };
        }),
      purchaseContent: (content) => {
        const state = get();
        if (state.purchases.some((p) => p.contentId === content.id)) {
          return { success: false, message: '이미 구매한 콘텐츠입니다.' };
        }
        if (state.credits < content.price) {
          return { success: false, message: '크레딧이 부족합니다.' };
        }
        const balance = state.credits - content.price;
        set({
          credits: balance,
          purchases: [
            {
              contentId: content.id,
              title: content.title,
              slug: content.slug,
              type: content.type,
              thumbnail: content.thumbnail,
              license: content.license,
              price: content.price,
              creatorName: content.creatorName,
              tool: content.tool,
              purchasedAt: new Date().toISOString(),
            },
            ...state.purchases,
          ],
          transactions: [
            {
              type: 'purchase',
              amount: -content.price,
              balance,
              description: `${content.title} 구매`,
              createdAt: new Date().toISOString(),
            },
            ...state.transactions,
          ],
        });
        return { success: true, message: '구매가 완료되었습니다!' };
      },
      isPurchased: (contentId) => get().purchases.some((p) => p.contentId === contentId),
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