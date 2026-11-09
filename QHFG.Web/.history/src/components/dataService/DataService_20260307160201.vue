<template>
  <div class="dataService_page">
    <div class="map_box">
      <ConditionBox
        v-show="showConditionFlag"
        @updateEmitResultList="updateEmitResultList"
      />
      <div class="show_condition_box" @click="showCondition">
        <i class="iconfont icon-shouqi"></i>
      </div>
      <div class="result_box gray-text" v-show="showResultFlag">
        <div class="result_box_top">
          <div class="result_box_top_left gray-text">
            <i class="iconfont icon-data-screen"></i>
            {{ $t("message.result") }}
          </div>
          <div class="result_box_top_right">
            <el-button type="primary" @click="clearResult">
              {{ $t("message.clear") }}</el-button
            >
          </div>
        </div>
        <div class="result_content">
          <div class="result_content_title">
            <div
              v-for="(result, index) in filterResultList"
              :key="index"
              class="checkbox-wrapper"
            >
              <el-checkbox
                v-model="selectedResults[index]"
                class="wrap-checkbox"
                @change="(val) => handleResultSelect(val, index)"
              >
                <span v-html="formatResult(result)"></span>
              </el-checkbox>
              <div
                class="result_content_detail"
                v-show="selectedResults[index]"
              >
                <div class="result_img">
                  <img v-if="imageUrl" :src="imageUrl" alt="图例" />
                  <p v-else class="error-text">{{ $t("result.noLegend") }}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="show_result_box" @click="showResult">
        <i class="iconfont icon-shouqi"></i>
      </div>
      <ChartBox
        v-show="showChartFlag"
        :selectMapPath="selectMapPath"
        :dataUnit="dataUnit"
        :dataTypeThree="dataTypeThree"
        :hourYear="hourYear"
        :tifMapPath="tifMapPath"
      />
      <div class="show_chart_box" @click="showChart">
        <i class="iconfont icon-shouqi"></i>
      </div>
      <div class="show_two_three" @click="changeMap">
        <i class="iconfont icon-diqiu"></i>
      </div>
      <Map2D
        v-show="dimensionFlag == 2"
        :qgsMapPath="qgsMapPath"
        :tifMapPath="tifMapPath"
        :dataUnit="dataUnit"
        :dataTypeThree="dataTypeThree"
        :selectMapPath="selectMapPath"
      >
      </Map2D>
      <div class="mapInfo">
        <span>{{ $t("message.lon") }}:{{ Longitude }}°</span>
        <span>{{ $t("message.lat") }}:{{ Latitude }}°</span>
        <!-- <span class="label" v-html="dataUnit"></span> -->
        <span class="map-license-inline">
          <!-- 注释掉跳转链接功能，保留文本信息 -->
          <!-- <a
            href="https://cloudcenter.tianditu.gov.cn/administrativeDivision/"
            target="_blank"
            rel="noopener noreferrer"
          > -->
          审图号：GS（2024）0650号
          <!-- </a> -->
        </span>
      </div>
      <Map3D
        v-show="dimensionFlag == 3"
        :qgsMapPath="qgsMapPath"
        :tifMapPath="tifMapPath"
        :selectMapPath="selectMapPath"
        :dataUnit="dataUnit"
        :dataTypeThree="dataTypeThree"
      >
      </Map3D>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from "vue";
import { ElMessage } from "element-plus";
import Map2D from "./Map2D.vue";
import Map3D from "./Map3D.vue";
import ConditionBox from "./condition/ConditionBox.vue";
import ChartBox from "./chart/ChartBox.vue";
import * as echarts from "echarts";
import { getLegendGraphic, getlayerListResourceData } from "@/request";
import { Delete, Edit, Search, CirclePlus } from "@element-plus/icons-vue";
import bus from "@/utils/bus";
import { useI18n } from "vue-i18n";
const { t, locale } = useI18n();
import { useRouter } from "vue-router";

const router = useRouter();

