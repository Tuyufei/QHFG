# qgsMapPath 和 tifMapPath 数据流详解

## 📋 问题：这两个路径从哪里来？

**答案：** 它们是通过**用户选择条件 → 生成业务路径 → 根据时间格式拼接**生成的。

---

## 🔄 完整数据流程图

```
用户在 ConditionBox 中选择条件
    ↓
点击"确认"按钮
    ↓
ConditionBox.sendConfirm()
    ↓
生成业务路径（pathUrl）
    ↓
存储到 pathUrlList
    ↓
通过 emit 传递给 DataService
    ↓
存储到 mapPathUrlList
    ↓
用户选择结果项
    ↓
handleResultSelect()
    ↓
根据索引从 mapPathUrlList 获取路径
    ↓
根据时间格式生成 qgsMapPath 和 tifMapPath
    ↓
通过 props 传递给 Map2D/Map3D
```

---

## 📍 第一步：ConditionBox 生成业务路径

### 位置：`src/components/dataService/condition/ConditionBox.vue`

### 关键代码：`sendConfirm()` 函数（889-1003行）

```javascript
const sendConfirm = () => {
  // 1. 获取各层级的路径（通过递归算法）
  pathListResource.value = getPathByKey(
    selectedResourceId.value,
    layerListResourceData
  );
  // 结果：[
  //   {id: 1, name: "发电侧"},
  //   {id: 2, name: "风力发电"},
  //   {id: 3, name: "低空风电"},
  //   {id: 4, name: "陆上风电"}
  // ]

  pathListDataType.value = getPathByKey(
    selectedDataTypeId.value,
    layerListTypeData
  );
  // 结果：[
  //   {id: 5, name: "潜力数据"},
  //   {id: 6, name: "容量因子"}
  // ]

  pathListChild.value = getPathByKey(
    selectedChildId.value,
    layerListChildData
  );
  // 结果：[
  //   {id: 7, name: "2.5MW"},
  //   {id: 8, name: "10km"}
  // ]

  pathListTime.value = getPathByKey(selectedTimeId.value, layerListTimeData);
  // 结果：[
  //   {id: 9, name: "年"}
  // ]

  // 2. 组合完整业务路径
  pathUrl.value =
    pathListResource.value.map((item) => item.name).join("/") +
    "/" +
    pathListDataType.value.map((item) => item.name).join("/") +
    "/" +
    pathListChild.value.map((item) => item.name).join("/") +
    "/" +
    pathListTime.value.map((item) => item.name).join("/");
  
  // 结果：pathUrl.value = "发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/年"

  // 3. 根据时间结果生成多个路径（每个时间点一个路径）
  pathUrlList.value = [];
  if (timeResult.value !== t("conditionBox.annualAverage")) {
    // 普通时间：为每个时间点生成一个路径
    for (let i = 0; i < timeResult.value.length; i++) {
      pathUrlList.value.push(pathUrl.value);
    }
    // 例如：timeResult = ["2021", "2022", "2023"]
    // pathUrlList = [
    //   "发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/年",
    //   "发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/年",
    //   "发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/年"
    // ]
  } else {
    // 多年平均：只有一个路径
    pathUrlList.value.push(pathUrl.value);
  }

  // 4. 通过 emit 传递给父组件
  emitResultList.value = [
    [...filterResultList.value],      // 结果名称列表
    [...selectedResults.value],        // 选中状态列表
    [...timeResult.value],             // 时间列表
    [...pathUrlList.value],            // ⭐ 业务路径列表（关键！）
    [...dataUnitList.value],           // 单位列表
    [...dataTypeThreeList.value],      // 数据类型列表
  ];
  
  emit("updateEmitResultList", emitResultList.value);
};
```

**关键点：**
- `pathUrlList` 存储的是**业务路径**（如：`"发电侧/风力发电/.../年"`）
- 这个路径**不包含时间信息**（如：`"2021"`）
- 时间信息存储在 `timeResult` 中

---

## 📍 第二步：DataService 接收并存储路径

### 位置：`src/components/dataService/DataService.vue`

