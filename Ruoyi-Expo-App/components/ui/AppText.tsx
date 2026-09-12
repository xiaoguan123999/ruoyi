import { forwardRef } from 'react';
import {
  Text as RNText,
  TextInput as RNTextInput,
  type TextInputProps,
  type TextProps,
} from 'react-native';

import { MAX_FONT_SIZE_MULTIPLIER } from '@/theme/typography';

type AppTextInputProps = Omit<TextInputProps, 'style'> & {
  style?: TextInputProps['style'] | object;
};

/** 跟随系统字号。布局用 minHeight / 滚动适配，不要锁死 1.0。 */
export const Text = forwardRef<RNText, TextProps>(function Text(
  { allowFontScaling = true, maxFontSizeMultiplier = MAX_FONT_SIZE_MULTIPLIER, ...props },
  ref,
) {
  return (
    <RNText
      ref={ref}
      {...props}
      allowFontScaling={allowFontScaling}
      maxFontSizeMultiplier={maxFontSizeMultiplier}
    />
  );
});
Text.displayName = 'AppText';

export const TextInput = forwardRef<RNTextInput, AppTextInputProps>(function TextInput(
  { allowFontScaling = true, maxFontSizeMultiplier = MAX_FONT_SIZE_MULTIPLIER, ...props },
  ref,
) {
  return (
    <RNTextInput
      ref={ref}
      {...(props as TextInputProps)}
      allowFontScaling={allowFontScaling}
      maxFontSizeMultiplier={maxFontSizeMultiplier}
    />
  );
});
TextInput.displayName = 'AppTextInput';
