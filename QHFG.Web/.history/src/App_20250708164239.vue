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

let unloadTimer = null;

function handleBeforeUnload(event) {
  // 设置一个延迟，只有关闭页面时才会执行清除
  unloadTimer = setTimeout(() => {
    // localStorage.removeItem('dataServiceResults_en');
    // localStorage.removeItem('dataServiceResults_zh');
    localStorage.clear();
    // localStorage.removeItem('leiyangUser');
  }, 50);
};

function handlePageShow() {
  // 如果是刷新/跳转，pageshow会很快触发，清除定时器，localStorage不会被清空
  if (unloadTimer) {
    clearTimeout(unloadTimer);
    unloadTimer = null;
  }
};

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload);
  window.addEventListener('pageshow', handlePageShow);
});
onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload);
  window.removeEventListener('pageshow', handlePageShow);
});
</script>

<style lang="scss" scoped>

</style>
