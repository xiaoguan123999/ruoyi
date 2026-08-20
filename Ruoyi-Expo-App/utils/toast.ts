import { Alert, Platform } from 'react-native';

export type ToastType = 'success' | 'warning' | 'error' | 'info';

export type ToastPresentation = 'toast' | 'modal';

export type ToastOptions = {
  type?: ToastType;
  presentation?: ToastPresentation;
  duration?: number;
  autoClose?: boolean;
  showButton?: boolean;
  buttonText?: string;
};

export type ToastPayload = {
  message: string;
  type: ToastType;
  presentation: ToastPresentation;
  duration: number;
  autoClose: boolean;
  showButton: boolean;
  buttonText: string;
};

type ToastHandler = (payload: ToastPayload) => void;
type DismissHandler = () => void;

const DEFAULT_DURATION = 2500;
const DEFAULT_BUTTON_TEXT = '知道了';

let handler: ToastHandler | null = null;
let dismissHandler: DismissHandler | null = null;
const dismissListeners = new Set<() => void>();

export function setToastHandler(next: ToastHandler | null): void {
  handler = next;
}

export function setDismissToastHandler(next: DismissHandler | null): void {
  dismissHandler = next;
}

export function notifyToastDismissed(): void {
  for (const listener of dismissListeners) {
    listener();
  }
  dismissListeners.clear();
}

function onToastDismissed(cb: () => void): () => void {
  dismissListeners.add(cb);
  return () => {
    dismissListeners.delete(cb);
  };
}

export function dismissToast(): void {
  dismissHandler?.();
}

function normalizeOptions(input?: ToastType | ToastOptions): Omit<ToastPayload, 'message'> {
  const opts: ToastOptions = typeof input === 'string' ? { type: input } : (input ?? {});
  return {
    type: opts.type ?? 'info',
    presentation: opts.presentation ?? 'toast',
    duration: opts.duration ?? DEFAULT_DURATION,
    autoClose: opts.autoClose ?? true,
    showButton: opts.showButton ?? false,
    buttonText: opts.buttonText ?? DEFAULT_BUTTON_TEXT,
  };
}

function showToast(message: string, input?: ToastType | ToastOptions): void {
  const payload: ToastPayload = { message, ...normalizeOptions(input) };
  if (handler) {
    handler(payload);
    return;
  }
  if (Platform.OS === 'web' && typeof window !== 'undefined') {
    window.alert(message);
    return;
  }
  Alert.alert('', message);
}

export function toastThenNavigate(
  message: string,
  navigate: () => void,
  input?: ToastType | ToastOptions,
): void {
  let done = false;
  const run = () => {
    if (done) {
      return;
    }
    done = true;
    unsubscribe();
    navigate();
  };
  const unsubscribe = onToastDismissed(run);
  showToast(message, input);
  if (!handler) {
    run();
  }
}

export function toast(message: string, options?: ToastType | ToastOptions): void {
  showToast(message, options);
}

export function toastSuccess(message: string, options?: Omit<ToastOptions, 'type'>): void {
  showToast(message, { ...options, type: 'success' });
}

export function toastWarning(message: string, options?: Omit<ToastOptions, 'type'>): void {
  showToast(message, { ...options, type: 'warning' });
}

export function toastError(message: string, options?: Omit<ToastOptions, 'type'>): void {
  showToast(message, { ...options, type: 'error' });
}

export function toastInfo(message: string, options?: Omit<ToastOptions, 'type'>): void {
  showToast(message, { ...options, type: 'info' });
}

export function modalToast(message: string, options?: ToastType | ToastOptions): void {
  const opts: ToastOptions = typeof options === 'string' ? { type: options } : (options ?? {});
  showToast(message, { ...opts, presentation: 'modal' });
}

export function modalSuccess(message: string, options?: Omit<ToastOptions, 'type' | 'presentation'>): void {
  modalToast(message, { ...options, type: 'success' });
}

export function modalWarning(message: string, options?: Omit<ToastOptions, 'type' | 'presentation'>): void {
  modalToast(message, { ...options, type: 'warning' });
}

export function modalError(message: string, options?: Omit<ToastOptions, 'type' | 'presentation'>): void {
  modalToast(message, { ...options, type: 'error' });
}

export function modalInfo(message: string, options?: Omit<ToastOptions, 'type' | 'presentation'>): void {
  modalToast(message, { ...options, type: 'info' });
}
