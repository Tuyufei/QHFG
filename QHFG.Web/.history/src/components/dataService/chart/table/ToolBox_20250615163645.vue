<template>
  <div class="tools_box2 gray-text">
    <el-button class="clear_button" @click="clearSelected">{{
      $t("chart.clear")
    }}</el-button>
    <!-- 文件下载，下载数据前需要同意用户使用协议 -->
    <el-button class="download_button" @click="open">{{
      $t("chart.download")
    }}</el-button>
    <!-- 数据来源i展开是简要的数据说明，以及对应数据的引用来源。 -->
    <el-button class="info" circle size="large" @click="showDetailInfo"
      >i</el-button
    >
    <!-- 控制展示的 el-card -->
    <el-card v-if="showCard" class="info-card" shadow="always">
      <template #header>
        <div class="clearfix">
          <span>{{ dataDescription }}</span>
        </div>
      </template>
      <div class="card-content">
        <p>
          {{ dataDescriptionDetail }}
        </p>
        <!-- <h4 style="padding-left: 10px">Data source:</h4>
        <p>
          Ember (2024); Energy Institute- Statistical Review of Worid Energy
          (2024)-Learn more about this data
        </p> -->
      </div>
    </el-card>
  </div>
</template>
<script setup>
import { ref, onMounted, onUnmounted, watch } from "vue";
import bus from "@/utils/bus";
import { getDownloadData, getDescription } from "@/request";
import { ElMessage, ElMessageBox } from "element-plus";
import { useI18n } from "vue-i18n";
import { NoneType } from "ol/expr/expression";
import { lengthToRadians } from "@turf/turf";

const open = () => {
  ElMessageBox.confirm(
    1,
    1,
    {
      confirmButtonText: '同意并下载',
      cancelButtonText: '取消',
      type: 'info',
      dangerouslyUseHTMLString: true,
      customClass: 'custom-message-box',
      center: true,
      showClose: true,
      closeOnClickModal: false,
      closeOnPressEscape: false,
      overflow: true
    }
  )
    .then(() => {
      // 点击OK时调用下载
      downloadExcel();
    })
    .catch(() => {
      ElMessage({
        type: 'info',
        message: '已取消下载',
      })
    })
}

const showAgreement = ref(false);

let dataDescription = "数据说明";
let dataDescriptionDetail = "数据来源说明";


// 控制 el-card 显隐
const showCard = ref(false);

const acceptAgreement = () => {
  showAgreement.value = false;
  // 调用下载函数
  downloadExcel();
};
const { t } = useI18n();
onMounted(() => {
  bus.on("excelDataParams", handleExcelDataParams);
});
onUnmounted(() => {
  bus.off("tableParams");
  bus.off("clearSelected");
  bus.off("excelDataParams");
});
const tableParams = ref({});
bus.on("tableParams", (params) => {
  tableParams.value = params;
});

const clearSelected = () => {
  tableParams.value = [];
  bus.emit("clearSelected", tableParams.value);
  showCard.value = false; // 清除后关闭信息卡片
};
const handleExcelDataParams = (params) => {
  tableParams.value = params;
};

//文件下载
const downloadExcel = async () => {
  try {
    if (tableParams.value.length == 0) {
      ElMessage.error(t("chart.noParamsSelected"));
      return;
    }
    //从服务器获取数据并下载为Excel文件
    const res = await getDownloadData(...Object.values(tableParams.value));
    console.log("res", res); // 先打印查看数据是否正确
    // 明确指定Blob的type
    const blob = new Blob([res], {
      type: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    });
    //创建下载链接并触发下载
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", "数据.xlsx");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    if (error.status == 403) {
      ElMessage.error(t("chart.hasNoPermission"));
      return;
    }
    console.error("下载失败：", error);
  }
};

function a(str1,str2){
  dataDescription = str1;
  dataDescriptionDetail = str2;
  console.log("@@:",dataDescription);
}

const showDetailInfo = () => {
  console.log("str1:",dataDescription);
  showCard.value = !showCard.value;
  console.log(showCard.value ? "展开" : "收起");
};
//接收DataService文件传的地图数据
const props = defineProps({
  selectMapPath: String,
});

// 监听 props.selectMapPath 变化  左侧
watch(
  () => props.selectMapPath,
  async (newPath) => {
    try {
      console.log("newPath:", newPath);
      if (newPath) {
        // 更新选中值
        let tmpMap = ""
        newPath = newPath.split('/').slice(0, -1).join('/')
        tmpMap = newPath.split('/').slice(0, -1).join('/')
        console.log("tmpMap:",tmpMap)
        const description_params = {
          map: tmpMap,
        }
        const description_res = await getDescription(description_params);
        if(description_res){
          console.log("description_res:",description_res);
          
          a(description_res.dataName,description_res.description);
          
        }
      } else {
        console.log("未选中地图数据")
      }
    } catch (error) {
      console.error("数据处理失败:", error);
      // 可以在这里添加错误处理逻辑，比如显示错误提示
    }
  }
  // { immediate: true } // 根据需求决定是否启用
);

