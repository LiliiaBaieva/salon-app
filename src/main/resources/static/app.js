function showMsg(id, text, ok) {
  var el = document.getElementById(id);
  el.textContent = text;
  el.className = 'msg ' + (ok ? 'ok' : 'err');
}

async function api(url, options) {
  var resp = await fetch(url, options);
  var text = await resp.text();
  var data = text ? JSON.parse(text) : null;
  if (!resp.ok) {
    throw new Error((data && data.message) || ('Помилка ' + resp.status));
  }
  return data;
}

function jsonOptions(method, body) {
  return { method: method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) };
}

async function loadServicesInto(selectId) {
  var services = await api('/api/services');
  var sel = document.getElementById(selectId);
  sel.innerHTML = '';
  services.forEach(function (s) {
    var o = document.createElement('option');
    o.value = s.id;
    o.textContent = s.name + ' (' + s.durationMin + ' хв, ' + s.price + ' грн)';
    sel.appendChild(o);
  });
  return services;
}

async function loadSlotsInto(selectId, serviceId, date, url, current) {
  var sel = document.getElementById(selectId);
  sel.innerHTML = '';
  if (!serviceId || !date) { return; }
  var slots = await api(url + (url.indexOf('?') >= 0 ? '&' : '?') + 'serviceId=' + serviceId + '&date=' + date);
  slots.forEach(function (t) {
    var o = document.createElement('option');
    o.value = t;
    o.textContent = t;
    sel.appendChild(o);
  });
  if (current && !slots.includes(current)) {
    var o2 = document.createElement('option');
    o2.value = current;
    o2.textContent = current;
    sel.insertBefore(o2, sel.firstChild);
  }
  if (current) { sel.value = current; }
}

function today() {
  var d = new Date();
  var m = String(d.getMonth() + 1).padStart(2, '0');
  var day = String(d.getDate()).padStart(2, '0');
  return d.getFullYear() + '-' + m + '-' + day;
}
