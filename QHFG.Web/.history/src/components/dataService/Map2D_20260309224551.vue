<template>
  <div class="map-container">
    <div class="mapView" id="mapView"></div>
    <!-- 经纬度弹窗 -->
    <div v-if="showPopover" class="coordinate-popover" :style="popoverStyle">
      <button class="close-btn" @click.stop="closePopover">×</button>
      <div class="popover-content">
        <div class="coordinates">
          <div class="coord-item">
            <span>{{ dataTypeThree }}</span>
            <span class="label">
              <span v-html="dataUnit ? `(${dataUnit})` : ''"></span>:
            </span>
            <span class="value">
              <span v-if="isLoading" class="loading-text">{{
                $t("result.loading")
              }}</span>
              <span v-else>{{ identifyNumResult }}</span>
            </span>
          </div>
        </div>
      </div>
      <div class="popover-arrow"></div>
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  onMounted,
  onBeforeUnmount,
  onUnmounted,
  watchEffect,
  reactive,
  watch,
} from "vue";
import { Tile as TileLayer, Vector as VectorLayer } from "ol/layer";
import Map from "ol/Map.js";
import View from "ol/View.js";
import { Vector as VectorSource } from "ol/source";
import { Fill, Stroke, Style, Circle, Text } from "ol/style.js";
import XYZ from "ol/source/XYZ";
import { TileWMS } from "ol/source";
import Tile from "ol/layer/Tile";
import bus from "@/utils/bus";
import Feature from "ol/Feature";
import Point from "ol/geom/Point";
import TileGrid from "ol/tilegrid/TileGrid";
import { getPointData } from "@/request/index";
import _ from "lodash";
import { useI18n } from "vue-i18n";
import GeoJSON from "ol/format/GeoJSON";
import request from "@/request/axios";
const { t, locale } = useI18n();
const baseURL = "http://129.211.174.149:18080";
const map = ref(null);

//天地图底图
const tiandituMapSource = new XYZ({
  url: "http://t0.tianditu.com/DataServer?T=vec_w&tk=d212aa1b10fc6b036280ee9eab8b389c&x={x}&y={y}&l={z}",
});
const tiandiMapLayer = new Tile({
  zindex: 1,
  title: "矢量底图",
  source: tiandituMapSource,
});

const qgjZoomLayer1 = ref(null);
const qgjZoomLayer2 = ref(null);
//渲染地图（qgs服务的地图数据）,低分辨率
const createQgjZoomLayer1 = () => {
  const customTileGrid = new TileGrid({
    tileSize: 1024, // 修改瓦片大小
    // 以下参数需根据实际坐标系和服务端配置调整
    origin: [-180, 90], // 瓦片网格原点（示例为 EPSG:4326 常用值）
    resolutions: [
      // 分辨率数组（需与服务端支持的缩放级别匹配）
      0.703125, // 缩放级别 0
      0.3515625, // 缩放级别 1
      0.17578125, // 缩放级别 2（根据实际情况扩展）
    ],
  });
  const token = JSON.parse(localStorage.getItem("leiyangUser"))?.token;

  const qgjMapSource = new TileWMS({
    url: `${baseURL}/map/getmap`,
    params: {
      SERVICE: "WMS",
      VERSION: "1.1.0",
      REQUEST: "GetMap",
      Map: props.qgsMapPath,
      LAYERS: props.tifMapPath,
      FORMAT: "image/png",
      CRS: "EPSG:4326",
      language: locale.value,
    },
    tileGrid: customTileGrid,
    serverType: "geoserver",

    // 自定义 tile 加载函数
    tileLoadFunction: function (imageTile, src) {
      fetch(src, {
        headers: {
          Token: `${token}`,
        },
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.blob();
        })
        .then((blob) => {
          const objectUrl = URL.createObjectURL(blob);
          imageTile.getImage().src = objectUrl;
        })
        .catch((err) => {
          console.error("Tile loading error:", err);
        });
    },
  });
  return new Tile({
    zindex: 2,
    title: "渲染地图",
    source: qgjMapSource,
    minZoom: 0,
    maxZoom: 12,
  });
};
//渲染地图（qgs服务的地图数据），高分辨率
const createQgjZoomLayer2 = () => {
  const customTileGrid = new TileGrid({
    tileSize: 256,
    origin: [-180, 90],
    resolutions: [0.3515625, 0.17578125, 0.087890625],
  });
  const token = JSON.parse(localStorage.getItem("leiyangUser"))?.token;

  const qgjMapSource = new TileWMS({
    url: `${baseURL}/map/getmap`,
    params: {
      SERVICE: "WMS",
      VERSION: "1.1.0",
      REQUEST: "GetMap",
      Map: props.qgsMapPath,
      LAYERS: props.tifMapPath,
      FORMAT: "image/png",
      CRS: "EPSG:4326",
      language: locale.value,
    },
    tileGrid: customTileGrid,
    serverType: "geoserver",

    //  自定义 tile 加载函数
    tileLoadFunction: function (imageTile, src) {
      // 使用fetch替代默认的img.src加载
      fetch(src, {
        headers: {
          Token: `${token}`,  //添加Token到请求头
        },
      })
        .then((response) => {
          if (!response.ok) {
            throw new Error("Network response was not ok");
          }
          return response.blob();
        })
        .then((blob) => {
          const objectUrl = URL.createObjectURL(blob);
          imageTile.getImage().src = objectUrl;
        })
        .catch((err) => {
          console.error("Tile loading error:", err);
        });
    },
  });

  return new Tile({
    zindex: 2,
    title: "渲染地图",
    source: qgjMapSource,
    minZoom: 0,
    maxZoom: 12,
  });
};

