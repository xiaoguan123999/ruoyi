const { getDefaultConfig } = require('expo/metro-config');
const { withTamagui } = require('@tamagui/metro-plugin');

const config = getDefaultConfig(__dirname);
if (!config.resolver.assetExts.includes('bin')) {
  config.resolver.assetExts.push('bin');
}

module.exports = withTamagui(config, {
  components: ['tamagui'],
  config: './tamagui.config.ts',
});
