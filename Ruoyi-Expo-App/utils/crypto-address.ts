const TRC20_RE = /^T[1-9A-HJ-NP-Za-km-z]{33}$/;
const BEP20_RE = /^0x[a-fA-F0-9]{40}$/;

export function normalizeCryptoAddress(value: string): string {
  return value.replace(/\s/g, '');
}

export function usdtAddressError(network: string, address: string): string | null {
  const no = normalizeCryptoAddress(address);
  if (!no) {
    return '请输入虚拟币地址';
  }
  const chain = network.trim().toUpperCase();
  if (chain === 'TRC20') {
    return TRC20_RE.test(no) ? null : '请输入有效的 TRC20 地址';
  }
  if (chain === 'BEP20') {
    return BEP20_RE.test(no) ? null : '请输入有效的 BEP20 地址';
  }
  return '请选择虚拟币协议';
}
