// 拖拽
const moveModal = id => {
  let dragBox = document.getElementById(id);
  let isDragging = false;
  let currentX, currentY, initialX, initialY;
  function dragStart(e) {
    isDragging = true;
    initialX = e.clientX - dragBox.parentElement.offsetLeft;
    initialY = e.clientY - dragBox.parentElement.offsetTop;
    document.addEventListener("mousemove", drag);
  }

  function drag(e) {
    if (!isDragging) return;
    // e.preventDefault();
    currentX = e.clientX - initialX;
    currentY = e.clientY - initialY;
    dragBox.parentElement.style.left = currentX + "px";
    dragBox.parentElement.style.top = currentY + "px";
  }

  function dragEnd() {
    isDragging = false;
    document.removeEventListener("mousemove", drag);
    localStorage.setItem(
      id,
      JSON.stringify({
        left: dragBox.parentElement.style.left,
        top: dragBox.parentElement.style.top,
      })
    );
  }
  dragBox.addEventListener("mousedown", dragStart);
  document.addEventListener("mouseup", dragEnd);
};

export default moveModal;
