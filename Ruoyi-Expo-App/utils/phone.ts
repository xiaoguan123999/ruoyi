/** 去掉手机号里所有空白（含中间空格），如 138 0000 0000 → 13800000000 */
export function normalizePhone(value: string): string {
  return value.replace(/\s/g, '');
}
