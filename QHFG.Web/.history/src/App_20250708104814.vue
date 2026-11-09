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
  localStorage.clear();
};

onMounted(() => {
  window.addEventListener('beforeunload', handleBeforeUnload)
});
onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
});
</script>

<style lang="scss" scoped>

</style>