watch(locale, () => {
  // clearResult(); // 先清空
  restoreLocalData(); // 再恢复对应语言的数据
  // 保存用户选择的语言到localStorage
  localStorage.setItem("user-locale", locale.value);
});
//切换二维三维地图
const dimensionFlag = ref(2);
const changeMap = () => {
  if (dimensionFlag.value == 2) {
    dimensionFlag.value = 3;
    // 切换到3D地图时，延迟通知Map3D组件开始旋转
    setTimeout(() => {
      bus.emit("map3DVisible");
    }, 100);
  } else if (dimensionFlag.value == 3) {
    dimensionFlag.value = 2;
  }
};

//控制条件框的显示隐藏
const showConditionFlag = ref(1);
const showCondition = () => {
  showConditionFlag.value = showConditionFlag.value ? 0 : 1;
};

//控制结果框的显示隐藏
const showResultFlag = ref(1);
const showResult = () => {
  showResultFlag.value = showResultFlag.value ? 0 : 1;
};

//控制图表的显示隐藏
const showChartFlag = ref(1);
const showChart = () => {
  showChartFlag.value = showChartFlag.value ? 0 : 1;
};

//结果框相关的状态
//结果框相关的状态
const filterResultList = ref([]);        // 结果名称列表
const selectedResults = ref([]);         // 选中状态列表
const imageUrl = ref("");                // 图例图片URL
const timeResult = ref([]);              // 时间列表
const mapPathUrlList = ref([]);          // 路径列表
const layerUrl = ref("");                // 图层URL
const dataUnit = ref("");                // 数据单位
const dataUnlitList = ref([]);           // 单位列表
const dataTypeThree = ref("");          // 数据类型
const dataTypeThreeList = ref([]);      // 数据类型列表

//用于接收子组件传递的数据
const emitResultList = ref([]);


const updateEmitResultList = (data) => {
  emitResultList.value = data;
  // 将emitResultList.value中的数据加进去，给filterResultList.value即保存所有已选路径的列表
  for (let i = 0; i < emitResultList.value[0].length; i++) {
    if (!filterResultList.value.includes(emitResultList.value[0][i])) {
      filterResultList.value.push(emitResultList.value[0][i]);
      selectedResults.value.push(emitResultList.value[1][i]);
      timeResult.value.push(emitResultList.value[2][i]);
      mapPathUrlList.value.push(emitResultList.value[3][i]);
      dataUnlitList.value.push(emitResultList.value[4][i]);
      dataTypeThreeList.value.push(emitResultList.value[5][i]);
    } else {
      ElMessage.error(t("result.dataExists"));
    }
  }
  // --- 新增：按语言存储到 localStorage ---
  const lang = locale.value === "chinese" ? "zh" : "en";
  const saveObj = {
    filterResultList: filterResultList.value,
    selectedResults: selectedResults.value,
    timeResult: timeResult.value,
    mapPathUrlList: mapPathUrlList.value,
    dataUnlitList: dataUnlitList.value,
    dataTypeThreeList: dataTypeThreeList.value,
  };
  localStorage.setItem(`dataServiceResults_${lang}`, JSON.stringify(saveObj));
};

