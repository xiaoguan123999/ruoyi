-- App 会员不需要谷歌验证：提现、登录均不校验。后台账号谷歌验证不受影响。
UPDATE sys_config
   SET config_value = 'false',
       remark = 'App提现和登录不校验谷歌验证'
 WHERE config_key = 'biz.google.requireWithdraw';