//全球境界
const tiandituBoundarySource = new XYZ({
  url:
    "http://t0.tianditu.gov.cn/ibo_w/wmts?" +
    "SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0" +
    "&LAYER=ibo&STYLE=default&TILEMATRIXSET=w" +
    "&FORMAT=tiles&TILEMATRIX={z}&TILEROW={y}&TILECOL={x}" +
    "&tk=d212aa1b10fc6b036280ee9eab8b389c",
});
const tiandituBoundaryLayer = new Tile({
  zindex: 3,
  title: "全球境界",
  source: tiandituBoundarySource,
});

//天地图矢量注记
const tiandituMessageSource = new XYZ({
  url: "http://t0.tianditu.com/DataServer?T=cia_w&x={x}&y={y}&l={z}&tk=d212aa1b10fc6b036280ee9eab8b389c",
  tilePixelRatio: 2, // 固定设置为2以获得更高清晰度
  crossOrigin: "anonymous", // 添加跨域支持
});

const tiandituMessageLayer = new Tile({
  zindex: 100,
  title: "矢量注记地图",
  source: tiandituMessageSource,
  zoom: 12,
  minZoom: 1,
  maxZoom: 18,
  preload: Infinity, // 预加载所有瓦片
  updateWhileAnimating: true, // 移动时更新瓦片
  updateWhileInteracting: true, // 交互时更新瓦片
  renderMode: "vector", // 使用矢量渲染模式
  renderBuffer: 200, // 增加渲染缓冲区
});

// 创建矢量源和图层用于标记点
const markerSource = new VectorSource();
const markerLayer = new VectorLayer({
  zindex: 5,
  source: markerSource,
  style: new Style({
    image: new Circle({
      radius: 6,
      fill: new Fill({ color: "red" }),
      stroke: new Stroke({ color: "white", width: 2 }),
    }),
  }),
});

// 国家点图层，只显示地名标注，不显示蓝色圆点
const countryPointLayer = new VectorLayer({
  zIndex: 10,
  title: "国家点",
  visible: true,
  source: new VectorSource({
    url: "/map/国家点.json",
    format: new GeoJSON(),
  }),
  style: function (feature) {
    return new Style({
      // 注释掉蓝色圆点的显示
      /*
      image: new Circle({
        radius: 6,
        fill: new Fill({ color: '#00b6ff' }),
        stroke: new Stroke({ color: '#fff', width: 2 })
      }),
      */
      text: new Text({
        font: "normal 14px 微软雅黑",
        text: feature.get("NAME") || "",
        fill: new Fill({ color: "#222" }),
        stroke: new Stroke({ color: "#fff", width: 2 }),
        offsetY: -12,
      }),
    });
  },
});

