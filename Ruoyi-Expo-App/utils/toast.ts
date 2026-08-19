import { Alert, Platform } from 'react-native';

type ToastHandler = (message: string) => void;

let handler: ToastHandler | null = null;

export function setToastHandler(next: ToastHandler | null): void {
  handler = next;
}

export function toast(message: string): void {
  if (handler) {
    handler(message);
    return;
  }
  if (Platform.OS === 'web' && typeof window !== 'undefined') {
    window.alert(message);
    return;
  }
  Alert.alert('', message);
}
