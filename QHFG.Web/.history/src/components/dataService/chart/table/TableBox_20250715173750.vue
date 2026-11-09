<template>
  <div class="table_box gray-text">
    <div class="table">
      <el-table
        :data="tableData"
        border
        class="table"
        :header-cell-style="{ color: '#000' }"
        :cell-style="{ color: '#000' }"
        empty-text=""
      >
        <el-table-column
          v-for="(col, index) in columns"
          :key="index"
          :prop="col.prop"
          :label="col.label"
          :width="col.width || 'auto'"
        />
      </el-table>

      <!-- 无数据提示 -->
      <!-- <el-empty
        v-if="tableData.length > 0 && columns.length > 0"
        v-else
        :description="$t('message.noData')"
        class="noTableData"
      /> -->
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from "vue";
import bus from "@/utils/bus";
import { getExcelData } from "@/request/index";

const columns = ref([]);
const tableData = ref([]);

// 接收 ChartBox 文件传来的地图数据
const tableParams = ref({});
const handleTableParams = (data) => {
  console.log("handleTableParams 被调用");
  tableParams.value = data;
  getExcel();
};
// 接收 结果列表文件传来的初始地图数据
const handleExcelDataParams = (data) => {
  console.log("TableBox收到excelDataParams:", data);
  tableParams.value = data;
  getExcel();
};
const handleLanguage = (data) => {
  console.log("handleLanguage", data);
  tableData.value = [];
  columns.value = [];
};
const handleClear = (data) => {
  tableData.value = data;
  columns.value = [];
  tableData.value = [];
};
// 获取数据并格式化列结构
const getExcel = async () => {
  console.log("getExcel请求参数:", tableParams.value);
  console.log("getExcelData返回:", res);
  const res = await getExcelData(...Object.values(tableParams.value));
  console.log("###",res);
  // ✅ 转换为带 label/prop 的列结构
  columns.value = res.columns.map((col) => ({
    label: col,
    prop: col,
  }));
  tableData.value = res.data;
};


// 初始加载
onMounted(() => {
  console.log("TableBox.vue onMounted 被调用");
  bus.on("tableParams", handleTableParams);
  bus.on("clearSelected", handleClear);
  bus.on("excelDataParams", handleExcelDataParams);
  bus.on("changeLanguage", handleLanguage);
});
onUnmounted(() => {
  console.log("TableBox.vue onUnmounted 被调用");
  bus.off("tableParams");
  bus.off("clearSelected");
  bus.off("excelDataParams");
  bus.off("changeLanguage");
  bus.off("clearSelected", handleClear);
});
</script>

<style scoped lang="scss">
.gray-text {
  color: black;
  font-size: 14px;
}

.table_box {
  width: 100%;
  height: 40%;
  margin-top: 10px;
  background-color: transparent;
  // background: url("../../assets/img/login.png") no-repeat center;
  background-size: cover;

  .table {
    width: 95%;
    height: 100%;
    margin: 0 auto;
    background-color: transparent;
    // background: url("../../assets/img/login.png") no-repeat center;
    background-size: cover;

    :deep(.el-table) {
      background-color: rgba(0, 0, 0, 0.01) !important; // 从0.4调整为0.3
      --el-table-tr-bg-color: transparent !important; /* 行的背景透明 */
      backdrop-filter: blur(4px); /* 可选：毛玻璃效果 */
    }

    :deep(.el-table__header-wrapper),
    :deep(.el-table__body-wrapper),
    :deep(.el-table__header),
    :deep(.el-table__row),
    :deep(.el-table__cell),
    :deep(.el-table th),
    :deep(.el-table td),
    :deep(.el-table tr) {
      background-color: transparent !important; /* 确保所有子元素背景透明 */
    }

    /* 鼠标悬停时行背景色（加些透明） */
    :deep(.el-table__row:hover) {
      background-color: rgba(0, 0, 0, 0.05) !important;
    }
    :deep(.--el-table-border) {
      border: 1px solid rgb(85, 82, 82, 0.2) !important;
    }

    .noTableData {
      width: 95%;
      height: 100%;
      margin: 0 auto;
      border: 1px solid rgb(85, 82, 82, 0.2);
    }
  }
}
</style>
