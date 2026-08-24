import { handleUnauthorized } from '@/api/auth-state';
import type { AjaxResult } from '@/api/types';
import { config } from '@/config';
import { getToken } from '@/utils/storage';

export class ApiError extends Error {
  constructor(
    message: string,
    public code: number,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export async function request<T>(
  path: string,
  options: { method?: string; body?: unknown; withToken?: boolean } = {},
): Promise<AjaxResult<T>> {
  if (!config.API_URL) {
    throw new ApiError('服务暂不可用，请稍后再试', -1);
  }

  const { method = 'GET', body, withToken = true } = options;
  const headers = new Headers({ 'Content-Type': 'application/json;charset=utf-8' });
  if (withToken) {
    const token = await getToken();
    if (token) {
      headers.set('Authorization', `Bearer ${token}`);
    }
  }

  let response: Response;
  try {
    response = await fetch(`${config.API_URL}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  } catch {
    throw new ApiError('网络连接失败，请检查网络后重试', -1);
  }

  let json: AjaxResult<T>;
  try {
    json = (await response.json()) as AjaxResult<T>;
  } catch {
    throw new ApiError(response.ok ? '服务异常，请稍后再试' : '服务繁忙，请稍后再试', response.status);
  }

  const code = Number(json.code);
  if (response.status === 401 || code === 401) {
    void handleUnauthorized('登录已过期，请重新登录');
    throw new ApiError('登录已过期，请重新登录', 401);
  }
  if (code !== 200) {
    throw new ApiError(json.msg || '操作失败，请稍后再试', code);
  }
  return json;
}
