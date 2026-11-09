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
            </div>
          </template>
        </el-cascader>
      </div>
      <el-select
        v-model="select_time_value"
        :placeholder="$t('chart.elSelectPlaceholder')"
        @change="handleTimeChange"
        :style="{ width: timeSelectWidth }"
        v-show="showTimePicker > 0"
      >
        <el-option
          v-for="item in timeSelection"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <!-- 当路径包含具体年份时，显示该年份 -->
      <div v-show="showTimePicker === 0 && time_value" class="specific-time-display">
        {{ time_value }}
      </div>
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
import { ElMessage } from "element-plus";


const { t, locale } = useI18n();


// 监听语言变化并更新列表
watch(locale, async () => {
  console.log("语言已切换!!!!!!!!!!!!!!!!");
  console.log("当前语言:", locale.value);
  
  update_options();
  updateChart();
  
  // 清空当前选择的区域，避免语言切换后显示不匹配的选项
  selectedAreaValue.value = [];
  lastValidSelection.value = [];
  
  // 重新获取区域选项数据
  if (selectedMap.value) {
    const region_params = {
      // map: selectedMap.value.split('/').slice(0, -1).join('/'),
      map: getPureMapPath(selectedMap.value),
    };
    
    try {
      const region_res = await getRegions(region_params);
      if(region_res) {
        console.log("语言切换时重新获取区域数据:", region_res);
        options.value = dynamic_region(region_res.regionsZn);
        console.log("语言切换后的options:", options.value);
        console.log("options长度:", options.value.length);
      } else {
        console.log("region_res为空");
        options.value = [];
      }
    } catch (regionError) {
      console.error("语言切换时获取区域数据失败:", regionError);
      options.value = [];
    }
    
    // 重新获取数据说明
    const description_params = {
      map: selectedMap.value.split('/').slice(0, -1).join('/'),
      language: locale.value,
    };
    console.log("语言切换时重新获取数据说明:", description_params);
    try {
      const description_res = await getDescription(description_params);
      if (description_res) {
        resData1.value = description_res;
      } else {
        resData1.value = null;
      }
      console.log("语言切换后的数据说明:", description_res);
    } catch (error) {
      console.error("语言切换时获取数据说明失败:", error);
      resData1.value = null;
    }
  }
});


const resData1 = ref(null);


onMounted(() => {
  bus.on("clearSelected", handleClearSelected);
  initChart();
});


const chartRef = ref(null);


// 图相关数据
const chartData = ref({});
const chartType = ref("line"); // 默认折线图
// 时间分辨率的使用
const select_time_value = ref("");    // 右侧选择的时间分辨率
const map_time_value = ref("");       // 地图传的时间分辨率
const time_value = ref("");           // 地图传的时间分辨率
const choose_time_value = ref(false); // 决定图的横坐标


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
const yearValue = ref("");
// 右侧区域选择的使用
const selectedMap = ref("");
const selectedLevel = ref("区域");
const selectedPlaces = ref([]);
let tmpTimeUnit = ref("");


let tableParams_year = ref("");
let tableParams_timescale = ref("");


let options = ref([]);
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
                          {
                            label: "罗山县",
                            value: "罗山县",
                            leaf: false,
                            level: 7,
                          },
                          {
                            label: "光山县",
                            value: "光山县",
                            leaf: false,
                            level: 7,
                          },
                          {
                            label: "新县",
                            value: "新县",
                            leaf: false,
                            level: 7,
                          },
                          {
                            label: "商城县",
                            value: "商城县",
                            leaf: false,
                            level: 7,
                          },
                          {
                            label: "固始县",
                            value: "固始县",
                            leaf: false,
                            level: 7,
                          },
                          {
                            label: "淮滨县",
                            value: "淮滨县",
                            leaf: false,
                            level: 7,
                          },
                          {
                            label: "息县",
                            value: "息县",
                            leaf: false,
                            level: 7,
                          }
                        ],
                      },
                      {
                        label:"郑州市",
                        value: "郑州市",
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
                            label: "中牟县",
                            value: "中牟县",
                            leaf: false,
                            level: 7,
                          },
                          {
                            label: "中原区",
                            value: "中原区",
                            leaf: false,
                            level: 7,
                          },
                          {
                            label: "二七区",
                            value: "二七区",
                            leaf: false,
                            level: 7,
                          },
                          {
                            label: "巩义市",
                            value: "巩义市",
                            leaf: false,
                            level: 7,
                          },
                          {
                            label: "荥阳市",
                            value: "荥阳市",
                            leaf: false,
                            level: 7,
                          }

                        ]
                      },
                      {
                        label: "开封市",
                        value: "开封市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "龙亭区",
value: "龙亭区",
leaf: false,
level: 7,
},
{
label: "顺河回族区",
value: "顺河回族区",
leaf: false,
level: 7,
},
{
label: "鼓楼区",
value: "鼓楼区",
leaf: false,
level: 7,
},
{
label: "禹王台区",
value: "禹王台区",
leaf: false,
level: 7,
},
{
label: "祥符区",
value: "祥符区",
leaf: false,
level: 7,
}
                          
                        ]
                      },
                      {
                        label: "洛阳市",
                        value: "洛阳市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "老城区",
value: "老城区",
leaf: false,
level: 7,
},
{
label: "西工区",
value: "西工区",
leaf: false,
level: 7,
},
{
label: "瀍河回族区",
value: "瀍河回族区",
leaf: false,
level: 7,
},
{
label: "涧西区",
value: "涧西区",
leaf: false,
level: 7,
},
{
label: "洛龙区",
value: "洛龙区",
leaf: false,
level: 7,
},
{
label: "孟津区",
value: "孟津区",
leaf: false,
level: 7,
},
{
label: "偃师区",
value: "偃师区",
leaf: false,
level: 7,
},
{
label: "新安县",
value: "新安县",
leaf: false,
level: 7,
},
{
label: "栾川县",
value: "栾川县",
leaf: false,
level: 7,
},
{
label: "嵩县",
value: "嵩县",
leaf: false,
level: 7,
},
{
label: "汝阳县",
value: "汝阳县",
leaf: false,
level: 7,
},
{
label: "宜阳县",
value: "宜阳县",
leaf: false,
level: 7,
},
{
label: "洛宁县",
value: "洛宁县",
leaf: false,
level: 7,
},
{
label: "伊川县",
value: "伊川县",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "平顶山市",
                        value: "平顶山市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "新华区",
value: "新华区",
leaf: false,
level: 7,
},
{
label: "卫东区",
value: "卫东区",
leaf: false,
level: 7,
},
{
label: "石龙区",
value: "石龙区",
leaf: false,
level: 7,
},
{
label: "湛河区",
value: "湛河区",
leaf: false,
level: 7,
},
{
label: "宝丰县",
value: "宝丰县",
leaf: false,
level: 7,
},
{
label: "叶县",
value: "叶县",
leaf: false,
level: 7,
},
{
label: "鲁山县",
value: "鲁山县",
leaf: false,
level: 7,
},
{
label: "郏县",
value: "郏县",
leaf: false,
level: 7,
},
{
label: "舞钢市",
value: "舞钢市",
leaf: false,
level: 7,
},
{
label: "汝州市",
value: "汝州市",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "安阳市",
                        value: "安阳市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "文峰区",
value: "文峰区",
leaf: false,
level: 7,
},
{
label: "北关区",
value: "北关区",
leaf: false,
level: 7,
},
{
label: "殷都区",
value: "殷都区",
leaf: false,
level: 7,
},
{
label: "龙安区",
value: "龙安区",
leaf: false,
level: 7,
},
{
label: "安阳县",
value: "安阳县",
leaf: false,
level: 7,
},
{
label: "汤阴县",
value: "汤阴县",
leaf: false,
level: 7,
},
{
label: "滑县",
value: "滑县",
leaf: false,
level: 7,
},
{
label: "内黄县",
value: "内黄县",
leaf: false,
level: 7,
},
{
label: "林州市",
value: "林州市",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "鹤壁市",
                        value: "鹤壁市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "鹤山区",
value: "鹤山区",
leaf: false,
level: 7,
},
{
label: "山城区",
value: "山城区",
leaf: false,
level: 7,
},
{
label: "淇滨区",
value: "淇滨区",
leaf: false,
level: 7,
},
{
label: "浚县",
value: "浚县",
leaf: false,
level: 7,
},
{
label: "淇县",
value: "淇县",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "新乡市",
                        value: "新乡市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "红旗区",
value: "红旗区",
leaf: false,
level: 7,
},
{
label: "卫滨区",
value: "卫滨区",
leaf: false,
level: 7,
},
{
label: "凤泉区",
value: "凤泉区",
leaf: false,
level: 7,
},
{
label: "牧野区",
value: "牧野区",
leaf: false,
level: 7,
},
{
label: "新乡县",
value: "新乡县",
leaf: false,
level: 7,
},
{
label: "获嘉县",
value: "获嘉县",
leaf: false,
level: 7,
},
{
label: "原阳县",
value: "原阳县",
leaf: false,
level: 7,
},
{
label: "延津县",
value: "延津县",
leaf: false,
level: 7,
},
{
label: "封丘县",
value: "封丘县",
leaf: false,
level: 7,
},
{
label: "长垣市",
value: "长垣市",
leaf: false,
level: 7,
},
{
label: "卫辉市",
value: "卫辉市",
leaf: false,
level: 7,
},
{
label: "辉县市",
value: "辉县市",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "焦作市",
                        value: "焦作市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "解放区",
value: "解放区",
leaf: false,
level: 7,
},
{
label: "中站区",
value: "中站区",
leaf: false,
level: 7,
},
{
label: "马村区",
value: "马村区",
leaf: false,
level: 7,
},
{
label: "山阳区",
value: "山阳区",
leaf: false,
level: 7,
},
{
label: "修武县",
value: "修武县",
leaf: false,
level: 7,
},
{
label: "博爱县",
value: "博爱县",
leaf: false,
level: 7,
},
{
label: "武陟县",
value: "武陟县",
leaf: false,
level: 7,
},
{
label: "温县",
value: "温县",
leaf: false,
level: 7,
},
{
label: "沁阳市",
value: "沁阳市",
leaf: false,
level: 7,
},
{
label: "孟州市",
value: "孟州市",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "濮阳市",
                        value: "濮阳市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "华龙区",
value: "华龙区",
leaf: false,
level: 7,
},
{
label: "清丰县",
value: "清丰县",
leaf: false,
level: 7,
},
{
label: "南乐县",
value: "南乐县",
leaf: false,
level: 7,
},
{
label: "范县",
value: "范县",
leaf: false,
level: 7,
},
{
label: "台前县",
value: "台前县",
leaf: false,
level: 7,
},
{
label: "濮阳县",
value: "濮阳县",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "许昌市",
                        value: "许昌市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "魏都区",
value: "魏都区",
leaf: false,
level: 7,
},
{
label: "建安区",
value: "建安区",
leaf: false,
level: 7,
},
{
label: "鄢陵县",
value: "鄢陵县",
leaf: false,
level: 7,
},
{
label: "襄城县",
value: "襄城县",
leaf: false,
level: 7,
},
{
label: "禹州市",
value: "禹州市",
leaf: false,
level: 7,
},
{
label: "长葛市",
value: "长葛市",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "漯河市",
                        value: "漯河市",
                        leaf: false,
                      level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "源汇区",
value: "源汇区",
leaf: false,
level: 7,
},
{
label: "郾城区",
value: "郾城区",
leaf: false,
level: 7,
},
{
label: "召陵区",
value: "召陵区",
leaf: false,
level: 7,
},
{
label: "舞阳县",
value: "舞阳县",
leaf: false,
level: 7,
},
{
label: "临颍县",
value: "临颍县",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "三门峡市",
                        value: "三门峡市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "湖滨区",
value: "湖滨区",
leaf: false,
level: 7,
},
{
label: "陕州区",
value: "陕州区",
leaf: false,
level: 7,
},
{
label: "渑池县",
value: "渑池县",
leaf: false,
level: 7,
},
{
label: "卢氏县",
value: "卢氏县",
leaf: false,
level: 7,
},
{
label: "义马市",
value: "义马市",
leaf: false,
level: 7,
},
{
label: "灵宝市",
value: "灵宝市",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "南阳市",
                        value: "南阳市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "宛城区",
value: "宛城区",
leaf: false,
level: 7,
},
{
label: "卧龙区",
value: "卧龙区",
leaf: false,
level: 7,
},
{
label: "南召县",
value: "南召县",
leaf: false,
level: 7,
},
{
label: "方城县",
value: "方城县",
leaf: false,
level: 7,
},
{
label: "西峡县",
value: "西峡县",
leaf: false,
level: 7,
},
{
label: "镇平县",
value: "镇平县",
leaf: false,
level: 7,
},
{
label: "内乡县",
value: "内乡县",
leaf: false,
level: 7,
},
{
label: "淅川县",
value: "淅川县",
leaf: false,
level: 7,
},
{
label: "社旗县",
value: "社旗县",
leaf: false,
level: 7,
},
{
label: "唐河县",
value: "唐河县",
leaf: false,
level: 7,
},
{
label: "新野县",
value: "新野县",
leaf: false,
level: 7,
},
{
label: "桐柏县",
value: "桐柏县",
leaf: false,
level: 7,
},
{
label: "邓州市",
value: "邓州市",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "商丘市",
                        value: "商丘市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "梁园区",
value: "梁园区",
leaf: false,
level: 7,
},
{
label: "睢阳区",
value: "睢阳区",
leaf: false,
level: 7,
},
{
label: "民权县",
value: "民权县",
leaf: false,
level: 7,
},
{
label: "睢县",
value: "睢县",
leaf: false,
level: 7,
},
{
label: "宁陵县",
value: "宁陵县",
leaf: false,
level: 7,
},
{
label: "柘城县",
value: "柘城县",
leaf: false,
level: 7,
},
{
label: "虞城县",
value: "虞城县",
leaf: false,
level: 7,
},
{
label: "夏邑县",
value: "夏邑县",
leaf: false,
level: 7,
},
{
label: "永城市",
value: "永城市",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "周口市",
                        value: "周口市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "川汇区",
value: "川汇区",
leaf: false,
level: 7,
},
{
label: "淮阳区",
value: "淮阳区",
leaf: false,
level: 7,
},
{
label: "扶沟县",
value: "扶沟县",
leaf: false,
level: 7,
},
{
label: "西华县",
value: "西华县",
leaf: false,
level: 7,
},
{
label: "商水县",
value: "商水县",
leaf: false,
level: 7,
},
{
label: "沈丘县",
value: "沈丘县",
leaf: false,
level: 7,
},
{
label: "郸城县",
value: "郸城县",
leaf: false,
level: 7,
},
{
label: "太康县",
value: "太康县",
leaf: false,
level: 7,
},
{
label: "鹿邑县",
value: "鹿邑县",
leaf: false,
level: 7,
},
{
label: "项城市",
value: "项城市",
leaf: false,
level: 7,
}
                        ]
                      },
                      {
                        label: "驻马店市",
                        value: "驻马店市",
                        leaf: false,
                        level: 6,
                        children: [
                           {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label:"驿城区",
value: "驿城区",
leaf: false,
level: 7,
},
{
label:"西平县",
value: "西平县",
leaf: false,
level: 7,
},
{
label:"上蔡县",
value: "上蔡县",
leaf: false,
level: 7,
},
{
label:"平舆县",
value: "平舆县",
leaf: false,
level: 7,
},
{
label:"正阳县",
value: "正阳县",
leaf: false,
level: 7,
},
{
label:"确山县",
value: "确山县",
leaf: false,
level: 7,
},
{
label:"泌阳县",
value: "泌阳县",
leaf: false,
level: 7,
},
{
label:"遂平县",
value: "遂平县",
leaf: false,
level: 7,
},
{
label:"汝南县",
value: "汝南县",
leaf: false,
level: 7,
},
{
label:"新蔡县",
value: "新蔡县",
leaf: false,
level: 7,
},
                        ]
                      }
                    ],
                  },
                  {
                    label: "湖北省", value: "湖北省", leaf: false, level: 5,
                    children:[
                              {
                                label:"地级",
                                value: "地级",
                                leaf: false,
                                level: 6,
                              },
                              {
                                label: "武汉市",
                                value: "武汉市",
                                leaf: false,
                                level: 6,
                                children: [
                                            {
                                              label:"县级",
                                              value: "县级",
                                              leaf: false,
                                              level: 7,
                                            },
                                            {label: "江岸区", value: "江岸区", leaf: false, level: 7},
                                            { label: "江汉区", value: "江汉区", leaf: false, level: 7 },
                                            { label: "硚口区", value: "硚口区", leaf: false, level: 7 },
                                            { label: "汉阳区", value: "汉阳区", leaf: false, level: 7 },
                                            { label: "武昌区", value: "武昌区", leaf: false, level: 7 },
                                            { label: "青山区", value: "青山区", leaf: false, level: 7 },
                                            { label: "洪山区", value: "洪山区", leaf: false, level: 7 },
                                            { label: "东西湖区", value: "东西湖区", leaf: false, level: 7 },
                                            { label: "汉南区", value: "汉南区", leaf: false, level: 7 },
                                            { label: "蔡甸区", value: "蔡甸区", leaf: false, level: 7 },
                                            { label: "江夏区", value: "江夏区", leaf: false, level: 7 },
                                            { label: "黄陂区", value: "黄陂区", leaf: false, level: 7 },
                                            { label: "新洲区", value: "新洲区", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "黄石市",
                    value: "黄石市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "黄石港区", value: "黄石港区", leaf: false, level: 7 },
{ label: "西塞山区", value: "西塞山区", leaf: false, level: 7 },
{ label: "下陆区", value: "下陆区", leaf: false, level: 7 },
{ label: "铁山区", value: "铁山区", leaf: false, level: 7 },
{ label: "阳新县", value: "阳新县", leaf: false, level: 7 },
{ label: "大冶市", value: "大冶市", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "十堰市",
                    value: "十堰市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "茅箭区", value: "茅箭区", leaf: false, level: 7 },
{ label: "张湾区", value: "张湾区", leaf: false, level: 7 },
{ label: "郧阳区", value: "郧阳区", leaf: false, level: 7 },
{ label: "郧西县", value: "郧西县", leaf: false, level: 7 },
{ label: "竹山县", value: "竹山县", leaf: false, level: 7 },
{ label: "竹溪县", value: "竹溪县", leaf: false, level: 7 },
{ label: "房县", value: "房县", leaf: false, level: 7 },
{ label: "丹江口市", value: "丹江口市", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "宜昌市",
                    value: "宜昌市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "西陵区", value: "西陵区", leaf: false, level: 7 },
{ label: "伍家岗区", value: "伍家岗区", leaf: false, level: 7 },
{ label: "点军区", value: "点军区", leaf: false, level: 7 },
{ label: "猇亭区", value: "猇亭区", leaf: false, level: 7 },
{ label: "夷陵区", value: "夷陵区", leaf: false, level: 7 },
{ label: "远安县", value: "远安县", leaf: false, level: 7 },
{ label: "兴山县", value: "兴山县", leaf: false, level: 7 },
{ label: "秭归县", value: "秭归县", leaf: false, level: 7 },
{ label: "长阳土家族自治县", value: "长阳土家族自治县", leaf: false, level: 7 },
{ label: "五峰土家族自治县", value: "五峰土家族自治县", leaf: false, level: 7 },
{ label: "宜都市", value: "宜都市", leaf: false, level: 7 },
{ label: "当阳市", value: "当阳市", leaf: false, level: 7 },
{ label: "枝江市", value: "枝江市", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "襄阳市",
                    value: "襄阳市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "襄城区", value: "襄城区", leaf: false, level: 7 },
{ label: "樊城区", value: "樊城区", leaf: false, level: 7 },
{ label: "襄州区", value: "襄州区", leaf: false, level: 7 },
{ label: "南漳县", value: "南漳县", leaf: false, level: 7 },
{ label: "谷城县", value: "谷城县", leaf: false, level: 7 },
{ label: "保康县", value: "保康县", leaf: false, level: 7 },
{ label: "老河口市", value: "老河口市", leaf: false, level: 7 },
{ label: "枣阳市", value: "枣阳市", leaf: false, level: 7 },
{ label: "宜城市", value: "宜城市", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "鄂州市",
                    value: "鄂州市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "梁子湖区", value: "梁子湖区", leaf: false, level: 7 },
{ label: "华容区", value: "华容区", leaf: false, level: 7 },
{ label: "鄂城区", value: "鄂城区", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "荆门市",
                    value: "荆门市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "东宝区", value: "东宝区", leaf: false, level: 7 },
{ label: "掇刀区", value: "掇刀区", leaf: false, level: 7 },
{ label: "京山市", value: "京山市", leaf: false, level: 7 },
{ label: "沙洋县", value: "沙洋县", leaf: false, level: 7 },
{ label: "钟祥市", value: "钟祥市", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "孝感市",
                    value: "孝感市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "孝南区", value: "孝南区", leaf: false, level: 7 },
{ label: "孝昌县", value: "孝昌县", leaf: false, level: 7 },
{ label: "大悟县", value: "大悟县", leaf: false, level: 7 },
{ label: "云梦县", value: "云梦县", leaf: false, level: 7 },
{ label: "应城市", value: "应城市", leaf: false, level: 7 },
{ label: "安陆市", value: "安陆市", leaf: false, level: 7 },
{ label: "汉川市", value: "汉川市", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "荆州市",
                    value: "荆州市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "沙市区", value: "沙市区", leaf: false, level: 7 },
{ label: "荆州区", value: "荆州区", leaf: false, level: 7 },
{ label: "公安县", value: "公安县", leaf: false, level: 7 },
{ label: "监利市", value: "监利市", leaf: false, level: 7 },
{ label: "江陵县", value: "江陵县", leaf: false, level: 7 },
{ label: "石首市", value: "石首市", leaf: false, level: 7 },
{ label: "洪湖市", value: "洪湖市", leaf: false, level: 7 },
{ label: "松滋市", value: "松滋市", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "黄冈市",
                    value: "黄冈市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "黄州区", value: "黄州区", leaf: false, level: 7 },
{ label: "团风县", value: "团风县", leaf: false, level: 7 },
{ label: "红安县", value: "红安县", leaf: false, level: 7 },
{ label: "罗田县", value: "罗田县", leaf: false, level: 7 },
{ label: "英山县", value: "英山县", leaf: false, level: 7 },
{ label: "浠水县", value: "浠水县", leaf: false, level: 7 },
{ label: "蕲春县", value: "蕲春县", leaf: false, level: 7 },
{ label: "黄梅县", value: "黄梅县", leaf: false, level: 7 },
{ label: "麻城市", value: "麻城市", leaf: false, level: 7 },
{ label: "武穴市", value: "武穴市", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "咸宁市",
                    value: "咸宁市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "咸安区", value: "咸安区", leaf: false, level: 7 },
{ label: "嘉鱼县", value: "嘉鱼县", leaf: false, level: 7 },
{ label: "通城县", value: "通城县", leaf: false, level: 7 },
{ label: "崇阳县", value: "崇阳县", leaf: false, level: 7 },
{ label: "通山县", value: "通山县", leaf: false, level: 7 },
{ label: "赤壁市", value: "赤壁市", leaf: false, level: 7 }
                    ]
                  },
                  {
                    label: "随州市",
                    value: "随州市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          { label: "曾都区", value: "曾都区", leaf: false, level: 7 },
{ label: "随县", value: "随县", leaf: false, level: 7 },
{ label: "广水市", value: "广水市", leaf: false, level: 7 }
                    ]
                  }

                  ]
                  },
                  {
                  label: "湖南省", value: "湖南省", leaf: false, level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
                    label: "长沙市",
                    value: "长沙市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "芙蓉区",
value: "芙蓉区",
leaf: false,
level: 7,
},
{
label: "天心区",
value: "天心区",
leaf: false,
level: 7,
},
{
label: "岳麓区",
value: "岳麓区",
leaf: false,
level: 7,
},
{
label: "雨花区",
value: "雨花区",
leaf: false,
level: 7,
},
{
label: "望城区",
value: "望城区",
leaf: false,
level: 7,
},
{
label: "长沙县",
value: "长沙县",
leaf: false,
level: 7,
},
{
label: "宁乡市",
value: "宁乡市",
leaf: false,
level: 7,
},
{
label: "浏阳市",
value: "浏阳市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "株洲市",
                    value: "株洲市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "天元区",
value: "天元区",
leaf: false,
level: 7,
},
{
label: "荷塘区",
value: "荷塘区",
leaf: false,
level: 7,
},
{
label: "芦淞区",
value: "芦淞区",
leaf: false,
level: 7,
},
{
label: "石峰区",
value: "石峰区",
leaf: false,
level: 7,
},
{
label: "渌口区",
value: "渌口区",
leaf: false,
level: 7,
},
{
label: "攸县",
value: "攸县",
leaf: false,
level: 7,
},
{
label: "茶陵县",
value: "茶陵县",
leaf: false,
level: 7,
},
{
label: "炎陵县",
value: "炎陵县",
leaf: false,
level: 7,
},
{
label: "醴陵市",
value: "醴陵市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "湘潭市",
                    value: "湘潭市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "雨湖区",
value: "雨湖区",
leaf: false,
level: 7,
},
{
label: "岳塘区",
value: "岳塘区",
leaf: false,
level: 7,
},
{
label: "湘潭县",
value: "湘潭县",
leaf: false,
level: 7,
},
{
label: "湘乡市",
value: "湘乡市",
leaf: false,
level: 7,
},
{
label: "韶山市",
value: "韶山市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "衡阳市",
                    value: "衡阳市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "珠晖区",
value: "珠晖区",
leaf: false,
level: 7,
},
{
label: "雁峰区",
value: "雁峰区",
leaf: false,
level: 7,
},
{
label: "石鼓区",
value: "石鼓区",
leaf: false,
level: 7,
},
{
label: "蒸湘区",
value: "蒸湘区",
leaf: false,
level: 7,
},
{
label: "南岳区",
value: "南岳区",
leaf: false,
level: 7,
},
{
label: "衡阳县",
value: "衡阳县",
leaf: false,
level: 7,
},
{
label: "衡南县",
value: "衡南县",
leaf: false,
level: 7,
},
{
label: "衡山县",
value: "衡山县",
leaf: false,
level: 7,
},
{
label: "衡东县",
value: "衡东县",
leaf: false,
level: 7,
},
{
label: "祁东县",
value: "祁东县",
leaf: false,
level: 7,
},
{
label: "耒阳市",
value: "耒阳市",
leaf: false,
level: 7,
},
{
label: "常宁市",
value: "常宁市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "邵阳市",
                    value: "邵阳市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "双清区",
value: "双清区",
leaf: false,
level: 7,
},
{
label: "大祥区",
value: "大祥区",
leaf: false,
level: 7,
},
{
label: "北塔区",
value: "北塔区",
leaf: false,
level: 7,
},
{
label: "邵东市",
value: "邵东市",
leaf: false,
level: 7,
},
{
label: "新邵县",
value: "新邵县",
leaf: false,
level: 7,
},
{
label: "邵阳县",
value: "邵阳县",
leaf: false,
level: 7,
},
{
label: "隆回县",
value: "隆回县",
leaf: false,
level: 7,
},
{
label: "洞口县",
value: "洞口县",
leaf: false,
level: 7,
},
{
label: "绥宁县",
value: "绥宁县",
leaf: false,
level: 7,
},
{
label: "新宁县",
value: "新宁县",
leaf: false,
level: 7,
},
{
label: "城步苗族自治县",
value: "城步苗族自治县",
leaf: false,
level: 7,
},
{
label: "武冈市",
value: "武冈市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "岳阳市",
                    value: "岳阳市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "岳阳楼区",
value: "岳阳楼区",
leaf: false,
level: 7,
},
{
label: "云溪区",
value: "云溪区",
leaf: false,
level: 7,
},
{
label: "君山区",
value: "君山区",
leaf: false,
level: 7,
},
{
label: "岳阳县",
value: "岳阳县",
leaf: false,
level: 7,
},
{
label: "华容县",
value: "华容县",
leaf: false,
level: 7,
},
{
label: "湘阴县",
value: "湘阴县",
leaf: false,
level: 7,
},
{
label: "平江县",
value: "平江县",
leaf: false,
level: 7,
},
{
label: "汨罗市",
value: "汨罗市",
leaf: false,
level: 7,
},
{
label: "临湘市",
value: "临湘市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "常德市",
                    value: "常德市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "武陵区",
value: "武陵区",
leaf: false,
level: 7,
},
{
label: "鼎城区",
value: "鼎城区",
leaf: false,
level: 7,
},
{
label: "安乡县",
value: "安乡县",
leaf: false,
level: 7,
},
{
label: "汉寿县",
value: "汉寿县",
leaf: false,
level: 7,
},
{
label: "澧县",
value: "澧县",
leaf: false,
level: 7,
},
{
label: "临澧县",
value: "临澧县",
leaf: false,
level: 7,
},
{
label: "桃源县",
value: "桃源县",
leaf: false,
level: 7,
},
{
label: "石门县",
value: "石门县",
leaf: false,
level: 7,
},
{
label: "津市市",
value: "津市市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "张家界市",
                    value: "张家界市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "永定区",
value: "永定区",
leaf: false,
level: 7,
},
{
label: "武陵源区",
value: "武陵源区",
leaf: false,
level: 7,
},
{
label: "慈利县",
value: "慈利县",
leaf: false,
level: 7,
},
{
label: "桑植县",
value: "桑植县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "益阳市",
                    value: "益阳市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "资阳区",
value: "资阳区",
leaf: false,
level: 7,
},
{
label: "赫山区",
value: "赫山区",
leaf: false,
level: 7,
},
{
label: "南县",
value: "南县",
leaf: false,
level: 7,
},
{
label: "桃江县",
value: "桃江县",
leaf: false,
level: 7,
},
{
label: "安化县",
value: "安化县",
leaf: false,
level: 7,
},
{
label: "沅江市",
value: "沅江市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "郴州市",
                    value: "郴州市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "北湖区",
value: "北湖区",
leaf: false,
level: 7,
},
{
label: "苏仙区",
value: "苏仙区",
leaf: false,
level: 7,
},
{
label: "桂阳县",
value: "桂阳县",
leaf: false,
level: 7,
},
{
label: "宜章县",
value: "宜章县",
leaf: false,
level: 7,
},
{
label: "永兴县",
value: "永兴县",
leaf: false,
level: 7,
},
{
label: "嘉禾县",
value: "嘉禾县",
leaf: false,
level: 7,
},
{
label: "临武县",
value: "临武县",
leaf: false,
level: 7,
},
{
label: "汝城县",
value: "汝城县",
leaf: false,
level: 7,
},
{
label: "桂东县",
value: "桂东县",
leaf: false,
level: 7,
},
{
label: "安仁县",
value: "安仁县",
leaf: false,
level: 7,
},
{
label: "资兴市",
value: "资兴市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "永州市",
                    value: "永州市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "零陵区",
value: "零陵区",
leaf: false,
level: 7,
},
{
label: "冷水滩区",
value: "冷水滩区",
leaf: false,
level: 7,
},
{
label: "祁阳县",
value: "祁阳县",
leaf: false,
level: 7,
},
{
label: "东安县",
value: "东安县",
leaf: false,
level: 7,
},
{
label: "双牌县",
value: "双牌县",
leaf: false,
level: 7,
},
{
label: "道县",
value: "道县",
leaf: false,
level: 7,
},
{
label: "江永县",
value: "江永县",
leaf: false,
level: 7,
},
{
label: "宁远县",
value: "宁远县",
leaf: false,
level: 7,
},
{
label: "蓝山县",
value: "蓝山县",
leaf: false,
level: 7,
},
{
label: "新田县",
value: "新田县",
leaf: false,
level: 7,
},
{
label: "江华瑶族自治县",
value: "江华瑶族自治县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "怀化市",
                    value: "怀化市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "鹤城区",
value: "鹤城区",
leaf: false,
level: 7,
},
{
label: "中方县",
value: "中方县",
leaf: false,
level: 7,
},
{
label: "沅陵县",
value: "沅陵县",
leaf: false,
level: 7,
},
{
label: "辰溪县",
value: "辰溪县",
leaf: false,
level: 7,
},
{
label: "溆浦县",
value: "溆浦县",
leaf: false,
level: 7,
},
{
label: "会同县",
value: "会同县",
leaf: false,
level: 7,
},
{
label: "麻阳苗族自治县",
value: "麻阳苗族自治县",
leaf: false,
level: 7,
},
{
label: "新晃侗族自治县",
value: "新晃侗族自治县",
leaf: false,
level: 7,
},
{
label: "芷江侗族自治县",
value: "芷江侗族自治县",
leaf: false,
level: 7,
},
{
label: "靖州苗族侗族自治县",
value: "靖州苗族侗族自治县",
leaf: false,
level: 7,
},
{
label: "通道侗族自治县",
value: "通道侗族自治县",
leaf: false,
level: 7,
},
{
label: "洪江市",
value: "洪江市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "娄底市",
                    value: "娄底市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "娄星区",
value: "娄星区",
leaf: false,
level: 7,
},
{
label: "双峰县",
value: "双峰县",
leaf: false,
level: 7,
},
{
label: "新化县",
value: "新化县",
leaf: false,
level: 7,
},
{
label: "冷水江市",
value: "冷水江市",
leaf: false,
level: 7,
},
{
label: "涟源市",
value: "涟源市",
leaf: false,
level: 7,
}
                    ]
                    }
                  ]
                  },
                  {
                  label: "江西省", value: "江西省", leaf: false, level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
                    label: "南昌市",
                    value: "南昌市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "东湖区",
value: "东湖区",
leaf: false,
level: 7,
},
{
label: "西湖区",
value: "西湖区",
leaf: false,
level: 7,
},
{
label: "青云谱区",
value: "青云谱区",
leaf: false,
level: 7,
},
{
label: "新建区",
value: "新建区",
leaf: false,
level: 7,
},
{
label: "南昌县",
value: "南昌县",
leaf: false,
level: 7,
},
{
label: "进贤县",
value: "进贤县",
leaf: false,
level: 7,
},
{
label: "安义县",
value: "安义县",
leaf: false,
level: 7,
},
                    ]
                    },
                    {
                    label: "九江市",
                    value: "九江市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "浔阳区",
value: "浔阳区",
leaf: false,
level: 7,
},
{
label: "濂溪区",
value: "濂溪区",
leaf: false,
level: 7,
},
{
label: "柴桑区",
value: "柴桑区",
leaf: false,
level: 7,
},
{
label: "武宁县",
value: "武宁县",
leaf: false,
level: 7,
},
{
label: "修水县",
value: "修水县",
leaf: false,
level: 7,
},
{
label: "永修县",
value: "永修县",
leaf: false,
level: 7,
},
{
label: "德安县",
value: "德安县",
leaf: false,
level: 7,
},
{
label: "都昌县",
value: "都昌县",
leaf: false,
level: 7,
},
{
label: "湖口县",
value: "湖口县",
leaf: false,
level: 7,
},
{
label: "彭泽县",
value: "彭泽县",
leaf: false,
level: 7,
},
{
label: "瑞昌市",
value: "瑞昌市",
leaf: false,
level: 7,
},
{
label: "庐山市",
value: "庐山市",
leaf: false,
level: 7,
},
{
label: "共青城市",
value: "共青城市",
leaf: false,
level: 7,
},
                    ]
                    },
                    {
                    label: "景德镇市",
                    value: "景德镇市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "昌江区",
value: "昌江区",
leaf: false,
level: 7,
},
{
label: "珠山区",
value: "珠山区",
leaf: false,
level: 7,
},
{
label: "乐平市",
value: "乐平市",
leaf: false,
level: 7,
},
{
label: "浮梁县",
value: "浮梁县",
leaf: false,
level: 7,
},
                    ]
                    },
                    {
                    label: "萍乡市",
                    value: "萍乡市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "安源区",
value: "安源区",
leaf: false,
level: 7,
},
{
label: "湘东区",
value: "湘东区",
leaf: false,
level: 7,
},
{
label: "莲花县",
value: "莲花县",
leaf: false,
level: 7,
},
{
label: "上栗县",
value: "上栗县",
leaf: false,
level: 7,
},
{
label: "芦溪县",
value: "芦溪县",
leaf: false,
level: 7,
},
                    ]
                    },
                    {
                    label: "新余市",
                    value: "新余市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "渝水区",
value: "渝水区",
leaf: false,
level: 7,
},
{
label: "分宜县",
value: "分宜县",
leaf: false,
level: 7,
},
                    ]
                    },
                    {
                    label: "鹰潭市",
                    value: "鹰潭市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "月湖区",
value: "月湖区",
leaf: false,
level: 7,
},
{
label: "余江区",
value: "余江区",
leaf: false,
level: 7,
},
{
label: "贵溪市",
value: "贵溪市",
leaf: false,
level: 7,
},
                    ]
                    },
                    {
                    label: "赣州市",
                    value: "赣州市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "章贡区",
value: "章贡区",
leaf: false,
level: 7,
},
{
label: "南康区",
value: "南康区",
leaf: false,
level: 7,
},
{
label: "赣县区",
value: "赣县区",
leaf: false,
level: 7,
},
{
label: "瑞金市",
value: "瑞金市",
leaf: false,
level: 7,
},
{
label: "信丰县",
value: "信丰县",
leaf: false,
level: 7,
},
{
label: "大余县",
value: "大余县",
leaf: false,
level: 7,
},
{
label: "上犹县",
value: "上犹县",
leaf: false,
level: 7,
},
{
label: "崇义县",
value: "崇义县",
leaf: false,
level: 7,
},
{
label: "安远县",
value: "安远县",
leaf: false,
level: 7,
},
{
label: "龙南市",
value: "龙南市",
leaf: false,
level: 7,
},
{
label: "定南县",
value: "定南县",
leaf: false,
level: 7,
},
{
label: "全南县",
value: "全南县",
leaf: false,
level: 7,
},
{
label: "宁都县",
value: "宁都县",
leaf: false,
level: 7,
},
{
label: "于都县",
value: "于都县",
leaf: false,
level: 7,
},
{
label: "兴国县",
value: "兴国县",
leaf: false,
level: 7,
},
{
label: "会昌县",
value: "会昌县",
leaf: false,
level: 7,
},
{
label: "寻乌县",
value: "寻乌县",
leaf: false,
level: 7,
},
{
label: "石城县",
value: "石城县",
leaf: false,
level: 7,
},

                    ]
                    },
                    {
                    label: "宜春市",
                    alue: "宜春市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "袁州区",
value: "袁州区",
leaf: false,
level: 7,
},
{
label: "丰城市",
value: "丰城市",
leaf: false,
level: 7,
},
{
label: "樟树市",
value: "樟树市",
leaf: false,
level: 7,
},
{
label: "高安市",
value: "高安市",
leaf: false,
level: 7,
},
{
label: "奉新县",
value: "奉新县",
leaf: false,
level: 7,
},
{
label: "万载县",
value: "万载县",
leaf: false,
level: 7,
},
{
label: "上高县",
value: "上高县",
leaf: false,
level: 7,
},
{
label: "宜丰县",
value: "宜丰县",
leaf: false,
level: 7,
},
{
label: "靖安县",
value: "靖安县",
leaf: false,
level: 7,
},
{
label: "铜鼓县",
value: "铜鼓县",
leaf: false,
level: 7,
},

                    ]
                    },
                    {
                    label: "上饶市",
                    value: "上饶市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "信州区",
value: "信州区",
leaf: false,
level: 7,
},
{
label: "广丰区",
value: "广丰区",
leaf: false,
level: 7,
},
{
label: "广信区",
value: "广信区",
leaf: false,
level: 7,
},
{
label: "德兴市",
value: "德兴市",
leaf: false,
level: 7,
},
{
label: "玉山县",
value: "玉山县",
leaf: false,
level: 7,
},
{
label: "铅山县",
value: "铅山县",
leaf: false,
level: 7,
},
{
label: "横峰县",
value: "横峰县",
leaf: false,
level: 7,
},
{
label: "弋阳县",
value: "弋阳县",
leaf: false,
level: 7,
},
{
label: "余干县",
value: "余干县",
leaf: false,
level: 7,
},
{
label: "鄱阳县",
value: "鄱阳县",
leaf: false,
level: 7,
},
{
label: "万年县",
value: "万年县",
leaf: false,
level: 7,
},
{
label: "婺源县",
value: "婺源县",
leaf: false,
level: 7,
},
                    ]
                    },
                    {
                    label: "吉安市",
                    value: "吉安市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "吉州区",
value: "吉州区",
leaf: false,
level: 7,
},
{
label: "青原区",
value: "青原区",
leaf: false,
level: 7,
},
{
label: "井冈山市",
value: "井冈山市",
leaf: false,
level: 7,
},
{
label: "吉安县",
value: "吉安县",
leaf: false,
level: 7,
},
{
label: "吉水县",
value: "吉水县",
leaf: false,
level: 7,
},
{
label: "峡江县",
value: "峡江县",
leaf: false,
level: 7,
},
{
label: "新干县",
value: "新干县",
leaf: false,
level: 7,
},
{
label: "永丰县",
value: "永丰县",
leaf: false,
level: 7,
},
{
label: "泰和县",
value: "泰和县",
leaf: false,
level: 7,
},
{
label: "遂川县",
value: "遂川县",
leaf: false,
level: 7,
},
{
label: "万安县",
value: "万安县",
leaf: false,
level: 7,
},
{
label: "安福县",
value: "安福县",
leaf: false,
level: 7,
},
{
label: "永新县",
value: "永新县",
leaf: false,
level: 7,
},
                    ]
                    },
                    {
                    label: "抚州市",
                    value: "抚州市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "临川区",
value: "临川区",
leaf: false,
level: 7,
},
{
label: "东乡区",
value: "东乡区",
leaf: false,
level: 7,
},
{
label: "南城县",
value: "南城县",
leaf: false,
level: 7,
},
{
label: "黎川县",
value: "黎川县",
leaf: false,
level: 7,
},
{
label: "南丰县",
value: "南丰县",
leaf: false,
level: 7,
},
{
label: "崇仁县",
value: "崇仁县",
leaf: false,
level: 7,
},
{
label: "乐安县",
value: "乐安县",
leaf: false,
level: 7,
},
{
label: "宜黄县",
value: "宜黄县",
leaf: false,
level: 7,
},
{
label: "金溪县",
value: "金溪县",
leaf: false,
level: 7,
},
{
label: "资溪县",
value: "资溪县",
leaf: false,
level: 7,
},
{
label: "广昌县",
value: "广昌县",
leaf: false,
level: 7,
},
                    ]
                    }
                  ]
                  }
                ],
              },
              {
                label: "东北",
                value: "东北",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "省级",
                    value: "省级",
                    leaf: true,
                    level: 5,
                    disabled: true,
                    display: true,
                  },
                  {
                    label: "黑龙江省", value: "黑龙江省", leaf: false, level: 5,
                    children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
                      label: "哈尔滨市",
                      value: "哈尔滨市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "道里区",
value: "道里区",
leaf: false,
level: 7,
},
{
label: "道外区",
value: "道外区",
leaf: false,
level: 7,
},
{
label: "南岗区",
value: "南岗区",
leaf: false,
level: 7,
},
{
label: "香坊区",
value: "香坊区",
leaf: false,
level: 7,
},
{
label: "平房区",
value: "平房区",
leaf: false,
level: 7,
},
{
label: "松北区",
value: "松北区",
leaf: false,
level: 7,
},
{
label: "呼兰区",
value: "呼兰区",
leaf: false,
level: 7,
},
{
label: "阿城区",
value: "阿城区",
leaf: false,
level: 7,
},
{
label: "双城区",
value: "双城区",
leaf: false,
level: 7,
},
{
label: "依兰县",
value: "依兰县",
leaf: false,
level: 7,
},
{
label: "方正县",
value: "方正县",
leaf: false,
level: 7,
},
{
label: "宾县",
value: "宾县",
leaf: false,
level: 7,
},
{
label: "巴彦县",
value: "巴彦县",
leaf: false,
level: 7,
},
{
label: "木兰县",
value: "木兰县",
leaf: false,
level: 7,
},
{
label: "通河县",
value: "通河县",
leaf: false,
level: 7,
},
{
label: "延寿县",
value: "延寿县",
leaf: false,
level: 7,
},
                      ]
                      },
                      {
                      label: "齐齐哈尔市",
                      value: "齐齐哈尔市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "龙沙区",
value: "龙沙区",
leaf: false,
level: 7,
},
{
label: "建华区",
value: "建华区",
leaf: false,
level: 7,
},
{
label: "铁锋区",
value: "铁锋区",
leaf: false,
level: 7,
},
{
label: "昂昂溪区",
value: "昂昂溪区",
leaf: false,
level: 7,
},
{
label: "富拉尔基区",
value: "富拉尔基区",
leaf: false,
level: 7,
},
{
label: "碾子山区",
value: "碾子山区",
leaf: false,
level: 7,
},
{
label: "梅里斯达斡尔族区",
value: "梅里斯达斡尔族区",
leaf: false,
level: 7,
},
{
label: "龙江县",
value: "龙江县",
leaf: false,
level: 7,
},
{
label: "依安县",
value: "依安县",
leaf: false,
level: 7,
},
{
label: "泰来县",
value: "泰来县",
leaf: false,
level: 7,
},
{
label: "甘南县",
value: "甘南县",
leaf: false,
level: 7,
},
{
label: "富裕县",
value: "富裕县",
leaf: false,
level: 7,
},
{
label: "克山县",
value: "克山县",
leaf: false,
level: 7,
},
{
label: "克东县",
value: "克东县",
leaf: false,
level: 7,
},
{
label: "拜泉县",
value: "拜泉县",
leaf: false,
level: 7,
},
                      ]
                      },
                      {
                      label: "鸡西市",
                      value: "鸡西市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "鸡冠区",
value: "鸡冠区",
leaf: false,
level: 7,
},
{
label: "恒山区",
value: "恒山区",
leaf: false,
level: 7,
},
{
label: "滴道区",
value: "滴道区",
leaf: false,
level: 7,
},
{
label: "梨树区",
value: "梨树区",
leaf: false,
level: 7,
},
{
label: "城子河区",
value: "城子河区",
leaf: false,
level: 7,
},
{
label: "麻山区",
value: "麻山区",
leaf: false,
level: 7,
},
{
label: "鸡东县",
value: "鸡东县",
leaf: false,
level: 7,
},
{
label: "虎林市",
value: "虎林市",
leaf: false,
level: 7,
},
{
label: "密山市",
value: "密山市",
leaf: false,
level: 7,
},
                      ]
                      },
                      {
                      label: "鹤岗市",
                      value: "鹤岗市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "向阳区",
value: "向阳区",
leaf: false,
level: 7,
},
{
label: "工农区",
value: "工农区",
leaf: false,
level: 7,
},
{
label: "南山区",
value: "南山区",
leaf: false,
level: 7,
},
{
label: "兴安区",
value: "兴安区",
leaf: false,
level: 7,
},
{
label: "东山区",
value: "东山区",
leaf: false,
level: 7,
},
{
label: "兴山区",
value: "兴山区",
leaf: false,
level: 7,
},
{
label: "萝北县",
value: "萝北县",
leaf: false,
level: 7,
},
{
label: "绥滨县",
value: "绥滨县",
leaf: false,
level: 7,
},

                      ]
                      },
                      {
                      label: "双鸭山市",
                      value: "双鸭山市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "尖山区",
value: "尖山区",
leaf: false,
level: 7,
},
{
label: "岭东区",
value: "岭东区",
leaf: false,
level: 7,
},
{
label: "四方台区",
value: "四方台区",
leaf: false,
level: 7,
},
{
label: "宝山区",
value: "宝山区",
leaf: false,
level: 7,
},
{
label: "集贤县",
value: "集贤县",
leaf: false,
level: 7,
},
{
label: "友谊县",
value: "友谊县",
leaf: false,
level: 7,
},
{
label: "宝清县",
value: "宝清县",
leaf: false,
level: 7,
},
{
label: "饶河县",
value: "饶河县",
leaf: false,
level: 7,
},

                      ]
                      },
                      {
                      label: "大庆市",
                      value: "大庆市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "萨尔图区",
value: "萨尔图区",
leaf: false,
level: 7,
},
{
label: "龙凤区",
value: "龙凤区",
leaf: false,
level: 7,
},
{
label: "让胡路区",
value: "让胡路区",
leaf: false,
level: 7,
},
{
label: "红岗区",
value: "红岗区",
leaf: false,
level: 7,
},
{
label: "肇州县",
value: "肇州县",
leaf: false,
level: 7,
},
{
label: "肇源县",
value: "肇源县",
leaf: false,
level: 7,
},
{
label: "林甸县",
value: "林甸县",
leaf: false,
level: 7,
},
{
label: "杜尔伯特蒙古族自治县",
value: "杜尔伯特蒙古族自治县",
leaf: false,
level: 7,
},
                      ]
                      },
                      {
                      label: "伊春市",
                      value: "伊春市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "伊美区",
value: "伊美区",
leaf: false,
level: 7,
},
{
label: "乌翠区",
value: "乌翠区",
leaf: false,
level: 7,
},
{
label: "友好区",
value: "友好区",
leaf: false,
level: 7,
},
{
label: "金林区",
value: "金林区",
leaf: false,
level: 7,
},
{
label: "汤旺县",
value: "汤旺县",
leaf: false,
level: 7,
},
{
label: "丰林县",
value: "丰林县",
leaf: false,
level: 7,
},
{
label: "大箐山县",
value: "大箐山县",
leaf: false,
level: 7,
},
{
label: "南岔县",
value: "南岔县",
leaf: false,
level: 7,
},
{
label: "嘉荫县",
value: "嘉荫县",
leaf: false,
level: 7,
},
{
label: "铁力市",
value: "铁力市",
leaf: false,
level: 7,
},
                      ]
                      },
                      {
                      label: "佳木斯市",
                      value: "佳木斯市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "向阳区",
value: "向阳区",
leaf: false,
level: 7,
},
{
label: "前进区",
value: "前进区",
leaf: false,
level: 7,
},
{
label: "东风区",
value: "东风区",
leaf: false,
level: 7,
},
{
label: "郊区",
value: "郊区",
leaf: false,
level: 7,
},
{
label: "桦南县",
value: "桦南县",
leaf: false,
level: 7,
},
{
label: "桦川县",
value: "桦川县",
leaf: false,
level: 7,
},
{
label: "汤原县",
value: "汤原县",
leaf: false,
level: 7,
},
{
label: "同江市",
value: "同江市",
leaf: false,
level: 7,
},
{
label: "富锦市",
value: "富锦市",
leaf: false,
level: 7,
},
{
label: "抚远市",
value: "抚远市",
leaf: false,
level: 7,
},
                      ]
                      },
                      {
                      label: "七台河市",
                      value: "七台河市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "新兴区",
value: "新兴区",
leaf: false,
level: 7,
},
{
label: "桃山区",
value: "桃山区",
leaf: false,
level: 7,
},
{
label: "茄子河区",
value: "茄子河区",
leaf: false,
level: 7,
},
{
label: "勃利县",
value: "勃利县",
leaf: false,
level: 7,
},
                      ]
                      },
                      {
                      label: "牡丹江市",
                      value: "牡丹江市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "东安区",
value: "东安区",
leaf: false,
level: 7,
},
{
label: "阳明区",
value: "阳明区",
leaf: false,
level: 7,
},
{
label: "爱民区",
value: "爱民区",
leaf: false,
level: 7,
},
{
label: "西安区",
value: "西安区",
leaf: false,
level: 7,
},
{
label: "林口县",
value: "林口县",
leaf: false,
level: 7,
},
{
label: "绥芬河市",
value: "绥芬河市",
leaf: false,
level: 7,
},
{
label: "海林市",
value: "海林市",
leaf: false,
level: 7,
},
{
label: "宁安市",
value: "宁安市",
leaf: false,
level: 7,
},
{
label: "穆棱市",
value: "穆棱市",
leaf: false,
level: 7,
},
{
label: "东宁市",
value: "东宁市",
leaf: false,
level: 7,
},
                      ]
                      },
                      {
                      label: "黑河市",
                      value: "黑河市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "爱辉区",
value: "爱辉区",
leaf: false,
level: 7,
},
{
label: "嫩江市",
value: "嫩江市",
leaf: false,
level: 7,
},
{
label: "逊克县",
value: "逊克县",
leaf: false,
level: 7,
},
{
label: "孙吴县",
value: "孙吴县",
leaf: false,
level: 7,
},
{
label: "北安市",
value: "北安市",
leaf: false,
level: 7,
},
                      ]
                      },
                      {
                      label: "绥化市",
                      value: "绥化市",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "北林区",
value: "北林区",
leaf: false,
level: 7,
},
{
label: "望奎县",
value: "望奎县",
leaf: false,
level: 7,
},
{
label: "兰西县",
value: "兰西县",
leaf: false,
level: 7,
},
{
label: "青冈县",
value: "青冈县",
leaf: false,
level: 7,
},
{
label: "庆安县",
value: "庆安县",
leaf: false,
level: 7,
},
{
label: "明水县",
value: "明水县",
leaf: false,
level: 7,
},
{
label: "绥棱县",
value: "绥棱县",
leaf: false,
level: 7,
},
{
label: "安达市",
value: "安达市",
leaf: false,
level: 7,
},
{
label: "肇东市",
value: "肇东市",
leaf: false,
level: 7,
},
{
label: "海伦市",
value: "海伦市",
leaf: false,
level: 7,
},
                      ]
                      },
                      {
                      label: "大兴安岭地区",
                      value: "大兴安岭地区",
                      leaf: false,
                      level: 6,
                      children: [
                         {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "呼玛县",
value: "呼玛县",
leaf: false,
level: 7,
},
{
label: "塔河县",
value: "塔河县",
leaf: false,
level: 7,
},
{
label: "漠河市",
value: "漠河市",
leaf: false,
level: 7,
},
                      ]
                      }

                    ]
                  },
                  {
                    label: "吉林省", value: "吉林省", leaf: false, level: 5,
                    children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
                    label: "长春市",
                    value: "长春市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "南关区",
value: "南关区",
leaf: false,
level: 7,
},
{
label: "宽城区",
value: "宽城区",
leaf: false,
level: 7,
},
{
label: "朝阳区",
value: "朝阳区",
leaf: false,
level: 7,
},
{
label: "二道区",
value: "二道区",
leaf: false,
level: 7,
},
{
label: "绿园区",
value: "绿园区",
leaf: false,
level: 7,
},
{
label: "双阳区",
value: "双阳区",
leaf: false,
level: 7,
},
{
label: "九台区",
value: "九台区",
leaf: false,
level: 7,
},
{
label: "农安县",
value: "农安县",
leaf: false,
level: 7,
},
{
label: "榆树市",
value: "榆树市",
leaf: false,
level: 7,
},
{
label: "德惠市",
value: "德惠市",
leaf: false,
level: 7,
},
{
label: "公主岭市",
value: "公主岭市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "吉林市",
                    value: "吉林市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "船营区",
value: "船营区",
leaf: false,
level: 7,
},
{
label: "丰满区",
value: "丰满区",
leaf: false,
level: 7,
},
{
label: "永吉县",
value: "永吉县",
leaf: false,
level: 7,
},
{
label: "蛟河市",
value: "蛟河市",
leaf: false,
level: 7,
},
{
label: "桦甸市",
value: "桦甸市",
leaf: false,
level: 7,
},
{
label: "舒兰市",
value: "舒兰市",
leaf: false,
level: 7,
},
{
label: "磐石市",
value: "磐石市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "四平市",
                    value: "四平市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "铁西区",
value: "铁西区",
leaf: false,
level: 7,
},
{
label: "铁东区",
value: "铁东区",
leaf: false,
level: 7,
},
{
label: "梨树县",
value: "梨树县",
leaf: false,
level: 7,
},
{
label: "伊通满族自治县",
value: "伊通满族自治县",
leaf: false,
level: 7,
},
{
label: "双辽市",
value: "双辽市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "辽源市",
                    value: "辽源市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "龙山区",
value: "龙山区",
leaf: false,
level: 7,
},
{
label: "西安区",
value: "西安区",
leaf: false,
level: 7,
},
{
label: "东丰县",
value: "东丰县",
leaf: false,
level: 7,
},
{
label: "东辽县",
value: "东辽县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "通化市",
                    value: "通化市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "东昌区",
value: "东昌区",
leaf: false,
level: 7,
},
{
label: "二道江区",
value: "二道江区",
leaf: false,
level: 7,
},
{
label: "通化县",
value: "通化县",
leaf: false,
level: 7,
},
{
label: "辉南县",
value: "辉南县",
leaf: false,
level: 7,
},
{
label: "柳河县",
value: "柳河县",
leaf: false,
level: 7,
},
{
label: "梅河口市",
value: "梅河口市",
leaf: false,
level: 7,
},
{
label: "集安市",
value: "集安市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "白山市",
                    value: "白山市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "浑江区",
value: "浑江区",
leaf: false,
level: 7,
},
{
label: "江源区",
value: "江源区",
leaf: false,
level: 7,
},
{
label: "抚松县",
value: "抚松县",
leaf: false,
level: 7,
},
{
label: "靖宇县",
value: "靖宇县",
leaf: false,
level: 7,
},
{
label: "临江市",
value: "临江市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "松原市",
                    value: "松原市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "宁江区",
value: "宁江区",
leaf: false,
level: 7,
},
{
label: "前郭尔罗斯蒙古族自治县",
value: "前郭尔罗斯蒙古族自治县",
leaf: false,
level: 7,
},
{
label: "长岭县",
value: "长岭县",
leaf: false,
level: 7,
},
{
label: "乾安县",
value: "乾安县",
leaf: false,
level: 7,
},
{
label: "扶余市",
value: "扶余市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "白城市",
                    value: "白城市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "洮北区",
value: "洮北区",
leaf: false,
level: 7,
},
{
label: "镇赉县",
value: "镇赉县",
leaf: false,
level: 7,
},
{
label: "通榆县",
value: "通榆县",
leaf: false,
level: 7,
},
{
label: "洮南市",
value: "洮南市",
leaf: false,
level: 7,
},
{
label: "大安市",
value: "大安市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "延边朝鲜族自治州",
                    value: "延边朝鲜族自治州",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "延吉市",
value: "延吉市",
leaf: false,
level: 7,
},
{
label: "图们市",
value: "图们市",
leaf: false,
level: 7,
},
{
label: "敦化市",
value: "敦化市",
leaf: false,
level: 7,
},
{
label: "珲春市",
value: "珲春市",
leaf: false,
level: 7,
},
{
label: "龙井市",
value: "龙井市",
leaf: false,
level: 7,
},
{
label: "和龙市",
value: "和龙市",
leaf: false,
level: 7,
},
{
label: "汪清县",
value: "汪清县",
leaf: false,
level: 7,
},
{
label: "安图县",
value: "安图县",
leaf: false,
level: 7,
}
                    ]
                    }
                    

                    ]
                  },
                  {
                    label: "辽宁省", value: "辽宁省", leaf: false, level: 5,
                    children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
                    label: "沈阳市",
                    value: "沈阳市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "沈河区",
value: "沈河区",
leaf: false,
level: 7,
},
{
label: "和平区",
value: "和平区",
leaf: false,
level: 7,
},
{
label: "大东区",
value: "大东区",
leaf: false,
level: 7,
},
{
label: "皇姑区",
value: "皇姑区",
leaf: false,
level: 7,
},
{
label: "铁西区",
value: "铁西区",
leaf: false,
level: 7,
},
{
label: "苏家屯区",
value: "苏家屯区",
leaf: false,
level: 7,
},
{
label: "浑南区",
value: "浑南区",
leaf: false,
level: 7,
},
{
label: "沈北新区",
value: "沈北新区",
leaf: false,
level: 7,
},
{
label: "于洪区",
value: "于洪区",
leaf: false,
level: 7,
},
{
label: "辽中区",
value: "辽中区",
leaf: false,
level: 7,
},
{
label: "新民市",
value: "新民市",
leaf: false,
level: 7,
},
{
label: "康平县",
value: "康平县",
leaf: false,
level: 7,
},
{
label: "法库县",
value: "法库县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "大连市",
                    value: "大连市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "中山区",
value: "中山区",
leaf: false,
level: 7,
},
{
label: "西岗区",
value: "西岗区",
leaf: false,
level: 7,
},
{
label: "沙河口区",
value: "沙河口区",
leaf: false,
level: 7,
},
{
label: "甘井子区",
value: "甘井子区",
leaf: false,
level: 7,
},
{
label: "旅顺口区",
value: "旅顺口区",
leaf: false,
level: 7,
},
{
label: "金州区",
value: "金州区",
leaf: false,
level: 7,
},
{
label: "普兰店区",
value: "普兰店区",
leaf: false,
level: 7,
},
{
label: "瓦房店市",
value: "瓦房店市",
leaf: false,
level: 7,
},
{
label: "庄河市",
value: "庄河市",
leaf: false,
level: 7,
},
{
label: "长海县",
value: "长海县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "鞍山市",
                    value: "鞍山市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "铁东区",
value: "铁东区",
leaf: false,
level: 7,
},
{
label: "铁西区",
value: "铁西区",
leaf: false,
level: 7,
},
{
label: "立山区",
value: "立山区",
leaf: false,
level: 7,
},
{
label: "千山区",
value: "千山区",
leaf: false,
level: 7,
},
{
label: "台安县",
value: "台安县",
leaf: false,
level: 7,
},
{
label: "岫岩满族自治县",
value: "岫岩满族自治县",
leaf: false,
level: 7,
},
{
label: "海城市",
value: "海城市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "抚顺市",
                    value: "抚顺市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "新抚区",
value: "新抚区",
leaf: false,
level: 7,
},
{
label: "东洲区",
value: "东洲区",
leaf: false,
level: 7,
},
{
label: "望花区",
value: "望花区",
leaf: false,
level: 7,
},
{
label: "顺城区",
value: "顺城区",
leaf: false,
level: 7,
},
{
label: "抚顺县",
value: "抚顺县",
leaf: false,
level: 7,
},
{
label: "新宾满族自治县",
value: "新宾满族自治县",
leaf: false,
level: 7,
},
{
label: "清原满族自治县",
value: "清原满族自治县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "本溪市",
                    value: "本溪市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "平山区",
value: "平山区",
leaf: false,
level: 7,
},
{
label: "溪湖区",
value: "溪湖区",
leaf: false,
level: 7,
},
{
label: "明山区",
value: "明山区",
leaf: false,
level: 7,
},
{
label: "南芬区",
value: "南芬区",
leaf: false,
level: 7,
},
{
label: "本溪满族自治县",
value: "本溪满族自治县",
leaf: false,
level: 7,
},
{
label: "桓仁满族自治县",
value: "桓仁满族自治县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "丹东市",
                    value: "丹东市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "元宝区",
value: "元宝区",
leaf: false,
level: 7,
},
{
label: "振兴区",
value: "振兴区",
leaf: false,
level: 7,
},
{
label: "振安区",
value: "振安区",
leaf: false,
level: 7,
},
{
label: "宽甸满族自治县",
value: "宽甸满族自治县",
leaf: false,
level: 7,
},
{
label: "东港市",
value: "东港市",
leaf: false,
level: 7,
},
{
label: "凤城市",
value: "凤城市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "锦州市",
                    value: "锦州市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "古塔区",
value: "古塔区",
leaf: false,
level: 7,
},
{
label: "凌河区",
value: "凌河区",
leaf: false,
level: 7,
},
{
label: "太和区",
value: "太和区",
leaf: false,
level: 7,
},
{
label: "黑山县",
value: "黑山县",
leaf: false,
level: 7,
},
{
label: "义县",
value: "义县",
leaf: false,
level: 7,
},
{
label: "凌海市",
value: "凌海市",
leaf: false,
level: 7,
},
{
label: "北镇市",
value: "北镇市",
leaf: false,
level: 7,
}

                    ]
                    },
                    {
                    label: "营口市",
                    value: "营口市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "站前区",
value: "站前区",
leaf: false,
level: 7,
},
{
label: "西市区",
value: "西市区",
leaf: false,
level: 7,
},
{
label: "鲅鱼圈区",
value: "鲅鱼圈区",
leaf: false,
level: 7,
},
{
label: "老边区",
value: "老边区",
leaf: false,
level: 7,
},
{
label: "盖州市",
value: "盖州市",
leaf: false,
level: 7,
},
{
label: "大石桥市",
value: "大石桥市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "阜新市",
                    value: "阜新市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "海州区",
value: "海州区",
leaf: false,
level: 7,
},
{
label: "新邱区",
value: "新邱区",
leaf: false,
level: 7,
},
{
label: "太平区",
value: "太平区",
leaf: false,
level: 7,
},
{
label: "清河门区",
value: "清河门区",
leaf: false,
level: 7,
},
{
label: "细河区",
value: "细河区",
leaf: false,
level: 7,
},
{
label: "阜新蒙古族自治县",
value: "阜新蒙古族自治县",
leaf: false,
level: 7,
},
{
label: "彰武县",
value: "彰武县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "辽阳市",
                    value: "辽阳市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "白塔区",
value: "白塔区",
leaf: false,
level: 7,
},
{
label: "文圣区",
value: "文圣区",
leaf: false,
level: 7,
},
{
label: "宏伟区",
value: "宏伟区",
leaf: false,
level: 7,
},
{
label: "弓长岭区",
value: "弓长岭区",
leaf: false,
level: 7,
},
{
label: "太子河区",
value: "太子河区",
leaf: false,
level: 7,
},
{
label: "辽阳县",
value: "辽阳县",
leaf: false,
level: 7,
},
{
label: "灯塔市",
value: "灯塔市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "盘锦市",
                    value: "盘锦市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "双台子区",
value: "双台子区",
leaf: false,
level: 7,
},
{
label: "兴隆台区",
value: "兴隆台区",
leaf: false,
level: 7,
},
{
label: "大洼区",
value: "大洼区",
leaf: false,
level: 7,
},
{
label: "盘山县",
value: "盘山县",
leaf: false,
level: 7,
}

                    ]
                    },
                    {
                    label: "铁岭市",
                    value: "铁岭市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "银州区",
value: "银州区",
leaf: false,
level: 7,
},
{
label: "清河区",
value: "清河区",
leaf: false,
level: 7,
},
{
label: "铁岭县",
value: "铁岭县",
leaf: false,
level: 7,
},
{
label: "西丰县",
value: "西丰县",
leaf: false,
level: 7,
},
{
label: "昌图县",
value: "昌图县",
leaf: false,
level: 7,
},
{
label: "调兵山市",
value: "调兵山市",
leaf: false,
level: 7,
},
{
label: "开原市",
value: "开原市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "朝阳市",
                    value: "朝阳市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "双塔区",
value: "双塔区",
leaf: false,
level: 7,
},
{
label: "龙城区",
value: "龙城区",
leaf: false,
level: 7,
},
{
label: "朝阳县",
value: "朝阳县",
leaf: false,
level: 7,
},
{
label: "建平县",
value: "建平县",
leaf: false,
level: 7,
},
{
label: "喀喇沁左翼蒙古族自治县",
value: "喀喇沁左翼蒙古族自治县",
leaf: false,
level: 7,
},
{
label: "北票市",
value: "北票市",
leaf: false,
level: 7,
},
{
label: "凌源市",
value: "凌源市",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "葫芦岛市",
                    value: "葫芦岛市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "连山区",
value: "连山区",
leaf: false,
level: 7,
},
{
label: "龙港区",
value: "龙港区",
leaf: false,
level: 7,
},
{
label: "南票区",
value: "南票区",
leaf: false,
level: 7,
},
{
label: "绥中县",
value: "绥中县",
leaf: false,
level: 7,
},
{
label: "建昌县",
value: "建昌县",
leaf: false,
level: 7,
},
{
label: "兴城市",
value: "兴城市",
leaf: false,
level: 7,
}
                    ]
                    }


                    ]
                  },
                  {
                    label: "内蒙古自治区", value: "内蒙古自治区", leaf: false, level: 5,
                    children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
                    label: "乌兰察布市",
                    value: "乌兰察布市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "集宁区",
value: "集宁区",
leaf: false,
level: 7,
},
{
label: "丰镇市",
value: "丰镇市",
leaf: false,
level: 7,
},
{
label: "卓资县",
value: "卓资县",
leaf: false,
level: 7,
},
{
label: "化德县",
value: "化德县",
leaf: false,
level: 7,
},
{
label: "商都县",
value: "商都县",
leaf: false,
level: 7,
},
{
label: "兴和县",
value: "兴和县",
leaf: false,
level: 7,
},
{
label: "凉城县",
value: "凉城县",
leaf: false,
level: 7,
},
{
label: "察哈尔右翼前旗",
value: "察哈尔右翼前旗",
leaf: false,
level: 7,
},
{
label: "察哈尔右翼中旗",
value: "察哈尔右翼中旗",
leaf: false,
level: 7,
},
{
label: "察哈尔右翼后旗",
value: "察哈尔右翼后旗",
leaf: false,
level: 7,
},
{
label: "四子王旗",
value: "四子王旗",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "乌海市",
                    value: "乌海市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "海勃湾区",
value: "海勃湾区",
leaf: false,
level: 7,
},
{
label: "海南区",
value: "海南区",
leaf: false,
level: 7,
},
{
label: "乌达区",
value: "乌达区",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "呼和浩特市",
                    value: "呼和浩特市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "回民区",
value: "回民区",
leaf: false,
level: 7,
},
{
label: "赛罕区",
value: "赛罕区",
leaf: false,
level: 7,
},
{
label: "土默特左旗",
value: "土默特左旗",
leaf: false,
level: 7,
},
{
label: "托克托县",
value: "托克托县",
leaf: false,
level: 7,
},
{
label: "和林格尔县",
value: "和林格尔县",
leaf: false,
level: 7,
},
{
label: "清水河县",
value: "清水河县",
leaf: false,
level: 7,
},
{
label: "武川县",
value: "武川县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "包头市",
                    value: "包头市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "昆都仑区",
value: "昆都仑区",
leaf: false,
level: 7,
},
{
label: "白云鄂博矿区",
value: "白云鄂博矿区",
leaf: false,
level: 7,
},
{
label: "土默特右旗",
value: "土默特右旗",
leaf: false,
level: 7,
},
{
label: "达尔罕茂明安联合旗",
value: "达尔罕茂明安联合旗",
leaf: false,
level: 7,
},
{
label: "固阳县",
value: "固阳县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "巴彦淖尔市",
                    value: "巴彦淖尔市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "临河区",
value: "临河区",
leaf: false,
level: 7,
},
{
label: "五原县",
value: "五原县",
leaf: false,
level: 7,
},
{
label: "磴口县",
value: "磴口县",
leaf: false,
level: 7,
},
{
label: "乌拉特中旗",
value: "乌拉特中旗",
leaf: false,
level: 7,
},
{
label: "乌拉特前旗",
value: "乌拉特前旗",
leaf: false,
level: 7,
},
{
label: "乌拉特后旗",
value: "乌拉特后旗",
leaf: false,
level: 7,
},
{
label: "杭锦后旗",
value: "杭锦后旗",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "呼伦贝尔市",
                    value: "呼伦贝尔市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "海拉尔区",
value: "海拉尔区",
leaf: false,
level: 7,
},
{
label: "扎兰屯市",
value: "扎兰屯市",
leaf: false,
level: 7,
},
{
label: "满洲里市",
value: "满洲里市",
leaf: false,
level: 7,
},
{
label: "牙克石市",
value: "牙克石市",
leaf: false,
level: 7,
},
{
label: "额尔古纳市",
value: "额尔古纳市",
leaf: false,
level: 7,
},
{
label: "根河市",
value: "根河市",
leaf: false,
level: 7,
},
{
label: "阿荣旗",
value: "阿荣旗",
leaf: false,
level: 7,
},
{
label: "莫力达瓦达斡尔族自治旗",
value: "莫力达瓦达斡尔族自治旗",
leaf: false,
level: 7,
},
{
label: "鄂伦春自治旗",
value: "鄂伦春自治旗",
leaf: false,
level: 7,
},
{
label: "鄂温克族自治旗",
value: "鄂温克族自治旗",
leaf: false,
level: 7,
},
{
label: "陈巴尔虎旗",
value: "陈巴尔虎旗",
leaf: false,
level: 7,
},
{
label: "新巴尔虎右旗",
value: "新巴尔虎右旗",
leaf: false,
level: 7,
},
{
label: "新巴尔虎左旗",
value: "新巴尔虎左旗",
leaf: false,
level: 7,
},
{
label: "扎赉诺尔区",
value: "扎赉诺尔区",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "兴安盟",
                    value: "兴安盟",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "乌兰浩特市",
value: "乌兰浩特市",
leaf: false,
level: 7,
},
{
label: "阿尔山市",
value: "阿尔山市",
leaf: false,
level: 7,
},
{
label: "科尔沁右翼前旗",
value: "科尔沁右翼前旗",
leaf: false,
level: 7,
},
{
label: "科尔沁右翼中旗",
value: "科尔沁右翼中旗",
leaf: false,
level: 7,
},
{
label: "扎赉特旗",
value: "扎赉特旗",
leaf: false,
level: 7,
},
{
label: "突泉县",
value: "突泉县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "锡林郭勒盟",
                    value: "锡林郭勒盟",
                    eaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "锡林浩特市",
value: "锡林浩特市",
leaf: false,
level: 7,
},
{
label: "二连浩特市",
value: "二连浩特市",
leaf: false,
level: 7,
},
{
label: "阿巴嘎旗",
value: "阿巴嘎旗",
leaf: false,
level: 7,
},
{
label: "苏尼特左旗",
value: "苏尼特左旗",
leaf: false,
level: 7,
},
{
label: "苏尼特右旗",
value: "苏尼特右旗",
leaf: false,
level: 7,
},
{
label: "东乌珠穆沁旗",
value: "东乌珠穆沁旗",
leaf: false,
level: 7,
},
{
label: "西乌珠穆沁旗",
value: "西乌珠穆沁旗",
leaf: false,
level: 7,
},
{
label: "太仆寺旗",
value: "太仆寺旗",
leaf: false,
level: 7,
},
{
label: "镶黄旗",
value: "镶黄旗",
leaf: false,
level: 7,
},
{
label: "正镶白旗",
value: "正镶白旗",
leaf: false,
level: 7,
},
{
label: "正蓝旗",
value: "正蓝旗",
leaf: false,
level: 7,
},
{
label: "多伦县",
value: "多伦县",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "阿拉善盟",
                    value: "阿拉善盟",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "阿拉善右旗",
value: "阿拉善右旗",
leaf: false,
level: 7,
},
{
label: "阿拉善左旗",
value: "阿拉善左旗",
leaf: false,
level: 7,
},
{
label: "额济纳旗",
value: "额济纳旗",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "赤峰市",
                    value: "赤峰市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "红山区",
value: "红山区",
leaf: false,
level: 7,
},
{
label: "元宝山区",
value: "元宝山区",
leaf: false,
level: 7,
},
{
label: "松山区",
value: "松山区",
leaf: false,
level: 7,
},
{
label: "阿鲁科尔沁旗",
value: "阿鲁科尔沁旗",
leaf: false,
level: 7,
},
{
label: "巴林左旗",
value: "巴林左旗",
leaf: false,
level: 7,
},
{
label: "巴林右旗",
value: "巴林右旗",
leaf: false,
level: 7,
},
{
label: "林西县",
value: "林西县",
leaf: false,
level: 7,
},
{
label: "克什克腾旗",
value: "克什克腾旗",
leaf: false,
level: 7,
},
{
label: "翁牛特旗",
value: "翁牛特旗",
leaf: false,
level: 7,
},
{
label: "喀喇沁旗",
value: "喀喇沁旗",
leaf: false,
level: 7,
},
{
label: "宁城县",
value: "宁城县",
leaf: false,
level: 7,
},
{
label: "敖汉旗",
value: "敖汉旗",
leaf: false,
level: 7,
},

                    ]
                    },
                    {
                    label: "通辽市",
                    value: "通辽市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "科尔沁区",
value: "科尔沁区",
leaf: false,
level: 7,
},
{
label: "霍林郭勒市",
value: "霍林郭勒市",
leaf: false,
level: 7,
},
{
label: "科尔沁左翼中旗",
value: "科尔沁左翼中旗",
leaf: false,
level: 7,
},
{
label: "科尔沁左翼后旗",
value: "科尔沁左翼后旗",
leaf: false,
level: 7,
},
{
label: "开鲁县",
value: "开鲁县",
leaf: false,
level: 7,
},
{
label: "库伦旗",
value: "库伦旗",
leaf: false,
level: 7,
},
{
label: "奈曼旗",
value: "奈曼旗",
leaf: false,
level: 7,
},
{
label: "扎鲁特旗",
value: "扎鲁特旗",
leaf: false,
level: 7,
}
                    ]
                    },
                    {
                    label: "鄂尔多斯市",
                    value: "鄂尔多斯市",
                    leaf: false,
                    level: 6,
                    children: [
                       {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "东胜区",
value: "东胜区",
leaf: false,
level: 7,
},
{
label: "康巴什区",
value: "康巴什区",
leaf: false,
level: 7,
},
{
label: "达拉特旗",
value: "达拉特旗",
leaf: false,
level: 7,
},
{
label: "准格尔旗",
value: "准格尔旗",
leaf: false,
level: 7,
},
{
label: "鄂托克前旗",
value: "鄂托克前旗",
leaf: false,
level: 7,
},
{
label: "鄂托克旗",
value: "鄂托克旗",
leaf: false,
level: 7,
},
{
label: "杭锦旗",
value: "杭锦旗",
leaf: false,
level: 7,
},
{
label: "乌审旗",
value: "乌审旗",
leaf: false,
level: 7,
},
{
label: "伊金霍洛旗",
value: "伊金霍洛旗",
leaf: false,
level: 7,
}
                    ]
                    }


                    ]
                  },


                ],
              },
              {
                label: "华东",
                value: "华东",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "省级",
                    value: "省级",
                    leaf: true,
                    level: 5,
                    disabled: true,
                    display: true,
                  },
                  {
                  label: "上海市",
                  value: "上海市",
                  leaf: false,
                  level: 5,
                  children:[
                    {
label: "黄浦区",
value: "黄浦区",
leaf: false,
level: 7,
},
{
label: "徐汇区",
value: "徐汇区",
leaf: false,
level: 7,
},
{
label: "长宁区",
value: "长宁区",
leaf: false,
level: 7,
},
{
label: "静安区",
value: "静安区",
leaf: false,
level: 7,
},
{
label: "普陀区",
value: "普陀区",
leaf: false,
level: 7,
},
{
label: "虹口区",
value: "虹口区",
leaf: false,
level: 7,
},
{
label: "杨浦区",
value: "杨浦区",
leaf: false,
level: 7,
},
{
label: "闵行区",
value: "闵行区",
leaf: false,
level: 7,
},
{
label: "宝山区",
value: "宝山区",
leaf: false,
level: 7,
},
{
label: "嘉定区",
value: "嘉定区",
leaf: false,
level: 7,
},
{
label: "浦东新区",
value: "浦东新区",
leaf: false,
level: 7,
},
{
label: "金山区",
value: "金山区",
leaf: false,
level: 7,
},
{
label: "松江区",
value: "松江区",
leaf: false,
level: 7,
},
{
label: "青浦区",
value: "青浦区",
leaf: false,
level: 7,
},
{
label: "奉贤区",
value: "奉贤区",
leaf: false,
level: 7,
},
{
label: "崇明区",
value: "崇明区",
leaf: false,
level: 7,
}

                  ]
                  },
                  {
                  label: "江苏省",
                  value: "江苏省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
                      label: "南京市",
                      value: "南京市",
                      leaf: false,
                      level: 6,
                      children: [
                          {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "玄武区",
value: "玄武区",
leaf: false,
level: 7,
},
{
label: "秦淮区",
value: "秦淮区",
leaf: false,
level: 7,
},
{
label: "建邺区",
value: "建邺区",
leaf: false,
level: 7,
},
{
label: "鼓楼区",
value: "鼓楼区",
leaf: false,
level: 7,
},
{
label: "浦口区",
value: "浦口区",
leaf: false,
level: 7,
},
{
label: "栖霞区",
value: "栖霞区",
leaf: false,
level: 7,
},
{
label: "雨花台区",
value: "雨花台区",
leaf: false,
level: 7,
},
{
label: "江宁区",
value: "江宁区",
leaf: false,
level: 7,
},
{
label: "六合区",
value: "六合区",
leaf: false,
level: 7,
},
{
label: "溧水区",
value: "溧水区",
leaf: false,
level: 7,
},
{
label: "高淳区",
value: "高淳区",
leaf: false,
level: 7,
}
                          
                        ]
                      },
{
label: "南通市",
value: "南通市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "崇川区",
value: "崇川区",
leaf: false,
level: 7,
},
{
label: "通州区",
value: "通州区",
leaf: false,
level: 7,
},
{
label: "海门区",
value: "海门区",
leaf: false,
level: 7,
},
{
label: "启东市",
value: "启东市",
leaf: false,
level: 7,
},
{
label: "如皋市",
value: "如皋市",
leaf: false,
level: 7,
},
{
label: "如东县",
value: "如东县",
leaf: false,
level: 7,
}

]
},
{
label: "徐州市",
value: "徐州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "鼓楼区",
value: "鼓楼区",
leaf: false,
level: 7,
},
{
label: "云龙区",
value: "云龙区",
leaf: false,
level: 7,
},
{
label: "贾汪区",
value: "贾汪区",
leaf: false,
level: 7,
},
{
label: "泉山区",
value: "泉山区",
leaf: false,
level: 7,
},
{
label: "铜山区",
value: "铜山区",
leaf: false,
level: 7,
},
{
label: "丰县",
value: "丰县",
leaf: false,
level: 7,
},
{
label: "沛县",
value: "沛县",
leaf: false,
level: 7,
},
{
label: "睢宁县",
value: "睢宁县",
leaf: false,
level: 7,
},
{
label: "新沂市",
value: "新沂市",
leaf: false,
level: 7,
},
{
label: "邳州市",
value: "邳州市",
leaf: false,
level: 7,
}
]
},
{
label: "常州市",
value: "常州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "天宁区",
value: "天宁区",
leaf: false,
level: 7,
},
{
label: "钟楼区",
value: "钟楼区",
leaf: false,
level: 7,
},
{
label: "新北区",
value: "新北区",
leaf: false,
level: 7,
},
{
label: "武进区",
value: "武进区",
leaf: false,
level: 7,
},
{
label: "金坛区",
value: "金坛区",
leaf: false,
level: 7,
},
{
label: "溧阳市",
value: "溧阳市",
leaf: false,
level: 7,
}
]
},
{
label: "无锡市",
value: "无锡市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "梁溪区",
value: "梁溪区",
leaf: false,
level: 7,
},
{
label: "锡山区",
value: "锡山区",
leaf: false,
level: 7,
},
{
label: "惠山区",
value: "惠山区",
leaf: false,
level: 7,
},
{
label: "滨湖区",
value: "滨湖区",
leaf: false,
level: 7,
},
{
label: "新吴区",
value: "新吴区",
leaf: false,
level: 7,
},
{
label: "江阴市",
value: "江阴市",
leaf: false,
level: 7,
},
{
label: "宜兴市",
value: "宜兴市",
leaf: false,
level: 7,
}
]
},
{
label: "苏州市",
value: "苏州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "姑苏区",
value: "姑苏区",
leaf: false,
level: 7,
},
{
label: "虎丘区",
value: "虎丘区",
leaf: false,
level: 7,
},
{
label: "吴中区",
value: "吴中区",
leaf: false,
level: 7,
},
{
label: "相城区",
value: "相城区",
leaf: false,
level: 7,
},
{
label: "吴江区",
value: "吴江区",
leaf: false,
level: 7,
},
{
label: "常熟市",
value: "常熟市",
leaf: false,
level: 7,
},
{
label: "张家港市",
value: "张家港市",
leaf: false,
level: 7,
},
{
label: "昆山市",
value: "昆山市",
leaf: false,
level: 7,
},
{
label: "太仓市",
value: "太仓市",
leaf: false,
level: 7,
}
]
},
{
label: "扬州市",
value: "扬州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "广陵区",
value: "广陵区",
leaf: false,
level: 7,
},
{
label: "邗江区",
value: "邗江区",
leaf: false,
level: 7,
},
{
label: "江都区",
value: "江都区",
leaf: false,
level: 7,
},
{
label: "宝应县",
value: "宝应县",
leaf: false,
level: 7,
},
{
label: "仪征市",
value: "仪征市",
leaf: false,
level: 7,
},
{
label: "高邮市",
value: "高邮市",
leaf: false,
level: 7,
}
]
},
{
label: "泰州市",
value: "泰州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "海陵区",
value: "海陵区",
leaf: false,
level: 7,
},
{
label: "高港区",
value: "高港区",
leaf: false,
level: 7,
},
{
label: "姜堰区",
value: "姜堰区",
leaf: false,
level: 7,
},
{
label: "兴化市",
value: "兴化市",
leaf: false,
level: 7,
},
{
label: "靖江市",
value: "靖江市",
leaf: false,
level: 7,
},
{
label: "泰兴市",
value: "泰兴市",
leaf: false,
level: 7,
}
]
},
{
label: "淮安市",
value: "淮安市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "淮安区",
value: "淮安区",
leaf: false,
level: 7,
},
{
label: "淮阴区",
value: "淮阴区",
leaf: false,
level: 7,
},
{
label: "清江浦区",
value: "清江浦区",
leaf: false,
level: 7,
},
{
label: "洪泽区",
value: "洪泽区",
leaf: false,
level: 7,
},
{
label: "涟水县",
value: "涟水县",
leaf: false,
level: 7,
},
{
label: "盱眙县",
value: "盱眙县",
leaf: false,
level: 7,
},
{
label: "金湖县",
value: "金湖县",
leaf: false,
level: 7,
}
]
},
{
label: "连云港市",
value: "连云港市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "连云区",
value: "连云区",
leaf: false,
level: 7,
},
{
label: "海州区",
value: "海州区",
leaf: false,
level: 7,
},
{
label: "赣榆区",
value: "赣榆区",
leaf: false,
level: 7,
},
{
label: "东海县",
value: "东海县",
leaf: false,
level: 7,
},
{
label: "灌云县",
value: "灌云县",
leaf: false,
level: 7,
},
{
label: "灌南县",
value: "灌南县",
leaf: false,
level: 7,
}
]
},
{
label: "宿迁市",
value: "宿迁市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "宿城区",
value: "宿城区",
leaf: false,
level: 7,
},
{
label: "宿豫区",
value: "宿豫区",
leaf: false,
level: 7,
},
{
label: "沭阳县",
value: "沭阳县",
leaf: false,
level: 7,
},
{
label: "泗阳县",
value: "泗阳县",
leaf: false,
level: 7,
},
{
label: "泗洪县",
value: "泗洪县",
leaf: false,
level: 7,
}
]
}
                  ]

                  },
                  {
                  label: "浙江省",
                  value: "浙江省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "杭州市",
value: "杭州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "上城区",
value: "上城区",
leaf: false,
level: 7,
},
{
label: "拱墅区",
value: "拱墅区",
leaf: false,
level: 7,
},
{
label: "西湖区",
value: "西湖区",
leaf: false,
level: 7,
},
{
label: "滨江区",
value: "滨江区",
leaf: false,
level: 7,
},
{
label: "萧山区",
value: "萧山区",
leaf: false,
level: 7,
},
{
label: "余杭区",
value: "余杭区",
leaf: false,
level: 7,
},
{
label: "富阳区",
value: "富阳区",
leaf: false,
level: 7,
},
{
label: "临安区",
value: "临安区",
leaf: false,
level: 7,
},
{
label: "桐庐县",
value: "桐庐县",
leaf: false,
level: 7,
},
{
label: "淳安县",
value: "淳安县",
leaf: false,
level: 7,
},
{
label: "建德市",
value: "建德市",
leaf: false,
level: 7,
}
]
},
{
label: "宁波市",
value: "宁波市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "海曙区",
value: "海曙区",
leaf: false,
level: 7,
},
{
label: "江北区",
value: "江北区",
leaf: false,
level: 7,
},
{
label: "北仑区",
value: "北仑区",
leaf: false,
level: 7,
},
{
label: "镇海区",
value: "镇海区",
leaf: false,
level: 7,
},
{
label: "鄞州区",
value: "鄞州区",
leaf: false,
level: 7,
},
{
label: "奉化区",
value: "奉化区",
leaf: false,
level: 7,
},
{
label: "象山县",
value: "象山县",
leaf: false,
level: 7,
},
{
label: "宁海县",
value: "宁海县",
leaf: false,
level: 7,
},
{
label: "余姚市",
value: "余姚市",
leaf: false,
level: 7,
},
{
label: "慈溪市",
value: "慈溪市",
leaf: false,
level: 7,
}
]
},
{
label: "温州市",
value: "温州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "鹿城区",
value: "鹿城区",
leaf: false,
level: 7,
},
{
label: "龙湾区",
value: "龙湾区",
leaf: false,
level: 7,
},
{
label: "瓯海区",
value: "瓯海区",
leaf: false,
level: 7,
},
{
label: "洞头区",
value: "洞头区",
leaf: false,
level: 7,
},
{
label: "永嘉县",
value: "永嘉县",
leaf: false,
level: 7,
},
{
label: "平阳县",
value: "平阳县",
leaf: false,
level: 7,
},
{
label: "苍南县",
value: "苍南县",
leaf: false,
level: 7,
},
{
label: "文成县",
value: "文成县",
leaf: false,
level: 7,
},
{
label: "泰顺县",
value: "泰顺县",
leaf: false,
level: 7,
},
{
label: "瑞安市",
value: "瑞安市",
leaf: false,
level: 7,
},
{
label: "乐清市",
value: "乐清市",
leaf: false,
level: 7,
}


]
},
{
label: "嘉兴市",
value: "嘉兴市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "南湖区",
value: "南湖区",
leaf: false,
level: 7,
},
{
label: "秀洲区",
value: "秀洲区",
leaf: false,
level: 7,
},
{
label: "嘉善县",
value: "嘉善县",
leaf: false,
level: 7,
},
{
label: "海盐县",
value: "海盐县",
leaf: false,
level: 7,
},
{
label: "海宁市",
value: "海宁市",
leaf: false,
level: 7,
},
{
label: "平湖市",
value: "平湖市",
leaf: false,
level: 7,
},
{
label: "桐乡市",
value: "桐乡市",
leaf: false,
level: 7,
}
]
},
{
label: "湖州市",
value: "湖州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "吴兴区",
value: "吴兴区",
leaf: false,
level: 7,
},
{
label: "南浔区",
value: "南浔区",
leaf: false,
level: 7,
},
{
label: "德清县",
value: "德清县",
leaf: false,
level: 7,
},
{
label: "长兴县",
value: "长兴县",
leaf: false,
level: 7,
},
{
label: "安吉县",
value: "安吉县",
leaf: false,
level: 7,
}
]
},
{
label: "绍兴市",
value: "绍兴市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "越城区",
value: "越城区",
leaf: false,
level: 7,
},
{
label: "柯桥区",
value: "柯桥区",
leaf: false,
level: 7,
},
{
label: "上虞区",
value: "上虞区",
leaf: false,
level: 7,
},
{
label: "新昌县",
value: "新昌县",
leaf: false,
level: 7,
},
{
label: "诸暨市",
value: "诸暨市",
leaf: false,
level: 7,
},
{
label: "嵊州市",
value: "嵊州市",
leaf: false,
level: 7,
}
]
},
{
label: "金华市",
value: "金华市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "婺城区",
value: "婺城区",
leaf: false,
level: 7,
},
{
label: "金东区",
value: "金东区",
leaf: false,
level: 7,
},
{
label: "武义县",
value: "武义县",
leaf: false,
level: 7,
},
{
label: "浦江县",
value: "浦江县",
leaf: false,
level: 7,
},
{
label: "磐安县",
value: "磐安县",
leaf: false,
level: 7,
},
{
label: "兰溪市",
value: "兰溪市",
leaf: false,
level: 7,
},
{
label: "义乌市",
value: "义乌市",
leaf: false,
level: 7,
},
{
label: "东阳市",
value: "东阳市",
leaf: false,
level: 7,
},
{
label: "永康市",
value: "永康市",
leaf: false,
level: 7,
}

]
},
{
label: "衢州市",
value: "衢州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "柯城区",
value: "柯城区",
leaf: false,
level: 7,
},
{
label: "衢江区",
value: "衢江区",
leaf: false,
level: 7,
},
{
label: "常山县",
value: "常山县",
leaf: false,
level: 7,
},
{
label: "开化县",
value: "开化县",
leaf: false,
level: 7,
},
{
label: "龙游县",
value: "龙游县",
leaf: false,
level: 7,
},
{
label: "江山市",
value: "江山市",
leaf: false,
level: 7,
}

]
},
{
label: "舟山市",
value: "舟山市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "定海区",
value: "定海区",
leaf: false,
level: 7,
},
{
label: "普陀区",
value: "普陀区",
leaf: false,
level: 7,
},
{
label: "岱山县",
value: "岱山县",
leaf: false,
level: 7,
},
{
label: "嵊泗县",
value: "嵊泗县",
leaf: false,
level: 7,
}
]
},
{
label: "台州市",
value: "台州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "椒江区",
value: "椒江区",
leaf: false,
level: 7,
},
{
label: "黄岩区",
value: "黄岩区",
leaf: false,
level: 7,
},
{
label: "路桥区",
value: "路桥区",
leaf: false,
level: 7,
},
{
label: "玉环市",
value: "玉环市",
leaf: false,
level: 7,
},
{
label: "三门县",
value: "三门县",
leaf: false,
level: 7,
},
{
label: "天台县",
value: "天台县",
leaf: false,
level: 7,
},
{
label: "仙居县",
value: "仙居县",
leaf: false,
level: 7,
},
{
label: "温岭市",
value: "温岭市",
leaf: false,
level: 7,
},
{
label: "临海市",
value: "临海市",
leaf: false,
level: 7,
}
]
},
{
label: "丽水市",
value: "丽水市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "莲都区",
value: "莲都区",
leaf: false,
level: 7,
},
{
label: "青田县",
value: "青田县",
leaf: false,
level: 7,
},
{
label: "缙云县",
value: "缙云县",
leaf: false,
level: 7,
},
{
label: "遂昌县",
value: "遂昌县",
leaf: false,
level: 7,
},
{
label: "松阳县",
value: "松阳县",
leaf: false,
level: 7,
},
{
label: "云和县",
value: "云和县",
leaf: false,
level: 7,
},
{
label: "庆元县",
value: "庆元县",
leaf: false,
level: 7,
},
{
label: "景宁畲族自治县",
value: "景宁畲族自治县",
leaf: false,
level: 7,
},
{
label: "龙泉市",
value: "龙泉市",
leaf: false,
level: 7,
}
]
}
                  ]

                  },
                  {
                  label: "安徽省",
                  value: "安徽省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "合肥市",
value: "合肥市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "瑶海区",
value: "瑶海区",
leaf: false,
level: 7,
},
{
label: "庐阳区",
value: "庐阳区",
leaf: false,
level: 7,
},
{
label: "蜀山区",
value: "蜀山区",
leaf: false,
level: 7,
},
{
label: "包河区",
value: "包河区",
leaf: false,
level: 7,
},
{
label: "长丰县",
value: "长丰县",
leaf: false,
level: 7,
},
{
label: "肥东县",
value: "肥东县",
leaf: false,
level: 7,
},
{
label: "肥西县",
value: "肥西县",
leaf: false,
level: 7,
}
]
},
{
label: "芜湖市",
value: "芜湖市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "镜湖区",
value: "镜湖区",
leaf: false,
level: 7,
},
{
label: "弋江区",
value: "弋江区",
leaf: false,
level: 7,
},
{
label: "鸠江区",
value: "鸠江区",
leaf: false,
level: 7,
},
{
label: "湾沚区",
value: "湾沚区",
leaf: false,
level: 7,
},
{
label: "繁昌区",
value: "繁昌区",
leaf: false,
level: 7,
},
{
label: "南陵县",
value: "南陵县",
leaf: false,
level: 7,
}
]
},
{
label: "蚌埠市",
value: "蚌埠市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "龙子湖区",
value: "龙子湖区",
leaf: false,
level: 7,
},
{
label: "蚌山区",
value: "蚌山区",
leaf: false,
level: 7,
},
{
label: "禹会区",
value: "禹会区",
leaf: false,
level: 7,
},
{
label: "淮上区",
value: "淮上区",
leaf: false,
level: 7,
},
{
label: "怀远县",
value: "怀远县",
leaf: false,
level: 7,
},
{
label: "五河县",
value: "五河县",
leaf: false,
level: 7,
},
{
label: "固镇县",
value: "固镇县",
leaf: false,
level: 7,
}

]
},
{
label: "淮南市",
value: "淮南市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "大通区",
value: "大通区",
leaf: false,
level: 7,
},
{
label: "田家庵区",
value: "田家庵区",
leaf: false,
level: 7,
},
{
label: "谢家集区",
value: "谢家集区",
leaf: false,
level: 7,
},
{
label: "八公山区",
value: "八公山区",
leaf: false,
level: 7,
},
{
label: "潘集区",
value: "潘集区",
leaf: false,
level: 7,
},
{
label: "凤台县",
value: "凤台县",
leaf: false,
level: 7,
}
]
},
{
label: "马鞍山市",
value: "马鞍山市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "花山区",
value: "花山区",
leaf: false,
level: 7,
},
{
label: "雨山区",
value: "雨山区",
leaf: false,
level: 7,
},
{
label: "博望区",
value: "博望区",
leaf: false,
level: 7,
},
{
label: "当涂县",
value: "当涂县",
leaf: false,
level: 7,
},
{
label: "和县",
value: "和县",
leaf: false,
level: 7,
}
]
},
{
label: "淮北市",
value: "淮北市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "杜集区",
value: "杜集区",
leaf: false,
level: 7,
},
{
label: "相山区",
value: "相山区",
leaf: false,
level: 7,
},
{
label: "烈山区",
value: "烈山区",
leaf: false,
level: 7,
},
{
label: "濉溪县",
value: "濉溪县",
leaf: false,
level: 7,
}
]
},
{
label: "铜陵市",
value: "铜陵市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "铜官区",
value: "铜官区",
leaf: false,
level: 7,
},
{
label: "郊区",
value: "郊区",
leaf: false,
level: 7,
},
{
label: "义安区",
value: "义安区",
leaf: false,
level: 7,
},
{
label: "枞阳县",
value: "枞阳县",
leaf: false,
level: 7,
}
]
},
{
label: "安庆市",
value: "安庆市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "迎江区",
value: "迎江区",
leaf: false,
level: 7,
},
{
label: "大观区",
value: "大观区",
leaf: false,
level: 7,
},
{
label: "宜秀区",
value: "宜秀区",
leaf: false,
level: 7,
},
{
label: "怀宁县",
value: "怀宁县",
leaf: false,
level: 7,
},
{
label: "潜山市",
value: "潜山市",
leaf: false,
level: 7,
},
{
label: "太湖县",
value: "太湖县",
leaf: false,
level: 7,
},
{
label: "宿松县",
value: "宿松县",
leaf: false,
level: 7,
},
{
label: "望江县",
value: "望江县",
leaf: false,
level: 7,
},
{
label: "岳西县",
value: "岳西县",
leaf: false,
level: 7,
}
]
},
{
label: "黄山市",
value: "黄山市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "屯溪区",
value: "屯溪区",
leaf: false,
level: 7,
},
{
label: "黄山区",
value: "黄山区",
leaf: false,
level: 7,
},
{
label: "徽州区",
value: "徽州区",
leaf: false,
level: 7,
},
{
label: "歙县",
value: "歙县",
leaf: false,
level: 7,
},
{
label: "休宁县",
value: "休宁县",
leaf: false,
level: 7,
},
{
label: "黟县",
value: "黟县",
leaf: false,
level: 7,
},
{
label: "祁门县",
value: "祁门县",
leaf: false,
level: 7,
}

]
},
{
label: "滁州市",
value: "滁州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "琅琊区",
value: "琅琊区",
leaf: false,
level: 7,
},
{
label: "南谯区",
value: "南谯区",
leaf: false,
level: 7,
},
{
label: "来安县",
value: "来安县",
leaf: false,
level: 7,
},
{
label: "全椒县",
value: "全椒县",
leaf: false,
level: 7,
},
{
label: "定远县",
value: "定远县",
leaf: false,
level: 7,
},
{
label: "凤阳县",
value: "凤阳县",
leaf: false,
level: 7,
},
{
label: "天长市",
value: "天长市",
leaf: false,
level: 7,
},
{
label: "明光市",
value: "明光市",
leaf: false,
level: 7,
}

]
},
{
label: "阜阳市",
value: "阜阳市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "颍州区",
value: "颍州区",
leaf: false,
level: 7,
},
{
label: "颍东区",
value: "颍东区",
leaf: false,
level: 7,
},
{
label: "颍泉区",
value: "颍泉区",
leaf: false,
level: 7,
},
{
label: "颍上县",
value: "颍上县",
leaf: false,
level: 7,
},
{
label: "界首市",
value: "界首市",
leaf: false,
level: 7,
},
{
label: "临泉县",
value: "临泉县",
leaf: false,
level: 7,
},
{
label: "太和县",
value: "太和县",
leaf: false,
level: 7,
},
{
label: "阜南县",
value: "阜南县",
leaf: false,
level: 7,
}

]
},
{
label: "宿州市",
value: "宿州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "埇桥区",
value: "埇桥区",
leaf: false,
level: 7,
},
{
label: "砀山县",
value: "砀山县",
leaf: false,
level: 7,
},
{
label: "萧县",
value: "萧县",
leaf: false,
level: 7,
},
{
label: "灵璧县",
value: "灵璧县",
leaf: false,
level: 7,
},
{
label: "泗县",
value: "泗县",
leaf: false,
level: 7,
}

]
},
{
label: "六安市",
value: "六安市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
]
},
{
label: "亳州市",
value: "亳州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "谯城区",
value: "谯城区",
leaf: false,
level: 7,
},
{
label: "涡阳县",
value: "涡阳县",
leaf: false,
level: 7,
},
{
label: "蒙城县",
value: "蒙城县",
leaf: false,
level: 7,
},
{
label: "利辛县",
value: "利辛县",
leaf: false,
level: 7,
}
]
},
{
label: "池州市",
value: "池州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "贵池区",
value: "贵池区",
leaf: false,
level: 7,
},
{
label: "东至县",
value: "东至县",
leaf: false,
level: 7,
},
{
label: "石台县",
value: "石台县",
leaf: false,
level: 7,
},
{
label: "青阳县",
value: "青阳县",
leaf: false,
level: 7,
}
]
},
{
label: "宣城市",
value: "宣城市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "宣州区",
value: "宣州区",
leaf: false,
level: 7,
},
{
label: "郎溪县",
value: "郎溪县",
leaf: false,
level: 7,
},
{
label: "广德市",
value: "广德市",
leaf: false,
level: 7,
},
{
label: "泾县",
value: "泾县",
leaf: false,
level: 7,
},
{
label: "绩溪县",
value: "绩溪县",
leaf: false,
level: 7,
},
{
label: "旌德县",
value: "旌德县",
leaf: false,
level: 7,
},
{
label: "宁国市",
value: "宁国市",
leaf: false,
level: 7,
}
]
}
                  ]

                  },
                  {
                  label: "福建省",
                  value: "福建省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "福州市",
value: "福州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "鼓楼区",
value: "鼓楼区",
leaf: false,
level: 7,
},
{
label: "台江区",
value: "台江区",
leaf: false,
level: 7,
},
{
label: "仓山区",
value: "仓山区",
leaf: false,
level: 7,
},
{
label: "晋安区",
value: "晋安区",
leaf: false,
level: 7,
},
{
label: "马尾区",
value: "马尾区",
leaf: false,
level: 7,
},
{
label: "长乐区",
value: "长乐区",
leaf: false,
level: 7,
},
{
label: "闽侯县",
value: "闽侯县",
leaf: false,
level: 7,
},
{
label: "连江县",
value: "连江县",
leaf: false,
level: 7,
},
{
label: "罗源县",
value: "罗源县",
leaf: false,
level: 7,
},
{
label: "闽清县",
value: "闽清县",
leaf: false,
level: 7,
},
{
label: "永泰县",
value: "永泰县",
leaf: false,
level: 7,
},
{
label: "平潭县",
value: "平潭县",
leaf: false,
level: 7,
}
]
},
{
label: "厦门市",
value: "厦门市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "思明区",
value: "思明区",
leaf: false,
level: 7,
},
{
label: "湖里区",
value: "湖里区",
leaf: false,
level: 7,
},
{
label: "集美区",
value: "集美区",
leaf: false,
level: 7,
},
{
label: "海沧区",
value: "海沧区",
leaf: false,
level: 7,
},
{
label: "同安区",
value: "同安区",
leaf: false,
level: 7,
},
{
label: "翔安区",
value: "翔安区",
leaf: false,
level: 7,
}
]
},
{
label: "莆田市",
value: "莆田市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "城厢区",
value: "城厢区",
leaf: false,
level: 7,
},
{
label: "涵江区",
value: "涵江区",
leaf: false,
level: 7,
},
{
label: "荔城区",
value: "荔城区",
leaf: false,
level: 7,
},
{
label: "秀屿区",
value: "秀屿区",
leaf: false,
level: 7,
},
{
label: "仙游县",
value: "仙游县",
leaf: false,
level: 7,
}
]
},
{
label: "三明市",
value: "三明市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "三元区",
value: "三元区",
leaf: false,
level: 7,
},
{
label: "明溪县",
value: "明溪县",
leaf: false,
level: 7,
},
{
label: "清流县",
value: "清流县",
leaf: false,
level: 7,
},
{
label: "宁化县",
value: "宁化县",
leaf: false,
level: 7,
},
{
label: "大田县",
value: "大田县",
leaf: false,
level: 7,
},
{
label: "尤溪县",
value: "尤溪县",
leaf: false,
level: 7,
},
{
label: "沙县区",
value: "沙县区",
leaf: false,
level: 7,
},
{
label: "将乐县",
value: "将乐县",
leaf: false,
level: 7,
},
{
label: "泰宁县",
value: "泰宁县",
leaf: false,
level: 7,
},
{
label: "建宁县",
value: "建宁县",
leaf: false,
level: 7,
},
{
label: "永安市",
value: "永安市",
leaf: false,
level: 7,
}
]
},
{
label: "泉州市",
value: "泉州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "鲤城区",
value: "鲤城区",
leaf: false,
level: 7,
},
{
label: "丰泽区",
value: "丰泽区",
leaf: false,
level: 7,
},
{
label: "洛江区",
value: "洛江区",
leaf: false,
level: 7,
},
{
label: "泉港区",
value: "泉港区",
leaf: false,
level: 7,
},
{
label: "惠安县",
value: "惠安县",
leaf: false,
level: 7,
},
{
label: "安溪县",
value: "安溪县",
leaf: false,
level: 7,
},
{
label: "永春县",
value: "永春县",
leaf: false,
level: 7,
},
{
label: "德化县",
value: "德化县",
leaf: false,
level: 7,
},
{
label: "金门县",
value: "金门县",
leaf: false,
level: 7,
},
{
label: "石狮市",
value: "石狮市",
leaf: false,
level: 7,
},
{
label: "晋江市",
value: "晋江市",
leaf: false,
level: 7,
},
{
label: "南安市",
value: "南安市",
leaf: false,
level: 7,
}
]
},
{
label: "漳州市",
value: "漳州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "芗城区",
value: "芗城区",
leaf: false,
level: 7,
},
{
label: "龙文区",
value: "龙文区",
leaf: false,
level: 7,
},
{
label: "龙海区",
value: "龙海区",
leaf: false,
level: 7,
},
{
label: "长泰区",
value: "长泰区",
leaf: false,
level: 7,
},
{
label: "云霄县",
value: "云霄县",
leaf: false,
level: 7,
},
{
label: "漳浦县",
value: "漳浦县",
leaf: false,
level: 7,
},
{
label: "诏安县",
value: "诏安县",
leaf: false,
level: 7,
},
{
label: "东山县",
value: "东山县",
leaf: false,
level: 7,
},
{
label: "南靖县",
value: "南靖县",
leaf: false,
level: 7,
},
{
label: "平和县",
value: "平和县",
leaf: false,
level: 7,
},
{
label: "华安县",
value: "华安县",
leaf: false,
level: 7,
}
]
},
{
label: "南平市",
value: "南平市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "延平区",
value: "延平区",
leaf: false,
level: 7,
},
{
label: "建阳区",
value: "建阳区",
leaf: false,
level: 7,
},
{
label: "顺昌县",
value: "顺昌县",
leaf: false,
level: 7,
},
{
label: "浦城县",
value: "浦城县",
leaf: false,
level: 7,
},
{
label: "光泽县",
value: "光泽县",
leaf: false,
level: 7,
},
{
label: "松溪县",
value: "松溪县",
leaf: false,
level: 7,
},
{
label: "政和县",
value: "政和县",
leaf: false,
level: 7,
},
{
label: "邵武市",
value: "邵武市",
leaf: false,
level: 7,
},
{
label: "武夷山市",
value: "武夷山市",
leaf: false,
level: 7,
},
{
label: "建瓯市",
value: "建瓯市",
leaf: false,
level: 7,
}
]
},
{
label: "龙岩市",
value: "龙岩市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "新罗区",
value: "新罗区",
leaf: false,
level: 7,
},
{
label: "永定区",
value: "永定区",
leaf: false,
level: 7,
},
{
label: "长汀县",
value: "长汀县",
leaf: false,
level: 7,
},
{
label: "上杭县",
value: "上杭县",
leaf: false,
level: 7,
},
{
label: "武平县",
value: "武平县",
leaf: false,
level: 7,
},
{
label: "连城县",
value: "连城县",
leaf: false,
level: 7,
},
{
label: "漳平市",
value: "漳平市",
leaf: false,
level: 7,
}
]
},
{
label: "宁德市",
value: "宁德市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "蕉城区",
value: "蕉城区",
leaf: false,
level: 7,
},
{
label: "古田县",
value: "古田县",
leaf: false,
level: 7,
},
{
label: "屏南县",
value: "屏南县",
leaf: false,
level: 7,
},
{
label: "周宁县",
value: "周宁县",
leaf: false,
level: 7,
},
{
label: "寿宁县",
value: "寿宁县",
leaf: false,
level: 7,
},
{
label: "福安市",
value: "福安市",
leaf: false,
level: 7,
},
{
label: "柘荣县",
value: "柘荣县",
leaf: false,
level: 7,
},
{
label: "福鼎市",
value: "福鼎市",
leaf: false,
level: 7,
},
{
label: "霞浦县",
value: "霞浦县",
leaf: false,
level: 7,
}
]
}
                  ]

                  },


                ],
              },
              {
                label: "华北",
                value: "华北",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "省级",
                    value: "省级",
                    leaf: true,
                    level: 5,

                  },
                  {
                  label: "北京市",
                  value: "北京市",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"县级",
                      value: "县级",
                      leaf: false,
                      level: 7,
                    },
                    {
label: "东城区",
value: "东城区",
leaf: false,
level: 7,
},
{
label: "西城区",
value: "西城区",
leaf: false,
level: 7,
},
{
label: "朝阳区",
value: "朝阳区",
leaf: false,
level: 7,
},
{
label: "丰台区",
value: "丰台区",
leaf: false,
level: 7,
},
{
label: "石景山区",
value: "石景山区",
leaf: false,
level: 7,
},
{
label: "海淀区",
value: "海淀区",
leaf: false,
level: 7,
},
{
label: "顺义区",
value: "顺义区",
leaf: false,
level: 7,
},
{
label: "通州区",
value: "通州区",
leaf: false,
level: 7,
},
{
label: "大兴区",
value: "大兴区",
leaf: false,
level: 7,
},
{
label: "房山区",
value: "房山区",
leaf: false,
level: 7,
},
{
label: "门头沟区",
value: "门头沟区",
leaf: false,
level: 7,
},
{
label: "昌平区",
value: "昌平区",
leaf: false,
level: 7,
},
{
label: "平谷区",
value: "平谷区",
leaf: false,
level: 7,
},
{
label: "密云区",
value: "密云区",
leaf: false,
level: 7,
},
{
label: "怀柔区",
value: "怀柔区",
leaf: false,
level: 7,
},
{
label: "延庆区",
value: "延庆区",
leaf: false,
level: 7,
},
                  ]

                  },
                  {
                  label: "天津市",
                  value: "天津市",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"县级",
                      value: "县级",
                      leaf: false,
                      level: 7,
                    },
                    {
label: "和平区",
value: "和平区",
leaf: false,
level: 7,
},
{
label: "河东区",
value: "河东区",
leaf: false,
level: 7,
},
{
label: "河西区",
value: "河西区",
leaf: false,
level: 7,
},
{
label: "南开区",
value: "南开区",
leaf: false,
level: 7,
},
{
label: "河北区",
value: "河北区",
leaf: false,
level: 7,
},
{
label: "红桥区",
value: "红桥区",
leaf: false,
level: 7,
},
{
label: "东丽区",
value: "东丽区",
leaf: false,
level: 7,
},
{
label: "西青区",
value: "西青区",
leaf: false,
level: 7,
},
{
label: "津南区",
value: "津南区",
leaf: false,
level: 7,
},
{
label: "北辰区",
value: "北辰区",
leaf: false,
level: 7,
},
{
label: "武清区",
value: "武清区",
leaf: false,
level: 7,
},
{
label: "宝坻区",
value: "宝坻区",
leaf: false,
level: 7,
},
{
label: "滨海新区",
value: "滨海新区",
leaf: false,
level: 7,
},
{
label: "宁河区",
value: "宁河区",
leaf: false,
level: 7,
},
{
label: "静海区",
value: "静海区",
leaf: false,
level: 7,
},
{
label: "蓟州区",
value: "蓟州区",
leaf: false,
level: 7,
},
                  ]

                  },
                  {
                  label: "河北省",
                  value: "河北省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "石家庄市",
value: "石家庄市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "桥西区",
value: "桥西区",
leaf: false,
level: 7,
},
{
label: "新华区",
value: "新华区",
leaf: false,
level: 7,
},
{
label: "井陉矿区",
value: "井陉矿区",
leaf: false,
level: 7,
},
{
label: "裕华区",
value: "裕华区",
leaf: false,
level: 7,
},
{
label: "藁城区",
value: "藁城区",
leaf: false,
level: 7,
},
{
label: "鹿泉区",
value: "鹿泉区",
leaf: false,
level: 7,
},
{
label: "栾城区",
value: "栾城区",
leaf: false,
level: 7,
},
{
label: "井陉县",
value: "井陉县",
leaf: false,
level: 7,
},
{
label: "正定县",
value: "正定县",
leaf: false,
level: 7,
},
{
label: "行唐县",
value: "行唐县",
leaf: false,
level: 7,
},
{
label: "灵寿县",
value: "灵寿县",
leaf: false,
level: 7,
},
{
label: "高邑县",
value: "高邑县",
leaf: false,
level: 7,
},
{
label: "深泽县",
value: "深泽县",
leaf: false,
level: 7,
},
{
label: "赞皇县",
value: "赞皇县",
leaf: false,
level: 7,
},
{
label: "无极县",
value: "无极县",
leaf: false,
level: 7,
},
{
label: "平山县",
value: "平山县",
leaf: false,
level: 7,
},
{
label: "元氏县",
value: "元氏县",
leaf: false,
level: 7,
},
{
label: "赵县",
value: "赵县",
leaf: false,
level: 7,
},
{
label: "晋州市",
value: "晋州市",
leaf: false,
level: 7,
},
{
label: "新乐市",
value: "新乐市",
leaf: false,
level: 7,
},
{
label: "辛集市",
value: "辛集市",
leaf: false,
level: 7,
},
]
},
{
label: "唐山市",
value: "唐山市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "路南区",
value: "路南区",
leaf: false,
level: 7,
},
{
label: "路北区",
value: "路北区",
leaf: false,
level: 7,
},
{
label: "古冶区",
value: "古冶区",
leaf: false,
level: 7,
},
{
label: "开平区",
value: "开平区",
leaf: false,
level: 7,
},
{
label: "丰南区",
value: "丰南区",
leaf: false,
level: 7,
},
{
label: "丰润区",
value: "丰润区",
leaf: false,
level: 7,
},
{
label: "曹妃甸区",
value: "曹妃甸区",
leaf: false,
level: 7,
},
{
label: "滦州市",
value: "滦州市",
leaf: false,
level: 7,
},
{
label: "遵化市",
value: "遵化市",
leaf: false,
level: 7,
},
{
label: "迁安市",
value: "迁安市",
leaf: false,
level: 7,
},
{
label: "滦南县",
value: "滦南县",
leaf: false,
level: 7,
},
{
label: "乐亭县",
value: "乐亭县",
leaf: false,
level: 7,
},
{
label: "迁西县",
value: "迁西县",
leaf: false,
level: 7,
},
{
label: "玉田县",
value: "玉田县",
leaf: false,
level: 7,
},
]
},
{
label: "秦皇岛市",
value: "秦皇岛市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "海港区",
value: "海港区",
leaf: false,
level: 7,
},
{
label: "山海关区",
value: "山海关区",
leaf: false,
level: 7,
},
{
label: "北戴河区",
value: "北戴河区",
leaf: false,
level: 7,
},
{
label: "抚宁区",
value: "抚宁区",
leaf: false,
level: 7,
},
{
label: "青龙满族自治县",
value: "青龙满族自治县",
leaf: false,
level: 7,
},
{
label: "昌黎县",
value: "昌黎县",
leaf: false,
level: 7,
},
{
label: "卢龙县",
value: "卢龙县",
leaf: false,
level: 7,
},
]
},
{
label: "邯郸市",
value: "邯郸市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "邯山区",
value: "邯山区",
leaf: false,
level: 7,
},
{
label: "丛台区",
value: "丛台区",
leaf: false,
level: 7,
},
{
label: "复兴区",
value: "复兴区",
leaf: false,
level: 7,
},
{
label: "峰峰矿区",
value: "峰峰矿区",
leaf: false,
level: 7,
},
{
label: "肥乡区",
value: "肥乡区",
leaf: false,
level: 7,
},
{
label: "永年区",
value: "永年区",
leaf: false,
level: 7,
},
{
label: "临漳县",
value: "临漳县",
leaf: false,
level: 7,
},
{
label: "成安县",
value: "成安县",
leaf: false,
level: 7,
},
{
label: "大名县",
value: "大名县",
leaf: false,
level: 7,
},
{
label: "涉县",
value: "涉县",
leaf: false,
level: 7,
},
{
label: "磁县",
value: "磁县",
leaf: false,
level: 7,
},
{
label: "邱县",
value: "邱县",
leaf: false,
level: 7,
},
{
label: "鸡泽县",
value: "鸡泽县",
leaf: false,
level: 7,
},
{
label: "广平县",
value: "广平县",
leaf: false,
level: 7,
},
{
label: "馆陶县",
value: "馆陶县",
leaf: false,
level: 7,
},
{
label: "魏县",
value: "魏县",
leaf: false,
level: 7,
},
{
label: "曲周县",
value: "曲周县",
leaf: false,
level: 7,
},
{
label: "武安市",
value: "武安市",
leaf: false,
level: 7,
},
]
},
{
label: "邢台市",
value: "邢台市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "襄都区",
value: "襄都区",
leaf: false,
level: 7,
},
{
label: "信都区",
value: "信都区",
leaf: false,
level: 7,
},
{
label: "任泽区",
value: "任泽区",
leaf: false,
level: 7,
},
{
label: "南和区",
value: "南和区",
leaf: false,
level: 7,
},
{
label: "临城县",
value: "临城县",
leaf: false,
level: 7,
},
{
label: "内丘县",
value: "内丘县",
leaf: false,
level: 7,
},
{
label: "柏乡县",
value: "柏乡县",
leaf: false,
level: 7,
},
{
label: "隆尧县",
value: "隆尧县",
leaf: false,
level: 7,
},
{
label: "宁晋县",
value: "宁晋县",
leaf: false,
level: 7,
},
{
label: "巨鹿县",
value: "巨鹿县",
leaf: false,
level: 7,
},
{
label: "新河县",
value: "新河县",
leaf: false,
level: 7,
},
{
label: "广宗县",
value: "广宗县",
leaf: false,
level: 7,
},
{
label: "平乡县",
value: "平乡县",
leaf: false,
level: 7,
},
{
label: "威县",
value: "威县",
leaf: false,
level: 7,
},
{
label: "清河县",
value: "清河县",
leaf: false,
level: 7,
},
{
label: "临西县",
value: "临西县",
leaf: false,
level: 7,
},
{
label: "南宫市",
value: "南宫市",
leaf: false,
level: 7,
},
{
label: "沙河市",
value: "沙河市",
leaf: false,
level: 7,
},
]
},
{
label: "保定市",
value: "保定市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "竞秀区",
value: "竞秀区",
leaf: false,
level: 7,
},
{
label: "莲池区",
value: "莲池区",
leaf: false,
level: 7,
},
{
label: "满城区",
value: "满城区",
leaf: false,
level: 7,
},
{
label: "清苑区",
value: "清苑区",
leaf: false,
level: 7,
},
{
label: "徐水区",
value: "徐水区",
leaf: false,
level: 7,
},
{
label: "涞水县",
value: "涞水县",
leaf: false,
level: 7,
},
{
label: "阜平县",
value: "阜平县",
leaf: false,
level: 7,
},
{
label: "定兴县",
value: "定兴县",
leaf: false,
level: 7,
},
{
label: "唐县",
value: "唐县",
leaf: false,
level: 7,
},
{
label: "高阳县",
value: "高阳县",
leaf: false,
level: 7,
},
{
label: "容城县",
value: "容城县",
leaf: false,
level: 7,
},
{
label: "涞源县",
value: "涞源县",
leaf: false,
level: 7,
},
{
label: "望都县",
value: "望都县",
leaf: false,
level: 7,
},
{
label: "安新县",
value: "安新县",
leaf: false,
level: 7,
},
{
label: "易县",
value: "易县",
leaf: false,
level: 7,
},
{
label: "曲阳县",
value: "曲阳县",
leaf: false,
level: 7,
},
{
label: "蠡县",
value: "蠡县",
leaf: false,
level: 7,
},
{
label: "顺平县",
value: "顺平县",
leaf: false,
level: 7,
},
{
label: "博野县",
value: "博野县",
leaf: false,
level: 7,
},
{
label: "雄县",
value: "雄县",
leaf: false,
level: 7,
},
{
label: "涿州市",
value: "涿州市",
leaf: false,
level: 7,
},
{
label: "安国市",
value: "安国市",
leaf: false,
level: 7,
},
{
label: "高碑店市",
value: "高碑店市",
leaf: false,
level: 7,
}
]
},
{
label: "张家口市",
value: "张家口市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "桥东区",
value: "桥东区",
leaf: false,
level: 7,
},
{
label: "桥西区",
value: "桥西区",
leaf: false,
level: 7,
},
{
label: "宣化区",
value: "宣化区",
leaf: false,
level: 7,
},
{
label: "下花园区",
value: "下花园区",
leaf: false,
level: 7,
},
{
label: "万全区",
value: "万全区",
leaf: false,
level: 7,
},
{
label: "崇礼区",
value: "崇礼区",
leaf: false,
level: 7,
},
{
label: "张北县",
value: "张北县",
leaf: false,
level: 7,
},
{
label: "康保县",
value: "康保县",
leaf: false,
level: 7,
},
{
label: "沽源县",
value: "沽源县",
leaf: false,
level: 7,
},
{
label: "尚义县",
value: "尚义县",
leaf: false,
level: 7,
},
{
label: "蔚县",
value: "蔚县",
leaf: false,
level: 7,
},
{
label: "阳原县",
value: "阳原县",
leaf: false,
level: 7,
},
{
label: "怀安县",
value: "怀安县",
leaf: false,
level: 7,
},
{
label: "怀来县",
value: "怀来县",
leaf: false,
level: 7,
},
{
label: "涿鹿县",
value: "涿鹿县",
leaf: false,
level: 7,
},
{
label: "赤城县",
value: "赤城县",
leaf: false,
level: 7,
},
]
},
{
label: "承德市",
value: "承德市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "双桥区",
value: "双桥区",
leaf: false,
level: 7,
},
{
label: "双滦区",
value: "双滦区",
leaf: false,
level: 7,
},
{
label: "鹰手营子矿区",
value: "鹰手营子矿区",
leaf: false,
level: 7,
},
{
label: "承德县",
value: "承德县",
leaf: false,
level: 7,
},
{
label: "兴隆县",
value: "兴隆县",
leaf: false,
level: 7,
},
{
label: "平泉市",
value: "平泉市",
leaf: false,
level: 7,
},
{
label: "滦平县",
value: "滦平县",
leaf: false,
level: 7,
},
{
label: "隆化县",
value: "隆化县",
leaf: false,
level: 7,
},
{
label: "丰宁满族自治县",
value: "丰宁满族自治县",
leaf: false,
level: 7,
},
{
label: "宽城满族自治县",
value: "宽城满族自治县",
leaf: false,
level: 7,
},
{
label: "围场满族蒙古族自治县",
value: "围场满族蒙古族自治县",
leaf: false,
level: 7,
},
]
},
{
label: "沧州市",
value: "沧州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "新华区",
value: "新华区",
leaf: false,
level: 7,
},
{
label: "运河区",
value: "运河区",
leaf: false,
level: 7,
},
{
label: "沧县",
value: "沧县",
leaf: false,
level: 7,
},
{
label: "青县",
value: "青县",
leaf: false,
level: 7,
},
{
label: "东光县",
value: "东光县",
leaf: false,
level: 7,
},
{
label: "海兴县",
value: "海兴县",
leaf: false,
level: 7,
},
{
label: "盐山县",
value: "盐山县",
leaf: false,
level: 7,
},
{
label: "肃宁县",
value: "肃宁县",
leaf: false,
level: 7,
},
{
label: "南皮县",
value: "南皮县",
leaf: false,
level: 7,
},
{
label: "吴桥县",
value: "吴桥县",
leaf: false,
level: 7,
},
{
label: "献县",
value: "献县",
leaf: false,
level: 7,
},
{
label: "孟村回族自治县",
value: "孟村回族自治县",
leaf: false,
level: 7,
},
{
label: "泊头市",
value: "泊头市",
leaf: false,
level: 7,
},
{
label: "任丘市",
value: "任丘市",
leaf: false,
level: 7,
},
{
label: "黄骅市",
value: "黄骅市",
leaf: false,
level: 7,
},
{
label: "河间市",
value: "河间市",
leaf: false,
level: 7,
}
]
},
{
label: "廊坊市",
value: "廊坊市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "安次区",
value: "安次区",
leaf: false,
level: 7,
},
{
label: "广阳区",
value: "广阳区",
leaf: false,
level: 7,
},
{
label: "固安县",
value: "固安县",
leaf: false,
level: 7,
},
{
label: "永清县",
value: "永清县",
leaf: false,
level: 7,
},
{
label: "香河县",
value: "香河县",
leaf: false,
level: 7,
},
{
label: "大城县",
value: "大城县",
leaf: false,
level: 7,
},
{
label: "文安县",
value: "文安县",
leaf: false,
level: 7,
},
{
label: "大厂回族自治县",
value: "大厂回族自治县",
leaf: false,
level: 7,
},
{
label: "霸州市",
value: "霸州市",
leaf: false,
level: 7,
},
{
label: "三河市",
value: "三河市",
leaf: false,
level: 7,
},
]
},
{
label: "衡水市",
value: "衡水市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "桃城区",
value: "桃城区",
leaf: false,
level: 7,
},
{
label: "冀州区",
value: "冀州区",
leaf: false,
level: 7,
},
{
label: "枣强县",
value: "枣强县",
leaf: false,
level: 7,
},
{
label: "武邑县",
value: "武邑县",
leaf: false,
level: 7,
},
{
label: "武强县",
value: "武强县",
leaf: false,
level: 7,
},
{
label: "饶阳县",
value: "饶阳县",
leaf: false,
level: 7,
},
{
label: "安平县",
value: "安平县",
leaf: false,
level: 7,
},
{
label: "故城县",
value: "故城县",
leaf: false,
level: 7,
},
{
label: "景县",
value: "景县",
leaf: false,
level: 7,
},
{
label: "阜城县",
value: "阜城县",
leaf: false,
level: 7,
}
]
}
                  ]

                  },
                  {
                  label: "山西省",
                  value: "山西省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "太原市",
value: "太原市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "小店区",
value: "小店区",
leaf: false,
level: 7,
},
{
label: "迎泽区",
value: "迎泽区",
leaf: false,
level: 7,
},
{
label: "杏花岭区",
value: "杏花岭区",
leaf: false,
level: 7,
},
{
label: "尖草坪区",
value: "尖草坪区",
leaf: false,
level: 7,
},
{
label: "万柏林区",
value: "万柏林区",
leaf: false,
level: 7,
},
{
label: "晋源区",
value: "晋源区",
leaf: false,
level: 7,
},
{
label: "阳曲县",
value: "阳曲县",
leaf: false,
level: 7,
},
{
label: "娄烦县",
value: "娄烦县",
leaf: false,
level: 7,
},
{
label: "古交市",
value: "古交市",
leaf: false,
level: 7,
}
]
},
{
label: "大同市",
value: "大同市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "平城区",
value: "平城区",
leaf: false,
level: 7,
},
{
label: "云冈区",
value: "云冈区",
leaf: false,
level: 7,
},
{
label: "新荣区",
value: "新荣区",
leaf: false,
level: 7,
},
{
label: "云州区",
value: "云州区",
leaf: false,
level: 7,
},
{
label: "阳高县",
value: "阳高县",
leaf: false,
level: 7,
},
{
label: "天镇县",
value: "天镇县",
leaf: false,
level: 7,
},
{
label: "广灵县",
value: "广灵县",
leaf: false,
level: 7,
},
{
label: "灵丘县",
value: "灵丘县",
leaf: false,
level: 7,
},
{
label: "浑源县",
value: "浑源县",
leaf: false,
level: 7,
},
{
label: "左云县",
value: "左云县",
leaf: false,
level: 7,
}
]
},
{
label: "阳泉市",
value: "阳泉市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "城区",
value: "城区",
leaf: false,
level: 7,
},
{
label: "矿区",
value: "矿区",
leaf: false,
level: 7,
},
{
label: "郊区",
value: "郊区",
leaf: false,
level: 7,
},
{
label: "平定县",
value: "平定县",
leaf: false,
level: 7,
},
{
label: "盂县",
value: "盂县",
leaf: false,
level: 7,
}
]
},
{
label: "长治市",
value: "长治市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "潞州区",
value: "潞州区",
leaf: false,
level: 7,
},
{
label: "上党区",
value: "上党区",
leaf: false,
level: 7,
},
{
label: "屯留区",
value: "屯留区",
leaf: false,
level: 7,
},
{
label: "潞城区",
value: "潞城区",
leaf: false,
level: 7,
},
{
label: "襄垣县",
value: "襄垣县",
leaf: false,
level: 7,
},
{
label: "平顺县",
value: "平顺县",
leaf: false,
level: 7,
},
{
label: "黎城县",
value: "黎城县",
leaf: false,
level: 7,
},
{
label: "壶关县",
value: "壶关县",
leaf: false,
level: 7,
},
{
label: "长子县",
value: "长子县",
leaf: false,
level: 7,
},
{
label: "武乡县",
value: "武乡县",
leaf: false,
level: 7,
},
{
label: "沁县",
value: "沁县",
leaf: false,
level: 7,
},
{
label: "沁源县",
value: "沁源县",
leaf: false,
level: 7,
}
]
},
{
label: "晋城市",
value: "晋城市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "城区",
value: "城区",
leaf: false,
level: 7,
},
{
label: "沁水县",
value: "沁水县",
leaf: false,
level: 7,
},
{
label: "阳城县",
value: "阳城县",
leaf: false,
level: 7,
},
{
label: "陵川县",
value: "陵川县",
leaf: false,
level: 7,
},
{
label: "泽州县",
value: "泽州县",
leaf: false,
level: 7,
},
{
label: "高平市",
value: "高平市",
leaf: false,
level: 7,
}
]
},
{
label: "朔州市",
value: "朔州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "朔城区",
value: "朔城区",
leaf: false,
level: 7,
},
{
label: "平鲁区",
value: "平鲁区",
leaf: false,
level: 7,
},
{
label: "山阴县",
value: "山阴县",
leaf: false,
level: 7,
},
{
label: "应县",
value: "应县",
leaf: false,
level: 7,
},
{
label: "右玉县",
value: "右玉县",
leaf: false,
level: 7,
},
{
label: "怀仁市",
value: "怀仁市",
leaf: false,
level: 7,
}
]
},
{
label: "晋中市",
value: "晋中市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "榆次区",
value: "榆次区",
leaf: false,
level: 7,
},
{
label: "太谷区",
value: "太谷区",
leaf: false,
level: 7,
},
{
label: "榆社县",
value: "榆社县",
leaf: false,
level: 7,
},
{
label: "左权县",
value: "左权县",
leaf: false,
level: 7,
},
{
label: "和顺县",
value: "和顺县",
leaf: false,
level: 7,
},
{
label: "昔阳县",
value: "昔阳县",
leaf: false,
level: 7,
},
{
label: "寿阳县",
value: "寿阳县",
leaf: false,
level: 7,
},
{
label: "祁县",
value: "祁县",
leaf: false,
level: 7,
},
{
label: "平遥县",
value: "平遥县",
leaf: false,
level: 7,
},
{
label: "灵石县",
value: "灵石县",
leaf: false,
level: 7,
},
{
label: "介休市",
value: "介休市",
leaf: false,
level: 7,
}
]
},
{
label: "运城市",
value: "运城市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "盐湖区",
value: "盐湖区",
leaf: false,
level: 7,
},
{
label: "临猗县",
value: "临猗县",
leaf: false,
level: 7,
},
{
label: "万荣县",
value: "万荣县",
leaf: false,
level: 7,
},
{
label: "闻喜县",
value: "闻喜县",
leaf: false,
level: 7,
},
{
label: "稷山县",
value: "稷山县",
leaf: false,
level: 7,
},
{
label: "新绛县",
value: "新绛县",
leaf: false,
level: 7,
},
{
label: "绛县",
value: "绛县",
leaf: false,
level: 7,
},
{
label: "垣曲县",
value: "垣曲县",
leaf: false,
level: 7,
},
{
label: "夏县",
value: "夏县",
leaf: false,
level: 7,
},
{
label: "平陆县",
value: "平陆县",
leaf: false,
level: 7,
},
{
label: "芮城县",
value: "芮城县",
leaf: false,
level: 7,
},
{
label: "永济市",
value: "永济市",
leaf: false,
level: 7,
},
{
label: "河津市",
value: "河津市",
leaf: false,
level: 7,
}
]
},
{
label: "忻州市",
value: "忻州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "忻府区",
value: "忻府区",
leaf: false,
level: 7,
},
{
label: "定襄县",
value: "定襄县",
leaf: false,
level: 7,
},
{
label: "五台县",
value: "五台县",
leaf: false,
level: 7,
},
{
label: "代县",
value: "代县",
leaf: false,
level: 7,
},
{
label: "繁峙县",
value: "繁峙县",
leaf: false,
level: 7,
},
{
label: "宁武县",
value: "宁武县",
leaf: false,
level: 7,
},
{
label: "静乐县",
value: "静乐县",
leaf: false,
level: 7,
},
{
label: "神池县",
value: "神池县",
leaf: false,
level: 7,
},
{
label: "五寨县",
value: "五寨县",
leaf: false,
level: 7,
},
{
label: "岢岚县",
value: "岢岚县",
leaf: false,
level: 7,
},
{
label: "河曲县",
value: "河曲县",
leaf: false,
level: 7,
},
{
label: "保德县",
value: "保德县",
leaf: false,
level: 7,
},
{
label: "偏关县",
value: "偏关县",
leaf: false,
level: 7,
},
{
label: "原平市",
value: "原平市",
leaf: false,
level: 7,
}
]
},
{
label: "临汾市",
value: "临汾市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "尧都区",
value: "尧都区",
leaf: false,
level: 7,
},
{
label: "曲沃县",
value: "曲沃县",
leaf: false,
level: 7,
},
{
label: "翼城县",
value: "翼城县",
leaf: false,
level: 7,
},
{
label: "襄汾县",
value: "襄汾县",
leaf: false,
level: 7,
},
{
label: "洪洞县",
value: "洪洞县",
leaf: false,
level: 7,
},
{
label: "古县",
value: "古县",
leaf: false,
level: 7,
},
{
label: "安泽县",
value: "安泽县",
leaf: false,
level: 7,
},
{
label: "浮山县",
value: "浮山县",
leaf: false,
level: 7,
},
{
label: "吉县",
value: "吉县",
leaf: false,
level: 7,
},
{
label: "乡宁县",
value: "乡宁县",
leaf: false,
level: 7,
},
{
label: "大宁县",
value: "大宁县",
leaf: false,
level: 7,
},
{
label: "隰县",
value: "隰县",
leaf: false,
level: 7,
},
{
label: "永和县",
value: "永和县",
leaf: false,
level: 7,
},
{
label: "蒲县",
value: "蒲县",
leaf: false,
level: 7,
},
{
label: "汾西县",
value: "汾西县",
leaf: false,
level: 7,
},
{
label: "侯马市",
value: "侯马市",
leaf: false,
level: 7,
},
{
label: "霍州市",
value: "霍州市",
leaf: false,
level: 7,
}
]
},
{
label: "吕梁市",
value: "吕梁市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "离石区",
value: "离石区",
leaf: false,
level: 7,
},
{
label: "文水县",
value: "文水县",
leaf: false,
level: 7,
},
{
label: "交城县",
value: "交城县",
leaf: false,
level: 7,
},
{
label: "兴县",
value: "兴县",
leaf: false,
level: 7,
},
{
label: "临县",
value: "临县",
leaf: false,
level: 7,
},
{
label: "柳林县",
value: "柳林县",
leaf: false,
level: 7,
},
{
label: "石楼县",
value: "石楼县",
leaf: false,
level: 7,
},
{
label: "岚县",
value: "岚县",
leaf: false,
level: 7,
},
{
label: "方山县",
value: "方山县",
leaf: false,
level: 7,
},
{
label: "中阳县",
value: "中阳县",
leaf: false,
level: 7,
},
{
label: "交口县",
value: "交口县",
leaf: false,
level: 7,
},
{
label: "孝义市",
value: "孝义市",
leaf: false,
level: 7,
},
{
label: "汾阳市",
value: "汾阳市",
leaf: false,
level: 7,
}
]
}
                  ]

                  },
                  {
                  label: "山东省",
                  value: "山东省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "济南市",
value: "济南市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "历下区",
value: "历下区",
leaf: false,
level: 7,
},
{
label: "历城区",
value: "历城区",
leaf: false,
level: 7,
},
{
label: "章丘区",
value: "章丘区",
leaf: false,
level: 7,
},
{
label: "槐荫区",
value: "槐荫区",
leaf: false,
level: 7,
},
{
label: "天桥区",
value: "天桥区",
leaf: false,
level: 7,
},
{
label: "市中区",
value: "市中区",
leaf: false,
level: 7,
},
{
label: "长清区",
value: "长清区",
leaf: false,
level: 7,
},
{
label: "莱芜区",
value: "莱芜区",
leaf: false,
level: 7,
},
{
label: "钢城区",
value: "钢城区",
leaf: false,
level: 7,
},
{
label: "平阴县",
value: "平阴县",
leaf: false,
level: 7,
},
{
label: "商河县",
value: "商河县",
leaf: false,
level: 7,
},

]
},
{
label: "青岛市",
value: "青岛市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "市南区",
value: "市南区",
leaf: false,
level: 7,
},
{
label: "市北区",
value: "市北区",
leaf: false,
level: 7,
},
{
label: "李沧区",
value: "李沧区",
leaf: false,
level: 7,
},
{
label: "崂山区",
value: "崂山区",
leaf: false,
level: 7,
},
{
label: "黄岛区",
value: "黄岛区",
leaf: false,
level: 7,
},
{
label: "城阳区",
value: "城阳区",
leaf: false,
level: 7,
},
{
label: "即墨区",
value: "即墨区",
leaf: false,
level: 7,
},
{
label: "胶州市",
value: "胶州市",
leaf: false,
level: 7,
},
{
label: "平度市",
value: "平度市",
leaf: false,
level: 7,
},
{
label: "莱西市",
value: "莱西市",
leaf: false,
level: 7,
},
]
},
{
label: "淄博市",
value: "淄博市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "淄川区",
value: "淄川区",
leaf: false,
level: 7,
},
{
label: "张店区",
value: "张店区",
leaf: false,
level: 7,
},
{
label: "博山区",
value: "博山区",
leaf: false,
level: 7,
},
{
label: "临淄区",
value: "临淄区",
leaf: false,
level: 7,
},
{
label: "周村区",
value: "周村区",
leaf: false,
level: 7,
},
{
label: "桓台县",
value: "桓台县",
leaf: false,
level: 7,
},
{
label: "高青县",
value: "高青县",
leaf: false,
level: 7,
},
{
label: "沂源县",
value: "沂源县",
leaf: false,
level: 7,
},
]
},
{
label: "枣庄市",
value: "枣庄市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "市中区",
value: "市中区",
leaf: false,
level: 7,
},
{
label: "薛城区",
value: "薛城区",
leaf: false,
level: 7,
},
{
label: "峄城区",
value: "峄城区",
leaf: false,
level: 7,
},
{
label: "台儿庄区",
value: "台儿庄区",
leaf: false,
level: 7,
},
{
label: "山亭区",
value: "山亭区",
leaf: false,
level: 7,
},
{
label: "滕州市",
value: "滕州市",
leaf: false,
level: 7,
},
]
},
{
label: "东营市",
value: "东营市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "东营区",
value: "东营区",
leaf: false,
level: 7,
},
{
label: "河口区",
value: "河口区",
leaf: false,
level: 7,
},
{
label: "垦利区",
value: "垦利区",
leaf: false,
level: 7,
},
{
label: "利津县",
value: "利津县",
leaf: false,
level: 7,
},
{
label: "广饶县",
value: "广饶县",
leaf: false,
level: 7,
},
]
},
{
label: "烟台市",
value: "烟台市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "芝罘区",
value: "芝罘区",
leaf: false,
level: 7,
},
{
label: "福山区",
value: "福山区",
leaf: false,
level: 7,
},
{
label: "牟平区",
value: "牟平区",
leaf: false,
level: 7,
},
{
label: "莱山区",
value: "莱山区",
leaf: false,
level: 7,
},
{
label: "蓬莱区",
value: "蓬莱区",
leaf: false,
level: 7,
},
{
label: "龙口市",
value: "龙口市",
leaf: false,
level: 7,
},
{
label: "莱阳市",
value: "莱阳市",
leaf: false,
level: 7,
},
{
label: "莱州市",
value: "莱州市",
leaf: false,
level: 7,
},
{
label: "招远市",
value: "招远市",
leaf: false,
level: 7,
},
{
label: "栖霞市",
value: "栖霞市",
leaf: false,
level: 7,
},
{
label: "海阳市",
value: "海阳市",
leaf: false,
level: 7,
},
]
},
{
label: "潍坊市",
value: "潍坊市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "潍城区",
value: "潍城区",
leaf: false,
level: 7,
},
{
label: "寒亭区",
value: "寒亭区",
leaf: false,
level: 7,
},
{
label: "坊子区",
value: "坊子区",
leaf: false,
level: 7,
},
{
label: "奎文区",
value: "奎文区",
leaf: false,
level: 7,
},
{
label: "青州市",
value: "青州市",
leaf: false,
level: 7,
},
{
label: "诸城市",
value: "诸城市",
leaf: false,
level: 7,
},
{
label: "寿光市",
value: "寿光市",
leaf: false,
level: 7,
},
{
label: "安丘市",
value: "安丘市",
leaf: false,
level: 7,
},
{
label: "高密市",
value: "高密市",
leaf: false,
level: 7,
},
{
label: "昌邑市",
value: "昌邑市",
leaf: false,
level: 7,
},
{
label: "临朐县",
value: "临朐县",
leaf: false,
level: 7,
},
{
label: "昌乐县",
value: "昌乐县",
leaf: false,
level: 7,
},
]
},
{
label: "济宁市",
value: "济宁市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
{                          
label: "任城区",
value: "任城区",
leaf: false,
level: 7,
},
{
label: "兖州区",
value: "兖州区",
leaf: false,
level: 7,
},
{
label: "曲阜市",
value: "曲阜市",
leaf: false,
level: 7,
},
{
label: "邹城市",
value: "邹城市",
leaf: false,
level: 7,
},
{
label: "微山县",
value: "微山县",
leaf: false,
level: 7,
},
{
label: "鱼台县",
value: "鱼台县",
leaf: false,
level: 7,
},
{
label: "金乡县",
value: "金乡县",
leaf: false,
level: 7,
},
{
label: "嘉祥县",
value: "嘉祥县",
leaf: false,
level: 7,
},
{
label: "汶上县",
value: "汶上县",
leaf: false,
level: 7,
},
{
label: "泗水县",
value: "泗水县",
leaf: false,
level: 7,
},
{
label: "梁山县",
value: "梁山县",
leaf: false,
level: 7,
},
]
},
{
label: "泰安市",
value: "泰安市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "泰山区",
value: "泰山区",
leaf: false,
level: 7,
},
{
label: "岱岳区",
value: "岱岳区",
leaf: false,
level: 7,
},
{
label: "新泰市",
value: "新泰市",
leaf: false,
level: 7,
},
{
label: "肥城市",
value: "肥城市",
leaf: false,
level: 7,
},
{
label: "宁阳县",
value: "宁阳县",
leaf: false,
level: 7,
},
{
label: "东平县",
value: "东平县",
leaf: false,
level: 7,
},
]
},
{
label: "威海市",
value: "威海市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "环翠区",
value: "环翠区",
leaf: false,
level: 7,
},
{
label: "文登区",
value: "文登区",
leaf: false,
level: 7,
},
{
label: "荣成市",
value: "荣成市",
leaf: false,
level: 7,
},
{
label: "乳山市",
value: "乳山市",
leaf: false,
level: 7,
},
]
},
{
label: "日照市",
value: "日照市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "东港区",
value: "东港区",
leaf: false,
level: 7,
},
{
label: "岚山区",
value: "岚山区",
leaf: false,
level: 7,
},
{
label: "五莲县",
value: "五莲县",
leaf: false,
level: 7,
},
{
label: "莒县",
value: "莒县",
leaf: false,
level: 7,
},
]
},
{
label: "临沂市",
value: "临沂市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "兰山区",
value: "兰山区",
leaf: false,
level: 7,
},
{
label: "罗庄区",
value: "罗庄区",
leaf: false,
level: 7,
},
{
label: "河东区",
value: "河东区",
leaf: false,
level: 7,
},
{
label: "沂南县",
value: "沂南县",
leaf: false,
level: 7,
},
{
label: "郯城县",
value: "郯城县",
leaf: false,
level: 7,
},
{
label: "沂水县",
value: "沂水县",
leaf: false,
level: 7,
},
{
label: "兰陵县",
value: "兰陵县",
leaf: false,
level: 7,
},
{
label: "费县",
value: "费县",
leaf: false,
level: 7,
},
{
label: "平邑县",
value: "平邑县",
leaf: false,
level: 7,
},
{
label: "莒南县",
value: "莒南县",
leaf: false,
level: 7,
},
{
label: "蒙阴县",
value: "蒙阴县",
leaf: false,
level: 7,
},
{
label: "临沭县",
value: "临沭县",
leaf: false,
level: 7,
},
]
},
{
label: "德州市",
value: "德州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "德城区",
value: "德城区",
leaf: false,
level: 7,
},
{
label: "陵城区",
value: "陵城区",
leaf: false,
level: 7,
},
{
label: "乐陵市",
value: "乐陵市",
leaf: false,
level: 7,
},
{
label: "禹城市",
value: "禹城市",
leaf: false,
level: 7,
},
{
label: "宁津县",
value: "宁津县",
leaf: false,
level: 7,
},
{
label: "庆云县",
value: "庆云县",
leaf: false,
level: 7,
},
{
label: "临邑县",
value: "临邑县",
leaf: false,
level: 7,
},
{
label: "齐河县",
value: "齐河县",
leaf: false,
level: 7,
},
{
label: "平原县",
value: "平原县",
leaf: false,
level: 7,
},
{
label: "夏津县",
value: "夏津县",
leaf: false,
level: 7,
},
{
label: "武城县",
value: "武城县",
leaf: false,
level: 7,
},
]
},
{
label: "聊城市",
value: "聊城市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "东昌府区",
value: "东昌府区",
leaf: false,
level: 7,
},
{
label: "茌平区",
value: "茌平区",
leaf: false,
level: 7,
},
{
label: "临清市",
value: "临清市",
leaf: false,
level: 7,
},
{
label: "阳谷县",
value: "阳谷县",
leaf: false,
level: 7,
},
{
label: "莘县",
value: "莘县",
leaf: false,
level: 7,
},
{
label: "东阿县",
value: "东阿县",
leaf: false,
level: 7,
},
{
label: "冠县",
value: "冠县",
leaf: false,
level: 7,
},
{
label: "高唐县",
value: "高唐县",
leaf: false,
level: 7,
},
]
},
{
label: "滨州市",
value: "滨州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "滨城区",
value: "滨城区",
leaf: false,
level: 7,
},
{
label: "沾化区",
value: "沾化区",
leaf: false,
level: 7,
},
{
label: "邹平市",
value: "邹平市",
leaf: false,
level: 7,
},
{
label: "惠民县",
value: "惠民县",
leaf: false,
level: 7,
},
{
label: "阳信县",
value: "阳信县",
leaf: false,
level: 7,
},
{
label: "无棣县",
value: "无棣县",
leaf: false,
level: 7,
},
{
label: "博兴县",
value: "博兴县",
leaf: false,
level: 7,
},
]
},
{
label: "菏泽市",
value: "菏泽市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "牡丹区",
value: "牡丹区",
leaf: false,
level: 7,
},
{
label: "定陶区",
value: "定陶区",
leaf: false,
level: 7,
},
{
label: "曹县",
value: "曹县",
leaf: false,
level: 7,
},
{
label: "单县",
value: "单县",
leaf: false,
level: 7,
},
{
label: "成武县",
value: "成武县",
leaf: false,
level: 7,
},
{
label: "巨野县",
value: "巨野县",
leaf: false,
level: 7,
},
{
label: "郓城县",
value: "郓城县",
leaf: false,
level: 7,
},
{
label: "鄄城县",
value: "鄄城县",
leaf: false,
level: 7,
},
{
label: "东明县",
value: "东明县",
leaf: false,
level: 7,
}
]
}
                  ]

                  }

                ],
              },
              {
                label: "南方",
                value: "南方",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "省级",
                    value: "省级",
                    leaf: true,
                    level: 5,

                  },
                  {
                  label: "广东省",
                  value: "广东省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "广州市",
value: "广州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "越秀区",
value: "越秀区",
leaf: false,
level: 7,
},
{
label: "海珠区",
value: "海珠区",
leaf: false,
level: 7,
},
{
label: "荔湾区",
value: "荔湾区",
leaf: false,
level: 7,
},
{
label: "天河区",
value: "天河区",
leaf: false,
level: 7,
},
{
label: "白云区",
value: "白云区",
leaf: false,
level: 7,
},
{
label: "黄埔区",
value: "黄埔区",
leaf: false,
level: 7,
},
{
label: "番禺区",
value: "番禺区",
leaf: false,
level: 7,
},
{
label: "花都区",
value: "花都区",
leaf: false,
level: 7,
},
{
label: "南沙区",
value: "南沙区",
leaf: false,
level: 7,
},
{
label: "从化区",
value: "从化区",
leaf: false,
level: 7,
},
{
label: "增城区",
value: "增城区",
leaf: false,
level: 7,
}
]
},
{
label: "深圳市",
value: "深圳市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "福田区",
value: "福田区",
leaf: false,
level: 7,
},
{
label: "罗湖区",
value: "罗湖区",
leaf: false,
level: 7,
},
{
label: "盐田区",
value: "盐田区",
leaf: false,
level: 7,
},
{
label: "南山区",
value: "南山区",
leaf: false,
level: 7,
},
{
label: "宝安区",
value: "宝安区",
leaf: false,
level: 7,
},
{
label: "龙岗区",
value: "龙岗区",
leaf: false,
level: 7,
},
{
label: "龙华区",
value: "龙华区",
leaf: false,
level: 7,
},
{
label: "坪山区",
value: "坪山区",
leaf: false,
level: 7,
},
{
label: "光明区",
value: "光明区",
leaf: false,
level: 7,
}
]
},
{
label: "珠海市",
value: "珠海市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "香洲区",
value: "香洲区",
leaf: false,
level: 7,
},
{
label: "斗门区",
value: "斗门区",
leaf: false,
level: 7,
},
{
label: "金湾区",
value: "金湾区",
leaf: false,
level: 7,
}
]
},
{
label: "汕头市",
value: "汕头市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "金平区",
value: "金平区",
leaf: false,
level: 7,
},
{
label: "龙湖区",
value: "龙湖区",
leaf: false,
level: 7,
},
{
label: "澄海区",
value: "澄海区",
leaf: false,
level: 7,
},
{
label: "濠江区",
value: "濠江区",
leaf: false,
level: 7,
},
{
label: "潮阳区",
value: "潮阳区",
leaf: false,
level: 7,
},
{
label: "潮南区",
value: "潮南区",
leaf: false,
level: 7,
},
{
label: "南澳县",
value: "南澳县",
leaf: false,
level: 7,
}
]
},
{
label: "佛山市",
value: "佛山市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "禅城区",
value: "禅城区",
leaf: false,
level: 7,
},
{
label: "南海区",
value: "南海区",
leaf: false,
level: 7,
},
{
label: "顺德区",
value: "顺德区",
leaf: false,
level: 7,
},
{
label: "三水区",
value: "三水区",
leaf: false,
level: 7,
},
{
label: "高明区",
value: "高明区",
leaf: false,
level: 7,
}
]
},
{
label: "韶关市",
value: "韶关市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "浈江区",
value: "浈江区",
leaf: false,
level: 7,
},
{
label: "武江区",
value: "武江区",
leaf: false,
level: 7,
},
{
label: "曲江区",
value: "曲江区",
leaf: false,
level: 7,
},
{
label: "乐昌市",
value: "乐昌市",
leaf: false,
level: 7,
},
{
label: "南雄市",
value: "南雄市",
leaf: false,
level: 7,
},
{
label: "始兴县",
value: "始兴县",
leaf: false,
level: 7,
},
{
label: "仁化县",
value: "仁化县",
leaf: false,
level: 7,
},
{
label: "翁源县",
value: "翁源县",
leaf: false,
level: 7,
},
{
label: "乳源瑶族自治县",
value: "乳源瑶族自治县",
leaf: false,
level: 7,
},
{
label: "新丰县",
value: "新丰县",
leaf: false,
level: 7,
}
]
},
{
label: "湛江市",
value: "湛江市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "赤坎区",
value: "赤坎区",
leaf: false,
level: 7,
},
{
label: "霞山区",
value: "霞山区",
leaf: false,
level: 7,
},
{
label: "坡头区",
value: "坡头区",
leaf: false,
level: 7,
},
{
label: "麻章区",
value: "麻章区",
leaf: false,
level: 7,
},
{
label: "廉江市",
value: "廉江市",
leaf: false,
level: 7,
},
{
label: "雷州市",
value: "雷州市",
leaf: false,
level: 7,
},
{
label: "吴川市",
value: "吴川市",
leaf: false,
level: 7,
},
{
label: "遂溪县",
value: "遂溪县",
leaf: false,
level: 7,
},
{
label: "徐闻县",
value: "徐闻县",
leaf: false,
level: 7,
}
]
},
{
label: "肇庆市",
value: "肇庆市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "端州区",
value: "端州区",
leaf: false,
level: 7,
},
{
label: "鼎湖区",
value: "鼎湖区",
leaf: false,
level: 7,
},
{
label: "高要区",
value: "高要区",
leaf: false,
level: 7,
},
{
label: "四会市",
value: "四会市",
leaf: false,
level: 7,
},
{
label: "广宁县",
value: "广宁县",
leaf: false,
level: 7,
},
{
label: "怀集县",
value: "怀集县",
leaf: false,
level: 7,
},
{
label: "封开县",
value: "封开县",
leaf: false,
level: 7,
},
{
label: "德庆县",
value: "德庆县",
leaf: false,
level: 7,
}

]
},
{
label: "江门市",
value: "江门市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "蓬江区",
value: "蓬江区",
leaf: false,
level: 7,
},
{
label: "江海区",
value: "江海区",
leaf: false,
level: 7,
},
{
label: "新会区",
value: "新会区",
leaf: false,
level: 7,
},
{
label: "台山市",
value: "台山市",
leaf: false,
level: 7,
},
{
label: "开平市",
value: "开平市",
leaf: false,
level: 7,
},
{
label: "鹤山市",
value: "鹤山市",
leaf: false,
level: 7,
},
{
label: "恩平市",
value: "恩平市",
leaf: false,
level: 7,
}
]
},
{
label: "茂名市",
value: "茂名市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "茂南区",
value: "茂南区",
leaf: false,
level: 7,
},
{
label: "电白区",
value: "电白区",
leaf: false,
level: 7,
},
{
label: "高州市",
value: "高州市",
leaf: false,
level: 7,
},
{
label: "化州市",
value: "化州市",
leaf: false,
level: 7,
},
{
label: "信宜市",
value: "信宜市",
leaf: false,
level: 7,
}
]
},
{
label: "惠州市",
value: "惠州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "惠城区",
value: "惠城区",
leaf: false,
level: 7,
},
{
label: "惠阳区",
value: "惠阳区",
leaf: false,
level: 7,
},
{
label: "惠东县",
value: "惠东县",
leaf: false,
level: 7,
},
{
label: "博罗县",
value: "博罗县",
leaf: false,
level: 7,
},
{
label: "龙门县",
value: "龙门县",
leaf: false,
level: 7,
}
]
},
{
label: "梅州市",
value: "梅州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "梅江区",
value: "梅江区",
leaf: false,
level: 7,
},
{
label: "梅县区",
value: "梅县区",
leaf: false,
level: 7,
},
{
label: "兴宁市",
value: "兴宁市",
leaf: false,
level: 7,
},
{
label: "大埔县",
value: "大埔县",
leaf: false,
level: 7,
},
{
label: "丰顺县",
value: "丰顺县",
leaf: false,
level: 7,
},
{
label: "五华县",
value: "五华县",
leaf: false,
level: 7,
},
{
label: "平远县",
value: "平远县",
leaf: false,
level: 7,
},
{
label: "蕉岭县",
value: "蕉岭县",
leaf: false,
level: 7,
}
]
},
{
label: "汕尾市",
value: "汕尾市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "海丰县",
value: "海丰县",
leaf: false,
level: 7,
},
{
label: "陆河县",
value: "陆河县",
leaf: false,
level: 7,
},
{
label: "陆丰市",
value: "陆丰市",
leaf: false,
level: 7,
}
]
},
{
label: "河源市",
value: "河源市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "源城区",
value: "源城区",
leaf: false,
level: 7,
},
{
label: "紫金县",
value: "紫金县",
leaf: false,
level: 7,
},
{
label: "龙川县",
value: "龙川县",
leaf: false,
level: 7,
},
{
label: "连平县",
value: "连平县",
leaf: false,
level: 7,
},
{
label: "和平县",
value: "和平县",
leaf: false,
level: 7,
},
{
label: "东源县",
value: "东源县",
leaf: false,
level: 7,
}
]
},
{
label: "阳江市",
value: "阳江市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "江城区",
value: "江城区",
leaf: false,
level: 7,
},
{
label: "阳东区",
value: "阳东区",
leaf: false,
level: 7,
},
{
label: "阳西县",
value: "阳西县",
leaf: false,
level: 7,
},
{
label: "阳春市",
value: "阳春市",
leaf: false,
level: 7,
}
]
},
{
label: "清远市",
value: "清远市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "清城区",
value: "清城区",
leaf: false,
level: 7,
},
{
label: "清新区",
value: "清新区",
leaf: false,
level: 7,
},
{
label: "英德市",
value: "英德市",
leaf: false,
level: 7,
},
{
label: "连州市",
value: "连州市",
leaf: false,
level: 7,
},
{
label: "佛冈县",
value: "佛冈县",
leaf: false,
level: 7,
},
{
label: "阳山县",
value: "阳山县",
leaf: false,
level: 7,
},
{
label: "连南瑶族自治县",
value: "连南瑶族自治县",
leaf: false,
level: 7,
},
{
label: "连山壮族瑶族自治县",
value: "连山壮族瑶族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "东莞市",
value: "东莞市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
]
},
{
label: "中山市",
value: "中山市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
]
},
{
label: "潮州市",
value: "潮州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "湘桥区",
value: "湘桥区",
leaf: false,
level: 7,
},
{
label: "潮安区",
value: "潮安区",
leaf: false,
level: 7,
},
{
label: "饶平县",
value: "饶平县",
leaf: false,
level: 7,
}
]
},
{
label: "揭阳市",
value: "揭阳市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "榕城区",
value: "榕城区",
leaf: false,
level: 7,
},
{
label: "揭东区",
value: "揭东区",
leaf: false,
level: 7,
},
{
label: "揭西县",
value: "揭西县",
leaf: false,
level: 7,
},
{
label: "惠来县",
value: "惠来县",
leaf: false,
level: 7,
},
{
label: "普宁市",
value: "普宁市",
leaf: false,
level: 7,
}
]
},
{
label: "云浮市",
value: "云浮市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "云城区",
value: "云城区",
leaf: false,
level: 7,
},
{
label: "云安区",
value: "云安区",
leaf: false,
level: 7,
},
{
label: "罗定市",
value: "罗定市",
leaf: false,
level: 7,
},
{
label: "新兴县",
value: "新兴县",
leaf: false,
level: 7,
},
{
label: "郁南县",
value: "郁南县",
leaf: false,
level: 7,
}
]
},

                  ]

                  },
                  {
                  label: "广西壮族自治区",
                  value: "广西壮族自治区",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "南宁市",
value: "南宁市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "兴宁区",
value: "兴宁区",
leaf: false,
level: 7,
},
{
label: "青秀区",
value: "青秀区",
leaf: false,
level: 7,
},
{
label: "江南区",
value: "江南区",
leaf: false,
level: 7,
},
{
label: "西乡塘区",
value: "西乡塘区",
leaf: false,
level: 7,
},
{
label: "良庆区",
value: "良庆区",
leaf: false,
level: 7,
},
{
label: "邕宁区",
value: "邕宁区",
leaf: false,
level: 7,
},
{
label: "武鸣区",
value: "武鸣区",
leaf: false,
level: 7,
},

]
},
{
label: "柳州市",
value: "柳州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "城中区",
value: "城中区",
leaf: false,
level: 7,
},
{
label: "鱼峰区",
value: "鱼峰区",
leaf: false,
level: 7,
},
{
label: "柳南区",
value: "柳南区",
leaf: false,
level: 7,
},
{
label: "柳北区",
value: "柳北区",
leaf: false,
level: 7,
},
{
label: "柳江区",
value: "柳江区",
leaf: false,
level: 7,
},
{
label: "柳城县",
value: "柳城县",
leaf: false,
level: 7,
},
{
label: "鹿寨县",
value: "鹿寨县",
leaf: false,
level: 7,
},
{
label: "融安县",
value: "融安县",
leaf: false,
level: 7,
},
{
label: "融水苗族自治县",
value: "融水苗族自治县",
leaf: false,
level: 7,
},
{
label: "三江侗族自治县",
value: "三江侗族自治县",
leaf: false,
level: 7,
},

]
},
{
label: "桂林市",
value: "桂林市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "叠彩区",
value: "叠彩区",
leaf: false,
level: 7,
},
{
label: "象山区",
value: "象山区",
leaf: false,
level: 7,
},
{
label: "七星区",
value: "七星区",
leaf: false,
level: 7,
},
{
label: "雁山区",
value: "雁山区",
leaf: false,
level: 7,
},
{
label: "临桂区",
value: "临桂区",
leaf: false,
level: 7,
},
{
label: "阳朔县",
value: "阳朔县",
leaf: false,
level: 7,
},
{
label: "灵川县",
value: "灵川县",
leaf: false,
level: 7,
},
{
label: "全州县",
value: "全州县",
leaf: false,
level: 7,
},
{
label: "兴安县",
value: "兴安县",
leaf: false,
level: 7,
},
{
label: "永福县",
value: "永福县",
leaf: false,
level: 7,
},
{
label: "灌阳县",
value: "灌阳县",
leaf: false,
level: 7,
},
{
label: "龙胜各族自治县",
value: "龙胜各族自治县",
leaf: false,
level: 7,
},
{
label: "资源县",
value: "资源县",
leaf: false,
level: 7,
},
{
label: "平乐县",
value: "平乐县",
leaf: false,
level: 7,
},
{
label: "荔浦市",
value: "荔浦市",
leaf: false,
level: 7,
},
{
label: "恭城瑶族自治县",
value: "恭城瑶族自治县",
leaf: false,
level: 7,
},
]
},
{
label: "梧州市",
value: "梧州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "万秀区",
value: "万秀区",
leaf: false,
level: 7,
},
{
label: "长洲区",
value: "长洲区",
leaf: false,
level: 7,
},
{
label: "龙圩区",
value: "龙圩区",
leaf: false,
level: 7,
},
{
label: "苍梧县",
value: "苍梧县",
leaf: false,
level: 7,
},
{
label: "藤县",
value: "藤县",
leaf: false,
level: 7,
},
{
label: "蒙山县",
value: "蒙山县",
leaf: false,
level: 7,
},
{
label: "岑溪市",
value: "岑溪市",
leaf: false,
level: 7,
},
]
},
{
label: "北海市",
value: "北海市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "海城区",
value: "海城区",
leaf: false,
level: 7,
},
{
label: "银海区",
value: "银海区",
leaf: false,
level: 7,
},
{
label: "铁山港区",
value: "铁山港区",
leaf: false,
level: 7,
},
{
label: "合浦县",
value: "合浦县",
leaf: false,
level: 7,
},
]
},
{
label: "防城港市",
value: "防城港市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "港口区",
value: "港口区",
leaf: false,
level: 7,
},
{
label: "防城区",
value: "防城区",
leaf: false,
level: 7,
},
{
label: "上思县",
value: "上思县",
leaf: false,
level: 7,
},
{
label: "东兴市",
value: "东兴市",
leaf: false,
level: 7,
},
]
},
{
label: "钦州市",
value: "钦州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "钦南区",
value: "钦南区",
leaf: false,
level: 7,
},
{
label: "钦北区",
value: "钦北区",
leaf: false,
level: 7,
},
{
label: "灵山县",
value: "灵山县",
leaf: false,
level: 7,
},
{
label: "浦北县",
value: "浦北县",
leaf: false,
level: 7,
},
]
},
{
label: "贵港市",
value: "贵港市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "港北区",
value: "港北区",
leaf: false,
level: 7,
},
{
label: "港南区",
value: "港南区",
leaf: false,
level: 7,
},
{
label: "覃塘区",
value: "覃塘区",
leaf: false,
level: 7,
},
{
label: "平南县",
value: "平南县",
leaf: false,
level: 7,
},
{
label: "桂平市",
value: "桂平市",
leaf: false,
level: 7,
},
]
},
{
label: "玉林市",
value: "玉林市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "玉州区",
value: "玉州区",
leaf: false,
level: 7,
},
{
label: "福绵区",
value: "福绵区",
leaf: false,
level: 7,
},
{
label: "容县",
value: "容县",
leaf: false,
level: 7,
},
{
label: "陆川县",
value: "陆川县",
leaf: false,
level: 7,
},
{
label: "博白县",
value: "博白县",
leaf: false,
level: 7,
},
{
label: "兴业县",
value: "兴业县",
leaf: false,
level: 7,
},
{
label: "北流市",
value: "北流市",
leaf: false,
level: 7,
},
]
},
{
label: "百色市",
value: "百色市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "右江区",
value: "右江区",
leaf: false,
level: 7,
},
{
label: "田阳区",
value: "田阳区",
leaf: false,
level: 7,
},
{
label: "田东县",
value: "田东县",
leaf: false,
level: 7,
},
{
label: "平果市",
value: "平果市",
leaf: false,
level: 7,
},
{
label: "德保县",
value: "德保县",
leaf: false,
level: 7,
},
{
label: "靖西市",
value: "靖西市",
leaf: false,
level: 7,
},
{
label: "那坡县",
value: "那坡县",
leaf: false,
level: 7,
},
{
label: "凌云县",
value: "凌云县",
leaf: false,
level: 7,
},
{
label: "乐业县",
value: "乐业县",
leaf: false,
level: 7,
},
{
label: "田林县",
value: "田林县",
leaf: false,
level: 7,
},
{
label: "西林县",
value: "西林县",
leaf: false,
level: 7,
},
{
label: "隆林各族自治县",
value: "隆林各族自治县",
leaf: false,
level: 7,
},
]
},
{
label: "贺州市",
value: "贺州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "八步区",
value: "八步区",
leaf: false,
level: 7,
},
{
label: "平桂区",
value: "平桂区",
leaf: false,
level: 7,
},
{
label: "昭平县",
value: "昭平县",
leaf: false,
level: 7,
},
{
label: "钟山县",
value: "钟山县",
leaf: false,
level: 7,
},
{
label: "富川瑶族自治县",
value: "富川瑶族自治县",
leaf: false,
level: 7,
},
]
},
{
label: "河池市",
value: "河池市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "金城江区",
value: "金城江区",
leaf: false,
level: 7,
},
{
label: "宜州区",
value: "宜州区",
leaf: false,
level: 7,
},
{
label: "南丹县",
value: "南丹县",
leaf: false,
level: 7,
},
{
label: "天峨县",
value: "天峨县",
leaf: false,
level: 7,
},
{
label: "凤山县",
value: "凤山县",
leaf: false,
level: 7,
},
{
label: "东兰县",
value: "东兰县",
leaf: false,
level: 7,
},
{
label: "罗城仫佬族自治县",
value: "罗城仫佬族自治县",
leaf: false,
level: 7,
},
{
label: "环江毛南族自治县",
value: "环江毛南族自治县",
leaf: false,
level: 7,
},
{
label: "巴马瑶族自治县",
value: "巴马瑶族自治县",
leaf: false,
level: 7,
},
{
label: "都安瑶族自治县",
value: "都安瑶族自治县",
leaf: false,
level: 7,
},
{
label: "大化瑶族自治县",
value: "大化瑶族自治县",
leaf: false,
level: 7,
},
]
},
{
label: "来宾市",
value: "来宾市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "兴宾区",
value: "兴宾区",
leaf: false,
level: 7,
},
{
label: "忻城县",
value: "忻城县",
leaf: false,
level: 7,
},
{
label: "象州县",
value: "象州县",
leaf: false,
level: 7,
},
{
label: "武宣县",
value: "武宣县",
leaf: false,
level: 7,
},
{
label: "金秀瑶族自治县",
value: "金秀瑶族自治县",
leaf: false,
level: 7,
},
]
},
{
label: "崇左市",
value: "崇左市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "江州区",
value: "江州区",
leaf: false,
level: 7,
},
{
label: "扶绥县",
value: "扶绥县",
leaf: false,
level: 7,
},
{
label: "宁明县",
value: "宁明县",
leaf: false,
level: 7,
},
{
label: "龙州县",
value: "龙州县",
leaf: false,
level: 7,
},
{
label: "大新县",
value: "大新县",
leaf: false,
level: 7,
},
{
label: "天等县",
value: "天等县",
leaf: false,
level: 7,
},
{
label: "凭祥市",
value: "凭祥市",
leaf: false,
level: 7,
},
]
}
                  ]

                  },
                  {
                  label: "云南省",
                  value: "云南省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "昆明市",
value: "昆明市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "五华区",
value: "五华区",
leaf: false,
level: 7,
},
{
label: "盘龙区",
value: "盘龙区",
leaf: false,
level: 7,
},
{
label: "官渡区",
value: "官渡区",
leaf: false,
level: 7,
},
{
label: "西山区",
value: "西山区",
leaf: false,
level: 7,
},
{
label: "东川区",
value: "东川区",
leaf: false,
level: 7,
},
{
label: "呈贡区",
value: "呈贡区",
leaf: false,
level: 7,
},
{
label: "晋宁区",
value: "晋宁区",
leaf: false,
level: 7,
},
{
label: "富民县",
value: "富民县",
leaf: false,
level: 7,
},
{
label: "宜良县",
value: "宜良县",
leaf: false,
level: 7,
},
{
label: "石林彝族自治县",
value: "石林彝族自治县",
leaf: false,
level: 7,
},
{
label: "嵩明县",
value: "嵩明县",
leaf: false,
level: 7,
},
{
label: "禄劝彝族苗族自治县",
value: "禄劝彝族苗族自治县",
leaf: false,
level: 7,
},
{
label: "寻甸回族彝族自治县",
value: "寻甸回族彝族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "曲靖市",
value: "曲靖市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "麒麟区",
value: "麒麟区",
leaf: false,
level: 7,
},
{
label: "沾益区",
value: "沾益区",
leaf: false,
level: 7,
},
{
label: "马龙区",
value: "马龙区",
leaf: false,
level: 7,
},
{
label: "宣威市",
value: "宣威市",
leaf: false,
level: 7,
},
{
label: "陆良县",
value: "陆良县",
leaf: false,
level: 7,
},
{
label: "师宗县",
value: "师宗县",
leaf: false,
level: 7,
},
{
label: "罗平县",
value: "罗平县",
leaf: false,
level: 7,
},
{
label: "富源县",
value: "富源县",
leaf: false,
level: 7,
},
{
label: "会泽县",
value: "会泽县",
leaf: false,
level: 7,
}
]
},
{
label: "玉溪市",
value: "玉溪市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "红塔区",
value: "红塔区",
leaf: false,
level: 7,
},
{
label: "江川区",
value: "江川区",
leaf: false,
level: 7,
},
{
label: "澄江市",
value: "澄江市",
leaf: false,
level: 7,
},
{
label: "通海县",
value: "通海县",
leaf: false,
level: 7,
},
{
label: "华宁县",
value: "华宁县",
leaf: false,
level: 7,
},
{
label: "易门县",
value: "易门县",
leaf: false,
level: 7,
},
{
label: "峨山彝族自治县",
value: "峨山彝族自治县",
leaf: false,
level: 7,
},
{
label: "新平彝族傣族自治县",
value: "新平彝族傣族自治县",
leaf: false,
level: 7,
},
{
label: "元江哈尼族彝族傣族自治县",
value: "元江哈尼族彝族傣族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "保山市",
value: "保山市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "隆阳区",
value: "隆阳区",
leaf: false,
level: 7,
},
{
label: "腾冲市",
value: "腾冲市",
leaf: false,
level: 7,
},
{
label: "施甸县",
value: "施甸县",
leaf: false,
level: 7,
},
{
label: "龙陵县",
value: "龙陵县",
leaf: false,
level: 7,
},
{
label: "昌宁县",
value: "昌宁县",
leaf: false,
level: 7,
}
]
},
{
label: "昭通市",
value: "昭通市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "昭阳区",
value: "昭阳区",
leaf: false,
level: 7,
},
{
label: "鲁甸县",
value: "鲁甸县",
leaf: false,
level: 7,
},
{
label: "巧家县",
value: "巧家县",
leaf: false,
level: 7,
},
{
label: "盐津县",
value: "盐津县",
leaf: false,
level: 7,
},
{
label: "大关县",
value: "大关县",
leaf: false,
level: 7,
},
{
label: "永善县",
value: "永善县",
leaf: false,
level: 7,
},
{
label: "绥江县",
value: "绥江县",
leaf: false,
level: 7,
},
{
label: "镇雄县",
value: "镇雄县",
leaf: false,
level: 7,
},
{
label: "彝良县",
value: "彝良县",
leaf: false,
level: 7,
},
{
label: "威信县",
value: "威信县",
leaf: false,
level: 7,
},
{
label: "水富市",
value: "水富市",
leaf: false,
level: 7,
}
]
},
{
label: "丽江市",
value: "丽江市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "古城区",
value: "古城区",
leaf: false,
level: 7,
},
{
label: "玉龙纳西族自治县",
value: "玉龙纳西族自治县",
leaf: false,
level: 7,
},
{
label: "永胜县",
value: "永胜县",
leaf: false,
level: 7,
},
{
label: "华坪县",
value: "华坪县",
leaf: false,
level: 7,
},
{
label: "宁蒗彝族自治县",
value: "宁蒗彝族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "普洱市",
value: "普洱市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "思茅区",
value: "思茅区",
leaf: false,
level: 7,
},
{
label: "宁洱哈尼族彝族自治县",
value: "宁洱哈尼族彝族自治县",
leaf: false,
level: 7,
},
{
label: "墨江哈尼族自治县",
value: "墨江哈尼族自治县",
leaf: false,
level: 7,
},
{
label: "景东彝族自治县",
value: "景东彝族自治县",
leaf: false,
level: 7,
},
{
label: "景谷傣族彝族自治县",
value: "景谷傣族彝族自治县",
leaf: false,
level: 7,
},
{
label: "镇沅彝族哈尼族拉祜族自治县",
value: "镇沅彝族哈尼族拉祜族自治县",
leaf: false,
level: 7,
},
{
label: "江城哈尼族彝族自治县",
value: "江城哈尼族彝族自治县",
leaf: false,
level: 7,
},
{
label: "孟连傣族拉祜族佤族自治县",
value: "孟连傣族拉祜族佤族自治县",
leaf: false,
level: 7,
},
{
label: "澜沧拉祜族自治县",
value: "澜沧拉祜族自治县",
leaf: false,
level: 7,
},
{
label: "西盟佤族自治县",
value: "西盟佤族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "临沧市",
value: "临沧市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "临翔区",
value: "临翔区",
leaf: false,
level: 7,
},
{
label: "凤庆县",
value: "凤庆县",
leaf: false,
level: 7,
},
{
label: "云县",
value: "云县",
leaf: false,
level: 7,
},
{
label: "永德县",
value: "永德县",
leaf: false,
level: 7,
},
{
label: "镇康县",
value: "镇康县",
leaf: false,
level: 7,
},
{
label: "双江拉祜族佤族布朗族傣族自治县",
value: "双江拉祜族佤族布朗族傣族自治县",
leaf: false,
level: 7,
},
{
label: "耿马傣族佤族自治县",
value: "耿马傣族佤族自治县",
leaf: false,
level: 7,
},
{
label: "沧源佤族自治县",
value: "沧源佤族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "文山壮族苗族自治州",
value: "文山壮族苗族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "文山市",
value: "文山市",
leaf: false,
level: 7,
},
{
label: "砚山县",
value: "砚山县",
leaf: false,
level: 7,
},
{
label: "西畴县",
value: "西畴县",
leaf: false,
level: 7,
},
{
label: "麻栗坡县",
value: "麻栗坡县",
leaf: false,
level: 7,
},
{
label: "马关县",
value: "马关县",
leaf: false,
level: 7,
},
{
label: "丘北县",
value: "丘北县",
leaf: false,
level: 7,
},
{
label: "广南县",
value: "广南县",
leaf: false,
level: 7,
},
{
label: "富宁县",
value: "富宁县",
leaf: false,
level: 7,
}
]
},
{
label: "红河哈尼族彝族自治州",
value: "红河哈尼族彝族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "蒙自市",
value: "蒙自市",
leaf: false,
level: 7,
},
{
label: "个旧市",
value: "个旧市",
leaf: false,
level: 7,
},
{
label: "开远市",
value: "开远市",
leaf: false,
level: 7,
},
{
label: "弥勒市",
value: "弥勒市",
leaf: false,
level: 7,
},
{
label: "建水县",
value: "建水县",
leaf: false,
level: 7,
},
{
label: "石屏县",
value: "石屏县",
leaf: false,
level: 7,
},
{
label: "泸西县",
value: "泸西县",
leaf: false,
level: 7,
},
{
label: "元阳县",
value: "元阳县",
leaf: false,
level: 7,
},
{
label: "红河县",
value: "红河县",
leaf: false,
level: 7,
},
{
label: "金平苗族瑶族傣族自治县",
value: "金平苗族瑶族傣族自治县",
leaf: false,
level: 7,
},
{
label: "绿春县",
value: "绿春县",
leaf: false,
level: 7,
},
{
label: "屏边苗族自治县",
value: "屏边苗族自治县",
leaf: false,
level: 7,
},
{
label: "河口瑶族自治县",
value: "河口瑶族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "西双版纳傣族自治州",
value: "西双版纳傣族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "景洪市",
value: "景洪市",
leaf: false,
level: 7,
},
{
label: "勐海县",
value: "勐海县",
leaf: false,
level: 7,
},
{
label: "勐腊县",
value: "勐腊县",
leaf: false,
level: 7,
}
]
},
{
label: "楚雄彝族自治州",
value: "楚雄彝族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "楚雄市",
value: "楚雄市",
leaf: false,
level: 7,
},
{
label: "双柏县",
value: "双柏县",
leaf: false,
level: 7,
},
{
label: "牟定县",
value: "牟定县",
leaf: false,
level: 7,
},
{
label: "南华县",
value: "南华县",
leaf: false,
level: 7,
},
{
label: "姚安县",
value: "姚安县",
leaf: false,
level: 7,
},
{
label: "大姚县",
value: "大姚县",
leaf: false,
level: 7,
},
{
label: "永仁县",
value: "永仁县",
leaf: false,
level: 7,
},
{
label: "元谋县",
value: "元谋县",
leaf: false,
level: 7,
},
{
label: "武定县",
value: "武定县",
leaf: false,
level: 7,
},
{
label: "禄丰市",
value: "禄丰市",
leaf: false,
level: 7,
}
]
},
{
label: "大理白族自治州",
value: "大理白族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "大理市",
value: "大理市",
leaf: false,
level: 7,
},
{
label: "漾濞彝族自治县",
value: "漾濞彝族自治县",
leaf: false,
level: 7,
},
{
label: "祥云县",
value: "祥云县",
leaf: false,
level: 7,
},
{
label: "宾川县",
value: "宾川县",
leaf: false,
level: 7,
},
{
label: "弥渡县",
value: "弥渡县",
leaf: false,
level: 7,
},
{
label: "南涧彝族自治县",
value: "南涧彝族自治县",
leaf: false,
level: 7,
},
{
label: "巍山彝族回族自治县",
value: "巍山彝族回族自治县",
leaf: false,
level: 7,
},
{
label: "永平县",
value: "永平县",
leaf: false,
level: 7,
},
{
label: "云龙县",
value: "云龙县",
leaf: false,
level: 7,
},
{
label: "洱源县",
value: "洱源县",
leaf: false,
level: 7,
},
{
label: "剑川县",
value: "剑川县",
leaf: false,
level: 7,
},
{
label: "鹤庆县",
value: "鹤庆县",
leaf: false,
level: 7,
}
]
},
{
label: "德宏傣族景颇族自治州",
value: "德宏傣族景颇族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "芒市",
value: "芒市",
leaf: false,
level: 7,
},
{
label: "瑞丽市",
value: "瑞丽市",
leaf: false,
level: 7,
},
{
label: "梁河县",
value: "梁河县",
leaf: false,
level: 7,
},
{
label: "盈江县",
value: "盈江县",
leaf: false,
level: 7,
},
{
label: "陇川县",
value: "陇川县",
leaf: false,
level: 7,
}
]
},
{
label: "怒江傈僳族自治州",
value: "怒江傈僳族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "泸水市",
value: "泸水市",
leaf: false,
level: 7,
},
{
label: "福贡县",
value: "福贡县",
leaf: false,
level: 7,
},
{
label: "贡山独龙族怒族自治县",
value: "贡山独龙族怒族自治县",
leaf: false,
level: 7,
},
{
label: "兰坪白族普米族自治县",
value: "兰坪白族普米族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "迪庆藏族自治州",
value: "迪庆藏族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "香格里拉市",
value: "香格里拉市",
leaf: false,
level: 7,
},
{
label: "德钦县",
value: "德钦县",
leaf: false,
level: 7,
},
{
label: "维西傈僳族自治县",
value: "维西傈僳族自治县",
leaf: false,
level: 7,
}
]
}
                  ]

                  },
                  {
                  label: "贵州省",
                  value: "贵州省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "贵阳市",
value: "贵阳市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "南明区",
value: "南明区",
leaf: false,
level: 7,
},
{
label: "云岩区",
value: "云岩区",
leaf: false,
level: 7,
},
{
label: "花溪区",
value: "花溪区",
leaf: false,
level: 7,
},
{
label: "乌当区",
value: "乌当区",
leaf: false,
level: 7,
},
{
label: "白云区",
value: "白云区",
leaf: false,
level: 7,
},
{
label: "观山湖区",
value: "观山湖区",
leaf: false,
level: 7,
},
{
label: "开阳县",
value: "开阳县",
leaf: false,
level: 7,
},
{
label: "息烽县",
value: "息烽县",
leaf: false,
level: 7,
},
{
label: "修文县",
value: "修文县",
leaf: false,
level: 7,
}

]
},
{
label: "六盘水市",
value: "六盘水市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "钟山区",
value: "钟山区",
leaf: false,
level: 7,
},
{
label: "水城区",
value: "水城区",
leaf: false,
level: 7,
},
{
label: "六枝特区",
value: "六枝特区",
leaf: false,
level: 7,
},
{
label: "盘州市",
value: "盘州市",
leaf: false,
level: 7,
}


]
},
{
label: "遵义市",
value: "遵义市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "红花岗区",
value: "红花岗区",
leaf: false,
level: 7,
},
{
label: "汇川区",
value: "汇川区",
leaf: false,
level: 7,
},
{
label: "播州区",
value: "播州区",
leaf: false,
level: 7,
},
{
label: "赤水市",
value: "赤水市",
leaf: false,
level: 7,
},
{
label: "仁怀市",
value: "仁怀市",
leaf: false,
level: 7,
},
{
label: "桐梓县",
value: "桐梓县",
leaf: false,
level: 7,
},
{
label: "绥阳县",
value: "绥阳县",
leaf: false,
level: 7,
},
{
label: "正安县",
value: "正安县",
leaf: false,
level: 7,
},
{
label: "道真仡佬族苗族自治县",
value: "道真仡佬族苗族自治县",
leaf: false,
level: 7,
},
{
label: "务川仡佬族苗族自治县",
value: "务川仡佬族苗族自治县",
leaf: false,
level: 7,
},
{
label: "凤冈县",
value: "凤冈县",
leaf: false,
level: 7,
},
{
label: "湄潭县",
value: "湄潭县",
leaf: false,
level: 7,
},
{
label: "余庆县",
value: "余庆县",
leaf: false,
level: 7,
},
{
label: "习水县",
value: "习水县",
leaf: false,
level: 7,
}
]
},
{
label: "安顺市",
value: "安顺市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "西秀区",
value: "西秀区",
leaf: false,
level: 7,
},
{
label: "平坝区",
value: "平坝区",
leaf: false,
level: 7,
},
{
label: "普定县",
value: "普定县",
leaf: false,
level: 7,
},
{
label: "镇宁布依族苗族自治县",
value: "镇宁布依族苗族自治县",
leaf: false,
level: 7,
},
{
label: "关岭布依族苗族自治县",
value: "关岭布依族苗族自治县",
leaf: false,
level: 7,
},
{
label: "紫云苗族布依族自治县",
value: "紫云苗族布依族自治县",
leaf: false,
level: 7,
}

]
},
{
label: "毕节市",
value: "毕节市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "七星关区",
value: "七星关区",
leaf: false,
level: 7,
},
{
label: "大方县",
value: "大方县",
leaf: false,
level: 7,
},
{
label: "黔西市",
value: "黔西市",
leaf: false,
level: 7,
},
{
label: "金沙县",
value: "金沙县",
leaf: false,
level: 7,
},
{
label: "织金县",
value: "织金县",
leaf: false,
level: 7,
},
{
label: "纳雍县",
value: "纳雍县",
leaf: false,
level: 7,
},
{
label: "威宁彝族回族苗族自治县",
value: "威宁彝族回族苗族自治县",
leaf: false,
level: 7,
},
{
label: "赫章县",
value: "赫章县",
leaf: false,
level: 7,
}
]
},
{
label: "铜仁市",
value: "铜仁市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "碧江区",
value: "碧江区",
leaf: false,
level: 7,
},
{
label: "万山区",
value: "万山区",
leaf: false,
level: 7,
},
{
label: "江口县",
value: "江口县",
leaf: false,
level: 7,
},
{
label: "玉屏侗族自治县",
value: "玉屏侗族自治县",
leaf: false,
level: 7,
},
{
label: "石阡县",
value: "石阡县",
leaf: false,
level: 7,
},
{
label: "思南县",
value: "思南县",
leaf: false,
level: 7,
},
{
label: "印江土家族苗族自治县",
value: "印江土家族苗族自治县",
leaf: false,
level: 7,
},
{
label: "德江县",
value: "德江县",
leaf: false,
level: 7,
},
{
label: "沿河土家族自治县",
value: "沿河土家族自治县",
leaf: false,
level: 7,
},
{
label: "松桃苗族自治县",
value: "松桃苗族自治县",
leaf: false,
level: 7,
}

]
},
{
label: "黔东南苗族侗族自治州",
value: "黔东南苗族侗族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "凯里市",
value: "凯里市",
leaf: false,
level: 7,
},
{
label: "黄平县",
value: "黄平县",
leaf: false,
level: 7,
},
{
label: "施秉县",
value: "施秉县",
leaf: false,
level: 7,
},
{
label: "三穗县",
value: "三穗县",
leaf: false,
level: 7,
},
{
label: "镇远县",
value: "镇远县",
leaf: false,
level: 7,
},
{
label: "岑巩县",
value: "岑巩县",
leaf: false,
level: 7,
},
{
label: "天柱县",
value: "天柱县",
leaf: false,
level: 7,
},
{
label: "锦屏县",
value: "锦屏县",
leaf: false,
level: 7,
},
{
label: "剑河县",
value: "剑河县",
leaf: false,
level: 7,
},
{
label: "台江县",
value: "台江县",
leaf: false,
level: 7,
},
{
label: "黎平县",
value: "黎平县",
leaf: false,
level: 7,
},
{
label: "榕江县",
value: "榕江县",
leaf: false,
level: 7,
},
{
label: "从江县",
value: "从江县",
leaf: false,
level: 7,
},
{
label: "雷山县",
value: "雷山县",
leaf: false,
level: 7,
},
{
label: "麻江县",
value: "麻江县",
leaf: false,
level: 7,
},
{
label: "丹寨县",
value: "丹寨县",
leaf: false,
level: 7,
}
]
},
{
label: "黔南布依族苗族自治州",
value: "黔南布依族苗族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "都匀市",
value: "都匀市",
leaf: false,
level: 7,
},
{
label: "福泉市",
value: "福泉市",
leaf: false,
level: 7,
},
{
label: "荔波县",
value: "荔波县",
leaf: false,
level: 7,
},
{
label: "贵定县",
value: "贵定县",
leaf: false,
level: 7,
},
{
label: "瓮安县",
value: "瓮安县",
leaf: false,
level: 7,
},
{
label: "独山县",
value: "独山县",
leaf: false,
level: 7,
},
{
label: "平塘县",
value: "平塘县",
leaf: false,
level: 7,
},
{
label: "罗甸县",
value: "罗甸县",
leaf: false,
level: 7,
},
{
label: "长顺县",
value: "长顺县",
leaf: false,
level: 7,
},
{
label: "龙里县",
value: "龙里县",
leaf: false,
level: 7,
},
{
label: "惠水县",
value: "惠水县",
leaf: false,
level: 7,
},
{
label: "三都水族自治县",
value: "三都水族自治县",
leaf: false,
level: 7,
}

]
},
{
label: "黔西南布依族苗族自治州",
value: "黔西南布依族苗族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "兴义市",
value: "兴义市",
leaf: false,
level: 7,
},
{
label: "兴仁市",
value: "兴仁市",
leaf: false,
level: 7,
},
{
label: "普安县",
value: "普安县",
leaf: false,
level: 7,
},
{
label: "晴隆县",
value: "晴隆县",
leaf: false,
level: 7,
},
{
label: "贞丰县",
value: "贞丰县",
leaf: false,
level: 7,
},
{
label: "望谟县",
value: "望谟县",
leaf: false,
level: 7,
},
{
label: "册亨县",
value: "册亨县",
leaf: false,
level: 7,
},
{
label: "安龙县",
value: "安龙县",
leaf: false,
level: 7,
}

]
}
                  ]

                  },
                  {
                  label: "海南省",
                  value: "海南省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "海口市",
value: "海口市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "秀英区",
value: "秀英区",
leaf: false,
level: 7,
},
{
label: "龙华区",
value: "龙华区",
leaf: false,
level: 7,
},
{
label: "琼山区",
value: "琼山区",
leaf: false,
level: 7,
},
{
label: "美兰区",
value: "美兰区",
leaf: false,
level: 7,
},
]
},
{
label: "三亚市",
value: "三亚市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "海棠区",
value: "海棠区",
leaf: false,
level: 7,
},
{
label: "吉阳区",
value: "吉阳区",
leaf: false,
level: 7,
},
{
label: "天涯区",
value: "天涯区",
leaf: false,
level: 7,
},
{
label: "崖州区",
value: "崖州区",
leaf: false,
level: 7,
},
]
},
{
label: "儋州市",
value: "儋州市",
leaf: false,
level: 6,
children: [

]
},
{
label: "三沙市",
value: "三沙市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "西沙区",
value: "西沙区",
leaf: false,
level: 7,
},
{
label: "南沙区",
value: "南沙区",
leaf: false,
level: 7,
},
]
}
                  ]

                  },
                 

                ],
              },
              {
                label: "西北",
                value: "西北",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "省级",
                    value: "省级",
                    leaf: true,
                    level: 5,
 
                  },
                  {
                  label: "陕西省",
                  value: "陕西省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "西安市",
value: "西安市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "未央区",
value: "未央区",
leaf: false,
level: 7,
},
{
label: "碑林区",
value: "碑林区",
leaf: false,
level: 7,
},
{
label: "莲湖区",
value: "莲湖区",
leaf: false,
level: 7,
},
{
label: "灞桥区",
value: "灞桥区",
leaf: false,
level: 7,
},
{
label: "雁塔区",
value: "雁塔区",
leaf: false,
level: 7,
},
{
label: "阎良区",
value: "阎良区",
leaf: false,
level: 7,
},
{
label: "临潼区",
value: "临潼区",
leaf: false,
level: 7,
},
{
label: "长安区",
value: "长安区",
leaf: false,
level: 7,
},
{
label: "高陵区",
value: "高陵区",
leaf: false,
level: 7,
},
{
label: "鄠邑区",
value: "鄠邑区",
leaf: false,
level: 7,
}

]
},
{
label: "宝鸡市",
value: "宝鸡市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "渭滨区",
value: "渭滨区",
leaf: false,
level: 7,
},
{
label: "金台区",
value: "金台区",
leaf: false,
level: 7,
},
{
label: "陈仓区",
value: "陈仓区",
leaf: false,
level: 7,
},
{
label: "凤翔区",
value: "凤翔区",
leaf: false,
level: 7,
},
{
label: "岐山县",
value: "岐山县",
leaf: false,
level: 7,
},
{
label: "扶风县",
value: "扶风县",
leaf: false,
level: 7,
},
{
label: "眉县",
value: "眉县",
leaf: false,
level: 7,
},
{
label: "陇县",
value: "陇县",
leaf: false,
level: 7,
},
{
label: "千阳县",
value: "千阳县",
leaf: false,
level: 7,
},
{
label: "麟游县",
value: "麟游县",
leaf: false,
level: 7,
},
{
label: "凤县",
value: "凤县",
leaf: false,
level: 7,
},
{
label: "太白县",
value: "太白县",
leaf: false,
level: 7,
}
]
},
{
label: "咸阳市",
value: "咸阳市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "秦都区",
value: "秦都区",
leaf: false,
level: 7,
},
{
label: "渭城区",
value: "渭城区",
leaf: false,
level: 7,
},
{
label: "杨陵区",
value: "杨陵区",
leaf: false,
level: 7,
},
{
label: "兴平市",
value: "兴平市",
leaf: false,
level: 7,
},
{
label: "三原县",
value: "三原县",
leaf: false,
level: 7,
},
{
label: "泾阳县",
value: "泾阳县",
leaf: false,
level: 7,
},
{
label: "乾县",
value: "乾县",
leaf: false,
level: 7,
},
{
label: "礼泉县",
value: "礼泉县",
leaf: false,
level: 7,
},
{
label: "永寿县",
value: "永寿县",
leaf: false,
level: 7,
},
{
label: "彬州市",
value: "彬州市",
leaf: false,
level: 7,
},
{
label: "长武县",
value: "长武县",
leaf: false,
level: 7,
},
{
label: "旬邑县",
value: "旬邑县",
leaf: false,
level: 7,
},
{
label: "淳化县",
value: "淳化县",
leaf: false,
level: 7,
},
{
label: "武功县",
value: "武功县",
leaf: false,
level: 7,
}
]
},
{
label: "渭南市",
value: "渭南市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "临渭区",
value: "临渭区",
leaf: false,
level: 7,
},
{
label: "华州区",
value: "华州区",
leaf: false,
level: 7,
},
{
label: "潼关县",
value: "潼关县",
leaf: false,
level: 7,
},
{
label: "大荔县",
value: "大荔县",
leaf: false,
level: 7,
},
{
label: "合阳县",
value: "合阳县",
leaf: false,
level: 7,
},
{
label: "澄城县",
value: "澄城县",
leaf: false,
level: 7,
},
{
label: "蒲城县",
value: "蒲城县",
leaf: false,
level: 7,
},
{
label: "白水县",
value: "白水县",
leaf: false,
level: 7,
},
{
label: "富平县",
value: "富平县",
leaf: false,
level: 7,
},
{
label: "韩城市",
value: "韩城市",
leaf: false,
level: 7,
},
{
label: "华阴市",
value: "华阴市",
leaf: false,
level: 7,
}

]
},
{
label: "铜川市",
value: "铜川市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "王益区",
value: "王益区",
leaf: false,
level: 7,
},
{
label: "印台区",
value: "印台区",
leaf: false,
level: 7,
},
{
label: "耀州区",
value: "耀州区",
leaf: false,
level: 7,
},
{
label: "宜君县",
value: "宜君县",
leaf: false,
level: 7,
}
]
},
{
label: "延安市",
value: "延安市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "宝塔区",
value: "宝塔区",
leaf: false,
level: 7,
},
{
label: "安塞区",
value: "安塞区",
leaf: false,
level: 7,
},
{
label: "延长县",
value: "延长县",
leaf: false,
level: 7,
},
{
label: "延川县",
value: "延川县",
leaf: false,
level: 7,
},
{
label: "子长市",
value: "子长市",
leaf: false,
level: 7,
},
{
label: "志丹县",
value: "志丹县",
leaf: false,
level: 7,
},
{
label: "吴起县",
value: "吴起县",
leaf: false,
level: 7,
},
{
label: "甘泉县",
value: "甘泉县",
leaf: false,
level: 7,
},
{
label: "富县",
value: "富县",
leaf: false,
level: 7,
},
{
label: "洛川县",
value: "洛川县",
leaf: false,
level: 7,
},
{
label: "宜川县",
value: "宜川县",
leaf: false,
level: 7,
},
{
label: "黄龙县",
value: "黄龙县",
leaf: false,
level: 7,
},
{
label: "黄陵县",
value: "黄陵县",
leaf: false,
level: 7,
}
]
},
{
label: "榆林市",
value: "榆林市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "榆阳区",
value: "榆阳区",
leaf: false,
level: 7,
},
{
label: "横山区",
value: "横山区",
leaf: false,
level: 7,
},
{
label: "神木市",
value: "神木市",
leaf: false,
level: 7,
},
{
label: "府谷县",
value: "府谷县",
leaf: false,
level: 7,
},
{
label: "靖边县",
value: "靖边县",
leaf: false,
level: 7,
},
{
label: "定边县",
value: "定边县",
leaf: false,
level: 7,
},
{
label: "绥德县",
value: "绥德县",
leaf: false,
level: 7,
},
{
label: "米脂县",
value: "米脂县",
leaf: false,
level: 7,
},
{
label: "佳县",
value: "佳县",
leaf: false,
level: 7,
},
{
label: "吴堡县",
value: "吴堡县",
leaf: false,
level: 7,
},
{
label: "清涧县",
value: "清涧县",
leaf: false,
level: 7,
},
{
label: "子洲县",
value: "子洲县",
leaf: false,
level: 7,
}
]
},
{
label: "汉中市",
value: "汉中市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "汉台区",
value: "汉台区",
leaf: false,
level: 7,
},
{
label: "南郑区",
value: "南郑区",
leaf: false,
level: 7,
},
{
label: "城固县",
value: "城固县",
leaf: false,
level: 7,
},
{
label: "洋县",
value: "洋县",
leaf: false,
level: 7,
},
{
label: "西乡县",
value: "西乡县",
leaf: false,
level: 7,
},
{
label: "勉县",
value: "勉县",
leaf: false,
level: 7,
},
{
label: "宁强县",
value: "宁强县",
leaf: false,
level: 7,
},
{
label: "略阳县",
value: "略阳县",
leaf: false,
level: 7,
},
{
label: "镇巴县",
value: "镇巴县",
leaf: false,
level: 7,
},
{
label: "留坝县",
value: "留坝县",
leaf: false,
level: 7,
},
{
label: "佛坪县",
value: "佛坪县",
leaf: false,
level: 7,
}
]
},
{
label: "安康市",
value: "安康市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "汉滨区",
value: "汉滨区",
leaf: false,
level: 7,
},
{
label: "汉阴县",
value: "汉阴县",
leaf: false,
level: 7,
},
{
label: "石泉县",
value: "石泉县",
leaf: false,
level: 7,
},
{
label: "宁陕县",
value: "宁陕县",
leaf: false,
level: 7,
},
{
label: "紫阳县",
value: "紫阳县",
leaf: false,
level: 7,
},
{
label: "岚皋县",
value: "岚皋县",
leaf: false,
level: 7,
},
{
label: "平利县",
value: "平利县",
leaf: false,
level: 7,
},
{
label: "镇坪县",
value: "镇坪县",
leaf: false,
level: 7,
},
{
label: "旬阳市",
value: "旬阳市",
leaf: false,
level: 7,
},
{
label: "白河县",
value: "白河县",
leaf: false,
level: 7,
}
]
},
{
label: "商洛市",
value: "商洛市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "商州区",
value: "商州区",
leaf: false,
level: 7,
},
{
label: "洛南县",
value: "洛南县",
leaf: false,
level: 7,
},
{
label: "丹凤县",
value: "丹凤县",
leaf: false,
level: 7,
},
{
label: "商南县",
value: "商南县",
leaf: false,
level: 7,
},
{
label: "山阳县",
value: "山阳县",
leaf: false,
level: 7,
},
{
label: "镇安县",
value: "镇安县",
leaf: false,
level: 7,
},
{
label: "柞水县",
value: "柞水县",
leaf: false,
level: 7,
}
]
}
                  ]

                  },
                  {
                  label: "甘肃省",
                  value: "甘肃省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "兰州市",
value: "兰州市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "七里河区",
value: "七里河区",
leaf: false,
level: 7,
},
{
label: "安宁区",
value: "安宁区",
leaf: false,
level: 7,
},
{
label: "红古区",
value: "红古区",
leaf: false,
level: 7,
},
{
label: "皋兰县",
value: "皋兰县",
leaf: false,
level: 7,
},
{
label: "榆中县",
value: "榆中县",
leaf: false,
level: 7,
},
]
},
{
label: "嘉峪关市",
value: "嘉峪关市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
]
},
{
label: "金昌市",
value: "金昌市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "金川区",
value: "金川区",
leaf: false,
level: 7,
},
{
label: "永昌县",
value: "永昌县",
leaf: false,
level: 7,
},
]
},
{
label: "白银市",
value: "白银市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "白银区",
value: "白银区",
leaf: false,
level: 7,
},
{
label: "平川区",
value: "平川区",
leaf: false,
level: 7,
},
{
label: "靖远县",
value: "靖远县",
leaf: false,
level: 7,
},
{
label: "会宁县",
value: "会宁县",
leaf: false,
level: 7,
},
{
label: "景泰县",
value: "景泰县",
leaf: false,
level: 7,
},
]
},
{
label: "天水市",
value: "天水市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "秦州区",
value: "秦州区",
leaf: false,
level: 7,
},
{
label: "麦积区",
value: "麦积区",
leaf: false,
level: 7,
},
{
label: "清水县",
value: "清水县",
leaf: false,
level: 7,
},
{
label: "秦安县",
value: "秦安县",
leaf: false,
level: 7,
},
{
label: "甘谷县",
value: "甘谷县",
leaf: false,
level: 7,
},
{
label: "武山县",
value: "武山县",
leaf: false,
level: 7,
},
{
label: "张家川回族自治县",
value: "张家川回族自治县",
leaf: false,
level: 7,
},
]
},
{
label: "武威市",
value: "武威市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "凉州区",
value: "凉州区",
leaf: false,
level: 7,
},
{
label: "民勤县",
value: "民勤县",
leaf: false,
level: 7,
},
{
label: "古浪县",
value: "古浪县",
leaf: false,
level: 7,
},
{
label: "天祝藏族自治县",
value: "天祝藏族自治县",
leaf: false,
level: 7,
},
]
},
{
label: "张掖市",
value: "张掖市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "甘州区",
value: "甘州区",
leaf: false,
level: 7,
},
{
label: "肃南裕固族自治县",
value: "肃南裕固族自治县",
leaf: false,
level: 7,
},
{
label: "民乐县",
value: "民乐县",
leaf: false,
level: 7,
},
{
label: "临泽县",
value: "临泽县",
leaf: false,
level: 7,
},
{
label: "高台县",
value: "高台县",
leaf: false,
level: 7,
},
{
label: "山丹县",
value: "山丹县",
leaf: false,
level: 7,
},

]
},
{
label: "平凉市",
value: "平凉市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "崆峒区",
value: "崆峒区",
leaf: false,
level: 7,
},
{
label: "泾川县",
value: "泾川县",
leaf: false,
level: 7,
},
{
label: "灵台县",
value: "灵台县",
leaf: false,
level: 7,
},
{
label: "崇信县",
value: "崇信县",
leaf: false,
level: 7,
},
{
label: "华亭市",
value: "华亭市",
leaf: false,
level: 7,
},
{
label: "庄浪县",
value: "庄浪县",
leaf: false,
level: 7,
},
{
label: "静宁县",
value: "静宁县",
leaf: false,
level: 7,
},
]
},
{
label: "酒泉市",
value: "酒泉市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "肃州区",
value: "肃州区",
leaf: false,
level: 7,
},
{
label: "金塔县",
value: "金塔县",
leaf: false,
level: 7,
},
{
label: "瓜州县",
value: "瓜州县",
leaf: false,
level: 7,
},
{
label: "肃北蒙古族自治县",
value: "肃北蒙古族自治县",
leaf: false,
level: 7,
},
{
label: "阿克塞哈萨克族自治县",
value: "阿克塞哈萨克族自治县",
leaf: false,
level: 7,
},
{
label: "玉门市",
value: "玉门市",
leaf: false,
level: 7,
},
{
label: "敦煌市",
value: "敦煌市",
leaf: false,
level: 7,
},
]
},
{
label: "庆阳市",
value: "庆阳市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "西峰区",
value: "西峰区",
leaf: false,
level: 7,
},
{
label: "庆城县",
value: "庆城县",
leaf: false,
level: 7,
},
{
label: "环县",
value: "环县",
leaf: false,
level: 7,
},
{
label: "华池县",
value: "华池县",
leaf: false,
level: 7,
},
{
label: "合水县",
value: "合水县",
leaf: false,
level: 7,
},
{
label: "正宁县",
value: "正宁县",
leaf: false,
level: 7,
},
{
label: "宁县",
value: "宁县",
leaf: false,
level: 7,
},
{
label: "镇原县",
value: "镇原县",
leaf: false,
level: 7,
},
]
},
{
label: "定西市",
value: "定西市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "安定区",
value: "安定区",
leaf: false,
level: 7,
},
{
label: "通渭县",
value: "通渭县",
leaf: false,
level: 7,
},
{
label: "陇西县",
value: "陇西县",
leaf: false,
level: 7,
},
{
label: "渭源县",
value: "渭源县",
leaf: false,
level: 7,
},
{
label: "临洮县",
value: "临洮县",
leaf: false,
level: 7,
},
{
label: "漳县",
value: "漳县",
leaf: false,
level: 7,
},
{
label: "岷县",
value: "岷县",
leaf: false,
level: 7,
},
]
},
{
label: "陇南市",
value: "陇南市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "武都区",
value: "武都区",
leaf: false,
level: 7,
},
{
label: "成县",
value: "成县",
leaf: false,
level: 7,
},
{
label: "文县",
value: "文县",
leaf: false,
level: 7,
},
{
label: "宕昌县",
value: "宕昌县",
leaf: false,
level: 7,
},
{
label: "康县",
value: "康县",
leaf: false,
level: 7,
},
{
label: "西和县",
value: "西和县",
leaf: false,
level: 7,
},
{
label: "礼县",
value: "礼县",
leaf: false,
level: 7,
},
{
label: "徽县",
value: "徽县",
leaf: false,
level: 7,
},
{
label: "两当县",
value: "两当县",
leaf: false,
level: 7,
},
]
},
{
label: "甘南藏族自治州",
value: "甘南藏族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "合作市",
value: "合作市",
leaf: false,
level: 7,
},
{
label: "临潭县",
value: "临潭县",
leaf: false,
level: 7,
},
{
label: "卓尼县",
value: "卓尼县",
leaf: false,
level: 7,
},
{
label: "舟曲县",
value: "舟曲县",
leaf: false,
level: 7,
},
{
label: "迭部县",
value: "迭部县",
leaf: false,
level: 7,
},
{
label: "玛曲县",
value: "玛曲县",
leaf: false,
level: 7,
},
{
label: "碌曲县",
value: "碌曲县",
leaf: false,
level: 7,
},
{
label: "夏河县",
value: "夏河县",
leaf: false,
level: 7,
},
]
},
{
label: "临夏回族自治州",
value: "临夏回族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "临夏市",
value: "临夏市",
leaf: false,
level: 7,
},
{
label: "临夏县",
value: "临夏县",
leaf: false,
level: 7,
},
{
label: "康乐县",
value: "康乐县",
leaf: false,
level: 7,
},
{
label: "永靖县",
value: "永靖县",
leaf: false,
level: 7,
},
{
label: "广河县",
value: "广河县",
leaf: false,
level: 7,
},
{
label: "和政县",
value: "和政县",
leaf: false,
level: 7,
},
{
label: "东乡族自治县",
value: "东乡族自治县",
leaf: false,
level: 7,
},
{
label: "积石山保安族东乡族撒拉族自治县",
value: "积石山保安族东乡族撒拉族自治县",
leaf: false,
level: 7,
},
]
}
                  ]

                  },
                  {
                  label: "青海省",
                  value: "青海省",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "西宁市",
value: "西宁市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "城东区",
value: "城东区",
leaf: false,
level: 7,
},
{
label: "城中区",
value: "城中区",
leaf: false,
level: 7,
},
{
label: "城西区",
value: "城西区",
leaf: false,
level: 7,
},
{
label: "城北区",
value: "城北区",
leaf: false,
level: 7,
},
{
label: "湟中区",
value: "湟中区",
leaf: false,
level: 7,
},
{
label: "湟源县",
value: "湟源县",
leaf: false,
level: 7,
},
{
label: "大通回族土族自治县",
value: "大通回族土族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "海东市",
value: "海东市",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "乐都区",
value: "乐都区",
leaf: false,
level: 7,
},
{
label: "平安区",
value: "平安区",
leaf: false,
level: 7,
},
{
label: "民和回族土族自治县",
value: "民和回族土族自治县",
leaf: false,
level: 7,
},
{
label: "互助土族自治县",
value: "互助土族自治县",
leaf: false,
level: 7,
},
{
label: "化隆回族自治县",
value: "化隆回族自治县",
leaf: false,
level: 7,
},
{
label: "循化撒拉族自治县",
value: "循化撒拉族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "海北藏族自治州",
value: "海北藏族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "门源回族自治县",
value: "门源回族自治县",
leaf: false,
level: 7,
},
{
label: "祁连县",
value: "祁连县",
leaf: false,
level: 7,
},
{
label: "海晏县",
value: "海晏县",
leaf: false,
level: 7,
},
{
label: "刚察县",
value: "刚察县",
leaf: false,
level: 7,
}
]
},
{
label: "黄南藏族自治州",
value: "黄南藏族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "同仁市",
value: "同仁市",
leaf: false,
level: 7,
},
{
label: "尖扎县",
value: "尖扎县",
leaf: false,
level: 7,
},
{
label: "泽库县",
value: "泽库县",
leaf: false,
level: 7,
},
{
label: "河南蒙古族自治县",
value: "河南蒙古族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "海南藏族自治州",
value: "海南藏族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "共和县",
value: "共和县",
leaf: false,
level: 7,
},
{
label: "贵德县",
value: "贵德县",
leaf: false,
level: 7,
},
{
label: "贵南县",
value: "贵南县",
leaf: false,
level: 7,
},
{
label: "同德县",
value: "同德县",
leaf: false,
level: 7,
},
{
label: "兴海县",
value: "兴海县",
leaf: false,
level: 7,
}
]
},
{
label: "果洛藏族自治州",
value: "果洛藏族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "玛沁县",
value: "玛沁县",
leaf: false,
level: 7,
},
{
label: "班玛县",
value: "班玛县",
leaf: false,
level: 7,
},
{
label: "甘德县",
value: "甘德县",
leaf: false,
level: 7,
},
{
label: "达日县",
value: "达日县",
leaf: false,
level: 7,
},
{
label: "久治县",
value: "久治县",
leaf: false,
level: 7,
},
{
label: "玛多县",
value: "玛多县",
leaf: false,
level: 7,
}
]
},
{
label: "玉树藏族自治州",
value: "玉树藏族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "玉树市",
value: "玉树市",
leaf: false,
level: 7,
},
{
label: "杂多县",
value: "杂多县",
leaf: false,
level: 7,
},
{
label: "称多县",
value: "称多县",
leaf: false,
level: 7,
},
{
label: "治多县",
value: "治多县",
leaf: false,
level: 7,
},
{
label: "囊谦县",
value: "囊谦县",
leaf: false,
level: 7,
},
{
label: "曲麻莱县",
value: "曲麻莱县",
leaf: false,
level: 7,
}
]
},
{
label: "海西蒙古族藏族自治州",
value: "海西蒙古族藏族自治州",
leaf: false,
level: 6,
children: [
   {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "格尔木市",
value: "格尔木市",
leaf: false,
level: 7,
},
{
label: "德令哈市",
value: "德令哈市",
leaf: false,
level: 7,
},
{
label: "茫崖市",
value: "茫崖市",
leaf: false,
level: 7,
},
{
label: "都兰县",
value: "都兰县",
leaf: false,
level: 7,
},
{
label: "乌兰县",
value: "乌兰县",
leaf: false,
level: 7,
},
{
label: "天峻县",
value: "天峻县",
leaf: false,
level: 7,
}
]
}
                  ]

                  },
                  {
                  label: "宁夏回族自治区",
                  value: "宁夏回族自治区",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "银川市",
value: "银川市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "兴庆区",
value: "兴庆区",
leaf: false,
level: 7,
},
{
label: "西夏区",
value: "西夏区",
leaf: false,
level: 7,
},
{
label: "金凤区",
value: "金凤区",
leaf: false,
level: 7,
},
{
label: "永宁县",
value: "永宁县",
leaf: false,
level: 7,
},
{
label: "贺兰县",
value: "贺兰县",
leaf: false,
level: 7,
},
{
label: "灵武市",
value: "灵武市",
leaf: false,
level: 7,
},

]
},
{
label: "石嘴山市",
value: "石嘴山市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "大武口区",
value: "大武口区",
leaf: false,
level: 7,
},
{
label: "惠农区",
value: "惠农区",
leaf: false,
level: 7,
},
{
label: "平罗县",
value: "平罗县",
leaf: false,
level: 7,
},
]
},
{
label: "吴忠市",
value: "吴忠市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "利通区",
value: "利通区",
leaf: false,
level: 7,
},
{
label: "红寺堡区",
value: "红寺堡区",
leaf: false,
level: 7,
},
{
label: "青铜峡市",
value: "青铜峡市",
leaf: false,
level: 7,
},
{
label: "盐池县",
value: "盐池县",
leaf: false,
level: 7,
},
{
label: "同心县",
value: "同心县",
leaf: false,
level: 7,
},
]
},
{
label: "固原市",
value: "固原市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "原州区",
value: "原州区",
leaf: false,
level: 7,
},
{
label: "西吉县",
value: "西吉县",
leaf: false,
level: 7,
},
{
label: "隆德县",
value: "隆德县",
leaf: false,
level: 7,
},
{
label: "泾源县",
value: "泾源县",
leaf: false,
level: 7,
},
{
label: "彭阳县",
value: "彭阳县",
leaf: false,
level: 7,
},
]
},
{
label: "中卫市",
value: "中卫市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "沙坡头区",
value: "沙坡头区",
leaf: false,
level: 7,
},
{
label: "中宁县",
value: "中宁县",
leaf: false,
level: 7,
},
{
label: "海原县",
value: "海原县",
leaf: false,
level: 7,
},
]
}
                  ]

                  },
                  {
                  label: "新疆维吾尔自治区",
                  value: "新疆维吾尔自治区",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "乌鲁木齐市",
value: "乌鲁木齐市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "天山区",
value: "天山区",
leaf: false,
level: 7,
},
{
label: "沙依巴克区",
value: "沙依巴克区",
leaf: false,
level: 7,
},
{
label: "新市区",
value: "新市区",
leaf: false,
level: 7,
},
{
label: "水磨沟区",
value: "水磨沟区",
leaf: false,
level: 7,
},
{
label: "头屯河区",
value: "头屯河区",
leaf: false,
level: 7,
},
{
label: "达坂城区",
value: "达坂城区",
leaf: false,
level: 7,
},
{
label: "米东区",
value: "米东区",
leaf: false,
level: 7,
},
{
label: "乌鲁木齐县",
value: "乌鲁木齐县",
leaf: false,
level: 7,
}
]
},
{
label: "克拉玛依市",
value: "克拉玛依市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "克拉玛依区",
value: "克拉玛依区",
leaf: false,
level: 7,
},
{
label: "独山子区",
value: "独山子区",
leaf: false,
level: 7,
},
{
label: "白碱滩区",
value: "白碱滩区",
leaf: false,
level: 7,
},
{
label: "乌尔禾区",
value: "乌尔禾区",
leaf: false,
level: 7,
}

]
},
{
label: "吐鲁番市",
value: "吐鲁番市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "高昌区",
value: "高昌区",
leaf: false,
level: 7,
},
{
label: "鄯善县",
value: "鄯善县",
leaf: false,
level: 7,
},
{
label: "托克逊县",
value: "托克逊县",
leaf: false,
level: 7,
}

]
},
{
label: "哈密市",
value: "哈密市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "伊州区",
value: "伊州区",
leaf: false,
level: 7,
},
{
label: "巴里坤哈萨克自治县",
value: "巴里坤哈萨克自治县",
leaf: false,
level: 7,
},
{
label: "伊吾县",
value: "伊吾县",
leaf: false,
level: 7,
}
]
},
{
label: "阿克苏地区",
value: "阿克苏地区",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "阿克苏市",
value: "阿克苏市",
leaf: false,
level: 7,
},
{
label: "温宿县",
value: "温宿县",
leaf: false,
level: 7,
},
{
label: "库车市",
value: "库车市",
leaf: false,
level: 7,
},
{
label: "沙雅县",
value: "沙雅县",
leaf: false,
level: 7,
},
{
label: "新和县",
value: "新和县",
leaf: false,
level: 7,
},
{
label: "拜城县",
value: "拜城县",
leaf: false,
level: 7,
},
{
label: "乌什县",
value: "乌什县",
leaf: false,
level: 7,
},
{
label: "阿瓦提县",
value: "阿瓦提县",
leaf: false,
level: 7,
},
{
label: "柯坪县",
value: "柯坪县",
leaf: false,
level: 7,
}

]
},
{
label: "喀什地区",
value: "喀什地区",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "喀什市",
value: "喀什市",
leaf: false,
level: 7,
},
{
label: "疏附县",
value: "疏附县",
leaf: false,
level: 7,
},
{
label: "疏勒县",
value: "疏勒县",
leaf: false,
level: 7,
},
{
label: "英吉沙县",
value: "英吉沙县",
leaf: false,
level: 7,
},
{
label: "泽普县",
value: "泽普县",
leaf: false,
level: 7,
},
{
label: "莎车县",
value: "莎车县",
leaf: false,
level: 7,
},
{
label: "叶城县",
value: "叶城县",
leaf: false,
level: 7,
},
{
label: "麦盖提县",
value: "麦盖提县",
leaf: false,
level: 7,
},
{
label: "岳普湖县",
value: "岳普湖县",
leaf: false,
level: 7,
},
{
label: "伽师县",
value: "伽师县",
leaf: false,
level: 7,
},
{
label: "巴楚县",
value: "巴楚县",
leaf: false,
level: 7,
},
{
label: "塔什库尔干塔吉克自治县",
value: "塔什库尔干塔吉克自治县",
leaf: false,
level: 7,
}
]
},
{
label: "和田地区",
value: "和田地区",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "和田市",
value: "和田市",
leaf: false,
level: 7,
},
{
label: "和田县",
value: "和田县",
leaf: false,
level: 7,
},
{
label: "墨玉县",
value: "墨玉县",
leaf: false,
level: 7,
},
{
label: "皮山县",
value: "皮山县",
leaf: false,
level: 7,
},
{
label: "洛浦县",
value: "洛浦县",
leaf: false,
level: 7,
},
{
label: "策勒县",
value: "策勒县",
leaf: false,
level: 7,
},
{
label: "于田县",
value: "于田县",
leaf: false,
level: 7,
},
{
label: "民丰县",
value: "民丰县",
leaf: false,
level: 7,
}
]
},
{
label: "昌吉回族自治州",
value: "昌吉回族自治州",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "昌吉市",
value: "昌吉市",
leaf: false,
level: 7,
},
{
label: "阜康市",
value: "阜康市",
leaf: false,
level: 7,
},
{
label: "呼图壁县",
value: "呼图壁县",
leaf: false,
level: 7,
},
{
label: "玛纳斯县",
value: "玛纳斯县",
leaf: false,
level: 7,
},
{
label: "奇台县",
value: "奇台县",
leaf: false,
level: 7,
},
{
label: "吉木萨尔县",
value: "吉木萨尔县",
leaf: false,
level: 7,
},
{
label: "木垒哈萨克自治县",
value: "木垒哈萨克自治县",
leaf: false,
level: 7,
}

]
},
{
label: "博尔塔拉蒙古自治州",
value: "博尔塔拉蒙古自治州",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "博乐市",
value: "博乐市",
leaf: false,
level: 7,
},
{
label: "阿拉山口市",
value: "阿拉山口市",
leaf: false,
level: 7,
},
{
label: "精河县",
value: "精河县",
leaf: false,
level: 7,
},
{
label: "温泉县",
value: "温泉县",
leaf: false,
level: 7,
}
]
},
{
label: "巴音郭楞蒙古自治州",
value: "巴音郭楞蒙古自治州",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "库尔勒市",
value: "库尔勒市",
leaf: false,
level: 7,
},
{
label: "轮台县",
value: "轮台县",
leaf: false,
level: 7,
},
{
label: "尉犁县",
value: "尉犁县",
leaf: false,
level: 7,
},
{
label: "若羌县",
value: "若羌县",
leaf: false,
level: 7,
},
{
label: "且末县",
value: "且末县",
leaf: false,
level: 7,
},
{
label: "焉耆回族自治县",
value: "焉耆回族自治县",
leaf: false,
level: 7,
},
{
label: "和静县",
value: "和静县",
leaf: false,
level: 7,
},
{
label: "和硕县",
value: "和硕县",
leaf: false,
level: 7,
},
{
label: "博湖县",
value: "博湖县",
leaf: false,
level: 7,
}
]
},
{
label: "克孜勒苏柯尔克孜自治州",
value: "克孜勒苏柯尔克孜自治州",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "阿图什市",
value: "阿图什市",
leaf: false,
level: 7,
},
{
label: "阿克陶县",
value: "阿克陶县",
leaf: false,
level: 7,
},
{
label: "阿合奇县",
value: "阿合奇县",
leaf: false,
level: 7,
},
{
label: "乌恰县",
value: "乌恰县",
leaf: false,
level: 7,
}
]
},
{
label: "伊犁哈萨克自治州",
value: "伊犁哈萨克自治州",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "伊宁市",
value: "伊宁市",
leaf: false,
level: 7,
},
{
label: "奎屯市",
value: "奎屯市",
leaf: false,
level: 7,
},
{
label: "霍尔果斯市",
value: "霍尔果斯市",
leaf: false,
level: 7,
},
{
label: "伊宁县",
value: "伊宁县",
leaf: false,
level: 7,
},
{
label: "察布查尔锡伯自治县",
value: "察布查尔锡伯自治县",
leaf: false,
level: 7,
},
{
label: "霍城县",
value: "霍城县",
leaf: false,
level: 7,
},
{
label: "巩留县",
value: "巩留县",
leaf: false,
level: 7,
},
{
label: "新源县",
value: "新源县",
leaf: false,
level: 7,
},
{
label: "昭苏县",
value: "昭苏县",
leaf: false,
level: 7,
},
{
label: "特克斯县",
value: "特克斯县",
leaf: false,
level: 7,
},
{
label: "尼勒克县",
value: "尼勒克县",
leaf: false,
level: 7,
}

]
},
{
label: "塔城地区",
value: "塔城地区",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "塔城市",
value: "塔城市",
leaf: false,
level: 7,
},
{
label: "乌苏市",
value: "乌苏市",
leaf: false,
level: 7,
},
{
label: "沙湾市",
value: "沙湾市",
leaf: false,
level: 7,
},
{
label: "额敏县",
value: "额敏县",
leaf: false,
level: 7,
},
{
label: "托里县",
value: "托里县",
leaf: false,
level: 7,
},
{
label: "和布克赛尔蒙古自治县",
value: "和布克赛尔蒙古自治县",
leaf: false,
level: 7,
}

]
},
{
label: "阿勒泰地区",
value: "阿勒泰地区",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "阿勒泰市",
value: "阿勒泰市",
leaf: false,
level: 7,
},
{
label: "布尔津县",
value: "布尔津县",
leaf: false,
level: 7,
},
{
label: "富蕴县",
value: "富蕴县",
leaf: false,
level: 7,
},
{
label: "福海县",
value: "福海县",
leaf: false,
level: 7,
},
{
label: "哈巴河县",
value: "哈巴河县",
leaf: false,
level: 7,
},
{
label: "青河县",
value: "青河县",
leaf: false,
level: 7,
},
{
label: "吉木乃县",
value: "吉木乃县",
leaf: false,
level: 7,
}
]
}
                  ]

                  }

                ],
              },
              {
                label: "西藏",
                value: "西藏",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "省级",
                    value: "省级",
                    leaf: true,
                    level: 5,
                    disabled: true,
                    display: true,
                  },
                  {
                  label: "西藏自治区",
                  value: "西藏自治区",
                  leaf: false,
                  level: 5,
                  children:[
                    {
                      label:"地级",
                      value: "地级",
                      leaf: false,
                      level: 6,
                    },
                    {
label: "拉萨市",
value: "拉萨市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "城关区",
value: "城关区",
leaf: false,
level: 7,
},
{
label: "堆龙德庆区",
value: "堆龙德庆区",
leaf: false,
level: 7,
},
{
label: "达孜区",
value: "达孜区",
leaf: false,
level: 7,
},
]
},
{
label: "日喀则市",
value: "日喀则市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "桑珠孜区",
value: "桑珠孜区",
leaf: false,
level: 7,
},
{
label: "南木林县",
value: "南木林县",
leaf: false,
level: 7,
},
{
label: "江孜县",
value: "江孜县",
leaf: false,
level: 7,
},
{
label: "定日县",
value: "定日县",
leaf: false,
level: 7,
},
{
label: "萨迦县",
value: "萨迦县",
leaf: false,
level: 7,
},
{
label: "拉孜县",
value: "拉孜县",
leaf: false,
level: 7,
},
{
label: "昂仁县",
value: "昂仁县",
leaf: false,
level: 7,
},
{
label: "谢通门县",
value: "谢通门县",
leaf: false,
level: 7,
},
{
label: "白朗县",
value: "白朗县",
leaf: false,
level: 7,
},
]
},
{
label: "昌都市",
value: "昌都市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "卡若区",
value: "卡若区",
leaf: false,
level: 7,
},
{
label: "江达县",
value: "江达县",
leaf: false,
level: 7,
},
{
label: "贡觉县",
value: "贡觉县",
leaf: false,
level: 7,
},
{
label: "类乌齐县",
value: "类乌齐县",
leaf: false,
level: 7,
},
]
},
{
label: "林芝市",
value: "林芝市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "巴宜区",
value: "巴宜区",
leaf: false,
level: 7,
},
{
label: "米林县",
value: "米林县",
leaf: false,
level: 7,
},
{
label: "墨脱县",
value: "墨脱县",
leaf: false,
level: 7,
},
]
},
{
label: "山南市",
value: "山南市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "乃东区",
value: "乃东区",
leaf: false,
level: 7,
},
{
label: "扎囊县",
value: "扎囊县",
leaf: false,
level: 7,
},
{
label: "贡嘎县",
value: "贡嘎县",
leaf: false,
level: 7,
},
{
label: "加查县",
value: "加查县",
leaf: false,
level: 7,
},
{
label: "隆子县",
value: "隆子县",
leaf: false,
level: 7,
},
]
},
{
label: "那曲市",
value: "那曲市",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "色尼区",
value: "色尼区",
leaf: false,
level: 7,
},
{
label: "安多县",
value: "安多县",
leaf: false,
level: 7,
},
{
label: "比如县",
value: "比如县",
leaf: false,
level: 7,
},
{
label: "聂荣县",
value: "聂荣县",
leaf: false,
level: 7,
},
{
label: "申扎县",
value: "申扎县",
leaf: false,
level: 7,
},
{
label: "班戈县",
value: "班戈县",
leaf: false,
level: 7,
},
{
label: "尼玛县",
value: "尼玛县",
leaf: false,
level: 7,
},
]
},
{
label: "阿里地区",
value: "阿里地区",
leaf: false,
level: 6,
children: [
     {
                            label:"县级",
                            value: "县级",
                            leaf: false,
                            level: 7,
                          },
                          {
label: "噶尔县",
value: "噶尔县",
leaf: false,
level: 7,
},
{
label: "普兰县",
value: "普兰县",
leaf: false,
level: 7,
},
{
label: "札达县",
value: "札达县",
leaf: false,
level: 7,
},
{
label: "日土县",
value: "日土县",
leaf: false,
level: 7,
},
{
label: "革吉县",
value: "革吉县",
leaf: false,
level: 7,
},
{
label: "改则县",
value: "改则县",
leaf: false,
level: 7,
},
{
label: "措勤县",
value: "措勤县",
leaf: false,
level: 7,
},
]
}
                  ]

                  }

                ],
              },
              {
                label:"其他",
                value:"其他",
                leaf:false,
                level:4,
                children:[
                  {
                    label: "台湾省",
                    value: "台湾省",
                    leaf: true,
                    level: 5,
                  },
                   {
                  label: "香港特别行政区",
                  value: "香港特别行政区",
                  leaf: true,
                  level: 5,

                  },
                  {
                  label: "澳门特别行政区",
                  value: "澳门特别行政区",
                  leaf: true,
                  level: 5,

                  }
                ]
              }
              

            ],
          },
          { label: "日本", value: "日本", leaf: false, level: 3 },
          { label: "韩国", value: "韩国", leaf: false, level: 3 },
          { label: "朝鲜", value: "朝鲜", leaf: false, level: 3 },
        ],
      },
      { label: "东南亚", value: "东南亚", leaf: false, level: 2 ,children:[
                  {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "印度尼西亚",
value: "印度尼西亚",
leaf: false,
level: 3,
},
{
label: "菲律宾",
value: "菲律宾",
leaf: false,
level: 3,
},
{
label: "马来西亚",
value: "马来西亚",
leaf: false,
level: 3,
},
{
label: "新加坡",
value: "新加坡",
leaf: false,
level: 3,
},
{
label: "泰国",
value: "泰国",
leaf: false,
level: 3,
},
{
label: "越南",
value: "越南",
leaf: false,
level: 3,
},
{
label: "老挝",
value: "老挝",
leaf: false,
level: 3,
},
{
label: "柬埔寨",
value: "柬埔寨",
leaf: false,
level: 3,
},
{
label: "缅甸",
value: "缅甸",
leaf: false,
level: 3,
},
{
label: "文莱",
value: "文莱",
leaf: false,
level: 3,
},
      ]},
      {
        label: "太平洋岛国及地区",
        value: "太平洋岛国及地区",
        leaf: false,
        level: 2,
        children:[
                    {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "斐济",
value: "斐济",
leaf: false,
level: 3,
},
{
label: "图瓦卢",
value: "图瓦卢",
leaf: false,
level: 3,
},
{
label: "基里巴斯",
value: "基里巴斯",
leaf: false,
level: 3,
},
{
label: "瑙鲁",
value: "瑙鲁",
leaf: false,
level: 3,
},
{
label: "瓦努阿图",
value: "瓦努阿图",
leaf: false,
level: 3,
},
{
label: "萨摩亚",
value: "萨摩亚",
leaf: false,
level: 3,
},
{
label: "汤加",
value: "汤加",
leaf: false,
level: 3,
},
{
label: "密克罗尼西亚联邦",
value: "密克罗尼西亚联邦",
leaf: false,
level: 3,
},
{
label: "马绍尔群岛",
value: "马绍尔群岛",
leaf: false,
level: 3,
},
        ]
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
{label: "中东", value: "中东", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },

{
label: "以色列",
value: "以色列",
leaf: false,
level: 3,
},
{
label: "伊拉克",
value: "伊拉克",
leaf: false,
level: 3,
},
{
label: "伊朗",
value: "伊朗",
leaf: false,
level: 3,
},
{
label: "叙利亚",
value: "叙利亚",
leaf: false,
level: 3,
},
{
label: "约旦",
value: "约旦",
leaf: false,
level: 3,
},
{
label: "沙特阿拉伯",
value: "沙特阿拉伯",
leaf: false,
level: 3,
},
{
label: "卡塔尔",
value: "卡塔尔",
leaf: false,
level: 3,
},
{
label: "科威特",
value: "科威特",
leaf: false,
level: 3,
},
{
label: "巴林",
value: "巴林",
leaf: false,
level: 3,
},
{
label: "阿拉伯联合酋长国",
value: "阿拉伯联合酋长国",
leaf: false,
level: 3,
},
{
label: "阿曼",
value: "阿曼",
leaf: false,
level: 3,
},
{
label: "也门",
value: "也门",
leaf: false,
level: 3,
},
]},
{ label: "北非", value: "北非", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "埃及",
value: "埃及",
leaf: false,
level: 3,
},
{
label: "利比亚",
value: "利比亚",
leaf: false,
level: 3,
},
{
label: "阿尔及利亚",
value: "阿尔及利亚",
leaf: false,
level: 3,
},
{
label: "摩洛哥",
value: "摩洛哥",
leaf: false,
level: 3,
},
{
label: "突尼斯",
value: "突尼斯",
leaf: false,
level: 3,
},
]},
{ label: "西亚", value: "西亚", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "亚美尼亚",
value: "亚美尼亚",
leaf: false,
level: 3,
},
{
label: "阿塞拜疆",
value: "阿塞拜疆",
leaf: false,
level: 3,
},
{
label: "格鲁吉亚",
value: "格鲁吉亚",
leaf: false,
level: 3,
},
{
label: "土耳其",
value: "土耳其",
leaf: false,
level: 3,
},
{
label: "阿富汗",
value: "阿富汗",
leaf: false,
level: 3,
},
]},
{ label: "西亚或中东", value: "西亚或中东", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "也门",
value: "也门",
leaf: false,
level: 3,
},
{
label: "以色列",
value: "以色列",
leaf: false,
level: 3,
},
{
label: "伊拉克",
value: "伊拉克",
leaf: false,
level: 3,
},
{
label: "伊朗",
value: "伊朗",
leaf: false,
level: 3,
},
{
label: "卡塔尔",
value: "卡塔尔",
leaf: false,
level: 3,
},
{
label: "沙特阿拉伯",
value: "沙特阿拉伯",
leaf: false,
level: 3,
},
{
label: "科威特",
value: "科威特",
leaf: false,
level: 3,
},
{
label: "巴林",
value: "巴林",
leaf: false,
level: 3,
},
{
label: "阿拉伯联合酋长国",
value: "阿拉伯联合酋长国",
leaf: false,
level: 3,
},
{
label: "阿曼",
value: "阿曼",
leaf: false,
level: 3,
},
{
label: "加沙地带",
value: "加沙地带",
leaf: false,
level: 3,
},
{
label: "西岸",
value: "西岸",
leaf: false,
level: 3,
},
{
label: "叙利亚",
value: "叙利亚",
leaf: false,
level: 3,
},
{
label: "约旦",
value: "约旦",
leaf: false,
level: 3,
},
{
label: "黎巴嫩",
value: "黎巴嫩",
leaf: false,
level: 3,
}

          
]},

    ],
  },
  { label: "北美", value: "北美地区", leaf: false, level: 1 ,children:[
    {label: "北美", value: "北美", leaf: false, level: 2 ,children:[
                {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "加拿大",
value: "加拿大",
leaf: false,
level: 3,
},
{
label: "美国",
value: "美国",
leaf: false,
level: 3,
},

    ]},
{ label: "中美洲", value: "中美洲", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "伯利兹",
value: "伯利兹",
leaf: false,
level: 3,
},
{
label: "危地马拉",
value: "危地马拉",
leaf: false,
level: 3,
},
{
label: "哥斯达黎加",
value: "哥斯达黎加",
leaf: false,
level: 3,
},
{
label: "巴拿马",
value: "巴拿马",
leaf: false,
level: 3,
},
{
label: "尼加拉瓜",
value: "尼加拉瓜",
leaf: false,
level: 3,
},
{
label: "洪都拉斯",
value: "洪都拉斯",
leaf: false,
level: 3,
},
{
label: "萨尔瓦多",
value: "萨尔瓦多",
leaf: false,
level: 3,
},
]},
  ]},
  { label: "南亚", value: "南亚地区", leaf: false, level: 1,children:[
    {label: "南亚", value: "南亚", leaf: false, level: 2 ,children:[
                {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "不丹",
value: "不丹",
leaf: false,
level: 3,
},
{
label: "印度",
value: "印度",
leaf: false,
level: 3,
},
{
label: "尼泊尔",
value: "尼泊尔",
leaf: false,
level: 3,
},
{
label: "巴基斯坦",
value: "巴基斯坦",
leaf: false,
level: 3,
},
{
label: "孟加拉国",
value: "孟加拉国",
leaf: false,
level: 3,
},
{
label: "斯里兰卡",
value: "斯里兰卡",
leaf: false,
level: 3,
},
{
label: "马尔代夫",
value: "马尔代夫",
leaf: false,
level: 3,
},
    ]},
  ] },
  {
    label: "拉丁美洲与加勒比",
    value: "拉丁美洲与加勒比地区",
    leaf: false,
    level: 1,
    children:[
      { label: "加勒比地区", value: "加勒比地区", leaf: false, level: 2 ,children:[
                  {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "伯利兹",
value: "伯利兹",
leaf: false,
level: 3,
},
{
label: "古巴",
value: "古巴",
leaf: false,
level: 3,
},
{
label: "海地",
value: "海地",
leaf: false,
level: 3,
},
{
label: "多米尼加共和国",
value: "多米尼加共和国",
leaf: false,
level: 3,
},
{
label: "牙买加",
value: "牙买加",
leaf: false,
level: 3,
},
{
label: "特立尼达和多巴哥",
value: "特立尼达和多巴哥",
leaf: false,
level: 3,
},
{
label: "巴哈马",
value: "巴哈马",
leaf: false,
level: 3,
},
{
label: "巴巴多斯",
value: "巴巴多斯",
leaf: false,
level: 3,
},
{
label: "多米尼克",
value: "多米尼克",
leaf: false,
level: 3,
},
{
label: "圣卢西亚",
value: "圣卢西亚",
leaf: false,
level: 3,
},
{
label: "圣基茨和尼维斯",
value: "圣基茨和尼维斯",
leaf: false,
level: 3,
},
{
label: "圣文森特和格林纳丁斯",
value: "圣文森特和格林纳丁斯",
leaf: false,
level: 3,
},
{
label: "安提瓜和巴布达",
value: "安提瓜和巴布达",
leaf: false,
level: 3,
},
{
label: "格林纳达",
value: "格林纳达",
leaf: false,
level: 3,
},
      ]},
{ label: "南美洲", value: "南美洲", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "乌拉圭",
value: "乌拉圭",
leaf: false,
level: 3,
},
{
label: "厄瓜多尔",
value: "厄瓜多尔",
leaf: false,
level: 3,
},
{
label: "哥伦比亚",
value: "哥伦比亚",
leaf: false,
level: 3,
},
{
label: "圭亚那",
value: "圭亚那",
leaf: false,
level: 3,
},
{
label: "委内瑞拉",
value: "委内瑞拉",
leaf: false,
level: 3,
},
{
label: "玻利维亚",
value: "玻利维亚",
leaf: false,
level: 3,
},
{
label: "秘鲁",
value: "秘鲁",
leaf: false,
level: 3,
},
{
label: "巴西",
value: "巴西",
leaf: false,
level: 3,
},
{
label: "巴拉圭",
value: "巴拉圭",
leaf: false,
level: 3,
},
{
label: "苏里南",
value: "苏里南",
leaf: false,
level: 3,
},
{
label: "智利",
value: "智利",
leaf: false,
level: 3,
},
{
label: "阿根廷",
value: "阿根廷",
leaf: false,
level: 3,
},
]},
    ]
  },
  {
    label: "撒哈拉以南非洲",
    value: "撒哈拉以南非洲地区",
    leaf: false,
    level: 1,
    children:[
      {label: "东非", value: "东非", leaf: false, level: 2 ,children:[
                  {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "埃塞俄比亚",
value: "埃塞俄比亚",
leaf: false,
level: 3,
},
{
label: "厄立特里亚",
value: "厄立特里亚",
leaf: false,
level: 3,
},
{
label: "肯尼亚",
value: "肯尼亚",
leaf: false,
level: 3,
},
{
label: "坦桑尼亚",
value: "坦桑尼亚",
leaf: false,
level: 3,
},
{
label: "卢旺达",
value: "卢旺达",
leaf: false,
level: 3,
},
{
label: "布隆迪",
value: "布隆迪",
leaf: false,
level: 3,
},
{
label: "塞舌尔",
value: "塞舌尔",
leaf: false,
level: 3,
},
{
label: "索马里",
value: "索马里",
leaf: false,
level: 3,
},

      ]},
{ label: "中非", value: "中非", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "中非共和国",
value: "中非共和国",
leaf: false,
level: 3,
},
{
label: "乍得",
value: "乍得",
leaf: false,
level: 3,
},
{
label: "喀麦隆",
value: "喀麦隆",
leaf: false,
level: 3,
},
{
label: "加蓬",
value: "加蓬",
leaf: false,
level: 3,
},
{
label: "刚果（布）",
value: "刚果（布）",
leaf: false,
level: 3,
},
{
label: "刚果（金）",
value: "刚果（金）",
leaf: false,
level: 3,
},
{
label: "圣多美和普林西比",
value: "圣多美和普林西比",
leaf: false,
level: 3,
},
{
label: "赤道几内亚",
value: "赤道几内亚",
leaf: false,
level: 3,
},

]},
{ label: "南部非洲", value: "南部非洲", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "南非",
value: "南非",
leaf: false,
level: 3,
},
{
label: "博茨瓦纳",
value: "博茨瓦纳",
leaf: false,
level: 3,
},
{
label: "纳米比亚",
value: "纳米比亚",
leaf: false,
level: 3,
},
{
label: "莱索托",
value: "莱索托",
leaf: false,
level: 3,
},
{
label: "斯威士兰",
value: "斯威士兰",
leaf: false,
level: 3,
},
{
label: "津巴布韦",
value: "津巴布韦",
leaf: false,
level: 3,
},
{
label: "莫桑比克",
value: "莫桑比克",
leaf: false,
level: 3,
},
{
label: "马拉维",
value: "马拉维",
leaf: false,
level: 3,
},
]},
{ label: "西非", value: "西非", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "尼日利亚",
value: "尼日利亚",
leaf: false,
level: 3,
},
{
label: "尼日尔",
value: "尼日尔",
leaf: false,
level: 3,
},
{
label: "马里",
value: "马里",
leaf: false,
level: 3,
},
{
label: "布基纳法索",
value: "布基纳法索",
leaf: false,
level: 3,
},
{
label: "几内亚",
value: "几内亚",
leaf: false,
level: 3,
},
{
label: "几内亚比绍",
value: "几内亚比绍",
leaf: false,
level: 3,
},
{
label: "塞内加尔",
value: "塞内加尔",
leaf: false,
level: 3,
},
{
label: "冈比亚",
value: "冈比亚",
leaf: false,
level: 3,
},
{
label: "利比里亚",
value: "利比里亚",
leaf: false,
level: 3,
},
{
label: "科特迪瓦",
value: "科特迪瓦",
leaf: false,
level: 3,
},
{
label: "加纳",
value: "加纳",
leaf: false,
level: 3,
},
{
label: "多哥",
value: "多哥",
leaf: false,
level: 3,
},
{
label: "贝宁",
value: "贝宁",
leaf: false,
level: 3,
},
{
label: "毛里塔尼亚",
value: "毛里塔尼亚",
leaf: false,
level: 3,
},

]},
    ]
  },
  { label: "欧洲与中亚", value: "欧洲与中亚地区", leaf: false, level: 1 ,children:[
{ label: "东南欧", value: "东南欧", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "塞尔维亚",
value: "塞尔维亚",
leaf: false,
level: 3,
},
{
label: "北马其顿",
value: "北马其顿",
leaf: false,
level: 3,
},
{
label: "保加利亚",
value: "保加利亚",
leaf: false,
level: 3,
},
]},
{ label: "东欧", value: "东欧", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "俄罗斯",
value: "俄罗斯",
leaf: false,
level: 3,
},
{
label: "乌克兰",
value: "乌克兰",
leaf: false,
level: 3,
},
{
label: "白俄罗斯",
value: "白俄罗斯",
leaf: false,
level: 3,
},
{
label: "摩尔多瓦",
value: "摩尔多瓦",
leaf: false,
level: 3,
},
]},
{ label: "中亚", value: "中亚", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "哈萨克斯坦",
value: "哈萨克斯坦",
leaf: false,
level: 3,
},
{
label: "乌兹别克斯坦",
value: "乌兹别克斯坦",
leaf: false,
level: 3,
},
{
label: "土库曼斯坦",
value: "土库曼斯坦",
leaf: false,
level: 3,
},
{
label: "塔吉克斯坦",
value: "塔吉克斯坦",
leaf: false,
level: 3,
},
{
label: "吉尔吉斯斯坦",
value: "吉尔吉斯斯坦",
leaf: false,
level: 3,
},
]},
{ label: "中欧", value: "中欧", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "德国",
value: "德国",
leaf: false,
level: 3,
},
{
label: "波兰",
value: "波兰",
leaf: false,
level: 3,
},
{
label: "捷克共和国",
value: "捷克共和国",
leaf: false,
level: 3,
},
{
label: "斯洛伐克",
value: "斯洛伐克",
leaf: false,
level: 3,
},
{
label: "匈牙利",
value: "匈牙利",
leaf: false,
level: 3,
},
{
label: "奥地利",
value: "奥地利",
leaf: false,
level: 3,
},
{
label: "列支敦士登",
value: "列支敦士登",
leaf: false,
level: 3,
},
{
label: "瑞士",
value: "瑞士",
leaf: false,
level: 3,
},
]},
{ label: "北欧", value: "北欧", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "丹麦",
value: "丹麦",
leaf: false,
level: 3,
},
{
label: "挪威",
value: "挪威",
leaf: false,
level: 3,
},
{
label: "瑞典",
value: "瑞典",
leaf: false,
level: 3,
},
{
label: "芬兰",
value: "芬兰",
leaf: false,
level: 3,
},
{
label: "冰岛",
value: "冰岛",
leaf: false,
level: 3,
},
]},
{ label: "南欧", value: "南欧", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "意大利",
value: "意大利",
leaf: false,
level: 3,
},
{
label: "西班牙",
value: "西班牙",
leaf: false,
level: 3,
},
{
label: "葡萄牙",
value: "葡萄牙",
leaf: false,
level: 3,
},
{
label: "希腊",
value: "希腊",
leaf: false,
level: 3,
},
{
label: "马耳他",
value: "马耳他",
leaf: false,
level: 3,
},
{
label: "圣马力诺",
value: "圣马力诺",
leaf: false,
level: 3,
},
{
label: "摩纳哥",
value: "摩纳哥",
leaf: false,
level: 3,
},
{
label: "斯洛文尼亚",
value: "斯洛文尼亚",
leaf: false,
level: 3,
},
{
label: "克罗地亚",
value: "克罗地亚",
leaf: false,
level: 3,
},
{
label: "波斯尼亚和黑塞哥维那",
value: "波斯尼亚和黑塞哥维那",
leaf: false,
level: 3,
},
{
label: "黑山",
value: "黑山",
leaf: false,
level: 3,
},
{
label: "阿尔巴尼亚",
value: "阿尔巴尼亚",
leaf: false,
level: 3,
},
]},
{ label: "西欧", value: "西欧", leaf: false, level: 2 ,children:[
            {
            label: "国家",
            value: "国家",
            leaf: true,
            level: 3,
            disabled: true,
            display: true,
          },
          {
label: "英国",
value: "英国",
leaf: false,
level: 3,
},
{
label: "法国",
value: "法国",
leaf: false,
level: 3,
},
{
label: "荷兰",
value: "荷兰",
leaf: false,
level: 3,
},
{
label: "比利时",
value: "比利时",
leaf: false,
level: 3,
},
{
label: "卢森堡",
value: "卢森堡",
leaf: false,
level: 3,
},
{
label: "爱尔兰",
value: "爱尔兰",
leaf: false,
level: 3,
},
]},
  ]},
];


const original_options_en = [
  {
    label: "Regions",
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
        level: 2,
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
            level: 3,
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
                label: "Grid",
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
                    level: 5,
                    disabled: true,
                    display: true,
                  },
                  {
                    label: "Henan",
                    value: "河南省",
                    leaf: false,
                    level: 5,
                    children: [
                                {
        label: "Prefecture",
        value: "地级",
        leaf: false,
        level: 6
                                },
                                {
        label: "Xinyang",
        value: "信阳市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Huangchuan",
            value: "潢川县",
            leaf: false,
            level: 7
          },
          {
            label: "Luoshan",
            value: "罗山县",
            leaf: false,
            level: 7
          },
          {
            label: "Guangshan",
            value: "光山县",
            leaf: false,
            level: 7
          },
          {
            label: "Xinxian",
            value: "新县",
            leaf: false,
            level: 7
          },
          {
            label: "Shangcheng",
            value: "商城县",
            leaf: false,
            level: 7
          },
          {
            label: "Gushi",
            value: "固始县",
            leaf: false,
            level: 7
          },
          {
            label: "Huaibin",
            value: "淮滨县",
            leaf: false,
            level: 7
          },
          {
            label: "Xixian",
            value: "息县",
            leaf: false,
            level: 7
          }
        ]
                                },
      {
        label: "Zhengzhou",
        value: "郑州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Zhongmu",
            value: "中牟县",
            leaf: false,
            level: 7
          },
          {
            label: "Zhongyuan",
            value: "中原区",
            leaf: false,
            level: 7
          },
          {
            label: "Erqi",
            value: "二七区",
            leaf: false,
            level: 7
          },
          {
            label: "Gongyi",
            value: "巩义市",
            leaf: false,
            level: 7
          },
          {
            label: "Xingyang",
            value: "荥阳市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Kaifeng",
        value: "开封市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Longting",
            value: "龙亭区",
            leaf: false,
            level: 7
          },
          {
            label: "Shunhehuizu",
            value: "顺河回族区",
            leaf: false,
            level: 7
          },
          {
            label: "Gulou",
            value: "鼓楼区",
            leaf: false,
            level: 7
          },
          {
            label: "Yuwangtai",
            value: "禹王台区",
            leaf: false,
            level: 7
          },
          {
            label: "Xiangfu",
            value: "祥符区",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Luoyang",
        value: "洛阳市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Laocheng",
            value: "老城区",
            leaf: false,
            level: 7
          },
          {
            label: "Xigong",
            value: "西工区",
            leaf: false,
            level: 7
          },
          {
            label: "Chanhehuizu",
            value: "瀍河回族区",
            leaf: false,
            level: 7
          },
          {
            label: "Jianxi",
            value: "涧西区",
            leaf: false,
            level: 7
          },
          {
            label: "Luolong",
            value: "洛龙区",
            leaf: false,
            level: 7
          },
          {
            label: "Mengjin",
            value: "孟津区",
            leaf: false,
            level: 7
          },
          {
            label: "Yanshi",
            value: "偃师区",
            leaf: false,
            level: 7
          },
          {
            label: "Xinan",
            value: "新安县",
            leaf: false,
            level: 7
          },
          {
            label: "Luanchuan",
            value: "栾川县",
            leaf: false,
            level: 7
          },
          {
            label: "Songxian",
            value: "嵩县",
            leaf: false,
            level: 7
          },
          {
            label: "Ruyang",
            value: "汝阳县",
            leaf: false,
            level: 7
          },
          {
            label: "Yiyang",
            value: "宜阳县",
            leaf: false,
            level: 7
          },
          {
            label: "Luoning",
            value: "洛宁县",
            leaf: false,
            level: 7
          },
          {
            label: "Yichuan",
            value: "伊川县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Pingdingshan",
        value: "平顶山市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Xinhua",
            value: "新华区",
            leaf: false,
            level: 7
          },
          {
            label: "Weidong",
            value: "卫东区",
            leaf: false,
            level: 7
          },
          {
            label: "Shilong",
            value: "石龙区",
            leaf: false,
            level: 7
          },
          {
            label: "Zhanhe",
            value: "湛河区",
            leaf: false,
            level: 7
          },
          {
            label: "Baofeng",
            value: "宝丰县",
            leaf: false,
            level: 7
          },
          {
            label: "Yexian",
            value: "叶县",
            leaf: false,
            level: 7
          },
          {
            label: "Lushan",
            value: "鲁山县",
            leaf: false,
            level: 7
          },
          {
            label: "Jiaxian",
            value: "郏县",
            leaf: false,
            level: 7
          },
          {
            label: "Wugang",
            value: "舞钢市",
            leaf: false,
            level: 7
          },
          {
            label: "Ruzhou",
            value: "汝州市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Anyang",
        value: "安阳市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Wenfeng",
            value: "文峰区",
            leaf: false,
            level: 7
          },
          {
            label: "Beiguan",
            value: "北关区",
            leaf: false,
            level: 7
          },
          {
            label: "Yindu",
            value: "殷都区",
            leaf: false,
            level: 7
          },
          {
            label: "Longan",
            value: "龙安区",
            leaf: false,
            level: 7
          },
          {
            label: "Anyang",
            value: "安阳县",
            leaf: false,
            level: 7
          },
          {
            label: "Tangyin",
            value: "汤阴县",
            leaf: false,
            level: 7
          },
          {
            label: "Huaxian",
            value: "滑县",
            leaf: false,
            level: 7
          },
          {
            label: "Neihuang",
            value: "内黄县",
            leaf: false,
            level: 7
          },
          {
            label: "Linzhou",
            value: "林州市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Hebi",
        value: "鹤壁市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Heshan",
            value: "鹤山区",
            leaf: false,
            level: 7
          },
          {
            label: "Shancheng",
            value: "山城区",
            leaf: false,
            level: 7
          },
          {
            label: "Qibin",
            value: "淇滨区",
            leaf: false,
            level: 7
          },
          {
            label: "Xunxian",
            value: "浚县",
            leaf: false,
            level: 7
          },
          {
            label: "Qixian",
            value: "淇县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Xinxiang",
        value: "新乡市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Hongqi",
            value: "红旗区",
            leaf: false,
            level: 7
          },
          {
            label: "Weibin",
            value: "卫滨区",
            leaf: false,
            level: 7
          },
          {
            label: "Fengquan",
            value: "凤泉区",
            leaf: false,
            level: 7
          },
          {
            label: "Muye",
            value: "牧野区",
            leaf: false,
            level: 7
          },
          {
            label: "Xinxiang",
            value: "新乡县",
            leaf: false,
            level: 7
          },
          {
            label: "Huojia",
            value: "获嘉县",
            leaf: false,
            level: 7
          },
          {
            label: "Yuanyang",
            value: "原阳县",
            leaf: false,
            level: 7
          },
          {
            label: "Yanjin",
            value: "延津县",
            leaf: false,
            level: 7
          },
          {
            label: "Fengqiu",
            value: "封丘县",
            leaf: false,
            level: 7
          },
          {
            label: "Changyuan",
            value: "长垣市",
            leaf: false,
            level: 7
          },
          {
            label: "Weihui",
            value: "卫辉市",
            leaf: false,
            level: 7
          },
          {
            label: "Huixian",
            value: "辉县市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Jiaozuo",
        value: "焦作市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Jiefang",
            value: "解放区",
            leaf: false,
            level: 7
          },
          {
            label: "Zhongzhan",
            value: "中站区",
            leaf: false,
            level: 7
          },
          {
            label: "Macun",
            value: "马村区",
            leaf: false,
            level: 7
          },
          {
            label: "Shanyang",
            value: "山阳区",
            leaf: false,
            level: 7
          },
          {
            label: "Xiuwu",
            value: "修武县",
            leaf: false,
            level: 7
          },
          {
            label: "Boai",
            value: "博爱县",
            leaf: false,
            level: 7
          },
          {
            label: "Wuzhi",
            value: "武陟县",
            leaf: false,
            level: 7
          },
          {
            label: "Wenxian",
            value: "温县",
            leaf: false,
            level: 7
          },
          {
            label: "Qinyang",
            value: "沁阳市",
            leaf: false,
            level: 7
          },
          {
            label: "Mengzhou",
            value: "孟州市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Puyang",
        value: "濮阳市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Hualong",
            value: "华龙区",
            leaf: false,
            level: 7
          },
          {
            label: "Qingfeng",
            value: "清丰县",
            leaf: false,
            level: 7
          },
          {
            label: "Nanle",
            value: "南乐县",
            leaf: false,
            level: 7
          },
          {
            label: "Fanxian",
            value: "范县",
            leaf: false,
            level: 7
          },
          {
            label: "Taiqian",
            value: "台前县",
            leaf: false,
            level: 7
          },
          {
            label: "Puyang",
            value: "濮阳县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Xuchang",
        value: "许昌市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Weidu",
            value: "魏都区",
            leaf: false,
            level: 7
          },
          {
            label: "Jianan",
            value: "建安区",
            leaf: false,
            level: 7
          },
          {
            label: "Yanling",
            value: "鄢陵县",
            leaf: false,
            level: 7
          },
          {
            label: "Xiangcheng",
            value: "襄城县",
            leaf: false,
            level: 7
          },
          {
            label: "Yuzhou",
            value: "禹州市",
            leaf: false,
            level: 7
          },
          {
            label: "Changge",
            value: "长葛市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Luohe",
        value: "漯河市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Yuanhui",
            value: "源汇区",
            leaf: false,
            level: 7
          },
          {
            label: "Yancheng",
            value: "郾城区",
            leaf: false,
            level: 7
          },
          {
            label: "Shaoling",
            value: "召陵区",
            leaf: false,
            level: 7
          },
          {
            label: "Wuyang",
            value: "舞阳县",
            leaf: false,
            level: 7
          },
          {
            label: "Linying",
            value: "临颍县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Sanmenxia",
        value: "三门峡市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Hubin",
            value: "湖滨区",
            leaf: false,
            level: 7
          },
          {
            label: "Shanzhou",
            value: "陕州区",
            leaf: false,
            level: 7
          },
          {
            label: "Mianchi",
            value: "渑池县",
            leaf: false,
            level: 7
          },
          {
            label: "Lushi",
            value: "卢氏县",
            leaf: false,
            level: 7
          },
          {
            label: "Yima",
            value: "义马市",
            leaf: false,
            level: 7
          },
          {
            label: "Lingbao",
            value: "灵宝市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Nanyang",
        value: "南阳市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Wancheng",
            value: "宛城区",
            leaf: false,
            level: 7
          },
          {
            label: "Wolong",
            value: "卧龙区",
            leaf: false,
            level: 7
          },
          {
            label: "Nanzhao",
            value: "南召县",
            leaf: false,
            level: 7
          },
          {
            label: "Fangcheng",
            value: "方城县",
            leaf: false,
            level: 7
          },
          {
            label: "Xixia",
            value: "西峡县",
            leaf: false,
            level: 7
          },
          {
            label: "Zhenping",
            value: "镇平县",
            leaf: false,
            level: 7
          },
          {
            label: "Neixiang",
            value: "内乡县",
            leaf: false,
            level: 7
          },
          {
            label: "Xichuan",
            value: "淅川县",
            leaf: false,
            level: 7
          },
          {
            label: "Sheqi",
            value: "社旗县",
            leaf: false,
            level: 7
          },
          {
            label: "Tanghe",
            value: "唐河县",
            leaf: false,
            level: 7
          },
          {
            label: "Xinye",
            value: "新野县",
            leaf: false,
            level: 7
          },
          {
            label: "Tongbai",
            value: "桐柏县",
            leaf: false,
            level: 7
          },
          {
            label: "Dengzhou",
            value: "邓州市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Shangqiu",
        value: "商丘市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Liangyuan",
            value: "梁园区",
            leaf: false,
            level: 7
          },
          {
            label: "Suiyang",
            value: "睢阳区",
            leaf: false,
            level: 7
          },
          {
            label: "Minquan",
            value: "民权县",
            leaf: false,
            level: 7
          },
          {
            label: "Suixian",
            value: "睢县",
            leaf: false,
            level: 7
          },
          {
            label: "Ningling",
            value: "宁陵县",
            leaf: false,
            level: 7
          },
          {
            label: "Zhecheng",
            value: "柘城县",
            leaf: false,
            level: 7
          },
          {
            label: "Yucheng",
            value: "虞城县",
            leaf: false,
            level: 7
          },
          {
            label: "Xiayi",
            value: "夏邑县",
            leaf: false,
            level: 7
          },
          {
            label: "Yongcheng",
            value: "永城市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Zhoukou",
        value: "周口市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Chuanhui",
            value: "川汇区",
            leaf: false,
            level: 7
          },
          {
            label: "Huaiyang",
            value: "淮阳区",
            leaf: false,
            level: 7
          },
          {
            label: "Fugou",
            value: "扶沟县",
            leaf: false,
            level: 7
          },
          {
            label: "Xihua",
            value: "西华县",
            leaf: false,
            level: 7
          },
          {
            label: "Shangshui",
            value: "商水县",
            leaf: false,
            level: 7
          },
          {
            label: "Shenqiu",
            value: "沈丘县",
            leaf: false,
            level: 7
          },
          {
            label: "Dancheng",
            value: "郸城县",
            leaf: false,
            level: 7
          },
          {
            label: "Taikang",
            value: "太康县",
            leaf: false,
            level: 7
          },
          {
            label: "Luyi",
            value: "鹿邑县",
            leaf: false,
            level: 7
          },
          {
            label: "Xiangcheng",
            value: "项城市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Zhumadian",
        value: "驻马店市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Yicheng",
            value: "驿城区",
            leaf: false,
            level: 7
          },
          {
            label: "Xiping",
            value: "西平县",
            leaf: false,
            level: 7
          },
          {
            label: "Shangcai",
            value: "上蔡县",
            leaf: false,
            level: 7
          },
          {
            label: "Pingyu",
            value: "平舆县",
            leaf: false,
            level: 7
          },
          {
            label: "Zhengyang",
            value: "正阳县",
            leaf: false,
            level: 7
          },
          {
            label: "Queshan",
            value: "确山县",
            leaf: false,
            level: 7
          },
          {
            label: "Biyang",
            value: "泌阳县",
            leaf: false,
            level: 7
          },
          {
            label: "Suiping",
            value: "遂平县",
            leaf: false,
            level: 7
          },
          {
            label: "Runan",
            value: "汝南县",
            leaf: false,
            level: 7
          },
          {
            label: "Xincai",
            value: "新蔡县",
            leaf: false,
            level: 7
          }
        ]
      }
    ]
  },
  {
    label: "Hubei",
    value: "湖北省",
    leaf: false,
    level: 5,
    children: [
      {
        label: "Prefecture",
        value: "地级",
        leaf: false,
        level: 6
      },
      {
        label: "Wuhan",
        value: "武汉市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Jiangan",
            value: "江岸区",
            leaf: false,
            level: 7
          },
          {
            label: "Jianghan",
            value: "江汉区",
            leaf: false,
            level: 7
          },
          {
            label: "Qiaokou",
            value: "硚口区",
            leaf: false,
            level: 7
          },
          {
            label: "Hanyang",
            value: "汉阳区",
            leaf: false,
            level: 7
          },
          {
            label: "Wuchang",
            value: "武昌区",
            leaf: false,
            level: 7
          },
          {
            label: "Qingshan",
            value: "青山区",
            leaf: false,
            level: 7
          },
          {
            label: "Hongshan",
            value: "洪山区",
            leaf: false,
            level: 7
          },
          {
            label: "Dongxihu",
            value: "东西湖区",
            leaf: false,
            level: 7
          },
          {
            label: "Hannan",
            value: "汉南区",
            leaf: false,
            level: 7
          },
          {
            label: "Caidian",
            value: "蔡甸区",
            leaf: false,
            level: 7
          },
          {
            label: "Jiangxia",
            value: "江夏区",
            leaf: false,
            level: 7
          },
          {
            label: "Huangpi",
            value: "黄陂区",
            leaf: false,
            level: 7
          },
          {
            label: "Xinzhou",
            value: "新洲区",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Huangshi",
        value: "黄石市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Huangshigang",
            value: "黄石港区",
            leaf: false,
            level: 7
          },
          {
            label: "Xisaishan",
            value: "西塞山区",
            leaf: false,
            level: 7
          },
          {
            label: "Xialu",
            value: "下陆区",
            leaf: false,
            level: 7
          },
          {
            label: "Tieshan",
            value: "铁山区",
            leaf: false,
            level: 7
          },
          {
            label: "Yangxin",
            value: "阳新县",
            leaf: false,
            level: 7
          },
          {
            label: "Daye",
            value: "大冶市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Shiyan",
        value: "十堰市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Maojian",
            value: "茅箭区",
            leaf: false,
            level: 7
          },
          {
            label: "Zhangwan",
            value: "张湾区",
            leaf: false,
            level: 7
          },
          {
            label: "Yunyang",
            value: "郧阳区",
            leaf: false,
            level: 7
          },
          {
            label: "Yunxi",
            value: "郧西县",
            leaf: false,
            level: 7
          },
          {
            label: "Zhushan",
            value: "竹山县",
            leaf: false,
            level: 7
          },
          {
            label: "Zhuxi",
            value: "竹溪县",
            leaf: false,
            level: 7
          },
          {
            label: "Fangxian",
            value: "房县",
            leaf: false,
            level: 7
          },
          {
            label: "Danjiangkou",
            value: "丹江口市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Yichang",
        value: "宜昌市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Xiling",
            value: "西陵区",
            leaf: false,
            level: 7
          },
          {
            label: "Wujiagang",
            value: "伍家岗区",
            leaf: false,
            level: 7
          },
          {
            label: "Dianjun",
            value: "点军区",
            leaf: false,
            level: 7
          },
          {
            label: "Xiaoting",
            value: "猇亭区",
            leaf: false,
            level: 7
          },
          {
            label: "Yiling",
            value: "夷陵区",
            leaf: false,
            level: 7
          },
          {
            label: "Yuanan",
            value: "远安县",
            leaf: false,
            level: 7
          },
          {
            label: "Xingshan",
            value: "兴山县",
            leaf: false,
            level: 7
          },
          {
            label: "Zigui",
            value: "秭归县",
            leaf: false,
            level: 7
          },
          {
            label: "Changyangtujiazu",
            value: "长阳土家族自治县",
            leaf: false,
            level: 7
          },
          {
            label: "Wufengtujiazu",
            value: "五峰土家族自治县",
            leaf: false,
            level: 7
          },
          {
            label: "Yidu",
            value: "宜都市",
            leaf: false,
            level: 7
          },
          {
            label: "Dangyang",
            value: "当阳市",
            leaf: false,
            level: 7
          },
          {
            label: "Zhijiang",
            value: "枝江市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Xiangyang",
        value: "襄阳市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Xiangcheng",
            value: "襄城区",
            leaf: false,
            level: 7
          },
          {
            label: "Fancheng",
            value: "樊城区",
            leaf: false,
            level: 7
          },
          {
            label: "Xiangzhou",
            value: "襄州区",
            leaf: false,
            level: 7
          },
          {
            label: "Nanzhang",
            value: "南漳县",
            leaf: false,
            level: 7
          },
          {
            label: "Gucheng",
            value: "谷城县",
            leaf: false,
            level: 7
          },
          {
            label: "Baokang",
            value: "保康县",
            leaf: false,
            level: 7
          },
          {
            label: "Laohekou",
            value: "老河口市",
            leaf: false,
            level: 7
          },
          {
            label: "Zaoyang",
            value: "枣阳市",
            leaf: false,
            level: 7
          },
          {
            label: "Yicheng",
            value: "宜城市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Ezhou",
        value: "鄂州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Liangzihu",
            value: "梁子湖区",
            leaf: false,
            level: 7
          },
          {
            label: "Huarong",
            value: "华容区",
            leaf: false,
            level: 7
          },
          {
            label: "Echeng",
            value: "鄂城区",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Jingmen",
        value: "荆门市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Dongbao",
            value: "东宝区",
            leaf: false,
            level: 7
          },
          {
            label: "Duodao",
            value: "掇刀区",
            leaf: false,
            level: 7
          },
          {
            label: "Jingshan",
            value: "京山市",
            leaf: false,
            level: 7
          },
          {
            label: "Shayang",
            value: "沙洋县",
            leaf: false,
            level: 7
          },
          {
            label: "Zhongxiang",
            value: "钟祥市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Xiaogan",
        value: "孝感市",
        leaf: false,
        level: 6,
        children: [
           {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Xiaonan",
            value: "孝南区",
            leaf: false,
            level: 7
          },
          {
            label: "Xiaochang",
            value: "孝昌县",
            leaf: false,
            level: 7
          },
          {
            label: "Dawu",
            value: "大悟县",
            leaf: false,
            level: 7
          },
          {
            label: "Yunmeng",
            value: "云梦县",
            leaf: false,
            level: 7
          },
          {
            label: "Yingcheng",
            value: "应城市",
            leaf: false,
            level: 7
          },
          {
            label: "Anlu",
            value: "安陆市",
            leaf: false,
            level: 7
          },
          {
            label: "Hanchuan",
            value: "汉川市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Jingzhou",
        value: "荆州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Shashi",
            value: "沙市区",
            leaf: false,
            level: 7
          },
          {
            label: "Jingzhou",
            value: "荆州区",
            leaf: false,
            level: 7
          },
          {
            label: "Gongan",
            value: "公安县",
            leaf: false,
            level: 7
          },
          {
            label: "Jianli",
            value: "监利市",
            leaf: false,
            level: 7
          },
          {
            label: "Jiangling",
            value: "江陵县",
            leaf: false,
            level: 7
          },
          {
            label: "Shishou",
            value: "石首市",
            leaf: false,
            level: 7
          },
          {
            label: "Honghu",
            value: "洪湖市",
            leaf: false,
            level: 7
          },
          {
            label: "Songzi",
            value: "松滋市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Huanggang",
        value: "黄冈市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Huangzhou",
            value: "黄州区",
            leaf: false,
            level: 7
          },
          {
            label: "Tuanfeng",
            value: "团风县",
            leaf: false,
            level: 7
          },
          {
            label: "Hongan",
            value: "红安县",
            leaf: false,
            level: 7
          },
          {
            label: "Luotian",
            value: "罗田县",
            leaf: false,
            level: 7
          },
          {
            label: "Yingshan",
            value: "英山县",
            leaf: false,
            level: 7
          },
          {
            label: "Xishui",
            value: "浠水县",
            leaf: false,
            level: 7
          },
          {
            label: "Qichun",
            value: "蕲春县",
            leaf: false,
            level: 7
          },
          {
            label: "Huangmei",
            value: "黄梅县",
            leaf: false,
            level: 7
          },
          {
            label: "Macheng",
            value: "麻城市",
            leaf: false,
            level: 7
          },
          {
            label: "Wuxue",
            value: "武穴市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Xianning",
        value: "咸宁市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Xianan",
            value: "咸安区",
            leaf: false,
            level: 7
          },
          {
            label: "Jiayu",
            value: "嘉鱼县",
            leaf: false,
            level: 7
          },
          {
            label: "Tongcheng",
            value: "通城县",
            leaf: false,
            level: 7
          },
          {
            label: "Chongyang",
            value: "崇阳县",
            leaf: false,
            level: 7
          },
          {
            label: "Tongshan",
            value: "通山县",
            leaf: false,
            level: 7
          },
          {
            label: "Chibi",
            value: "赤壁市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Suizhou",
        value: "随州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Zengdu",
            value: "曾都区",
            leaf: false,
            level: 7
          },
          {
            label: "Suixian",
            value: "随县",
            leaf: false,
            level: 7
          },
          {
            label: "Guangshui",
            value: "广水市",
            leaf: false,
            level: 7
          }
        ]
      }
    ]
  },
{
  label: "Hunan",
  value: "湖南省",
  leaf: false,
  level: 5,
  children: [
    {
      label: "Prefecture",
      value: "地级",
      leaf: false,
      level: 6
    },
    {
      label: "Changsha",
      value: "长沙市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Furong",
          value: "芙蓉区",
          leaf: false,
          level: 7
        },
        {
          label: "Tianxin",
          value: "天心区",
          leaf: false,
          level: 7
        },
        {
          label: "Yuelu",
          value: "岳麓区",
          leaf: false,
          level: 7
        },
        {
          label: "Yuhua",
          value: "雨花区",
          leaf: false,
          level: 7
        },
        {
          label: "Wangcheng",
          value: "望城区",
          leaf: false,
          level: 7
        },
        {
          label: "Changsha",
          value: "长沙县",
          leaf: false,
          level: 7
        },
        {
          label: "Ningxiang",
          value: "宁乡市",
          leaf: false,
          level: 7
        },
        {
          label: "Liuyang",
          value: "浏阳市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Zhuzhou",
      value: "株洲市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Tianyuan",
          value: "天元区",
          leaf: false,
          level: 7
        },
        {
          label: "Hetang",
          value: "荷塘区",
          leaf: false,
          level: 7
        },
        {
          label: "Lusong",
          value: "芦淞区",
          leaf: false,
          level: 7
        },
        {
          label: "Shifeng",
          value: "石峰区",
          leaf: false,
          level: 7
        },
        {
          label: "Lukou",
          value: "渌口区",
          leaf: false,
          level: 7
        },
        {
          label: "Youxian",
          value: "攸县",
          leaf: false,
          level: 7
        },
        {
          label: "Chaling",
          value: "茶陵县",
          leaf: false,
          level: 7
        },
        {
          label: "Yanling",
          value: "炎陵县",
          leaf: false,
          level: 7
        },
        {
          label: "Liling",
          value: "醴陵市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Xiangtan",
      value: "湘潭市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yuhu",
          value: "雨湖区",
          leaf: false,
          level: 7
        },
        {
          label: "Yuetang",
          value: "岳塘区",
          leaf: false,
          level: 7
        },
        {
          label: "Xiangtan",
          value: "湘潭县",
          leaf: false,
          level: 7
        },
        {
          label: "Xiangxiang",
          value: "湘乡市",
          leaf: false,
          level: 7
        },
        {
          label: "Shaoshan",
          value: "韶山市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Hengyang",
      value: "衡阳市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Zhuhui",
          value: "珠晖区",
          leaf: false,
          level: 7
        },
        {
          label: "Yanfeng",
          value: "雁峰区",
          leaf: false,
          level: 7
        },
        {
          label: "Shigu",
          value: "石鼓区",
          leaf: false,
          level: 7
        },
        {
          label: "Zhengxiang",
          value: "蒸湘区",
          leaf: false,
          level: 7
        },
        {
          label: "Nanyue",
          value: "南岳区",
          leaf: false,
          level: 7
        },
        {
          label: "Hengyang",
          value: "衡阳县",
          leaf: false,
          level: 7
        },
        {
          label: "Hengnan",
          value: "衡南县",
          leaf: false,
          level: 7
        },
        {
          label: "Hengshan",
          value: "衡山县",
          leaf: false,
          level: 7
        },
        {
          label: "Hengdong",
          value: "衡东县",
          leaf: false,
          level: 7
        },
        {
          label: "Qidong",
          value: "祁东县",
          leaf: false,
          level: 7
        },
        {
          label: "Leiyang",
          value: "耒阳市",
          leaf: false,
          level: 7
        },
        {
          label: "Changning",
          value: "常宁市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Shaoyang",
      value: "邵阳市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Shuangqing",
          value: "双清区",
          leaf: false,
          level: 7
        },
        {
          label: "Daxiang",
          value: "大祥区",
          leaf: false,
          level: 7
        },
        {
          label: "Beita",
          value: "北塔区",
          leaf: false,
          level: 7
        },
        {
          label: "Shaodong",
          value: "邵东市",
          leaf: false,
          level: 7
        },
        {
          label: "Xinshao",
          value: "新邵县",
          leaf: false,
          level: 7
        },
        {
          label: "Shaoyang",
          value: "邵阳县",
          leaf: false,
          level: 7
        },
        {
          label: "Longhui",
          value: "隆回县",
          leaf: false,
          level: 7
        },
        {
          label: "Dongkou",
          value: "洞口县",
          leaf: false,
          level: 7
        },
        {
          label: "Suining",
          value: "绥宁县",
          leaf: false,
          level: 7
        },
        {
          label: "Xinning",
          value: "新宁县",
          leaf: false,
          level: 7
        },
        {
          label: "Chengbumiaozu",
          value: "城步苗族自治县",
          leaf: false,
          level: 7
        },
        {
          label: "Wugang",
          value: "武冈市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Yueyang",
      value: "岳阳市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yueyanglou",
          value: "岳阳楼区",
          leaf: false,
          level: 7
        },
        {
          label: "Yunxi",
          value: "云溪区",
          leaf: false,
          level: 7
        },
        {
          label: "Junshan",
          value: "君山区",
          leaf: false,
          level: 7
        },
        {
          label: "Yueyang",
          value: "岳阳县",
          leaf: false,
          level: 7
        },
        {
          label: "Huarong",
          value: "华容县",
          leaf: false,
          level: 7
        },
        {
          label: "Xiangyin",
          value: "湘阴县",
          leaf: false,
          level: 7
        },
        {
          label: "Pingjiang",
          value: "平江县",
          leaf: false,
          level: 7
        },
        {
          label: "Miluo",
          value: "汨罗市",
          leaf: false,
          level: 7
        },
        {
          label: "Linxiang",
          value: "临湘市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Changde",
      value: "常德市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Wuling",
          value: "武陵区",
          leaf: false,
          level: 7
        },
        {
          label: "Dingcheng",
          value: "鼎城区",
          leaf: false,
          level: 7
        },
        {
          label: "Anxiang",
          value: "安乡县",
          leaf: false,
          level: 7
        },
        {
          label: "Hanshou",
          value: "汉寿县",
          leaf: false,
          level: 7
        },
        {
          label: "Lixian",
          value: "澧县",
          leaf: false,
          level: 7
        },
        {
          label: "Linli",
          value: "临澧县",
          leaf: false,
          level: 7
        },
        {
          label: "Taoyuan",
          value: "桃源县",
          leaf: false,
          level: 7
        },
        {
          label: "Shimen",
          value: "石门县",
          leaf: false,
          level: 7
        },
        {
          label: "Jinshi",
          value: "津市市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Zhangjiajie",
      value: "张家界市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yongding",
          value: "永定区",
          leaf: false,
          level: 7
        },
        {
          label: "Wulingyuan",
          value: "武陵源区",
          leaf: false,
          level: 7
        },
        {
          label: "Cili",
          value: "慈利县",
          leaf: false,
          level: 7
        },
        {
          label: "Sangzhi",
          value: "桑植县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Yiyang",
      value: "益阳市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Ziyang",
          value: "资阳区",
          leaf: false,
          level: 7
        },
        {
          label: "Heshan",
          value: "赫山区",
          leaf: false,
          level: 7
        },
        {
          label: "Nanxian",
          value: "南县",
          leaf: false,
          level: 7
        },
        {
          label: "Taojiang",
          value: "桃江县",
          leaf: false,
          level: 7
        },
        {
          label: "Anhua",
          value: "安化县",
          leaf: false,
          level: 7
        },
        {
          label: "Yuanjiang",
          value: "沅江市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Chenzhou",
      value: "郴州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Beihu",
          value: "北湖区",
          leaf: false,
          level: 7
        },
        {
          label: "Suxian",
          value: "苏仙区",
          leaf: false,
          level: 7
        },
        {
          label: "Guiyang",
          value: "桂阳县",
          leaf: false,
          level: 7
        },
        {
          label: "Yizhang",
          value: "宜章县",
          leaf: false,
          level: 7
        },
        {
          label: "Yongxing",
          value: "永兴县",
          leaf: false,
          level: 7
        },
        {
          label: "Jiahe",
          value: "嘉禾县",
          leaf: false,
          level: 7
        },
        {
          label: "Linwu",
          value: "临武县",
          leaf: false,
          level: 7
        },
        {
          label: "Rucheng",
          value: "汝城县",
          leaf: false,
          level: 7
        },
        {
          label: "Guidong",
          value: "桂东县",
          leaf: false,
          level: 7
        },
        {
          label: "Anren",
          value: "安仁县",
          leaf: false,
          level: 7
        },
        {
          label: "Zixing",
          value: "资兴市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Yongzhou",
      value: "永州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Lingling",
          value: "零陵区",
          leaf: false,
          level: 7
        },
        {
          label: "Lengshuitan",
          value: "冷水滩区",
          leaf: false,
          level: 7
        },
        {
          label: "Qiyang",
          value: "祁阳县",
          leaf: false,
          level: 7
        },
        {
          label: "Dongan",
          value: "东安县",
          leaf: false,
          level: 7
        },
        {
          label: "Shuangpai",
          value: "双牌县",
          leaf: false,
          level: 7
        },
        {
          label: "Daoxian",
          value: "道县",
          leaf: false,
          level: 7
        },
        {
          label: "Jiangyong",
          value: "江永县",
          leaf: false,
          level: 7
        },
        {
          label: "Ningyuan",
          value: "宁远县",
          leaf: false,
          level: 7
        },
        {
          label: "Lanshan",
          value: "蓝山县",
          leaf: false,
          level: 7
        },
        {
          label: "Xintian",
          value: "新田县",
          leaf: false,
          level: 7
        },
        {
          label: "Jianghuayaozu",
          value: "江华瑶族自治县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Huaihua",
      value: "怀化市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Hecheng",
          value: "鹤城区",
          leaf: false,
          level: 7
        },
        {
          label: "Zhongfang",
          value: "中方县",
          leaf: false,
          level: 7
        },
        {
          label: "Yuanling",
          value: "沅陵县",
          leaf: false,
          level: 7
        },
        {
          label: "Chenxi",
          value: "辰溪县",
          leaf: false,
          level: 7
        },
        {
          label: "Xupu",
          value: "溆浦县",
          leaf: false,
          level: 7
        },
        {
          label: "Huitong",
          value: "会同县",
          leaf: false,
          level: 7
        },
        {
          label: "Mayangmiaozu",
          value: "麻阳苗族自治县",
          leaf: false,
          level: 7
        },
        {
          label: "Xinhuangdongzu",
          value: "新晃侗族自治县",
          leaf: false,
          level: 7
        },
        {
          label: "Zhijiangdongzu",
          value: "芷江侗族自治县",
          leaf: false,
          level: 7
        },
        {
          label: "Jingzhoumiaozuanddongzu",
          value: "靖州苗族侗族自治县",
          leaf: false,
          level: 7
        },
        {
          label: "Tongdaodongzu",
          value: "通道侗族自治县",
          leaf: false,
          level: 7
        },
        {
          label: "Hongjiang",
          value: "洪江市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Loudi",
      value: "娄底市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Louxing",
          value: "娄星区",
          leaf: false,
          level: 7
        },
        {
          label: "Shuangfeng",
          value: "双峰县",
          leaf: false,
          level: 7
        },
        {
          label: "Xinhua",
          value: "新化县",
          leaf: false,
          level: 7
        },
        {
          label: "Lengshuijiang",
          value: "冷水江市",
          leaf: false,
          level: 7
        },
        {
          label: "Lianyuan",
          value: "涟源市",
          leaf: false,
          level: 7
        }
      ]
    }
  ]
},
{
  label: "Jiangxi",
  value: "江西省",
  leaf: false,
  level: 5,
  children: [
    {
      label: "Prefecture",
      value: "地级",
      leaf: false,
      level: 6
    },
    {
      label: "Nanchang",
      value: "南昌市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Donghu",
          value: "东湖区",
          leaf: false,
          level: 7
        },
        {
          label: "Xihu",
          value: "西湖区",
          leaf: false,
          level: 7
        },
        {
          label: "Qingyunpu",
          value: "青云谱区",
          leaf: false,
          level: 7
        },
        {
          label: "Xinjian",
          value: "新建区",
          leaf: false,
          level: 7
        },
        {
          label: "Nanchang",
          value: "南昌县",
          leaf: false,
          level: 7
        },
        {
          label: "Jinxian",
          value: "进贤县",
          leaf: false,
          level: 7
        },
        {
          label: "Anyi",
          value: "安义县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Jiujiang",
      value: "九江市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Xunyang",
          value: "浔阳区",
          leaf: false,
          level: 7
        },
        {
          label: "Lianxi",
          value: "濂溪区",
          leaf: false,
          level: 7
        },
        {
          label: "Chaisang",
          value: "柴桑区",
          leaf: false,
          level: 7
        },
        {
          label: "Wuning",
          value: "武宁县",
          leaf: false,
          level: 7
        },
        {
          label: "Xiushui",
          value: "修水县",
          leaf: false,
          level: 7
        },
        {
          label: "Yongxiu",
          value: "永修县",
          leaf: false,
          level: 7
        },
        {
          label: "Dean",
          value: "德安县",
          leaf: false,
          level: 7
        },
        {
          label: "Duchang",
          value: "都昌县",
          leaf: false,
          level: 7
        },
        {
          label: "Hukou",
          value: "湖口县",
          leaf: false,
          level: 7
        },
        {
          label: "Pengze",
          value: "彭泽县",
          leaf: false,
          level: 7
        },
        {
          label: "Ruichang",
          value: "瑞昌市",
          leaf: false,
          level: 7
        },
        {
          label: "Lushan",
          value: "庐山市",
          leaf: false,
          level: 7
        },
        {
          label: "Gongqingcheng",
          value: "共青城市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Jingdezhen",
      value: "景德镇市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Changjiang",
          value: "昌江区",
          leaf: false,
          level: 7
        },
        {
          label: "Zhushan",
          value: "珠山区",
          leaf: false,
          level: 7
        },
        {
          label: "Leping",
          value: "乐平市",
          leaf: false,
          level: 7
        },
        {
          label: "Fuliang",
          value: "浮梁县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Pingxiang",
      value: "萍乡市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Anyuan",
          value: "安源区",
          leaf: false,
          level: 7
        },
        {
          label: "Xiangdong",
          value: "湘东区",
          leaf: false,
          level: 7
        },
        {
          label: "Lianhua",
          value: "莲花县",
          leaf: false,
          level: 7
        },
        {
          label: "Shangli",
          value: "上栗县",
          leaf: false,
          level: 7
        },
        {
          label: "Luxi",
          value: "芦溪县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Xinyu",
      value: "新余市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yushui",
          value: "渝水区",
          leaf: false,
          level: 7
        },
        {
          label: "Fenyi",
          value: "分宜县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Yingtan",
      value: "鹰潭市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yuehu",
          value: "月湖区",
          leaf: false,
          level: 7
        },
        {
          label: "Yujiang",
          value: "余江区",
          leaf: false,
          level: 7
        },
        {
          label: "Guixi",
          value: "贵溪市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Ganzhou",
      value: "赣州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Zhanggong",
          value: "章贡区",
          leaf: false,
          level: 7
        },
        {
          label: "Nankang",
          value: "南康区",
          leaf: false,
          level: 7
        },
        {
          label: "Ganxian",
          value: "赣县区",
          leaf: false,
          level: 7
        },
        {
          label: "Ruijin",
          value: "瑞金市",
          leaf: false,
          level: 7
        },
        {
          label: "Xinfeng",
          value: "信丰县",
          leaf: false,
          level: 7
        },
        {
          label: "Dayu",
          value: "大余县",
          leaf: false,
          level: 7
        },
        {
          label: "Shangyou",
          value: "上犹县",
          leaf: false,
          level: 7
        },
        {
          label: "Chongyi",
          value: "崇义县",
          leaf: false,
          level: 7
        },
        {
          label: "Anyuan",
          value: "安远县",
          leaf: false,
          level: 7
        },
        {
          label: "Longnan",
          value: "龙南市",
          leaf: false,
          level: 7
        },
        {
          label: "Dingnan",
          value: "定南县",
          leaf: false,
          level: 7
        },
        {
          label: "Quannan",
          value: "全南县",
          leaf: false,
          level: 7
        },
        {
          label: "Ningdu",
          value: "宁都县",
          leaf: false,
          level: 7
        },
        {
          label: "Yudu",
          value: "于都县",
          leaf: false,
          level: 7
        },
        {
          label: "Xingguo",
          value: "兴国县",
          leaf: false,
          level: 7
        },
        {
          label: "Huichang",
          value: "会昌县",
          leaf: false,
          level: 7
        },
        {
          label: "Xunwu",
          value: "寻乌县",
          leaf: false,
          level: 7
        },
        {
          label: "Shicheng",
          value: "石城县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Yichun",
      value: "宜春市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yuanzhou",
          value: "袁州区",
          leaf: false,
          level: 7
        },
        {
          label: "Fengcheng",
          value: "丰城市",
          leaf: false,
          level: 7
        },
        {
          label: "Zhangshu",
          value: "樟树市",
          leaf: false,
          level: 7
        },
        {
          label: "Gaoan",
          value: "高安市",
          leaf: false,
          level: 7
        },
        {
          label: "Fengxin",
          value: "奉新县",
          leaf: false,
          level: 7
        },
        {
          label: "Wanzai",
          value: "万载县",
          leaf: false,
          level: 7
        },
        {
          label: "Shanggao",
          value: "上高县",
          leaf: false,
          level: 7
        },
        {
          label: "Yifeng",
          value: "宜丰县",
          leaf: false,
          level: 7
        },
        {
          label: "Jingan",
          value: "靖安县",
          leaf: false,
          level: 7
        },
        {
          label: "Tonggu",
          value: "铜鼓县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Shangrao",
      value: "上饶市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Xinzhou",
          value: "信州区",
          leaf: false,
          level: 7
        },
        {
          label: "Guangfeng",
          value: "广丰区",
          leaf: false,
          level: 7
        },
        {
          label: "Guangxin",
          value: "广信区",
          leaf: false,
          level: 7
        },
        {
          label: "Dexing",
          value: "德兴市",
          leaf: false,
          level: 7
        },
        {
          label: "Yushan",
          value: "玉山县",
          leaf: false,
          level: 7
        },
        {
          label: "Yanshan",
          value: "铅山县",
          leaf: false,
          level: 7
        },
        {
          label: "Hengfeng",
          value: "横峰县",
          leaf: false,
          level: 7
        },
        {
          label: "Yiyang",
          value: "弋阳县",
          leaf: false,
          level: 7
        },
        {
          label: "Yugan",
          value: "余干县",
          leaf: false,
          level: 7
        },
        {
          label: "Poyang",
          value: "鄱阳县",
          leaf: false,
          level: 7
        },
        {
          label: "Wannian",
          value: "万年县",
          leaf: false,
          level: 7
        },
        {
          label: "Wuyuan",
          value: "婺源县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Jian",
      value: "吉安市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Jizhou",
          value: "吉州区",
          leaf: false,
          level: 7
        },
        {
          label: "Qingyuan",
          value: "青原区",
          leaf: false,
          level: 7
        },
        {
          label: "Jinggangshan",
          value: "井冈山市",
          leaf: false,
          level: 7
        },
        {
          label: "Jian",
          value: "吉安县",
          leaf: false,
          level: 7
        },
        {
          label: "Jishui",
          value: "吉水县",
          leaf: false,
          level: 7
        },
        {
          label: "Xiajiang",
          value: "峡江县",
          leaf: false,
          level: 7
        },
        {
          label: "Xingan",
          value: "新干县",
          leaf: false,
          level: 7
        },
        {
          label: "Yongfeng",
          value: "永丰县",
          leaf: false,
          level: 7
        },
        {
          label: "Taihe",
          value: "泰和县",
          leaf: false,
          level: 7
        },
        {
          label: "Suichuan",
          value: "遂川县",
          leaf: false,
          level: 7
        },
        {
          label: "Wanan",
          value: "万安县",
          leaf: false,
          level: 7
        },
        {
          label: "Anfu",
          value: "安福县",
          leaf: false,
          level: 7
        },
        {
          label: "Yongxin",
          value: "永新县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Fuzhou",
      value: "抚州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Linchuan",
          value: "临川区",
          leaf: false,
          level: 7
        },
        {
          label: "Dongxiang",
          value: "东乡区",
          leaf: false,
          level: 7
        },
        {
          label: "Nancheng",
          value: "南城县",
          leaf: false,
          level: 7
        },
        {
          label: "Lichuan",
          value: "黎川县",
          leaf: false,
          level: 7
        },
        {
          label: "Nanfeng",
          value: "南丰县",
          leaf: false,
          level: 7
        },
        {
          label: "Chongren",
          value: "崇仁县",
          leaf: false,
          level: 7
        },
        {
          label: "Lean",
          value: "乐安县",
          leaf: false,
          level: 7
        },
        {
          label: "Yihuang",
          value: "宜黄县",
          leaf: false,
          level: 7
        },
        {
          label: "Jinxi",
          value: "金溪县",
          leaf: false,
          level: 7
        },
        {
          label: "Zixi",
          value: "资溪县",
          leaf: false,
          level: 7
        },
        {
          label: "Guangchang",
          value: "广昌县",
          leaf: false,
          level: 7
        }
      ]
    }
  ]
}
      
                ],
              },
              {
                label: "Northeast",
                value: "东北",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "Province",
                    value: "省级",
                    leaf: true,
                    level: 5,
                    disabled: true,
                    display: true,
                  },
{
label: "Heilongjiang",
value: "黑龙江省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Haerbin",
value: "哈尔滨市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Daoli",
value: "道里区",
leaf: false,
level: 7
},
{
label: "Daowai",
value: "道外区",
leaf: false,
level: 7
},
{
label: "Nangang",
value: "南岗区",
leaf: false,
level: 7
},
{
label: "Xiangfang",
value: "香坊区",
leaf: false,
level: 7
},
{
label: "Pingfang",
value: "平房区",
leaf: false,
level: 7
},
{
label: "Songbei",
value: "松北区",
leaf: false,
level: 7
},
{
label: "Hulan",
value: "呼兰区",
leaf: false,
level: 7
},
{
label: "Acheng",
value: "阿城区",
leaf: false,
level: 7
},
{
label: "Shuangcheng",
value: "双城区",
leaf: false,
level: 7
},
{
label: "Yilan",
value: "依兰县",
leaf: false,
level: 7
},
{
label: "Fangzheng",
value: "方正县",
leaf: false,
level: 7
},
{
label: "Binxian",
value: "宾县",
leaf: false,
level: 7
},
{
label: "Bayan",
value: "巴彦县",
leaf: false,
level: 7
},
{
label: "Mulan",
value: "木兰县",
leaf: false,
level: 7
},
{
label: "Tonghe",
value: "通河县",
leaf: false,
level: 7
},
{
label: "Yanshou",
value: "延寿县",
leaf: false,
level: 7
}
]
},
{
label: "Qiqihaer",
value: "齐齐哈尔市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Longsha",
value: "龙沙区",
leaf: false,
level: 7
},
{
label: "Jianhua",
value: "建华区",
leaf: false,
level: 7
},
{
label: "Tiefeng",
value: "铁锋区",
leaf: false,
level: 7
},
{
label: "Angangxi",
value: "昂昂溪区",
leaf: false,
level: 7
},
{
label: "Fulaerji",
value: "富拉尔基区",
leaf: false,
level: 7
},
{
label: "Nianzishan",
value: "碾子山区",
leaf: false,
level: 7
},
{
label: "Meilisidawoerzu",
value: "梅里斯达斡尔族区",
leaf: false,
level: 7
},
{
label: "Longjiang",
value: "龙江县",
leaf: false,
level: 7
},
{
label: "Yian",
value: "依安县",
leaf: false,
level: 7
},
{
label: "Tailai",
value: "泰来县",
leaf: false,
level: 7
},
{
label: "Gannan",
value: "甘南县",
leaf: false,
level: 7
},
{
label: "Fuyu",
value: "富裕县",
leaf: false,
level: 7
},
{
label: "Keshan",
value: "克山县",
leaf: false,
level: 7
},
{
label: "Kedong",
value: "克东县",
leaf: false,
level: 7
},
{
label: "Baiquan",
value: "拜泉县",
leaf: false,
level: 7
}
]
},
{
label: "Jixi",
value: "鸡西市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Jiguan",
value: "鸡冠区",
leaf: false,
level: 7
},
{
label: "Hengshan",
value: "恒山区",
leaf: false,
level: 7
},
{
label: "Didao",
value: "滴道区",
leaf: false,
level: 7
},
{
label: "Lishu",
value: "梨树区",
leaf: false,
level: 7
},
{
label: "Chengzihe",
value: "城子河区",
leaf: false,
level: 7
},
{
label: "Mashan",
value: "麻山区",
leaf: false,
level: 7
},
{
label: "Jidong",
value: "鸡东县",
leaf: false,
level: 7
},
{
label: "Hulin",
value: "虎林市",
leaf: false,
level: 7
},
{
label: "Mishan",
value: "密山市",
leaf: false,
level: 7
}
]
},
{
label: "Hegang",
value: "鹤岗市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xiangyang",
value: "向阳区",
leaf: false,
level: 7
},
{
label: "Gongnong",
value: "工农区",
leaf: false,
level: 7
},
{
label: "Nanshan",
value: "南山区",
leaf: false,
level: 7
},
{
label: "Xingan",
value: "兴安区",
leaf: false,
level: 7
},
{
label: "Dongshan",
value: "东山区",
leaf: false,
level: 7
},
{
label: "Xingshan",
value: "兴山区",
leaf: false,
level: 7
},
{
label: "Luobei",
value: "萝北县",
leaf: false,
level: 7
},
{
label: "Suibin",
value: "绥滨县",
leaf: false,
level: 7
}
]
},
{
label: "Shuangyashan",
value: "双鸭山市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Jianshan",
value: "尖山区",
leaf: false,
level: 7
},
{
label: "Lingdong",
value: "岭东区",
leaf: false,
level: 7
},
{
label: "Sifangtai",
value: "四方台区",
leaf: false,
level: 7
},
{
label: "Baoshan",
value: "宝山区",
leaf: false,
level: 7
},
{
label: "Jixian",
value: "集贤县",
leaf: false,
level: 7
},
{
label: "Youyi",
value: "友谊县",
leaf: false,
level: 7
},
{
label: "Baoqing",
value: "宝清县",
leaf: false,
level: 7
},
{
label: "Raohe",
value: "饶河县",
leaf: false,
level: 7
}
]
},
{
label: "Daqing",
value: "大庆市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Saertu",
value: "萨尔图区",
leaf: false,
level: 7
},
{
label: "Longfeng",
value: "龙凤区",
leaf: false,
level: 7
},
{
label: "Ranghulu",
value: "让胡路区",
leaf: false,
level: 7
},
{
label: "Honggang",
value: "红岗区",
leaf: false,
level: 7
},
{
label: "Zhaozhou",
value: "肇州县",
leaf: false,
level: 7
},
{
label: "Zhaoyuan",
value: "肇源县",
leaf: false,
level: 7
},
{
label: "Lindian",
value: "林甸县",
leaf: false,
level: 7
},
{
label: "Duerbotemengguzu",
value: "杜尔伯特蒙古族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Yichun",
value: "伊春市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yimei",
value: "伊美区",
leaf: false,
level: 7
},
{
label: "Wucui",
value: "乌翠区",
leaf: false,
level: 7
},
{
label: "Youhao",
value: "友好区",
leaf: false,
level: 7
},
{
label: "Jinlin",
value: "金林区",
leaf: false,
level: 7
},
{
label: "Tangwang",
value: "汤旺县",
leaf: false,
level: 7
},
{
label: "Fenglin",
value: "丰林县",
leaf: false,
level: 7
},
{
label: "Daqingshan",
value: "大箐山县",
leaf: false,
level: 7
},
{
label: "Nancha",
value: "南岔县",
leaf: false,
level: 7
},
{
label: "Jiayin",
value: "嘉荫县",
leaf: false,
level: 7
},
{
label: "Tieli",
value: "铁力市",
leaf: false,
level: 7
}
]
},
{
label: "Jiamusi",
value: "佳木斯市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xiangyang",
value: "向阳区",
leaf: false,
level: 7
},
{
label: "Qianjin",
value: "前进区",
leaf: false,
level: 7
},
{
label: "Dongfeng",
value: "东风区",
leaf: false,
level: 7
},
{
label: "Jiaoqu",
value: "郊区",
leaf: false,
level: 7
},
{
label: "Huanan",
value: "桦南县",
leaf: false,
level: 7
},
{
label: "Huachuan",
value: "桦川县",
leaf: false,
level: 7
},
{
label: "Tangyuan",
value: "汤原县",
leaf: false,
level: 7
},
{
label: "Tongjiang",
value: "同江市",
leaf: false,
level: 7
},
{
label: "Fujin",
value: "富锦市",
leaf: false,
level: 7
},
{
label: "Fuyuan",
value: "抚远市",
leaf: false,
level: 7
}
]
},
{
label: "Qitaihe",
value: "七台河市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xinxing",
value: "新兴区",
leaf: false,
level: 7
},
{
label: "Taoshan",
value: "桃山区",
leaf: false,
level: 7
},
{
label: "Qiezihe",
value: "茄子河区",
leaf: false,
level: 7
},
{
label: "Boli",
value: "勃利县",
leaf: false,
level: 7
}
]
},
{
label: "Mudanjiang",
value: "牡丹江市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Dongan",
value: "东安区",
leaf: false,
level: 7
},
{
label: "Yangming",
value: "阳明区",
leaf: false,
level: 7
},
{
label: "Aimin",
value: "爱民区",
leaf: false,
level: 7
},
{
label: "Xian",
value: "西安区",
leaf: false,
level: 7
},
{
label: "Linkou",
value: "林口县",
leaf: false,
level: 7
},
{
label: "Suifenhe",
value: "绥芬河市",
leaf: false,
level: 7
},
{
label: "Hailin",
value: "海林市",
leaf: false,
level: 7
},
{
label: "Ningan",
value: "宁安市",
leaf: false,
level: 7
},
{
label: "Muling",
value: "穆棱市",
leaf: false,
level: 7
},
{
label: "Dongning",
value: "东宁市",
leaf: false,
level: 7
}
]
},
{
label: "Heihe",
value: "黑河市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Aihui",
value: "爱辉区",
leaf: false,
level: 7
},
{
label: "Nenjiang",
value: "嫩江市",
leaf: false,
level: 7
},
{
label: "Xunke",
value: "逊克县",
leaf: false,
level: 7
},
{
label: "Sunwu",
value: "孙吴县",
leaf: false,
level: 7
},
{
label: "Beian",
value: "北安市",
leaf: false,
level: 7
}
]
},
{
label: "Suihua",
value: "绥化市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Beilin",
value: "北林区",
leaf: false,
level: 7
},
{
label: "Wangkui",
value: "望奎县",
leaf: false,
level: 7
},
{
label: "Lanxi",
value: "兰西县",
leaf: false,
level: 7
},
{
label: "Qinggang",
value: "青冈县",
leaf: false,
level: 7
},
{
label: "Qingan",
value: "庆安县",
leaf: false,
level: 7
},
{
label: "Mingshui",
value: "明水县",
leaf: false,
level: 7
},
{
label: "Suiling",
value: "绥棱县",
leaf: false,
level: 7
},
{
label: "Anda",
value: "安达市",
leaf: false,
level: 7
},
{
label: "Zhaodong",
value: "肇东市",
leaf: false,
level: 7
},
{
label: "Hailun",
value: "海伦市",
leaf: false,
level: 7
}
]
},
{
label: "Daxinganling",
value: "大兴安岭地区",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Huma",
value: "呼玛县",
leaf: false,
level: 7
},
{
label: "Tahe",
value: "塔河县",
leaf: false,
level: 7
},
{
label: "Mohe",
value: "漠河市",
leaf: false,
level: 7
}
]
}
]
},
{
label: "Jilin",
value: "吉林省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Changchun",
value: "长春市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Nanguan",
value: "南关区",
leaf: false,
level: 7
},
{
label: "Kuancheng",
value: "宽城区",
leaf: false,
level: 7
},
{
label: "Chaoyang",
value: "朝阳区",
leaf: false,
level: 7
},
{
label: "Erdao",
value: "二道区",
leaf: false,
level: 7
},
{
label: "Lüyuan",
value: "绿园区",
leaf: false,
level: 7
},
{
label: "Shuangyang",
value: "双阳区",
leaf: false,
level: 7
},
{
label: "Jiutai",
value: "九台区",
leaf: false,
level: 7
},
{
label: "Nongan",
value: "农安县",
leaf: false,
level: 7
},
{
label: "Yushu",
value: "榆树市",
leaf: false,
level: 7
},
{
label: "Dehui",
value: "德惠市",
leaf: false,
level: 7
},
{
label: "Gongzhuling",
value: "公主岭市",
leaf: false,
level: 7
}
]
},
{
label: "Jilin",
value: "吉林市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Chuanying",
value: "船营区",
leaf: false,
level: 7
},
{
label: "Fengman",
value: "丰满区",
leaf: false,
level: 7
},
{
label: "Yongji",
value: "永吉县",
leaf: false,
level: 7
},
{
label: "Jiaohe",
value: "蛟河市",
leaf: false,
level: 7
},
{
label: "Huadian",
value: "桦甸市",
leaf: false,
level: 7
},
{
label: "Shulan",
value: "舒兰市",
leaf: false,
level: 7
},
{
label: "Panshi",
value: "磐石市",
leaf: false,
level: 7
}
]
},
{
label: "Siping",
value: "四平市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Tiexi",
value: "铁西区",
leaf: false,
level: 7
},
{
label: "Tiedong",
value: "铁东区",
leaf: false,
level: 7
},
{
label: "Lishu",
value: "梨树县",
leaf: false,
level: 7
},
{
label: "Yitongmanzu",
value: "伊通满族自治县",
leaf: false,
level: 7
},
{
label: "Shuangliao",
value: "双辽市",
leaf: false,
level: 7
}
]
},
{
label: "Liaoyuan",
value: "辽源市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Longshan",
value: "龙山区",
leaf: false,
level: 7
},
{
label: "Xian",
value: "西安区",
leaf: false,
level: 7
},
{
label: "Dongfeng",
value: "东丰县",
leaf: false,
level: 7
},
{
label: "Dongliao",
value: "东辽县",
leaf: false,
level: 7
}
]
},
{
label: "Tonghua",
value: "通化市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Dongchang",
value: "东昌区",
leaf: false,
level: 7
},
{
label: "Erdaojiang",
value: "二道江区",
leaf: false,
level: 7
},
{
label: "Tonghua",
value: "通化县",
leaf: false,
level: 7
},
{
label: "Huinan",
value: "辉南县",
leaf: false,
level: 7
},
{
label: "Liuhe",
value: "柳河县",
leaf: false,
level: 7
},
{
label: "Meihekou",
value: "梅河口市",
leaf: false,
level: 7
},
{
label: "Jian",
value: "集安市",
leaf: false,
level: 7
}
]
},
{
label: "Baishan",
value: "白山市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Hunjiang",
value: "浑江区",
leaf: false,
level: 7
},
{
label: "Jiangyuan",
value: "江源区",
leaf: false,
level: 7
},
{
label: "Fusong",
value: "抚松县",
leaf: false,
level: 7
},
{
label: "Jingyu",
value: "靖宇县",
leaf: false,
level: 7
},
{
label: "Linjiang",
value: "临江市",
leaf: false,
level: 7
}
]
},
{
label: "Songyuan",
value: "松原市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Ningjiang",
value: "宁江区",
leaf: false,
level: 7
},
{
label: "Qianguoerluosimengguzu",
value: "前郭尔罗斯蒙古族自治县",
leaf: false,
level: 7
},
{
label: "Changling",
value: "长岭县",
leaf: false,
level: 7
},
{
label: "Qianan",
value: "乾安县",
leaf: false,
level: 7
},
{
label: "Fuyu",
value: "扶余市",
leaf: false,
level: 7
}
]
},
{
label: "Baicheng",
value: "白城市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Taobei",
value: "洮北区",
leaf: false,
level: 7
},
{
label: "Zhenlai",
value: "镇赉县",
leaf: false,
level: 7
},
{
label: "Tongyu",
value: "通榆县",
leaf: false,
level: 7
},
{
label: "Taonan",
value: "洮南市",
leaf: false,
level: 7
},
{
label: "Daan",
value: "大安市",
leaf: false,
level: 7
}
]
},
{
label: "Yanbianchaoxianzu",
value: "延边朝鲜族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yanji",
value: "延吉市",
leaf: false,
level: 7
},
{
label: "Tumen",
value: "图们市",
leaf: false,
level: 7
},
{
label: "Dunhua",
value: "敦化市",
leaf: false,
level: 7
},
{
label: "Hunchun",
value: "珲春市",
leaf: false,
level: 7
},
{
label: "Longjing",
value: "龙井市",
leaf: false,
level: 7
},
{
label: "Helong",
value: "和龙市",
leaf: false,
level: 7
},
{
label: "Wangqing",
value: "汪清县",
leaf: false,
level: 7
},
{
label: "Antu",
value: "安图县",
leaf: false,
level: 7
}
]
}
]
},
{
label: "Liaoning",
value: "辽宁省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Shenyang",
value: "沈阳市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Shenhe",
value: "沈河区",
leaf: false,
level: 7
},
{
label: "Heping",
value: "和平区",
leaf: false,
level: 7
},
{
label: "Dadong",
value: "大东区",
leaf: false,
level: 7
},
{
label: "Huanggu",
value: "皇姑区",
leaf: false,
level: 7
},
{
label: "Tiexi",
value: "铁西区",
leaf: false,
level: 7
},
{
label: "Sujiatun",
value: "苏家屯区",
leaf: false,
level: 7
},
{
label: "Hunnan",
value: "浑南区",
leaf: false,
level: 7
},
{
label: "Shenbeixin",
value: "沈北新区",
leaf: false,
level: 7
},
{
label: "Yuhong",
value: "于洪区",
leaf: false,
level: 7
},
{
label: "Liaozhong",
value: "辽中区",
leaf: false,
level: 7
},
{
label: "Xinmin",
value: "新民市",
leaf: false,
level: 7
},
{
label: "Kangping",
value: "康平县",
leaf: false,
level: 7
},
{
label: "Faku",
value: "法库县",
leaf: false,
level: 7
}
]
},
{
label: "Dalian",
value: "大连市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Zhongshan",
value: "中山区",
leaf: false,
level: 7
},
{
label: "Xigang",
value: "西岗区",
leaf: false,
level: 7
},
{
label: "Shahekou",
value: "沙河口区",
leaf: false,
level: 7
},
{
label: "Ganjingzi",
value: "甘井子区",
leaf: false,
level: 7
},
{
label: "Lüshunkou",
value: "旅顺口区",
leaf: false,
level: 7
},
{
label: "Jinzhou",
value: "金州区",
leaf: false,
level: 7
},
{
label: "Pulandian",
value: "普兰店区",
leaf: false,
level: 7
},
{
label: "Wafangdian",
value: "瓦房店市",
leaf: false,
level: 7
},
{
label: "Zhuanghe",
value: "庄河市",
leaf: false,
level: 7
},
{
label: "Changhai",
value: "长海县",
leaf: false,
level: 7
}
]
},
{
label: "Anshan",
value: "鞍山市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Tiedong",
value: "铁东区",
leaf: false,
level: 7
},
{
label: "Tiexi",
value: "铁西区",
leaf: false,
level: 7
},
{
label: "Lishan",
value: "立山区",
leaf: false,
level: 7
},
{
label: "Qianshan",
value: "千山区",
leaf: false,
level: 7
},
{
label: "Taian",
value: "台安县",
leaf: false,
level: 7
},
{
label: "Xiuyanmanzu",
value: "岫岩满族自治县",
leaf: false,
level: 7
},
{
label: "Haicheng",
value: "海城市",
leaf: false,
level: 7
}
]
},
{
label: "Fushun",
value: "抚顺市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xinfu",
value: "新抚区",
leaf: false,
level: 7
},
{
label: "Dongzhou",
value: "东洲区",
leaf: false,
level: 7
},
{
label: "Wanghua",
value: "望花区",
leaf: false,
level: 7
},
{
label: "Shuncheng",
value: "顺城区",
leaf: false,
level: 7
},
{
label: "Fushun",
value: "抚顺县",
leaf: false,
level: 7
},
{
label: "Xinbinmanzu",
value: "新宾满族自治县",
leaf: false,
level: 7
},
{
label: "Qingyuanmanzu",
value: "清原满族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Benxi",
value: "本溪市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Pingshan",
value: "平山区",
leaf: false,
level: 7
},
{
label: "Xihu",
value: "溪湖区",
leaf: false,
level: 7
},
{
label: "Mingshan",
value: "明山区",
leaf: false,
level: 7
},
{
label: "Nanfen",
value: "南芬区",
leaf: false,
level: 7
},
{
label: "Benximanzu",
value: "本溪满族自治县",
leaf: false,
level: 7
},
{
label: "Huanrenmanzu",
value: "桓仁满族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Dandong",
value: "丹东市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yuanbao",
value: "元宝区",
leaf: false,
level: 7
},
{
label: "Zhenxing",
value: "振兴区",
leaf: false,
level: 7
},
{
label: "Zhenan",
value: "振安区",
leaf: false,
level: 7
},
{
label: "Kuandianmanzu",
value: "宽甸满族自治县",
leaf: false,
level: 7
},
{
label: "Donggang",
value: "东港市",
leaf: false,
level: 7
},
{
label: "Fengcheng",
value: "凤城市",
leaf: false,
level: 7
}
]
},
{
label: "Jinzhou",
value: "锦州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Guta",
value: "古塔区",
leaf: false,
level: 7
},
{
label: "Linghe",
value: "凌河区",
leaf: false,
level: 7
},
{
label: "Taihe",
value: "太和区",
leaf: false,
level: 7
},
{
label: "Heishan",
value: "黑山县",
leaf: false,
level: 7
},
{
label: "Yixian",
value: "义县",
leaf: false,
level: 7
},
{
label: "Linghai",
value: "凌海市",
leaf: false,
level: 7
},
{
label: "Beizhen",
value: "北镇市",
leaf: false,
level: 7
}
]
},
{
label: "Yingkou",
value: "营口市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Zhanqian",
value: "站前区",
leaf: false,
level: 7
},
{
label: "Xishi",
value: "西市区",
leaf: false,
level: 7
},
{
label: "Bayuquan",
value: "鲅鱼圈区",
leaf: false,
level: 7
},
{
label: "Laobian",
value: "老边区",
leaf: false,
level: 7
},
{
label: "Gaizhou",
value: "盖州市",
leaf: false,
level: 7
},
{
label: "Dashiqiao",
value: "大石桥市",
leaf: false,
level: 7
}
]
},
{
label: "Fuxin",
value: "阜新市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Haizhou",
value: "海州区",
leaf: false,
level: 7
},
{
label: "Xinqiu",
value: "新邱区",
leaf: false,
level: 7
},
{
label: "Taiping",
value: "太平区",
leaf: false,
level: 7
},
{
label: "Qinghemen",
value: "清河门区",
leaf: false,
level: 7
},
{
label: "Xihe",
value: "细河区",
leaf: false,
level: 7
},
{
label: "Fuxinmengguzu",
value: "阜新蒙古族自治县",
leaf: false,
level: 7
},
{
label: "Zhangwu",
value: "彰武县",
leaf: false,
level: 7
}
]
},
{
label: "Liaoyang",
value: "辽阳市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Baita",
value: "白塔区",
leaf: false,
level: 7
},
{
label: "Wensheng",
value: "文圣区",
leaf: false,
level: 7
},
{
label: "Hongwei",
value: "宏伟区",
leaf: false,
level: 7
},
{
label: "Gongchangling",
value: "弓长岭区",
leaf: false,
level: 7
},
{
label: "Taizihe",
value: "太子河区",
leaf: false,
level: 7
},
{
label: "Liaoyang",
value: "辽阳县",
leaf: false,
level: 7
},
{
label: "Dengta",
value: "灯塔市",
leaf: false,
level: 7
}
]
},
{
label: "Panjin",
value: "盘锦市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Shuangtaizi",
value: "双台子区",
leaf: false,
level: 7
},
{
label: "Xinglongtai",
value: "兴隆台区",
leaf: false,
level: 7
},
{
label: "Dawa",
value: "大洼区",
leaf: false,
level: 7
},
{
label: "Panshan",
value: "盘山县",
leaf: false,
level: 7
}
]
},
{
label: "Tieling",
value: "铁岭市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yinzhou",
value: "银州区",
leaf: false,
level: 7
},
{
label: "Qinghe",
value: "清河区",
leaf: false,
level: 7
},
{
label: "Tieling",
value: "铁岭县",
leaf: false,
level: 7
},
{
label: "Xifeng",
value: "西丰县",
leaf: false,
level: 7
},
{
label: "Changtu",
value: "昌图县",
leaf: false,
level: 7
},
{
label: "Diaobingshan",
value: "调兵山市",
leaf: false,
level: 7
},
{
label: "Kaiyuan",
value: "开原市",
leaf: false,
level: 7
}
]
},
{
label: "Chaoyang",
value: "朝阳市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Shuangta",
value: "双塔区",
leaf: false,
level: 7
},
{
label: "Longcheng",
value: "龙城区",
leaf: false,
level: 7
},
{
label: "Chaoyang",
value: "朝阳县",
leaf: false,
level: 7
},
{
label: "Jianping",
value: "建平县",
leaf: false,
level: 7
},
{
label: "Kalaqinzuoyimengguzu",
value: "喀喇沁左翼蒙古族自治县",
leaf: false,
level: 7
},
{
label: "Beipiao",
value: "北票市",
leaf: false,
level: 7
},
{
label: "Lingyuan",
value: "凌源市",
leaf: false,
level: 7
}
]
},
{
label: "Huludao",
value: "葫芦岛市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Lianshan",
value: "连山区",
leaf: false,
level: 7
},
{
label: "Longgang",
value: "龙港区",
leaf: false,
level: 7
},
{
label: "Nanpiao",
value: "南票区",
leaf: false,
level: 7
},
{
label: "Suizhong",
value: "绥中县",
leaf: false,
level: 7
},
{
label: "Jianchang",
value: "建昌县",
leaf: false,
level: 7
},
{
label: "Xingcheng",
value: "兴城市",
leaf: false,
level: 7
}
]
}
]
},
{
label: "Neimenggu",
value: "内蒙古自治区",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Wulanchabu",
value: "乌兰察布市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Jining",
value: "集宁区",
leaf: false,
level: 7
},
{
label: "Fengzhen",
value: "丰镇市",
leaf: false,
level: 7
},
{
label: "Zhuozi",
value: "卓资县",
leaf: false,
level: 7
},
{
label: "Huade",
value: "化德县",
leaf: false,
level: 7
},
{
label: "Shangdu",
value: "商都县",
leaf: false,
level: 7
},
{
label: "Xinghe",
value: "兴和县",
leaf: false,
level: 7
},
{
label: "Liangcheng",
value: "凉城县",
leaf: false,
level: 7
},
{
label: "Chaharrightfrontbanner",
value: "察哈尔右翼前旗",
leaf: false,
level: 7
},
{
label: "Chaharrightmiddlebanner",
value: "察哈尔右翼中旗",
leaf: false,
level: 7
},
{
label: "Chaharrightrearbanner",
value: "察哈尔右翼后旗",
leaf: false,
level: 7
},
{
label: "Siziwang",
value: "四子王旗",
leaf: false,
level: 7
}
]
},
{
label: "Wuhai",
value: "乌海市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Haibowan",
value: "海勃湾区",
leaf: false,
level: 7
},
{
label: "Hainan",
value: "海南区",
leaf: false,
level: 7
},
{
label: "Wuda",
value: "乌达区",
leaf: false,
level: 7
}
]
},
{
label: "Huhehaote",
value: "呼和浩特市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Huimin",
value: "回民区",
leaf: false,
level: 7
},
{
label: "Saihan",
value: "赛罕区",
leaf: false,
level: 7
},
{
label: "Tumotezuo",
value: "土默特左旗",
leaf: false,
level: 7
},
{
label: "Tuoketuo",
value: "托克托县",
leaf: false,
level: 7
},
{
label: "Helingeer",
value: "和林格尔县",
leaf: false,
level: 7
},
{
label: "Qingshuihe",
value: "清水河县",
leaf: false,
level: 7
},
{
label: "Wuchuan",
value: "武川县",
leaf: false,
level: 7
}
]
},
{
label: "Baotou",
value: "包头市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Kundulun",
value: "昆都仑区",
leaf: false,
level: 7
},
{
label: "Baiyunebokuang",
value: "白云鄂博矿区",
leaf: false,
level: 7
},
{
label: "Tumoteyou",
value: "土默特右旗",
leaf: false,
level: 7
},
{
label: "Daerhanmaominganlianhe",
value: "达尔罕茂明安联合旗",
leaf: false,
level: 7
},
{
label: "Guyang",
value: "固阳县",
leaf: false,
level: 7
}
]
},
{
label: "Bayannaoer",
value: "巴彦淖尔市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Linhe",
value: "临河区",
leaf: false,
level: 7
},
{
label: "Wuyuan",
value: "五原县",
leaf: false,
level: 7
},
{
label: "Dengkou",
value: "磴口县",
leaf: false,
level: 7
},
{
label: "Wulatezhong",
value: "乌拉特中旗",
leaf: false,
level: 7
},
{
label: "Wulateqian",
value: "乌拉特前旗",
leaf: false,
level: 7
},
{
label: "Wulatehou",
value: "乌拉特后旗",
leaf: false,
level: 7
},
{
label: "Hangjinhou",
value: "杭锦后旗",
leaf: false,
level: 7
}
]
},
{
label: "Hulunbeier",
value: "呼伦贝尔市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Hailaer",
value: "海拉尔区",
leaf: false,
level: 7
},
{
label: "Zhalantun",
value: "扎兰屯市",
leaf: false,
level: 7
},
{
label: "Manzhouli",
value: "满洲里市",
leaf: false,
level: 7
},
{
label: "Yakeshi",
value: "牙克石市",
leaf: false,
level: 7
},
{
label: "Eerguna",
value: "额尔古纳市",
leaf: false,
level: 7
},
{
label: "Genhe",
value: "根河市",
leaf: false,
level: 7
},
{
label: "Arong",
value: "阿荣旗",
leaf: false,
level: 7
},
{
label: "Molidawadawoerzu",
value: "莫力达瓦达斡尔族自治旗",
leaf: false,
level: 7
},
{
label: "Elunchun",
value: "鄂伦春自治旗",
leaf: false,
level: 7
},
{
label: "Ewenkezu",
value: "鄂温克族自治旗",
leaf: false,
level: 7
},
{
label: "Chenbaerhu",
value: "陈巴尔虎旗",
leaf: false,
level: 7
},
{
label: "Xinbaerhuyou",
value: "新巴尔虎右旗",
leaf: false,
level: 7
},
{
label: "Xinbaerhuzuo",
value: "新巴尔虎左旗",
leaf: false,
level: 7
},
{
label: "Zhalainuoer",
value: "扎赉诺尔区",
leaf: false,
level: 7
}
]
},
{
label: "Xingan",
value: "兴安盟",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Wulanhaote",
value: "乌兰浩特市",
leaf: false,
level: 7
},
{
label: "Aershan",
value: "阿尔山市",
leaf: false,
level: 7
},
{
label: "Keerqinyouyiqian",
value: "科尔沁右翼前旗",
leaf: false,
level: 7
},
{
label: "Keerqinyouyizhong",
value: "科尔沁右翼中旗",
leaf: false,
level: 7
},
{
label: "Zhalaite",
value: "扎赉特旗",
leaf: false,
level: 7
},
{
label: "Tuquan",
value: "突泉县",
leaf: false,
level: 7
}
]
},
{
label: "Xilinguole",
value: "锡林郭勒盟",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xilinhaote",
value: "锡林浩特市",
leaf: false,
level: 7
},
{
label: "Erlianhaote",
value: "二连浩特市",
leaf: false,
level: 7
},
{
label: "Abaga",
value: "阿巴嘎旗",
leaf: false,
level: 7
},
{
label: "Sunitezuo",
value: "苏尼特左旗",
leaf: false,
level: 7
},
{
label: "Suniteyou",
value: "苏尼特右旗",
leaf: false,
level: 7
},
{
label: "Dongwuzhumuqin",
value: "东乌珠穆沁旗",
leaf: false,
level: 7
},
{
label: "Xiwuzhumuqin",
value: "西乌珠穆沁旗",
leaf: false,
level: 7
},
{
label: "Taipusi",
value: "太仆寺旗",
leaf: false,
level: 7
},
{
label: "Xianghuang",
value: "镶黄旗",
leaf: false,
level: 7
},
{
label: "Zhengxiangbai",
value: "正镶白旗",
leaf: false,
level: 7
},
{
label: "Zhenglan",
value: "正蓝旗",
leaf: false,
level: 7
},
{
label: "Duolun",
value: "多伦县",
leaf: false,
level: 7
}
]
},
{
label: "Alashan",
value: "阿拉善盟",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Alashanyou",
value: "阿拉善右旗",
leaf: false,
level: 7
},
{
label: "Alashanzuo",
value: "阿拉善左旗",
leaf: false,
level: 7
},
{
label: "Ejina",
value: "额济纳旗",
leaf: false,
level: 7
}
]
},
{
label: "Chifeng",
value: "赤峰市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Hongshan",
value: "红山区",
leaf: false,
level: 7
},
{
label: "Yuanbaoshan",
value: "元宝山区",
leaf: false,
level: 7
},
{
label: "Songshan",
value: "松山区",
leaf: false,
level: 7
},
{
label: "Alukeerqin",
value: "阿鲁科尔沁旗",
leaf: false,
level: 7
},
{
label: "Balinzuo",
value: "巴林左旗",
leaf: false,
level: 7
},
{
label: "Balinyou",
value: "巴林右旗",
leaf: false,
level: 7
},
{
label: "Linxi",
value: "林西县",
leaf: false,
level: 7
},
{
label: "Keshiketeng",
value: "克什克腾旗",
leaf: false,
level: 7
},
{
label: "Wengniute",
value: "翁牛特旗",
leaf: false,
level: 7
},
{
label: "Kalaqin",
value: "喀喇沁旗",
leaf: false,
level: 7
},
{
label: "Ningcheng",
value: "宁城县",
leaf: false,
level: 7
},
{
label: "Aohan",
value: "敖汉旗",
leaf: false,
level: 7
}
]
},
{
label: "Tongliao",
value: "通辽市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Keerqin",
value: "科尔沁区",
leaf: false,
level: 7
},
{
label: "Huolinguole",
value: "霍林郭勒市",
leaf: false,
level: 7
},
{
label: "Keerqinzuoyizhong",
value: "科尔沁左翼中旗",
leaf: false,
level: 7
},
{
label: "Keerqinzuoyihou",
value: "科尔沁左翼后旗",
leaf: false,
level: 7
},
{
label: "Kailu",
value: "开鲁县",
leaf: false,
level: 7
},
{
label: "Kulun",
value: "库伦旗",
leaf: false,
level: 7
},
{
label: "Naiman",
value: "奈曼旗",
leaf: false,
level: 7
},
{
label: "Zhalute",
value: "扎鲁特旗",
leaf: false,
level: 7
}
]
},
{
label: "Eerduosi",
value: "鄂尔多斯市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Dongsheng",
value: "东胜区",
leaf: false,
level: 7
},
{
label: "Kangbashi",
value: "康巴什区",
leaf: false,
level: 7
},
{
label: "Dalate",
value: "达拉特旗",
leaf: false,
level: 7
},
{
label: "Zhungeer",
value: "准格尔旗",
leaf: false,
level: 7
},
{
label: "Etuokeqian",
value: "鄂托克前旗",
leaf: false,
level: 7
},
{
label: "Etuoke",
value: "鄂托克旗",
leaf: false,
level: 7
},
{
label: "Hangjin",
value: "杭锦旗",
leaf: false,
level: 7
},
{
label: "Wushen",
value: "乌审旗",
leaf: false,
level: 7
},
{
label: "Yijinhuoluo",
value: "伊金霍洛旗",
leaf: false,
level: 7
}
]
}
]
}


                ],
              },
              {
                label: "East China",
                value: "华东",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "Province",
                    value: "省级",
                    leaf: true,
                    level: 5,
                    disabled: true,
                    display: true,
                  },
{
label: "Shanghai",
value: "上海市",
leaf: false,
level: 5,
children: [
{
label: "Huangpu",
value: "黄浦区",
leaf: false,
level: 7
},
{
label: "Xuhui",
value: "徐汇区",
leaf: false,
level: 7
},
{
label: "Changning",
value: "长宁区",
leaf: false,
level: 7
},
{
label: "Jingan",
value: "静安区",
leaf: false,
level: 7
},
{
label: "Putuo",
value: "普陀区",
leaf: false,
level: 7
},
{
label: "Hongkou",
value: "虹口区",
leaf: false,
level: 7
},
{
label: "Yangpu",
value: "杨浦区",
leaf: false,
level: 7
},
{
label: "Minhang",
value: "闵行区",
leaf: false,
level: 7
},
{
label: "Baoshan",
value: "宝山区",
leaf: false,
level: 7
},
{
label: "Jiading",
value: "嘉定区",
leaf: false,
level: 7
},
{
label: "Pudongxin",
value: "浦东新区",
leaf: false,
level: 7
},
{
label: "Jinshan",
value: "金山区",
leaf: false,
level: 7
},
{
label: "Songjiang",
value: "松江区",
leaf: false,
level: 7
},
{
label: "Qingpu",
value: "青浦区",
leaf: false,
level: 7
},
{
label: "Fengxian",
value: "奉贤区",
leaf: false,
level: 7
},
{
label: "Chongming",
value: "崇明区",
leaf: false,
level: 7
}
]
},
{
label: "Jiangsu",
value: "江苏省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Nanjing",
value: "南京市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xuanwu",
value: "玄武区",
leaf: false,
level: 7
},
{
label: "Qinhuai",
value: "秦淮区",
leaf: false,
level: 7
},
{
label: "Jianye",
value: "建邺区",
leaf: false,
level: 7
},
{
label: "Gulou",
value: "鼓楼区",
leaf: false,
level: 7
},
{
label: "Pukou",
value: "浦口区",
leaf: false,
level: 7
},
{
label: "Qixia",
value: "栖霞区",
leaf: false,
level: 7
},
{
label: "Yuhuatai",
value: "雨花台区",
leaf: false,
level: 7
},
{
label: "Jiangning",
value: "江宁区",
leaf: false,
level: 7
},
{
label: "Liuhe",
value: "六合区",
leaf: false,
level: 7
},
{
label: "Lishui",
value: "溧水区",
leaf: false,
level: 7
},
{
label: "Gaochun",
value: "高淳区",
leaf: false,
level: 7
}
]
},
{
label: "Nantong",
value: "南通市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Chongchuan",
value: "崇川区",
leaf: false,
level: 7
},
{
label: "Tongzhou",
value: "通州区",
leaf: false,
level: 7
},
{
label: "Haimen",
value: "海门区",
leaf: false,
level: 7
},
{
label: "Qidong",
value: "启东市",
leaf: false,
level: 7
},
{
label: "Rugao",
value: "如皋市",
leaf: false,
level: 7
},
{
label: "Rudong",
value: "如东县",
leaf: false,
level: 7
}
]
},
{
label: "Xuzhou",
value: "徐州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Gulou",
value: "鼓楼区",
leaf: false,
level: 7
},
{
label: "Yunlong",
value: "云龙区",
leaf: false,
level: 7
},
{
label: "Jiawang",
value: "贾汪区",
leaf: false,
level: 7
},
{
label: "Quanshan",
value: "泉山区",
leaf: false,
level: 7
},
{
label: "Tongshan",
value: "铜山区",
leaf: false,
level: 7
},
{
label: "Fengxian",
value: "丰县",
leaf: false,
level: 7
},
{
label: "Peixian",
value: "沛县",
leaf: false,
level: 7
},
{
label: "Suining",
value: "睢宁县",
leaf: false,
level: 7
},
{
label: "Xinyi",
value: "新沂市",
leaf: false,
level: 7
},
{
label: "Pizhou",
value: "邳州市",
leaf: false,
level: 7
}
]
},
{
label: "Changzhou",
value: "常州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Tianning",
value: "天宁区",
leaf: false,
level: 7
},
{
label: "Zhonglou",
value: "钟楼区",
leaf: false,
level: 7
},
{
label: "Xinbei",
value: "新北区",
leaf: false,
level: 7
},
{
label: "Wujin",
value: "武进区",
leaf: false,
level: 7
},
{
label: "Jintan",
value: "金坛区",
leaf: false,
level: 7
},
{
label: "Liyang",
value: "溧阳市",
leaf: false,
level: 7
}
]
},
{
label: "Wuxi",
value: "无锡市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Liangxi",
value: "梁溪区",
leaf: false,
level: 7
},
{
label: "Xishan",
value: "锡山区",
leaf: false,
level: 7
},
{
label: "Huishan",
value: "惠山区",
leaf: false,
level: 7
},
{
label: "Binhu",
value: "滨湖区",
leaf: false,
level: 7
},
{
label: "Xinwu",
value: "新吴区",
leaf: false,
level: 7
},
{
label: "Jiangyin",
value: "江阴市",
leaf: false,
level: 7
},
{
label: "Yixing",
value: "宜兴市",
leaf: false,
level: 7
}
]
},
{
label: "Suzhou",
value: "苏州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Gusu",
value: "姑苏区",
leaf: false,
level: 7
},
{
label: "Huqiu",
value: "虎丘区",
leaf: false,
level: 7
},
{
label: "Wuzhong",
value: "吴中区",
leaf: false,
level: 7
},
{
label: "Xiangcheng",
value: "相城区",
leaf: false,
level: 7
},
{
label: "Wujiang",
value: "吴江区",
leaf: false,
level: 7
},
{
label: "Changshu",
value: "常熟市",
leaf: false,
level: 7
},
{
label: "Zhangjiagang",
value: "张家港市",
leaf: false,
level: 7
},
{
label: "Kunshan",
value: "昆山市",
leaf: false,
level: 7
},
{
label: "Taicang",
value: "太仓市",
leaf: false,
level: 7
}
]
},
{
label: "Yangzhou",
value: "扬州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Guangling",
value: "广陵区",
leaf: false,
level: 7
},
{
label: "Hanjiang",
value: "邗江区",
leaf: false,
level: 7
},
{
label: "Jiangdu",
value: "江都区",
leaf: false,
level: 7
},
{
label: "Baoying",
value: "宝应县",
leaf: false,
level: 7
},
{
label: "Yizheng",
value: "仪征市",
leaf: false,
level: 7
},
{
label: "Gaoyou",
value: "高邮市",
leaf: false,
level: 7
}
]
},
{
label: "Taizhou",
value: "泰州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Hailing",
value: "海陵区",
leaf: false,
level: 7
},
{
label: "Gaogang",
value: "高港区",
leaf: false,
level: 7
},
{
label: "Jiangyan",
value: "姜堰区",
leaf: false,
level: 7
},
{
label: "Xinghua",
value: "兴化市",
leaf: false,
level: 7
},
{
label: "Jingjiang",
value: "靖江市",
leaf: false,
level: 7
},
{
label: "Taixing",
value: "泰兴市",
leaf: false,
level: 7
}
]
},
{
label: "Huaian",
value: "淮安市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Huaian",
value: "淮安区",
leaf: false,
level: 7
},
{
label: "Huaiyin",
value: "淮阴区",
leaf: false,
level: 7
},
{
label: "Qingjiangpu",
value: "清江浦区",
leaf: false,
level: 7
},
{
label: "Hongze",
value: "洪泽区",
leaf: false,
level: 7
},
{
label: "Lianshui",
value: "涟水县",
leaf: false,
level: 7
},
{
label: "Xuyi",
value: "盱眙县",
leaf: false,
level: 7
},
{
label: "Jinhu",
value: "金湖县",
leaf: false,
level: 7
}
]
},
{
label: "Lianyungang",
value: "连云港市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Lianyun",
value: "连云区",
leaf: false,
level: 7
},
{
label: "Haizhou",
value: "海州区",
leaf: false,
level: 7
},
{
label: "Ganyu",
value: "赣榆区",
leaf: false,
level: 7
},
{
label: "Donghai",
value: "东海县",
leaf: false,
level: 7
},
{
label: "Guanyun",
value: "灌云县",
leaf: false,
level: 7
},
{
label: "Guannan",
value: "灌南县",
leaf: false,
level: 7
}
]
},
{
label: "Suqian",
value: "宿迁市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Sucheng",
value: "宿城区",
leaf: false,
level: 7
},
{
label: "Suyu",
value: "宿豫区",
leaf: false,
level: 7
},
{
label: "Shuyang",
value: "沭阳县",
leaf: false,
level: 7
},
{
label: "Siyang",
value: "泗阳县",
leaf: false,
level: 7
},
{
label: "Sihong",
value: "泗洪县",
leaf: false,
level: 7
}
]
}
]
},
{
label: "Zhejiang",
value: "浙江省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Hangzhou",
value: "杭州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Shangcheng",
value: "上城区",
leaf: false,
level: 7
},
{
label: "Gongshu",
value: "拱墅区",
leaf: false,
level: 7
},
{
label: "Xihu",
value: "西湖区",
leaf: false,
level: 7
},
{
label: "Binjiang",
value: "滨江区",
leaf: false,
level: 7
},
{
label: "Xiaoshan",
value: "萧山区",
leaf: false,
level: 7
},
{
label: "Yuhang",
value: "余杭区",
leaf: false,
level: 7
},
{
label: "Fuyang",
value: "富阳区",
leaf: false,
level: 7
},
{
label: "Lin'an",
value: "临安区",
leaf: false,
level: 7
},
{
label: "Tonglu",
value: "桐庐县",
leaf: false,
level: 7
},
{
label: "Chunan",
value: "淳安县",
leaf: false,
level: 7
},
{
label: "Jiande",
value: "建德市",
leaf: false,
level: 7
}
]
},
{
label: "Ningbo",
value: "宁波市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Haishu",
value: "海曙区",
leaf: false,
level: 7
},
{
label: "Jiangbei",
value: "江北区",
leaf: false,
level: 7
},
{
label: "Beilun",
value: "北仑区",
leaf: false,
level: 7
},
{
label: "Zhenhai",
value: "镇海区",
leaf: false,
level: 7
},
{
label: "Yinzhou",
value: "鄞州区",
leaf: false,
level: 7
},
{
label: "Fenghua",
value: "奉化区",
leaf: false,
level: 7
},
{
label: "Xiangshan",
value: "象山县",
leaf: false,
level: 7
},
{
label: "Ninghai",
value: "宁海县",
leaf: false,
level: 7
},
{
label: "Yuyao",
value: "余姚市",
leaf: false,
level: 7
},
{
label: "Cixi",
value: "慈溪市",
leaf: false,
level: 7
}
]
},
{
label: "Wenzhou",
value: "温州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Lucheng",
value: "鹿城区",
leaf: false,
level: 7
},
{
label: "Longwan",
value: "龙湾区",
leaf: false,
level: 7
},
{
label: "Ouhai",
value: "瓯海区",
leaf: false,
level: 7
},
{
label: "Dongtou",
value: "洞头区",
leaf: false,
level: 7
},
{
label: "Yongjia",
value: "永嘉县",
leaf: false,
level: 7
},
{
label: "Pingyang",
value: "平阳县",
leaf: false,
level: 7
},
{
label: "Cangnan",
value: "苍南县",
leaf: false,
level: 7
},
{
label: "Wencheng",
value: "文成县",
leaf: false,
level: 7
},
{
label: "Taishun",
value: "泰顺县",
leaf: false,
level: 7
},
{
label: "Ruian",
value: "瑞安市",
leaf: false,
level: 7
},
{
label: "Yueqing",
value: "乐清市",
leaf: false,
level: 7
}
]
},
{
label: "Jiaxing",
value: "嘉兴市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Nanhu",
value: "南湖区",
leaf: false,
level: 7
},
{
label: "Xiuzhou",
value: "秀洲区",
leaf: false,
level: 7
},
{
label: "Jiashan",
value: "嘉善县",
leaf: false,
level: 7
},
{
label: "Haiyan",
value: "海盐县",
leaf: false,
level: 7
},
{
label: "Haining",
value: "海宁市",
leaf: false,
level: 7
},
{
label: "Pinghu",
value: "平湖市",
leaf: false,
level: 7
},
{
label: "Tongxiang",
value: "桐乡市",
leaf: false,
level: 7
}
]
},
{
label: "Huzhou",
value: "湖州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Wuxing",
value: "吴兴区",
leaf: false,
level: 7
},
{
label: "Nanxun",
value: "南浔区",
leaf: false,
level: 7
},
{
label: "Deqing",
value: "德清县",
leaf: false,
level: 7
},
{
label: "Changxing",
value: "长兴县",
leaf: false,
level: 7
},
{
label: "Anji",
value: "安吉县",
leaf: false,
level: 7
}
]
},
{
label: "Shaoxing",
value: "绍兴市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yuecheng",
value: "越城区",
leaf: false,
level: 7
},
{
label: "Keqiao",
value: "柯桥区",
leaf: false,
level: 7
},
{
label: "Shangyu",
value: "上虞区",
leaf: false,
level: 7
},
{
label: "Xinchang",
value: "新昌县",
leaf: false,
level: 7
},
{
label: "Zhuji",
value: "诸暨市",
leaf: false,
level: 7
},
{
label: "Shengzhou",
value: "嵊州市",
leaf: false,
level: 7
}
]
},
{
label: "Jinhua",
value: "金华市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Wucheng",
value: "婺城区",
leaf: false,
level: 7
},
{
label: "Jindong",
value: "金东区",
leaf: false,
level: 7
},
{
label: "Wuyi",
value: "武义县",
leaf: false,
level: 7
},
{
label: "Pujiang",
value: "浦江县",
leaf: false,
level: 7
},
{
label: "Panan",
value: "磐安县",
leaf: false,
level: 7
},
{
label: "Lanxi",
value: "兰溪市",
leaf: false,
level: 7
},
{
label: "Yiwu",
value: "义乌市",
leaf: false,
level: 7
},
{
label: "Dongyang",
value: "东阳市",
leaf: false,
level: 7
},
{
label: "Yongkang",
value: "永康市",
leaf: false,
level: 7
}
]
},
{
label: "Quzhou",
value: "衢州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Kecheng",
value: "柯城区",
leaf: false,
level: 7
},
{
label: "Qujiang",
value: "衢江区",
leaf: false,
level: 7
},
{
label: "Changshan",
value: "常山县",
leaf: false,
level: 7
},
{
label: "Kaihua",
value: "开化县",
leaf: false,
level: 7
},
{
label: "Longyou",
value: "龙游县",
leaf: false,
level: 7
},
{
label: "Jiangshan",
value: "江山市",
leaf: false,
level: 7
}
]
},
{
label: "Zhoushan",
value: "舟山市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Dinghai",
value: "定海区",
leaf: false,
level: 7
},
{
label: "Putuo",
value: "普陀区",
leaf: false,
level: 7
},
{
label: "Daishan",
value: "岱山县",
leaf: false,
level: 7
},
{
label: "Shengsi",
value: "嵊泗县",
leaf: false,
level: 7
}
]
},
{
label: "Taizhou",
value: "台州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Jiaojiang",
value: "椒江区",
leaf: false,
level: 7
},
{
label: "Huangyan",
value: "黄岩区",
leaf: false,
level: 7
},
{
label: "Luqiao",
value: "路桥区",
leaf: false,
level: 7
},
{
label: "Yuhuan",
value: "玉环市",
leaf: false,
level: 7
},
{
label: "Sanmen",
value: "三门县",
leaf: false,
level: 7
},
{
label: "Tiantai",
value: "天台县",
leaf: false,
level: 7
},
{
label: "Xianju",
value: "仙居县",
leaf: false,
level: 7
},
{
label: "Wenling",
value: "温岭市",
leaf: false,
level: 7
},
{
label: "Linhai",
value: "临海市",
leaf: false,
level: 7
}
]
},
{
label: "Lishui",
value: "丽水市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Liandu",
value: "莲都区",
leaf: false,
level: 7
},
{
label: "Qingtian",
value: "青田县",
leaf: false,
level: 7
},
{
label: "Jinyun",
value: "缙云县",
leaf: false,
level: 7
},
{
label: "Suichang",
value: "遂昌县",
leaf: false,
level: 7
},
{
label: "Songyang",
value: "松阳县",
leaf: false,
level: 7
},
{
label: "Yunhe",
value: "云和县",
leaf: false,
level: 7
},
{
label: "Qingyuan",
value: "庆元县",
leaf: false,
level: 7
},
{
label: "Jingningshezu",
value: "景宁畲族自治县",
leaf: false,
level: 7
},
{
label: "Longquan",
value: "龙泉市",
leaf: false,
level: 7
}
]
}
]
},
{
  label: "Anhui",
  value: "安徽省",
  leaf: false,
  level: 5,
  children: [
    {
      label: "Prefecture",
      value: "地级",
      leaf: false,
      level: 6
    },
    {
      label: "Hefei",
      value: "合肥市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yaohai",
          value: "瑶海区",
          leaf: false,
          level: 7
        },
        {
          label: "Luyang",
          value: "庐阳区",
          leaf: false,
          level: 7
        },
        {
          label: "Shushan",
          value: "蜀山区",
          leaf: false,
          level: 7
        },
        {
          label: "Baohe",
          value: "包河区",
          leaf: false,
          level: 7
        },
        {
          label: "Changfeng",
          value: "长丰县",
          leaf: false,
          level: 7
        },
        {
          label: "Feidong",
          value: "肥东县",
          leaf: false,
          level: 7
        },
        {
          label: "Feixi",
          value: "肥西县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Wuhu",
      value: "芜湖市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Jinghu",
          value: "镜湖区",
          leaf: false,
          level: 7
        },
        {
          label: "Yijiang",
          value: "弋江区",
          leaf: false,
          level: 7
        },
        {
          label: "Jiujiang",
          value: "鸠江区",
          leaf: false,
          level: 7
        },
        {
          label: "Wanzhi",
          value: "湾沚区",
          leaf: false,
          level: 7
        },
        {
          label: "Fanchang",
          value: "繁昌区",
          leaf: false,
          level: 7
        },
        {
          label: "Nanling",
          value: "南陵县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Bengbu",
      value: "蚌埠市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Longzihu",
          value: "龙子湖区",
          leaf: false,
          level: 7
        },
        {
          label: "Bengshan",
          value: "蚌山区",
          leaf: false,
          level: 7
        },
        {
          label: "Yuhui",
          value: "禹会区",
          leaf: false,
          level: 7
        },
        {
          label: "Huaishang",
          value: "淮上区",
          leaf: false,
          level: 7
        },
        {
          label: "Huaiyuan",
          value: "怀远县",
          leaf: false,
          level: 7
        },
        {
          label: "Wuhe",
          value: "五河县",
          leaf: false,
          level: 7
        },
        {
          label: "Guzhen",
          value: "固镇县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Huainan",
      value: "淮南市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Datong",
          value: "大通区",
          leaf: false,
          level: 7
        },
        {
          label: "Tianjiaan",
          value: "田家庵区",
          leaf: false,
          level: 7
        },
        {
          label: "Xiejiaji",
          value: "谢家集区",
          leaf: false,
          level: 7
        },
        {
          label: "Bagongshan",
          value: "八公山区",
          leaf: false,
          level: 7
        },
        {
          label: "Panji",
          value: "潘集区",
          leaf: false,
          level: 7
        },
        {
          label: "Fengtai",
          value: "凤台县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Maanshan",
      value: "马鞍山市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Huashan",
          value: "花山区",
          leaf: false,
          level: 7
        },
        {
          label: "Yushan",
          value: "雨山区",
          leaf: false,
          level: 7
        },
        {
          label: "Bowang",
          value: "博望区",
          leaf: false,
          level: 7
        },
        {
          label: "Dangtu",
          value: "当涂县",
          leaf: false,
          level: 7
        },
        {
          label: "Hexian",
          value: "和县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Huaibei",
      value: "淮北市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Duji",
          value: "杜集区",
          leaf: false,
          level: 7
        },
        {
          label: "Xiangshan",
          value: "相山区",
          leaf: false,
          level: 7
        },
        {
          label: "Lieshan",
          value: "烈山区",
          leaf: false,
          level: 7
        },
        {
          label: "Suixi",
          value: "濉溪县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Tongling",
      value: "铜陵市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Tongguan",
          value: "铜官区",
          leaf: false,
          level: 7
        },
        {
          label: "Jiaoqu",
          value: "郊区",
          leaf: false,
          level: 7
        },
        {
          label: "Yian",
          value: "义安区",
          leaf: false,
          level: 7
        },
        {
          label: "Zongyang",
          value: "枞阳县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Anqing",
      value: "安庆市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yingjiang",
          value: "迎江区",
          leaf: false,
          level: 7
        },
        {
          label: "Daguan",
          value: "大观区",
          leaf: false,
          level: 7
        },
        {
          label: "Yixiu",
          value: "宜秀区",
          leaf: false,
          level: 7
        },
        {
          label: "Huaining",
          value: "怀宁县",
          leaf: false,
          level: 7
        },
        {
          label: "Qianshan",
          value: "潜山市",
          leaf: false,
          level: 7
        },
        {
          label: "Taihu",
          value: "太湖县",
          leaf: false,
          level: 7
        },
        {
          label: "Susong",
          value: "宿松县",
          leaf: false,
          level: 7
        },
        {
          label: "Wangjiang",
          value: "望江县",
          leaf: false,
          level: 7
        },
        {
          label: "Yuexi",
          value: "岳西县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Huangshan",
      value: "黄山市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Tunxi",
          value: "屯溪区",
          leaf: false,
          level: 7
        },
        {
          label: "Huangshan",
          value: "黄山区",
          leaf: false,
          level: 7
        },
        {
          label: "Huizhou",
          value: "徽州区",
          leaf: false,
          level: 7
        },
        {
          label: "Shexian",
          value: "歙县",
          leaf: false,
          level: 7
        },
        {
          label: "Xiuning",
          value: "休宁县",
          leaf: false,
          level: 7
        },
        {
          label: "Yixian",
          value: "黟县",
          leaf: false,
          level: 7
        },
        {
          label: "Qimen",
          value: "祁门县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Chuzhou",
      value: "滁州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Langya",
          value: "琅琊区",
          leaf: false,
          level: 7
        },
        {
          label: "Nanqiao",
          value: "南谯区",
          leaf: false,
          level: 7
        },
        {
          label: "Laian",
          value: "来安县",
          leaf: false,
          level: 7
        },
        {
          label: "Quanjiao",
          value: "全椒县",
          leaf: false,
          level: 7
        },
        {
          label: "Dingyuan",
          value: "定远县",
          leaf: false,
          level: 7
        },
        {
          label: "Fengyang",
          value: "凤阳县",
          leaf: false,
          level: 7
        },
        {
          label: "Tianchang",
          value: "天长市",
          leaf: false,
          level: 7
        },
        {
          label: "Mingguang",
          value: "明光市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Fuyang",
      value: "阜阳市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yingzhou",
          value: "颍州区",
          leaf: false,
          level: 7
        },
        {
          label: "Yingdong",
          value: "颍东区",
          leaf: false,
          level: 7
        },
        {
          label: "Yingquan",
          value: "颍泉区",
          leaf: false,
          level: 7
        },
        {
          label: "Yingshang",
          value: "颍上县",
          leaf: false,
          level: 7
        },
        {
          label: "Jieshou",
          value: "界首市",
          leaf: false,
          level: 7
        },
        {
          label: "Linquan",
          value: "临泉县",
          leaf: false,
          level: 7
        },
        {
          label: "Taihe",
          value: "太和县",
          leaf: false,
          level: 7
        },
        {
          label: "Funan",
          value: "阜南县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Suzhou",
      value: "宿州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yongqiao",
          value: "埇桥区",
          leaf: false,
          level: 7
        },
        {
          label: "Dangshan",
          value: "砀山县",
          leaf: false,
          level: 7
        },
        {
          label: "Xiaoxian",
          value: "萧县",
          leaf: false,
          level: 7
        },
        {
          label: "Lingbi",
          value: "灵璧县",
          leaf: false,
          level: 7
        },
        {
          label: "Sixian",
          value: "泗县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Liuan",
      value: "六安市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Bozhou",
      value: "亳州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Qiaocheng",
          value: "谯城区",
          leaf: false,
          level: 7
        },
        {
          label: "Guoyang",
          value: "涡阳县",
          leaf: false,
          level: 7
        },
        {
          label: "Mengcheng",
          value: "蒙城县",
          leaf: false,
          level: 7
        },
        {
          label: "Lixin",
          value: "利辛县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Chizhou",
      value: "池州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Guichi",
          value: "贵池区",
          leaf: false,
          level: 7
        },
        {
          label: "Dongzhi",
          value: "东至县",
          leaf: false,
          level: 7
        },
        {
          label: "Shitai",
          value: "石台县",
          leaf: false,
          level: 7
        },
        {
          label: "Qingyang",
          value: "青阳县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Xuancheng",
      value: "宣城市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Xuanzhou",
          value: "宣州区",
          leaf: false,
          level: 7
        },
        {
          label: "Langxi",
          value: "郎溪县",
          leaf: false,
          level: 7
        },
        {
          label: "Guangde",
          value: "广德市",
          leaf: false,
          level: 7
        },
        {
          label: "Jingxian",
          value: "泾县",
          leaf: false,
          level: 7
        },
        {
          label: "Jixi",
          value: "绩溪县",
          leaf: false,
          level: 7
        },
        {
          label: "Jingde",
          value: "旌德县",
          leaf: false,
          level: 7
        },
        {
          label: "Ningguo",
          value: "宁国市",
          leaf: false,
          level: 7
        }
      ]
    }
  ]
},
{
  label: "Fujian",
  value: "福建省",
  leaf: false,
  level: 5,
  children: [
    {
      label: "Prefecture",
      value: "地级",
      leaf: false,
      level: 6
    },
    {
      label: "Fuzhou",
      value: "福州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Gulou",
          value: "鼓楼区",
          leaf: false,
          level: 7
        },
        {
          label: "Taijiang",
          value: "台江区",
          leaf: false,
          level: 7
        },
        {
          label: "Cangshan",
          value: "仓山区",
          leaf: false,
          level: 7
        },
        {
          label: "Jinan",
          value: "晋安区",
          leaf: false,
          level: 7
        },
        {
          label: "Mawei",
          value: "马尾区",
          leaf: false,
          level: 7
        },
        {
          label: "Changle",
          value: "长乐区",
          leaf: false,
          level: 7
        },
        {
          label: "Minhou",
          value: "闽侯县",
          leaf: false,
          level: 7
        },
        {
          label: "Lianjiang",
          value: "连江县",
          leaf: false,
          level: 7
        },
        {
          label: "Luoyuan",
          value: "罗源县",
          leaf: false,
          level: 7
        },
        {
          label: "Minqing",
          value: "闽清县",
          leaf: false,
          level: 7
        },
        {
          label: "Yongtai",
          value: "永泰县",
          leaf: false,
          level: 7
        },
        {
          label: "Pingtan",
          value: "平潭县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Xiamen",
      value: "厦门市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Siming",
          value: "思明区",
          leaf: false,
          level: 7
        },
        {
          label: "Huli",
          value: "湖里区",
          leaf: false,
          level: 7
        },
        {
          label: "Jimei",
          value: "集美区",
          leaf: false,
          level: 7
        },
        {
          label: "Haicang",
          value: "海沧区",
          leaf: false,
          level: 7
        },
        {
          label: "Tongan",
          value: "同安区",
          leaf: false,
          level: 7
        },
        {
          label: "Xiangan",
          value: "翔安区",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Putian",
      value: "莆田市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Chengxiang",
          value: "城厢区",
          leaf: false,
          level: 7
        },
        {
          label: "Hanjiang",
          value: "涵江区",
          leaf: false,
          level: 7
        },
        {
          label: "Licheng",
          value: "荔城区",
          leaf: false,
          level: 7
        },
        {
          label: "Xiuyu",
          value: "秀屿区",
          leaf: false,
          level: 7
        },
        {
          label: "Xianyou",
          value: "仙游县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Sanming",
      value: "三明市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Sanyuan",
          value: "三元区",
          leaf: false,
          level: 7
        },
        {
          label: "Mingxi",
          value: "明溪县",
          leaf: false,
          level: 7
        },
        {
          label: "Qingliu",
          value: "清流县",
          leaf: false,
          level: 7
        },
        {
          label: "Ninghua",
          value: "宁化县",
          leaf: false,
          level: 7
        },
        {
          label: "Datian",
          value: "大田县",
          leaf: false,
          level: 7
        },
        {
          label: "Youxi",
          value: "尤溪县",
          leaf: false,
          level: 7
        },
        {
          label: "Shaxian",
          value: "沙县区",
          leaf: false,
          level: 7
        },
        {
          label: "Jiangle",
          value: "将乐县",
          leaf: false,
          level: 7
        },
        {
          label: "Taining",
          value: "泰宁县",
          leaf: false,
          level: 7
        },
        {
          label: "Jianning",
          value: "建宁县",
          leaf: false,
          level: 7
        },
        {
          label: "Yongan",
          value: "永安市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Quanzhou",
      value: "泉州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Licheng",
          value: "鲤城区",
          leaf: false,
          level: 7
        },
        {
          label: "Fengze",
          value: "丰泽区",
          leaf: false,
          level: 7
        },
        {
          label: "Luojiang",
          value: "洛江区",
          leaf: false,
          level: 7
        },
        {
          label: "Quangang",
          value: "泉港区",
          leaf: false,
          level: 7
        },
        {
          label: "Huian",
          value: "惠安县",
          leaf: false,
          level: 7
        },
        {
          label: "Anxi",
          value: "安溪县",
          leaf: false,
          level: 7
        },
        {
          label: "Yongchun",
          value: "永春县",
          leaf: false,
          level: 7
        },
        {
          label: "Dehua",
          value: "德化县",
          leaf: false,
          level: 7
        },
        {
          label: "Jinmen",
          value: "金门县",
          leaf: false,
          level: 7
        },
        {
          label: "Shishi",
          value: "石狮市",
          leaf: false,
          level: 7
        },
        {
          label: "Jinjiang",
          value: "晋江市",
          leaf: false,
          level: 7
        },
        {
          label: "Nanan",
          value: "南安市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Zhangzhou",
      value: "漳州市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Xiangcheng",
          value: "芗城区",
          leaf: false,
          level: 7
        },
        {
          label: "Longwen",
          value: "龙文区",
          leaf: false,
          level: 7
        },
        {
          label: "Longhai",
          value: "龙海区",
          leaf: false,
          level: 7
        },
        {
          label: "Changtai",
          value: "长泰区",
          leaf: false,
          level: 7
        },
        {
          label: "Yunxiao",
          value: "云霄县",
          leaf: false,
          level: 7
        },
        {
          label: "Zhangpu",
          value: "漳浦县",
          leaf: false,
          level: 7
        },
        {
          label: "Zhaoan",
          value: "诏安县",
          leaf: false,
          level: 7
        },
        {
          label: "Dongshan",
          value: "东山县",
          leaf: false,
          level: 7
        },
        {
          label: "Nanjing",
          value: "南靖县",
          leaf: false,
          level: 7
        },
        {
          label: "Pinghe",
          value: "平和县",
          leaf: false,
          level: 7
        },
        {
          label: "Huaan",
          value: "华安县",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Nanping",
      value: "南平市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Yanping",
          value: "延平区",
          leaf: false,
          level: 7
        },
        {
          label: "Jianyang",
          value: "建阳区",
          leaf: false,
          level: 7
        },
        {
          label: "Shunchang",
          value: "顺昌县",
          leaf: false,
          level: 7
        },
        {
          label: "Pucheng",
          value: "浦城县",
          leaf: false,
          level: 7
        },
        {
          label: "Guangze",
          value: "光泽县",
          leaf: false,
          level: 7
        },
        {
          label: "Songxi",
          value: "松溪县",
          leaf: false,
          level: 7
        },
        {
          label: "Zhenghe",
          value: "政和县",
          leaf: false,
          level: 7
        },
        {
          label: "Shaowu",
          value: "邵武市",
          leaf: false,
          level: 7
        },
        {
          label: "Wuyishan",
          value: "武夷山市",
          leaf: false,
          level: 7
        },
        {
          label: "Jianou",
          value: "建瓯市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Longyan",
      value: "龙岩市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Xinluo",
          value: "新罗区",
          leaf: false,
          level: 7
        },
        {
          label: "Yongding",
          value: "永定区",
          leaf: false,
          level: 7
        },
        {
          label: "Changting",
          value: "长汀县",
          leaf: false,
          level: 7
        },
        {
          label: "Shanghang",
          value: "上杭县",
          leaf: false,
          level: 7
        },
        {
          label: "Wuping",
          value: "武平县",
          leaf: false,
          level: 7
        },
        {
          label: "Liancheng",
          value: "连城县",
          leaf: false,
          level: 7
        },
        {
          label: "Zhangping",
          value: "漳平市",
          leaf: false,
          level: 7
        }
      ]
    },
    {
      label: "Ningde",
      value: "宁德市",
      leaf: false,
      level: 6,
      children: [
        {
          label: "County",
          value: "县级",
          leaf: false,
          level: 7
        },
        {
          label: "Jiaocheng",
          value: "蕉城区",
          leaf: false,
          level: 7
        },
        {
          label: "Gutian",
          value: "古田县",
          leaf: false,
          level: 7
        },
        {
          label: "Pingnan",
          value: "屏南县",
          leaf: false,
          level: 7
        },
        {
          label: "Zhouning",
          value: "周宁县",
          leaf: false,
          level: 7
        },
        {
          label: "Shouning",
          value: "寿宁县",
          leaf: false,
          level: 7
        },
        {
          label: "Fuan",
          value: "福安市",
          leaf: false,
          level: 7
        },
        {
          label: "Zherong",
          value: "柘荣县",
          leaf: false,
          level: 7
        },
        {
          label: "Fuding",
          value: "福鼎市",
          leaf: false,
          level: 7
        },
        {
          label: "Xiapu",
          value: "霞浦县",
          leaf: false,
          level: 7
        }
      ]
    }
  ]
},


                ],
              },
              {
                label: "North China",
                value: "华北",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "Province",
                    value: "省级",
                    leaf: true,
                    level: 5,

                  },
{
label: "Beijing",
value: "北京市",
leaf: false,
level: 5,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Dongcheng",
value: "东城区",
leaf: false,
level: 7
},
{
label: "Xicheng",
value: "西城区",
leaf: false,
level: 7
},
{
label: "Chaoyang",
value: "朝阳区",
leaf: false,
level: 7
},
{
label: "Fengtai",
value: "丰台区",
leaf: false,
level: 7
},
{
label: "Shijingshan",
value: "石景山区",
leaf: false,
level: 7
},
{
label: "Haidian",
value: "海淀区",
leaf: false,
level: 7
},
{
label: "Shunyi",
value: "顺义区",
leaf: false,
level: 7
},
{
label: "Tongzhou",
value: "通州区",
leaf: false,
level: 7
},
{
label: "Daxing",
value: "大兴区",
leaf: false,
level: 7
},
{
label: "Fangshan",
value: "房山区",
leaf: false,
level: 7
},
{
label: "Mentougou",
value: "门头沟区",
leaf: false,
level: 7
},
{
label: "Changping",
value: "昌平区",
leaf: false,
level: 7
},
{
label: "Pinggu",
value: "平谷区",
leaf: false,
level: 7
},
{
label: "Miyun",
value: "密云区",
leaf: false,
level: 7
},
{
label: "Huairou",
value: "怀柔区",
leaf: false,
level: 7
},
{
label: "Yanqing",
value: "延庆区",
leaf: false,
level: 7
}
]
},
{
label: "Tianjin",
value: "天津市",
leaf: false,
level: 5,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Heping",
value: "和平区",
leaf: false,
level: 7
},
{
label: "Hedong",
value: "河东区",
leaf: false,
level: 7
},
{
label: "Hexi",
value: "河西区",
leaf: false,
level: 7
},
{
label: "Nankai",
value: "南开区",
leaf: false,
level: 7
},
{
label: "Hebei",
value: "河北区",
leaf: false,
level: 7
},
{
label: "Hongqiao",
value: "红桥区",
leaf: false,
level: 7
},
{
label: "Dongli",
value: "东丽区",
leaf: false,
level: 7
},
{
label: "Xiqing",
value: "西青区",
leaf: false,
level: 7
},
{
label: "Jinnan",
value: "津南区",
leaf: false,
level: 7
},
{
label: "Beichen",
value: "北辰区",
leaf: false,
level: 7
},
{
label: "Wuqing",
value: "武清区",
leaf: false,
level: 7
},
{
label: "Baodi",
value: "宝坻区",
leaf: false,
level: 7
},
{
label: "Binhaixin",
value: "滨海新区",
leaf: false,
level: 7
},
{
label: "Ninghe",
value: "宁河区",
leaf: false,
level: 7
},
{
label: "Jinghai",
value: "静海区",
leaf: false,
level: 7
},
{
label: "Jizhou",
value: "蓟州区",
leaf: false,
level: 7
}
]
},
{
label: "Hebei",
value: "河北省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Shijiazhuang",
value: "石家庄市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Qiaoxi",
value: "桥西区",
leaf: false,
level: 7
},
{
label: "Xinhua",
value: "新华区",
leaf: false,
level: 7
},
{
label: "Jingxingkuang",
value: "井陉矿区",
leaf: false,
level: 7
},
{
label: "Yuhua",
value: "裕华区",
leaf: false,
level: 7
},
{
label: "Gaocheng",
value: "藁城区",
leaf: false,
level: 7
},
{
label: "Luquan",
value: "鹿泉区",
leaf: false,
level: 7
},
{
label: "Luancheng",
value: "栾城区",
leaf: false,
level: 7
},
{
label: "Jingxing",
value: "井陉县",
leaf: false,
level: 7
},
{
label: "Zhengding",
value: "正定县",
leaf: false,
level: 7
},
{
label: "Xingtang",
value: "行唐县",
leaf: false,
level: 7
},
{
label: "Lingshou",
value: "灵寿县",
leaf: false,
level: 7
},
{
label: "Gaoyi",
value: "高邑县",
leaf: false,
level: 7
},
{
label: "Shenze",
value: "深泽县",
leaf: false,
level: 7
},
{
label: "Zanhuang",
value: "赞皇县",
leaf: false,
level: 7
},
{
label: "Wuji",
value: "无极县",
leaf: false,
level: 7
},
{
label: "Pingshan",
value: "平山县",
leaf: false,
level: 7
},
{
label: "Yuanshi",
value: "元氏县",
leaf: false,
level: 7
},
{
label: "Zhaoxian",
value: "赵县",
leaf: false,
level: 7
},
{
label: "Jinzhou",
value: "晋州市",
leaf: false,
level: 7
},
{
label: "Xinle",
value: "新乐市",
leaf: false,
level: 7
},
{
label: "Xinji",
value: "辛集市",
leaf: false,
level: 7
}
]
},
{
label: "Tangshan",
value: "唐山市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Lunan",
value: "路南区",
leaf: false,
level: 7
},
{
label: "Lubei",
value: "路北区",
leaf: false,
level: 7
},
{
label: "Guye",
value: "古冶区",
leaf: false,
level: 7
},
{
label: "Kaiping",
value: "开平区",
leaf: false,
level: 7
},
{
label: "Fengnan",
value: "丰南区",
leaf: false,
level: 7
},
{
label: "Fengrun",
value: "丰润区",
leaf: false,
level: 7
},
{
label: "Caofeidian",
value: "曹妃甸区",
leaf: false,
level: 7
},
{
label: "Luanzhou",
value: "滦州市",
leaf: false,
level: 7
},
{
label: "Zunhua",
value: "遵化市",
leaf: false,
level: 7
},
{
label: "Qianan",
value: "迁安市",
leaf: false,
level: 7
},
{
label: "Luannan",
value: "滦南县",
leaf: false,
level: 7
},
{
label: "Leting",
value: "乐亭县",
leaf: false,
level: 7
},
{
label: "Qianxi",
value: "迁西县",
leaf: false,
level: 7
},
{
label: "Yutian",
value: "玉田县",
leaf: false,
level: 7
}
]
},
{
label: "Qinhuangdao",
value: "秦皇岛市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Haigang",
value: "海港区",
leaf: false,
level: 7
},
{
label: "Shanhaiguan",
value: "山海关区",
leaf: false,
level: 7
},
{
label: "Beidaihe",
value: "北戴河区",
leaf: false,
level: 7
},
{
label: "Funing",
value: "抚宁区",
leaf: false,
level: 7
},
{
label: "Qinglongmanzu",
value: "青龙满族自治县",
leaf: false,
level: 7
},
{
label: "Changli",
value: "昌黎县",
leaf: false,
level: 7
},
{
label: "Lulong",
value: "卢龙县",
leaf: false,
level: 7
}
]
},
{
label: "Handan",
value: "邯郸市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Hanshan",
value: "邯山区",
leaf: false,
level: 7
},
{
label: "Congtai",
value: "丛台区",
leaf: false,
level: 7
},
{
label: "Fuxing",
value: "复兴区",
leaf: false,
level: 7
},
{
label: "Fengfengkuang",
value: "峰峰矿区",
leaf: false,
level: 7
},
{
label: "Feixiang",
value: "肥乡区",
leaf: false,
level: 7
},
{
label: "Yongnian",
value: "永年区",
leaf: false,
level: 7
},
{
label: "Linzhang",
value: "临漳县",
leaf: false,
level: 7
},
{
label: "Chengan",
value: "成安县",
leaf: false,
level: 7
},
{
label: "Daming",
value: "大名县",
leaf: false,
level: 7
},
{
label: "Shexian",
value: "涉县",
leaf: false,
level: 7
},
{
label: "Cixian",
value: "磁县",
leaf: false,
level: 7
},
{
label: "Qiuxian",
value: "邱县",
leaf: false,
level: 7
},
{
label: "Jize",
value: "鸡泽县",
leaf: false,
level: 7
},
{
label: "Guangping",
value: "广平县",
leaf: false,
level: 7
},
{
label: "Guantao",
value: "馆陶县",
leaf: false,
level: 7
},
{
label: "Weixian",
value: "魏县",
leaf: false,
level: 7
},
{
label: "Quzhou",
value: "曲周县",
leaf: false,
level: 7
},
{
label: "Wuan",
value: "武安市",
leaf: false,
level: 7
}
]
},
{
label: "Xingtai",
value: "邢台市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xiangdu",
value: "襄都区",
leaf: false,
level: 7
},
{
label: "Xindu",
value: "信都区",
leaf: false,
level: 7
},
{
label: "Renze",
value: "任泽区",
leaf: false,
level: 7
},
{
label: "Nanhe",
value: "南和区",
leaf: false,
level: 7
},
{
label: "Lincheng",
value: "临城县",
leaf: false,
level: 7
},
{
label: "Neiqiu",
value: "内丘县",
leaf: false,
level: 7
},
{
label: "Baixiang",
value: "柏乡县",
leaf: false,
level: 7
},
{
label: "Longyao",
value: "隆尧县",
leaf: false,
level: 7
},
{
label: "Ningjin",
value: "宁晋县",
leaf: false,
level: 7
},
{
label: "Julu",
value: "巨鹿县",
leaf: false,
level: 7
},
{
label: "Xinhe",
value: "新河县",
leaf: false,
level: 7
},
{
label: "Guangzong",
value: "广宗县",
leaf: false,
level: 7
},
{
label: "Pingxiang",
value: "平乡县",
leaf: false,
level: 7
},
{
label: "Weixian",
value: "威县",
leaf: false,
level: 7
},
{
label: "Qinghe",
value: "清河县",
leaf: false,
level: 7
},
{
label: "Linxi",
value: "临西县",
leaf: false,
level: 7
},
{
label: "Nangong",
value: "南宫市",
leaf: false,
level: 7
},
{
label: "Shahe",
value: "沙河市",
leaf: false,
level: 7
}
]
},
{
label: "Baoding",
value: "保定市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Jingxiu",
value: "竞秀区",
leaf: false,
level: 7
},
{
label: "Lianchi",
value: "莲池区",
leaf: false,
level: 7
},
{
label: "Mancheng",
value: "满城区",
leaf: false,
level: 7
},
{
label: "Qingyuan",
value: "清苑区",
leaf: false,
level: 7
},
{
label: "Xushui",
value: "徐水区",
leaf: false,
level: 7
},
{
label: "Laishui",
value: "涞水县",
leaf: false,
level: 7
},
{
label: "Fuping",
value: "阜平县",
leaf: false,
level: 7
},
{
label: "Dingxing",
value: "定兴县",
leaf: false,
level: 7
},
{
label: "Tangxian",
value: "唐县",
leaf: false,
level: 7
},
{
label: "Gaoyang",
value: "高阳县",
leaf: false,
level: 7
},
{
label: "Rongcheng",
value: "容城县",
leaf: false,
level: 7
},
{
label: "Laiyuan",
value: "涞源县",
leaf: false,
level: 7
},
{
label: "Wangdu",
value: "望都县",
leaf: false,
level: 7
},
{
label: "Anxin",
value: "安新县",
leaf: false,
level: 7
},
{
label: "Yixian",
value: "易县",
leaf: false,
level: 7
},
{
label: "Quyang",
value: "曲阳县",
leaf: false,
level: 7
},
{
label: "Lixian",
value: "蠡县",
leaf: false,
level: 7
},
{
label: "Shunping",
value: "顺平县",
leaf: false,
level: 7
},
{
label: "Boye",
value: "博野县",
leaf: false,
level: 7
},
{
label: "Xiongxian",
value: "雄县",
leaf: false,
level: 7
},
{
label: "Zhuozhou",
value: "涿州市",
leaf: false,
level: 7
},
{
label: "Anguo",
value: "安国市",
leaf: false,
level: 7
},
{
label: "Gaobeidian",
value: "高碑店市",
leaf: false,
level: 7
}
]
},
{
label: "Zhangjiakou",
value: "张家口市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Qiaodong",
value: "桥东区",
leaf: false,
level: 7
},
{
label: "Qiaoxi",
value: "桥西区",
leaf: false,
level: 7
},
{
label: "Xuanhua",
value: "宣化区",
leaf: false,
level: 7
},
{
label: "Xiahuayuan",
value: "下花园区",
leaf: false,
level: 7
},
{
label: "Wanquan",
value: "万全区",
leaf: false,
level: 7
},
{
label: "Chongli",
value: "崇礼区",
leaf: false,
level: 7
},
{
label: "Zhangbei",
value: "张北县",
leaf: false,
level: 7
},
{
label: "Kangbao",
value: "康保县",
leaf: false,
level: 7
},
{
label: "Guyuan",
value: "沽源县",
leaf: false,
level: 7
},
{
label: "Shangyi",
value: "尚义县",
leaf: false,
level: 7
},
{
label: "Yuxian",
value: "蔚县",
leaf: false,
level: 7
},
{
label: "Yangyuan",
value: "阳原县",
leaf: false,
level: 7
},
{
label: "Huaian",
value: "怀安县",
leaf: false,
level: 7
},
{
label: "Huailai",
value: "怀来县",
leaf: false,
level: 7
},
{
label: "Zhuolu",
value: "涿鹿县",
leaf: false,
level: 7
},
{
label: "Chicheng",
value: "赤城县",
leaf: false,
level: 7
}
]
},
{
label: "Chengde",
value: "承德市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Shuangqiao",
value: "双桥区",
leaf: false,
level: 7
},
{
label: "Shuangluan",
value: "双滦区",
leaf: false,
level: 7
},
{
label: "Yingshouyingzikuang",
value: "鹰手营子矿区",
leaf: false,
level: 7
},
{
label: "Chengde",
value: "承德县",
leaf: false,
level: 7
},
{
label: "Xinglong",
value: "兴隆县",
leaf: false,
level: 7
},
{
label: "Pingquan",
value: "平泉市",
leaf: false,
level: 7
},
{
label: "Luanping",
value: "滦平县",
leaf: false,
level: 7
},
{
label: "Longhua",
value: "隆化县",
leaf: false,
level: 7
},
{
label: "Fengningmanzu",
value: "丰宁满族自治县",
leaf: false,
level: 7
},
{
label: "Kuanchengmanzu",
value: "宽城满族自治县",
leaf: false,
level: 7
},
{
label: "Weichangmanzuandmengguzu",
value: "围场满族蒙古族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Cangzhou",
value: "沧州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xinhua",
value: "新华区",
leaf: false,
level: 7
},
{
label: "Yunhe",
value: "运河区",
leaf: false,
level: 7
},
{
label: "Cangxian",
value: "沧县",
leaf: false,
level: 7
},
{
label: "Qingxian",
value: "青县",
leaf: false,
level: 7
},
{
label: "Dongguang",
value: "东光县",
leaf: false,
level: 7
},
{
label: "Haixing",
value: "海兴县",
leaf: false,
level: 7
},
{
label: "Yanshan",
value: "盐山县",
leaf: false,
level: 7
},
{
label: "Suning",
value: "肃宁县",
leaf: false,
level: 7
},
{
label: "Nanpi",
value: "南皮县",
leaf: false,
level: 7
},
{
label: "Wuqiao",
value: "吴桥县",
leaf: false,
level: 7
},
{
label: "Xianxian",
value: "献县",
leaf: false,
level: 7
},
{
label: "Mengcunhuizu",
value: "孟村回族自治县",
leaf: false,
level: 7
},
{
label: "Botou",
value: "泊头市",
leaf: false,
level: 7
},
{
label: "Renqiu",
value: "任丘市",
leaf: false,
level: 7
},
{
label: "Huanghua",
value: "黄骅市",
leaf: false,
level: 7
},
{
label: "Hejian",
value: "河间市",
leaf: false,
level: 7
}
]
},
{
label: "Langfang",
value: "廊坊市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Anci",
value: "安次区",
leaf: false,
level: 7
},
{
label: "Guangyang",
value: "广阳区",
leaf: false,
level: 7
},
{
label: "Guan",
value: "固安县",
leaf: false,
level: 7
},
{
label: "Yongqing",
value: "永清县",
leaf: false,
level: 7
},
{
label: "Xianghe",
value: "香河县",
leaf: false,
level: 7
},
{
label: "Dacheng",
value: "大城县",
leaf: false,
level: 7
},
{
label: "Wenan",
value: "文安县",
leaf: false,
level: 7
},
{
label: "Dachanghuizu",
value: "大厂回族自治县",
leaf: false,
level: 7
},
{
label: "Bazhou",
value: "霸州市",
leaf: false,
level: 7
},
{
label: "Sanhe",
value: "三河市",
leaf: false,
level: 7
}
]
},
{
label: "Hengshui",
value: "衡水市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Taocheng",
value: "桃城区",
leaf: false,
level: 7
},
{
label: "Jizhou",
value: "冀州区",
leaf: false,
level: 7
},
{
label: "Zaoqiang",
value: "枣强县",
leaf: false,
level: 7
},
{
label: "Wuyi",
value: "武邑县",
leaf: false,
level: 7
},
{
label: "Wuqiang",
value: "武强县",
leaf: false,
level: 7
},
{
label: "Raoyang",
value: "饶阳县",
leaf: false,
level: 7
},
{
label: "Anping",
value: "安平县",
leaf: false,
level: 7
},
{
label: "Gucheng",
value: "故城县",
leaf: false,
level: 7
},
{
label: "Jingxian",
value: "景县",
leaf: false,
level: 7
},
{
label: "Fucheng",
value: "阜城县",
leaf: false,
level: 7
}
]
}
]
},
{
label: "Shanxi",
value: "山西省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Taiyuan",
value: "太原市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xiaodian",
value: "小店区",
leaf: false,
level: 7
},
{
label: "Yingze",
value: "迎泽区",
leaf: false,
level: 7
},
{
label: "Xinghualing",
value: "杏花岭区",
leaf: false,
level: 7
},
{
label: "Jiancaoping",
value: "尖草坪区",
leaf: false,
level: 7
},
{
label: "Wanbailin",
value: "万柏林区",
leaf: false,
level: 7
},
{
label: "Jinyuan",
value: "晋源区",
leaf: false,
level: 7
},
{
label: "Yangqu",
value: "阳曲县",
leaf: false,
level: 7
},
{
label: "Loufan",
value: "娄烦县",
leaf: false,
level: 7
},
{
label: "Gujiao",
value: "古交市",
leaf: false,
level: 7
}
]
},
{
label: "Datong",
value: "大同市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Pingcheng",
value: "平城区",
leaf: false,
level: 7
},
{
label: "Yungang",
value: "云冈区",
leaf: false,
level: 7
},
{
label: "Xinrong",
value: "新荣区",
leaf: false,
level: 7
},
{
label: "Yunzhou",
value: "云州区",
leaf: false,
level: 7
},
{
label: "Yanggao",
value: "阳高县",
leaf: false,
level: 7
},
{
label: "Tianzhen",
value: "天镇县",
leaf: false,
level: 7
},
{
label: "Guangling",
value: "广灵县",
leaf: false,
level: 7
},
{
label: "Lingqiu",
value: "灵丘县",
leaf: false,
level: 7
},
{
label: "Hunyuan",
value: "浑源县",
leaf: false,
level: 7
},
{
label: "Zuoyun",
value: "左云县",
leaf: false,
level: 7
}
]
},
{
label: "Yangquan",
value: "阳泉市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Chengqu",
value: "城区",
leaf: false,
level: 7
},
{
label: "Kuangqu",
value: "矿区",
leaf: false,
level: 7
},
{
label: "Jiaoqu",
value: "郊区",
leaf: false,
level: 7
},
{
label: "Pingding",
value: "平定县",
leaf: false,
level: 7
},
{
label: "Yuxian",
value: "盂县",
leaf: false,
level: 7
}
]
},
{
label: "Changzhi",
value: "长治市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Luzhou",
value: "潞州区",
leaf: false,
level: 7
},
{
label: "Shangdang",
value: "上党区",
leaf: false,
level: 7
},
{
label: "Tunliu",
value: "屯留区",
leaf: false,
level: 7
},
{
label: "Lucheng",
value: "潞城区",
leaf: false,
level: 7
},
{
label: "Xiangyuan",
value: "襄垣县",
leaf: false,
level: 7
},
{
label: "Pingshun",
value: "平顺县",
leaf: false,
level: 7
},
{
label: "Licheng",
value: "黎城县",
leaf: false,
level: 7
},
{
label: "Huguan",
value: "壶关县",
leaf: false,
level: 7
},
{
label: "Zhangzi",
value: "长子县",
leaf: false,
level: 7
},
{
label: "Wuxiang",
value: "武乡县",
leaf: false,
level: 7
},
{
label: "Qinxian",
value: "沁县",
leaf: false,
level: 7
},
{
label: "Qinyuan",
value: "沁源县",
leaf: false,
level: 7
}
]
},
{
label: "Jincheng",
value: "晋城市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Chengqu",
value: "城区",
leaf: false,
level: 7
},
{
label: "Qinshui",
value: "沁水县",
leaf: false,
level: 7
},
{
label: "Yangcheng",
value: "阳城县",
leaf: false,
level: 7
},
{
label: "Lingchuan",
value: "陵川县",
leaf: false,
level: 7
},
{
label: "Zezehou",
value: "泽州县",
leaf: false,
level: 7
},
{
label: "Gaoping",
value: "高平市",
leaf: false,
level: 7
}
]
},
{
label: "Shuozhou",
value: "朔州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Shuocheng",
value: "朔城区",
leaf: false,
level: 7
},
{
label: "Pinglu",
value: "平鲁区",
leaf: false,
level: 7
},
{
label: "Shanyin",
value: "山阴县",
leaf: false,
level: 7
},
{
label: "Yingxian",
value: "应县",
leaf: false,
level: 7
},
{
label: "Youyu",
value: "右玉县",
leaf: false,
level: 7
},
{
label: "Huairen",
value: "怀仁市",
leaf: false,
level: 7
}
]
},
{
label: "Jinzhong",
value: "晋中市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yuci",
value: "榆次区",
leaf: false,
level: 7
},
{
label: "Taigu",
value: "太谷区",
leaf: false,
level: 7
},
{
label: "Yushe",
value: "榆社县",
leaf: false,
level: 7
},
{
label: "Zuoquan",
value: "左权县",
leaf: false,
level: 7
},
{
label: "Heshun",
value: "和顺县",
leaf: false,
level: 7
},
{
label: "Xiyang",
value: "昔阳县",
leaf: false,
level: 7
},
{
label: "Shouyang",
value: "寿阳县",
leaf: false,
level: 7
},
{
label: "Qixian",
value: "祁县",
leaf: false,
level: 7
},
{
label: "Pingyao",
value: "平遥县",
leaf: false,
level: 7
},
{
label: "Lingshi",
value: "灵石县",
leaf: false,
level: 7
},
{
label: "Jiexiu",
value: "介休市",
leaf: false,
level: 7
}
]
},
{
label: "Yuncheng",
value: "运城市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yanhu",
value: "盐湖区",
leaf: false,
level: 7
},
{
label: "Linyi",
value: "临猗县",
leaf: false,
level: 7
},
{
label: "Wanrong",
value: "万荣县",
leaf: false,
level: 7
},
{
label: "Wenxi",
value: "闻喜县",
leaf: false,
level: 7
},
{
label: "Jishan",
value: "稷山县",
leaf: false,
level: 7
},
{
label: "Xinjiang",
value: "新绛县",
leaf: false,
level: 7
},
{
label: "Jiangxian",
value: "绛县",
leaf: false,
level: 7
},
{
label: "Yuanqu",
value: "垣曲县",
leaf: false,
level: 7
},
{
label: "Xiaxian",
value: "夏县",
leaf: false,
level: 7
},
{
label: "Pinglu",
value: "平陆县",
leaf: false,
level: 7
},
{
label: "Ruicheng",
value: "芮城县",
leaf: false,
level: 7
},
{
label: "Yongji",
value: "永济市",
leaf: false,
level: 7
},
{
label: "Hejin",
value: "河津市",
leaf: false,
level: 7
}
]
},
{
label: "Xinzhou",
value: "忻州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xinfu",
value: "忻府区",
leaf: false,
level: 7
},
{
label: "Dingxiang",
value: "定襄县",
leaf: false,
level: 7
},
{
label: "Wutai",
value: "五台县",
leaf: false,
level: 7
},
{
label: "Daixian",
value: "代县",
leaf: false,
level: 7
},
{
label: "Fanshi",
value: "繁峙县",
leaf: false,
level: 7
},
{
label: "Ningwu",
value: "宁武县",
leaf: false,
level: 7
},
{
label: "Jingle",
value: "静乐县",
leaf: false,
level: 7
},
{
label: "Shenchi",
value: "神池县",
leaf: false,
level: 7
},
{
label: "Wuzhai",
value: "五寨县",
leaf: false,
level: 7
},
{
label: "Kelan",
value: "岢岚县",
leaf: false,
level: 7
},
{
label: "Hequ",
value: "河曲县",
leaf: false,
level: 7
},
{
label: "Baode",
value: "保德县",
leaf: false,
level: 7
},
{
label: "Pianguan",
value: "偏关县",
leaf: false,
level: 7
},
{
label: "Yuanping",
value: "原平市",
leaf: false,
level: 7
}
]
},
{
label: "Linfen",
value: "临汾市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yaodu",
value: "尧都区",
leaf: false,
level: 7
},
{
label: "Quwo",
value: "曲沃县",
leaf: false,
level: 7
},
{
label: "Yicheng",
value: "翼城县",
leaf: false,
level: 7
},
{
label: "Xiangfen",
value: "襄汾县",
leaf: false,
level: 7
},
{
label: "Hongdong",
value: "洪洞县",
leaf: false,
level: 7
},
{
label: "Guxian",
value: "古县",
leaf: false,
level: 7
},
{
label: "Anze",
value: "安泽县",
leaf: false,
level: 7
},
{
label: "Fushan",
value: "浮山县",
leaf: false,
level: 7
},
{
label: "Jixian",
value: "吉县",
leaf: false,
level: 7
},
{
label: "Xiangning",
value: "乡宁县",
leaf: false,
level: 7
},
{
label: "Daning",
value: "大宁县",
leaf: false,
level: 7
},
{
label: "Xixian",
value: "隰县",
leaf: false,
level: 7
},
{
label: "Yonghe",
value: "永和县",
leaf: false,
level: 7
},
{
label: "Puxian",
value: "蒲县",
leaf: false,
level: 7
},
{
label: "Fenxi",
value: "汾西县",
leaf: false,
level: 7
},
{
label: "Houma",
value: "侯马市",
leaf: false,
level: 7
},
{
label: "Huozhou",
value: "霍州市",
leaf: false,
level: 7
}
]
},
{
label: "Lüliang",
value: "吕梁市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Lishi",
value: "离石区",
leaf: false,
level: 7
},
{
label: "Wenshui",
value: "文水县",
leaf: false,
level: 7
},
{
label: "Jiaocheng",
value: "交城县",
leaf: false,
level: 7
},
{
label: "Xingxian",
value: "兴县",
leaf: false,
level: 7
},
{
label: "Linxian",
value: "临县",
leaf: false,
level: 7
},
{
label: "Liulin",
value: "柳林县",
leaf: false,
level: 7
},
{
label: "Shilou",
value: "石楼县",
leaf: false,
level: 7
},
{
label: "Lanxian",
value: "岚县",
leaf: false,
level: 7
},
{
label: "Fangshan",
value: "方山县",
leaf: false,
level: 7
},
{
label: "Zhongyang",
value: "中阳县",
leaf: false,
level: 7
},
{
label: "Jiaokou",
value: "交口县",
leaf: false,
level: 7
},
{
label: "Xiaoyi",
value: "孝义市",
leaf: false,
level: 7
},
{
label: "Fenyang",
value: "汾阳市",
leaf: false,
level: 7
}
]
}
]
},
{
label: "Shandong",
value: "山东省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Jinan",
value: "济南市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Lixia",
value: "历下区",
leaf: false,
level: 7
},
{
label: "Licheng",
value: "历城区",
leaf: false,
level: 7
},
{
label: "Zhangqiu",
value: "章丘区",
leaf: false,
level: 7
},
{
label: "Huaiyin",
value: "槐荫区",
leaf: false,
level: 7
},
{
label: "Tianqiao",
value: "天桥区",
leaf: false,
level: 7
},
{
label: "Shizhong",
value: "市中区",
leaf: false,
level: 7
},
{
label: "Changqing",
value: "长清区",
leaf: false,
level: 7
},
{
label: "Laiwu",
value: "莱芜区",
leaf: false,
level: 7
},
{
label: "Gangcheng",
value: "钢城区",
leaf: false,
level: 7
},
{
label: "Pingyin",
value: "平阴县",
leaf: false,
level: 7
},
{
label: "Shanghe",
value: "商河县",
leaf: false,
level: 7
}
]
},
{
label: "Qingdao",
value: "青岛市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Shinan",
value: "市南区",
leaf: false,
level: 7
},
{
label: "Shibei",
value: "市北区",
leaf: false,
level: 7
},
{
label: "Licang",
value: "李沧区",
leaf: false,
level: 7
},
{
label: "Laoshan",
value: "崂山区",
leaf: false,
level: 7
},
{
label: "Huangdao",
value: "黄岛区",
leaf: false,
level: 7
},
{
label: "Chengyang",
value: "城阳区",
leaf: false,
level: 7
},
{
label: "Jimo",
value: "即墨区",
leaf: false,
level: 7
},
{
label: "Jiaozhou",
value: "胶州市",
leaf: false,
level: 7
},
{
label: "Pingdu",
value: "平度市",
leaf: false,
level: 7
},
{
label: "Laixi",
value: "莱西市",
leaf: false,
level: 7
}
]
},
{
label: "Zibo",
value: "淄博市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Zichuan",
value: "淄川区",
leaf: false,
level: 7
},
{
label: "Zhangdian",
value: "张店区",
leaf: false,
level: 7
},
{
label: "Boshan",
value: "博山区",
leaf: false,
level: 7
},
{
label: "Linzi",
value: "临淄区",
leaf: false,
level: 7
},
{
label: "Zhoucun",
value: "周村区",
leaf: false,
level: 7
},
{
label: "Huantai",
value: "桓台县",
leaf: false,
level: 7
},
{
label: "Gaoqing",
value: "高青县",
leaf: false,
level: 7
},
{
label: "Yiyuan",
value: "沂源县",
leaf: false,
level: 7
}
]
},
{
label: "Zaozhuang",
value: "枣庄市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Shizhong",
value: "市中区",
leaf: false,
level: 7
},
{
label: "Xuecheng",
value: "薛城区",
leaf: false,
level: 7
},
{
label: "Yicheng",
value: "峄城区",
leaf: false,
level: 7
},
{
label: "Taierzhuang",
value: "台儿庄区",
leaf: false,
level: 7
},
{
label: "Shanting",
value: "山亭区",
leaf: false,
level: 7
},
{
label: "Tengzhou",
value: "滕州市",
leaf: false,
level: 7
}
]
},
{
label: "Dongying",
value: "东营市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Dongying",
value: "东营区",
leaf: false,
level: 7
},
{
label: "Hekou",
value: "河口区",
leaf: false,
level: 7
},
{
label: "Kenli",
value: "垦利区",
leaf: false,
level: 7
},
{
label: "Lijin",
value: "利津县",
leaf: false,
level: 7
},
{
label: "Guangrao",
value: "广饶县",
leaf: false,
level: 7
}
]
},
{
label: "Yantai",
value: "烟台市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Zhifu",
value: "芝罘区",
leaf: false,
level: 7
},
{
label: "Fushan",
value: "福山区",
leaf: false,
level: 7
},
{
label: "Muping",
value: "牟平区",
leaf: false,
level: 7
},
{
label: "Laishan",
value: "莱山区",
leaf: false,
level: 7
},
{
label: "Penglai",
value: "蓬莱区",
leaf: false,
level: 7
},
{
label: "Longkou",
value: "龙口市",
leaf: false,
level: 7
},
{
label: "Laiyang",
value: "莱阳市",
leaf: false,
level: 7
},
{
label: "Laizhou",
value: "莱州市",
leaf: false,
level: 7
},
{
label: "Zhaoyuan",
value: "招远市",
leaf: false,
level: 7
},
{
label: "Qixia",
value: "栖霞市",
leaf: false,
level: 7
},
{
label: "Haiyang",
value: "海阳市",
leaf: false,
level: 7
}
]
},
{
label: "Weifang",
value: "潍坊市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Weicheng",
value: "潍城区",
leaf: false,
level: 7
},
{
label: "Hanting",
value: "寒亭区",
leaf: false,
level: 7
},
{
label: "Fangzi",
value: "坊子区",
leaf: false,
level: 7
},
{
label: "Kuiwen",
value: "奎文区",
leaf: false,
level: 7
},
{
label: "Qingzhou",
value: "青州市",
leaf: false,
level: 7
},
{
label: "Zhucheng",
value: "诸城市",
leaf: false,
level: 7
},
{
label: "Shouguang",
value: "寿光市",
leaf: false,
level: 7
},
{
label: "Anqiu",
value: "安丘市",
leaf: false,
level: 7
},
{
label: "Gaomi",
value: "高密市",
leaf: false,
level: 7
},
{
label: "Changyi",
value: "昌邑市",
leaf: false,
level: 7
},
{
label: "Linqu",
value: "临朐县",
leaf: false,
level: 7
},
{
label: "Changle",
value: "昌乐县",
leaf: false,
level: 7
}
]
},
{
label: "Jining",
value: "济宁市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Rencheng",
value: "任城区",
leaf: false,
level: 7
},
{
label: "Yanzhou",
value: "兖州区",
leaf: false,
level: 7
},
{
label: "Qufu",
value: "曲阜市",
leaf: false,
level: 7
},
{
label: "Zoucheng",
value: "邹城市",
leaf: false,
level: 7
},
{
label: "Weishan",
value: "微山县",
leaf: false,
level: 7
},
{
label: "Yutai",
value: "鱼台县",
leaf: false,
level: 7
},
{
label: "Jinxiang",
value: "金乡县",
leaf: false,
level: 7
},
{
label: "Jiaxiang",
value: "嘉祥县",
leaf: false,
level: 7
},
{
label: "Wenshang",
value: "汶上县",
leaf: false,
level: 7
},
{
label: "Sishui",
value: "泗水县",
leaf: false,
level: 7
},
{
label: "Liangshan",
value: "梁山县",
leaf: false,
level: 7
}
]
},
{
label: "Taian",
value: "泰安市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Taishan",
value: "泰山区",
leaf: false,
level: 7
},
{
label: "Daiyue",
value: "岱岳区",
leaf: false,
level: 7
},
{
label: "Xintai",
value: "新泰市",
leaf: false,
level: 7
},
{
label: "Feicheng",
value: "肥城市",
leaf: false,
level: 7
},
{
label: "Ningyang",
value: "宁阳县",
leaf: false,
level: 7
},
{
label: "Dongping",
value: "东平县",
leaf: false,
level: 7
}
]
},
{
label: "Weihai",
value: "威海市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Huancui",
value: "环翠区",
leaf: false,
level: 7
},
{
label: "Wendeng",
value: "文登区",
leaf: false,
level: 7
},
{
label: "Rongcheng",
value: "荣成市",
leaf: false,
level: 7
},
{
label: "Rushan",
value: "乳山市",
leaf: false,
level: 7
}
]
},
{
label: "Rizhao",
value: "日照市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Donggang",
value: "东港区",
leaf: false,
level: 7
},
{
label: "Lanshan",
value: "岚山区",
leaf: false,
level: 7
},
{
label: "Wulian",
value: "五莲县",
leaf: false,
level: 7
},
{
label: "Juxian",
value: "莒县",
leaf: false,
level: 7
}
]
},
{
label: "Linyi",
value: "临沂市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Lanshan",
value: "兰山区",
leaf: false,
level: 7
},
{
label: "Luozhuang",
value: "罗庄区",
leaf: false,
level: 7
},
{
label: "Hedong",
value: "河东区",
leaf: false,
level: 7
},
{
label: "Yinan",
value: "沂南县",
leaf: false,
level: 7
},
{
label: "Tancheng",
value: "郯城县",
leaf: false,
level: 7
},
{
label: "Yishui",
value: "沂水县",
leaf: false,
level: 7
},
{
label: "Lanling",
value: "兰陵县",
leaf: false,
level: 7
},
{
label: "Feixian",
value: "费县",
leaf: false,
level: 7
},
{
label: "Pingyi",
value: "平邑县",
leaf: false,
level: 7
},
{
label: "Junan",
value: "莒南县",
leaf: false,
level: 7
},
{
label: "Mengyin",
value: "蒙阴县",
leaf: false,
level: 7
},
{
label: "Linshu",
value: "临沭县",
leaf: false,
level: 7
}
]
},
{
label: "Dezhou",
value: "德州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Decheng",
value: "德城区",
leaf: false,
level: 7
},
{
label: "Lingcheng",
value: "陵城区",
leaf: false,
level: 7
},
{
label: "Leling",
value: "乐陵市",
leaf: false,
level: 7
},
{
label: "Yucheng",
value: "禹城市",
leaf: false,
level: 7
},
{
label: "Ningjin",
value: "宁津县",
leaf: false,
level: 7
},
{
label: "Qingyun",
value: "庆云县",
leaf: false,
level: 7
},
{
label: "Linyi",
value: "临邑县",
leaf: false,
level: 7
},
{
label: "Qihe",
value: "齐河县",
leaf: false,
level: 7
},
{
label: "Pingyuan",
value: "平原县",
leaf: false,
level: 7
},
{
label: "Xiajin",
value: "夏津县",
leaf: false,
level: 7
},
{
label: "Wucheng",
value: "武城县",
leaf: false,
level: 7
}
]
},
{
label: "Liaocheng",
value: "聊城市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Dongchangfu",
value: "东昌府区",
leaf: false,
level: 7
},
{
label: "Chiping",
value: "茌平区",
leaf: false,
level: 7
},
{
label: "Linqing",
value: "临清市",
leaf: false,
level: 7
},
{
label: "Yanggu",
value: "阳谷县",
leaf: false,
level: 7
},
{
label: "Shenxian",
value: "莘县",
leaf: false,
level: 7
},
{
label: "Dong'e",
value: "东阿县",
leaf: false,
level: 7
},
{
label: "Guanxian",
value: "冠县",
leaf: false,
level: 7
},
{
label: "Gaotang",
value: "高唐县",
leaf: false,
level: 7
}
]
},
{
label: "Binzhou",
value: "滨州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Bincheng",
value: "滨城区",
leaf: false,
level: 7
},
{
label: "Zhanhua",
value: "沾化区",
leaf: false,
level: 7
},
{
label: "Zouping",
value: "邹平市",
leaf: false,
level: 7
},
{
label: "Huimin",
value: "惠民县",
leaf: false,
level: 7
},
{
label: "Yangxin",
value: "阳信县",
leaf: false,
level: 7
},
{
label: "Wudi",
value: "无棣县",
leaf: false,
level: 7
},
{
label: "Boxing",
value: "博兴县",
leaf: false,
level: 7
}
]
},
{
label: "Heze",
value: "菏泽市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Mudan",
value: "牡丹区",
leaf: false,
level: 7
},
{
label: "Dingtao",
value: "定陶区",
leaf: false,
level: 7
},
{
label: "Caoxian",
value: "曹县",
leaf: false,
level: 7
},
{
label: "Shanxian",
value: "单县",
leaf: false,
level: 7
},
{
label: "Chengwu",
value: "成武县",
leaf: false,
level: 7
},
{
label: "Juye",
value: "巨野县",
leaf: false,
level: 7
},
{
label: "Yuncheng",
value: "郓城县",
leaf: false,
level: 7
},
{
label: "Juancheng",
value: "鄄城县",
leaf: false,
level: 7
},
{
label: "Dongming",
value: "东明县",
leaf: false,
level: 7
}
]
}
]
}

                ],
              },
              {
                label: "South China",
                value: "南方",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "Province",
                    value: "省级",
                    leaf: true,
                    level: 5,

                  },

  {
    label: "Guangdong",
    value: "广东省",
    leaf: false,
    level: 5,
    children: [
      {
        label: "Prefecture",
        value: "地级",
        leaf: false,
        level: 6
      },
      {
        label: "Guangzhou",
        value: "广州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Yuexiu",
            value: "越秀区",
            leaf: false,
            level: 7
          },
          {
            label: "Haizhu",
            value: "海珠区",
            leaf: false,
            level: 7
          },
          {
            label: "Liwan",
            value: "荔湾区",
            leaf: false,
            level: 7
          },
          {
            label: "Tianhe",
            value: "天河区",
            leaf: false,
            level: 7
          },
          {
            label: "Baiyun",
            value: "白云区",
            leaf: false,
            level: 7
          },
          {
            label: "Huangpu",
            value: "黄埔区",
            leaf: false,
            level: 7
          },
          {
            label: "Panyu",
            value: "番禺区",
            leaf: false,
            level: 7
          },
          {
            label: "Huadu",
            value: "花都区",
            leaf: false,
            level: 7
          },
          {
            label: "Nansha",
            value: "南沙区",
            leaf: false,
            level: 7
          },
          {
            label: "Conghua",
            value: "从化区",
            leaf: false,
            level: 7
          },
          {
            label: "Zengcheng",
            value: "增城区",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Shenzhen",
        value: "深圳市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Futian",
            value: "福田区",
            leaf: false,
            level: 7
          },
          {
            label: "Luohu",
            value: "罗湖区",
            leaf: false,
            level: 7
          },
          {
            label: "Yantian",
            value: "盐田区",
            leaf: false,
            level: 7
          },
          {
            label: "Nanshan",
            value: "南山区",
            leaf: false,
            level: 7
          },
          {
            label: "Baoan",
            value: "宝安区",
            leaf: false,
            level: 7
          },
          {
            label: "Longgang",
            value: "龙岗区",
            leaf: false,
            level: 7
          },
          {
            label: "Longhua",
            value: "龙华区",
            leaf: false,
            level: 7
          },
          {
            label: "Pingshan",
            value: "坪山区",
            leaf: false,
            level: 7
          },
          {
            label: "Guangming",
            value: "光明区",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Zhuhai",
        value: "珠海市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Xiangzhou",
            value: "香洲区",
            leaf: false,
            level: 7
          },
          {
            label: "Doumen",
            value: "斗门区",
            leaf: false,
            level: 7
          },
          {
            label: "Jinwan",
            value: "金湾区",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Shantou",
        value: "汕头市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Jinping",
            value: "金平区",
            leaf: false,
            level: 7
          },
          {
            label: "Longhu",
            value: "龙湖区",
            leaf: false,
            level: 7
          },
          {
            label: "Chenghai",
            value: "澄海区",
            leaf: false,
            level: 7
          },
          {
            label: "Haojiang",
            value: "濠江区",
            leaf: false,
            level: 7
          },
          {
            label: "Chaoyang",
            value: "潮阳区",
            leaf: false,
            level: 7
          },
          {
            label: "Chaonan",
            value: "潮南区",
            leaf: false,
            level: 7
          },
          {
            label: "Nanao",
            value: "南澳县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Foshan",
        value: "佛山市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Chancheng",
            value: "禅城区",
            leaf: false,
            level: 7
          },
          {
            label: "Nanhai",
            value: "南海区",
            leaf: false,
            level: 7
          },
          {
            label: "Shunde",
            value: "顺德区",
            leaf: false,
            level: 7
          },
          {
            label: "Sanshui",
            value: "三水区",
            leaf: false,
            level: 7
          },
          {
            label: "Gaoming",
            value: "高明区",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Shaoguan",
        value: "韶关市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Zhenjiang",
            value: "浈江区",
            leaf: false,
            level: 7
          },
          {
            label: "Wujiang",
            value: "武江区",
            leaf: false,
            level: 7
          },
          {
            label: "Qujiang",
            value: "曲江区",
            leaf: false,
            level: 7
          },
          {
            label: "Lechang",
            value: "乐昌市",
            leaf: false,
            level: 7
          },
          {
            label: "Nanxiong",
            value: "南雄市",
            leaf: false,
            level: 7
          },
          {
            label: "Shixing",
            value: "始兴县",
            leaf: false,
            level: 7
          },
          {
            label: "Renhua",
            value: "仁化县",
            leaf: false,
            level: 7
          },
          {
            label: "Wengyuan",
            value: "翁源县",
            leaf: false,
            level: 7
          },
          {
            label: "Ruyuanyaozu",
            value: "乳源瑶族自治县",
            leaf: false,
            level: 7
          },
          {
            label: "Xinfeng",
            value: "新丰县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Zhanjiang",
        value: "湛江市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Chikan",
            value: "赤坎区",
            leaf: false,
            level: 7
          },
          {
            label: "Xiashan",
            value: "霞山区",
            leaf: false,
            level: 7
          },
          {
            label: "Potou",
            value: "坡头区",
            leaf: false,
            level: 7
          },
          {
            label: "Mazhang",
            value: "麻章区",
            leaf: false,
            level: 7
          },
          {
            label: "Lianjiang",
            value: "廉江市",
            leaf: false,
            level: 7
          },
          {
            label: "Leizhou",
            value: "雷州市",
            leaf: false,
            level: 7
          },
          {
            label: "Wuchuan",
            value: "吴川市",
            leaf: false,
            level: 7
          },
          {
            label: "Suixi",
            value: "遂溪县",
            leaf: false,
            level: 7
          },
          {
            label: "Xuwen",
            value: "徐闻县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Zhaoqing",
        value: "肇庆市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Duanzhou",
            value: "端州区",
            leaf: false,
            level: 7
          },
          {
            label: "Dinghu",
            value: "鼎湖区",
            leaf: false,
            level: 7
          },
          {
            label: "Gaoyao",
            value: "高要区",
            leaf: false,
            level: 7
          },
          {
            label: "Sihui",
            value: "四会市",
            leaf: false,
            level: 7
          },
          {
            label: "Guangning",
            value: "广宁县",
            leaf: false,
            level: 7
          },
          {
            label: "Huaiji",
            value: "怀集县",
            leaf: false,
            level: 7
          },
          {
            label: "Fengkai",
            value: "封开县",
            leaf: false,
            level: 7
          },
          {
            label: "Deqing",
            value: "德庆县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Jiangmen",
        value: "江门市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Pengjiang",
            value: "蓬江区",
            leaf: false,
            level: 7
          },
          {
            label: "Jianghai",
            value: "江海区",
            leaf: false,
            level: 7
          },
          {
            label: "Xinhui",
            value: "新会区",
            leaf: false,
            level: 7
          },
          {
            label: "Taishan",
            value: "台山市",
            leaf: false,
            level: 7
          },
          {
            label: "Kaiping",
            value: "开平市",
            leaf: false,
            level: 7
          },
          {
            label: "Heshan",
            value: "鹤山市",
            leaf: false,
            level: 7
          },
          {
            label: "Enping",
            value: "恩平市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Maoming",
        value: "茂名市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Maonan",
            value: "茂南区",
            leaf: false,
            level: 7
          },
          {
            label: "Dianbai",
            value: "电白区",
            leaf: false,
            level: 7
          },
          {
            label: "Gaozhou",
            value: "高州市",
            leaf: false,
            level: 7
          },
          {
            label: "Huazhou",
            value: "化州市",
            leaf: false,
            level: 7
          },
          {
            label: "Xinyi",
            value: "信宜市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Huizhou",
        value: "惠州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Huicheng",
            value: "惠城区",
            leaf: false,
            level: 7
          },
          {
            label: "Huiyang",
            value: "惠阳区",
            leaf: false,
            level: 7
          },
          {
            label: "Huidong",
            value: "惠东县",
            leaf: false,
            level: 7
          },
          {
            label: "Boluo",
            value: "博罗县",
            leaf: false,
            level: 7
          },
          {
            label: "Longmen",
            value: "龙门县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Meizhou",
        value: "梅州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Meijiang",
            value: "梅江区",
            leaf: false,
            level: 7
          },
          {
            label: "Meixian",
            value: "梅县区",
            leaf: false,
            level: 7
          },
          {
            label: "Xingning",
            value: "兴宁市",
            leaf: false,
            level: 7
          },
          {
            label: "Dabu",
            value: "大埔县",
            leaf: false,
            level: 7
          },
          {
            label: "Fengshun",
            value: "丰顺县",
            leaf: false,
            level: 7
          },
          {
            label: "Wuhua",
            value: "五华县",
            leaf: false,
            level: 7
          },
          {
            label: "Pingyuan",
            value: "平远县",
            leaf: false,
            level: 7
          },
          {
            label: "Jiaoling",
            value: "蕉岭县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Shanwei",
        value: "汕尾市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Haifeng",
            value: "海丰县",
            leaf: false,
            level: 7
          },
          {
            label: "Luhe",
            value: "陆河县",
            leaf: false,
            level: 7
          },
          {
            label: "Lufeng",
            value: "陆丰市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Heyuan",
        value: "河源市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Yuancheng",
            value: "源城区",
            leaf: false,
            level: 7
          },
          {
            label: "Zijin",
            value: "紫金县",
            leaf: false,
            level: 7
          },
          {
            label: "Longchuan",
            value: "龙川县",
            leaf: false,
            level: 7
          },
          {
            label: "Lianping",
            value: "连平县",
            leaf: false,
            level: 7
          },
          {
            label: "Heping",
            value: "和平县",
            leaf: false,
            level: 7
          },
          {
            label: "Dongyuan",
            value: "东源县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Yangjiang",
        value: "阳江市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Jiangcheng",
            value: "江城区",
            leaf: false,
            level: 7
          },
          {
            label: "Yangdong",
            value: "阳东区",
            leaf: false,
            level: 7
          },
          {
            label: "Yangxi",
            value: "阳西县",
            leaf: false,
            level: 7
          },
          {
            label: "Yangchun",
            value: "阳春市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Qingyuan",
        value: "清远市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Qingcheng",
            value: "清城区",
            leaf: false,
            level: 7
          },
          {
            label: "Qingxin",
            value: "清新区",
            leaf: false,
            level: 7
          },
          {
            label: "Yingde",
            value: "英德市",
            leaf: false,
            level: 7
          },
          {
            label: "Lianzhou",
            value: "连州市",
            leaf: false,
            level: 7
          },
          {
            label: "Fogang",
            value: "佛冈县",
            leaf: false,
            level: 7
          },
          {
            label: "Yangshan",
            value: "阳山县",
            leaf: false,
            level: 7
          },
          {
            label: "Liannanyaozu",
            value: "连南瑶族自治县",
            leaf: false,
            level: 7
          },
          {
            label: "Lianshanzhuangzuandyaozu",
            value: "连山壮族瑶族自治县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Dongguan",
        value: "东莞市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Zhongshan",
        value: "中山市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Chaozhou",
        value: "潮州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Xiangqiao",
            value: "湘桥区",
            leaf: false,
            level: 7
          },
          {
            label: "Chaoan",
            value: "潮安区",
            leaf: false,
            level: 7
          },
          {
            label: "Raoping",
            value: "饶平县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Jieyang",
        value: "揭阳市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Rongcheng",
            value: "榕城区",
            leaf: false,
            level: 7
          },
          {
            label: "Jiedong",
            value: "揭东区",
            leaf: false,
            level: 7
          },
          {
            label: "Jiexi",
            value: "揭西县",
            leaf: false,
            level: 7
          },
          {
            label: "Huilai",
            value: "惠来县",
            leaf: false,
            level: 7
          },
          {
            label: "Puning",
            value: "普宁市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Yunfu",
        value: "云浮市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Yuncheng",
            value: "云城区",
            leaf: false,
            level: 7
          },
          {
            label: "Yunan",
            value: "云安区",
            leaf: false,
            level: 7
          },
          {
            label: "Luoding",
            value: "罗定市",
            leaf: false,
            level: 7
          },
          {
            label: "Xinxing",
            value: "新兴县",
            leaf: false,
            level: 7
          },
          {
            label: "Yunan",
            value: "郁南县",
            leaf: false,
            level: 7
          }
        ]
      }
    ]
  },
  {
    label: "Guangxi",
    value: "广西壮族自治区",
    leaf: false,
    level: 5,
    children: [
      {
        label: "Prefecture",
        value: "地级",
        leaf: false,
        level: 6
      },
      {
        label: "Nanning",
        value: "南宁市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Xingning",
            value: "兴宁区",
            leaf: false,
            level: 7
          },
          {
            label: "Qingxiu",
            value: "青秀区",
            leaf: false,
            level: 7
          },
          {
            label: "Jiangnan",
            value: "江南区",
            leaf: false,
            level: 7
          },
          {
            label: "Xixiangtang",
            value: "西乡塘区",
            leaf: false,
            level: 7
          },
          {
            label: "Liangqing",
            value: "良庆区",
            leaf: false,
            level: 7
          },
          {
            label: "Yongning",
            value: "邕宁区",
            leaf: false,
            level: 7
          },
          {
            label: "Wuming",
            value: "武鸣区",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Liuzhou",
        value: "柳州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Chengzhong",
            value: "城中区",
            leaf: false,
            level: 7
          },
          {
            label: "Yufeng",
            value: "鱼峰区",
            leaf: false,
            level: 7
          },
          {
            label: "Liunan",
            value: "柳南区",
            leaf: false,
            level: 7
          },
          {
            label: "Liubei",
            value: "柳北区",
            leaf: false,
            level: 7
          },
          {
            label: "Liujiang",
            value: "柳江区",
            leaf: false,
            level: 7
          },
          {
            label: "Liucheng",
            value: "柳城县",
            leaf: false,
            level: 7
          },
          {
            label: "Luzhai",
            value: "鹿寨县",
            leaf: false,
            level: 7
          },
          {
            label: "Rongan",
            value: "融安县",
            leaf: false,
            level: 7
          },
          {
            label: "Rongshuimiaozu",
            value: "融水苗族自治县",
            leaf: false,
            level: 7
          },
          {
            label: "Sanjiangdongzu",
            value: "三江侗族自治县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Guilin",
        value: "桂林市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Diecai",
            value: "叠彩区",
            leaf: false,
            level: 7
          },
          {
            label: "Xiangshan",
            value: "象山区",
            leaf: false,
            level: 7
          },
          {
            label: "Qixing",
            value: "七星区",
            leaf: false,
            level: 7
          },
          {
            label: "Yanshan",
            value: "雁山区",
            leaf: false,
            level: 7
          },
          {
            label: "Lingui",
            value: "临桂区",
            leaf: false,
            level: 7
          },
          {
            label: "Yangshuo",
            value: "阳朔县",
            leaf: false,
            level: 7
          },
          {
            label: "Lingchuan",
            value: "灵川县",
            leaf: false,
            level: 7
          },
          {
            label: "Quanzhou",
            value: "全州县",
            leaf: false,
            level: 7
          },
          {
            label: "Xingan",
            value: "兴安县",
            leaf: false,
            level: 7
          },
          {
            label: "Yongfu",
            value: "永福县",
            leaf: false,
            level: 7
          },
          {
            label: "Guanyang",
            value: "灌阳县",
            leaf: false,
            level: 7
          },
          {
            label: "Longshenggezu",
            value: "龙胜各族自治县",
            leaf: false,
            level: 7
          },
          {
            label: "Ziyuan",
            value: "资源县",
            leaf: false,
            level: 7
          },
          {
            label: "Pingle",
            value: "平乐县",
            leaf: false,
            level: 7
          },
          {
            label: "Lipu",
            value: "荔浦市",
            leaf: false,
            level: 7
          },
          {
            label: "Gongchengyaozu",
            value: "恭城瑶族自治县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Wuzhou",
        value: "梧州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Wanxiu",
            value: "万秀区",
            leaf: false,
            level: 7
          },
          {
            label: "Changzhou",
            value: "长洲区",
            leaf: false,
            level: 7
          },
          {
            label: "Longxu",
            value: "龙圩区",
            leaf: false,
            level: 7
          },
          {
            label: "Cangwu",
            value: "苍梧县",
            leaf: false,
            level: 7
          },
          {
            label: "Tengxian",
            value: "藤县",
            leaf: false,
            level: 7
          },
          {
            label: "Mengshan",
            value: "蒙山县",
            leaf: false,
            level: 7
          },
          {
            label: "Cenxi",
            value: "岑溪市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Beihai",
        value: "北海市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Haicheng",
            value: "海城区",
            leaf: false,
            level: 7
          },
          {
            label: "Yinhai",
            value: "银海区",
            leaf: false,
            level: 7
          },
          {
            label: "Tieshangang",
            value: "铁山港区",
            leaf: false,
            level: 7
          },
          {
            label: "Hepu",
            value: "合浦县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Fangchenggang",
        value: "防城港市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Gangkou",
            value: "港口区",
            leaf: false,
            level: 7
          },
          {
            label: "Fangcheng",
            value: "防城区",
            leaf: false,
            level: 7
          },
          {
            label: "Shangsi",
            value: "上思县",
            leaf: false,
            level: 7
          },
          {
            label: "Dongxing",
            value: "东兴市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Qinzhou",
        value: "钦州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Qinnan",
            value: "钦南区",
            leaf: false,
            level: 7
          },
          {
            label: "Qinbei",
            value: "钦北区",
            leaf: false,
            level: 7
          },
          {
            label: "Lingshan",
            value: "灵山县",
            leaf: false,
            level: 7
          },
          {
            label: "Pubei",
            value: "浦北县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Guigang",
        value: "贵港市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Gangbei",
            value: "港北区",
            leaf: false,
            level: 7
          },
          {
            label: "Gangnan",
            value: "港南区",
            leaf: false,
            level: 7
          },
          {
            label: "Qintang",
            value: "覃塘区",
            leaf: false,
            level: 7
          },
          {
            label: "Pingnan",
            value: "平南县",
            leaf: false,
            level: 7
          },
          {
            label: "Guiping",
            value: "桂平市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Yulin",
        value: "玉林市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Yuzhou",
            value: "玉州区",
            leaf: false,
            level: 7
          },
          {
            label: "Fumian",
            value: "福绵区",
            leaf: false,
            level: 7
          },
          {
            label: "Rongxian",
            value: "容县",
            leaf: false,
            level: 7
          },
          {
            label: "Luchuan",
            value: "陆川县",
            leaf: false,
            level: 7
          },
          {
            label: "Bobai",
            value: "博白县",
            leaf: false,
            level: 7
          },
          {
            label: "Xingye",
            value: "兴业县",
            leaf: false,
            level: 7
          },
          {
            label: "Beiliu",
            value: "北流市",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Baise",
        value: "百色市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Youjiang",
            value: "右江区",
            leaf: false,
            level: 7
          },
          {
            label: "Tianyang",
            value: "田阳区",
            leaf: false,
            level: 7
          },
          {
            label: "Tiandong",
            value: "田东县",
            leaf: false,
            level: 7
          },
          {
            label: "Pingguo",
            value: "平果市",
            leaf: false,
            level: 7
          },
          {
            label: "Debao",
            value: "德保县",
            leaf: false,
            level: 7
          },
          {
            label: "Jingxi",
            value: "靖西市",
            leaf: false,
            level: 7
          },
          {
            label: "Napo",
            value: "那坡县",
            leaf: false,
            level: 7
          },
          {
            label: "Lingyun",
            value: "凌云县",
            leaf: false,
            level: 7
          },
          {
            label: "Leye",
            value: "乐业县",
            leaf: false,
            level: 7
          },
          {
            label: "Tianlin",
            value: "田林县",
            leaf: false,
            level: 7
          },
          {
            label: "Xilin",
            value: "西林县",
            leaf: false,
            level: 7
          },
          {
            label: "Longlingezu",
            value: "隆林各族自治县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Hezhou",
        value: "贺州市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Babu",
            value: "八步区",
            leaf: false,
            level: 7
          },
          {
            label: "Pinggui",
            value: "平桂区",
            leaf: false,
            level: 7
          },
          {
            label: "Zhaoping",
            value: "昭平县",
            leaf: false,
            level: 7
          },
          {
            label: "Zhongshan",
            value: "钟山县",
            leaf: false,
            level: 7
          },
          {
            label: "Fuchuanyaozu",
            value: "富川瑶族自治县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Hechi",
        value: "河池市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Jinchengjiang",
            value: "金城江区",
            leaf: false,
            level: 7
          },
          {
            label: "Yizhou",
            value: "宜州区",
            leaf: false,
            level: 7
          },
          {
            label: "Nandan",
            value: "南丹县",
            leaf: false,
            level: 7
          },
          {
            label: "Tian'e",
            value: "天峨县",
            leaf: false,
            level: 7
          },
          {
            label: "Fengshan",
            value: "凤山县",
            leaf: false,
            level: 7
          },
          {
            label: "Donglan",
            value: "东兰县",
            leaf: false,
            level: 7
          },
          {
            label: "Luochengmulaozu",
            value: "罗城仫佬族自治县",
            leaf: false,
            level: 7
          },
          {
            label: "Huanjiangmaonanzu",
            value: "环江毛南族自治县",
            leaf: false,
            level: 7
          },
          {
            label: "Bamayaozu",
            value: "巴马瑶族自治县",
            leaf: false,
            level: 7
          },
          {
            label: "Duanyaozu",
            value: "都安瑶族自治县",
            leaf: false,
            level: 7
          },
          {
            label: "Dahuayaozu",
            value: "大化瑶族自治县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Laibin",
        value: "来宾市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Xingbin",
            value: "兴宾区",
            leaf: false,
            level: 7
          },
          {
            label: "Xincheng",
            value: "忻城县",
            leaf: false,
            level: 7
          },
          {
            label: "Xiangzhou",
            value: "象州县",
            leaf: false,
            level: 7
          },
          {
            label: "Wuxuan",
            value: "武宣县",
            leaf: false,
            level: 7
          },
          {
            label: "Jinxiuyaozu",
            value: "金秀瑶族自治县",
            leaf: false,
            level: 7
          }
        ]
      },
      {
        label: "Chongzuo",
        value: "崇左市",
        leaf: false,
        level: 6,
        children: [
          {
            label: "County",
            value: "县级",
            leaf: false,
            level: 7
          },
          {
            label: "Jiangzhou",
            value: "江州区",
            leaf: false,
            level: 7
          },
          {
            label: "Fusui",
            value: "扶绥县",
            leaf: false,
            level: 7
          },
          {
            label: "Ningming",
            value: "宁明县",
            leaf: false,
            level: 7
          },
          {
            label: "Longzhou",
            value: "龙州县",
            leaf: false,
            level: 7
          },
          {
            label: "Daxin",
            value: "大新县",
            leaf: false,
            level: 7
          },
          {
            label: "Tiandeng",
            value: "天等县",
            leaf: false,
            level: 7
          },
          {
            label: "Pingxiang",
            value: "凭祥市",
            leaf: false,
            level: 7
          }
        ]
      }
    ]
  },
{
label: "Yunnan",
value: "云南省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Kunming",
value: "昆明市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Wuhua",
value: "五华区",
leaf: false,
level: 7
},
{
label: "Panlong",
value: "盘龙区",
leaf: false,
level: 7
},
{
label: "Guandu",
value: "官渡区",
leaf: false,
level: 7
},
{
label: "Xishan",
value: "西山区",
leaf: false,
level: 7
},
{
label: "Dongchuan",
value: "东川区",
leaf: false,
level: 7
},
{
label: "Chenggong",
value: "呈贡区",
leaf: false,
level: 7
},
{
label: "Jinning",
value: "晋宁区",
leaf: false,
level: 7
},
{
label: "Fumin",
value: "富民县",
leaf: false,
level: 7
},
{
label: "Yiliang",
value: "宜良县",
leaf: false,
level: 7
},
{
label: "Shilinyizu",
value: "石林彝族自治县",
leaf: false,
level: 7
},
{
label: "Songming",
value: "嵩明县",
leaf: false,
level: 7
},
{
label: "Luquanyizuandmiaozu",
value: "禄劝彝族苗族自治县",
leaf: false,
level: 7
},
{
label: "Xundianhuizuandyizu",
value: "寻甸回族彝族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Qujing",
value: "曲靖市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Qilin",
value: "麒麟区",
leaf: false,
level: 7
},
{
label: "Zhanyi",
value: "沾益区",
leaf: false,
level: 7
},
{
label: "Malong",
value: "马龙区",
leaf: false,
level: 7
},
{
label: "Xuanwei",
value: "宣威市",
leaf: false,
level: 7
},
{
label: "Luliang",
value: "陆良县",
leaf: false,
level: 7
},
{
label: "Shizong",
value: "师宗县",
leaf: false,
level: 7
},
{
label: "Luoping",
value: "罗平县",
leaf: false,
level: 7
},
{
label: "Fuyuan",
value: "富源县",
leaf: false,
level: 7
},
{
label: "Huize",
value: "会泽县",
leaf: false,
level: 7
}
]
},
{
label: "Yuxi",
value: "玉溪市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Hongta",
value: "红塔区",
leaf: false,
level: 7
},
{
label: "Jiangchuan",
value: "江川区",
leaf: false,
level: 7
},
{
label: "Chengjiang",
value: "澄江市",
leaf: false,
level: 7
},
{
label: "Tonghai",
value: "通海县",
leaf: false,
level: 7
},
{
label: "Huaning",
value: "华宁县",
leaf: false,
level: 7
},
{
label: "Yimen",
value: "易门县",
leaf: false,
level: 7
},
{
label: "Eshanyizu",
value: "峨山彝族自治县",
leaf: false,
level: 7
},
{
label: "Xinpingyizuanddaizu",
value: "新平彝族傣族自治县",
leaf: false,
level: 7
},
{
label: "Yuanjianghanizuandyizudaizu",
value: "元江哈尼族彝族傣族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Baoshan",
value: "保山市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Longyang",
value: "隆阳区",
leaf: false,
level: 7
},
{
label: "Tengchong",
value: "腾冲市",
leaf: false,
level: 7
},
{
label: "Shidian",
value: "施甸县",
leaf: false,
level: 7
},
{
label: "Longling",
value: "龙陵县",
leaf: false,
level: 7
},
{
label: "Changning",
value: "昌宁县",
leaf: false,
level: 7
}
]
},
{
label: "Zhaotong",
value: "昭通市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Zhaoyang",
value: "昭阳区",
leaf: false,
level: 7
},
{
label: "Ludian",
value: "鲁甸县",
leaf: false,
level: 7
},
{
label: "Qiaojia",
value: "巧家县",
leaf: false,
level: 7
},
{
label: "Yanjin",
value: "盐津县",
leaf: false,
level: 7
},
{
label: "Daguan",
value: "大关县",
leaf: false,
level: 7
},
{
label: "Yongshan",
value: "永善县",
leaf: false,
level: 7
},
{
label: "Suijiang",
value: "绥江县",
leaf: false,
level: 7
},
{
label: "Zhenxiong",
value: "镇雄县",
leaf: false,
level: 7
},
{
label: "Yiliang",
value: "彝良县",
leaf: false,
level: 7
},
{
label: "Weixin",
value: "威信县",
leaf: false,
level: 7
},
{
label: "Shuifu",
value: "水富市",
leaf: false,
level: 7
}
]
},
{
label: "Lijiang",
value: "丽江市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Gucheng",
value: "古城区",
leaf: false,
level: 7
},
{
label: "Yulongnaxizu",
value: "玉龙纳西族自治县",
leaf: false,
level: 7
},
{
label: "Yongsheng",
value: "永胜县",
leaf: false,
level: 7
},
{
label: "Huaping",
value: "华坪县",
leaf: false,
level: 7
},
{
label: "Ninglangyizu",
value: "宁蒗彝族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Puer",
value: "普洱市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Simao",
value: "思茅区",
leaf: false,
level: 7
},
{
label: "Ningerhanizuandyizu",
value: "宁洱哈尼族彝族自治县",
leaf: false,
level: 7
},
{
label: "Mojianghanizu",
value: "墨江哈尼族自治县",
leaf: false,
level: 7
},
{
label: "Jingdongyizu",
value: "景东彝族自治县",
leaf: false,
level: 7
},
{
label: "Jinggudaizuandyizu",
value: "景谷傣族彝族自治县",
leaf: false,
level: 7
},
{
label: "Zhenyuanyizuandhanizulahuzu",
value: "镇沅彝族哈尼族拉祜族自治县",
leaf: false,
level: 7
},
{
label: "Jiangchenghanizuandyizu",
value: "江城哈尼族彝族自治县",
leaf: false,
level: 7
},
{
label: "Mengliandaizuandlahuzuwazu",
value: "孟连傣族拉祜族佤族自治县",
leaf: false,
level: 7
},
{
label: "Lancanglahuzu",
value: "澜沧拉祜族自治县",
leaf: false,
level: 7
},
{
label: "Ximengwazu",
value: "西盟佤族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Lincang",
value: "临沧市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Linxiang",
value: "临翔区",
leaf: false,
level: 7
},
{
label: "Fengqing",
value: "凤庆县",
leaf: false,
level: 7
},
{
label: "Yunxian",
value: "云县",
leaf: false,
level: 7
},
{
label: "Yongde",
value: "永德县",
leaf: false,
level: 7
},
{
label: "Zhenkang",
value: "镇康县",
leaf: false,
level: 7
},
{
label: "Shuangjianglahuzuwazubulangzudaizu",
value: "双江拉祜族佤族布朗族傣族自治县",
leaf: false,
level: 7
},
{
label: "Gengmadaizuwazu",
value: "耿马傣族佤族自治县",
leaf: false,
level: 7
},
{
label: "Cangyuanwazu",
value: "沧源佤族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Wenshanzhuangzumiaozu",
value: "文山壮族苗族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Wenshan",
value: "文山市",
leaf: false,
level: 7
},
{
label: "Yanshan",
value: "砚山县",
leaf: false,
level: 7
},
{
label: "Xichou",
value: "西畴县",
leaf: false,
level: 7
},
{
label: "Malipo",
value: "麻栗坡县",
leaf: false,
level: 7
},
{
label: "Maguan",
value: "马关县",
leaf: false,
level: 7
},
{
label: "Qiubei",
value: "丘北县",
leaf: false,
level: 7
},
{
label: "Guangnan",
value: "广南县",
leaf: false,
level: 7
},
{
label: "Funing",
value: "富宁县",
leaf: false,
level: 7
}
]
},
{
label: "Honghehanizuyizu",
value: "红河哈尼族彝族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Mengzi",
value: "蒙自市",
leaf: false,
level: 7
},
{
label: "Gejiu",
value: "个旧市",
leaf: false,
level: 7
},
{
label: "Kaiyuan",
value: "开远市",
leaf: false,
level: 7
},
{
label: "Mile",
value: "弥勒市",
leaf: false,
level: 7
},
{
label: "Jianshui",
value: "建水县",
leaf: false,
level: 7
},
{
label: "Shiping",
value: "石屏县",
leaf: false,
level: 7
},
{
label: "Luxi",
value: "泸西县",
leaf: false,
level: 7
},
{
label: "Yuanyang",
value: "元阳县",
leaf: false,
level: 7
},
{
label: "Honghe",
value: "红河县",
leaf: false,
level: 7
},
{
label: "Jinpingmiaozuandyaozudaizu",
value: "金平苗族瑶族傣族自治县",
leaf: false,
level: 7
},
{
label: "Lüchun",
value: "绿春县",
leaf: false,
level: 7
},
{
label: "Pingbianmiaozu",
value: "屏边苗族自治县",
leaf: false,
level: 7
},
{
label: "Hekouyaozu",
value: "河口瑶族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Xishuangbannadaizu",
value: "西双版纳傣族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Jinghong",
value: "景洪市",
leaf: false,
level: 7
},
{
label: "Menghai",
value: "勐海县",
leaf: false,
level: 7
},
{
label: "Mengla",
value: "勐腊县",
leaf: false,
level: 7
}
]
},
{
label: "Chuxiongyizu",
value: "楚雄彝族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Chuxiong",
value: "楚雄市",
leaf: false,
level: 7
},
{
label: "Shuangbai",
value: "双柏县",
leaf: false,
level: 7
},
{
label: "Mouding",
value: "牟定县",
leaf: false,
level: 7
},
{
label: "Nanhua",
value: "南华县",
leaf: false,
level: 7
},
{
label: "Yaoan",
value: "姚安县",
leaf: false,
level: 7
},
{
label: "Dayao",
value: "大姚县",
leaf: false,
level: 7
},
{
label: "Yongren",
value: "永仁县",
leaf: false,
level: 7
},
{
label: "Yuanmou",
value: "元谋县",
leaf: false,
level: 7
},
{
label: "Wuding",
value: "武定县",
leaf: false,
level: 7
},
{
label: "Lufeng",
value: "禄丰市",
leaf: false,
level: 7
}
]
},
{
label: "Dalibaizu",
value: "大理白族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Dali",
value: "大理市",
leaf: false,
level: 7
},
{
label: "Yangbiyizu",
value: "漾濞彝族自治县",
leaf: false,
level: 7
},
{
label: "Xiangyun",
value: "祥云县",
leaf: false,
level: 7
},
{
label: "Binchuan",
value: "宾川县",
leaf: false,
level: 7
},
{
label: "Midu",
value: "弥渡县",
leaf: false,
level: 7
},
{
label: "Nanjianyizu",
value: "南涧彝族自治县",
leaf: false,
level: 7
},
{
label: "Weishanyizuandhuizu",
value: "巍山彝族回族自治县",
leaf: false,
level: 7
},
{
label: "Yongping",
value: "永平县",
leaf: false,
level: 7
},
{
label: "Yunlong",
value: "云龙县",
leaf: false,
level: 7
},
{
label: "Eryuan",
value: "洱源县",
leaf: false,
level: 7
},
{
label: "Jianchuan",
value: "剑川县",
leaf: false,
level: 7
},
{
label: "Heqing",
value: "鹤庆县",
leaf: false,
level: 7
}
]
},
{
label: "Dehongdaizujingpozu",
value: "德宏傣族景颇族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Mangshi",
value: "芒市",
leaf: false,
level: 7
},
{
label: "Ruili",
value: "瑞丽市",
leaf: false,
level: 7
},
{
label: "Lianghe",
value: "梁河县",
leaf: false,
level: 7
},
{
label: "Yingjiang",
value: "盈江县",
leaf: false,
level: 7
},
{
label: "Longchuan",
value: "陇川县",
leaf: false,
level: 7
}
]
},
{
label: "Nujianglisuzu",
value: "怒江傈僳族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Lushui",
value: "泸水市",
leaf: false,
level: 7
},
{
label: "Fugong",
value: "福贡县",
leaf: false,
level: 7
},
{
label: "Gongshandulongzunuzu",
value: "贡山独龙族怒族自治县",
leaf: false,
level: 7
},
{
label: "Lanpingbaizuandpumizu",
value: "兰坪白族普米族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Diqingzangzu",
value: "迪庆藏族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xianggelila",
value: "香格里拉市",
leaf: false,
level: 7
},
{
label: "Deqin",
value: "德钦县",
leaf: false,
level: 7
},
{
label: "Weixilisuzu",
value: "维西傈僳族自治县",
leaf: false,
level: 7
}
]
}
]
},
{
label: "Guizhou",
value: "贵州省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Guiyang",
value: "贵阳市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Nanming",
value: "南明区",
leaf: false,
level: 7
},
{
label: "Yunyan",
value: "云岩区",
leaf: false,
level: 7
},
{
label: "Huaxi",
value: "花溪区",
leaf: false,
level: 7
},
{
label: "Wudang",
value: "乌当区",
leaf: false,
level: 7
},
{
label: "Baiyun",
value: "白云区",
leaf: false,
level: 7
},
{
label: "Guanshanhu",
value: "观山湖区",
leaf: false,
level: 7
},
{
label: "Kaiyang",
value: "开阳县",
leaf: false,
level: 7
},
{
label: "Xifeng",
value: "息烽县",
leaf: false,
level: 7
},
{
label: "Xiuwen",
value: "修文县",
leaf: false,
level: 7
}
]
},
{
label: "Liupanshui",
value: "六盘水市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Zhongshan",
value: "钟山区",
leaf: false,
level: 7
},
{
label: "Shuicheng",
value: "水城区",
leaf: false,
level: 7
},
{
label: "Liuzhite",
value: "六枝特区",
leaf: false,
level: 7
},
{
label: "Panzhou",
value: "盘州市",
leaf: false,
level: 7
}
]
},
{
label: "Zunyi",
value: "遵义市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Honghuagang",
value: "红花岗区",
leaf: false,
level: 7
},
{
label: "Huichuan",
value: "汇川区",
leaf: false,
level: 7
},
{
label: "Bozhou",
value: "播州区",
leaf: false,
level: 7
},
{
label: "Chishui",
value: "赤水市",
leaf: false,
level: 7
},
{
label: "Renhuai",
value: "仁怀市",
leaf: false,
level: 7
},
{
label: "Tongzi",
value: "桐梓县",
leaf: false,
level: 7
},
{
label: "Suiyang",
value: "绥阳县",
leaf: false,
level: 7
},
{
label: "Zhengan",
value: "正安县",
leaf: false,
level: 7
},
{
label: "Daozhengelaozumiaozu",
value: "道真仡佬族苗族自治县",
leaf: false,
level: 7
},
{
label: "Wuchuangelaozumiaozu",
value: "务川仡佬族苗族自治县",
leaf: false,
level: 7
},
{
label: "Fenggang",
value: "凤冈县",
leaf: false,
level: 7
},
{
label: "Meitan",
value: "湄潭县",
leaf: false,
level: 7
},
{
label: "Yuqing",
value: "余庆县",
leaf: false,
level: 7
},
{
label: "Xishui",
value: "习水县",
leaf: false,
level: 7
}
]
},
{
label: "Anshun",
value: "安顺市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xixiu",
value: "西秀区",
leaf: false,
level: 7
},
{
label: "Pingba",
value: "平坝区",
leaf: false,
level: 7
},
{
label: "Puding",
value: "普定县",
leaf: false,
level: 7
},
{
label: "Zhenningbuyizuandmiaozu",
value: "镇宁布依族苗族自治县",
leaf: false,
level: 7
},
{
label: "Guanlingbuyizumiaozu",
value: "关岭布依族苗族自治县",
leaf: false,
level: 7
},
{
label: "Ziyunmiaozuandbuyizu",
value: "紫云苗族布依族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Bijie",
value: "毕节市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Qixingguan",
value: "七星关区",
leaf: false,
level: 7
},
{
label: "Dafang",
value: "大方县",
leaf: false,
level: 7
},
{
label: "Qianxi",
value: "黔西市",
leaf: false,
level: 7
},
{
label: "Jinsha",
value: "金沙县",
leaf: false,
level: 7
},
{
label: "Zhijin",
value: "织金县",
leaf: false,
level: 7
},
{
label: "Nayong",
value: "纳雍县",
leaf: false,
level: 7
},
{
label: "Weiningyizuhuizumiaozu",
value: "威宁彝族回族苗族自治县",
leaf: false,
level: 7
},
{
label: "Hezhang",
value: "赫章县",
leaf: false,
level: 7
}
]
},
{
label: "Tongren",
value: "铜仁市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Bijiang",
value: "碧江区",
leaf: false,
level: 7
},
{
label: "Wanshan",
value: "万山区",
leaf: false,
level: 7
},
{
label: "Jiangkou",
value: "江口县",
leaf: false,
level: 7
},
{
label: "Yupingdongzu",
value: "玉屏侗族自治县",
leaf: false,
level: 7
},
{
label: "Shiqian",
value: "石阡县",
leaf: false,
level: 7
},
{
label: "Sinan",
value: "思南县",
leaf: false,
level: 7
},
{
label: "Yinjiangtujiazuandmiaozu",
value: "印江土家族苗族自治县",
leaf: false,
level: 7
},
{
label: "Dejiang",
value: "德江县",
leaf: false,
level: 7
},
{
label: "Yanhetujiazu",
value: "沿河土家族自治县",
leaf: false,
level: 7
},
{
label: "Songtaomiaozu",
value: "松桃苗族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Qiandongnanmiaozudongzu",
value: "黔东南苗族侗族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Kaili",
value: "凯里市",
leaf: false,
level: 7
},
{
label: "Huangping",
value: "黄平县",
leaf: false,
level: 7
},
{
label: "Shibing",
value: "施秉县",
leaf: false,
level: 7
},
{
label: "Sansui",
value: "三穗县",
leaf: false,
level: 7
},
{
label: "Zhenyuan",
value: "镇远县",
leaf: false,
level: 7
},
{
label: "Cengong",
value: "岑巩县",
leaf: false,
level: 7
},
{
label: "Tianzhu",
value: "天柱县",
leaf: false,
level: 7
},
{
label: "Jinping",
value: "锦屏县",
leaf: false,
level: 7
},
{
label: "Jianhe",
value: "剑河县",
leaf: false,
level: 7
},
{
label: "Taijiang",
value: "台江县",
leaf: false,
level: 7
},
{
label: "Liping",
value: "黎平县",
leaf: false,
level: 7
},
{
label: "Rongjiang",
value: "榕江县",
leaf: false,
level: 7
},
{
label: "Congjiang",
value: "从江县",
leaf: false,
level: 7
},
{
label: "Leishan",
value: "雷山县",
leaf: false,
level: 7
},
{
label: "Majiang",
value: "麻江县",
leaf: false,
level: 7
},
{
label: "Danzhai",
value: "丹寨县",
leaf: false,
level: 7
}
]
},
{
label: "Qiannanbuyizumiaozu",
value: "黔南布依族苗族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Duyun",
value: "都匀市",
leaf: false,
level: 7
},
{
label: "Fuquan",
value: "福泉市",
leaf: false,
level: 7
},
{
label: "Libo",
value: "荔波县",
leaf: false,
level: 7
},
{
label: "Guiding",
value: "贵定县",
leaf: false,
level: 7
},
{
label: "Wengan",
value: "瓮安县",
leaf: false,
level: 7
},
{
label: "Dushan",
value: "独山县",
leaf: false,
level: 7
},
{
label: "Pingtang",
value: "平塘县",
leaf: false,
level: 7
},
{
label: "Luodian",
value: "罗甸县",
leaf: false,
level: 7
},
{
label: "Changshun",
value: "长顺县",
leaf: false,
level: 7
},
{
label: "Longli",
value: "龙里县",
leaf: false,
level: 7
},
{
label: "Huishui",
value: "惠水县",
leaf: false,
level: 7
},
{
label: "Sandushuizu",
value: "三都水族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Qianxinanbuyizumiaozu",
value: "黔西南布依族苗族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xingyi",
value: "兴义市",
leaf: false,
level: 7
},
{
label: "Xingren",
value: "兴仁市",
leaf: false,
level: 7
},
{
label: "Puan",
value: "普安县",
leaf: false,
level: 7
},
{
label: "Qinglong",
value: "晴隆县",
leaf: false,
level: 7
},
{
label: "Zhenfeng",
value: "贞丰县",
leaf: false,
level: 7
},
{
label: "Wangmo",
value: "望谟县",
leaf: false,
level: 7
},
{
label: "Ceheng",
value: "册亨县",
leaf: false,
level: 7
},
{
label: "Anlong",
value: "安龙县",
leaf: false,
level: 7
}
]
}
]
},
{
label: "Hainan",
value: "海南省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6,
},
{
label: "Haikou",
value: "海口市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Xiuying",
value: "秀英区",
leaf: false,
level: 7,
},
{
label: "Longhua",
value: "龙华区",
leaf: false,
level: 7,
},
{
label: "Qiongshan",
value: "琼山区",
leaf: false,
level: 7,
},
{
label: "Meilan",
value: "美兰区",
leaf: false,
level: 7,
},
]
},
{
label: "Sanya",
value: "三亚市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Haitang",
value: "海棠区",
leaf: false,
level: 7,
},
{
label: "Jiyang",
value: "吉阳区",
leaf: false,
level: 7,
},
{
label: "Tianya",
value: "天涯区",
leaf: false,
level: 7,
},
{
label: "Yazhou",
value: "崖州区",
leaf: false,
level: 7,
},
]
},
{
label: "Danzhou",
value: "儋州市",
leaf: false,
level: 6,
children: [
]
},
{
label: "Sansha",
value: "三沙市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Xisha",
value: "西沙区",
leaf: false,
level: 7,
},
{
label: "Nansha",
value: "南沙区",
leaf: false,
level: 7,
},
]
}
]
},


                ],
              },
              {
                label: "Northwest",
                value: "西北",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "Province",
                    value: "省级",
                    leaf: true,
                    level: 5,
 
                  },
{
label: "Shaanxi",
value: "陕西省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6,
},
{
label: "Xian",
value: "西安市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Weiyang",
value: "未央区",
leaf: false,
level: 7,
},
{
label: "Beilin",
value: "碑林区",
leaf: false,
level: 7,
},
{
label: "Lianhu",
value: "莲湖区",
leaf: false,
level: 7,
},
{
label: "Baqiao",
value: "灞桥区",
leaf: false,
level: 7,
},
{
label: "Yanta",
value: "雁塔区",
leaf: false,
level: 7,
},
{
label: "Yanliang",
value: "阎良区",
leaf: false,
level: 7,
},
{
label: "Lintong",
value: "临潼区",
leaf: false,
level: 7,
},
{
label: "Changan",
value: "长安区",
leaf: false,
level: 7,
},
{
label: "Gaoling",
value: "高陵区",
leaf: false,
level: 7,
},
{
label: "Huyi",
value: "鄠邑区",
leaf: false,
level: 7,
}
]
},
{
label: "Baoji",
value: "宝鸡市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Weibin",
value: "渭滨区",
leaf: false,
level: 7,
},
{
label: "Jintai",
value: "金台区",
leaf: false,
level: 7,
},
{
label: "Chencang",
value: "陈仓区",
leaf: false,
level: 7,
},
{
label: "Fengxiang",
value: "凤翔区",
leaf: false,
level: 7,
},
{
label: "Qishan",
value: "岐山县",
leaf: false,
level: 7,
},
{
label: "Fufeng",
value: "扶风县",
leaf: false,
level: 7,
},
{
label: "Meixian",
value: "眉县",
leaf: false,
level: 7,
},
{
label: "Longxian",
value: "陇县",
leaf: false,
level: 7,
},
{
label: "Qianyang",
value: "千阳县",
leaf: false,
level: 7,
},
{
label: "Linyou",
value: "麟游县",
leaf: false,
level: 7,
},
{
label: "Fengxian",
value: "凤县",
leaf: false,
level: 7,
},
{
label: "Taibai",
value: "太白县",
leaf: false,
level: 7,
}
]
},
{
label: "Xianyang",
value: "咸阳市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Qindu",
value: "秦都区",
leaf: false,
level: 7,
},
{
label: "Weicheng",
value: "渭城区",
leaf: false,
level: 7,
},
{
label: "Yangling",
value: "杨陵区",
leaf: false,
level: 7,
},
{
label: "Xingping",
value: "兴平市",
leaf: false,
level: 7,
},
{
label: "Sanyuan",
value: "三原县",
leaf: false,
level: 7,
},
{
label: "Jingyang",
value: "泾阳县",
leaf: false,
level: 7,
},
{
label: "Qianxian",
value: "乾县",
leaf: false,
level: 7,
},
{
label: "Liquan",
value: "礼泉县",
leaf: false,
level: 7,
},
{
label: "Yongshou",
value: "永寿县",
leaf: false,
level: 7,
},
{
label: "Binzhou",
value: "彬州市",
leaf: false,
level: 7,
},
{
label: "Changwu",
value: "长武县",
leaf: false,
level: 7,
},
{
label: "Xunyi",
value: "旬邑县",
leaf: false,
level: 7,
},
{
label: "Chunhua",
value: "淳化县",
leaf: false,
level: 7,
},
{
label: "Wugong",
value: "武功县",
leaf: false,
level: 7,
}
]
},
{
label: "Weinan",
value: "渭南市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Linwei",
value: "临渭区",
leaf: false,
level: 7,
},
{
label: "Huazhou",
value: "华州区",
leaf: false,
level: 7,
},
{
label: "Tongguan",
value: "潼关县",
leaf: false,
level: 7,
},
{
label: "Dali",
value: "大荔县",
leaf: false,
level: 7,
},
{
label: "Heyang",
value: "合阳县",
leaf: false,
level: 7,
},
{
label: "Chengcheng",
value: "澄城县",
leaf: false,
level: 7,
},
{
label: "Pucheng",
value: "蒲城县",
leaf: false,
level: 7,
},
{
label: "Baishui",
value: "白水县",
leaf: false,
level: 7,
},
{
label: "Fuping",
value: "富平县",
leaf: false,
level: 7,
},
{
label: "Hancheng",
value: "韩城市",
leaf: false,
level: 7,
},
{
label: "Huayin",
value: "华阴市",
leaf: false,
level: 7,
}
]
},
{
label: "Tongchuan",
value: "铜川市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Wangyi",
value: "王益区",
leaf: false,
level: 7,
},
{
label: "Yintai",
value: "印台区",
leaf: false,
level: 7,
},
{
label: "Yaozhou",
value: "耀州区",
leaf: false,
level: 7,
},
{
label: "Yijun",
value: "宜君县",
leaf: false,
level: 7,
}
]
},
{
label: "Yanan",
value: "延安市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Baota",
value: "宝塔区",
leaf: false,
level: 7,
},
{
label: "Ansai",
value: "安塞区",
leaf: false,
level: 7,
},
{
label: "Yanchang",
value: "延长县",
leaf: false,
level: 7,
},
{
label: "Yanchuan",
value: "延川县",
leaf: false,
level: 7,
},
{
label: "Zichang",
value: "子长市",
leaf: false,
level: 7,
},
{
label: "Zhidan",
value: "志丹县",
leaf: false,
level: 7,
},
{
label: "Wuqi",
value: "吴起县",
leaf: false,
level: 7,
},
{
label: "Ganquan",
value: "甘泉县",
leaf: false,
level: 7,
},
{
label: "Fuxian",
value: "富县",
leaf: false,
level: 7,
},
{
label: "Luochuan",
value: "洛川县",
leaf: false,
level: 7,
},
{
label: "Yichuan",
value: "宜川县",
leaf: false,
level: 7,
},
{
label: "Huanglong",
value: "黄龙县",
leaf: false,
level: 7,
},
{
label: "Huangling",
value: "黄陵县",
leaf: false,
level: 7,
}
]
},
{
label: "Yulin",
value: "榆林市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Yuyang",
value: "榆阳区",
leaf: false,
level: 7,
},
{
label: "Hengshan",
value: "横山区",
leaf: false,
level: 7,
},
{
label: "Shenmu",
value: "神木市",
leaf: false,
level: 7,
},
{
label: "Fugu",
value: "府谷县",
leaf: false,
level: 7,
},
{
label: "Jingbian",
value: "靖边县",
leaf: false,
level: 7,
},
{
label: "Dingbian",
value: "定边县",
leaf: false,
level: 7,
},
{
label: "Suide",
value: "绥德县",
leaf: false,
level: 7,
},
{
label: "Mizhi",
value: "米脂县",
leaf: false,
level: 7,
},
{
label: "Jiaxian",
value: "佳县",
leaf: false,
level: 7,
},
{
label: "Wubu",
value: "吴堡县",
leaf: false,
level: 7,
},
{
label: "Qingjian",
value: "清涧县",
leaf: false,
level: 7,
},
{
label: "Zizhou",
value: "子洲县",
leaf: false,
level: 7,
}
]
},
{
label: "Hanzhong",
value: "汉中市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Hantai",
value: "汉台区",
leaf: false,
level: 7,
},
{
label: "Nanzheng",
value: "南郑区",
leaf: false,
level: 7,
},
{
label: "Chenggu",
value: "城固县",
leaf: false,
level: 7,
},
{
label: "Yangxian",
value: "洋县",
leaf: false,
level: 7,
},
{
label: "Xixiang",
value: "西乡县",
leaf: false,
level: 7,
},
{
label: "Mianxian",
value: "勉县",
leaf: false,
level: 7,
},
{
label: "Ningqiang",
value: "宁强县",
leaf: false,
level: 7,
},
{
label: "Lüeyang",
value: "略阳县",
leaf: false,
level: 7,
},
{
label: "Zhenba",
value: "镇巴县",
leaf: false,
level: 7,
},
{
label: "Liuba",
value: "留坝县",
leaf: false,
level: 7,
},
{
label: "Foping",
value: "佛坪县",
leaf: false,
level: 7,
}
]
},
{
label: "Ankang",
value: "安康市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Hanbin",
value: "汉滨区",
leaf: false,
level: 7,
},
{
label: "Hanyin",
value: "汉阴县",
leaf: false,
level: 7,
},
{
label: "Shiquan",
value: "石泉县",
leaf: false,
level: 7,
},
{
label: "Ningshan",
value: "宁陕县",
leaf: false,
level: 7,
},
{
label: "Ziyang",
value: "紫阳县",
leaf: false,
level: 7,
},
{
label: "Langao",
value: "岚皋县",
leaf: false,
level: 7,
},
{
label: "Pingli",
value: "平利县",
leaf: false,
level: 7,
},
{
label: "Zhenping",
value: "镇坪县",
leaf: false,
level: 7,
},
{
label: "Xunyang",
value: "旬阳市",
leaf: false,
level: 7,
},
{
label: "Baihe",
value: "白河县",
leaf: false,
level: 7,
}
]
},
{
label: "Shangluo",
value: "商洛市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Shangzhou",
value: "商州区",
leaf: false,
level: 7,
},
{
label: "Luonan",
value: "洛南县",
leaf: false,
level: 7,
},
{
label: "Danfeng",
value: "丹凤县",
leaf: false,
level: 7,
},
{
label: "Shangnan",
value: "商南县",
leaf: false,
level: 7,
},
{
label: "Shanyang",
value: "山阳县",
leaf: false,
level: 7,
},
{
label: "Zhenan",
value: "镇安县",
leaf: false,
level: 7,
},
{
label: "Zhashui",
value: "柞水县",
leaf: false,
level: 7,
}
]
}
]
},
{
label: "Gansu",
value: "甘肃省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6,
},
{
label: "Lanzhou",
value: "兰州市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Qilihe",
value: "七里河区",
leaf: false,
level: 7,
},
{
label: "Anning",
value: "安宁区",
leaf: false,
level: 7,
},
{
label: "Honggu",
value: "红古区",
leaf: false,
level: 7,
},
{
label: "Gaolan",
value: "皋兰县",
leaf: false,
level: 7,
},
{
label: "Yuzhong",
value: "榆中县",
leaf: false,
level: 7,
}
]
},
{
label: "Jiayuguan",
value: "嘉峪关市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
}
]
},
{
label: "Jinchang",
value: "金昌市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Jinchuan",
value: "金川区",
leaf: false,
level: 7,
},
{
label: "Yongchang",
value: "永昌县",
leaf: false,
level: 7,
}
]
},
{
label: "Baiyin",
value: "白银市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Baiyin",
value: "白银区",
leaf: false,
level: 7,
},
{
label: "Pingchuan",
value: "平川区",
leaf: false,
level: 7,
},
{
label: "Jingyuan",
value: "靖远县",
leaf: false,
level: 7,
},
{
label: "Huining",
value: "会宁县",
leaf: false,
level: 7,
},
{
label: "Jingtai",
value: "景泰县",
leaf: false,
level: 7,
}
]
},
{
label: "Tianshui",
value: "天水市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Qinzhou",
value: "秦州区",
leaf: false,
level: 7,
},
{
label: "Maiji",
value: "麦积区",
leaf: false,
level: 7,
},
{
label: "Qingshui",
value: "清水县",
leaf: false,
level: 7,
},
{
label: "Qinan",
value: "秦安县",
leaf: false,
level: 7,
},
{
label: "Gangu",
value: "甘谷县",
leaf: false,
level: 7,
},
{
label: "Wushan",
value: "武山县",
leaf: false,
level: 7,
},
{
label: "Zhangjiachuanhuizu",
value: "张家川回族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "Wuwei",
value: "武威市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Liangzhou",
value: "凉州区",
leaf: false,
level: 7,
},
{
label: "Minqin",
value: "民勤县",
leaf: false,
level: 7,
},
{
label: "Gulang",
value: "古浪县",
leaf: false,
level: 7,
},
{
label: "Tianzhuzangzu",
value: "天祝藏族自治县",
leaf: false,
level: 7,
}
]
},
{
label: "Zhangye",
value: "张掖市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Ganzhou",
value: "甘州区",
leaf: false,
level: 7,
},
{
label: "Sunanyuguzu",
value: "肃南裕固族自治县",
leaf: false,
level: 7,
},
{
label: "Minyue",
value: "民乐县",
leaf: false,
level: 7,
},
{
label: "Linze",
value: "临泽县",
leaf: false,
level: 7,
},
{
label: "Gaotai",
value: "高台县",
leaf: false,
level: 7,
},
{
label: "Shandan",
value: "山丹县",
leaf: false,
level: 7,
}
]
},
{
label: "Pingliang",
value: "平凉市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Kongtong",
value: "崆峒区",
leaf: false,
level: 7,
},
{
label: "Jingchuan",
value: "泾川县",
leaf: false,
level: 7,
},
{
label: "Lingtai",
value: "灵台县",
leaf: false,
level: 7,
},
{
label: "Chongxin",
value: "崇信县",
leaf: false,
level: 7,
},
{
label: "Huating",
value: "华亭市",
leaf: false,
level: 7,
},
{
label: "Zhuanglang",
value: "庄浪县",
leaf: false,
level: 7,
},
{
label: "Jingning",
value: "静宁县",
leaf: false,
level: 7,
}
]
},
{
label: "Jiuquan",
value: "酒泉市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Suzhou",
value: "肃州区",
leaf: false,
level: 7,
},
{
label: "Jinta",
value: "金塔县",
leaf: false,
level: 7,
},
{
label: "Guazhou",
value: "瓜州县",
leaf: false,
level: 7,
},
{
label: "Subeimengguzu",
value: "肃北蒙古族自治县",
leaf: false,
level: 7,
},
{
label: "AkesaiKazak",
value: "阿克塞哈萨克族自治县",
leaf: false,
level: 7,
},
{
label: "Yumen",
value: "玉门市",
leaf: false,
level: 7,
},
{
label: "Dunhuang",
value: "敦煌市",
leaf: false,
level: 7,
}
]
},
{
label: "Qingyang",
value: "庆阳市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Xifeng",
value: "西峰区",
leaf: false,
level: 7,
},
{
label: "Qingcheng",
value: "庆城县",
leaf: false,
level: 7,
},
{
label: "Huanxian",
value: "环县",
leaf: false,
level: 7,
},
{
label: "Huachi",
value: "华池县",
leaf: false,
level: 7,
},
{
label: "Heshui",
value: "合水县",
leaf: false,
level: 7,
},
{
label: "Zhengning",
value: "正宁县",
leaf: false,
level: 7,
},
{
label: "Ningxian",
value: "宁县",
leaf: false,
level: 7,
},
{
label: "Zhenyuan",
value: "镇原县",
leaf: false,
level: 7,
}
]
},
{
label: "Dingxi",
value: "定西市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Anding",
value: "安定区",
leaf: false,
level: 7,
},
{
label: "Tongwei",
value: "通渭县",
leaf: false,
level: 7,
},
{
label: "Longxi",
value: "陇西县",
leaf: false,
level: 7,
},
{
label: "Weiyuan",
value: "渭源县",
leaf: false,
level: 7,
},
{
label: "Lintao",
value: "临洮县",
leaf: false,
level: 7,
},
{
label: "Zhangxian",
value: "漳县",
leaf: false,
level: 7,
},
{
label: "Minxian",
value: "岷县",
leaf: false,
level: 7,
}
]
},
{
label: "Longnan",
value: "陇南市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Wudu",
value: "武都区",
leaf: false,
level: 7,
},
{
label: "Chengxian",
value: "成县",
leaf: false,
level: 7,
},
{
label: "Wenxian",
value: "文县",
leaf: false,
level: 7,
},
{
label: "Dangchang",
value: "宕昌县",
leaf: false,
level: 7,
},
{
label: "Kangxian",
value: "康县",
leaf: false,
level: 7,
},
{
label: "Xihe",
value: "西和县",
leaf: false,
level: 7,
},
{
label: "Lixian",
value: "礼县",
leaf: false,
level: 7,
},
{
label: "Huixian",
value: "徽县",
leaf: false,
level: 7,
},
{
label: "Liangdang",
value: "两当县",
leaf: false,
level: 7,
}
]
},
{
label: "Gannanzangzu",
value: "甘南藏族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Hezuo",
value: "合作市",
leaf: false,
level: 7,
},
{
label: "Lintan",
value: "临潭县",
leaf: false,
level: 7,
},
{
label: "Zhuoni",
value: "卓尼县",
leaf: false,
level: 7,
},
{
label: "Zhouqu",
value: "舟曲县",
leaf: false,
level: 7,
},
{
label: "Diebu",
value: "迭部县",
leaf: false,
level: 7,
},
{
label: "Maqu",
value: "玛曲县",
leaf: false,
level: 7,
},
{
label: "Luqu",
value: "碌曲县",
leaf: false,
level: 7,
},
{
label: "Xiahe",
value: "夏河县",
leaf: false,
level: 7,
}
]
},
{
label: "Linxiahuizu",
value: "临夏回族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7,
},
{
label: "Linxia",
value: "临夏市",
leaf: false,
level: 7,
},
{
label: "Linxia",
value: "临夏县",
leaf: false,
level: 7,
},
{
label: "Kangle",
value: "康乐县",
leaf: false,
level: 7,
},
{
label: "Yongjing",
value: "永靖县",
leaf: false,
level: 7,
},
{
label: "Guanghe",
value: "广河县",
leaf: false,
level: 7,
},
{
label: "Hezheng",
value: "和政县",
leaf: false,
level: 7,
},
{
label: "Dongxiangzu",
value: "东乡族自治县",
leaf: false,
level: 7,
},
{
label: "Jishishanbaoanzudongxiangzuandsalazu",
value: "积石山保安族东乡族撒拉族自治县",
leaf: false,
level: 7,
}
]
}
]
},
{
label: "Qinghai",
value: "青海省",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Xining",
value: "西宁市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Chengdong",
value: "城东区",
leaf: false,
level: 7
},
{
label: "Chengzhong",
value: "城中区",
leaf: false,
level: 7
},
{
label: "Chengxi",
value: "城西区",
leaf: false,
level: 7
},
{
label: "Chengbei",
value: "城北区",
leaf: false,
level: 7
},
{
label: "Huangzhong",
value: "湟中区",
leaf: false,
level: 7
},
{
label: "Huangyuan",
value: "湟源县",
leaf: false,
level: 7
},
{
label: "Datonghuizutuzu",
value: "大通回族土族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Haidong",
value: "海东市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Ledu",
value: "乐都区",
leaf: false,
level: 7
},
{
label: "Pingan",
value: "平安区",
leaf: false,
level: 7
},
{
label: "Minhehuizuandtuzu",
value: "民和回族土族自治县",
leaf: false,
level: 7
},
{
label: "Huzhutuzu",
value: "互助土族自治县",
leaf: false,
level: 7
},
{
label: "Hualonghuizu",
value: "化隆回族自治县",
leaf: false,
level: 7
},
{
label: "Xunhuasalazu",
value: "循化撒拉族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Haibeizangzu",
value: "海北藏族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Menyuanhuizu",
value: "门源回族自治县",
leaf: false,
level: 7
},
{
label: "Qilian",
value: "祁连县",
leaf: false,
level: 7
},
{
label: "Haiyan",
value: "海晏县",
leaf: false,
level: 7
},
{
label: "Gangcha",
value: "刚察县",
leaf: false,
level: 7
}
]
},
{
label: "Huangnanzangzu",
value: "黄南藏族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Tongren",
value: "同仁市",
leaf: false,
level: 7
},
{
label: "Jianzha",
value: "尖扎县",
leaf: false,
level: 7
},
{
label: "Zeku",
value: "泽库县",
leaf: false,
level: 7
},
{
label: "Henanmengguzu",
value: "河南蒙古族自治县",
leaf: false,
level: 7
}
]
},
{
label: "Hainanzangzu",
value: "海南藏族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Gonghe",
value: "共和县",
leaf: false,
level: 7
},
{
label: "Guide",
value: "贵德县",
leaf: false,
level: 7
},
{
label: "Guinan",
value: "贵南县",
leaf: false,
level: 7
},
{
label: "Tongde",
value: "同德县",
leaf: false,
level: 7
},
{
label: "Xinghai",
value: "兴海县",
leaf: false,
level: 7
}
]
},
{
label: "Guoluozangzu",
value: "果洛藏族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Maqin",
value: "玛沁县",
leaf: false,
level: 7
},
{
label: "Banma",
value: "班玛县",
leaf: false,
level: 7
},
{
label: "Gande",
value: "甘德县",
leaf: false,
level: 7
},
{
label: "Dari",
value: "达日县",
leaf: false,
level: 7
},
{
label: "Jiuzhi",
value: "久治县",
leaf: false,
level: 7
},
{
label: "Maduo",
value: "玛多县",
leaf: false,
level: 7
}
]
},
{
label: "Yushuzangzu",
value: "玉树藏族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yushu",
value: "玉树市",
leaf: false,
level: 7
},
{
label: "Zaduo",
value: "杂多县",
leaf: false,
level: 7
},
{
label: "Chengduo",
value: "称多县",
leaf: false,
level: 7
},
{
label: "Zhiduo",
value: "治多县",
leaf: false,
level: 7
},
{
label: "Nangqian",
value: "囊谦县",
leaf: false,
level: 7
},
{
label: "Qumalai",
value: "曲麻莱县",
leaf: false,
level: 7
}
]
},
{
label: "Haiximengguzuzangzu",
value: "海西蒙古族藏族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Geermu",
value: "格尔木市",
leaf: false,
level: 7
},
{
label: "Delingha",
value: "德令哈市",
leaf: false,
level: 7
},
{
label: "Mangya",
value: "茫崖市",
leaf: false,
level: 7
},
{
label: "Dulan",
value: "都兰县",
leaf: false,
level: 7
},
{
label: "Wulan",
value: "乌兰县",
leaf: false,
level: 7
},
{
label: "Tianjun",
value: "天峻县",
leaf: false,
level: 7
}
]
}
]
},
{
label: "Ningxia",
value: "宁夏回族自治区",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Yinchuan",
value: "银川市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Xingqing",
value: "兴庆区",
leaf: false,
level: 7
},
{
label: "Xixia",
value: "西夏区",
leaf: false,
level: 7
},
{
label: "Jinfeng",
value: "金凤区",
leaf: false,
level: 7
},
{
label: "Yongning",
value: "永宁县",
leaf: false,
level: 7
},
{
label: "Helan",
value: "贺兰县",
leaf: false,
level: 7
},
{
label: "Lingwu",
value: "灵武市",
leaf: false,
level: 7
}
]
},
{
label: "Shizuishan",
value: "石嘴山市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Dawukou",
value: "大武口区",
leaf: false,
level: 7
},
{
label: "Huinong",
value: "惠农区",
leaf: false,
level: 7
},
{
label: "Pingluo",
value: "平罗县",
leaf: false,
level: 7
}
]
},
{
label: "Wuzhong",
value: "吴忠市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Litong",
value: "利通区",
leaf: false,
level: 7
},
{
label: "Hongsibu",
value: "红寺堡区",
leaf: false,
level: 7
},
{
label: "Qingtongxia",
value: "青铜峡市",
leaf: false,
level: 7
},
{
label: "Yanchi",
value: "盐池县",
leaf: false,
level: 7
},
{
label: "Tongxin",
value: "同心县",
leaf: false,
level: 7
}
]
},
{
label: "Guyuan",
value: "固原市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yuanzhou",
value: "原州区",
leaf: false,
level: 7
},
{
label: "Xiji",
value: "西吉县",
leaf: false,
level: 7
},
{
label: "Longde",
value: "隆德县",
leaf: false,
level: 7
},
{
label: "Jingyuan",
value: "泾源县",
leaf: false,
level: 7
},
{
label: "Pengyang",
value: "彭阳县",
leaf: false,
level: 7
}
]
},
{
label: "Zhongwei",
value: "中卫市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Shapotou",
value: "沙坡头区",
leaf: false,
level: 7
},
{
label: "Zhongning",
value: "中宁县",
leaf: false,
level: 7
},
{
label: "Haiyuan",
value: "海原县",
leaf: false,
level: 7
}
]
}
]
},
{
label: "Xinjiang",
value: "新疆维吾尔自治区",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Wulumuqi",
value: "乌鲁木齐市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Tianshan",
value: "天山区",
leaf: false,
level: 7
},
{
label: "Shayibake",
value: "沙依巴克区",
leaf: false,
level: 7
},
{
label: "Xinshi",
value: "新市区",
leaf: false,
level: 7
},
{
label: "Shuimogou",
value: "水磨沟区",
leaf: false,
level: 7
},
{
label: "Toutunhe",
value: "头屯河区",
leaf: false,
level: 7
},
{
label: "Dabancheng",
value: "达坂城区",
leaf: false,
level: 7
},
{
label: "Midong",
value: "米东区",
leaf: false,
level: 7
},
{
label: "Wulumuqi",
value: "乌鲁木齐县",
leaf: false,
level: 7
}
]
},
{
label: "Kelamayi",
value: "克拉玛依市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Kelamayi",
value: "克拉玛依区",
leaf: false,
level: 7
},
{
label: "Dushanzi",
value: "独山子区",
leaf: false,
level: 7
},
{
label: "Baijiantan",
value: "白碱滩区",
leaf: false,
level: 7
},
{
label: "Wuerhe",
value: "乌尔禾区",
leaf: false,
level: 7
}
]
},
{
label: "Tulufan",
value: "吐鲁番市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Gaochang",
value: "高昌区",
leaf: false,
level: 7
},
{
label: "Shanshan",
value: "鄯善县",
leaf: false,
level: 7
},
{
label: "Tuokexun",
value: "托克逊县",
leaf: false,
level: 7
}
]
},
{
label: "Hami",
value: "哈密市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yizhou",
value: "伊州区",
leaf: false,
level: 7
},
{
label: "Balikunhasake",
value: "巴里坤哈萨克自治县",
leaf: false,
level: 7
},
{
label: "Yiwu",
value: "伊吾县",
leaf: false,
level: 7
}
]
},
{
label: "Akesu",
value: "阿克苏地区",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Akesu",
value: "阿克苏市",
leaf: false,
level: 7
},
{
label: "Wensu",
value: "温宿县",
leaf: false,
level: 7
},
{
label: "Kuche",
value: "库车市",
leaf: false,
level: 7
},
{
label: "Shaya",
value: "沙雅县",
leaf: false,
level: 7
},
{
label: "Xinhe",
value: "新和县",
leaf: false,
level: 7
},
{
label: "Baicheng",
value: "拜城县",
leaf: false,
level: 7
},
{
label: "Wushi",
value: "乌什县",
leaf: false,
level: 7
},
{
label: "Awati",
value: "阿瓦提县",
leaf: false,
level: 7
},
{
label: "Keping",
value: "柯坪县",
leaf: false,
level: 7
}
]
},
{
label: "Kashi",
value: "喀什地区",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Kashi",
value: "喀什市",
leaf: false,
level: 7
},
{
label: "Shufu",
value: "疏附县",
leaf: false,
level: 7
},
{
label: "Shule",
value: "疏勒县",
leaf: false,
level: 7
},
{
label: "Yingjisha",
value: "英吉沙县",
leaf: false,
level: 7
},
{
label: "Zepu",
value: "泽普县",
leaf: false,
level: 7
},
{
label: "Shache",
value: "莎车县",
leaf: false,
level: 7
},
{
label: "Yecheng",
value: "叶城县",
leaf: false,
level: 7
},
{
label: "Maigaiti",
value: "麦盖提县",
leaf: false,
level: 7
},
{
label: "Yuepuhu",
value: "岳普湖县",
leaf: false,
level: 7
},
{
label: "Jiashi",
value: "伽师县",
leaf: false,
level: 7
},
{
label: "Bachu",
value: "巴楚县",
leaf: false,
level: 7
},
{
label: "Tashikuergantajike",
value: "塔什库尔干塔吉克自治县",
leaf: false,
level: 7
}
]
},
{
label: "Hetian",
value: "和田地区",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Hetian",
value: "和田市",
leaf: false,
level: 7
},
{
label: "Hetian",
value: "和田县",
leaf: false,
level: 7
},
{
label: "Moyu",
value: "墨玉县",
leaf: false,
level: 7
},
{
label: "Pishan",
value: "皮山县",
leaf: false,
level: 7
},
{
label: "Luopu",
value: "洛浦县",
leaf: false,
level: 7
},
{
label: "Cele",
value: "策勒县",
leaf: false,
level: 7
},
{
label: "Yutian",
value: "于田县",
leaf: false,
level: 7
},
{
label: "Minfeng",
value: "民丰县",
leaf: false,
level: 7
}
]
},
{
label: "Changjihuizu",
value: "昌吉回族自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Changji",
value: "昌吉市",
leaf: false,
level: 7
},
{
label: "Fukang",
value: "阜康市",
leaf: false,
level: 7
},
{
label: "Hutubi",
value: "呼图壁县",
leaf: false,
level: 7
},
{
label: "Manasi",
value: "玛纳斯县",
leaf: false,
level: 7
},
{
label: "Qitai",
value: "奇台县",
leaf: false,
level: 7
},
{
label: "Jimusaer",
value: "吉木萨尔县",
leaf: false,
level: 7
},
{
label: "Muleihasake",
value: "木垒哈萨克自治县",
leaf: false,
level: 7
}
]
},
{
label: "Boertalamenggu",
value: "博尔塔拉蒙古自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Bole",
value: "博乐市",
leaf: false,
level: 7
},
{
label: "Alashankou",
value: "阿拉山口市",
leaf: false,
level: 7
},
{
label: "Jinghe",
value: "精河县",
leaf: false,
level: 7
},
{
label: "Wenquan",
value: "温泉县",
leaf: false,
level: 7
}
]
},
{
label: "Bayinguolengmenggu",
value: "巴音郭楞蒙古自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Kuerle",
value: "库尔勒市",
leaf: false,
level: 7
},
{
label: "Luntai",
value: "轮台县",
leaf: false,
level: 7
},
{
label: "Yuli",
value: "尉犁县",
leaf: false,
level: 7
},
{
label: "Ruoqiang",
value: "若羌县",
leaf: false,
level: 7
},
{
label: "Qiemo",
value: "且末县",
leaf: false,
level: 7
},
{
label: "Yanqihuizu",
value: "焉耆回族自治县",
leaf: false,
level: 7
},
{
label: "Hejing",
value: "和静县",
leaf: false,
level: 7
},
{
label: "Heshuo",
value: "和硕县",
leaf: false,
level: 7
},
{
label: "Bohu",
value: "博湖县",
leaf: false,
level: 7
}
]
},
{
label: "Kezilesukeerkezi",
value: "克孜勒苏柯尔克孜自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Atushi",
value: "阿图什市",
leaf: false,
level: 7
},
{
label: "Aketao",
value: "阿克陶县",
leaf: false,
level: 7
},
{
label: "Aheqi",
value: "阿合奇县",
leaf: false,
level: 7
},
{
label: "Wuqia",
value: "乌恰县",
leaf: false,
level: 7
}
]
},
{
label: "Yilihasake",
value: "伊犁哈萨克自治州",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Yining",
value: "伊宁市",
leaf: false,
level: 7
},
{
label: "Kuitun",
value: "奎屯市",
leaf: false,
level: 7
},
{
label: "Huoerguosi",
value: "霍尔果斯市",
leaf: false,
level: 7
},
{
label: "Yining",
value: "伊宁县",
leaf: false,
level: 7
},
{
label: "Chabuchaerxibo",
value: "察布查尔锡伯自治县",
leaf: false,
level: 7
},
{
label: "Huocheng",
value: "霍城县",
leaf: false,
level: 7
},
{
label: "Gongliu",
value: "巩留县",
leaf: false,
level: 7
},
{
label: "Xinyuan",
value: "新源县",
leaf: false,
level: 7
},
{
label: "Zhaosu",
value: "昭苏县",
leaf: false,
level: 7
},
{
label: "Tekesi",
value: "特克斯县",
leaf: false,
level: 7
},
{
label: "Nileke",
value: "尼勒克县",
leaf: false,
level: 7
}
]
},
{
label: "Tacheng",
value: "塔城地区",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Tacheng",
value: "塔城市",
leaf: false,
level: 7
},
{
label: "Wusu",
value: "乌苏市",
leaf: false,
level: 7
},
{
label: "Shawan",
value: "沙湾市",
leaf: false,
level: 7
},
{
label: "Emin",
value: "额敏县",
leaf: false,
level: 7
},
{
label: "Tuoli",
value: "托里县",
leaf: false,
level: 7
},
{
label: "Hebukesaiermenggu",
value: "和布克赛尔蒙古自治县",
leaf: false,
level: 7
}
]
},
{
label: "Aletai",
value: "阿勒泰地区",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Aletai",
value: "阿勒泰市",
leaf: false,
level: 7
},
{
label: "Buerjin",
value: "布尔津县",
leaf: false,
level: 7
},
{
label: "Fuyun",
value: "富蕴县",
leaf: false,
level: 7
},
{
label: "Fuhai",
value: "福海县",
leaf: false,
level: 7
},
{
label: "Habahe",
value: "哈巴河县",
leaf: false,
level: 7
},
{
label: "Qinghe",
value: "青河县",
leaf: false,
level: 7
},
{
label: "Jimunai",
value: "吉木乃县",
leaf: false,
level: 7
}
]
}
]
}

                ],
              },
              {
                label: "Tibet",
                value: "西藏",
                leaf: false,
                level: 4,
                children:[
                  {
                    label: "Province",
                    value: "省级",
                    leaf: true,
                    level: 5,
                    disabled: true,
                    display: true,
                  },
{
label: "Xizang",
value: "西藏自治区",
leaf: false,
level: 5,
children: [
{
label: "Prefecture",
value: "地级",
leaf: false,
level: 6
},
{
label: "Lasa",
value: "拉萨市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Chengguan",
value: "城关区",
leaf: false,
level: 7
},
{
label: "Duilongdeqing",
value: "堆龙德庆区",
leaf: false,
level: 7
},
{
label: "Dazi",
value: "达孜区",
leaf: false,
level: 7
}
]
},
{
label: "Rikaze",
value: "日喀则市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Sangzhuzi",
value: "桑珠孜区",
leaf: false,
level: 7
},
{
label: "Nanmulin",
value: "南木林县",
leaf: false,
level: 7
},
{
label: "Jiangzi",
value: "江孜县",
leaf: false,
level: 7
},
{
label: "Dingri",
value: "定日县",
leaf: false,
level: 7
},
{
label: "Sajia",
value: "萨迦县",
leaf: false,
level: 7
},
{
label: "Lazi",
value: "拉孜县",
leaf: false,
level: 7
},
{
label: "Angren",
value: "昂仁县",
leaf: false,
level: 7
},
{
label: "Xietongmen",
value: "谢通门县",
leaf: false,
level: 7
},
{
label: "Bailang",
value: "白朗县",
leaf: false,
level: 7
}
]
},
{
label: "Changdu",
value: "昌都市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Karuo",
value: "卡若区",
leaf: false,
level: 7
},
{
label: "Jiangda",
value: "江达县",
leaf: false,
level: 7
},
{
label: "Gongjue",
value: "贡觉县",
leaf: false,
level: 7
},
{
label: "Leiwuqi",
value: "类乌齐县",
leaf: false,
level: 7
}
]
},
{
label: "Linzhi",
value: "林芝市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Bayi",
value: "巴宜区",
leaf: false,
level: 7
},
{
label: "Milin",
value: "米林县",
leaf: false,
level: 7
},
{
label: "Motuo",
value: "墨脱县",
leaf: false,
level: 7
}
]
},
{
label: "Shannan",
value: "山南市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Naidong",
value: "乃东区",
leaf: false,
level: 7
},
{
label: "Zhanang",
value: "扎囊县",
leaf: false,
level: 7
},
{
label: "Gongga",
value: "贡嘎县",
leaf: false,
level: 7
},
{
label: "Jiacha",
value: "加查县",
leaf: false,
level: 7
},
{
label: "Longzi",
value: "隆子县",
leaf: false,
level: 7
}
]
},
{
label: "Naqu",
value: "那曲市",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Seni",
value: "色尼区",
leaf: false,
level: 7
},
{
label: "Anduo",
value: "安多县",
leaf: false,
level: 7
},
{
label: "Biru",
value: "比如县",
leaf: false,
level: 7
},
{
label: "Nierong",
value: "聂荣县",
leaf: false,
level: 7
},
{
label: "Shenzha",
value: "申扎县",
leaf: false,
level: 7
},
{
label: "Bange",
value: "班戈县",
leaf: false,
level: 7
},
{
label: "Nima",
value: "尼玛县",
leaf: false,
level: 7
}
]
},
{
label: "Ali",
value: "阿里地区",
leaf: false,
level: 6,
children: [
{
label: "County",
value: "县级",
leaf: false,
level: 7
},
{
label: "Gaer",
value: "噶尔县",
leaf: false,
level: 7
},
{
label: "Pulan",
value: "普兰县",
leaf: false,
level: 7
},
{
label: "Zhada",
value: "札达县",
leaf: false,
level: 7
},
{
label: "Ritu",
value: "日土县",
leaf: false,
level: 7
},
{
label: "Geji",
value: "革吉县",
leaf: false,
level: 7
},
{
label: "Gaize",
value: "改则县",
leaf: false,
level: 7
},
{
label: "Cuoqin",
value: "措勤县",
leaf: false,
level: 7
}
]
}
]
}

                ],
              },
              {
                label:"Others",
                value:"Others",
                leaf:false,
                level:4,
                children:[
                  {
                    label: "Taiwan",
                    value: "台湾省",
                    leaf: true,
                    level: 5,
                  },
                  {
label: "HongKong",
value: "香港特别行政区",
leaf: true,
level: 5,
},
{
label: "Aomen",
value: "澳门特别行政区",
leaf: true,
level: 5,
}

                ]
              }
              

            ],
          },
          { label: "Japan", value: "日本", leaf: false, level: 3 },
          { label: "South Korea", value: "韩国", leaf: false, level: 3 },
          { label: "North Korea", value: "朝鲜", leaf: false, level: 3 },
        ],
      },
{ label: "Southeast Asia", value: "东南亚", leaf: false, level: 2 ,children:[
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true,
},
{
label: "Indonesia",
value: "印度尼西亚",
leaf: false,
level: 3,
},
{
label: "Philippines",
value: "菲律宾",
leaf: false,
level: 3,
},
{
label: "Malaysia",
value: "马来西亚",
leaf: false,
level: 3,
},
{
label: "Singapore",
value: "新加坡",
leaf: false,
level: 3,
},
{
label: "Thailand",
value: "泰国",
leaf: false,
level: 3,
},
{
label: "Vietnam",
value: "越南",
leaf: false,
level: 3,
},
{
label: "Laos",
value: "老挝",
leaf: false,
level: 3,
},
{
label: "Cambodia",
value: "柬埔寨",
leaf: false,
level: 3,
},
{
label: "Myanmar",
value: "缅甸",
leaf: false,
level: 3,
},
{
label: "Brunei",
value: "文莱",
leaf: false,
level: 3,
},
]},
{
label: "Pacific Islands",
value: "太平洋岛国及地区",
leaf: false,
level: 2,
children:[
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true,
},
{
label: "Fiji",
value: "斐济",
leaf: false,
level: 3,
},
{
label: "Tuvalu",
value: "图瓦卢",
leaf: false,
level: 3,
},
{
label: "Kiribati",
value: "基里巴斯",
leaf: false,
level: 3,
},
{
label: "Nauru",
value: "瑙鲁",
leaf: false,
level: 3,
},
{
label: "Vanuatu",
value: "瓦努阿图",
leaf: false,
level: 3,
},
{
label: "Western Samoa",
value: "萨摩亚",
leaf: false,
level: 3,
},
{
label: "Tonga",
value: "汤加",
leaf: false,
level: 3,
},
{
label: "Federated States of Micronesia",
value: "密克罗尼西亚联邦",
leaf: false,
level: 3,
},
{
label: "Marshall Islands",
value: "马绍尔群岛",
leaf: false,
level: 3,
},
]
}
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
display: true
},
{
label: "Middle East",
value: "中东",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Israel",
value: "以色列",
leaf: false,
level: 3
},
{
label: "Iraq",
value: "伊拉克",
leaf: false,
level: 3
},
{
label: "Iran",
value: "伊朗",
leaf: false,
level: 3
},
{
label: "Syria",
value: "叙利亚",
leaf: false,
level: 3
},
{
label: "Jordan",
value: "约旦",
leaf: false,
level: 3
},
{
label: "Saudi Arabia",
value: "沙特阿拉伯",
leaf: false,
level: 3
},
{
label: "Qatar",
value: "卡塔尔",
leaf: false,
level: 3
},
{
label: "Kuwait",
value: "科威特",
leaf: false,
level: 3
},
{
label: "Bahrain",
value: "巴林",
leaf: false,
level: 3
},
{
label: "United Arab Emirates",
value: "阿拉伯联合酋长国",
leaf: false,
level: 3
},
{
label: "Oman",
value: "阿曼",
leaf: false,
level: 3
},
{
label: "Yemen",
value: "也门",
leaf: false,
level: 3
}
]
},
{
label: "North Africa",
value: "北非",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Egypt",
value: "埃及",
leaf: false,
level: 3
},
{
label: "Libya",
value: "利比亚",
leaf: false,
level: 3
},
{
label: "Algeria",
value: "阿尔及利亚",
leaf: false,
level: 3
},
{
label: "Morocco",
value: "摩洛哥",
leaf: false,
level: 3
},
{
label: "Tunisia",
value: "突尼斯",
leaf: false,
level: 3
}
]
},
{
label: "West Asia",
value: "西亚",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Armenia",
value: "亚美尼亚",
leaf: false,
level: 3
},
{
label: "Azerbaijan",
value: "阿塞拜疆",
leaf: false,
level: 3
},
{
label: "Georgia",
value: "格鲁吉亚",
leaf: false,
level: 3
},
{
label: "Turkey",
value: "土耳其",
leaf: false,
level: 3
},
{
label: "Afghanistan",
value: "阿富汗",
leaf: false,
level: 3
}
]
},
{
label: "West Asia or Middle East",
value: "西亚或中东",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Yemen",
value: "也门",
leaf: false,
level: 3
},
{
label: "Israel",
value: "以色列",
leaf: false,
level: 3
},
{
label: "Iraq",
value: "伊拉克",
leaf: false,
level: 3
},
{
label: "Iran",
value: "伊朗",
leaf: false,
level: 3
},
{
label: "Qatar",
value: "卡塔尔",
leaf: false,
level: 3
},
{
label: "Saudi Arabia",
value: "沙特阿拉伯",
leaf: false,
level: 3
},
{
label: "Kuwait",
value: "科威特",
leaf: false,
level: 3
},
{
label: "Bahrain",
value: "巴林",
leaf: false,
level: 3
},
{
label: "United Arab Emirates",
value: "阿拉伯联合酋长国",
leaf: false,
level: 3
},
{
label: "Oman",
value: "阿曼",
leaf: false,
level: 3
},
{
label: "Gaza Strip",
value: "加沙地带",
leaf: false,
level: 3
},
{
label: "West Bank",
value: "西岸",
leaf: false,
level: 3
},
{
label: "Syria",
value: "叙利亚",
leaf: false,
level: 3
},
{
label: "Jordan",
value: "约旦",
leaf: false,
level: 3
},
{
label: "Lebanon",
value: "黎巴嫩",
leaf: false,
level: 3
}
]
}
]
},
{
label: "North America",
value: "北美地区",
leaf: false,
level: 1,
children: [
{
label: "North America",
value: "北美",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Canada",
value: "加拿大",
leaf: false,
level: 3
},
{
label: "United States",
value: "美国",
leaf: false,
level: 3
}
]
},
{
label: "Central America",
value: "中美洲",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Belize",
value: "伯利兹",
leaf: false,
level: 3
},
{
label: "Guatemala",
value: "危地马拉",
leaf: false,
level: 3
},
{
label: "Costa Rica",
value: "哥斯达黎加",
leaf: false,
level: 3
},
{
label: "Panama",
value: "巴拿马",
leaf: false,
level: 3
},
{
label: "Nicaragua",
value: "尼加拉瓜",
leaf: false,
level: 3
},
{
label: "Honduras",
value: "洪都拉斯",
leaf: false,
level: 3
},
{
label: "El Salvador",
value: "萨尔瓦多",
leaf: false,
level: 3
}
]
}
]
},
{
label: "South Asia",
value: "南亚地区",
leaf: false,
level: 1,
children: [
{
label: "South Asia",
value: "南亚",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Bhutan",
value: "不丹",
leaf: false,
level: 3
},
{
label: "India",
value: "印度",
leaf: false,
level: 3
},
{
label: "Nepal",
value: "尼泊尔",
leaf: false,
level: 3
},
{
label: "Pakistan",
value: "巴基斯坦",
leaf: false,
level: 3
},
{
label: "Bangladesh",
value: "孟加拉国",
leaf: false,
level: 3
},
{
label: "Sri Lanka",
value: "斯里兰卡",
leaf: false,
level: 3
},
{
label: "Maldives",
value: "马尔代夫",
leaf: false,
level: 3
}
]
}
]
},
{
label: "Latin America & Caribbean",
value: "拉丁美洲与加勒比地区",
leaf: false,
level: 1,
children: [
{
label: "Caribbean",
value: "加勒比地区",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Belize",
value: "伯利兹",
leaf: false,
level: 3
},
{
label: "Cuba",
value: "古巴",
leaf: false,
level: 3
},
{
label: "Haiti",
value: "海地",
leaf: false,
level: 3
},
{
label: "Dominican Republic",
value: "多米尼加共和国",
leaf: false,
level: 3
},
{
label: "Jamaica",
value: "牙买加",
leaf: false,
level: 3
},
{
label: "Trinidad and Tobago",
value: "特立尼达和多巴哥",
leaf: false,
level: 3
},
{
label: "Bahamas, The",
value: "巴哈马",
leaf: false,
level: 3
},
{
label: "Barbados",
value: "巴巴多斯",
leaf: false,
level: 3
},
{
label: "Dominica",
value: "多米尼克",
leaf: false,
level: 3
},
{
label: "St. Lucia",
value: "圣卢西亚",
leaf: false,
level: 3
},
{
label: "St. Kitts and Nevis",
value: "圣基茨和尼维斯",
leaf: false,
level: 3
},
{
label: "St. Vincent and the Grenadines",
value: "圣文森特和格林纳丁斯",
leaf: false,
level: 3
},
{
label: "Antigua and Barbuda",
value: "安提瓜和巴布达",
leaf: false,
level: 3
},
{
label: "Grenada",
value: "格林纳达",
leaf: false,
level: 3
}
]
},
{
label: "South America",
value: "南美洲",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Uruguay",
value: "乌拉圭",
leaf: false,
level: 3
},
{
label: "Ecuador",
value: "厄瓜多尔",
leaf: false,
level: 3
},
{
label: "Colombia",
value: "哥伦比亚",
leaf: false,
level: 3
},
{
label: "Guyana",
value: "圭亚那",
leaf: false,
level: 3
},
{
label: "Venezuela",
value: "委内瑞拉",
leaf: false,
level: 3
},
{
label: "Bolivia",
value: "玻利维亚",
leaf: false,
level: 3
},
{
label: "Peru",
value: "秘鲁",
leaf: false,
level: 3
},
{
label: "Brazil",
value: "巴西",
leaf: false,
level: 3
},
{
label: "Paraguay",
value: "巴拉圭",
leaf: false,
level: 3
},
{
label: "Suriname",
value: "苏里南",
leaf: false,
level: 3
},
{
label: "Chile",
value: "智利",
leaf: false,
level: 3
},
{
label: "Argentina",
value: "阿根廷",
leaf: false,
level: 3
}
]
}
]
},
{
label: "Sub-Saharan Africa",
value: "撒哈拉以南非洲地区",
leaf: false,
level: 1,
children: [
{
label: "Eastern Africa",
value: "东非",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Ethiopia",
value: "埃塞俄比亚",
leaf: false,
level: 3
},
{
label: "Eritrea",
value: "厄立特里亚",
leaf: false,
level: 3
},
{
label: "Kenya",
value: "肯尼亚",
leaf: false,
level: 3
},
{
label: "Tanzania",
value: "坦桑尼亚",
leaf: false,
level: 3
},
{
label: "Rwanda",
value: "卢旺达",
leaf: false,
level: 3
},
{
label: "Burundi",
value: "布隆迪",
leaf: false,
level: 3
},
{
label: "Seychelles",
value: "塞舌尔",
leaf: false,
level: 3
},
{
label: "Somalia",
value: "索马里",
leaf: false,
level: 3
}
]
},
{
label: "Central Africa",
value: "中非",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Central African Republic",
value: "中非共和国",
leaf: false,
level: 3
},
{
label: "Chad",
value: "乍得",
leaf: false,
level: 3
},
{
label: "Cameroon",
value: "喀麦隆",
leaf: false,
level: 3
},
{
label: "Gabon",
value: "加蓬",
leaf: false,
level: 3
},
{
label: "Congo",
value: "刚果（布）",
leaf: false,
level: 3
},
{
label: "Democratic Republic of the Congo",
value: "刚果（金）",
leaf: false,
level: 3
},
{
label: "Sao Tome and Principe",
value: "圣多美和普林西比",
leaf: false,
level: 3
},
{
label: "Equatorial Guinea",
value: "赤道几内亚",
leaf: false,
level: 3
}
]
},
{
label: "Southern Africa",
value: "南部非洲",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "South Africa",
value: "南非",
leaf: false,
level: 3
},
{
label: "Botswana",
value: "博茨瓦纳",
leaf: false,
level: 3
},
{
label: "Namibia",
value: "纳米比亚",
leaf: false,
level: 3
},
{
label: "Lesotho",
value: "莱索托",
leaf: false,
level: 3
},
{
label: "Eswatini",
value: "斯威士兰",
leaf: false,
level: 3
},
{
label: "Zimbabwe",
value: "津巴布韦",
leaf: false,
level: 3
},
{
label: "Mozambique",
value: "莫桑比克",
leaf: false,
level: 3
},
{
label: "Malawi",
value: "马拉维",
leaf: false,
level: 3
}
]
},
{
label: "Western Africa",
value: "西非",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Nigeria",
value: "尼日利亚",
leaf: false,
level: 3
},
{
label: "Niger",
value: "尼日尔",
leaf: false,
level: 3
},
{
label: "Mali",
value: "马里",
leaf: false,
level: 3
},
{
label: "Burkina Faso",
value: "布基纳法索",
leaf: false,
level: 3
},
{
label: "Guinea",
value: "几内亚",
leaf: false,
level: 3
},
{
label: "Guinea-Bissau",
value: "几内亚比绍",
leaf: false,
level: 3
},
{
label: "Senegal",
value: "塞内加尔",
leaf: false,
level: 3
},
{
label: "Gambia",
value: "冈比亚",
leaf: false,
level: 3
},
{
label: "Liberia",
value: "利比里亚",
leaf: false,
level: 3
},
{
label: "Côte d'Ivoire",
value: "科特迪瓦",
leaf: false,
level: 3
},
{
label: "Ghana",
value: "加纳",
leaf: false,
level: 3
},
{
label: "Togo",
value: "多哥",
leaf: false,
level: 3
},
{
label: "Benin",
value: "贝宁",
leaf: false,
level: 3
},
{
label: "Mauritania",
value: "毛里塔尼亚",
leaf: false,
level: 3
}
]
}
]
},
{
label: "Europe & Central Asia",
value: "欧洲与中亚地区",
leaf: false,
level: 1,
children: [
{
label: "Southeast Europe",
value: "东南欧",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Serbia",
value: "塞尔维亚",
leaf: false,
level: 3
},
{
label: "North Macedonia",
value: "北马其顿",
leaf: false,
level: 3
},
{
label: "Bulgaria",
value: "保加利亚",
leaf: false,
level: 3
}
]
},
{
label: "Eastern Europe",
value: "东欧",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Russia",
value: "俄罗斯",
leaf: false,
level: 3
},
{
label: "Ukraine",
value: "乌克兰",
leaf: false,
level: 3
},
{
label: "Belarus",
value: "白俄罗斯",
leaf: false,
level: 3
},
{
label: "Moldova",
value: "摩尔多瓦",
leaf: false,
level: 3
}
]
},
{
label: "Central Asia",
value: "中亚",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Kazakhstan",
value: "哈萨克斯坦",
leaf: false,
level: 3
},
{
label: "Uzbekistan",
value: "乌兹别克斯坦",
leaf: false,
level: 3
},
{
label: "Turkmenistan",
value: "土库曼斯坦",
leaf: false,
level: 3
},
{
label: "Tajikistan",
value: "塔吉克斯坦",
leaf: false,
level: 3
},
{
label: "Kyrgyzstan",
value: "吉尔吉斯斯坦",
leaf: false,
level: 3
}
]
},
{
label: "Central Europe",
value: "中欧",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Germany",
value: "德国",
leaf: false,
level: 3
},
{
label: "Poland",
value: "波兰",
leaf: false,
level: 3
},
{
label: "Czech Republic",
value: "捷克共和国",
leaf: false,
level: 3
},
{
label: "Slovakia",
value: "斯洛伐克",
leaf: false,
level: 3
},
{
label: "Hungary",
value: "匈牙利",
leaf: false,
level: 3
},
{
label: "Austria",
value: "奥地利",
leaf: false,
level: 3
},
{
label: "Liechtenstein",
value: "列支敦士登",
leaf: false,
level: 3
},
{
label: "Switzerland",
value: "瑞士",
leaf: false,
level: 3
}
]
},
{
label: "Northern Europe",
value: "北欧",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Denmark",
value: "丹麦",
leaf: false,
level: 3
},
{
label: "Norway",
value: "挪威",
leaf: false,
level: 3
},
{
label: "Sweden",
value: "瑞典",
leaf: false,
level: 3
},
{
label: "Finland",
value: "芬兰",
leaf: false,
level: 3
},
{
label: "Iceland",
value: "冰岛",
leaf: false,
level: 3
}
]
},
{
label: "Southern Europe",
value: "南欧",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "Italy",
value: "意大利",
leaf: false,
level: 3
},
{
label: "Spain",
value: "西班牙",
leaf: false,
level: 3
},
{
label: "Portugal",
value: "葡萄牙",
leaf: false,
level: 3
},
{
label: "Greece",
value: "希腊",
leaf: false,
level: 3
},
{
label: "Malta",
value: "马耳他",
leaf: false,
level: 3
},
{
label: "San Marino",
value: "圣马力诺",
leaf: false,
level: 3
},
{
label: "Monaco",
value: "摩纳哥",
leaf: false,
level: 3
},
{
label: "Slovenia",
value: "斯洛文尼亚",
leaf: false,
level: 3
},
{
label: "Croatia",
value: "克罗地亚",
leaf: false,
level: 3
},
{
label: "Bosnia and Herzegovina",
value: "波斯尼亚和黑塞哥维那",
leaf: false,
level: 3
},
{
label: "Montenegro",
value: "黑山",
leaf: false,
level: 3
},
{
label: "Albania",
value: "阿尔巴尼亚",
leaf: false,
level: 3
}
]
},
{
label: "Western Europe",
value: "西欧",
leaf: false,
level: 2,
children: [
{
label: "Country",
value: "国家",
leaf: true,
level: 3,
disabled: true,
display: true
},
{
label: "United Kingdom",
value: "英国",
leaf: false,
level: 3
},
{
label: "France",
value: "法国",
leaf: false,
level: 3
},
{
label: "Netherlands",
value: "荷兰",
leaf: false,
level: 3
},
{
label: "Belgium",
value: "比利时",
leaf: false,
level: 3
},
{
label: "Luxembourg",
value: "卢森堡",
leaf: false,
level: 3
},
{
label: "Ireland",
value: "爱尔兰",
leaf: false,
level: 3
}
]
}
]
}
];


const original_options2 = [
  {
    label: "电网",
    value: "电网",
    leaf: false,
    level: 1,
    disabled: true,
    display: true,
  },
  {
    label: "华中",
    value: "华中",
    leaf: false,
    level: 1,
    children: [
      {
        label: "省级",
        value: "省级",
        leaf: true,
        level: 2,
        disabled: true,
        display: true,
      },
      { 
        label: "河南省", value: "河南省", leaf: false, level: 2,
        children:[
          {
            label:"地级",
            value: "地级",
            leaf: false,
            level: 3,
          },
          {
            label:"信阳市",
            value: "信阳市",
            leaf: false,
            level: 3,
            children:[
              {
                label:"县级",
                value: "县级",
                leaf: true,
                level: 4,
              },
              {
                label:"潢川县",
                value: "潢川县",
                leaf: true,
                level: 4,
              },
            ],
          },
        ],
      },
      { label: "北京市", value: "北京市", leaf: false, level: 2 },
      { label: "天津市", value: "天津市", leaf: false, level: 2 },
      { label: "河北省", value: "河北省", leaf: false, level: 2 },
      { label: "山西省", value: "山西省", leaf: false, level: 2 },
      { label: "湖北省", value: "湖北省", leaf: false, level: 2 },
      { label: "黑龙江省", value: "黑龙江省", leaf: false, level: 2 },
    ],
  },   
];


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
        label: "Henan", value: "河南省", leaf: false, level: 2,
        children:[
          {
            label:"Prefecture-level",
            value: "地级",
            leaf: false,
            level: 3,
          },
          {
            label:"Xinyang",
            value: "信阳市",
            leaf: false,
            level: 3,
            children:[
              {
                label:"County-level",
                value: "县级",
                leaf: false,
                level: 4,
              },
              {
                label:"Huangchuan",
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


function update_options() {
  // 更新时间相关的选项
  if (locale.value === "chinese") {
    timeSelection1.value = "年";
    timeSelection2.value = "月";
    timeSelection3.value = "小时";
    if(select_time_value.value === "Year" || select_time_value.value === "年"){
      timeScale.value = "年";
      select_time_value.value = "年";
      selectedAreaValue.value = "年";
    } else if(select_time_value.value === "Month" || select_time_value.value === "月"){
      timeScale.value = "月";
      select_time_value.value = "月";
      time_value.value = "月";
    } else if(select_time_value.value === "Hour" || select_time_value.value === "小时"){
      timeScale.value = "小时";
      select_time_value.value = "小时";
      time_value.value = "小时";
    }
    if(time_value.value === "Year"){
      time_value.value = "年";
    } else if(time_value.value === "Month"){
      time_value.value = "月";
    } else if(time_value.value === "Hour"){
      time_value.value = "小时";
    }
  } else {
    timeSelection1.value = "Year";
    timeSelection2.value = "Month";
    timeSelection3.value = "Hour";
    if(select_time_value.value === "年" || select_time_value.value === "Year"){
      timeScale.value = "Year";
      select_time_value.value = "Year";
      time_value.value = "Year";
    } else if(select_time_value.value === "月" || select_time_value.value === "Month"){
      timeScale.value = "Month";
      select_time_value.value = "Month";
      time_value.value = "Month";
    } else if(select_time_value.value === "小时" || select_time_value.value === "Hour"){
      timeScale.value = "Hour";
      select_time_value.value = "Hour";
      time_value.value = "Hour";
    }
    if(time_value.value === "年"){
      time_value.value = "Year";
    } else if(time_value.value === "月"){
      time_value.value = "Month";
    } else if(time_value.value === "小时"){
      time_value.value = "Hour";
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
    timeScale_line.value = `mo_${Year_Month.value}`;
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


// timeResult的值 右面日历变化
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
  timeScale.value = value;
  if (value === null) {
    timeSpan.value = [];
  } else {
    timeSpan.value = [...value];
    timeSpan.value = timeSpan.value.map((item) =>
      item.replace(/(\d{4})(\d{2})/, "$1$2")
    );
  }
  timeSpan_month.value = timeSpan.value;
  console.log("timeSpan.value:", timeSpan.value);
};


const getFormatTime = (date) => {
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
  
  // 传递给TableBox组件和Map2D的参数
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


  // level参数始终使用中文，后端可能不支持英文level值
  const tableParams = {
    map: selectedMap.value,
    timescale: tableParams_timescale.value,
    year: tableParams_year.value,
    level: selectedLevel.value, // 保持原始的中文level值
    language: locale.value,
  };
  
  bus.emit("boundaryParams", boundaryParams);
  bus.emit("tableParams", tableParams);
  
  // 没有选择则返回
  if (selectedMap.value === "" || timeScale_line.value === "") return;


  const params = {
    map: selectedMap.value,
    timescale: timeScale_line.value,
    timespan: timeSpan.value === null ? [] : timeSpan.value,
    level: selectedLevel.value,
    places: selectedPlaces.value,
    language: locale.value,
  };
  
  console.log("params:", params);
  
  try {
    const res = await getLinedata(params);
    console.log("getLinedata返回:", res);
    resData.value = [res];
    // 处理图表数据
    await getChartData();
    updateChart();
  } catch (error) {
    console.error("获取数据失败:", error);
    ElMessage.error(t("message.dataFetchError"));
  }
};


// 清除所选项
const handleClearSelected = (data) => {
  resData1.value = data;
};


// 时间选择器的时间范围
function disabledDate(time) {
  return time.getTime() > Date.now();
}


// 选择小时 -- 必须确定年份
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


// 接收DataService文件传的地图数据
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
  },
  tifMapPath: {
    type: String,
    default: ''
  }
});


const dataUnitMap = {
  "GWh/km<sup>2</sup>": "GWh/km²",
  "MW/km<sup>2</sup>": "MW/km²",
};


const selectedValues = ref("");
const is_download_Disabled = ref(true);


// 监听 props.selectMapPath 变化 左侧
watch(
  () => props.selectMapPath,
  async (newPath) => {
    try {
      console.log("props.selectMapPath:", props.selectMapPath);
      
      if (newPath) {
        if(newPath.includes('潜力数据') && !newPath.startsWith('用户侧') && !newPath.startsWith('电网侧')){
          is_download_Disabled.value = false;
        } else {
          is_download_Disabled.value = true;
        }
        
        // 更新选中值
        console.log("newPath:", newPath);
        selectedMap.value = newPath;
        selectedValues.value = newPath.split("/").slice(0, -1).join("/");
        
        if(selectedMap.value.split("/").pop() === "年" || 
           selectedMap.value.split("/").pop() === "月" || 
           selectedMap.value.split("/").pop() === "小时") {
          selectedMap.value = newPath.split("/").slice(0, -1).join("/");
        }
        
        map_time_value.value = newPath.split("/").pop();
        // 横坐标切换语言
        time_value.value = newPath.split("/").pop();
        if (locale.value === "english") {
          if(time_value.value === "年") {
            time_value.value = "Year";
          } else if(time_value.value === "月") {
            time_value.value = "Month";
          } else if(time_value.value === "小时") {
            time_value.value = "Hour";
          }
        } 
        
        // 重置时间选择器状态
        select_time_value.value = "";
        showTimePicker.value = 0;
        timeSelectWidth.value = '160px';
        selectedAreaValue.value = [];
        selectedPlaces.value = [];
        
        // 根据地图路径设置默认时间尺度
        // 检查路径是否包含4位数字年份（如2021）
        const specificYearMatch = newPath.match(/\b(20\d{2})\b/);
        const yearMatch = specificYearMatch || newPath.includes("年");
        const monthMatch = newPath.includes("月");
        const hourMatch = newPath.includes("小时");
        
        if(specificYearMatch && !monthMatch && !hourMatch) {
          // 路径包含具体年份，直接设置为该年份，不显示时间选择器
          const specificYear = specificYearMatch[1];
          select_time_value.value = "";
          time_value.value = specificYear;
          timeScale.value = specificYear;
          timeScale_line.value = "yr";
          showTimePicker.value = 0; // 不显示时间选择器
          timeSelectWidth.value = '160px';
          selectedYear.value = [specificYear];
          timeSpan_year.value = [specificYear];
          timeSpan.value = [specificYear];
        } else if(yearMatch && !monthMatch && !hourMatch) {
          // 路径只包含"年"字符，显示年份选择器
          select_time_value.value = locale.value === "chinese" ? "年" : "Year";
          time_value.value = locale.value === "chinese" ? "年" : "Year";
          timeScale.value = locale.value === "chinese" ? "年" : "Year";
          timeScale_line.value = "yr";
          showTimePicker.value = 1;
          timeSelectWidth.value = '80px';
        } else if(monthMatch) {
          select_time_value.value = locale.value === "chinese" ? "月" : "Month";
          time_value.value = locale.value === "chinese" ? "月" : "Month";
          timeScale.value = locale.value === "chinese" ? "月" : "Month";
          timeScale_line.value = `mo_${props.hourYear || new Date().getFullYear()}`;
          showTimePicker.value = 2;
          timeSelectWidth.value = '80px';
          yearValue.value = props.hourYear || new Date().getFullYear().toString();
          Year_Month.value = props.hourYear || new Date().getFullYear().toString();
        } else if(hourMatch) {
          select_time_value.value = locale.value === "chinese" ? "小时" : "Hour";
          time_value.value = locale.value === "chinese" ? "小时" : "Hour";
          timeScale.value = locale.value === "chinese" ? "小时" : "Hour";
          timeScale_line.value = `hr_${props.hourYear || new Date().getFullYear()}`;
          showTimePicker.value = 3;
          timeSelectWidth.value = '80px';
          yearValue.value = props.hourYear || new Date().getFullYear().toString();
          Year_Hour.value = props.hourYear || new Date().getFullYear().toString();
        }


        let tmpTime = "";
        let tmpMap = "";
        let tmpTimespan = [];
        
        if(specificYearMatch && !monthMatch && !hourMatch) {
          // 路径包含具体年份，只请求该年份的数据
          const specificYear = specificYearMatch[1];
          tmpTime = "yr";
          tmpMap = selectedMap.value;
          tmpTimespan = [specificYear]; // 只包含该具体年份
        } else if(yearMatch && !monthMatch && !hourMatch) {
          tmpTime = "yr";
          tmpMap = selectedMap.value;
          tmpTimespan = []; // 获取所有年份数据
        } else if(monthMatch) {
          tmpTime = `mo_${props.hourYear}`;
          tmpMap = selectedMap.value;
          tmpTimespan = [];
        } else if(hourMatch) {
          tmpTime = `hr_${props.hourYear}`;
          tmpMap = selectedMap.value;
          tmpTimespan = [];
        } else {
          // 默认情况，如果路径包含年份数字但没有明确的时间单位
          tmpTime = "yr";
          tmpMap = selectedMap.value;
          tmpTimespan = [];
        }


        const params = {
          map: tmpMap,
          timescale: tmpTime,
          timespan: tmpTimespan,
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
          language: locale.value,
        };


        const region_params = {
          // map: tmpMap.split('/').slice(0, -1).join('/'),
          map: getPureMapPath(tmpMap),
        };
        
        try {
          const region_res = await getRegions(region_params);
          if(region_res) {
            console.log("region_res.regions:", region_res);
            options.value = dynamic_region(region_res.regionsZn);
            console.log("options:", options);
          }


          const res = await getLinedata(params);
          console.log("getLinedata返回:", res);
          
          // 根据返回的数据设置时间选择器的默认值
          if (res && res.xAxisData && res.xAxisData.length > 0) {
            if(specificYearMatch && !monthMatch && !hourMatch) {
              // 路径包含具体年份，只设置该年份，确保图表只显示该年份数据
              const specificYear = specificYearMatch[1];
              selectedYear.value = [specificYear];
              timeSpan_year.value = [specificYear];
              timeSpan.value = [specificYear];
              
              // 过滤返回的数据，只保留该具体年份的数据
              if (res.xAxisData.includes(specificYear)) {
                const yearIndex = res.xAxisData.indexOf(specificYear);
                res.xAxisData = [specificYear];
                // 同时过滤SeriesData中对应的数据
                if (res.SeriesData && res.SeriesData.length > 0) {
                  res.SeriesData.forEach(series => {
                    if (series.data && series.data.length > yearIndex) {
                      series.data = [series.data[yearIndex]];
                    }
                  });
                }
              }
            } else if(yearMatch && !monthMatch && !hourMatch) {
              // 路径只包含"年"字符，设置所有返回的年份数据
              selectedYear.value = res.xAxisData;
              timeSpan_year.value = res.xAxisData;
              timeSpan.value = res.xAxisData;
            } else if(monthMatch) {
              selectedMonth.value = res.xAxisData;
              timeSpan_month.value = res.xAxisData;
              timeSpan.value = res.xAxisData;
            } else if(hourMatch) {
              selectedHour.value = res.xAxisData;
              timeSpan_hour.value = res.xAxisData;
              timeSpan.value = res.xAxisData;
            } else {
              // 默认情况，当路径包含年份数字时，设置为年份数据
              selectedYear.value = res.xAxisData;
              timeSpan_year.value = res.xAxisData;
              timeSpan.value = res.xAxisData;
            }
          }
          
          const description_params = {
            map: tmpMap.split('/').slice(0, -1).join('/'),
            language: locale.value,
          };
          console.log("description_params:", description_params);
          
          const description_res = await getDescription(description_params);
          if(description_res) {
            resData1.value = description_res;
          } else {
            resData1.value = null;
          }
          console.log("description_res:", description_res);
          resData.value = [res];
          
          // 设置默认选中的区域（区域级别）
          selectedLevel.value = "区域";
          selectedPlaces.value = [
            "东亚与太平洋地区",
            "中东与北非地区",
            "北美地区",
            "南亚地区",
            "拉丁美洲与加勒比地区",
            "撒哈拉以南非洲地区",
            "欧洲与中亚地区",
          ];
          
          await getChartData();
          updateChart();
        } catch (regionError) {
          console.error("获取区域数据失败:", regionError);
          await getChartData();
          updateChart();
        }
      } else {
        resData1.value = null;
        // 路径为空时清空数据
        selectedValues.value = [];
        resData.value = [];
        selectedMap.value = "";
        await getChartData();
        updateChart();
      }
    } catch (error) {
      console.error("数据处理失败:", error);
      await getChartData();
      updateChart();
    }
  }
);


function getPureMapPath(mapPath) {
  // 去掉最后的 /年、/月、/小时 或 /4位数字（年份）
  return mapPath.replace(/(\/年|\/月|\/小时|\/[0-9]{4})$/, '');
}


// 递归查找area在options中的正确路径，根据返回的level确定层级
function getValidPath(area, options, targetLevel) {
  console.log('getValidPath called with area:', area, 'targetLevel:', targetLevel);
  
  // 如果area是字符串，直接使用；如果是对象，根据targetLevel获取对应值
  let targetValue = '';
  if (typeof area === 'string') {
    targetValue = area;
  } else if (typeof area === 'object' && area !== null) {
    if (targetLevel === '区域' && area['区域']) {
      targetValue = area['区域'];
    } else if (targetLevel === '次区域' && area['次区域']) {
      targetValue = area['次区域'];
    } else if (targetLevel === '国家' && area['国家']) {
      targetValue = area['国家'];
    } else if (targetLevel === '省级' && area['省级']) {
      targetValue = area['省级'];
    } else if (targetLevel === '地级' && area['地级']) {
      targetValue = area['地级'];
    } else if (targetLevel === '县级' && area['县级']) {
      targetValue = area['县级'];
    }
  }
  
  console.log('Looking for targetValue:', targetValue, 'in level:', targetLevel);
  
  if (!targetValue) {
    console.log('No targetValue found, returning empty array');
    return [];
  }
  
  // 递归查找目标值的完整路径
  function findPath(opts, currentPath = []) {
    for (const opt of opts) {
      const newPath = [...currentPath, opt.value];
      
      // 如果找到目标值，返回路径
      if (opt.value === targetValue) {
        console.log('Found target path:', newPath);
        return newPath;
      }
      
      // 如果有children，递归查找
      if (opt.children && opt.children.length > 0) {
        const childPath = findPath(opt.children, newPath);
        if (childPath) return childPath;
      }
    }
    return null;
  }
  
  const path = findPath(options);
  
  // 如果没有找到路径，可能是因为需要包含父级路径
  // 对于"东亚"这种次区域，需要包含完整的层级路径
  if (!path) {
    // 特殊处理，需要找到包含该目标值的完整路径
    function findCompletePathForTarget(opts, currentPath = []) {
      for (const opt of opts) {
        const newPath = [...currentPath, opt.value];
        
        // 如果当前节点就是目标值，返回路径
        if (opt.value === targetValue) {
          console.log('Found complete path:', newPath);
          return newPath;
        }
        
        if (opt.children && opt.children.length > 0) {
          // 检查直接子节点中是否有目标值
          for (const child of opt.children) {
            if (child.value === targetValue) {
              const fullPath = [...newPath, child.value];
              console.log('Found target in children:', fullPath);
              return fullPath;
            }
          }
          
          // 递归查找更深层的子节点
          const childPath = findCompletePathForTarget(opt.children, newPath);
          if (childPath) return childPath;
        }
      }
      return null;
    }
    
    const completePath = findCompletePathForTarget(options);
    const result = completePath ? [completePath] : [];
    console.log('getValidPath result (complete path):', result);
    return result;
  }
  
  const result = path ? [path] : [];
  console.log('getValidPath result:', result);
  return result;
}


bus.on('mapPointSelected', async ({ level, places, map, language, area }) => {
  console.log('当前 options 结构:', options.value);
  console.log('接收到的 area 对象:', area);
  console.log('接收到的 level:', level);
  console.log('接收到的 places:', places);
  
  // 自动高亮 - 使用places数组中的第一个值作为目标值
  const targetPlace = places && places.length > 0 ? places[0] : '';
  const validPath = getValidPath(targetPlace, options.value, level);
  selectedAreaValue.value = validPath;
  console.log('[ChartBox] 自动高亮 selectedAreaValue:', validPath);
  
  // 请求统计图接口
  const pureMap = getPureMapPath(map);
  
  // 根据地图路径确定时间尺度
  let timescale = 'yr';
  let timespan = [];
  
  // 检查路径是否包含具体年份
  const specificYearMatch = map.match(/\b(20\d{2})\b/);
  
  if (specificYearMatch && !map.includes('月') && !map.includes('小时')) {
    // 路径包含具体年份，只请求该年份的数据
    timescale = 'yr';
    timespan = [specificYearMatch[1]];
  } else if (map.includes('年')) {
    timescale = 'yr';
    timespan = [];
  } else if (map.includes('月')) {
    timescale = `mo_${props.hourYear || new Date().getFullYear()}`;
    timespan = [];
  } else if (map.includes('小时')) {
    timescale = `hr_${props.hourYear || new Date().getFullYear()}`;
    timespan = [];
  }
  
  const params = {
    map: pureMap,
    timescale: timescale,
    timespan: timespan,
    level,
    places,
    language
  };
  console.log('[ChartBox] 请求 /statistical/getlinedata 参数:', params);
  
  try {
    const res = await getLinedata(params);
    console.log('getLinedata返回:', res);
    
    if (res && res.SeriesData && res.xAxisData) {
      // 更新组件数据
      resData.value = [res];
      
      // 自动设置时间尺度选择器
      if (res.xAxisData && res.xAxisData.length > 0) {
        const firstTimeData = res.xAxisData[0];
        // 根据返回的时间数据格式判断时间尺度
        const isChineseMode = language === "chinese" || locale.value === "chinese";
        
        // 检查地图路径是否包含具体年份
        const specificYearMatch = map.match(/\b(20\d{2})\b/);
        
        if (firstTimeData.length === 4) { // 年份格式如"2021"
          // 如果路径包含具体年份，只显示该年份
          if (specificYearMatch && !map.includes('月') && !map.includes('小时')) {
            const specificYear = specificYearMatch[1];
            select_time_value.value = "";
            time_value.value = specificYear; // 直接显示具体年份，如"2021"
            timeScale.value = specificYear;
            timeScale_line.value = "yr";
            showTimePicker.value = 0; // 不显示时间选择器，直接显示年份
            selectedYear.value = [specificYear];
            timeSpan_year.value = [specificYear];
            timeSpan.value = [specificYear];
            timeSelectWidth.value = '160px';
          } else {
            // 多个年份数据，显示年份选择器
            select_time_value.value = isChineseMode ? "年" : "Year";
            time_value.value = isChineseMode ? "年" : "Year";
            timeScale.value = isChineseMode ? "年" : "Year";
            timeScale_line.value = "yr";
            showTimePicker.value = 1;
            selectedYear.value = res.xAxisData;
            timeSpan_year.value = res.xAxisData;
            timeSpan.value = res.xAxisData;
            timeSelectWidth.value = '80px';
          }
        } else if (firstTimeData.length === 6) { // 月份格式如"202101"
          select_time_value.value = isChineseMode ? "月" : "Month";
          time_value.value = isChineseMode ? "月" : "Month";
          timeScale.value = isChineseMode ? "月" : "Month";
          timeScale_line.value = `mo_${props.hourYear || new Date().getFullYear()}`;
          showTimePicker.value = 2;
          selectedMonth.value = res.xAxisData;
          timeSpan_month.value = res.xAxisData;
          timeSpan.value = res.xAxisData;
          yearValue.value = props.hourYear || new Date().getFullYear().toString();
          Year_Month.value = props.hourYear || new Date().getFullYear().toString();
          timeSelectWidth.value = '80px';
        } else if (firstTimeData.length >= 10) { // 小时格式如"2021-01-01 00:00:00"
          select_time_value.value = isChineseMode ? "小时" : "Hour";
          time_value.value = isChineseMode ? "小时" : "Hour";
          timeScale.value = isChineseMode ? "小时" : "Hour";
          timeScale_line.value = `hr_${props.hourYear || new Date().getFullYear()}`;
          showTimePicker.value = 3;
          selectedHour.value = res.xAxisData;
          timeSpan_hour.value = res.xAxisData;
          timeSpan.value = res.xAxisData;
          yearValue.value = props.hourYear || new Date().getFullYear().toString();
          Year_Hour.value = props.hourYear || new Date().getFullYear().toString();
          timeSelectWidth.value = '80px';
        }
      }
      
      // 设置选中的区域和层级
      selectedPlaces.value = places;
      selectedLevel.value = level;
      selectedMap.value = pureMap;
      
      // 更新lastValidSelection以便手动选择时的验证逻辑能正常工作
      lastValidSelection.value = [...validPath];
      
      // 更新图表数据并渲染
      await getChartData();
      updateChart();
    }
  } catch (e) {
    console.error('获取折线图数据失败', e);
    ElMessage.error(t("message.dataFetchError"));
  }
  
  // 表格
  const isChineseMode = language === "chinese" || locale.value === "chinese";
  
  // 根据实际的时间尺度设置表格参数，但timeScale始终使用中文
  let tableTimeScale = "年"; // 默认使用中文
  let tableYear = '';
  
  if (timescale.startsWith('mo_')) {
    tableTimeScale = "月"; // 始终使用中文
    tableYear = timescale.split('_')[1] || new Date().getFullYear().toString();
  } else if (timescale.startsWith('hr_')) {
    tableTimeScale = "小时"; // 始终使用中文
    tableYear = timescale.split('_')[1] || new Date().getFullYear().toString();
  }
  
  // level和timeScale参数始终使用中文，后端可能不支持英文值
  const tableParams = {
    map: pureMap,
    timescale: tableTimeScale,
    year: tableYear,
    level: level, // 保持原始的中文level值
    language
  };
  console.log('[ChartBox] 发送 tableParams:', tableParams);
  bus.emit('tableParams', tableParams);
});


function renderECharts(data) {
  if (!window.myChart) {
    const echarts = require('echarts');
    window.myChart = echarts.init(document.querySelector('.chart'));
  }
  const option = {
    xAxis: { type: 'category', data: data.xAxisData },
    yAxis: { type: 'value' },
    series: data.SeriesData.map(item => ({
      name: item.name[0],
      data: item.data,
      type: 'line'
    }))
  };
  window.myChart.setOption(option);
}


// 根据路径区域动态显示
function dynamic_region(regions) {
  const arr = regions.split('/'); 
  const length = arr.length;
  if (locale.value === "chinese") {
    if(arr[0] === "电网") {
      console.log("original_options2");
      const newOption = structuredClone(original_options2);
      return filterNodesByLevel(newOption, length);
    } else {
      console.log("original_options");
      const newOption = structuredClone(original_options);
      return filterNodesByLevel(newOption, length);
    }
  } else {
    if(arr[0] === "电网") {
      console.log("original_options2_en");
      const newOption = structuredClone(original_options2_en);
      return filterNodesByLevel(newOption, length);
    } else {
      console.log("original_options_en");
      const newOption = structuredClone(original_options_en);
      return filterNodesByLevel(newOption, length);
    }
  }
}


function filterNodesByLevel(nodes, level) {
  return nodes.reduce((filtered, node) => {
    // 1. 过滤 > level 的节点
    if(node.level === level) {
      node.leaf = true;
    }
    if (node.level > level) {
      return filtered; // 跳过该节点
    }
    // 2. 深拷贝节点（避免修改原数据，可选但推荐）
    const newNode = { ...node };
    // 3. 递归处理子节点（若存在 children）
    if (newNode.children && Array.isArray(newNode.children)) {
      newNode.children = filterNodesByLevel(newNode.children, level);
    }
    // 4. 将处理后的节点加入结果数组
    filtered.push(newNode);
    return filtered;
  }, []);
}


let is_show = ref(false);


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


// 中英文层级映射
const LEVEL_TRANSLATION = {
  "区域": { chinese: "区域", english: "Region" },
  "次区域": { chinese: "次区域", english: "Subregion" },
  "国家": { chinese: "国家", english: "Country" },
  "省级": { chinese: "省级", english: "Province" },
  "地级": { chinese: "地级", english: "Prefecture" },
  "县级": { chinese: "县级", english: "County" },
};


// 根据语言转换level值
function translateLevel(level, targetLanguage) {
  const translation = LEVEL_TRANSLATION[level];
  if (!translation) return level;
  
  return targetLanguage === "chinese" ? translation.chinese : translation.english;
}


const selectedAreaValue = ref([]);
let lastValidSelection = ref([]);


const handlerChange = async (values) => {
  // 获取所有路径的长度
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
  // 确定图表类型
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
            type: chartType.value,
            smooth: true
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


// 初始化echarts
const initChart = async () => {
  console.log("initChart被调用");
  if (chartRef.value) {
    mychart = echarts.init(chartRef.value);
    window.addEventListener("resize", () => {
      mychart.resize();
    });
    console.log("chartRef.value:", chartRef.value);
    console.log("初始化图成功");
  }
};


const updateChart = () => {
  if (!mychart) {
    initChart();
    return;
  }
  
  mychart.clear();
  // 准备x轴数据（假设所有系列共享相同的x轴）
  const xAxisData = chartData.value.xAxis;
  console.log("chartData:", chartData.value);
  
  // 确定图表类型
  if (xAxisData.length === 1) {
    chartType.value = "bar";
  } else {
    chartType.value = "line";
  }
  console.log("props", props);


  // 转换series数据
  const series = chartData.value.SeriesData.map((dataItem, index) => ({
    name: dataItem.name, // 可以根据你的数据结构调整名称
    type: chartType.value,
    data: dataItem.data,
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
      name: time_value.value || t("chart.year"), // 添加横轴名称，默认为"年"
      nameLocation: "middle", // 名称位置在轴的末端
      nameTextStyle: {
        color: "#333",
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


// 处理节点点击事件
const handleNodeClick = (node, data) => {
  console.log("节点被点击:", node, data);
  // 可以根据需要添加节点点击逻辑
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
    justify-content: space-around;
    width: 100%;
    height: 50px;
    margin: 10px 1px 10px;
    // 设置时间选择器的背景色
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
    height: 308px;
    width: 504px;
    margin-top: 10px;


    .chart {
      height: 308px;
      width: 452px;
      margin: 0px auto;
      border: 1px solid rgb(85, 82, 82, 0.2);
    }
  }
}


.specific-time-display {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  padding: 0 12px;
  background-color: rgba(255, 255, 255, 0.4);
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  color: #333;
  font-size: 14px;
  min-width: 80px;
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
  width: 20px;
  height: 20px;
  background-color: yellow; /* 设置高亮背景颜色，可按需修改 */
}
.arrow {
  color: red;
  transition: color 0.3s ease;
}
</style>