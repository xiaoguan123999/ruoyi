SET NAMES utf8mb4;
-- FuWang / WuYou / FeiFan real deposit channels.

DROP PROCEDURE IF EXISTS patch_biz_pay_real_channels;
DELIMITER $$
CREATE PROCEDURE patch_biz_pay_real_channels()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_pay_provider' AND COLUMN_NAME = 'callback_ips'
  ) THEN
    ALTER TABLE biz_pay_provider ADD COLUMN callback_ips varchar(255) default '' comment 'callback source IPs' AFTER secret_key;
  END IF;
  IF EXISTS (
    SELECT 1 FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'biz_pay_provider' AND COLUMN_NAME = 'secret_key'
      AND CHARACTER_MAXIMUM_LENGTH < 256
  ) THEN
    ALTER TABLE biz_pay_provider MODIFY COLUMN secret_key varchar(256) default '' comment 'sign key';
  END IF;
END$$
DELIMITER ;
CALL patch_biz_pay_real_channels();
DROP PROCEDURE IF EXISTS patch_biz_pay_real_channels;

insert into biz_pay_provider (provider_code, provider_name, adapter_family, gateway_url, app_id, secret_key, callback_ips, mock_mode, status, sort_order, remark, create_time)
select * from (
  select 'fuwang' as provider_code, 'FuWang' as provider_name, 'jeepay' as adapter_family,
         'https://fuwang-pay.aaagood.xyz' as gateway_url,
         'M2100946162876579840' as app_id,
         '63d41a6ab1204846aa15cab2d423cf7b' as secret_key,
         '' as callback_ips, '0' as mock_mode, '0' as status, 10 as sort_order,
         'jeepay wayCode in channel.product_id; app_id may be mchNo|appId' as remark, sysdate() as create_time
  union all
  select 'wuyou', 'WuYou', 'wuyou', 'http://pay.wyoukj.click', '10109',
         'RGYNFXPAI7WX44GFFOALG4EY4U1WVQKHHSLOMWF17VHDOSZ7EYTIVEXSVUCVSBVCVRX4SOZ3HGSA9B4RTCSKNDMWVTXHIZPUYHINUZAECF8XAQPORGTYNIWB0PUS7KPM',
         '18.163.116.198', '0', '0', 11, 'product 8000 alipay 8001 wechat, amount fen', sysdate()
  union all
  select 'feifan', 'FeiFan', 'monpay', 'https://nicepaymon-api.pangukaitianpidi.xyz',
         '494b481b65cb95f6c1233133', '0E7020b1143951764DA751CFbb7706F39e302Bed',
         '43.199.5.177', '0', '0', 12, 'monpay product 13 alipay 15 wechat', sysdate()
) t
where not exists (select 1 from biz_pay_provider p where p.provider_code = t.provider_code);

insert into biz_pay_channel (provider_code, channel_code, channel_name, display_name, scene, product_id, currency, min_amount, max_amount, weight, status, sort_order, remark, create_time)
select * from (
  select 'fuwang' as provider_code, 'FUWANG_ALIPAY' as channel_code, 'Alipay' as channel_name, 'Alipay' as display_name,
         'alipay' as scene, 'ALI_QR' as product_id, 'CNY' as currency, 10 as min_amount, 50000 as max_amount,
         200 as weight, '0' as status, 1 as sort_order, 'fuwang wayCode' as remark, sysdate() as create_time
  union all
  select 'fuwang', 'FUWANG_WECHAT', 'WeChat', 'WeChat', 'wechat', 'WX_NATIVE', 'CNY', 10, 50000, 200, '0', 2, 'fuwang wayCode', sysdate()
  union all
  select 'wuyou', 'WUYOU_ALIPAY', 'Alipay', 'Alipay', 'alipay', '8000', 'CNY', 100, 20000, 190, '0', 1, 'wuyou 8000', sysdate()
  union all
  select 'wuyou', 'WUYOU_WECHAT', 'WeChat', 'WeChat', 'wechat', '8001', 'CNY', 100, 3000, 190, '0', 2, 'wuyou 8001', sysdate()
  union all
  select 'feifan', 'FEIFAN_ALIPAY', 'Alipay', 'Alipay', 'alipay', '13', 'CNY', 100, 50000, 180, '0', 1, 'feifan 13', sysdate()
  union all
  select 'feifan', 'FEIFAN_WECHAT', 'WeChat', 'WeChat', 'wechat', '15', 'CNY', 100, 50000, 180, '0', 2, 'feifan 15', sysdate()
) t
where not exists (select 1 from biz_pay_channel c where c.channel_code = t.channel_code);