const qgsMapPath = ref("");
const tifMapPath = ref("");
const selectMapPath = ref("");
const hourYear = ref("");
//显示识别点值
const showIdentify = ref(false);
//处理结果选择
const handleResultSelect = (checked, index) => {
  if (checked) {
    // 清空其他选择
    selectedResults.value = selectedResults.value.map(() => false);
    selectedResults.value[index] = true;

    if (timeResult.value[0] === t("conditionBox.annualAverage").slice(0, 1)) {
      layerUrl.value = t("conditionBox.annualAverage");
    } else {
      layerUrl.value = timeResult.value[index];
    }
    //向地图传递数据单位
    dataUnit.value = dataUnlitList.value[index];
    //向地图传递数据类型
    dataTypeThree.value = dataTypeThreeList.value[index];
    // 获取图例
    let tempTime = "";
    if (layerUrl.value.includes("-")) {
      if (layerUrl.value.length > 7) {
        tempTime = layerUrl.value.split(":")[0].replace(/[-\s]/g, "");
        hourYear.value = tempTime.slice(0, 4);
        // bus.emit("hourYear",tempTime.slice(0,4));
      } else {
        tempTime = layerUrl.value.replace(/-/g, "");
        hourYear.value = tempTime.slice(0, 4);
      }
      qgsMapPath.value = `/io/data/${
        mapPathUrlList.value[index]
      }/${layerUrl.value.substring(0, 4)}/${tempTime}.qgs`;
      tifMapPath.value = `${tempTime}.tif`;
    } else {
      qgsMapPath.value = `/io/data/${mapPathUrlList.value[index]}/${layerUrl.value}.qgs`;
      tifMapPath.value = `${layerUrl.value}.tif`;
    }

    imageUrl.value = "";
    selectMapPath.value = mapPathUrlList.value[index];
    getLegend(qgsMapPath.value, tifMapPath.value);
    showIdentify.value = false;
    //表格初始化的参数
    const excelDataParams = {
      map: "",
      timeScale: "年",
      year: "",
      level: "区域",
      language: locale.value,
    };
    const lastSlashIndex = selectMapPath.value.lastIndexOf("/");
    excelDataParams.map = selectMapPath.value.slice(0, lastSlashIndex);
    if (
      selectMapPath.value.split("/").pop() === "年" ||
      selectMapPath.value.split("/").pop() === "月" ||
      selectMapPath.value.split("/").pop() === "小时"
    ) {
      excelDataParams.timeScale = selectMapPath.value.split("/").pop();
      if (excelDataParams.timeScale !== "年") {
        excelDataParams.year = tifMapPath.value.substring(0, 4);
      }
    }

    bus.emit("excelDataParams", excelDataParams);
  } else {
    selectedResults.value[index] = false;
    qgsMapPath.value = "";
    tifMapPath.value = "";
    imageUrl.value = "";
    selectMapPath.value = "";
    dataTypeThree.value = "";
    const tableParams = [];
    bus.emit("clearSelected", tableParams);
  }
};

//清除结果列表
const clearResult = () => {
  filterResultList.value = [];
  selectedResults.value = [];
  imageUrl.value = "";
  qgsMapPath.value = "";
  tifMapPath.value = "";
  mapPathUrlList.value = [];
  timeResult.value = [];
  selectMapPath.value = "";
  dataUnlitList.value = [];
  dataUnit.value = "";
  dataTypeThree.value = "";
  dataTypeThreeList.value = [];
  localStorage.removeItem("dataServiceResults_en");
  localStorage.removeItem("dataServiceResults_zh");
  bus.emit("clearSelected", []); // 通知表格清空
};

// 监听2D坐标变化
bus.on("clickedCoordinate2D", (data) => {
  Latitude.value = data.latitude;
  Longitude.value = data.longitude;
});

bus.on("clickedCoordinate3D", (data) => {
  Latitude.value = data.latitude;
  Longitude.value = data.longitude;
});

const Latitude = ref(null);
const Longitude = ref(null);

//获取图例
const getLegend = async (mapPath, layer) => {
  const MAP = mapPath;
  const LAYER = layer;
  try {
    const base64Data = await getLegendGraphic(MAP, LAYER, locale.value);
    if (base64Data) {
      imageUrl.value = `data:image/png;base64,${base64Data}`;
    }
  } catch (error) {
    ElMessage.error(t("result.legendError"));
    console.error("生成图例URL失败:", error);
  }
};

// 页面初始化时调用的函数
const initializePage = async () => {
  // 保证有默认地图路径和图层
  if (!qgsMapPath.value) {
    qgsMapPath.value =
      "/io/data/发电侧/风力发电/低空风电/陆上风电/潜力数据/2.5MW/容量因子/10km/年/2021.qgs";
  }
  if (!tifMapPath.value) {
    tifMapPath.value = "2021.tif";
  }
  if (!selectMapPath.value) {
    selectMapPath.value =
      "发电侧/风力发电/低空风电/陆上风电/潜力数据/2.5MW/容量因子/10km";
  }
};

