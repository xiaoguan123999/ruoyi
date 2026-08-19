export type AjaxResult<T = unknown> = {
  code: number;
  msg: string;
  token?: string;
  img?: string;
  uuid?: string;
  captchaEnabled?: boolean;
  captchaOnOff?: boolean;
  user?: T;
  roles?: string[];
  permissions?: string[];
  rows?: unknown[];
  total?: number;
};

export type RuoyiUser = {
  userId: number;
  userName: string;
  nickName?: string;
  avatar?: string;
};

export type LoginBody = {
  username: string;
  password: string;
  code?: string;
  uuid?: string;
};
