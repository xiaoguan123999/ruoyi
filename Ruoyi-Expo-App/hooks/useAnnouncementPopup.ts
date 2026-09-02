import { useCallback, useEffect, useRef, useState } from 'react';
import { AppState, Platform, type AppStateStatus } from 'react-native';

import { fetchAppNoticeDetail, fetchAppNotices, NOTICE_TYPE_ANNOUNCEMENT } from '@/api/app-notice';
import type { AppNotice, AppNoticeDetail } from '@/api/types';
import {
  finishAnnouncementPopupQueue,
  isAnnouncementPopupFinished,
  resetAnnouncementPopupSession,
} from '@/utils/announcement-popup-session';

function toDetail(item: AppNotice): AppNoticeDetail | null {
  const html = item.contentHtml?.trim() || '';
  if (!html && !item.content?.trim()) {
    return null;
  }
  return {
    ...item,
    content: item.content ?? '',
    contentHtml: html || item.content || '',
  };
}

export function useAnnouncementPopup() {
  const [notice, setNotice] = useState<AppNoticeDetail | null>(null);
  const [index, setIndex] = useState(0);
  const [total, setTotal] = useState(0);
  const queueRef = useRef<AppNotice[]>([]);
  const indexRef = useRef(0);
  const openingRef = useRef(false);

  const showAt = useCallback(async (nextIndex: number) => {
    const queue = queueRef.current;
    if (nextIndex >= queue.length) {
      finishAnnouncementPopupQueue();
      setNotice(null);
      setTotal(0);
      setIndex(0);
      return;
    }
    const item = queue[nextIndex];
    let detail = toDetail(item);
    if (!detail) {
      try {
        detail = await fetchAppNoticeDetail(item.id);
      } catch {
        detail = null;
      }
    }
    if (isAnnouncementPopupFinished()) {
      return;
    }
    if (!detail) {
      await showAt(nextIndex + 1);
      return;
    }
    indexRef.current = nextIndex;
    setIndex(nextIndex);
    setTotal(queue.length);
    setNotice(detail);
  }, []);

  const close = useCallback(() => {
    void showAt(indexRef.current + 1);
  }, [showAt]);

  const tryOpen = useCallback(async () => {
    if (isAnnouncementPopupFinished() || openingRef.current || queueRef.current.length > 0) {
      return;
    }
    openingRef.current = true;
    try {
      const list = await fetchAppNotices(NOTICE_TYPE_ANNOUNCEMENT);
      if (isAnnouncementPopupFinished()) {
        return;
      }
      if (list.length === 0) {
        return;
      }
      queueRef.current = list;
      await showAt(0);
    } catch {
      queueRef.current = [];
    } finally {
      openingRef.current = false;
    }
  }, [showAt]);

  const resetQueue = useCallback(() => {
    resetAnnouncementPopupSession();
    queueRef.current = [];
    indexRef.current = 0;
    openingRef.current = false;
    setNotice(null);
    setIndex(0);
    setTotal(0);
  }, []);

  useEffect(() => {
    void tryOpen();
  }, [tryOpen]);

  useEffect(() => {
    if (Platform.OS === 'web') {
      return;
    }
    const onChange = (state: AppStateStatus) => {
      if (state === 'background') {
        resetQueue();
        return;
      }
      if (state === 'active') {
        void tryOpen();
      }
    };
    const sub = AppState.addEventListener('change', onChange);
    return () => sub.remove();
  }, [resetQueue, tryOpen]);

  return { notice, index, total, close };
}
