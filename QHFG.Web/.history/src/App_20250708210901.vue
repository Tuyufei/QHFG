<template>
  <router-view></router-view>
  <!-- 路由缓存 -->
  <!-- <router-view v-slot="{ Component }">
    <keep-alive>
      <component
        :is="Component"
        :key="$route.name"
        v-if="$route.meta.keepAlive"
      />
    </keep-alive>
    <component
      :is="Component"
      :key="$route.name"
      v-if="!$route.meta.keepAlive"
    />
  </router-view> -->
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';

function clearLocalData() {
  localStorage.removeItem('dataServiceResults_en');
  localStorage.removeItem('dataServiceResults_zh');
  localStorage.removeItem('leiyangUser');
}

// 页面加载时判断是否为新会话（所有标签页都关闭后重新打开）
if (!sessionStorage.getItem('isAlive')) {
  clearLocalData();
}
// 设置本会话标记
sessionStorage.setItem('isAlive', '1');

onMounted(() => {
  // 这里不需要再监听 unload 或 visibilitychange
});
onUnmounted(() => {
  // 这里也不需要移除监听
});
</script>

<style lang="scss" scoped>

</style>
