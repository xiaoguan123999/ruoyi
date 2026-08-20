import Svg, { Circle, Ellipse, Path, Rect, Text as SvgText } from 'react-native-svg';

type IconProps = {
  size?: number;
  color?: string;
};

export function ProductDailyIcon({ size = 44, color = '#C5D9F5' }: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 44 44" fill="none">
      <Ellipse cx="18" cy="12" rx="11" ry="5" stroke={color} strokeWidth="1.7" />
      <Path d="M7 12v4.5c0 2.76 4.92 5 11 5s11-2.24 11-5V12" stroke={color} strokeWidth="1.7" />
      <Path d="M7 16.5v4.5c0 2.76 4.92 5 11 5s11-2.24 11-5v-4.5" stroke={color} strokeWidth="1.7" />
      <Path d="M7 21v4.5c0 2.76 4.92 5 11 5s11-2.24 11-5V21" stroke={color} strokeWidth="1.7" />
      <Circle cx="31" cy="30" r="9" fill="#0A1528" stroke={color} strokeWidth="1.7" />
      <Path
        d="M31 25.5V30l3.2 2"
        stroke={color}
        strokeWidth="1.7"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
    </Svg>
  );
}

export function ProductCycleIcon({ size = 44, color = '#C5D9F5' }: IconProps) {
  return (
    <Svg width={size} height={size} viewBox="0 0 44 44" fill="none">
      <Rect x="8" y="10" width="28" height="26" rx="4" stroke={color} strokeWidth="1.7" />
      <Path d="M8 17.5h28" stroke={color} strokeWidth="1.7" />
      <Path
        d="M15 7v7M29 7v7"
        stroke={color}
        strokeWidth="1.7"
        strokeLinecap="round"
      />
      <SvgText
        x="22"
        y="33"
        fill={color}
        fontSize="13"
        fontWeight="700"
        textAnchor="middle"
      >
        30
      </SvgText>
    </Svg>
  );
}
