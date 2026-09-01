import { useCallback, useState } from 'react';

import {
  checkNativeAppUpdate,
  getAppRuntimeVersion,
  openAppUpdateUrl,
} from '@/api/app-version';
import {
  UpdateConfirmModal,
  type UpdateConfirmOptions,
} from '@/components/ui/UpdateConfirmModal';
import { isOtaSnoozed, setOtaSnooze } from '@/utils/ota-update-snooze';
import { checkOtaUpdate, fetchAndReloadOtaUpdate } from '@/utils/ota-updates';

const NATIVE_VERSION_CHECK_TIMEOUT_MS = 15_000;
const OTA_CHECK_TIMEOUT_MS = 30_000;
const OTA_APPLY_TIMEOUT_MS = 120_000;

type VersionUpdatePromptOptions = {
  silentIfLatest?: boolean;
  respectOtaSnooze?: boolean;
  nativeOnly?: boolean;
  otaOnly?: boolean;
  nativePoll?: boolean;
  onLatest?: () => void;
  onError?: () => void;
  onOpenUrlFailed?: () => void;
  onUnsupported?: () => void;
  onOtaApplied?: () => void;
};

function withTimeout<T>(promise: Promise<T>, ms: number): Promise<T> {
  return new Promise((resolve, reject) => {
    const timer = setTimeout(() => reject(new Error('timeout')), ms);
    promise.then(
      (value) => {
        clearTimeout(timer);
        resolve(value);
      },
      (error: unknown) => {
        clearTimeout(timer);
        reject(error);
      },
    );
  });
}

export function useVersionUpdatePrompt() {
  const currentVersion = getAppRuntimeVersion();
  const [isChecking, setIsChecking] = useState(false);
  const [dialog, setDialog] = useState<{
    options: UpdateConfirmOptions;
    resolve: (ok: boolean) => void;
  } | null>(null);

  const confirm = useCallback((options: UpdateConfirmOptions) => {
    return new Promise<boolean>((resolve) => {
      setDialog({ options, resolve });
    });
  }, []);

  const closeDialog = useCallback((ok: boolean) => {
    setDialog((current) => {
      current?.resolve(ok);
      return null;
    });
  }, []);

  const runVersionUpdateCheck = useCallback(
    async (options?: VersionUpdatePromptOptions) => {
      const {
        silentIfLatest = false,
        respectOtaSnooze = false,
        nativeOnly = false,
        otaOnly = false,
        nativePoll = false,
        onLatest,
        onError,
        onOpenUrlFailed,
        onUnsupported,
        onOtaApplied,
      } = options || {};

      setIsChecking(true);

      try {
        if (!otaOnly) {
          let nativeResult: Awaited<ReturnType<typeof checkNativeAppUpdate>>;
          try {
            nativeResult = await withTimeout(checkNativeAppUpdate(), NATIVE_VERSION_CHECK_TIMEOUT_MS);
          } catch {
            nativeResult = {
              hasUpdate: false,
              version: null,
              currentVersion,
              isSupportedPlatform: true,
            };
          }

          if (!nativeResult.isSupportedPlatform) {
            onUnsupported?.();
            return;
          }

          if (nativeResult.hasUpdate && nativeResult.version) {
            if (nativePoll && !nativeResult.version.forceUpdate) {
              return;
            }

            const latest = nativeResult.version;
            const isForce = latest.forceUpdate;
            const notes = latest.description.trim() || '请更新到最新版本后继续使用';
            const confirmed = await confirm({
              title: isForce ? '必须更新' : '发现新版本',
              description: `当前 ${currentVersion}，最新 ${latest.version}\n${notes}`,
              confirmText: '立即更新',
              cancelText: isForce ? null : '稍后',
            });

            if (!confirmed) {
              return;
            }

            const opened = await openAppUpdateUrl(latest.downloadUrl);
            if (!opened) {
              onOpenUrlFailed?.();
            }
            return;
          }

          if (nativeOnly) {
            return;
          }
        }

        if (respectOtaSnooze && (await isOtaSnoozed())) {
          return;
        }

        let otaResult: Awaited<ReturnType<typeof checkOtaUpdate>>;
        try {
          otaResult = await withTimeout(checkOtaUpdate(), OTA_CHECK_TIMEOUT_MS);
        } catch {
          onError?.();
          return;
        }

        if (otaResult.status === 'available') {
          const confirmed = await confirm({
            title: '发现热更新',
            description: '有新的应用内容，更新后即可使用，无需重新安装。',
            confirmText: '立即更新',
            cancelText: '稍后提醒',
          });

          if (!confirmed) {
            if (respectOtaSnooze) {
              await setOtaSnooze();
            }
            return;
          }

          try {
            const reloaded = await withTimeout(fetchAndReloadOtaUpdate(), OTA_APPLY_TIMEOUT_MS);
            if (reloaded) {
              onOtaApplied?.();
            } else {
              onError?.();
            }
          } catch {
            onError?.();
          }
          return;
        }

        if (otaResult.status === 'error') {
          onError?.();
          return;
        }

        if (!silentIfLatest) {
          onLatest?.();
        }
      } catch {
        onError?.();
      } finally {
        setIsChecking(false);
      }
    },
    [confirm, currentVersion],
  );

  return {
    isChecking,
    currentVersion,
    runVersionUpdateCheck,
    prompt: dialog,
    closePrompt: closeDialog,
  };
}
