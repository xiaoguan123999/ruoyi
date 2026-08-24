-- App 支付/交易密码（可重复执行）
DROP PROCEDURE IF EXISTS patch_biz_pay_password;
DELIMITER $$
CREATE PROCEDURE patch_biz_pay_password()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_member' AND COLUMN_NAME = 'pay_password'
  ) THEN
    ALTER TABLE biz_member
      ADD COLUMN pay_password varchar(100) default '' comment '支付/交易密码' AFTER password;
  END IF;
END$$
DELIMITER ;
CALL patch_biz_pay_password();
DROP PROCEDURE IF EXISTS patch_biz_pay_password;
