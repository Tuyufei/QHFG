<template>
  <div class="home_page" id="cesiumContainer">
    <div class="tdt-toolbar">
      <div class="tdt-btn" @click="zoomIn">
        <i class="iconfont icon-fangda"></i>
      </div>
      <div class="tdt-btn" @click="zoomOut">
        <i class="iconfont icon-suoxiao"></i>
      </div>
      <div class="tdt-btn" @click="rotate">
        <i class="iconfont icon-refresh-1-copy"></i>
      </div>
    </div>
    <!-- 3D地图点击弹窗 -->
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
import app from "../../main";
import * as Cesium from "cesium";
import {
  onMounted,
  onUnmounted,
  provide,
  reactive,
  ref,
  shallowRef,
  watch,
  watchEffect,
  nextTick,
} from "vue";
import { ElMessage } from "element-plus";
import bus from "@/utils/bus";
import { useI18n } from "vue-i18n";
import request from "@/request/axios";
const { t, locale } = useI18n();

// 接收index文件传的地图数据
const props = defineProps({
  qgsMapPath: String,
  tifMapPath: String,
  selectMapPath: String, // 新增业务路径
  dataUnit: String,
  dataTypeThree: String,
});
provide("props", props);
const wmsLayer = shallowRef(null);
let clickHandler = null; // 存储点击事件处理器
let viewer;
let initialCameraView = null;
let messageLayer = null; // 存储注记图层引用
let countryPointDataSource = null; // 存储国家点数据源
let clickMarkerEntity = null; // 存储点击标记点

// 弹窗相关变量
const showPopover = ref(false);
const popoverStyle = reactive({
  top: "0px",
  left: "0px",
});
const identifyNumResult = ref(null);
const isLoading = ref(false);

// 关闭弹窗
const closePopover = () => {
  showPopover.value = false;
  identifyNumResult.value = null;
  // 移除点击标记点
  if (clickMarkerEntity && viewer) {
    viewer.entities.remove(clickMarkerEntity);
    clickMarkerEntity = null;
  }
};

// 添加点击标记点
const addClickMarker = (longitude, latitude) => {
  // 移除之前的标记点
  if (clickMarkerEntity && viewer) {
    viewer.entities.remove(clickMarkerEntity);
  }

  // 创建新的标记点，只显示红色点，不显示文字标签
  clickMarkerEntity = viewer.entities.add({
    position: Cesium.Cartesian3.fromDegrees(longitude, latitude),
    point: {
      pixelSize: 12,
      color: Cesium.Color.RED,
      outlineColor: Cesium.Color.WHITE,
      outlineWidth: 2,
      heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
      disableDepthTestDistance: Number.POSITIVE_INFINITY,
      scaleByDistance: new Cesium.NearFarScalar(1.5e2, 1.0, 1.5e7, 0.5),
    },
  });
};

// 获取数据值的函数
const identifyNum = async (longitude, latitude) => {
  if (!props.qgsMapPath || !props.tifMapPath) return;

  isLoading.value = true;
  identifyNumResult.value = null;

  try {
    const url = `/map/getpoint`;
    const params = {
      MAP: props.qgsMapPath,
      LAYERS: props.tifMapPath,
      LONGITUDE: longitude,
      LATITUDE: latitude,
      language: locale.value,
    };

    const response = await request.get(url, {
      params,
      timeout: 15000, // 15秒超时
    });

    if (response && response.data && response.data.value !== undefined) {
      identifyNumResult.value =
        response.data.value !== null ? response.data.value.toFixed(2) : "null";
    } else {
      identifyNumResult.value = "null";
    }
  } catch (error) {
    console.warn("[Map3D] 数值获取失败:", error);
    identifyNumResult.value = "null";
  } finally {
    isLoading.value = false;
  }
};