// 注释掉中国市级边界图层（仅英文显示相关代码）
/*
// 中国市级边界图层（仅英文显示，样式与英文底图风格一致）
const chinaCityLayer = new VectorLayer({
  zIndex: 9,
  title: '中国市',
  visible: true, // 控制显示由图层数组决定
  source: new VectorSource({
    url: '/map/中国_市.geojson',
    format: new GeoJSON()
  }),
  style: function(feature) {
    return new Style({
      stroke: new Stroke({
        color: '#1E90FF', // 英文底图常用蓝色
        width: 2.5,
        lineDash: [6, 4] // 虚线样式
      }),
      fill: new Fill({
        color: 'rgba(30, 144, 255, 0.05)' // 更淡的蓝色半透明
      }),
      text: new Text({
        font: '12px Arial',
        text: locale.value === 'english'
          ? (feature.get('name_en') || feature.get('name') || '')
          : '', // 只在英文状态下显示label
        fill: new Fill({ color: '#1E90FF' }),
        stroke: new Stroke({ color: '#fff', width: 2 }),
        offsetY: -10
      })
    });
  }
});
*/

//地图边界
const boundaryParams = ref({
  level: "国家",
  area: "中国,美国",
  class: "boundary",
  CRS: "EPSG:4326",
});

const highLightSource = new TileWMS({
  url: `${baseURL}/map/getboundary`,
  params: boundaryParams.value,
});
//根据图表选择区域显示高亮
const highLightLayer = new Tile({
  zindex: 7,
  title: "地图边界",
  source: highLightSource,
  minZoom: 0,
  maxZoom: 12,
});

//坐标标准化
const normalizeCoordinate = (coordinate) => {
  let lon = coordinate[0];
  // 保持经度在[-180, 180)范围内
  lon = (((lon % 360) + 540) % 360) - 180;
  return [
    parseFloat(lon.toFixed(6)), // 保留6位小数
    parseFloat(coordinate[1].toFixed(6)), // 保留6位小数
  ];
};

//展示弹窗
const showPopover = ref(false);
const popoverStyle = reactive({
  top: "0px",
  left: "0px",
});
// 关闭弹窗
const closePopover = () => {
  showPopover.value = false;
  markerSource.clear();
  clickedCoordinate2D = {
    longitude: null,
    latitude: null,
  };

  bus.emit("clickedCoordinate2D", clickedCoordinate2D);  //发送点击坐标信息
};

//接收父组件参数
const props = defineProps({
  qgsMapPath: String,  //qgs地图路径
  tifMapPath: String,  //tif地图路径
  dataUnit: String,  //数据单位
  dataTypeThree: String,  //数据类型
  selectMapPath: String, // 新增业务路径
});

let clickedCoordinate2D = {
  longitude: null,
  latitude: null,
};
const longitude = ref(null);
const latitude = ref(null);
var marker = null;
// 维护标记点的原始坐标（未经标准化）
let rawMarkerCoordinate = null;
// 添加标记点的函数
const addMarker = (coordinate) => {
  markerSource.clear(); // 清除之前的标记
  rawMarkerCoordinate = coordinate; // 保存原始坐标
  // 标准化坐标
  const normalizeCoord = normalizeCoordinate(coordinate);
  marker = new Feature({
    geometry: new Point(normalizeCoord),
  });
  markerSource.addFeature(marker);
  // 输出标记点的经纬度信息
  clickedCoordinate2D = {
    longitude: normalizeCoord[0].toFixed(3),
    latitude: normalizeCoord[1].toFixed(3),
  };

  bus.emit("clickedCoordinate2D", clickedCoordinate2D); //发送点击坐标信息
  longitude.value = clickedCoordinate2D.longitude;
  latitude.value = clickedCoordinate2D.latitude;
};

//标记点是否可见
const containsCoordinate = (extent, coordinate) => {
  const x = coordinate[0];
  const y = coordinate[1];

  // 扩展判断逻辑以支持多世界
  return (
    y >= extent[1] && y <= extent[3] && (x >= extent[0] || x + 360 <= extent[2]) // 支持跨世界判断
  );
};

// 维护弹窗与标记点的固定像素偏移
const POPOVER_OFFSET = [0, -30]; // [水平偏移, 垂直偏移] 单位：像素

