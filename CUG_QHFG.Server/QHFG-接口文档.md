# QHFG——接口文档

## 获取表格数据

### 请求样例

**请求url：**

129.211.174.149:18080/statistical/getexceldata?map=发电侧/风力发电/低空风电/陆上风电/潜力数据/6.5MW/发电潜力/10km&timeScale=月&year=2021&level=区域&language=chinese

**返回样例：**

```html
{
    "code": 200,
    "msg": null,
    "data": {
        "columns": [
            "区域",
            "Regions",
            "202101"
        ],
        "data": [
            {
                "区域": "东亚与太平洋地区",
                "Regions": "East Asia & Pacific",
                "202101": 78391.26
            },
            {
                "区域": "中东与北非地区",
                "Regions": "Middle East & North Africa",
                "202101": 36943.98
            },
```

### 参数说明

**请求路径：**

129.211.174.149:18080/statistical/getexceldata

- **参数：**

  - map：（必填）字符串类型，与请求地图时的MAP相关，但不需要到时间的层级！

    （比如请求地图时MAP为：发电侧/风力发电/低空风电/陆上风电/潜力数据/6_5MW/装机潜力/25km/年/2021.qgs。但这里的map只需要给到：发电侧/风力发电/低空风电/陆上风电/潜力数据/6_5MW/装机潜力/25km）

  - timeScale：（必填）字符串类型，**可选三个值（”年“、"月"、"小时"）**,表示所需要的时间尺度。

  - year：（必填）字符串类型，**当 timeScale 为 ”年“ 时，该参数值可以为空**，当 timeScale 为"小时"、"月" 时需要给出年份，例如："2021"

  - level：（必填）字符串类型，表示请求的空间尺度，可选值（"区域"、"次区域"、"国家"）（"省级"、"地级"、"县级"、”电网“）。

  - language：（必填）字符串类型，可选值chinese，english。表示请求的列表中只显示中文列名或者英文列名
  
    



## 下载表格数据（参数和上面获取表格数据一致）

### 请求样例

**请求url：**

129.211.174.149:18080/statistical/download?map=发电侧/风力发电/低空风电/陆上风电/潜力数据/6.5MW/发电潜力/10km/&timeScale=月&year=2021&level=区域&language=chinese

**返回样例：**

```html

```

### 参数说明

**请求路径：**

129.211.174.149:18080/statistical/download

- **参数：**

  - map：（必填）字符串类型，与请求地图时的MAP相关，但不需要到时间的层级！

    （比如请求地图时MAP为：发电侧/风力发电/低空风电/陆上风电/潜力数据/6_5MW/装机潜力/25km/年/2021.qgs。但这里的map只需要给到：发电侧/风力发电/低空风电/陆上风电/潜力数据/6_5MW/装机潜力/25km）

  - timeScale：（必填）字符串类型，**可选三个值（”年“、"月"、"小时"）**,表示所需要的时间尺度。

  - year：（必填）字符串类型，**当 timeScale 为 ”年“ 时，该参数值可以为空**，当 timeScale 为"小时"、"月" 时需要给出年份，例如："2021"

  - level：（必填）字符串类型，表示请求的空间尺度，可选值（"区域"、"次区域"、"国家"）（"省级"、"地级"、"县级"、”电网“）。
  
  - language：（必填）字符串类型，可选值chinese，english。表示请求的列表中只显示中文列名或者英文列名





## 获取边界

### 请求样例

**请求url：**

127.0.0.1:8080/map/getboundary?level=国家&area=中国,美国&class=boundary

不通过openlayes来请求时，需要加上参数&BBOX=-180,-90,180,90&WIDTH=2000&HEIGHT=1500

**openlayers使用样例：**

```html
        const wmsUrl = "http://127.0.0.1:8080/map/getboundary"; 	
			
				// 添加 WMS 图层
        new ol.layer.Tile({
            source: new ol.source.TileWMS({
                url: wmsUrl,
                params: {
                    "level":'国家',
                    "area" :'中国,美国',
                    "class" :'boundary',
                    'CRS': 'EPSG:4326' // 确保与 QGIS 工程坐标系匹配
                }
            })
        }),
```

### 参数说明

**请求路径：**

127.0.0.1:8080/map/getboundary

**参数：**

- class：（必填）字符串类型，可选值有两个（"boundary" 和 "ocean"）"boundary"表示请求的是陆上的边界， "ocean"表示请求的是海上的边界

- level：（必填）字符串类型，可选值为（"区域"、"次区域"、"国家"、"地区"），表示请求的是哪个空间尺度下的数据，

- area：（必填）字符串类型，表示请求的具体位置，level和area具有对照关系，参考统计数据。例如：当level为"区域"时，area的值可为（"北欧"、"西欧"、"南亚".......等等）；当level为"国家"时，area的值可为（"中国"、"法国"、"芬兰".......等等）。对照统计数据的表格。**area可以为多个，使用逗号（,）分隔**

  



# 获取统计数据

## 请求样例

**请求url**：

