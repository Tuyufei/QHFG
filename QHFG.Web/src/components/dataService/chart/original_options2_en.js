const original_options2_en = [
  {
    label: "Power grid",
    value: "电网",
    leaf: false,
    level: 1,
    disabled: true,
    display: true,
  },
  {
    label: "Central China",
    value: "华中",
    leaf: false,
    level: 1,
    children: [
      {
        label: "Province",
        value: "省级",
        leaf: true,
        level: 1,
        disabled: true,
        display: true,
      },
      {
        label: "Henan",
        value: "河南省",
        leaf: false,
        level: 2,
        children: [
          {
            label: "Prefecture-level",
            value: "地级",
            leaf: false,
            level: 3,
          },
          {
            label: "Xinyang",
            value: "信阳市",
            leaf: false,
            level: 3,
            children: [
              {
                label: "County-level",
                value: "县级",
                leaf: false,
                level: 4,
              },
              {
                label: "Huangchuan",
                value: "潢川县",
                leaf: false,
                level: 4,
              },
            ],
          },
        ],
      },
      { label: "Beijing", value: "北京市", leaf: false, level: 2 },
      { label: "Tianjin", value: "天津市", leaf: false, level: 2 },
      { label: "Hebei", value: "河北省", leaf: false, level: 2 },
      { label: "Shanxi", value: "山西省", leaf: false, level: 2 },
      { label: "Hubei", value: "湖北省", leaf: false, level: 2 },
      { label: "Heilongjiang", value: "黑龙江省", leaf: false, level: 2 },
    ],
  },
];
export default original_options2_en;
