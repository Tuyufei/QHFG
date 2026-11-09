<template>
  <aside class="sidebar">
    <div class="themeTitle">{{ $t(`message.Home.theme2`) }}</div>
    <ul class="menu-list">
      <li v-for="item in themes" :key="item.id">
        <router-link :to="item.path" class="menu-item" active-class="active">
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
  position: fixed;
  top: 10;
  left: 0;
  width: 300px;
  height: 100vh;
  z-index: 100;
  background: #f8f9fa;
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  padding: 20px 0 0 0;
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
