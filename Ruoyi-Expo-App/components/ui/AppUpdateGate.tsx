import { usePathname } from 'expo-router';
import { useCallback, useEffect, useRef, useState } from 'react';
import { AppState, Platform } from 'react-native';

import { UpdateConfirmModal } from '@/components/ui/UpdateConfirmModal';
import { useAuth } from '@/hooks/useAuth';
import { useVersionUpdatePrompt } from '@/hooks/useVersionUpdatePrompt';

const STARTUP_CHECK_DELAY_MS = 800;
const STARTUP_CHECK_RETRY_DELAY_MS = 3_000;
const MAX_STARTUP_CHECK_ATTEMPTS = 2;
const NATIVE_VERSION_POLL_INTERVAL_MS = 60_000;
const OTA_POLL_INTERVAL_MS = 60_000;

/** 冷启动：整包强更 + OTA；前台轮询强制整包与热更新 */
export function AppUpdateGate() {
  const pathname = usePathname();
  const { hydrated } = useAuth();
  const hasStartupCheckedRef = useRef(false);
  const attemptRef = useRef(0);
  const isNativePollingRef = useRef(false);
  const isOtaPollingRef = useRef(false);
  const [startupCheckDone, setStartupCheckDone] = useState(false);
  const { runVersionUpdateCheck, prompt, closePrompt } = useVersionUpdatePrompt();

  const isPastSplash = pathname !== '/splash';
  const isAppReady = hydrated && isPastSplash;

  const runNativeVersionPoll = useCallback(() => {
    if (Platform.OS === 'web' || !isAppReady || isNativePollingRef.current) {
      return;
    }
    isNativePollingRef.current = true;
    void runVersionUpdateCheck({
      nativeOnly: true,
      nativePoll: true,
      silentIfLatest: true,
    }).finally(() => {
      isNativePollingRef.current = false;
    });
  }, [isAppReady, runVersionUpdateCheck]);

  const runOtaPoll = useCallback(() => {
    if (Platform.OS === 'web' || !isAppReady || isOtaPollingRef.current) {
      return;
    }
    isOtaPollingRef.current = true;
    void runVersionUpdateCheck({
      otaOnly: true,
      silentIfLatest: true,
      respectOtaSnooze: true,
    }).finally(() => {
      isOtaPollingRef.current = false;
    });
  }, [isAppReady, runVersionUpdateCheck]);

  useEffect(() => {
    if (!isAppReady || hasStartupCheckedRef.current) {
      return;
    }

    let cancelled = false;
    let retryTimer: ReturnType<typeof setTimeout> | undefined;

    const runStartupCheck = () => {
      let willRetry = false;
      void runVersionUpdateCheck({
        silentIfLatest: true,
        respectOtaSnooze: true,
        onError: () => {
          if (cancelled || attemptRef.current >= MAX_STARTUP_CHECK_ATTEMPTS - 1) {
            return;
          }
          willRetry = true;
          attemptRef.current += 1;
          retryTimer = setTimeout(runStartupCheck, STARTUP_CHECK_RETRY_DELAY_MS);
        },
      }).finally(() => {
        if (cancelled || willRetry) {
          return;
        }
        hasStartupCheckedRef.current = true;
        setStartupCheckDone(true);
      });
    };

    const startTimer = setTimeout(() => {
      if (cancelled || hasStartupCheckedRef.current) {
        return;
      }
      runStartupCheck();
    }, STARTUP_CHECK_DELAY_MS);

    return () => {
      cancelled = true;
      clearTimeout(startTimer);
      if (retryTimer) {
        clearTimeout(retryTimer);
      }
    };
  }, [isAppReady, runVersionUpdateCheck]);

  useEffect(() => {
    if (Platform.OS === 'web' || !isAppReady || !startupCheckDone) {
      return;
    }

    const nativeIntervalId = setInterval(runNativeVersionPoll, NATIVE_VERSION_POLL_INTERVAL_MS);
    const otaIntervalId = setInterval(runOtaPoll, OTA_POLL_INTERVAL_MS);
    const subscription = AppState.addEventListener('change', (state) => {
      if (state === 'active') {
        runNativeVersionPoll();
        runOtaPoll();
      }
    });

    return () => {
      clearInterval(nativeIntervalId);
      clearInterval(otaIntervalId);
      subscription.remove();
    };
  }, [isAppReady, runNativeVersionPoll, runOtaPoll, startupCheckDone]);

  return (
    <UpdateConfirmModal
      options={prompt?.options ?? null}
      onConfirm={() => closePrompt(true)}
      onCancel={() => closePrompt(false)}
    />
  );
}
