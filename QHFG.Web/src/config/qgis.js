// 0:本地localhost 1:本地局域网 2:外网43.138
let currentIP;
const getNetStatus = () => {
  currentIP = document.location.hostname;
  if (currentIP === "localhost") {
    return 0;
  } else if (currentIP.indexOf("43.138") === 0) {
    return 2;
  } else {
    return 1;
  }
};
export const NetStatus = getNetStatus();
export let ServerUrl = "http://localhost:8777";
if (NetStatus == 0) {
  ServerUrl = "http://localhost:8777"; //本机
} else if (NetStatus == 1) {
  ServerUrl = "http://" + currentIP + ":8777"; // 局域网
} else {
  ServerUrl = "http://43.138.35.113:7022"; // 外网（暂停使用）
}
// import { QGIS_Config } from '@/config/qgis';

export const QGIS_Config = {
  BaseUrl: ServerUrl + "/qgis-ltr/qgis_mapserv.fcgi.exe", // qgis服务地址
  MapPath: "C:/Users/dell/Desktop/QHFG项目代码/Data/MapData/gcepas.qgs", // qgs项目文件路径（替换为本地qgis项目路径）注：需要本地配置qgis服务
};
// 基础数据（一般是行政区划、地点标注等图层的名称或则ID）
export const BaseData = [
  // "XZQ",
  // "road",
  // "buildings",
  // "river",
];
