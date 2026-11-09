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
    { id: 1, path: '/home/theme1/paper1', title: '屋顶光伏的动态气候价值与潜力' },
    { id: 2, path: '/home/theme1/paper2', title: '文章2标题' },
    { id: 3, path: '/home/theme1/paper3', title: '文章3标题' },
    { id: 4, path: '/home/theme1/paper4', title: '文章4标题' },
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