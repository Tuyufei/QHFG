<template>
  <aside class="sidebar">
    <div class="themeTitle">{{ $t(`message.Home.theme3`) }}</div>
    <ul class="menu-list">
      <li v-for="item in themes" :key="item.id">
        <router-link :to="item.path" class="menu-item" active-class="active">
          <h3>{{ $t("theme3.paper1.title") }}</h3>
          <p class="time">{{ item.time }}</p>
        </router-link>
      </li>
    </ul>
  </aside>
</template>
<script setup>
import { useI18n } from "vue-i18n";
const { t, locale } = useI18n();
const themes = [{ id: 1, time: "2024-05-07", path: "/home/theme3/paper1" }];
</script>
<style lang="scss" scoped>
.sidebar {
  position: fixed;         // 固定定位
  top: 10;                  // 距离窗口顶部为0
  left: 0;                 // 距离窗口左侧为0
  width: 300px;            // 固定宽度
  height: 100vh;           // 高度占满整个视口
  z-index: 100;            // 保证在内容区之上
  background: #f8f9fa;     // 建议加背景色
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  padding: 20px 0 0 0;     // 顶部内边距
  display: flex;
  flex-direction: column;

  .themeTitle {
    font-size: 24px;
    color: #007bff;
    margin-bottom: 20px;
    padding: 0 20px;
  }

  .menu-list {
    list-style: none;
    padding: 0 20px;
    margin: 0;
    overflow-y: auto;
    flex: 1;

    .menu-item {
      display: block;
      margin-bottom: 15px;
      padding: 10px 15px;
      border-radius: 8px;
      text-decoration: none;
      color: #333;
      transition: all 0.3s ease;
      background-color: #ffffff;
      box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);

      h3 {
        margin: 0;
        font-size: 16px;
        color: #007bff;
      }

      .time {
        font-size: 14px;
        color: #666;
        margin-top: 5px;
      }

      &:hover {
        background-color: #e9f5ff;
        color: #0056b3;
        transform: translateY(-2px);
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
      }

      &.active {
        background-color: #007bff;
        color: #ffffff;
        transform: translateY(-2px);
        box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);

        h3 {
          color: #ffffff;
        }

        .time {
          color: #ffffff;
        }
      }
    }
  }
}
</style>
