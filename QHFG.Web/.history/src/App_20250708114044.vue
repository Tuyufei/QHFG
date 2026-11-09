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

function handleBeforeUnload() {
  localStorage.removeItem('dataServiceResults_en'); // 删除英文下的数据服务结果
  localStorage.removeItem('dataServiceResults_zh'); // 删除中文下的数据服务结果
};

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload);
});
onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload);
});
</script>

<style lang="scss" scoped>

</style>