// 弹窗位置计算（基于像素偏移）
const updatePopoverPosition = (pixel) => {
  if (!pixel) return;

  const mapElement = document.getElementById("mapView");
  const mapRect = mapElement.getBoundingClientRect();

  // 应用固定像素偏移
  const adjustedPixel = [
    pixel[0] + POPOVER_OFFSET[0],
    pixel[1] + POPOVER_OFFSET[1],
  ];

  // 转换为屏幕坐标
  const screenX = adjustedPixel[0] + mapRect.left;
  const screenY = adjustedPixel[1] + mapRect.top;

  // 边界处理（确保弹窗在视口内）
  const viewportWidth = window.innerWidth;
  const viewportHeight = window.innerHeight;
  const popoverWidth = 200;
  const popoverHeight = 80;
  const margin = 10;

  // 水平边界
  let finalX = Math.max(
    margin,
    Math.min(screenX, viewportWidth - popoverWidth - margin)
  );

  // 垂直边界
  let finalY = Math.max(
    margin,
    Math.min(screenY, viewportHeight - popoverHeight - margin)
  );

  // 应用最终位置
  Object.assign(popoverStyle, {
    left: `${finalX}px`,
    top: `${finalY}px`,
    visibility: "visible",
  });
};

// 缩放等级与层级自动映射
function getLevelByZoom(area, zoom, selectMapPath) {
  // 按照正确的层级顺序：区域 → 次区域 → 国家 → 电网 → 省级 → 地级 → 县级
  // 调整阈值让默认状态下（zoom = 3.32）选择"区域"层级
  if (zoom >= 8 && area["县级"])
    return { level: "县级", places: [area["县级"]] };
  if (zoom >= 7 && area["地级"])
    return { level: "地级", places: [area["地级"]] };
  if (zoom >= 6 && area["省级"])
    return { level: "省级", places: [area["省级"]] };
  if (zoom >= 5 && area["电网"])
    return { level: "电网", places: [area["电网"]] };
  if (zoom >= 4 && area["国家"])
    return { level: "国家", places: [area["国家"]] };
  if (zoom >= 3.5 && area["次区域"])
    return { level: "次区域", places: [area["次区域"]] };
  if (area["区域"]) return { level: "区域", places: [area["区域"]] };
  return null;
}

// 处理点击事件
const handleMapClick = async (event) => {
  closePopover();   //关闭之前的弹窗
  const coordinate = event.coordinate;  //获取点击坐标
  const normalizedCoordinate = normalizeCoordinate(coordinate);
  addMarker(normalizedCoordinate);  //添加标记点
  identifyNum(normalizedCoordinate[0], normalizedCoordinate[1]);  //查询数据值
  const rawPixel = map.value.getPixelFromCoordinate(coordinate);
  const adjustedPixel = wrapPixel(rawPixel, map.value.getSize()[0]);  //计算弹窗位置
  if (adjustedPixel) {
    updatePopoverPosition(adjustedPixel);
    showPopover.value = true;
  }
  // getpoint接口请求
  const params = {
    MAP: props.qgsMapPath,
    LAYERS: props.tifMapPath,
    LONGITUDE: normalizedCoordinate[0],
    LATITUDE: normalizedCoordinate[1],
    language: locale.value,
  };
  let res;
  try {
    res = await request.get("/map/getpoint", { params });
  } catch (e) {
    console.error("[Map2D] getpoint 请求异常:", e);
  }
  if (res && res.data && res.data.area) {
    const area = res.data.area;
    const zoom = map.value.getView().getZoom();
    const result = getLevelByZoom(area, zoom, props.selectMapPath);
    if (result) {
      bus.emit("mapPointSelected", {
        level: result.level,
        places: result.places,
        map: props.selectMapPath,
        language: locale.value,
        area,
      });
    }
  } else {
    console.error("[Map2D] getpoint 返回无 area:", res);
  }
};

// 世界副本计算
const calculateWorldCoordinates = (coord, extent) => {
  const worldWidth = 360;
  const visibleWorlds = Math.ceil((extent[2] - extent[0]) / worldWidth) + 1;
  const coordinates = [];

  for (let i = -visibleWorlds; i <= visibleWorlds; i++) {
    coordinates.push([coord[0] + i * worldWidth, coord[1]]);
  }
  return coordinates;
};