// 格式化结果的函数
const formatResult = (result) => {
  if (!result) return "";

  // 找到最后一个左括号的位置
  const lastBracketIndex = result.lastIndexOf("(");
  if (lastBracketIndex === -1) return result;

  // 分割字符串
  const mainText = result.substring(0, lastBracketIndex);
  const params = result.substring(lastBracketIndex);

  // 返回带换行符的HTML
  return `${mainText}<br>${params}`;
};

// 恢复本地存储数据的函数
function restoreLocalData() {
  const lang =
    locale.value === "chinese" || locale.value === "chinese" ? "zh" : "en";
  const saved = localStorage.getItem(`dataServiceResults_${lang}`);
  if (saved) {
    try {
      const obj = JSON.parse(saved);
      filterResultList.value = Array.isArray(obj.filterResultList)
        ? [...obj.filterResultList]
        : [];
      selectedResults.value = Array.isArray(obj.selectedResults)
        ? [...obj.selectedResults]
        : [];
      timeResult.value = Array.isArray(obj.timeResult)
        ? [...obj.timeResult]
        : [];
      mapPathUrlList.value = Array.isArray(obj.mapPathUrlList)
        ? [...obj.mapPathUrlList]
        : [];
      dataUnlitList.value = Array.isArray(obj.dataUnlitList)
        ? [...obj.dataUnlitList]
        : [];
      dataTypeThreeList.value = Array.isArray(obj.dataTypeThreeList)
        ? [...obj.dataTypeThreeList]
        : [];
      // 关键：无论selectedResults里有没有true，都要激活第一个
      let firstSelectedIndex = selectedResults.value.findIndex((v) => v);
      if (firstSelectedIndex === -1 && selectedResults.value.length > 0) {
        firstSelectedIndex = 0;
        selectedResults.value = selectedResults.value.map((_, i) => i === 0);
      }
      if (firstSelectedIndex !== -1) {
        handleResultSelect(true, firstSelectedIndex);
      }
    } catch (e) {
      console.error("恢复 localStorage 数据失败:", e);
    }
  } else {
    // 没有本地数据时，清空
    filterResultList.value = [];
    selectedResults.value = [];
    timeResult.value = [];
    mapPathUrlList.value = [];
    dataUnlitList.value = [];
    dataTypeThreeList.value = [];
    // 新增：没有本地数据时赋默认地图路径和图层
    qgsMapPath.value =
      "/io/data/发电侧/风力发电/低空风电/陆上风电/潜力数据/2.5MW/容量因子/10km/年/2021.qgs";
    tifMapPath.value = "2021.tif";
  }
}

// 页面初始化时调用函数
onMounted(async () => {
  // 添加Token验证逻辑
  try {
    // 调用一个需要认证的接口来验证Token是否有效
    await getlayerListResourceData(locale.value);
  } catch (error) {
    // 如果Token无效，清除本地存储并跳转到登录页面
    console.error("[DataService] Token验证失败:", error);
    localStorage.removeItem("leiyangUser");
    ElMessage.error(t("message.auth.loginRequired"));
    await router.push("/home/login");
    return; // 阻止继续执行初始化
  }

  restoreLocalData();
  await initializePage();
  bus.on("clearDataServiceResult", clearResult);
  bus.on("restoreDataServiceLocalData", restoreLocalData);
});

onUnmounted(() => {
  bus.off("clearDataServiceResult", clearResult);
  bus.off("restoreDataServiceLocalData", restoreLocalData);
});
</script>

