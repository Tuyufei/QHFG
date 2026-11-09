<template>
  <div>
    <!-- 语言切换按钮（以 el-dropdown 为例） -->
    <el-dropdown @command="changeLanguage">
      <span class="el-dropdown-link">
        切换语言<i class="el-icon-arrow-down el-icon--right"></i>
      </span>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command="zh">中文</el-dropdown-item>
        <el-dropdown-item command="en">English</el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
    <!-- 其它内容 -->
    <router-view></router-view>
  </div>
</template>

<script setup>
import { useI18n } from "vue-i18n";
import bus from "@/utils/bus";
const { locale } = useI18n();

const changeLanguage = (lang) => {
  locale.value = lang; // 切换语言
  bus.emit('restoreDataServiceLocalData'); // 强制刷新数据服务页面
};
</script>

<style lang="scss" scoped>

</style>