### 关键代码：`updateEmitResultList()` 函数（178-204行）

```javascript
const updateEmitResultList = (data) => {
  emitResultList.value = data;
  
  // 将子组件传递的数据存储到本地数组
  for (let i = 0; i < emitResultList.value[0].length; i++) {
    if (!filterResultList.value.includes(emitResultList.value[0][i])) {
      filterResultList.value.push(emitResultList.value[0][i]);
      selectedResults.value.push(emitResultList.value[1][i]);
      timeResult.value.push(emitResultList.value[2][i]);
      mapPathUrlList.value.push(emitResultList.value[3][i]); // ⭐ 存储业务路径
      dataUnlitList.value.push(emitResultList.value[4][i]);
      dataTypeThreeList.value.push(emitResultList.value[5][i]);
    }
  }
  
  // 持久化到 localStorage
  localStorage.setItem(`dataServiceResults_${lang}`, JSON.stringify(saveObj));
};
```

**关键点：**
- `mapPathUrlList` 存储的是从 ConditionBox 接收的业务路径
- 每个路径对应一个时间点（通过索引关联）

---

## 📍 第三步：用户选择结果项，生成地图路径

### 位置：`src/components/dataService/DataService.vue`

### 关键代码：`handleResultSelect()` 函数（213-284行）

```javascript
const handleResultSelect = (checked, index) => {
  if (checked) {
    // 1. 获取时间信息
    layerUrl.value = timeResult.value[index];
    // 例如：layerUrl.value = "2021" 或 "2021-01-01 00:00:00"

    // 2. 根据时间格式生成地图路径
    let tempTime = "";
    
    if (layerUrl.value.includes("-")) {
      // 情况1：时间范围（小时数据）
      // 例如：layerUrl.value = "2021-01-01 00:00:00"
      
      if (layerUrl.value.length > 7) {
        // 处理完整时间格式
        tempTime = layerUrl.value.split(":")[0].replace(/[-\s]/g, "");
        // tempTime = "20210101"
        hourYear.value = tempTime.slice(0, 4);
        // hourYear = "2021"
      } else {
        // 处理简化时间格式
        tempTime = layerUrl.value.replace(/-/g, "");
        hourYear.value = tempTime.slice(0, 4);
      }
      
      // 生成地图路径
      qgsMapPath.value = `/io/data/${
        mapPathUrlList.value[index]  // 业务路径
      }/${layerUrl.value.substring(0, 4)}/${tempTime}.qgs`;
      // 结果：qgsMapPath = "/io/data/发电侧/风力发电/.../年/2021/20210101.qgs"
      
      tifMapPath.value = `${tempTime}.tif`;
      // 结果：tifMapPath = "20210101.tif"
      
    } else {
      // 情况2：普通时间（年/月）
      // 例如：layerUrl.value = "2021"
      
      qgsMapPath.value = `/io/data/${mapPathUrlList.value[index]}/${layerUrl.value}.qgs`;
      // 结果：qgsMapPath = "/io/data/发电侧/风力发电/.../年/2021.qgs"
      
      tifMapPath.value = `${layerUrl.value}.tif`;
      // 结果：tifMapPath = "2021.tif"
    }

    // 3. 通过 props 传递给地图组件
    // 在模板中：
    // <Map2D :qgsMapPath="qgsMapPath" :tifMapPath="tifMapPath" />
    // <Map3D :qgsMapPath="qgsMapPath" :tifMapPath="tifMapPath" />
  }
};
```

---

## 📊 完整示例

### 示例1：选择"年"时间粒度

**用户选择：**
- 资源：发电侧/风力发电/低空风电/陆上风电
- 数据类型：潜力数据/容量因子
- 空间分辨率：2.5MW/10km
- 时间分辨率：年
- 时间：2021

**生成过程：**

1. **ConditionBox 生成业务路径：**
   ```
   pathUrl = "发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/年"
   pathUrlList = ["发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/年"]
   timeResult = ["2021"]
   ```

2. **DataService 接收并存储：**
   ```
   mapPathUrlList[0] = "发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/年"
   timeResult[0] = "2021"
   ```

