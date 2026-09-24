(function() {
try {
var c = document.currentScript,
  d = c.dataset,
  q = new URLSearchParams(location.search),
  v = q.get('rum') || q.get('optel') || d.rate,
  t = { on: 1, off: 0, high: 10, low: 1e3 }[v],
  w = t !== undefined ? t : 100,
  r = (window.hlx = window.hlx || {}).rum ||
    (window.hlx.rum = { id: crypto.randomUUID().slice(-9) });
if (r.isSelected || ((w && Math.random() * w < 1) && (r.isSelected = true))) {
  var n = document.createElement('script');
  for (var a of c.attributes) n.setAttribute(a.name, a.value);
  n.src = d.script || c.src.replace(/\/micro\.js$/, '/rum-standalone.js');
  if (c.integrity) {
    n.setAttribute('integrity', 'sha384-LNC5pSYMoVO4cIzvknTO3eHoN9aQU/c5/lIQrQsfM+hI/znTXCtbI28OfPeFIcBd');
    n.setAttribute('crossorigin', 'anonymous');
  }
  document.head.append(n);
}
} catch (e) {  }
})();