// 精确的可见坐标查找
const findFirstVisibleCoordinate = (coordinates, extent) => {
  const viewCenter = map.value.getView().getCenter();
  // 寻找距离视图中心最近的可见副本
  return coordinates.reduce(
    (nearest, coord) => {
      if (containsCoordinate(extent, coord)) {
        const dist = Math.abs(coord[0] - viewCenter[0]);
        return dist < nearest.distance ? { coord, distance: dist } : nearest;
      }
      return nearest;
    },
    { coord: null, distance: Infinity }
  ).coord;
};

// 地图视图变化事件处理
const handleViewChange = _.debounce(() => {
  if (!markerSource || markerSource.getFeatures().length === 0) {
    showPopover.value = false;
    return;
  }
  const view = map.value.getView();
  const mapSize = map.value.getSize();
  const extent = view.calculateExtent(mapSize);
  const resolution = view.getResolution();

  // 基于原始坐标和当前分辨率计算有效范围
  const isVisible = checkMarkerVisibility(
    rawMarkerCoordinate,
    extent,
    resolution
  );

  showPopover.value = isVisible;

  if (isVisible) {
    // 计算所有可能的世界副本
    const worldCoordinates = calculateWorldCoordinates(
      rawMarkerCoordinate,
      extent
    );
    const visibleCoord = findFirstVisibleCoordinate(worldCoordinates, extent);
    // 获取精确像素位置
    const pixel = map.value.getPixelFromCoordinate(visibleCoord);
    updatePopoverPosition(pixel);
  }
  if (marker) {
    const currentCoord = marker.getGeometry().getCoordinates();
    // 更新坐标信息
    clickedCoordinate2D = currentCoord;
    longitude.value = clickedCoordinate2D.longitude;
    latitude.value = clickedCoordinate2D.latitude;

    bus.emit("clickedCoordinate2D", clickedCoordinate2D);
  }
}, 100);

// 基于分辨率的可见性检测
const checkMarkerVisibility = (coord, extent, resolution) => {
  // 计算标记点实际占用的屏幕空间
  const markerSize = 20; // 标记点直径（像素）
  const markerBuffer = (markerSize / 2) * resolution;

  // 扩展检测范围
  const expandedExtent = [
    extent[0] - markerBuffer,
    extent[1] - markerBuffer,
    extent[2] + markerBuffer,
    extent[3] + markerBuffer,
  ];

  return containsCoordinate(expandedExtent, coord);
};

// 像素环绕处理函数
const wrapPixel = (pixel, mapWidth) => {
  if (!pixel) return null;
  let x = pixel[0];
  // 处理水平环绕
  x = ((x % mapWidth) + mapWidth) % mapWidth;
  return [x, pixel[1]];
};

const identifyNumResult = ref(null);
const isLoading = ref(false);

// 修改 identifyNum 函数
const identifyNum = async (longitude, latitude) => {
  isLoading.value = true;
  identifyNumResult.value = null;
  try {
    const result = await getPointData(
      props.qgsMapPath,
      props.tifMapPath,
      longitude,
      latitude,
      locale.value
    );
    identifyNumResult.value = result ? result.toFixed(2) : "null";
  } catch (error) {
    console.error("数值获取失败:", error);
    identifyNumResult.value = t("message.dataException");
  } finally {
    isLoading.value = false;
  }
};

// 工具函数：根据当前语言返回正确的图层数组（英文状态下显示国家点图层但不显示蓝色圆点）
function getBaseLayersByLocale(localeVal) {
  if (localeVal === "english") {
    // 英文：底图、边界、marker、国家点（只显示地名标注，不显示蓝色圆点）
    return [
      tiandiMapLayer,  //天地图矢量底图
      tiandituBoundaryLayer,  //全球境界
      markerLayer,  //标记点图层
      countryPointLayer,  //国家点图层
    ];
  } else {
    // 中文：底图、中文注记、边界、marker
    return [
      tiandiMapLayer,    //天地图矢量底图 
      tiandituMessageLayer,  //中文注记
      tiandituBoundaryLayer,  //全球境界
      markerLayer,  //标记点图层
    ];
  }
}

