import { request } from '@/api/request';
import type { AppPayAccount, AppPayAccountBody, AppPayAccountType } from '@/api/types';

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function toNumber(value: unknown, fallback = 0): number {
  const next = Number(value);
  return Number.isFinite(next) ? next : fallback;
}

function pickString(source: Record<string, unknown>, keys: string[], fallback = ''): string {
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null && String(source[key]).length > 0) {
      return String(source[key]);
    }
  }
  return fallback;
}

function pickNumber(source: Record<string, unknown>, keys: string[]): number {
  for (const key of keys) {
    if (source[key] !== undefined && source[key] !== null) {
      return toNumber(source[key]);
    }
  }
  return 0;
}

function normalizeType(raw: string): AppPayAccountType | null {
  const upper = raw.trim().toUpperCase();
  if (upper === 'USDT' || upper === 'BANK' || upper === 'ALIPAY') {
    return upper;
  }
  return null;
}

function extractList(res: Record<string, unknown>): unknown[] {
  if (Array.isArray(res.data)) {
    return res.data;
  }
  if (Array.isArray(res.rows)) {
    return res.rows;
  }
  if (isRecord(res.data)) {
    const nested = res.data.list ?? res.data.rows ?? res.data.records;
    if (Array.isArray(nested)) {
      return nested;
    }
  }
  return [];
}

export function mapPayAccount(raw: unknown): AppPayAccount | null {
  if (!isRecord(raw)) {
    return null;
  }
  const accountId = pickNumber(raw, ['accountId', 'id']);
  if (!accountId) {
    return null;
  }
  const accountType = normalizeType(pickString(raw, ['accountType', 'type']));
  if (!accountType) {
    return null;
  }
  return {
    accountId,
    accountType,
    accountName: pickString(raw, ['accountName', 'realName', 'name']) || undefined,
    accountNo: pickString(raw, ['accountNo', 'address', 'cardNo', 'alipayAccount']),
    bankName: pickString(raw, ['bankName']) || undefined,
    network: pickString(raw, ['network', 'protocol', 'chain']) || undefined,
    isDefault: pickString(raw, ['isDefault']) || undefined,
    status: pickString(raw, ['status']) || undefined,
    phone: pickString(raw, ['phone']) || undefined,
    remark: pickString(raw, ['remark']) || undefined,
  };
}

export function formatPayAccountLabel(account: AppPayAccount): string {
  if (account.accountType === 'USDT') {
    const network = account.network || 'TRC20';
    const no = account.accountNo || '';
    const short =
      no.length > 12 ? `${no.slice(0, 6)}…${no.slice(-4)}` : no || '--';
    return `USDT(${network}) ${short}`;
  }
  if (account.accountType === 'BANK') {
    const bank = account.bankName || '银行卡';
    const no = account.accountNo || '';
    const short = no.length > 4 ? `尾号${no.slice(-4)}` : no || '--';
    return `${bank} ${short}`;
  }
  const name = account.accountName ? `${account.accountName} ` : '';
  return `支付宝 ${name}${account.accountNo || '--'}`.trim();
}

export function payAccountCurrency(account: AppPayAccount): 'CNY' | 'USDT' {
  return account.accountType === 'USDT' ? 'USDT' : 'CNY';
}

/** GET /app/payAccounts?type=USDT|BANK|ALIPAY */
export async function fetchAppPayAccounts(type?: AppPayAccountType): Promise<AppPayAccount[]> {
  const query = type ? `?type=${encodeURIComponent(type)}` : '';
  const res = await request<unknown>(`/app/payAccounts${query}`);
  return extractList(res as Record<string, unknown>)
    .map(mapPayAccount)
    .filter((item): item is AppPayAccount => item !== null)
    .sort((a, b) => {
      const da = a.isDefault === '1' ? 0 : 1;
      const db = b.isDefault === '1' ? 0 : 1;
      if (da !== db) {
        return da - db;
      }
      return a.accountId - b.accountId;
    });
}

/** POST /app/payAccounts — 新增不传 accountId */
export async function createAppPayAccount(body: AppPayAccountBody): Promise<string> {
  const res = await request('/app/payAccounts', {
    method: 'POST',
    body: {
      accountType: body.accountType,
      accountName: body.accountName?.trim() || undefined,
      accountNo: body.accountNo.trim(),
      bankName: body.bankName?.trim() || undefined,
      network: body.network?.trim() || undefined,
      isDefault: body.isDefault,
      remark: body.remark?.trim() || undefined,
    },
  });
  return res.msg || '添加成功';
}

/** PUT /app/payAccounts/{id} */
export async function updateAppPayAccount(
  accountId: number,
  body: AppPayAccountBody,
): Promise<string> {
  const res = await request(`/app/payAccounts/${accountId}`, {
    method: 'PUT',
    body: {
      accountId,
      accountType: body.accountType,
      accountName: body.accountName?.trim() || undefined,
      accountNo: body.accountNo.trim(),
      bankName: body.bankName?.trim() || undefined,
      network: body.network?.trim() || undefined,
      isDefault: body.isDefault,
      remark: body.remark?.trim() || undefined,
    },
  });
  return res.msg || '修改成功';
}

/** DELETE /app/payAccounts/{id} */
export async function deleteAppPayAccount(accountId: number): Promise<string> {
  const res = await request(`/app/payAccounts/${accountId}`, { method: 'DELETE' });
  return res.msg || '删除成功';
}
