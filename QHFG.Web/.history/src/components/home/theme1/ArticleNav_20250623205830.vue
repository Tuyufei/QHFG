<template>
    <div class="nav-btns">
      <div class="nav-info" v-if="showTitle">
        <span class="current-title">当前：{{ currentArticle?.title }}</span>
      </div>
      <div class="nav-buttons">
        <button 
          @click="goPrev" 
          :disabled="!hasPrev" 
          :title="prevArticle?.title"
        >
          <span class="arrow">←</span> 上一篇
        </button>
        <button 
          @click="goNext" 
          :disabled="!hasNext"
          :title="nextArticle?.title"
        >
          下一篇 <span class="arrow">→</span>
        </button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { useRouter, useRoute } from 'vue-router';
  import { computed } from 'vue';
  
  // 文章列表从父组件props传递，定义 props
 // 定义 props
const props = defineProps({
  // 文章列表
  articles: {
    type: Array,
    required: true,
    default: () => []
  },
  // 是否显示当前文章标题
  showTitle: {
    type: Boolean,
    default: true
  }
});
  
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