// 更新弹窗位置
const updatePopoverPosition = (screenPosition) => {
  if (!screenPosition) return;

  const container = document.getElementById("cesiumContainer");
  const containerRect = container.getBoundingClientRect();

  // 计算弹窗位置（相对于容器）
  const x = screenPosition.x;
  const y = screenPosition.y - 30; // 向上偏移30px

  // 边界处理
  const viewportWidth = window.innerWidth;
  const viewportHeight = window.innerHeight;
  const popoverWidth = 200;
  const popoverHeight = 100;
  const margin = 20;

  // 左侧组件宽度估计值（结果框）
  const leftComponentWidth = 750;
  // 右侧组件宽度估计值（图表框）
  const rightComponentWidth = 720;

  // 计算点击位置相对于视口的位置
  const clickX = x + containerRect.left;
  const clickY = y + containerRect.top;

  let finalX, finalY;

  // 优先策略：尽量将弹窗放在组件上方，避免被遮挡
  if (clickY - popoverHeight - margin > margin) {
    // 上方有足够空间，优先放在上方
    finalY = clickY - popoverHeight - 10;
  } else {
    // 上方空间不足，尝试放在下方
    finalY = clickY + 10;
  }

  // 水平位置：避开左右两侧组件
  if (clickX < leftComponentWidth + margin) {
    finalX = leftComponentWidth + margin;
  } else if (
    clickX >
    viewportWidth - rightComponentWidth - popoverWidth - margin
  ) {
    finalX = viewportWidth - rightComponentWidth - popoverWidth - margin;
  } else {
    finalX = Math.max(
      leftComponentWidth + margin,
      Math.min(
        clickX - popoverWidth / 2,
        viewportWidth - rightComponentWidth - popoverWidth - margin
      )
    );
  }

  // 最终边界检查
  finalX = Math.max(
    margin,
    Math.min(finalX, viewportWidth - popoverWidth - margin)
  );
  finalY = Math.max(
    margin,
    Math.min(finalY, viewportHeight - popoverHeight - margin)
  );

  Object.assign(popoverStyle, {
    left: `${finalX}px`,
    top: `${finalY}px`,
    zIndex: "10000",
    position: "fixed",
    transform: "none",
    pointerEvents: "auto",
  });
};

// 监听相机变化，显示缩放等级
const handleCameraChange = () => {
  if (!viewer) return;

  const height = viewer.camera.positionCartographic.height;

  // 将相机高度转换为类似2D地图的缩放等级显示
  const zoomLevel = Math.max(
    0,
    Math.min(18, 18 - Math.log2(height / 156543.03392))
  );
};

// 缩放等级与层级自动映射
function getLevelByZoom(area, zoom, selectMapPath) {
  // 3D地图的缩放等级需要调整，因为Cesium的缩放机制与OpenLayers不同
  const height = viewer.camera.positionCartographic.height;

  // 根据实际默认相机高度（约2365万米）调整相机高度阈值
  // 按照正确的层级顺序：区域 → 次区域 → 国家 → 电网 → 省级 → 地级 → 县级
  // 调整阈值让默认状态下（height = 23652516米）选择"区域"层级
  if (height <= 1000000 && area["县级"])
    return { level: "县级", places: [area["县级"]] };
  if (height <= 2000000 && area["地级"])
    return { level: "地级", places: [area["地级"]] };
  if (height <= 4000000 && area["省级"])
    return { level: "省级", places: [area["省级"]] };
  if (height <= 7000000 && area["电网"])
    return { level: "电网", places: [area["电网"]] };
  if (height <= 10000000 && area["国家"])
    return { level: "国家", places: [area["国家"]] };
  if (height <= 15000000 && area["次区域"])
    return { level: "次区域", places: [area["次区域"]] };
  if (area["区域"]) return { level: "区域", places: [area["区域"]] };
  return null;
}

