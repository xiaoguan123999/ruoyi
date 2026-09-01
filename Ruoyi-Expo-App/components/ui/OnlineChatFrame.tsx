import { createElement } from 'react';
import { Platform, StyleSheet, View } from 'react-native';
import { WebView } from 'react-native-webview';

type Props = {
  url: string;
};

export function OnlineChatFrame({ url }: Props) {
  if (Platform.OS === 'web') {
    return (
      <View style={styles.fill}>
        {createElement('iframe', {
          src: url,
          style: {
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            width: '100%',
            height: '100%',
            border: 'none',
          },
          allow: 'microphone; camera; clipboard-write; clipboard-read; autoplay',
          referrerPolicy: 'no-referrer-when-downgrade',
        })}
      </View>
    );
  }

  return (
    <WebView
      source={{ uri: url }}
      style={styles.fill}
      javaScriptEnabled
      domStorageEnabled
      startInLoadingState
      allowsInlineMediaPlayback
      setSupportMultipleWindows={false}
    />
  );
}

const styles = StyleSheet.create({
  fill: {
    flex: 1,
    position: 'relative',
  },
});