<style lang="scss" scoped>
.gray-text {
  color: black;
  font-size: 14px;
}
:deep(.el-checkbox__inner),
:deep(
    .el-tree--highlight-current
      .el-tree-node.is-current
      > .el-tree-node__content
  ) {
  background-color: rgba(255, 255, 255, 0.4);
}
.dataService_page {
  position: relative;
  box-sizing: border-box;
  padding: 0px;
  margin: 0px;
  width: 100%;
  height: 100%;
  background: #fff;

  .map_box {
    position: relative;
    width: 100%;
    height: 100%;
    z-index: 10;

    .show_condition_box {
      position: fixed;
      top: 100px;
      left: 5px;
      z-index: 10;
      width: 25px;
      height: 25px;
      line-height: 25px;
      text-align: center;
      color: #fff;
      border-radius: 4px;
      background: #4ba0fd;
      cursor: pointer;
    }

    .result_box {
      z-index: 10;
      position: absolute;
      bottom: 5px;
      left: 30px;
      transition: width 0.3s ease;
      width: 360px;
      height: 35%;
      border-radius: 4px;
      background: rgba(255, 255, 255, 0.4);
      border: 1px solid rgba(0, 0, 0, 0.18);
      backdrop-filter: blur(3px);
      box-shadow: 0px 0px 0px 0px rgba(255, 255, 255, 0.8);

      .result_box_top {
        display: flex;
        width: 100%;
        height: 30px;
        justify-content: space-between;
        align-items: center;
        margin: 4px;

        .result_box_top_left {
          width: 70%;
        }

        .result_box_top_right {
          margin-right: 10px;
        }
      }

      .result_content {
        width: 100%;
        height: 85%;
        margin-left: 5px;
        margin-right: 5px;
        overflow-y: auto;
        overflow-x: hidden;

        .result_content_title {
          margin-top: 20px;
          max-width: 100%;
          height: 40px;
          .checkbox-wrapper {
            margin-bottom: 30px; /* 控制每个 checkbox 项之间的垂直间距 */
          }
          :deep(.wrap-checkbox .el-checkbox__label) {
            max-width: 100%;
            display: inline-block;
            white-space: normal !important;
            word-break: break-word;
            line-height: 1.6;
            color: black;
          }
        }

        .result_content_detail {
          width: 100%;
          .result_img {
            margin-left: 18px;
            width: 100%;
            overflow: hidden;

            .error-text {
              color: #999;
              text-align: center;
              line-height: 100px;
            }
          }

          .result_img img {
            width: 100px;
            height: auto;
          }
        }
      }
    }

    .show_result_box {
      width: 25px;
      height: 25px;
      line-height: 25px;
      text-align: center;
      color: #fff;
      border-radius: 4px;
      background: #4ba0fd;
      cursor: pointer;
      position: fixed;
      bottom: 230px;
      left: 5px;
      z-index: 10;
    }

    .show_chart_box {
      width: 25px;
      height: 25px;
      line-height: 25px;
      text-align: center;
      color: #fff;
      border-radius: 4px;
      background: #4ba0fd;
      cursor: pointer;
      position: fixed;
      top: 110px;
      right: 8px;
      z-index: 10;
    }

    .show_two_three {
      width: 25px;
      height: 25px;
      line-height: 25px;
      text-align: center;
      color: #fff;
      border-radius: 4px;
      background: #4ba0fd;
      cursor: pointer;
      position: fixed;
      top: 80px;
      right: 8px;
      z-index: 10;
    }

    .mapInfo {
      z-index: 10;
      position: fixed;
      right: 20px;
      bottom: 20px;
      display: flex;
      align-items: center;
      padding: 6px 12px;
      border-radius: 4px;
      color: #c8d8eb;
      font-size: 14px;
      font-family: "SoureHanSans";
      background-color: rgba(30, 49, 75, 0.8);
      max-width: calc(100% - 40px);
      min-width: 300px;
      height: auto;
      flex-wrap: wrap;
      gap: 15px;
      overflow: hidden;
      text-overflow: ellipsis;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    }

    .mapInfo span,
    .map-license-inline,
    .map-license-inline a {
      width: auto;
      min-width: 100px;
      text-align: left;
      color: #c8d8eb;
      font-size: 14px;
      font-family: "SoureHanSans";
      background: none;
      border-radius: 0;
      padding: 0;
      white-space: nowrap;
      vertical-align: middle;
      text-decoration: none;
    }

    .map-license-inline {
      flex: 1;
      min-width: 180px;
    }

    .map-license-inline a:hover {
      color: #409eff;
      text-decoration: underline;
    }
  }
}
</style>
