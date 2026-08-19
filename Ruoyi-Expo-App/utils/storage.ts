import { Platform } from 'react-native';

const TOKEN_KEY = 'RUOYI_ADMIN_TOKEN';

let memoryToken: string | null | undefined;

function webStorage(): Storage | null {
  if (typeof localStorage === 'undefined') {
    return null;
  }
  return localStorage;
}

export function getTokenSync(): string | null {
  if (memoryToken !== undefined) {
    return memoryToken;
  }
  if (Platform.OS === 'web') {
    memoryToken = webStorage()?.getItem(TOKEN_KEY) ?? null;
    return memoryToken;
  }
  return null;
}

export async function getToken(): Promise<string | null> {
  if (memoryToken !== undefined) {
    return memoryToken;
  }
  if (Platform.OS === 'web') {
    return getTokenSync();
  }
  const SecureStore = await import('expo-secure-store');
  const token = await SecureStore.getItemAsync(TOKEN_KEY);
  memoryToken = token;
  return token;
}

export async function setToken(token: string): Promise<void> {
  memoryToken = token;
  if (Platform.OS === 'web') {
    webStorage()?.setItem(TOKEN_KEY, token);
    return;
  }
  const SecureStore = await import('expo-secure-store');
  await SecureStore.setItemAsync(TOKEN_KEY, token);
}

export async function removeToken(): Promise<void> {
  memoryToken = null;
  if (Platform.OS === 'web') {
    webStorage()?.removeItem(TOKEN_KEY);
    return;
  }
  const SecureStore = await import('expo-secure-store');
  await SecureStore.deleteItemAsync(TOKEN_KEY);
}
