<template>
  <div class="layout">
    <themeAside2 />
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import themeAside2 from "./theme2/themeAside2.vue";
</script>

<style lang="scss" scoped>
.layout {
  display: flex;
  height: 100vh;
}

.content {
  flex: 1;
  margin-left: 300px;
  padding: 30px; /* 增加内边距 */
  background-color: #f8f9fa; /* 添加一个浅色背景 */
  display: flex;
  // justify-content: center; /* 水平居中内容 */
  // align-items: center; /* 垂直居中内容 */
  font-size: 24px; /* 增大字体 */
  color: #333; /* 调整字体颜色 */
  text-align: center; /* 文本居中 */
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05); /* 添加内部阴影 */
}
</style>
