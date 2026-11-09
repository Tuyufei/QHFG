<script setup>
import { onMounted, onUnmounted } from 'vue';
import bus from '@/utils/bus';

function handleClear() {
  // 清空表格数据的逻辑
  // 例如：tableData.value = [];
}

onMounted(() => {
  bus.on('clearSelected', handleClear);
});
onUnmounted(() => {
  bus.off('clearSelected', handleClear);
});
</script>
// ... 其他表格相关代码 ... 