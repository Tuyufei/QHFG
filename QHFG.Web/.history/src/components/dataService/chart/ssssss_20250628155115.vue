<template>
    <div class="chart_box gray-text">
      <div class="param_box">
        <div class="region-cascader">
          <el-cascader
            class="icon-cascader"
            ref="cascaderRef"
            v-model="selectedAreaValue"
            :options="options"
            :props="{
              expandTrigger: 'click',
              multiple: true,
              checkStrictly: true,
            }"
            collapse-tags
            collapse-tags-tooltip
            :max-collapse-tags="1"
            clearable
            @change="handlerChange"
            v-if="!is_show"
            :placeholder="$t('chart.selectRegion')"
            style="width: 80px"
          >
            <template #default="{ node, data }">
              <div class="custom-node">
                <!-- 根据数据的value值显示不同的标题（区域、次区域、国家、省级、县级） -->
                <div v-if="data.value === '区域'" class="icon-title">
                 <span style="color: #333">{{ data.label }}</span>
                </div>
                <div v-if="data.value === '次区域'" class="icon-title">
                 <span style="color: #333">{{ data.label }}</span>
                </div>
                <div v-if="data.value === '国家'" class="icon-title">
                 <span style="color: #333">{{ data.label }}</span>
                </div>
                <div v-if="data.value === '电网'" class="icon-title">
                 <span style="color: #333">{{ data.label }}</span>
                </div>
                <div v-if="data.value === '省级'" class="icon-title">
                 <span style="color: #333">{{ data.label }}</span>
                </div>
               <div v-if="data.value === '县级'" class="icon-title">
                 <span style="color: #333">{{ data.label }}</span>
                </div>
                <div v-if="data.value === '地级'" class="icon-title">
                 <span style="color: #333">{{ data.label }}</span>
                </div>
              </div>
              <div
                class="node-wrapper"
                @click.stop.prevent="handleNodeClick(node, data)"
              >
                <span class="node-text">{{ data.label }}</span>
                <span class="blank-area"></span>
                <!-- <div class="arrow-container" 
                  @click="handleArrowClick(node, data)">
                  <span class="arrow"></span>
                </div> -->
              </div>
            </template>
          </el-cascader>
        </div>
        <el-select
          v-model="select_time_value"
          :placeholder="$t('chart.elSelectPlaceholder')"
          @change="handleTimeChange"
          :style="{ width: timeSelectWidth }"
        >
          <el-option
            v-for="item in timeSelection"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
        <div class="temporal_resolution">
  
          <div class="yearPicker" v-show="showTimePicker === 1">
            <el-date-picker
              type="years"
              v-model="selectedYear"
              :placeholder="$t('chart.year')"
              format="YYYY"
              value-format="YYYY"
              :disabled-date="disabledDate"
              @change="handleYearChange"
              style="width: 160px"
            >
            </el-date-picker>
          </div>
          <div class="monthPicker" v-show="showTimePicker === 2">
            <el-date-picker
              type="year"
              v-model="yearValue"
              :placeholder="$t('chart.year')"
              format="YYYY"
              value-format="YYYY"
              :disabled-date="disabledDate"
              @change="handleYear_MonthChange"
              style="width: 80px"
            ></el-date-picker>
            <el-date-picker
              type="months"
              v-model="selectedMonth"
              :placeholder="$t('chart.month')"
              format="YYYY-MM"
              value-format="YYYYMM"
              :disabled-date="disabledMonthDate"
              @change="handleMonthChange"
              style="width: 100px"
            >
            </el-date-picker>
          </div>
          <div class="hourPicker" v-show="showTimePicker === 3">
            <el-date-picker
              type="year"
              v-model="yearValue"
              :placeholder="$t('chart.year')"
              format="YYYY"
              value-format="YYYY"
              :disabled-date="disabledDate"
              @change="handleYear_HourChange"
              style="width: 80px"
            >
            </el-date-picker>
            <el-date-picker
              type="datetimerange"
              v-model="selectedHour"
              :start-placeholder="$t('chart.startHourPlaceholder')"
              :end-placeholder="$t('chart.endHourPlaceholder')"
              format="YYYY-MM-DD HH"
              value-format="YYYY-MM-DD HH:00:00"
              :disabled-date="disabledHourDate"
              @change="handleHourChange"
              style="width: 200px"
            >
            </el-date-picker>
          </div>
        </div>
        <el-button type="primary" @click="searchData">{{
          $t("message.confirm")
        }}</el-button>
      </div>
      <div class="echart_box">
  
        <div 
          
          class="chart" 
          ref="chartRef"
        ></div>
        <!-- 无数据提示 -->
        <!-- <el-empty
          v-if="resData.length > 0"
          v-else
          :description="$t('message.noData')"
          class="noChartData"
        /> -->
      </div>
      
      <TableBox :resData1="resData1"></TableBox>
      <div class="tool-box-container">
        <ToolBox :resData1="resData1" :is_download_Disabled="is_download_Disabled"></ToolBox>
      </div>
    </div>
  </template>
  
  <script setup>
  import { onMounted, reactive, ref, watch, computed } from "vue";
  import * as echarts from "echarts";
  import { getLinedata, getRegions, getDescription } from "@/request";
  import TableBox from "./table/TableBox.vue";
  import ToolBox from "./table/ToolBox.vue";
  import bus from "@/utils/bus";
  import { useI18n } from "vue-i18n";
  
  const { t, locale } = useI18n();
  // 监听语言变化并更新列表
  watch(locale, () => {
    console.log("语言已切换!!!!!!!!!!!!!!!!");
    update_options();
    // getChartData();
    // updateChart();
  });
  const resData1 = ref(null)
  onMounted(() => {
    bus.on("clearSelected", handleClearSelected);
  });
  const chartRef = ref(null);
  
  // 图相关数据
  const chartData = ref({});
  const chartType = ref("line"); // 默认折线图
  // 时间分辨率的使用
  const select_time_value = ref("");    // 右侧选择的时间分辨率
  const map_time_value = ref("");         //地图传的时间分辨率
  const time_value = ref("");         //地图传的时间分辨率
  const choose_time_value = ref(false)    //决定图的横坐标，为true则使用map_time_value；为false则使用map_time_value
  
  const showTimePicker = ref(0);        
  const selectedYear = ref([]);
  const selectedMonth = ref([]);
  const selectedHour = ref([]);
  const timeResult = ref([]);
  const timeScale = ref("年");
  const timeScale_line = ref("");
  const timeSpan = ref([]);
  const timeSpan_year = ref([]);
  const timeSpan_month = ref([]);
  const timeSpan_hour = ref([]);
  let timeSelection1 = ref("年");
  let timeSelection2 = ref("月");
  let timeSelection3 = ref("小时");
  // 选择小时、月后的年份选择
  const Year_Hour = ref("");
  const Year_Month = ref("");
  // 右侧区域选择的使用
  const selectedMap = ref("");
  const selectedLevel = ref("区域");
  const selectedPlaces = ref([]);
  let tmpTimeUnit = ref("")
  
  let tableParams_year = ref("");
  let tableParams_timescale = ref("");
  
  let options = [
    {
      label: "区域",
      value: "区域",
      leaf: true,
      level: 1,
      disabled: true,
      display: true,
    },
    {
      label: "东亚与太平洋",
      value: "东亚与太平洋地区",
      leaf: false,
      level: 1,
      children: [
        {
          label: "次区域",
          value: "次区域",
          leaf: true,
          level: 2,
          disabled: true,
          display: true,
        },
        {
          label: "东亚",
          value: "东亚",
          leaf: false,
          level: 2,
          children: [
            {
              label: "国家",
              value: "国家",
              leaf: true,
              level: 3,
              disabled: true,
              display: true,
            },
            {
              label: "中国",
              value: "中国",
              leaf: false,
              level: 3,
              children: [
                {
                  label: "电网",
                  value: "电网",
                  leaf: false,
                  level: 4,
                  disabled: true,
                  display: true,
                },
                {
                  label: "华中",
                  value: "华中",
                  leaf: false,
                  level: 4,
                  children: [
                    {
                      label: "省级",
                      value: "省级",
                      leaf: true,
                      level: 5,
                      disabled: true,
                      display: true,
                    },
                    { 
                      label: "河南省", value: "河南省", leaf: false, level: 5,
                      children:[
                        {
                          label:"地级",
                          value: "地级",
                          leaf: false,
                          level: 6,
                        },
                        {
                          label:"信阳市",
                          value: "信阳市",
                          leaf: false,
                          level: 6,
                          children:[
                            {
                              label:"县级",
                              value: "县级",
                              leaf: false,
                              level: 7,
                            },
                            {
                              label:"潢川县",
                              value: "潢川县",
                              leaf: false,
                              level: 7,
                            },
                          ],
                        },
                      ],
                    },
                    { label: "北京市", value: "北京市", leaf: false, level: 5 },
                    { label: "天津市", value: "天津市", leaf: false, level: 5 },
                    { label: "河北省", value: "河北省", leaf: false, level: 5 },
                    { label: "山西省", value: "山西省", leaf: false, level: 5 },
                    { label: "湖北省", value: "湖北省", leaf: false, level: 5 },
                    
                    { label: "黑龙江省", value: "黑龙江省", leaf: false, level: 5 },
        
                  ],
                },
              ],
            },
            { label: "日本", value: "日本", leaf: false, level: 3 },
            { label: "韩国", value: "韩国", leaf: false, level: 3 },
            { label: "朝鲜", value: "朝鲜", leaf: false, level: 3 },
          ],
        },
        { label: "东南亚", value: "东南亚", leaf: false, level: 2 },
        {
          label: "太平洋岛屿地区",
          value: "太平洋岛屿地区",
          leaf: false,
          level: 2,
        },
      ],
    },
    {
      label: "中东与北非",
      value: "中东与北非地区",
      leaf: false,
      level: 1,
      children: [
        {
          label: "次区域",
          value: "次区域",
          leaf: true,
          level: 2,
          disabled: true,
          display: true,
        },
        { label: "北非", value: "北非", leaf: false, level: 2 },
        { label: "西亚", value: "西亚", leaf: false, level: 2 },
      ],
    },
    { label: "北美", value: "北美地区", leaf: false, level: 1 },
    { label: "南亚", value: "南亚地区", leaf: false, level: 1 },
    {
      label: "拉丁美洲与加勒比",
      value: "拉丁美洲与加勒比地区",
      leaf: false,
      level: 1,
    },
    {
      label: "撒哈拉以南非洲",
      value: "撒哈拉以南非洲地区",
      leaf: false,
      level: 1,
    },
    { label: "欧洲与中亚", value: "欧洲与中亚地区", leaf: false, level: 1 },
  ];
  const original_options = [
    {
      label: "区域",
      value: "区域",
      leaf: true,
      level: 1,
      disabled: true,
      display: true,
    },
    {
      label: "东亚与太平洋",
      value: "东亚与太平洋地区",
      leaf: false,
      level: 1,
      children: [
        {
          label: "次区域",
          value: "次区域",
          leaf: true,
          level: 2,
          disabled: true,
          display: true,
        },
        {
          label: "东亚",
          value: "东亚",
          leaf: false,
          level: 2,
          children: [
            {
              label: "国家",
              value: "国家",
              leaf: true,
              level: 3,
              disabled: true,
              display: true,
            },
            {
              label: "中国",
              value: "中国",
              leaf: false,
              level: 3,
              children: [
                {
                  label: "电网",
                  value: "电网",
                  leaf: false,
                  level: 4,
                  disabled: true,
                  display: true,
                },
                {
                  label: "华中",
                  value: "华中",
                  leaf: false,
                  level: 4,
                  children: [
                    {
                      label: "省级",
                      value: "省级",
                      leaf: true,
                      level: 5,
                      disabled: true,
                      display: true,
                    },
                    { 
                      label: "河南省", value: "河南省", leaf: false, level: 5,
                      children:[
                        {
                          label:"地级",
                          value: "地级",
                          leaf: false,
                          level: 6,
                        },
                        {
                          label:"信阳市",
                          value: "信阳市",
                          leaf: false,
                          level: 6,
                          children:[
                            {
                              label:"县级",
                              value: "县级",
                              leaf: false,
                              level: 7,
                            },
                            {
                              label:"潢川县",
                              value: "潢川县",
                              leaf: false,
                              level: 7,
                            },
                          ],
                        },
                      ],
                    },
                    { label: "北京市", value: "北京市", leaf: false, level: 5 },
                    { label: "天津市", value: "天津市", leaf: false, level: 5 },
                    { label: "河北省", value: "河北省", leaf: false, level: 5 },
                    { label: "山西省", value: "山西省", leaf: false, level: 5 },
                    { label: "湖北省", value: "湖北省", leaf: false, level: 5 },
                    
                    { label: "黑龙江省", value: "黑龙江省", leaf: false, level: 5 },
        
                  ],
                },
              ],
            },
            { label: "日本", value: "日本", leaf: false, level: 3 },
            { label: "韩国", value: "韩国", leaf: false, level: 3 },
            { label: "朝鲜", value: "朝鲜", leaf: false, level: 3 },
          ],
        },
        { label: "东南亚", value: "东南亚", leaf: false, level: 2 },
        {
          label: "太平洋岛屿地区",
          value: "太平洋岛屿地区",
          leaf: false,
          level: 2,
        },
      ],
    },
    {
      label: "中东与北非",
      value: "中东与北非地区",
      leaf: false,
      level: 1,
      children: [
        {
          label: "次区域",
          value: "次区域",
          leaf: true,
          level: 2,
          disabled: true,
          display: true,
        },
        { label: "北非", value: "北非", leaf: false, level: 2 },
        { label: "西亚", value: "西亚", leaf: false, level: 2 },
      ],
    },
    { label: "北美", value: "北美地区", leaf: false, level: 1 },
    { label: "南亚", value: "南亚地区", leaf: false, level: 1 },
    {
      label: "拉丁美洲与加勒比",
      value: "拉丁美洲与加勒比地区",
      leaf: false,
      level: 1,
    },
    {
      label: "撒哈拉以南非洲",
      value: "撒哈拉以南非洲地区",
      leaf: false,
      level: 1,
    },
    { label: "欧洲与中亚", value: "欧洲与中亚地区", leaf: false, level: 1 },
  ];
  
  const original_options_en = [
    {
      label: "Region",
      value: "区域",
      leaf: true,
      level: 1,
      disabled: true,
      display: true,
    },
    {
      label: "East Asia & Pacific",
      value: "东亚与太平洋地区",
      leaf: false,
      level: 1,
      children: [
        {
          label: "Subregions",
          value: "次区域",
          leaf: true,
          level: 1,
          disabled: true,
          display: true,
        },
        {
          label: "East Asia",
          value: "东亚",
          leaf: false,
          level: 2,
          children: [
            {
              label: "Country",
              value: "国家",
              leaf: true,
              level: 1,
              disabled: true,
              display: true,
            },
            {
              label: "China",
              value: "中国",
              leaf: false,
              level: 3,
              children: [
                {
                  label: "Power grid",
                  value: "电网",
                  leaf: false,
                  level: 4,
                  disabled: true,
                  display: true,
                },
                {
                  label: "Central China",
                  value: "华中",
                  leaf: false,
                  level: 4,
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
                      label: "Henan", value: "河南省", leaf: false, level: 5,
                      children:[
                        {
                          label:"Prefecture-level",
                          value: "地级",
                          leaf: false,
                          level: 6,
                        },
                        {
                          label:"Xinyang",
                          value: "信阳市",
                          leaf: false,
                          level: 6,
                          children:[
                            {
                              label:"County-level",
                              value: "县级",
                              leaf: false,
                              level: 7,
                            },
                            {
                              label:"Huangchuan",
                              value: "潢川县",
                              leaf: false,
                              level: 7,
                            },
                          ],
                        },
                      ],
                    },
                    { label: "Beijing", value: "北京市", leaf: false, level: 5 },
                    { label: "Tianjin", value: "天津市", leaf: false, level: 5 },
                    { label: "Hebei", value: "河北省", leaf: false, level: 5 },
                    { label: "Shanxi", value: "山西省", leaf: false, level: 5 },
                    { label: "Hubei", value: "湖北省", leaf: false, level: 5 },
                    
                    { label: "Heilongjiang", value: "黑龙江省", leaf: false, level: 5 },
                  ],
                },
              ],
            },
            { label: "Japan", value: "日本", leaf: false, level: 3 },
            { label: "South Korea", value: "韩国", leaf: false, level: 3 },
            { label: "North Korea", value: "朝鲜", leaf: false, level: 3 },
          ],
        },
        {
          label: "Southeast Asia",
          value: "东南亚",
          leaf: false,
          level: 2,
        },
        {
          label: "Pacific Islands",
          value: "太平洋岛屿地区",
          leaf: false,
          level: 2,
        },
      ],
    },
    {
      label: "Middle East & North Africa",
      value: "中东与北非地区",
      leaf: false,
      level: 1,
      children: [
        {
          label: "Subregions",
          value: "次区域",
          leaf: true,
          level: 2,
          disabled: true,
          display: true,
        },
        { label: "North Africa", value: "北非", leaf: false, level: 2 },
        { label: "West Asia", value: "西亚", leaf: false, level: 2 },
      ],
    },
    { label: "North America", value: "北美地区", leaf: false, level: 1 },
    { label: "South Asia", value: "南亚地区", leaf: false, level: 1 },
    {
      label: "Latin America & Caribbean",
      value: "拉丁美洲与加勒比地区",
      leaf: false,
      level: 1,
    },
    {
      label: "Sub-Saharan Africa",
      value: "撒哈拉以南非洲地区",
      leaf: false,
      level: 1,
    },
    {
      label: "Europe & Central Asia",
      value: "欧洲与中亚地区",
      leaf: false,
      level: 1,
    },
  ];
  function update_options() {
    if (locale.value === "chinese") {
      options = original_options;
      timeSelection1.value = "年";
      timeSelection2.value = "月";
      timeSelection3.value = "小时";
      if(select_time_value.value === "Year" || select_time_value.value === "年"){
        timeScale.value = "年";
        select_time_value.value = "年";
      }else if(select_time_value.value === "Month" || select_time_value.value === "月"){
        timeScale.value = "月";
        select_time_value.value = "月";
      }else if(select_time_value.value === "Hour" || select_time_value.value === "小时"){
        timeScale.value = "小时";
        select_time_value.value = "小时";
      }
    } else {
      options = original_options_en;
      timeSelection1.value = "Year";
      timeSelection2.value = "Month";
      timeSelection3.value = "Hour";
      if(select_time_value.value === "年" || select_time_value.value === "Year"){
        timeScale.value = "Year";
        select_time_value.value = "Year";
      }else if(select_time_value.value === "月" || select_time_value.value === "Month"){
        timeScale.value = "Month";
        select_time_value.value = "Month";
      }else if(select_time_value.value === "小时" || select_time_value.value === "Hour"){
        timeScale.value = "Hour";
        select_time_value.value = "Hour";
      }
    }
    
  }
  let timeSelectWidth = ref('160px');
  // 右面时间分辨率年月小时改变
  const handleTimeChange = (newValue) => {
    if (newValue !== null) {
      // 修改宽度
      timeSelectWidth.value = '80px';
    }
    
    selectedYear.value = [];
    selectedMonth.value = [];
    selectedHour.value = [];
    Year_Hour.value = "";
    Year_Month.value = "";
    yearValue.value = "";
    timeScale.value = newValue;
    timeResult.value = [];
    timeSpan_year.value = [];
    timeSpan_month.value = [];
    timeSpan_hour.value = [];
    time_value.value = newValue;
    if (newValue === "年" || newValue === "Year") {
      timeScale_line.value = "yr";
      timeSpan.value = timeSpan_year.value;
      showTimePicker.value = 1;
    } else if (newValue === "月" || newValue === "Month") {
      timeScale_line.value = 'mo_${Year_Month.value}';
      timeSpan.value = timeSpan_month.value;
      showTimePicker.value = 2;
    } else if (newValue === "小时" || newValue === "Hour") {
      timeSpan.value = timeSpan_hour.value;
      timeScale_line.value = `hr_${Year_Hour.value}`;
      showTimePicker.value = 3;
    } else {
      showTimePicker.value = 0;
    }
  };
  
  let language = localStorage.getItem("language");
  const timeSelection = computed(() => [
    {
      value: t("chart.year"),
      label: t("chart.year"),
    },
    {
      value: t("chart.month"),
      label: t("chart.month"),
    },
    {
      value: t("chart.hour"),
      label: t("chart.hour"),
    },
  ]);
  
  //timeResult的值  右面日历变化
  const handleYearChange = (value) => {
    console.log("handleYearChange触发");
    timeResult.value = value;
    timeScale_line.value = "yr";
    timeScale.value = value;
    timeSpan.value = value;
    timeSpan_year.value = timeSpan.value;
  };
  const handleMonthChange = (value) => {
    console.log("handleMonthChange触发");
    console.log("新值为:", value);
    timeResult.value = value;
    // timeScale_line.value = "mo";
    timeScale.value = value;
    if (value === null) {
      timeSpan.value = [];
    } else {
      timeSpan.value = [...value];
      timeSpan.value = timeSpan.value.map((item) =>
        item.replace(/(\d{4})(\d{2})/, "$1$2")
      );
    };
    timeSpan_month.value = timeSpan.value;
  
    console.log("timeSpan.value:", timeSpan.value);
  };
  const getFormatTime=(date)=>{
    // 原始时间字符串（示例）
    // 1. 分割日期和时间部分（按空格拆分）
    const [dateStr, timeStr] = date.split(" "); 
  
    // 2. 分割日期为 年、月、日（按横杠拆分）
    const [year, month, day] = dateStr.split("-"); 
  
    // 3. 分割时间为 小时（取第一个冒号前的部分）
    const hour = timeStr.split(":")[0]; 
  
    // 4. 拼接为 "2021010100" 格式
    const formattedTime = year + month + day + hour; 
  
    return formattedTime;
  };
  const handleHourChange = (value) => {
    console.log("handleHourChange触发");
    timeScale_line.value = `hr_${Year_Hour.value}`;
    console.log("小时改变");
    console.log("value的值:", value);
    if (!value || !value[0] || !value[1]) {
      timeResult.value = [];
      timeSpan.value = [];
      return;
    }
    let startTime = value[0];
    let endTime = value[1];
  
    const result = [startTime, endTime];
    timeResult.value = result;
    timeSpan.value = [...value];
    timeSpan_hour.value = timeSpan.value;
  };
  // 月的年改变
  const handleYear_MonthChange = (value) => {
    Year_Month.value = value;
    timeScale_line.value = `mo_${Year_Month.value}`;
  };
  // 小时的年改变
  const handleYear_HourChange = (value) => {
    Year_Hour.value = value;
    timeScale_line.value = `hr_${Year_Hour.value}`;
  };
  
  // 右侧确定按钮响应
  const searchData = async (values) => {
    // 折线图
    console.log("map的值:", selectedMap.value);
    //传递给TableBox组件和Map2D的参数
    const boundaryType = selectedMap.value.includes("陆上风电")
      ? "boundary"
      : "ocean";
    const boundaryArea = selectedPlaces.value.join(",");
    const boundaryParams = {
      level: selectedLevel.value,
      area: boundaryArea,
      class: boundaryType,
      CRS: "EPSG:4326",
    };
  
  
    if (timeScale_line.value !== "yr") {
      tableParams_year.value = yearValue.value;
      if (timeScale_line.value.startsWith("mo")) {
        tableParams_timescale.value = "月";
      } else {
        tableParams_timescale.value = "小时";
      }
    } else {
      tableParams_year.value = "";
      tableParams_timescale.value = "年";
    }
  
    
    const tableParams = {
      map: selectedMap.value.split('/').slice(0, -1).join('/'),
      timescale: tableParams_timescale.value,
      year: tableParams_year.value,
      level: selectedLevel.value,
      language: language,
    };
    bus.emit("boundaryParams", boundaryParams);
    bus.emit("tableParams", tableParams);
    // 没有选择则返回
    if (selectedMap.value === "") return;
    if (timeScale_line.value === "") return;
  
    
    const params = {
      // map: "发电侧/风力发电/低空风电/陆上风电/潜力数据/2.5MW/发电潜力/10km/",
      // map: timeScale_line.value.length === 2 ? selectedMap.value.slice(0, -1) : selectedMap.value.slice(0, -2),
      map: selectedMap.value.slice(0, -1),
      timescale: timeScale_line.value,
      timespan: timeSpan.value === null ? [] : timeSpan.value,
      level: selectedLevel.value,
      places: selectedPlaces.value,
      language: language,
    };
  
    const res = await getLinedata(params);
    resData.value = [res];
    // 处理图表数据
    await getChartData();
    updateChart();
  
  
  };
  
  //清除所选项
  const handleClearSelected = (data) => {
    resData1.value = data;
  };
  
  //时间选择器的时间范围
  function disabledDate(time) {
    return time.getTime() > Date.now();
  }
  //选择小时 -- 必须确定年份
  const yearValue = ref("");
  
  function disabledMonthDate(time) {
    const selectedYear = Number(yearValue.value);
    if (time.getFullYear() !== selectedYear) {
      return true; // 不是指定年份，禁用
    }
    return false; // 没有首个时间，允许选择（只限制年份）
  }
  
  function disabledHourDate(time) {
    const selectedYear = Number(yearValue.value);
    if (time.getFullYear() !== selectedYear) {
      return true; // 不是指定年份，禁用
    }
    return false; // 没有首个时间，允许选择（只限制年份）
  }
  
  //接收DataService文件传的地图数据
  const props = defineProps({
    selectMapPath: {
      type: String,
      default: ''
    },
    dataUnit: {
      type: String,
      default: ''
    },
    dataTypeThree: {
      type: String,
      default: ''
    },
    hourYear: {
      type: String,
      default: ''
    }
  });
  
  const dataUnitMap = {
    "GWh/km<sup>2</sup>": "GWh/km²",
    "MW/km<sup>2</sup>": "MW/km²",
  };
  
  const selectedValues = ref("");
  let is_download_Disabled=true;
  // 监听 props.selectMapPath 变化  左侧
  watch(
    () => props.selectMapPath,
    async (newPath) => {
      try {
        if (newPath) {
          if(newPath.includes('潜力数据') && !newPath.startsWith('用户侧') && !newPath.startsWith('电网侧')){
            is_download_Disabled = false;
          }
          else{
            is_download_Disabled = true;
          }
          // 更新选中值\\
          console.log("newPath:", newPath);
          selectedValues.value = newPath.split("/").slice(0, -1).join("/");
          selectedMap.value = newPath.split("/").slice(0, -1).join("/");
          // map_time_value.value = newPath.split("/").pop();
          // time_value.value = newPath.split("/").pop();
          select_time_value.value = newPath.split("/").pop();
          showTimePicker.value = 0;
          
          let tmpTime = ref("")
          let tmpMap= ""
          if(newPath.includes("年")){
            tmpTime.value = "yr"
            tmpMap = selectedMap.value.slice(0, -1)
          }else if(newPath.includes("月")){
            tmpTime.value = `mo_${props.hourYear}`
            tmpMap = selectedMap.value.slice(0, -1)
          }else {
            const tempHour = ref("");
            const handleHourYear = (value) => {
              tempHour.value = value;
              tmpTime.value = `hr_${tempHour.value}`;
            }
            tmpTime.value = `hr_${props.hourYear}`;
            tmpMap = selectedMap.value.slice(0, -2)
          }
  
          const params = {
            // map: "发电侧/风力发电/低空风电/陆上风电/潜力数据/2.5MW/发电潜力/10km",
            map: tmpMap,
            timescale: tmpTime.value,
            timespan: [],
            level: "区域",
            places: [
              "东亚与太平洋地区",
              "中东与北非地区",
              "北美地区",
              "南亚地区",
              "拉丁美洲与加勒比地区",
              "撒哈拉以南非洲地区",
              "欧洲与中亚地区",
            ],
            language: language,
          };
  
          
          console.log("params:",params)
          
          const region_params = {
            map: tmpMap.split('/').slice(0, -1).join('/'),
          }
          const region_res = await getRegions(region_params);
          if(region_res){
            console.log("region_res.regions:",region_res.regionsZn)
            options = dynamic_region(region_res.regionsZn)
          }
  
          const res  = await getLinedata(params);
  
          const description_params = {
            map: tmpMap.split('/').slice(0, -1).join('/'),
            language:language,
          }
          const description_res = await getDescription(description_params);
          if(description_res){
            resData1.value = description_res
          }
          
          resData.value = [res];
          // 处理图表数据
          await getChartData();
          updateChart();
        } else {
          resData1.value = null
          // 路径为空时清空数据
          selectedValues.value = [];
          resData.value = [];
          selectedMap.value = "";
          // 可以考虑在这里也调用 updateChart() 来清空图表
          await getChartData();
          updateChart();
        }
        updateChart();
      } catch (error) {
        console.error("数据处理失败:", error);
        // 可以在这里添加错误处理逻辑，比如显示错误提示
      }
    }
    // { immediate: true } // 根据需求决定是否启用
  );
  // 根据路径区域动态显示
  function dynamic_region(regions) {
    const arr = regions.split('/'); 
    const length = arr.length;
    if (locale.value === "chinese") {
      const newOption = original_options
      return filterNodesByLevel(newOption,length)
    } else {
      const newOption = original_options_en
      return filterNodesByLevel(newOption,length)
    }
  }
  
  function filterNodesByLevel(nodes,level) {
    return nodes.reduce((filtered, node) => {
      // 1. 过滤 > level 的节点
      console.log("level:",level)
      if(node.level === level){
        node.leaf = true
        console.log("node:",node)
      }
      if (node.level > level) {
        return filtered; // 跳过该节点
      }
      // 2. 深拷贝节点（避免修改原数据，可选但推荐）
      const newNode = { ...node };
      // 3. 递归处理子节点（若存在 children）
      if (newNode.children && Array.isArray(newNode.children)) {
        newNode.children = filterNodesByLevel(newNode.children,level);
      }
      // 4. 将处理后的节点加入结果数组
      filtered.push(newNode);
      return filtered;
    }, []);
  }
  
  let is_show = ref(false);
  
  onMounted(() => {
    initChart();
  });
  
  // 响应式数据
  const resData = ref([]);
  
  // 层级映射配置
  const LEVEL_MAPPING = {
    1: "区域",
    2: "次区域",
    3: "国家",
    4: "省级",
    5: "县级",
    6: "县级",
  };
  
  const selectedAreaValue = ref([]);
  let lastValidSelection = ref([]);
  
  
  const handlerChange = async (values) => {
    // 获取所有路径的长度?????????
    // 空选不处理
    if (!values.length) return;
    const pathLengths = values.map((arr) => arr.length);
    const firstLevel = pathLengths[0];
    const isSameLevel = pathLengths.every((length) => length === firstLevel);
    if (!isSameLevel) {
      ElMessage.error("请选择相同层级的区域！");
      // 强制取消勾选（清空 UI 中的勾选项）
      selectedAreaValue.value = [...lastValidSelection.value]; // 彻底还原
      return;
    }
    // 合法则记录新值
    lastValidSelection.value = [...values];
    selectedPlaces.value = values.map((item) => item[item.length - 1]);
    // 获取所有子数组的 length，再取最大值
    const maxLength = Math.max(...values.map((arr) => arr.length));
    selectedLevel.value = LEVEL_MAPPING[maxLength] || "未知层级";
    await getChartData();
    updateChart();
  };
  
  
  
  const getChartData = () => {
    chartData.value = {
      SeriesData: [],
      xAxis: resData.value[0]?.xAxisData || [],
      min_y_value: 0,
      max_y_value: 0.5,
    };
    //确定图表类型
    if (chartData.value.xAxis.length === 1) {
      chartType.value = "bar";
    } else {
      chartType.value = "line";
    }
    // 遍历resData中的每个元素
    resData.value.forEach((element) => {
      // 检查element和SeriesData是否存在
      if (element && element.SeriesData) {
        // 遍历每个SeriesData项
        element.SeriesData.forEach((dataItem) => {
          // 确保dataItem有必要的属性
          if (dataItem.name && dataItem.data) {
            chartData.value.SeriesData.push({
              name: locale.value === "chinese" ? dataItem.name[0] : dataItem.name[1], // 如果name是数组则取第一个元素
              data: dataItem.data,
              // 可以添加其他需要的属性
              // type: 'line', // 例如指定图表类型
              // smooth: true  // 平滑曲线
            });
  
            const maxvalue = Math.max(...dataItem.data);
            const minvalue = Math.min(...dataItem.data);
            chartData.value.max_y_value = Math.max(
              maxvalue,
              chartData.value.max_y_value
            );
            chartData.value.min_y_value = Math.min(
              minvalue,
              chartData.value.min_y_value
            );
          } else {
            console.warn("无效的SeriesData项:", dataItem);
          }
        });
  
        // 如果需要，也可以处理xAxisData
        if (element.xAxisData) {
          chartData.value.xAxisData = element.xAxisData;
        }
      } else {
        console.warn("无效的resData元素:", element);
      }
    });
    console.log("处理后的图数据:", chartData.value);
  };
  
  let mychart = null;
  //初始化echarts
  const initChart = () => {
    console.log("initChart被调用")
    if (chartRef.value) {
      mychart = echarts.init(chartRef.value);
      window.addEventListener("resize", () => {
        mychart.resize();
      });
      console.log("chartRef.value:",chartRef.value)
      console.log("初始化图成功");
    }
  };
  
  const updateChart = () => {
    // initChart();
    mychart.clear();
    // 准备x轴数据（假设所有系列共享相同的x轴）
    const xAxisData = chartData.value.xAxis;
    //确定图表类型
    if (xAxisData.length === 1) {
      chartType.value = "bar";
    } else {
      chartType.value = "line";
    }
    console.log("props", props);
  
    // 转换series数据
    const series = chartData.value.SeriesData.map((dataItem, index) => ({
      name: dataItem.name, // 可以根据你的数据结构调整名称
      // type: "line",
      type: chartType.value,
      data: dataItem.data, // 假设每个数据项有yAxisData字段
      smooth: true,
      symbol: "circle",
      symbolSize: 6,
      itemStyle: {
        color: getColorByIndex(index), // 自定义颜色函数
      },
      lineStyle: {
        width: 2,
      },
      emphasis: {
        focus: "series",
      },
    }));
  
    const option = {
      title: {
        text: selectedMap.value
          ? `${props.dataTypeThree} ${
              props.dataUnit !== ""
                ? `${
                    dataUnitMap[props.dataUnit] === undefined
                      ? ""
                      : `(${dataUnitMap[props.dataUnit]})`
                  }`
                : ""
            }`
          : "",
        left: "center",
        textStyle: {
          color: "#333",
          fontSize: 14,
        },
      },
      tooltip: {
        trigger: "axis",
        axisPointer: {
          type: "cross",
        },
      },
      // legend: {
      //   data: series.map(s => s.name) // 显示图例
      // },
      grid: {
        left: "3%",
        top: "15%", // 为图例留出空间
        right: "5%",
        bottom: "6%",
        containLabel: true,
      },
      xAxis: {
        type: "category",
        boundaryGap: false,
        data: xAxisData,
        name: select_time_value.value || t("chart.year"), // 添加横轴名称，默认为"年"
        nameLocation: "middle", // 名称位置在轴的末端
        nameTextStyle: {
          color: "#333",
          // padding: [-10, 0, 0, -15], // 调整文字位置
          padding: [10, 0, 0, 0], // 调整文字位置
        },
        axisLine: {
          lineStyle: {
            color: "#999",
          },
        },
        axisLabel: {
          color: "#000",
        },
      },
      yAxis: {
        // name: props.dataUnit ? dataUnitMap[props.dataUnit] : "",
        nameTextStyle: {
          color: "#000",
        },
        type: "value",
        min: chartData.value.min_y_value,
        max: chartData.value.max_y_value,
        axisLine: {
          show: true,
        },
        splitLine: {
          lineStyle: {
            type: "dashed",
          },
        },
        axisLabel: {
          color: "#000",
          formatter: function (value) {
            // 使用科学计数法格式化数值
            return value.toExponential(2);
          },
        },
      },
      series: series,
    };
  
    mychart.setOption(option);
  };
  
  // 辅助函数：根据索引返回不同颜色
  const getColorByIndex = (index) => {
    const colors = [
      "#5470C6",
      "#91CC75",
      "#EE6666",
      "#FAC858",
      "#73C0DE",
      "#3BA272",
      "#FC8452",
      "#9A60B4",
    ];
    return colors[index % colors.length];
  };
  </script>
  
  <style lang="scss" scoped>
  .chart_box {
    z-index: 10;
    position: absolute;
    top: 10px;
    right: 30px;
    width: 540px;
    height: 770px;
    border-radius: 4px;
    background: rgba(255, 255, 255, 0.4);
    backdrop-filter: blur(3px);
    box-shadow: 0px 0px 0px 0px rgba(255, 255, 255, 0.8);
  
    .param_box {
      display: flex;
      // justify-content: space-between;
      justify-content: space-around;
      width: 100%;
      height: 50px;
      margin: 10px 1px 10px;
      //设置时间选择器的背景色
      :deep(.el-input__wrapper),
      :deep(.el-select__wrapper) {
        background-color: rgba(255, 255, 255, 0.4) !important;
        color: black !important;
      }
      // 修改el-select的输入框中显示已经选择的option name的字体颜色
      :deep(.el-select__placeholder) {
        color: black !important;
      }
      :deep(.el-input__inner::placeholder) {
        color: black !important;
      }
      .year .month .hour {
        width: 50px;
        height: 30px;
        text-align: center;
        line-height: 30px;
      }
      .hour {
        margin-right: 30px;
      }
      .temporal_resolution {
        :deep(.el-input__inner) {
          color: black; /* 修改为你需要的颜色 */
          --el-input-placeholder-color: black;
          // --el-input-placeholder-color: rgb(71, 66, 66);
        }
        display: inline-block;
        .hourPicker {
          display: inline-block;
          justify-content: space-between;
          :deep(.el-date-editor) {
            .el-range-input {
              color: black; // 修改输入框文本颜色
              &::placeholder {
                color: black; // 修改占位符文本颜色
              }
            }
            .el-range-separator {
              color: black; // 修改分隔符颜色
            }
          }
        }
      }
      /* 1. 禁用整个节点的默认点击事件 */
      .region-cascader .el-cascader-node {
        pointer-events: none;
      }
      /* 2. 仅允许复选框和箭头区域交互 */
      .region-cascader .el-cascader-node__prefix,
      .region-cascader .el-cascader-node__postfix {
        pointer-events: none;
        cursor: pointer;
      }
    }
  
    .echart_box {
      // width: 100%;
      // height: 40%;
      height: 308px;
      width: 504px;
      margin-top: 10px;
  
      .chart {
        height: 308px;
        width: 452px;
        // width: 100%;
        // height: 80%;
        margin: 0px auto;
        border: 1px solid rgb(85, 82, 82, 0.2);
        // :deep(.canvas) {
        //   height: 200px;
        //   width: 300px;
        // }
      }
    }
  }
  .tool-box-container {
    position: absolute;
    bottom: 10px;
    right: 10px;
  }
  </style>
  
  <style scoped>
  .icon-title {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    padding: 5px 10px;
    background: #ffffff;
    border-bottom: 1px solid #ffffff;
    z-index: 1;
  
    height: 40px; /* 固定高度 */
    box-sizing: border-box; /* 确保padding包含在高度内 */
  }
  .node-wrapper {
    display: flex;
    align-items: center;
  }
  .node-text {
    margin-left: 5px;
  }
  .blank-area {
    flex: 1;
  }
  .arrow-container {
    display: flex;
    align-items: center;
    justify-content: center;
    /* 可以根据需要调整宽度和高度 */
    width: 20px;
    height: 20px;
    /* margin-left: 100px; */
    background-color: yellow; /* 设置高亮背景颜色，可按需修改 */
  }
  .arrow {
    /* 红色 */
    color: red;
    /*添加过渡效果，让颜色变化更平滑 */
    transition: color 0.3s ease;
  }
  .el-cascader-arrow-right {
  }
  .noChartData {
        width: 95%;
        height: 100%;
        margin: 0 auto;
        border: 1px solid rgb(85, 82, 82, 0.2);
      }
  </style>
  