// 创建WMS图层的函数
const createWmsLayer = () => {
  // 如果已经存在图层，先移除
  if (wmsLayer.value) {
    viewer.imageryLayers.remove(wmsLayer.value, true);
  }

  // 创建新的 WMS 图层
  const wmsImageryProvider = new Cesium.WebMapServiceImageryProvider({
    url: "http://129.211.174.149:8080/ows/",
    layers: props.tifMapPath,
    parameters: {
      transparent: true,
      format: "image/png",
      MAP: props.qgsMapPath,
      service: "WMS",
      VERSION: "1.1.0",
      REQUEST: "GetMap",
      tiled: true, // 启用瓦片缓存
      width: 256, // 设置瓦片大小
      height: 256,
      language: locale.value, // 传递当前语言
    },
    crs: "EPSG:4326",
    tilingScheme: new Cesium.GeographicTilingScheme({
      numberOfLevelZeroTilesX: 2,
      numberOfLevelZeroTilesY: 1,
    }),
    enablePickFeatures: true,
    getFeatureInfoParameters: {
      MAP: props.qgsMapPath,
      format: "application/json",
      service: "WMS",
      version: "1.1.0",
      request: "GetFeatureInfo",
      info_format: "application/json",
      crs: "EPSG:4326",
      query_layers: props.tifMapPath,
      with_geometry: true,
    },
  });

  // 添加新图层并保存引用
  wmsLayer.value = viewer.imageryLayers.addImageryProvider(
    wmsImageryProvider,
    1
  );

  // 始终显示WMS图层
  wmsLayer.value.show = true;
  wmsLayer.value.alpha = 0.8;

  // 设置WMS图层的属性
  wmsLayer.value.brightness = 1.0;
  wmsLayer.value.maximumAnisotropy = 8; // 提高纹理质量
  wmsLayer.value.minimumTerrainLevel = 0; // 设置最小地形级别
  wmsLayer.value.maximumTerrainLevel = 12; // 设置最大地形级别
};

watchEffect(() => {
  if (props.qgsMapPath && props.tifMapPath) {
    createWmsLayer();
  } else {
    // 当条件不满足时，移除图层和点击事件
    if (wmsLayer.value) {
      viewer.imageryLayers.remove(wmsLayer.value, true);
      wmsLayer.value = null;
    }
    if (clickHandler) {
      clickHandler.destroy();
      clickHandler = null;
    }
  }
});

Cesium.Camera.DEFAULT_VIEW_RECTANGLE = Cesium.Rectangle.fromDegrees(
  -180,
  -90,
  180,
  90
);
Cesium.Camera.DEFAULT_VIEW_FACTOR = 2;
Cesium.Ion.defaultAccessToken =
  "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJqdGkiOiJiYzk2ZDMzMy04OTE5LTQyYzMtYTMxNi03ZDNlMWQ3NTEyMDIiLCJpZCI6MTI2OTI0LCJpYXQiOjE2Nzc3MjUwNTh9.PjAch1TW6HYeFYRL3J_Ukzjk1nB2fF2sBbCoDqTOtws";

const TDT_Token = "d212aa1b10fc6b036280ee9eab8b389c";

const TDT_Image = new Cesium.WebMapTileServiceImageryProvider({
  url:
    "http://{s}.tianditu.gov.cn/img_w/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=img&tileMatrixSet=w&TileMatrix={TileMatrix}&TileRow={TileRow}&TileCol={TileCol}&style=default&format=tiles&tk=" +
    TDT_Token,
  layer: "vec",
  style: "default",
  format: "tiles",
  tileMatrixSetID: "w",
  subdomains: ["t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"],
  maximumLevel: 18,
});

// 加载国家点数据的函数 - 只显示地名标注，不显示蓝色圆点
const loadCountryPoints = () => {
  // 移除旧的国家点数据
  if (countryPointDataSource) {
    viewer.dataSources.remove(countryPointDataSource, true);
    countryPointDataSource = null;
  }

  // 只在英文状态下加载国家点数据
  if (locale.value === "english") {
    Cesium.GeoJsonDataSource.load("/map/国家点.json", {
      clampToGround: true,
    })
      .then((dataSource) => {
        countryPointDataSource = dataSource;
        viewer.dataSources.add(dataSource);

        // 设置样式 - 只显示英文标签，不显示蓝色圆点
        dataSource.entities.values.forEach((entity) => {
          // 确保实体位置正确设置为地面高度
          if (entity.position) {
            const cartographic = Cesium.Cartographic.fromCartesian(
              entity.position.getValue(Cesium.JulianDate.now())
            );
            entity.position = Cesium.Cartesian3.fromRadians(
              cartographic.longitude,
              cartographic.latitude,
              0
            );
          }

          // 注释掉蓝色圆点的显示
          /*
        entity.point = new Cesium.PointGraphics({
          pixelSize: 12,
          color: Cesium.Color.fromCssColorString('#00b6ff'), // 蓝色点，与2D地图一致
          outlineColor: Cesium.Color.WHITE,
          outlineWidth: 2,
          heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
          disableDepthTestDistance: 500000000,//Number.POSITIVE_INFINITY,
          scaleByDistance: new Cesium.NearFarScalar(1.5e2, 1.0, 1.5e7, 0.5)
        });
        */

          // 只显示英文标签，确保贴合地面
          entity.label = new Cesium.LabelGraphics({
            text: entity.properties.NAME || "",
            font: "14px Arial, sans-serif",
            fillColor: Cesium.Color.fromCssColorString("#222222"),
            outlineColor: Cesium.Color.WHITE,
            outlineWidth: 2,
            style: Cesium.LabelStyle.FILL_AND_OUTLINE,
            pixelOffset: new Cesium.Cartesian2(0, -18),
            scale: 0.85,
            distanceDisplayCondition: new Cesium.DistanceDisplayCondition(
              0,
              100000000
            ),
            heightReference: Cesium.HeightReference.CLAMP_TO_GROUND,
            disableDepthTestDistance: 500000, //Number.POSITIVE_INFINITY
          });

          entity.billboard = undefined; // 不显示图标
        });
      })
      .catch((error) => {
        console.error("加载国家点数据失败:", error);
      });
  }
};

