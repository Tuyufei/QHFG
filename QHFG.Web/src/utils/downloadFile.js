//获取blob
export function handleGetBlob(url) {
  // console.log(url)
  return new Promise(resolve => {
    const xhr = new XMLHttpRequest();
    xhr.open("GET", url, true);
    xhr.responseType = "blob";
    xhr.onload = () => {
      if (xhr.status === 200) {
        resolve(xhr.response);
      }
    };
    xhr.setRequestHeader("Cache-Control", "no-cache");
    xhr.send();
  });
}
/**
 * 下载入口
 */
export function downloadFile(url, name) {
  return new Promise((resolve, reject) => {
    handleGetBlob(url)
      .then(blob => {
        let fileName = url.split("/");
        let fileType = "";
        fileName = fileName[fileName.length - 1];

        fileType = fileName.split(".");
        fileType = fileType[fileType.length - 1];

        let saveName = `${name}.${fileType}`;
        if (name.indexOf(".") > -1) {
          saveName = name;
          const lastType = name.split(".")[name.split(".").length - 1];
          if (fileType && lastType !== fileType) {
            saveName = `${name}.${fileType}`;
          }
        }

        handleSaveAs(blob, saveName);
        resolve();
      })
      .catch(err => {
        console.log("err", err);
        reject(err);
        // this.$message.warning('下载错误');
      });
  });
}
/**
 * 下载文件并重新命名
 */
export function handleSaveAs(blob, filename) {
  if (window.navigator.msSaveOrOpenBlob) {
    navigator.msSaveBlob(blob, filename);
  } else {
    const link = document.createElement("a");
    const body = document.querySelector("body");
    link.href = window.URL.createObjectURL(blob);
    link.download = filename;
    // fix Firefox
    link.style.display = "none";
    body.appendChild(link);
    link.click();
    body.removeChild(link);
    window.URL.revokeObjectURL(link.href);
  }
}
