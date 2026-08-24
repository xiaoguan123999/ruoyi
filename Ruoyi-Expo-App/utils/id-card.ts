/** 省级行政区划代码 */
const PROVINCE_CODES: Record<string, string> = {
  '11': '北京',
  '12': '天津',
  '13': '河北',
  '14': '山西',
  '15': '内蒙古',
  '21': '辽宁',
  '22': '吉林',
  '23': '黑龙江',
  '31': '上海',
  '32': '江苏',
  '33': '浙江',
  '34': '安徽',
  '35': '福建',
  '36': '江西',
  '37': '山东',
  '41': '河南',
  '42': '湖北',
  '43': '湖南',
  '44': '广东',
  '45': '广西',
  '46': '海南',
  '50': '重庆',
  '51': '四川',
  '52': '贵州',
  '53': '云南',
  '54': '西藏',
  '61': '陕西',
  '62': '甘肃',
  '63': '青海',
  '64': '宁夏',
  '65': '新疆',
  '71': '台湾',
  '81': '香港',
  '82': '澳门',
  '91': '国外',
};

/** 校验码加权因子（前 17 位） */
const CHECK_FACTORS = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2] as const;

/** 校验码对应值（sum % 11） */
const CHECK_PARITY = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'] as const;

const ID_CARD_REG =
  /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/;

/**
 * 校验中国大陆 18 位身份证号：
 * 格式 / 省份 / 出生日期 / 第 18 位校验码
 */
export function validateIdCard(idCard: string): boolean {
  const value = idCard.trim();
  if (!ID_CARD_REG.test(value)) {
    return false;
  }

  const province = value.substring(0, 2);
  if (!PROVINCE_CODES[province]) {
    return false;
  }

  const birthYear = parseInt(value.substring(6, 10), 10);
  const birthMonth = parseInt(value.substring(10, 12), 10) - 1;
  const birthDay = parseInt(value.substring(12, 14), 10);
  const birthDate = new Date(birthYear, birthMonth, birthDay);

  if (
    birthDate.getFullYear() !== birthYear ||
    birthDate.getMonth() !== birthMonth ||
    birthDate.getDate() !== birthDay
  ) {
    return false;
  }

  if (birthDate > new Date()) {
    return false;
  }

  let sum = 0;
  for (let i = 0; i < 17; i += 1) {
    sum += parseInt(value.charAt(i), 10) * CHECK_FACTORS[i];
  }

  const remainder = sum % 11;
  const lastChar = value.charAt(17).toUpperCase();
  return CHECK_PARITY[remainder] === lastChar;
}