onMounted(() => {
  viewer = new Cesium.Viewer("cesiumContainer", {
    imageryProvider: TDT_Image,
    timeline: false,
    animation: false,
    fullscreenButton: false,
    geocoder: false,
    homeButton: false,
    sceneModePicker: false,
    baseLayerPicker: false,
    navigationHelpButton: false,
    infoBox: false,
    selectionIndicator: false,
    shouldAnimate: true,
  });

  // 启用深度检测，解决点位悬浮问题
  viewer.scene.globe.depthTestAgainstTerrain = true;

  // 添加中文注记图层并保存引用
  const TDT_Message = new Cesium.WebMapTileServiceImageryProvider({
    url:
      "http://{s}.tianditu.gov.cn/cva_w/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=cva&tileMatrixSet=w&tk=" +
      TDT_Token,
    layer: "cva",
    style: "default",
    format: "tiles",
    tileMatrixSetID: "w",
    subdomains: ["t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7"],
    maximumLevel: 18,
  });

  messageLayer = viewer.imageryLayers.addImageryProvider(TDT_Message);

  // 根据当前语言设置图层可见性
  messageLayer.show = locale.value !== "english";

  viewer.scene.skyBox.show = true;
  viewer.scene.skyAtmosphere.show = true;
  viewer.scene.backgroundColor = Cesium.Color.BLACK;
  viewer.cesiumWidget.creditContainer.style.display = "none";
  viewer.camera.moveForward(3000000);

  // 记录初始相机参数
  initialCameraView = {
    destination: Cesium.Cartesian3.clone(viewer.camera.positionWC),
    orientation: {
      heading: viewer.camera.heading,
      pitch: viewer.camera.pitch,
      roll: viewer.camera.roll,
    },
  };

  // 确保地图完全加载后开始自动旋转
  viewer.scene.globe.tileLoadProgressEvent.addEventListener(() => {
    if (viewer.scene.globe.tilesLoaded) {
      startAutoRotate();
    }
  });

  // 备用方案：延迟2秒开始旋转，确保地图已加载
  setTimeout(() => {
    if (!autoRotateInterval) {
      startAutoRotate();
    }
  }, 2000);

  // 通知父组件Map3D已挂载
  bus.emit("map3DMounted");

  // 设置 MutationObserver 监听容器可见性变化
  setTimeout(() => {
    const container = document.getElementById("cesiumContainer");
    if (container) {
      observer = new MutationObserver(() => {
        startRotationWhenVisible();
      });

      observer.observe(container, {
        attributes: true,
        attributeFilter: ["style", "class"],
      });
    }
  }, 1000);

  // 加载国家点数据 - 只显示地名标注，不显示蓝色圆点
  loadCountryPoints();

  // 添加相机变化监听器
  viewer.camera.changed.addEventListener(handleCameraChange);
});

// 组件卸载时清理
onUnmounted(() => {
  stopAutoRotate();
  if (clickHandler) {
    clickHandler.destroy();
  }
  if (observer) {
    observer.disconnect();
  }
  if (viewer) {
    viewer.destroy();
  }
  // 清理事件监听器
  bus.off("map3DVisible");
  bus.off("map3DMounted");

  // 移除国家点数据
  if (countryPointDataSource) {
    viewer.dataSources.remove(countryPointDataSource, true);
    countryPointDataSource = null;
  }
});

