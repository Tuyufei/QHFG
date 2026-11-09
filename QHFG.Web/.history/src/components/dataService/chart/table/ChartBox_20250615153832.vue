<template>
  <!-- No changes to template section -->
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import { bus } from '../../utils/bus';

// 监听全局数据变化
onMounted(() => {
  bus.on("updateGlobalData", (data) => {
    console.log("收到全局数据更新:", data);
    // 更新本地状态
    if (data.dataDescription) {
      dataDescription.value = data.dataDescription;
    }
    if (data.dataDescriptionDetail) {
      dataDescriptionDetail.value = data.dataDescriptionDetail;
    }
  });
  
  bus.on("currentState", (state) => {
    console.log("收到当前状态:", state);
    // 可以在这里更新图表或其他组件状态
  });
});

// 清理事件监听
onUnmounted(() => {
  bus.off("updateGlobalData");
  bus.off("currentState");
});
</script>

<style>
  /* No changes to style section */
</style> 