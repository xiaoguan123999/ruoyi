import { Modal, Pressable, StyleSheet, Text, View } from 'react-native';

import { colors } from '@/theme/colors';

export type UpdateConfirmOptions = {
  title: string;
  description: string;
  confirmText?: string;
  cancelText?: string | null;
};

export function UpdateConfirmModal({
  options,
  onConfirm,
  onCancel,
}: {
  options: UpdateConfirmOptions | null;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  if (!options) {
    return null;
  }

  const confirmText = options.confirmText ?? '立即更新';
  const cancelText = options.cancelText === undefined ? '稍后' : options.cancelText;

  return (
    <Modal
      visible
      transparent
      animationType="fade"
      onRequestClose={cancelText ? onCancel : undefined}
    >
      <View style={styles.mask}>
        <View style={styles.card}>
          <Text style={styles.title}>{options.title}</Text>
          <Text style={styles.body}>{options.description}</Text>
          <View style={styles.actions}>
            {cancelText ? (
              <Pressable onPress={onCancel} style={[styles.btn, styles.btnGhost]}>
                <Text style={styles.btnGhostText}>{cancelText}</Text>
              </Pressable>
            ) : null}
            <Pressable onPress={onConfirm} style={[styles.btn, styles.btnPrimary]}>
              <Text style={styles.btnPrimaryText}>{confirmText}</Text>
            </Pressable>
          </View>
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
    borderRadius: 14,
    backgroundColor: colors.cardSolid,
    borderWidth: 1,
    borderColor: colors.cardBorder,
    paddingHorizontal: 20,
    paddingTop: 18,
    paddingBottom: 16,
  },
  title: {
    color: colors.text,
    fontSize: 17,
    fontWeight: '700',
    textAlign: 'center',
  },
  body: {
    marginTop: 12,
    color: 'rgba(220, 232, 255, 0.88)',
    fontSize: 14,
    lineHeight: 22,
    textAlign: 'center',
  },
  actions: {
    marginTop: 20,
    flexDirection: 'row',
    gap: 10,
  },
  btn: {
    flex: 1,
    height: 42,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
  },
  btnGhost: {
    borderWidth: 1,
    borderColor: 'rgba(180, 205, 235, 0.35)',
  },
  btnGhostText: {
    color: 'rgba(230, 238, 250, 0.92)',
    fontSize: 15,
    fontWeight: '600',
  },
  btnPrimary: {
    backgroundColor: colors.accent,
  },
  btnPrimaryText: {
    color: '#FFFFFF',
    fontSize: 15,
    fontWeight: '700',
  },
});
