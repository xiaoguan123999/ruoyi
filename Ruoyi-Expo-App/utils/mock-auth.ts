import { clearCurrentUser, markAuthenticated, setCurrentUser } from '@/api/auth-state';
import { mockUser } from '@/constants/mock';
import { removeToken, setToken } from '@/utils/storage';

export async function mockSignIn(phone?: string): Promise<void> {
  await setToken('mock-local-token');
  setCurrentUser(
    {
      userId: mockUser.userId,
      userName: phone?.trim() || mockUser.userName,
      nickName: mockUser.nickName,
      avatar: mockUser.avatar,
    },
    ['user'],
    [],
  );
  markAuthenticated();
}

export async function mockSignOut(): Promise<void> {
  await removeToken();
  clearCurrentUser();
}
