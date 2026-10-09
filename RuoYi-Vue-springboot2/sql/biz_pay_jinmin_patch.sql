SET NAMES utf8mb4;
-- Jinmin (xing) deposit. Repeatable.
-- product_id: 8055 alipay, 8057 wechat scan. Amount fen. Callback IP 18.181.19.99.

DROP PROCEDURE IF EXISTS patch_biz_pay_jinmin;
DELIMITER $$
CREATE PROCEDURE patch_biz_pay_jinmin()
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_pay_provider' AND COLUMN_NAME = 'secret_key'
      AND CHARACTER_MAXIMUM_LENGTH < 256
  ) THEN
    ALTER TABLE biz_pay_provider MODIFY COLUMN secret_key varchar(256) default '' comment 'sign key';
  END IF;
END$$
DELIMITER ;
CALL patch_biz_pay_jinmin();
DROP PROCEDURE IF EXISTS patch_biz_pay_jinmin;

insert into biz_pay_provider (provider_code, provider_name, adapter_family, gateway_url, app_id, secret_key, callback_ips, mock_mode, status, sort_order, remark, create_time)
select 'jinmin', 'Jinmin', 'jinmin',
       'http://pay.jinmin.club',
       '10042',
       '8JULVNH0Z4FXEZJ6HBNAKXBU3CERPQSVRWJCRUJ0ST9LEXMIW2ODOG0F3SXUZLEITXTQZUH6UXOD5XKNROJENR7HXW5Y1ETDHE3TZ44NFCEFXQJPRJQUIOQAII78PXUL',
       '18.181.19.99',
       '0', '0', 14,
       'form POST /api/pay/create_order; amount fen; product_id 8055/8057; notify reply success',
       sysdate()
from dual
where not exists (select 1 from biz_pay_provider p where p.provider_code = 'jinmin');

update biz_pay_provider
set provider_name = 'Jinmin',
    adapter_family = 'jinmin',
    gateway_url = 'http://pay.jinmin.club',
    app_id = '10042',
    secret_key = '8JULVNH0Z4FXEZJ6HBNAKXBU3CERPQSVRWJCRUJ0ST9LEXMIW2ODOG0F3SXUZLEITXTQZUH6UXOD5XKNROJENR7HXW5Y1ETDHE3TZ44NFCEFXQJPRJQUIOQAII78PXUL',
    callback_ips = '18.181.19.99',
    mock_mode = '0',
    status = '0',
    remark = 'form POST /api/pay/create_order; amount fen; product_id 8055/8057; notify reply success'
where provider_code = 'jinmin';

insert into biz_pay_channel (provider_code, channel_code, channel_name, display_name, scene, product_id, currency, min_amount, max_amount, weight, status, sort_order, remark, create_time)
select * from (
  select 'jinmin' as provider_code, 'JINMIN_ALIPAY' as channel_code, 'Alipay' as channel_name, 'Alipay' as display_name,
         'alipay' as scene, '8055' as product_id, 'CNY' as currency,
         100 as min_amount, 50000 as max_amount, 175 as weight, '0' as status, 1 as sort_order,
         'product_id = 8055 alipay' as remark, sysdate() as create_time
  union all
  select 'jinmin', 'JINMIN_WECHAT', 'WeChat', 'WeChat', 'wechat', '8057',
         'CNY', 100, 50000, 175, '0', 2, 'product_id = 8057 wechat scan', sysdate()
) t
where not exists (select 1 from biz_pay_channel c where c.channel_code = t.channel_code);

update biz_pay_channel
set product_id = '8055',
    scene = 'alipay',
    display_name = 'Alipay',
    remark = 'product_id = 8055 alipay'
where channel_code = 'JINMIN_ALIPAY';

update biz_pay_channel
set product_id = '8057',
    scene = 'wechat',
    display_name = 'WeChat',
    remark = 'product_id = 8057 wechat scan'
where channel_code = 'JINMIN_WECHAT';
