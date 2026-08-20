(function(){
  try {
    if(!document.createElement('canvas').toDataURL('image/webp').includes('webp'))return;
  } catch(e) { return; }
  document.querySelectorAll('img:not([data-wp])').forEach(function(img){
    var src=img.src;
    if(!src||src.indexOf('data:')===0)return;
    if(!src.match(/\.(jpg|jpeg|png)$/i))return;
    var webp=src.replace(/\.(jpg|jpeg|png)$/i,'.webp');
    img.setAttribute('data-wp','1');
    var test=new Image();
    test.onload=function(){ img.src=webp; };
    test.onerror=function(){};
    test.src=webp;
  });
})();
