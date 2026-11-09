import * as Cesium from "cesium";
/**
 * 坐标系转换
 * @class CoordTransform
 */
class CoordTransform {
  constructor() {}
  static transformWGS84ToCartesian(viewer, position, alt) {
    if (viewer) {
      return position
        ? Cesium.Cartesian3.fromDegrees(
            position.lng || position.lon,
            position.lat,
            (position.alt = alt || position.alt),
            Cesium.Ellipsoid.WGS84
          )
        : Cesium.Cartesian3.ZERO;
    }
  }
  /**
   * 经纬度弧度转角度
   * @param {Cesium.Cartographic} cartographic 经纬度（弧度）
   * @returns {Object} 经纬度（角度）
   */
  static cartographicRadianToDegrees(cartographic) {
    if (cartographic instanceof Cesium.Cartographic) {
      return {
        longitude: Cesium.Math.toDegrees(cartographic.longitude),
        latitude: Cesium.Math.toDegrees(cartographic.latitude),
        height: cartographic.height,
      };
    }
  }
  /**
   * cartesian3转经纬度（角度）
   * @param {Cesium.Cartesian3} cartesian3 cartesian3
   * @returns {Cesium.Cartographic} 经纬度（角度）
   */
  static cartesian3ToCartographic(cartesian3) {
    if (cartesian3 instanceof Cesium.Cartesian3) {
      const cartographic = Cesium.Cartographic.fromCartesian(cartesian3);
      return this.cartographicRadianToDegrees(cartographic);
    }
  }
  /**
   * cartesian3数组转经纬度（角度）数组
   * @param {Cesium.Cartesian3} cartesian3Arr cartesian3数组
   * @returns {Cesium.Cartographic} 经纬度（角度）数组
   */
  static cartesian3ArrToCartographicArr(cartesian3Arr) {
    if (cartesian3Arr instanceof Array && cartesian3Arr.length > 0) {
      return cartesian3Arr.map(item => {
        const cartographic = Cesium.Cartographic.fromCartesian(item);
        return this.cartographicRadianToDegrees(cartographic);
      });
    }
  }
  /**
   * 经纬度（角度）转cartesian3
   * @param {Cesium.Cartographic} cartographic 经纬度（角度）
   * @returns {Cesium.Cartesian3} cartesian3
   */
  static cartographicToCartesian3(longitude, latitude, height) {
    return Cesium.Cartesian3.fromDegrees(longitude, latitude, height);
  }
  /**
   * 从屏幕坐标获取cartesian3
   * @param {Cesium.Viewer} viewer viewer
   * @param {Cesium.Cartesian2} px 屏幕坐标
   * @returns {Cesium.Cartesian3} Cartesian3
   */
  static getCartesian3FromPx(viewer, px) {
    if (viewer && px) {
      let picks = viewer.scene.drillPick(px);
      // var picks = null;
      let cartesian3 = null;
      let isOn3dtiles = false,
        isOnTerrain = false;
      for (let i in picks) {
        let pick = picks[i];
        if (
          (pick && pick.primitive instanceof Cesium.Cesium3DTileFeature) ||
          (pick && pick.primitive instanceof Cesium.Cesium3DTileset) ||
          (pick && pick.primitive instanceof Cesium.Model)
        ) {
          isOn3dtiles = true;
        }
        // 3dtilset
        if (isOn3dtiles) {
          viewer.scene.pick(px);
          cartesian3 = viewer.scene.pickPosition(px);
          if (cartesian3) {
            let cartographic = Cesium.Cartographic.fromCartesian(cartesian3);
            if (cartographic.height < 0) cartographic.height = 0;
            let lon = Cesium.Math.toDegrees(cartographic.longitude),
              lat = Cesium.Math.toDegrees(cartographic.latitude),
              height = cartographic.height;
            cartesian3 = this.cartographicToCartesian3(lon, lat, height);
          }
        }
      }
      let boolTerrain =
        viewer.terrainProvider instanceof Cesium.EllipsoidTerrainProvider;
      if (!isOn3dtiles && !boolTerrain) {
        var ray = viewer.scene.camera.getPickRay(px);
        if (!ray) return null;
        cartesian3 = viewer.scene.globe.pick(ray, viewer.scene);
        isOnTerrain = true;
      }
      if (!isOn3dtiles && !isOnTerrain && boolTerrain) {
        cartesian3 = viewer.scene.camera.pickEllipsoid(
          px,
          viewer.scene.globe.ellipsoid
        );
      }
      if (cartesian3) {
        // let position = this.transformCartesianToWGS84(viewer, cartesian);
        const position = this.cartesian3ToCartographic(cartesian3);
        if (position.height < 0) {
          const { longitude, latitude } = position;
          cartesian3 = this.cartographicToCartesian3(longitude, latitude, 0.1);
        }
        return cartesian3;
      }
      return false;
    }
  }
  /**
   * 从屏幕坐标获取经纬度（角度）
   * @param {Cesium.Viewer} viewer viewer
   * @param {Cesium.Cartesian2} px 屏幕坐标
   * @returns {Cesium.Cartographic} 经纬度（角度）
   */
  static getCartographicFromPx(viewer, px) {
    return this.cartesian3ToCartographic(this.getCartesian3FromPx(viewer, px));
  }
}
export default CoordTransform;
