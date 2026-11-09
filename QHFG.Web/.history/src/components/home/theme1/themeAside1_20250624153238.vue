<template>
  <aside class="sidebar">
    <div class="themeTitle">{{ $t(`message.Home.theme1`) }}</div>
    <ul class="menu-list">
      <li v-for="item in themes" :key="item.id">
        <router-link :to="item.path" class="menu-item">
          <h3>{{ $t(`theme1.paper${item.id}.title`) }}</h3>
          <p class="time">{{ item.time }}</p>
        </router-link>
      </li>
    </ul>
  </aside>
</template>
<script setup>
import { useI18n } from "vue-i18n";
const { t, locale } = useI18n();
// themeAside1.vue
const themes = [
  { id: 1, time: "2024-04-22", path: "/home/theme1/paper1", title: "屋顶光伏的动态气候价值与潜力" },
  { id: 2, time: "2023-02-10", path: "/home/theme1/paper2", title: "大规模山区风电场对京津冀地区冬季空气质量的影响" },
  { id: 3, time: "2021-10-19", path: "/home/theme1/paper3", title: "我国太阳能发电平价路径与并网潜力" },
  { id: 4, time: "2021-01-28", path: "/home/theme1/paper4", title: "我国陆上风电跨区域联合并网及开发优化布局" },
];
</script>
<style lang="scss" scoped>
.sidebar {
  // position: fixed; // 固定定位
  // top: 10; // 距离窗口顶部为0
  // left: 0; // 距离窗口左侧为0
  width: 300px; // 固定宽度
  height: 100vh; // 高度占满整个视口
  // z-index: 100; // 保证在内容区之上
  background: #f8f9fa; // 建议加背景色
  box-shadow: 2px 0 5px rgba(0, 0, 0, 0.1);
  padding: 20px 10px 0 10px; // 顶部内边距
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
    padding: 0 10px;
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
        word-wrap: break-word;
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
