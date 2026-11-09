import * as Cesium from "cesium";
import { QGIS_Config } from "@/config/qgis";
// 实体高亮
export const setHighLight = item => {
  let x = 1;
  let flog = true;
  let initColor = item.polygon.material.color._value;
  item.polygon.material = new Cesium.ColorMaterialProperty(
    new Cesium.CallbackProperty(() => {
      if (flog) {
        x = x - 0.05;
        if (x <= 0) {
          flog = false;
        }
      } else {
        x = x + 0.05;
        if (x >= 1) {
          flog = true;
        }
      }
      return Cesium.Color.RED.withAlpha(x);
    }, false)
  );
  setTimeout(() => {
    item.polygon.material = initColor;
  }, 3000);
};

// 加载WMS图层服务
// export const addWMSLayer = (viewer, options, success) => {
//   const wmsImageryProvider = new Cesium.WebMapServiceImageryProvider({
//     url: QGIS_Config.BaseUrl,
//     layers: options.layerId, //
//     parameters: {
//       transparent: true,
//       format: "image/png",
//       map: QGIS_Config.MapPath,
//       service: "WMS",
//       VERSION: "1.1.1",
//       REQUEST: "GetMap",
//     },
//     // rectangle: Cesium.Rectangle.fromDegrees(...options.extent),
//     rectangle: Cesium.Rectangle.fromDegrees(-180, -90, 180, 90),
//     crs: "EPSG:4326",
//     tilingScheme: new Cesium.GeographicTilingScheme({
//       numberOfLevelZeroTilesX: 2,
//       numberOfLevelZeroTilesY: 1,
//     }),
//     enablePickFeatures: options.enablePickFeatures,
//     getFeatureInfoParameters: {
//       map: QGIS_Config.MapPath,
//       format: "application/json",
//       service: "WMS",
//       version: "1.1.1",
//       request: "GetFeatureInfo",
//       info_format: "application/json",
//       // feature_count: 10000,
//       crs: "EPSG:4326",
//       query_layers: options.layerId,
//       with_geometry: true,
//     },
//     // getFeatureInfoFormats: [
//     //   new Cesium.GetFeatureInfoFormat("json", "application/json", e => {
//     //     console.log(e, "wms服务查询");
//     //   }),
//     // ],
//   });
//   const wmsLayer = viewer.imageryLayers.addImageryProvider(wmsImageryProvider);
//   wmsLayer.id = options.layerId;
//   // options.layerId.includes("XZQ") && viewer.imageryLayers.raiseToTop(wmsLayer);
//   success && success(wmsLayer);
// };
// 加载WMTS图层服务
export const addWMTSLayer = (viewer, options, success) => {
  const wmtsImageryProvider = new Cesium.WebMapTileServiceImageryProvider({
    url:
      QGIS_Config.BaseUrl +
      `?SERVICE=WMTS&REQUEST=GetTile&MAP=${QGIS_Config.MapPath}&LAYER=${options.layerId}&FORMAT=image/png&TileMatrixSet=EPSG:4326&TileMatrix={TileMatrix}&TileCol={TileCol}&TileRow={TileRow}`,
    layer: options.layerId,
    style: "",
    format: "image/png",
    tileMatrixSetID: "EPSG:4326",
    tilingScheme: new Cesium.GeographicTilingScheme({
      numberOfLevelZeroTilesX: 2,
      numberOfLevelZeroTilesY: 1,
    }),
    rectangle: Cesium.Rectangle.fromDegrees(-180, -90, 180, 90),
    minimumLevel: 0,
    maximumLevel: 7,
  });
  const wmtsLayer =
    viewer.imageryLayers.addImageryProvider(wmtsImageryProvider);
  wmtsLayer.id = options.layerId;
  success && success(wmtsLayer);
};