// 监听地图数据变化
watch(
  () => [props.qgsMapPath, props.tifMapPath],
  ([newQgs, newTif]) => {
    if (map.value && newQgs && newTif) {
      closePopover();
      markerSource.clear();
      qgjZoomLayer1.value = createQgjZoomLayer1();
      // 只用 setLayers，按语言切换
      const layers = getBaseLayersByLocale(locale.value);
      // 插入渲染图层和高亮图层
      layers.splice(1, 0, qgjZoomLayer1.value);
      layers.push(highLightLayer);
      map.value.setLayers(layers);
      map.value.on("click", handleMapClick);
      map.value.on("moveend", handleViewChange);
    } else {
      map.value?.setLayers(getBaseLayersByLocale(locale.value));
      map.value?.un("click", handleMapClick);
      map.value?.un("moveend", handleViewChange);
      closePopover();
      markerSource.clear();
    }
  },
  { immediate: true }
);

onMounted(() => {
  map.value = new Map({
    target: "mapView",    //指定DOM元素ID(mapView是地图容器元素的ID)
    layers: getBaseLayersByLocale(locale.value),  //初始图层数组（根据语言返回不同的图层）
    view: new View({
      center: [116.46, 39.92],  //初始中心点（北京）
      projection: "EPSG:4326",  //地理坐标系
      zoom: 1,  //初始缩放级别
    }),
    controls: [],  //不显示默认控件
  });
});

// 监听语言切换
watch(locale, (newLocale) => {
  if (!map.value) return;

  // 如果有地图数据，需要重新创建WMS图层以使用新的语言参数
  if (props.qgsMapPath && props.tifMapPath) {
    closePopover();
    markerSource.clear();
    qgjZoomLayer1.value = createQgjZoomLayer1();

    // 重新设置图层，包括新创建的WMS图层
    const layers = getBaseLayersByLocale(newLocale);
    layers.splice(1, 0, qgjZoomLayer1.value);
    layers.push(highLightLayer);
    map.value.setLayers(layers);

    // 重新绑定事件
    map.value.on("click", handleMapClick);
    map.value.on("moveend", handleViewChange);
  } else {
    // 没有地图数据时，只更新基础图层
    map.value.setLayers(getBaseLayersByLocale(newLocale));
  }
});

const initMap = (layers) => {
  // 只处理中文注记图层
  const baseLayers = layers.filter((l) => l !== tiandituMessageLayer);
  baseLayers.splice(1, 0, tiandituMessageLayer);
  map.value = new Map({
    target: "mapView",
    layers: baseLayers,
    view: new View({
      center: [116.46, 39.92],
      projection: "EPSG:4326",
      zoom: 1,
    }),
    controls: [],
  });
};

// 组件卸载时移除事件监听
onBeforeUnmount(() => {
  if (map.value) {
    map.value.un("click", handleMapClick);
    map.value.un("moveend", handleViewChange);
  }
});
onUnmounted(() => {
  // bus.off("boundaryParams");
});
</script>

<style scoped lang="less">
.map-container {
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
}

.mapView {
  width: 100%;
  height: 100%;
  margin: 0;
}

.coordinate-popover {
  position: fixed;
  z-index: 1000;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #409eff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 5px;
  min-width: 170px;
  font-family: Arial, sans-serif;

  .close-btn {
    position: absolute;
    top: 0px;
    right: 0px;
    background: none;
    background-color: rgb(216, 213, 213);
    border: none;
    font-size: 16x;
    cursor: pointer;
    color: #666;

    &:hover {
      color: white;
      background-color: red;
    }
  }

  .popover-content {
    position: relative;
    margin: 5px;
    margin-top: 10px;

    .coordinates {
      .coord-item {
        margin: 8px 0;
        display: flex;
        align-items: center;
        justify-content: center;
        color: black;
        font-weight: 500;

        .label {
          min-width: 10px;
        }

        .value {
          min-width: 10px;
          .loading-text {
            color: black;
          }
        }
      }
    }
  }
}
.map-license {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 20px;
  margin: 0 auto;
  width: fit-content;
  z-index: 9999;
  padding: 4px 16px;
  background: #fff;
  color: #222;
  font-size: 16px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-radius: 4px;
}
.map-license a {
  color: #222;
  text-decoration: none;
}
.map-license a:hover {
  color: #409eff;
  text-decoration: underline;
}
</style>
