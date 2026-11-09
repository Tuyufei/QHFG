// 中文版本
export const countryOptionsZh = [
  { label: "东帝汶 (+670)", value: "+670" },
  { label: "中国 (+86)", value: "+86" },
  { label: "丹麦 (+45)", value: "+45" },
  { label: "乌克兰 (+380)", value: "+380" },
  { label: "乌拉圭 (+598)", value: "+598" },
  // ... 其他中文国家选项
];

// 英文版本
export const countryOptionsEn = [
  { label: "East Timor (+670)", value: "+670" },
  { label: "China (+86)", value: "+86" },
  { label: "Denmark (+45)", value: "+45" },
  { label: "Ukraine (+380)", value: "+380" },
  { label: "Uruguay (+598)", value: "+598" },
  // ... 其他英文国家选项
];

// 获取当前语言的国家选项
export const getCountryOptions = (locale) => {
  return locale === 'zh' ? countryOptionsZh : countryOptionsEn;
}; 