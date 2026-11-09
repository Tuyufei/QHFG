<template>
  <aside class="sidebar">
    <div class="themeTitle">{{ $t(`message.Home.theme2`) }}</div>
    <ul class="menu-list">
      <li v-for="item in themes" :key="item.id">
        <router-link :to="item.path" class="menu-item">
          <h3>{{ $t(`theme2.paper${item.id}.title`) }}</h3>
          <p class="time">{{ item.time }}</p>
        </router-link>
      </li>
    </ul>
  </aside>
</template>
<script setup>
import { useI18n } from "vue-i18n";
const { t, locale } = useI18n();
const themes = [
  { id: 1, time: "2024-04-23", path: "/home/theme2/paper1" },
  { id: 2, time: "2024-04-23", path: "/home/theme2/paper2" },
  { id: 3, time: "2024-01-11", path: "/home/theme2/paper3" },
  { id: 4, time: "2023-10-12", path: "/home/theme2/paper4" },
];
</script>
<style lang="scss" scoped>
.sidebar {
  width: 300px;
  min-height: 100vh;
  // background-color: #f3f4f6; /* 移除背景色 */
  padding: 20px;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  .themeTitle {
    font-size: 24px; /* 调整标题字体大小 */
    color: #007bff; /* 调整标题颜色 */
    margin-bottom: 20px; /* 增加标题与列表的间距 */
  }

  .menu-list {
    list-style: none;
    padding: 0;
    margin: 0;
    .menu-item {
      display: block;
      margin-bottom: 15px; /* 调整底部间距 */
      padding: 10px 15px; /* 增加内边距 */
      border-radius: 8px; /* 增加圆角 */
      text-decoration: none;
      color: #333;
      transition: all 0.3s ease; /* 添加过渡效果 */
      background-color: #ffffff; /* 添加背景色 */
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05); /* 添加轻微阴影 */

      h3 {
        margin: 0;
        font-size: 16px; /* 调整字体大小 */
        color: #007bff; /* 调整标题颜色 */
      }

      .time {
        font-size: 14px; /* 调整时间字体大小 */
        color: #666; /* 调整时间颜色 */
        margin-top: 5px; /* 增加时间与标题的间距 */
      }

      &:hover {
        background-color: #e9f5ff; /* 鼠标悬停时的背景色 */
        color: #0056b3; /* 鼠标悬停时的字体颜色 */
        transform: translateY(-2px); /* 鼠标悬停时轻微上浮 */
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1); /* 鼠标悬停时阴影加深 */
      }
    }
  }
}
</style>
