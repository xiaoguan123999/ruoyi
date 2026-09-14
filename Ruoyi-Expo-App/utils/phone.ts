/** 去掉手机号里所有空白（含中间空格），如 138 0000 0000 → 13800000000 */
export function normalizePhone(value: string): string {
  return value.replace(/\s/g, '');
}

const CN_MOBILE_RE = /^1\d{10}$/;

export function isValidPhone(value: string): boolean {
  return CN_MOBILE_RE.test(normalizePhone(value));
}

export function phoneError(value: string): string | null {
  const phone = normalizePhone(value);
  if (!phone) {
    return '请输入手机号码';
  }
  return isValidPhone(phone) ? null : '请输入有效的手机号码';
}
