<template>
  <div class="map_info">
    <!-- <span>{{ $t("message.fps") }}：<span :style="mapInfo.fps >= 60 ? 'color: #0DBC79;' : 'color:red'">{{ mapInfo.fps }}
        FPS
      </span></span> -->
    <span>{{ $t("message.lon") }}：{{ mapInfo.lng }}°</span>
    <span>{{ $t("message.lat") }}：{{ mapInfo.lat }}°</span>
    <!-- <span>{{ $t("message.alt") }}：{{ mapInfo.alt }}米</span> -->
    <!-- <span>{{ $t("message.heading") }}：{{ mapInfo.heading }}°</span>
    <span>{{ $t("message.pitch") }}：{{ mapInfo.pitch }}°</span>
    <span>{{ $t("message.roll") }}：{{ mapInfo.roll }}°</span> -->
    <!-- <span
      >{{ $t("message.mousePosition") }}：{{ mapInfo.screenDegreeX }}°，{{
        mapInfo.screenDegreeY
      }}°，{{ mapInfo.screenHeight }}{{ $t("message.meter") }}</span
    > -->
  </div>
</template>

<script setup>
import * as Cesium from "cesium";
import { onMounted, reactive, nextTick, inject } from "vue";
import CoordTransform from "@/utils/CoordTransform";
const viewer = inject("viewer");
// 状态栏信息
const mapInfo = reactive({
  fps: 0,
  lng: 0,
  lat: 0,
  alt: 0,
  heading: 0,
  pitch: 0,
  roll: 0,
  screenDegreeX: 0,
  screenDegreeY: 0,
  screenHeight: 0,
});
onMounted(() => {
  nextTick(() => {
    computeMapInfo(viewer);
  });
});

const computeMapInfo = viewer => {
  let lastTime = new Date().getTime();
  let lastFrames = 0;
  // 计算帧率
  function updateFrameRate() {
    const currentTime = new Date().getTime();
    const deltaTime = currentTime - lastTime;
    if (deltaTime > 1000) {
      mapInfo.fps = viewer.scene.frameState.frameNumber - lastFrames - 1;
      lastTime = currentTime;
      lastFrames = viewer.scene.frameState.frameNumber;
    }
  }
  viewer.scene.postRender.addEventListener(e => {
    updateFrameRate();
    mapInfo.lng = Cesium.Math.toDegrees(
      viewer.camera.positionCartographic.longitude
    ).toFixed(5);
    mapInfo.lat = Cesium.Math.toDegrees(
      viewer.camera.positionCartographic.latitude
    ).toFixed(5);
    mapInfo.alt = viewer.camera.positionCartographic.height.toFixed(2);
    mapInfo.heading = Cesium.Math.toDegrees(viewer.camera.heading).toFixed(2);
    mapInfo.pitch = Cesium.Math.toDegrees(viewer.camera.pitch).toFixed(2);
    mapInfo.roll = Cesium.Math.toDegrees(viewer.camera.roll).toFixed(2);
  });
  // 鼠标位置
  const handler = new Cesium.ScreenSpaceEventHandler(viewer.scene.canvas);
  handler.setInputAction(e => {
    const cartesian = CoordTransform.getCartesian3FromPx(viewer, e.endPosition);
    if (cartesian) {
      const cartographic = Cesium.Cartographic.fromCartesian(cartesian);
      mapInfo.screenDegreeX = Cesium.Math.toDegrees(
        cartographic.longitude
      ).toFixed(5);
      mapInfo.screenDegreeY = Cesium.Math.toDegrees(
        cartographic.latitude
      ).toFixed(5);
      mapInfo.screenHeight = cartographic.height.toFixed(2);
    }
  }, Cesium.ScreenSpaceEventType.MOUSE_MOVE);
};
</script>

<style lang="scss" scoped>
.map_info {
  z-index: 100;
  position: absolute;
  right: 20%;
  bottom: 0;
  // width: 100%;
  display: flex;
  gap: 5px;
  // padding: 2px 20px 2px 120px;
  padding: 2px 20px;
  border-radius: 3px;
  color: #c8d8eb;
  font-size: 14px;
  font-family: "SoureHanSans";
  background-color: rgba(30, 49, 75, 0.5);
}
</style>
