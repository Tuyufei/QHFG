// 格式化字段名
export const formatFiledName = (dicList, fieldName, typeName) => {
  let fieldValue = "",
    dicItem;
  if (typeName) {
    dicItem = dicList.find(
      item => item.type == typeName && item.name == fieldName
    );
  } else {
    dicItem = dicList.find(item => item.name == fieldName);
  }
  if (dicItem) {
    fieldValue = dicItem.value;
  } else {
    fieldValue = fieldName;
  }
  return fieldValue;
};