// 监听组件可见性变化，确保切换到3D地图时开始旋转
const startRotationWhenVisible = () => {
  nextTick(() => {
    const container = document.getElementById("cesiumContainer");
    if (container && viewer && !autoRotateInterval) {
      // 检查容器是否可见
      const rect = container.getBoundingClientRect();
      if (rect.width > 0 && rect.height > 0) {
        startAutoRotate();
      }
    }
  });
};

// 监听父组件的切换事件
bus.on("map3DVisible", () => {
  setTimeout(() => {
    // 清除之前的点位和弹窗，恢复初始状态
    closePopover();

    if (viewer && !autoRotateInterval) {
      startAutoRotate();
    }
  }, 500);
});

// 监听组件挂载完成事件
bus.on("map3DMounted", () => {
  setTimeout(() => {
    if (viewer && !autoRotateInterval) {
      startAutoRotate();
    }
  }, 1000);
});

// 使用 MutationObserver 监听容器可见性变化
let observer = null;

// 按钮功能实现
let layerVisible = ref(true);
const rotating = ref(false);
let autoRotateInterval = null;
let allowAutoRotate = true;

function zoomIn() {
  if (viewer) {
    viewer.camera.moveForward(2000000);
  }
}
function zoomOut() {
  if (viewer) {
    viewer.camera.moveBackward(2000000);
  }
}
function rotate() {
  if (!viewer || rotating.value) return;
  rotating.value = true;
  allowAutoRotate = true;

  // 关闭弹窗，恢复初始状态
  closePopover();

  if (initialCameraView) {
    viewer.camera.flyTo({
      destination: initialCameraView.destination,
      orientation: initialCameraView.orientation,
      duration: 2.0,
      complete: () => {
        rotating.value = false;
        startAutoRotate(); // 恢复自动旋转
      },
    });
  } else {
    viewer.camera.flyHome(2.0);
    setTimeout(() => {
      rotating.value = false;
      startAutoRotate(); // 恢复自动旋转
    }, 2000);
  }
}

// 自动旋转功能
function startAutoRotate() {
  if (!viewer || autoRotateInterval || !allowAutoRotate) return;
  const rotationSpeed = 0.002;
  autoRotateInterval = setInterval(() => {
    if (viewer && viewer.scene && viewer.scene.camera) {
      viewer.scene.camera.rotateRight(rotationSpeed);
    }
  }, 16);
  viewer.scene.canvas.addEventListener("click", handleMapClickToStopRotate, {
    once: true,
  });
}

function stopAutoRotate() {
  allowAutoRotate = false;
  if (autoRotateInterval) {
    clearInterval(autoRotateInterval);
    autoRotateInterval = null;
    if (viewer && viewer.scene && viewer.scene.canvas) {
      viewer.scene.canvas.removeEventListener(
        "click",
        handleMapClickToStopRotate
      );
    }
  }
}

function handleMapClickToStopRotate(e) {
  if (e.button === 0) {
    stopAutoRotate();
    // 停止旋转后，重新绑定点击事件用于地点选择
    setTimeout(() => {
      if (viewer && viewer.scene && viewer.scene.canvas) {
        viewer.scene.canvas.addEventListener(
          "click",
          handleMapClickForLocation,
          { once: true }
        );
      }
    }, 100);
  }
}