127.0.0.1:8080/statistical/getdata?map=1发电侧/风力发电/低空风电/陆上风电/潜力数据/6_5MW/装机潜力/25km/&timeScale=年&year=2021&isGlobal=true&level=区域&area=

**返回结果：**

```json
{
    "code": 200,
    "msg": null,
    "data": {
        "SeriesData": [
            {
                "data": [
                    249247.08
                ],
                "name": [
                    "东亚与太平洋地区",
                    "East Asia & Pacific"
                ]
            },
            {
                "data": [
                    126798.52
                ],
                "name": [
                    "中东与北非地区",
                    "Middle East & North Africa"
                ]
            },
            {
                "data": [
                    18.55
                ],
                "name": [
                    "其他地区与南极地区",
                    "Other Regions & Antarctic Regions"
                ]
            },
            {
                "data": [
                    0.0
                ],
                "name": [
                    "北美地区",
                    "North America"
                ]
            },
            {
                "data": [
                    55779.85
                ],
                "name": [
                    "南亚地区",
                    "South Asia"
                ]
            },
            {
                "data": [
                    0.0
                ],
                "name": [
                    "拉丁美洲与加勒比地区",
                    "Latin America & Caribbean"
                ]
            },
            {
                "data": [
                    212508.81
                ],
                "name": [
                    "撒哈拉以南非洲地区",
                    "Sub-Saharan Africa"
                ]
            },
            {
                "data": [
                    412913.72
                ],
                "name": [
                    "欧洲与中亚地区",
                    "Europe & Central Asia"
                ]
            }
        ],
        "xAxisData": [
            "Y2021"
        ]
    }
}
```

## 参数解释

**请求路径：**

127.0.0.1:8080/statistical/getdata

**参数：**

- map：（必填）字符串类型，与请求地图时的MAP相关，但不需要到时间的层级！

  （比如请求地图时MAP为：1发电侧/风力发电/低空风电/陆上风电/潜力数据/6_5MW/装机潜力/25km/年/2021.tif。但这里的map只需要给到：1发电侧/风力发电/低空风电/陆上风电/潜力数据/6_5MW/装机潜力/25km）

- timeScale：（必填）字符串类型，**可选三个值（”年“、"月"、"小时"）**,表示所需要的时间尺度。

- year：（必填）字符串类型，**当 timeScale 为 ”年“ 时，该参数值可以为空**，当 timeScale 为"小时"、"月" 时

  需要给出年份，例如："2021"

- isGlobal：（必填）布尔类型（true,false），当值为true时，表示请求的是全球的数据。反之则请求的是中国的数据。

- level：（必填）字符串类型，表示请求的空间尺度，当请求全球的数据时，可选三个值（"区域"、"次区域"、"国家"）。当请求中国的数据时，可选三个值（"省级"、"地级"、"县级"）。

- area：（必填）字符串类型，**值可以为空**，表示请求在上述参数下确定的所有的数据。**当值不为空时**，用于获取指定某个详细区域的数据。

  







## 获取地图上一个坐标点所对应的tif图的值

### 请求样例

**请求URL：**

127.0.0.1:8080/map/getpoint?LONGITUDE=16.1&LATITUDE=26.35&MAP=/io/data/供给侧数据/风力发电/低空风电/陆上风电/潜力数据/2.5MW/容量因子/10km/年/2022.qgs&LAYERS=2022.tif

**返回结果**：

```json
## 正确返回结果（有值时）
{
    "code": 200,
    "msg": null,
    "data": {
        "value": 0.470602 
    }
}

## 正确返回结果（无值时）
{
    "code": 200,
    "msg": null,
    "data": {
        "value": null
    }
}

## 参数类型不对
{
    "code": 0,
    "msg": "参数格式错误，longitude 和 latitude 必须为有效的数字",
    "data": null
}

## 参数缺失
{
    "code": 0,
    "msg": "参数缺失，请提供 longitude、latitude、map 和 layers 参数",
    "data": null
}
```

### 参数解释

**请求路径：**

127.0.0.1:8080/map/getpoint

**参数：**

- LONGITUDE：（必填）double类型，表示点的坐标经度坐
- LATITUDE：（必填）double类型，表示点的坐标纬度
- MAP：（必填）字符串类型，表示请求的图的存储路径（对应到.qgs文件）
- LAYERS：（必填）字符串类型，表示请求的图层（与map中.qgs文件同名的.tif文件）



##  获取图例

### 请求样例

**请求URL：**

127.0.0.1:8080/map/getlegend?MAP=/io/data/供给侧数据/风力发电/低空风电/陆上风电/潜力数据/2.5MW/容量因子/10km/年/2022.qgs&LAYER=2022.tif

**返回结果**：

```json
## 正确返回结果
{
    "code": 200,
    "msg": null,
    "data": "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"
}
```

### 参数解释

**请求路径：**

127.0.0.1:8080/map/getlegend

**参数：**

- MAP：（必填）字符串类型，表示请求的图的存储路径（对应到.qgs文件）
- LAYERS：（必填）字符串类型，表示请求的图层（与map中.qgs文件同名的.tif文件）
