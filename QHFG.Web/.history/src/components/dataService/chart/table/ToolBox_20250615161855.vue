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
    `数据使用者（下称用户）在使用"可再生能源时空协同分析平台"提供的数据产品之前，请务必仔细阅读并同意本协议。用户使用直接从本平台获取或从其他
    途径获取的本平台数据的行为，都将被视作无条件接受本协议所涉及的全部内容。若用户对本协议的任何条款有异议，请停止使用本平台及平台数据。协议一经
    变动，将在相关页面提示修改内容；用户如不同意协议的修改版本，应放弃继续访问平台及使用本平台提供的任何数据、产品和服务；用户如在协议修改后继续
    访问平台或使用平台数据，则视为已经接受修改后的协议。<br><br>
    1."可再生能源时空协同分析平台"致力于提供高质量的风光水等资源开发潜力数据，我们不对任何因使用我们的平台而产生的法律风险负责，数据使用者不得
    使用从平台获取的数据资料从事危害国家安全、社会公共利益和他人合法权益的活动。平台对于数据使用者基于数据所得出的任何结果和言论不承担任何责任。<br><br>
    2.平台所监测数据受监测方式、自然环境、气候等情况会发生波动。因此，使用从本平台获取的数据所产生的不当后果由用户自行承担。<br><br>
    3.我们将尽力保护用户的隐私。但是，由于网络的开放性，我们无法保证用户的信息绝对安全。因此，用户在使用平台时，应谨慎处理自己的个人信息。<br><br>
    4.如果平台出现任何技术问题，我们将尽快修复。但是，我们不对由此造成的任何损失负责。<br><br>
    5.在会议报告、论文发表等各种方式使用本平台数据时，（1）需致谢"可再生能源时空协同分析平台"；（2）发表论文时需引用数据提供者的相关研究成果。具
    体引用方式请见相关数据说明。<br><br>
    6.本平台提供的数据仅供非商业用途使用，未经"可再生能源时空协同分析平台"书面授权之前，任何单位和个人不得将数据用于商业用途。商业机构使用本平台
    数据的行为均被视为商业用途，需事先获得本平台团队书面授权。申请将本平台数据用于商业用途或商业机构申请使用本平台数据请使用单位邮箱联系：xuchun@tsinghua.edu.cn。<br><br>
    7.本平台数据不得用于司法途径且平台不对由此产生的任何后果负责。<br><br>
    8.用户保证只将平台授权使用的数据资料用于本人的研究，不得私自有偿、无偿向第三方转让本平台数据及网站账号，用户应自行承担因违反此要求而导致的责
    任。本平台团队保留追究上述行为人法律责任的权利。<br><br>
    9.用户在使用数据过程中需保证数据的完整性和独立性。未经本平台团队允许，任何用户不得将从平台获得的数据以原始形式或修改后的形式用于其他数据产品
    或模型中，对于违反协议的数据使用者，平台有权随时要求其停止对数据的使用和保存。对于其所造成的任何形式的损害，有权进行法律追诉。对本协议的解释、
    修改及更新权均属于本平台团队所有。本平台数据产品会进行定期更新，请注册用户直接从平台下载，以获取最新版本信息。请用户妥善保管自己的账号、密码及
    其他注册资料，如果发现账号被盗用或者其他安全问题，请立即联系本平台工作人员进行处理。本平台将不承担由于用户自身原因导致的账号和密码泄露、丢失或
    其他安全问题所造成的损失。`,
    '可再生能源时空协同分析平台数据使用协议',
    {
      confirmButtonText: '同意并下载',
      cancelButtonText: '取消',
      type: 'info',
      dangerouslyUseHTMLString: true,
      customClass: 'agreement-messagebox',
      center: true,
      showClose: true,
      closeOnClickModal: false,
      closeOnPressEscape: false
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

/* 协议弹窗自定义样式 */
:deep(.agreement-messagebox) {
  /* 调节弹窗整体大小 */
  .el-message-box {
    width: 95% !important; /* 增加到95%屏幕宽度 */
    max-width: 1400px !important; /* 增加最大宽度到1400px */
    min-width: 1000px !important; /* 增加最小宽度到1000px */
  }
  
  .el-message-box-header {
    background-color: #f5f7fa;
    border-bottom: 1px solid #e4e7ed;
    padding: 15px 20px; /* 调节头部内边距 */
  }
  
  .el-message-box-title {
    color: #303133;
    font-weight: 600;
    font-size: 18px; /* 调节标题字体大小 */
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
    width: 98% !important; /* 几乎全屏宽度 */
    max-width: 1600px !important; /* 非常大的最大宽度 */
    min-width: 1200px !important; /* 较大的最小宽度 */
    margin: 0 auto !important;
  }
  
  .el-message-box__content {
    max-width: 98vw; /* 内容区域几乎全屏宽度 */
    min-width: 90vw; /* 内容区域最小宽度90%视窗宽度 */
    padding: 30px; /* 增加内边距 */
  }
  
  .el-message-box__header {
    padding: 20px 30px; /* 增加头部内边距 */
  }
  
  .el-message-box__btns {
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