// 处理地图点击获取地点信息的函数
async function handleMapClickForLocation(e) {
  if (e.button !== 0) return; // 只处理左键点击

  // 先关闭之前的弹窗和标记点，确保切换点位时不会有延迟感
  closePopover();

  // 获取点击位置的经纬度
  const cartesian = viewer.camera.pickEllipsoid(
    new Cesium.Cartesian2(
      e.clientX - viewer.canvas.getBoundingClientRect().left,
      e.clientY - viewer.canvas.getBoundingClientRect().top
    ),
    viewer.scene.globe.ellipsoid
  );

  if (!cartesian) {
    // 重新绑定点击事件，即使这次点击无效
    setTimeout(() => {
      if (viewer && viewer.scene && viewer.scene.canvas) {
        viewer.scene.canvas.addEventListener(
          "click",
          handleMapClickForLocation,
          { once: true }
        );
      }
    }, 100);
    return;
  }

  const cartographic = Cesium.Cartographic.fromCartesian(cartesian);
  const longitude = Cesium.Math.toDegrees(cartographic.longitude);
  const latitude = Cesium.Math.toDegrees(cartographic.latitude);

  // 发送坐标信息
  let clickedCoordinate3D = {
    longitude: longitude.toFixed(3),
    latitude: latitude.toFixed(3),
  };
  bus.emit("clickedCoordinate3D", clickedCoordinate3D);

  // 添加红点标记
  addClickMarker(longitude, latitude);

  // 显示弹窗并获取数据值
  const screenPosition = new Cesium.Cartesian2(
    e.clientX - viewer.canvas.getBoundingClientRect().left,
    e.clientY - viewer.canvas.getBoundingClientRect().top
  );
  updatePopoverPosition(screenPosition);
  showPopover.value = true;

  // 如果没有地图数据，直接返回，但仍要重新绑定事件
  if (!props.qgsMapPath || !props.tifMapPath || !props.selectMapPath) {
    // 重新绑定点击事件
    setTimeout(() => {
      if (viewer && viewer.scene && viewer.scene.canvas) {
        viewer.scene.canvas.addEventListener(
          "click",
          handleMapClickForLocation,
          { once: true }
        );
      }
    }, 100);
    return;
  }

  // 获取该点的数据值
  identifyNum(longitude, latitude);

  try {
    // 调用getpoint接口获取地点信息
    const params = {
      MAP: props.qgsMapPath,
      LAYERS: props.tifMapPath,
      LONGITUDE: longitude,
      LATITUDE: latitude,
      language: locale.value,
    };

    const res = await request.get("/map/getpoint", {
      params,
      timeout: 10000, // 10秒超时
    });

    if (res && res.data && res.data.area) {
      const area = res.data.area;

      // 获取相机高度作为缩放参考
      const height = viewer.camera.positionCartographic.height;
      const result = getLevelByZoom(area, height, props.selectMapPath);

      if (result) {
        // 发送地图点选事件，驱动统计图表和表格更新
        bus.emit("mapPointSelected", {
          level: result.level,
          places: result.places,
          map: props.selectMapPath,
          language: locale.value,
          area,
        });
      }
    } else {
      console.error("[Map3D] getpoint 返回无 area:", res);
    }
  } catch (error) {
    console.error("[Map3D] getpoint 请求异常:", error);
  } finally {
    // 无论成功还是失败，都重新绑定点击事件
    setTimeout(() => {
      if (viewer && viewer.scene && viewer.scene.canvas) {
        viewer.scene.canvas.addEventListener(
          "click",
          handleMapClickForLocation,
          { once: true }
        );
      }
    }, 100);
  }
}

// 监听语言变化，控制图层显示
watch(locale, (newLocale) => {
  if (viewer) {
    // 根据语言设置显示/隐藏注记图层
    if (messageLayer) {
      messageLayer.show = newLocale !== "english";
    }
    // 重新加载国家点数据 - 只显示地名标注，不显示蓝色圆点
    loadCountryPoints();
    // 强制重新创建WMS图层以更新语言参数
    if (props.qgsMapPath && props.tifMapPath) {
      if (wmsLayer.value) {
        viewer.imageryLayers.remove(wmsLayer.value, true);
        wmsLayer.value = null;
      }
      createWmsLayer();
    }
  }
});
</script>

<style lang="scss" scoped>
.home_page {
  position: relative;
  width: 100vw;
  height: 100vh;
}
#cesiumContainer {
  width: 100vw;
  height: 100vh;
  position: relative;
  z-index: 1;
}
.tdt-toolbar {
  position: fixed;
  top: 162px;
  right: 8px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 10;
}
.tdt-btn {
  width: 25px;
  height: 25px;
  background: #4ba0fd;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: background 0.2s;
}
.tdt-btn:hover {
  background: #3772ff;
}
.tdt-btn.active {
  background: #ff6b35;
  animation: pulse 2s infinite;
}
.tdt-btn .iconfont {
  color: #fff;
  font-size: 18px;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 107, 53, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(255, 107, 53, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 107, 53, 0);
  }
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
    font-size: 16px;
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
</style>
