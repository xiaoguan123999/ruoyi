import { Modal, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';

import type { AppNoticeDetail } from '@/api/types';
import { NoticeHtmlContent } from '@/components/ui/NoticeHtmlContent';
import { colors } from '@/theme/colors';

type Props = {
  notice: AppNoticeDetail | null;
  index?: number;
  total?: number;
  onClose: () => void;
};

export function AnnouncementPopup({ notice, index = 0, total = 0, onClose }: Props) {
  if (!notice) {
    return null;
  }

  const hasMore = total > 1 && index + 1 < total;

  return (
    <Modal visible transparent animationType="fade" statusBarTranslucent onRequestClose={onClose}>
      <View style={styles.mask}>
        <View style={styles.card}>
          <Text style={styles.kicker}>
            公告{total > 1 ? `  ${index + 1}/${total}` : ''}
          </Text>
          <Text style={styles.title}>{notice.title}</Text>
          {notice.createTime ? <Text style={styles.date}>{notice.createTime}</Text> : null}
          <ScrollView
            style={styles.scroll}
            contentContainerStyle={styles.scrollContent}
            showsVerticalScrollIndicator={false}
          >
            <NoticeHtmlContent html={notice.contentHtml || notice.content} textStyle={styles.body} />
          </ScrollView>
          <Pressable onPress={onClose} style={styles.btn}>
            <Text style={styles.btnText}>{hasMore ? '下一条' : '知道了'}</Text>
          </Pressable>
        </View>
      </View>
    </Modal>
  );
}

const styles = StyleSheet.create({
  mask: {
    flex: 1,
    backgroundColor: 'rgba(5, 11, 28, 0.72)',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 28,
  },
  card: {
    width: '100%',
    maxWidth: 360,
    maxHeight: '78%',
    borderRadius: 14,
    backgroundColor: colors.cardSolid,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    paddingHorizontal: 20,
    paddingTop: 18,
    paddingBottom: 16,
  },
  kicker: {
    alignSelf: 'center',
    color: colors.accent,
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 1,
  },
  title: {
    marginTop: 8,
    color: colors.text,
    fontSize: 17,
    fontWeight: '700',
    textAlign: 'center',
    lineHeight: 24,
  },
  date: {
    marginTop: 8,
    color: colors.muted,
    fontSize: 12,
    textAlign: 'center',
  },
  scroll: {
    marginTop: 12,
  },
  scrollContent: {
    paddingBottom: 4,
  },
  body: {
    color: 'rgba(220, 232, 255, 0.88)',
    fontSize: 14,
    lineHeight: 22,
  },
  btn: {
    marginTop: 16,
    height: 42,
    borderRadius: 10,
    backgroundColor: colors.accent,
    alignItems: 'center',
    justifyContent: 'center',
  },
  btnText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
});