3. **用户选择结果项（index=0）：**
   ```
   layerUrl = timeResult[0] = "2021"
   // 不包含"-"，走 else 分支
   qgsMapPath = "/io/data/发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/年/2021.qgs"
   tifMapPath = "2021.tif"
   ```

---

### 示例2：选择"小时"时间粒度

**用户选择：**
- 资源：发电侧/风力发电/低空风电/陆上风电
- 数据类型：潜力数据/容量因子
- 空间分辨率：2.5MW/10km
- 时间分辨率：小时
- 时间范围：2021-01-01 00:00 到 2021-01-01 23:00

**生成过程：**

1. **ConditionBox 生成业务路径：**
   ```
   pathUrl = "发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/小时"
   pathUrlList = [
     "发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/小时",
     "发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/小时",
     ... (24个，每个小时一个)
   ]
   timeResult = [
     "2021-01-01 00:00:00",
     "2021-01-01 01:00:00",
     ... (24个)
   ]
   ```

2. **DataService 接收并存储：**
   ```
   mapPathUrlList[0] = "发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/小时"
   timeResult[0] = "2021-01-01 00:00:00"
   ```

3. **用户选择结果项（index=0）：**
   ```
   layerUrl = timeResult[0] = "2021-01-01 00:00:00"
   // 包含"-"，走 if 分支
   tempTime = "20210101"  // 去掉"-"和空格
   qgsMapPath = "/io/data/发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/小时/2021/20210101.qgs"
   tifMapPath = "20210101.tif"
   ```

---

## 🎯 关键理解点

### 1. 业务路径 vs 地图路径

**业务路径（mapPathUrlList）：**
- 格式：`"发电侧/风力发电/.../年"`
- 来源：ConditionBox 根据用户选择生成
- 用途：用于图表、表格等业务逻辑

**地图路径（qgsMapPath/tifMapPath）：**
- 格式：`"/io/data/发电侧/风力发电/.../年/2021.qgs"`
- 来源：根据业务路径 + 时间信息拼接生成
- 用途：传递给地图组件，加载WMS图层

### 2. 时间格式处理

**年/月时间：**
```javascript
// layerUrl = "2021"
qgsMapPath = `/io/data/${mapPathUrlList[index]}/${layerUrl}.qgs`
// 结果：/io/data/.../年/2021.qgs
```

**小时时间：**
```javascript
// layerUrl = "2021-01-01 00:00:00"
tempTime = "20210101"  // 去掉"-"和空格
qgsMapPath = `/io/data/${mapPathUrlList[index]}/${layerUrl.substring(0, 4)}/${tempTime}.qgs`
// 结果：/io/data/.../小时/2021/20210101.qgs
```

### 3. 路径结构

```
qgsMapPath = /io/data + 业务路径 + 年份目录 + 时间.qgs
            └─固定前缀  └─用户选择  └─时间提取  └─文件名

例如：
/io/data/发电侧/风力发电/低空风电/陆上风电/潜力数据/容量因子/2.5MW/10km/年/2021.qgs
│        │───────────────────────────────────────────│  │    │
│        └─ 业务路径（mapPathUrlList）                │  │    └─ 时间（layerUrl）
│                                                     │  └─ 固定后缀
└─ 固定前缀                                           └─ 年份目录（小时数据才有）
```

---

## 📝 总结

**qgsMapPath 和 tifMapPath 的来源：**

1. **业务路径来源**：ConditionBox 根据用户选择的三级树形结构生成
2. **时间信息来源**：用户选择的时间分辨率（年/月/小时）
3. **最终路径生成**：在 `handleResultSelect` 中，根据业务路径和时间信息拼接生成
4. **传递方式**：通过 props 传递给 Map2D/Map3D 组件

**关键文件：**
- `ConditionBox.vue`：生成业务路径
- `DataService.vue`：接收路径，生成地图路径
- `Map2D.vue` / `Map3D.vue`：接收地图路径，加载WMS图层

---

**希望这个解释能帮助你理解完整的数据流！** 🚀

