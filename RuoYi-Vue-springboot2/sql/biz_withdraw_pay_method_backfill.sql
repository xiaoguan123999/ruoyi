SET NAMES utf8mb4;
-- 历史提现：用户提交银行卡但 pay_method 被记成支付宝时回填。可重复执行。

UPDATE biz_withdraw
SET pay_method = 'BANK'
WHERE IFNULL(pay_method, '') IN ('', 'ALIPAY')
  AND IFNULL(currency, '') <> 'USDT'
  AND (
    IFNULL(account_info, '') REGEXP '银行|储蓄|信用社|银行卡|卡号|尾号|开户|借记|支行|工行|农行|建行|中行|交行|招行|邮储'
    OR IFNULL(remark, '') REGEXP '银行|银行卡|尾号'
    OR IFNULL(account_info, '') REGEXP '[0-9]{16,19}'
  );
