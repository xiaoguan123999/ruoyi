-- 已有会员邀请码改为 7 位随机数字（1000000-9999999），不与现有邀请码重复
DROP PROCEDURE IF EXISTS patch_biz_invite_code;
DELIMITER $$
CREATE PROCEDURE patch_biz_invite_code()
BEGIN
  DECLARE done INT DEFAULT 0;
  DECLARE mid BIGINT;
  DECLARE new_code VARCHAR(7);
  DECLARE cur CURSOR FOR
    SELECT member_id FROM biz_member
    WHERE invite_code IS NULL OR CHAR_LENGTH(invite_code) <> 7 OR invite_code NOT REGEXP '^[0-9]{7}$';
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;
  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO mid;
    IF done = 1 THEN
      LEAVE read_loop;
    END IF;
    retry: LOOP
      SET new_code = CAST(FLOOR(1000000 + RAND() * 9000000) AS CHAR);
      IF NOT EXISTS (SELECT 1 FROM biz_member WHERE invite_code = new_code) THEN
        UPDATE biz_member SET invite_code = new_code WHERE member_id = mid;
        LEAVE retry;
      END IF;
    END LOOP;
  END LOOP;
  CLOSE cur;
END$$
DELIMITER ;
CALL patch_biz_invite_code();
DROP PROCEDURE IF EXISTS patch_biz_invite_code;
