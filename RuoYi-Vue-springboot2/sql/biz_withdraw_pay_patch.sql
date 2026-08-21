-- 提现审核打款：收款方式 + 打款凭证（可重复执行）
DROP PROCEDURE IF EXISTS patch_biz_withdraw_pay;
DELIMITER $$
CREATE PROCEDURE patch_biz_withdraw_pay()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_withdraw' AND COLUMN_NAME = 'pay_method'
  ) THEN
    ALTER TABLE biz_withdraw
      ADD COLUMN pay_method varchar(32) default '' comment '收款方式（ALIPAY/USDT）' AFTER account_info;
  END IF;
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_withdraw' AND COLUMN_NAME = 'pay_proof_url'
  ) THEN
    ALTER TABLE biz_withdraw
      ADD COLUMN pay_proof_url varchar(500) default '' comment '打款凭证图片' AFTER audit_remark;
  END IF;
END$$
DELIMITER ;
CALL patch_biz_withdraw_pay();
DROP PROCEDURE IF EXISTS patch_biz_withdraw_pay;

UPDATE biz_withdraw SET pay_method = 'USDT' WHERE (pay_method is null or pay_method = '') AND currency = 'USDT';
UPDATE biz_withdraw SET pay_method = 'ALIPAY' WHERE (pay_method is null or pay_method = '') AND currency = 'CNY';
