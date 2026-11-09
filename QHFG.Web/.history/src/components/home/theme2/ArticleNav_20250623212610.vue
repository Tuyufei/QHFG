<template>
    <div class="nav-btns">
      <button @click="goPrev" :disabled="!hasPrev">上一篇</button>
      <button @click="goNext" :disabled="!hasNext">下一篇</button>
    </div>
  </template>
  
  <script setup>
  import { useRouter, useRoute } from 'vue-router';
  import { computed } from 'vue';
  
  // 文章列表（可从父组件props传递，这里直接写死示例）
  const themes = [
    { id: 1, path: '/home/theme1/paper1', title: '中国碳中和目标下的风电时空碳足迹及相关成本' },
    { id: 2, path: '/home/theme1/paper2', title: '碳中和目标下可再生能源发展布局' },
    { id: 3, path: '/home/theme1/paper3', title: '中国碳中和目标下的风光技术展望' },
    { id: 4, path: '/home/theme1/paper4', title: '全球光伏产业链温室气体排放与减排的时空分布特征' },
  ];
  
  const router = useRouter();
  const route = useRoute();
  
  const currentIndex = computed(() => {
    return themes.findIndex(item => item.path === route.path);
  });
  
  const hasPrev = computed(() => currentIndex.value > 0);
  const hasNext = computed(() => currentIndex.value < themes.length - 1);
  
  function goPrev() {
    if (hasPrev.value) {
      router.push(themes[currentIndex.value - 1].path);
    }
  }
  function goNext() {
    if (hasNext.value) {
      router.push(themes[currentIndex.value + 1].path);
    }
  }
  </script>
  
  <style scoped>
  .nav-btns {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin: 40px 0 0 0;
  }
  button {
    padding: 8px 24px;
    border-radius: 6px;
    border: none;
    background: #007bff;
    color: #fff;
    font-size: 16px;
    cursor: pointer;
    transition: 0.2s;
  }
  button:disabled {
    background: #ccc;
    cursor: not-allowed;
  }
  </style>