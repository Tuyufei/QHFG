<template>
  <div class="legend" v-show="imageUrl">
    <div class="title">{{ $t("message.legend") }}</div>
    <img :src="imageUrl" alt="" />
  </div>
</template>

<script setup>
import { inject, ref, watch, onMounted, watchEffect } from "vue";
import bus from "@/utils/bus";
import { getLegendGraphic } from "@/request";
import { useI18n } from "vue-i18n";

const { t, locale } = useI18n();
//接收index文件传的地图数据
// const props = defineProps({
//   qgsMapPath: String,
//   tifMapPath: String,
// })

const props = inject("props");

const imageUrl = ref(""); //图例Url
const getLegend = async (mapPath, layer) => {
  const MAP = mapPath;
  const LAYER = layer;
  try {
    const base64Data = await getLegendGraphic(MAP, LAYER, locale.value);
    if (base64Data) {
      imageUrl.value = `data:image/png;base64,${base64Data}`;
    }
  } catch (error) {
    //???
    ElMessage.error(t("reslut.legendError"));
    console.error("生成图例URL失败:", error);
  }
};

watchEffect(() => {
  if (props.qgsMapPath && props.tifMapPath) {
    imageUrl.value = "";
    getLegend(props.qgsMapPath, props.tifMapPath);
  } else {
    imageUrl.value = "";
  }
});
</script>

<style lang="scss" scoped>
.legend {
  z-index: 100;
  position: absolute;
  right: 10px;
  bottom: 10px;
  padding: 10px;
  width: 150px;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 5px;

  img {
    margin-top: 10px;
    width: 100%;
    // height: 100%;
  }
}
</style>
