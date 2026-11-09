<template>
  <div class="layout">
    <themeAside1 />
    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import themeAside1 from "./theme1/themeAside1.vue";
</script>

<style lang="scss" scoped>
a {
  color: #333; /* 调整字体颜色 */
  font-size: 24px; /* 增大字体 */
  text-align: center; /* 文本居中 */
}
.layout {
  display: flex;
  min-height: 100vh;

  .content {
    margin-left: 300px;  // 与sidebar宽度相同
    padding: 20px;
    min-height: 100vh;
    background-color: #ffffff;
    flex: 1;
    justify-content: center; /* 水平居中内容 */
    align-items: center; /* 垂直居中内容 */
    font-size: 24px; /* 增大字体 */
    color: #333; /* 调整字体颜色 */
    box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05); /* 添加内部阴影 */
    .title,
    .subtitle {
      text-align: center; /* 文本居中 */
    }
    .subtitle {
      font-size: 18px; /* 调整副标题字体大小 */
      margin-bottom: 20px; /* 增加副标题与描述之间的间距 */
    }
    .image {
      // margin: 0 auto;
      width: 100%;
      height: auto;
    }
  }
}
</style>