</script>
<style scoped lang="scss">
.gray-text {
  color: black;
  font-size: 14px;
}
/* 覆盖所有 el-button 的字体背景颜色 */
:deep(.el-button) {
  color: black !important;
  background-color: rgba(255, 255, 255, 0.4) !important;
}
.custom-message-box .el-message-box {
  width: 600px; /* 设置弹窗宽度 */
}
/* 协议弹窗自定义样式 */
:deep(.agreement-messagebox) {
  /* 调节弹窗整体大小 */
  .el-message-box {
    width: 60% !important; /* 增加到95%屏幕宽度 */
    max-width: 14000px !important; /* 增加最大宽度到1400px */
    min-width: 10000px !important; /* 增加最小宽度到1000px */
  }
  
  .el-message-box-header {
    background-color: #f5f7fa;
    border-bottom: 1px solid #e4e7ed;
    padding: 15px 20px; /* 调节头部内边距 */
  }
  
  .el-message-box-title {
    color: #303133;
    font-weight: 600;
    font-size: 180px; /* 调节标题字体大小 */
  }
  
  .el-message-box-content {
    padding: 25px; /* 调节内容区域内边距 */
    max-height: 60vh; /* 高度占视窗高度的60% */
    min-height: 300px; /* 最小高度 */
    max-width: 95vw; /* 内容区域最大宽度95%视窗宽度 */
    min-width: 80vw; /* 内容区域最小宽度80%视窗宽度 */
    overflow-y: auto;
    line-height: 1.8; /* 调节行高 */
    font-size: 14px;
  }
  
  .el-message-box-btns {
    padding: 15px 25px 25px; /* 调节按钮区域内边距 */
  }
  
  .el-button--primary {
    background-color: #409EFF;
    border-color: #409EFF;
    padding: 10px 20px; /* 调节按钮大小 */
    font-size: 14px;
  }
  
  .el-button--default {
    background-color: #f5f7fa;
    border-color: #dcdfe6;
    color: #606266;
    padding: 10px 20px; /* 调节按钮大小 */
    font-size: 14px;
  }
}

/* 小尺寸弹窗样式 */
:deep(.small-messagebox) {
  .el-message-box {
    width: 50% !important;
    max-width: 500px !important;
    min-width: 400px !important;
  }
  
  .el-message-box__content {
    max-height: 40vh;
    min-height: 200px;
  }
}

/* 大尺寸弹窗样式 */
:deep(.large-messagebox) {
  .el-message-box {
    width: 90% !important;
    max-width: 1200px !important;
    min-width: 800px !important;
  }
  
  .el-message-box__content {
    max-height: 70vh;
    min-height: 400px;
  }
}

/* 全屏宽度弹窗样式 */
:deep(.full-width-messagebox) {
  .el-message-box {
    width: 98%; /* 几乎全屏宽度 */
    max-width: 1600px; /* 非常大的最大宽度 */
    min-width: 1200px; /* 较大的最小宽度 */
    margin: 0 auto;
  }
  .el-message-box-title {
    color: #303133;
    font-weight: 600;
    font-size: 180px; /* 调节标题字体大小 */
  }
  .el-message-box-content {
    max-width: 98vw; /* 内容区域几乎全屏宽度 */
    min-width: 90vw; /* 内容区域最小宽度90%视窗宽度 */
    padding: 30px; /* 增加内边距 */
  }
  
  .el-message-box-header {
    padding: 20px 30px; /* 增加头部内边距 */
  }
  
  .el-message-box-btns {
    padding: 20px 30px 30px; /* 增加按钮区域内边距 */
  }
}

.agreement-modal {
  position: fixed;
  top: 0;
  left: 0;
  // // width: 50vh;
  width: 100%;
  height: 85vh;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  margin: auto;
  z-index: 1000;
}
.agreement-content {
  // background: #fff;
  // padding: 60px;
  padding: 2rem;
  border-radius: 10px;
  width: 100%;
  height: 100%;
  // max-width: none;
  overflow-y: auto;
  font-size: 16px;
  background: rgba(255, 255, 255, 0.9 ); // 半透明白色背景
  backdrop-filter: blur(3px); // 背景模糊效果（毛玻璃效果）
  box-shadow: 0px 0px 0px 0px rgba(255, 255, 255, 0.8); // 透明的阴影效果
}
.tools_box2 {
  position: relative; // 为 absolute 卡片提供定位参考
  display: flex;
  width: 100%;
  height: calc(20% - 60px);
  margin-top: 10px;

  .clear_button {
    width: 100px;
    height: 30px;
    line-height: 30px;
    margin-left: 50px;
  }

  .download_button {
    width: 100px;
    height: 30px;
    line-height: 30px;
    text-align: center;
    margin-left: 120px;
    border-radius: 4px;
  }

  .info {
    width: 20px;
    height: 20px;
    line-height: 20px;
    text-align: center;
    justify-content: center;
    margin-left: 20px;
    margin-top: 2px;
  }
  // 重点：覆盖 el-card 的默认背景
  :deep(.el-card) {
    background: rgb(255, 255, 255) !important;
  }
  .info-card {
    position: absolute;
    top: -310px;
    left: 50%;
    transform: translateX(-50%);
    width: 200px;
    height: 300px;
    z-index: 1000;
    padding: 0 !important; // 重点：取消 el-card 默认 padding
    background: rgb(255, 255, 255);

    :deep(.el-card__body) {
      padding: 0;
    }
    // // 重点：覆盖 el-card 的默认背景
    // :deep(.el-card) {
    //   background: rgba(255, 255, 255, 0.8) !important;
    // }

    .clearfix {
      text-align: center;
      margin: 2px;
      padding: 4px;
    }

    .card-content {
      height: 280px;
      overflow-y: auto;
      margin: 0;
      padding-bottom: 10px;
      box-sizing: border-box;
      /* 优化滚动条样式 */
      // &::-webkit-scrollbar {
      //   width: 6px;
      // }
      // &::-webkit-scrollbar-track {
      //   background: #f1f1f1;
      // }
      // &::-webkit-scrollbar-thumb {
      //   background: #888;
      //   border-radius: 3px;
      // }
      // &::-webkit-scrollbar-thumb:hover {
      //   background: #555;
      // }
    }

    p {
      margin: 6px 10px;
    }
  }
}
</style>
