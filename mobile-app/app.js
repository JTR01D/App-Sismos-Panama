// Sismos Panamá - Lógica Móvil en Tiempo Real
const PANAMA_BBOX = {
  minLat: 6.5,
  maxLat: 10.3,
  minLon: -83.5,
  maxLon: -77.0
};

const PANAMA_CITIES = [
  { name: 'Ciudad de Panamá', lat: 8.9824, lon: -79.5199 },
  { name: 'David, Chiriquí', lat: 8.4274, lon: -82.4309 },
  { name: 'Puerto Armuelles', lat: 8.2825, lon: -82.8631 },
  { name: 'Santiago, Veraguas', lat: 8.1042, lon: -80.9717 },
  { name: 'Chitré, Herrera', lat: 7.9614, lon: -80.4289 },
  { name: 'Las Tablas, Los Santos', lat: 7.7667, lon: -80.2833 },
  { name: 'Tonosí, Los Santos', lat: 7.4042, lon: -80.4458 },
  { name: 'Colón', lat: 9.3598, lon: -79.9015 },
  { name: 'Penonomé, Coclé', lat: 8.5197, lon: -80.3556 },
  { name: 'Changuinola, Bocas', lat: 9.4300, lon: -82.5200 },
  { name: 'La Palma, Darién', lat: 8.4072, lon: -78.1417 }
];

// Fallas activas georreferenciadas de Panamá
const PANAMA_FAULTS = [
  {
    name: 'Zona de Fractura de Panamá (ZFP)',
    color: '#EF4444',
    coords: [[5.0, -82.8], [7.0, -82.8], [8.1, -82.8]],
    info: 'Régimen dextral transformante (>40 mm/año). Concentra el 68% de los sismos nacionales.'
  },
  {
    name: 'Falla Soná - Tonosí',
    color: '#F59E0B',
    coords: [[7.25, -80.4], [7.6, -80.8], [8.0, -81.2]],
    info: 'Falla transpresiva cortical de Azuero y Veraguas. Sismo 1913 y 2026.'
  },
  {
    name: 'Falla de Pedro Miguel (Canal)',
    color: '#3B82F6',
    coords: [[8.95, -79.60], [9.15, -79.66]],
    info: 'Falla cortical que cruza el Corte Culebra del Canal. Terremoto histórico de 1621.'
  },
  {
    name: 'Falla de Limón (Colón)',
    color: '#8B5CF6',
    coords: [[9.20, -79.90], [9.38, -79.80]],
    info: 'Conexión Caribe - Cuenca del Canal.'
  },
  {
    name: 'Cinturón Deformado Norte (CDNP)',
    color: '#EC4899',
    coords: [[9.3, -82.8], [9.6, -81.8], [9.6, -80.0], [9.5, -78.5]],
    info: 'Frente compresivo del Caribe panameño (Sismos 1882 y 1991 Bocas del Toro).'
  }
];

let map, markersLayer, faultsLayer;
let earthquakes = [];
let currentFilter = 'all';
let currentRegion = null;
let soundEnabled = true;
let knownEventIds = new Set();
let deferredPrompt = null;

// Inicialización de la aplicación
document.addEventListener('DOMContentLoaded', () => {
  initMap();
  setupEvents();
  fetchEarthquakes();

  // Polling automático cada 45 segundos
  setInterval(fetchEarthquakes, 45000);

  // Registro de Service Worker
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('sw.js').catch(console.warn);
  }
});

// PWA Install prompt listener
window.addEventListener('beforeinstallprompt', (e) => {
  e.preventDefault();
  deferredPrompt = e;
  const banner = document.getElementById('install-banner');
  if (banner) banner.style.display = 'flex';
});

document.getElementById('btn-install')?.addEventListener('click', async () => {
  if (deferredPrompt) {
    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    if (outcome === 'accepted') {
      document.getElementById('install-banner').style.display = 'none';
    }
    deferredPrompt = null;
  }
});

function initMap() {
  map = L.map('map', {
    zoomControl: false,
    attributionControl: false
  }).setView([8.4, -80.5], 7);

  // CartoDB Dark Matter tiles
  L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
    maxZoom: 18,
    subdomains: 'abcd'
  }).addTo(map);

  markersLayer = L.layerGroup().addTo(map);
  faultsLayer = L.layerGroup().addTo(map);

  drawFaultLines();
}

function drawFaultLines() {
  PANAMA_FAULTS.forEach(fault => {
    const polyline = L.polyline(fault.coords, {
      color: fault.color,
      weight: 3,
      opacity: 0.8,
      dashArray: '6, 6'
    }).addTo(faultsLayer);

    polyline.bindPopup(`<b>${fault.name}</b><br><small>${fault.info}</small>`);
  });
}

function setupEvents() {
  document.getElementById('btn-refresh').addEventListener('click', () => {
    fetchEarthquakes();
  });

  const btnSound = document.getElementById('btn-sound');
  btnSound.addEventListener('click', () => {
    soundEnabled = !soundEnabled;
    btnSound.textContent = soundEnabled ? '🔔' : '🔕';
  });

  // Filter chips
  document.querySelectorAll('.chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');

      const filterMag = chip.getAttribute('data-filter');
      const filterReg = chip.getAttribute('data-region');

      if (filterMag) {
        currentFilter = filterMag;
        currentRegion = null;
      } else if (filterReg) {
        currentFilter = 'region';
        currentRegion = filterReg;
      }

      renderFeed();
      renderMarkers();
    });
  });
}

async function fetchEarthquakes() {
  try {
    const url = `https://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&minlatitude=${PANAMA_BBOX.minLat}&maxlatitude=${PANAMA_BBOX.maxLat}&minlongitude=${PANAMA_BBOX.minLon}&maxlongitude=${PANAMA_BBOX.maxLon}&orderby=time&limit=50`;
    const res = await fetch(url);
    const data = await res.json();

    const newQuakes = (data.features || []).map(feat => {
      const [lon, lat, depth] = feat.geometry.coordinates;
      const mag = feat.properties.mag || 0;
      const time = feat.properties.time;
      const originalPlace = feat.properties.place || 'Panamá';

      const nearestCity = getNearestPanamaCity(lat, lon);
      const nearestFault = getNearestFault(lat, lon);

      return {
        id: feat.id,
        mag: mag,
        lat: lat,
        lon: lon,
        depth: depth || 10,
        time: time,
        place: cleanPlaceName(originalPlace, nearestCity),
        nearestCity: nearestCity.name,
        distToCity: nearestCity.dist,
        fault: nearestFault.name,
        faultInfo: nearestFault.info,
        felt: feat.properties.felt || 0,
        tsunami: feat.properties.tsunami || 0,
        url: feat.properties.url
      };
    }).filter(q => isInsidePanama(q.lat, q.lon, q.place));

    // Detección de nuevo evento para sonido y vibración
    if (earthquakes.length > 0 && newQuakes.length > 0) {
      const latest = newQuakes[0];
      if (!knownEventIds.has(latest.id)) {
        triggerAlert(latest);
      }
    }

    newQuakes.forEach(q => knownEventIds.add(q.id));
    earthquakes = newQuakes;

    renderFeed();
    renderMarkers();
  } catch (err) {
    console.error('Error fetching seismic data:', err);
  }
}

function isInsidePanama(lat, lon, place) {
  if (lat < PANAMA_BBOX.minLat || lat > PANAMA_BBOX.maxLat) return false;
  if (lon < PANAMA_BBOX.minLon || lon > PANAMA_BBOX.maxLon) return false;

  const low = place.toLowerCase();
  if (low.includes('costa rica') && lon < -83.1 && lat > 8.5) return false;
  if (low.includes('colombia') && lon > -77.1 && lat < 7.8) return false;
  return true;
}

function getNearestPanamaCity(lat, lon) {
  let closest = PANAMA_CITIES[0];
  let minDist = Infinity;

  PANAMA_CITIES.forEach(c => {
    const d = haversine(lat, lon, c.lat, c.lon);
    if (d < minDist) {
      minDist = d;
      closest = c;
    }
  });
  return { name: closest.name, dist: Math.round(minDist) };
}

function getNearestFault(lat, lon) {
  let closest = PANAMA_FAULTS[0];
  let minDist = Infinity;

  PANAMA_FAULTS.forEach(f => {
    f.coords.forEach(([fLat, fLon]) => {
      const d = haversine(lat, lon, fLat, fLon);
      if (d < minDist) {
        minDist = d;
        closest = f;
      }
    });
  });
  return closest;
}

function cleanPlaceName(original, nearestCity) {
  if (original.includes('Panama')) {
    return original
      .replace('Panama', 'Panamá')
      .replace('km of', 'km de')
      .replace('km N of', 'km al N de')
      .replace('km S of', 'km al S de')
      .replace('km E of', 'km al E de')
      .replace('km W of', 'km al O de')
      .replace('km NE of', 'km al NE de')
      .replace('km NW of', 'km al NO de')
      .replace('km SE of', 'km al SE de')
      .replace('km SW of', 'km al SO de')
      .replace('km SSW of', 'km al SSO de')
      .replace('km WNW of', 'km al ONO de')
      .replace('km ESE of', 'km al ESE de');
  }
  return `A ${nearestCity.dist} km de ${nearestCity.name}`;
}

function haversine(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat/2) * Math.sin(dLat/2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}

function getMagClass(mag) {
  if (mag >= 6.0) return 'mag-red';
  if (mag >= 5.0) return 'mag-orange';
  if (mag >= 4.0) return 'mag-yellow';
  return 'mag-green';
}

function getMagColor(mag) {
  if (mag >= 6.0) return '#EF4444';
  if (mag >= 5.0) return '#F97316';
  if (mag >= 4.0) return '#FBBF24';
  return '#10B981';
}

function timeAgo(ms) {
  const diff = Date.now() - ms;
  const min = Math.floor(diff / 60000);
  if (min < 1) return 'Hace unos instantes';
  if (min < 60) return `Hace ${min} min`;
  const hr = Math.floor(min / 60);
  if (hr < 24) return `Hace ${hr} ${hr === 1 ? 'hora' : 'horas'}`;
  const d = Math.floor(hr / 24);
  return `Hace ${d} ${d === 1 ? 'día' : 'días'}`;
}

function renderFeed() {
  const container = document.getElementById('feed-container');
  const filtered = filterList(earthquakes);

  if (filtered.length === 0) {
    container.innerHTML = `<div style="text-align:center; padding: 40px; color: var(--text-muted);">No hay eventos registrados para el filtro seleccionado.</div>`;
    return;
  }

  container.innerHTML = filtered.map(q => `
    <div class="quake-card" onclick="openDetail('${q.id}')">
      <div class="mag-badge ${getMagClass(q.mag)}">${q.mag.toFixed(1)}</div>
      <div class="quake-info">
        <div class="quake-title">${q.place}</div>
        <div class="quake-time">${timeAgo(q.time)} • ${new Date(q.time).toLocaleTimeString('es-PA')}</div>
        <div class="quake-meta">
          <span class="meta-tag">Prof: <b>${Math.round(q.depth)} km</b></span>
          <span class="meta-tag">Falla: <b>${q.fault.slice(0, 18)}</b></span>
          ${q.tsunami ? '<span class="meta-tag" style="color:#ef4444; border-color:#ef4444;">⚠️ Tsunami</span>' : ''}
        </div>
      </div>
    </div>
  `).join('');
}

function renderMarkers() {
  markersLayer.clearLayers();
  const filtered = filterList(earthquakes);

  filtered.forEach(q => {
    const marker = L.circleMarker([q.lat, q.lon], {
      radius: Math.max(6, q.mag * 3.5),
      fillColor: getMagColor(q.mag),
      color: '#fff',
      weight: 1.5,
      opacity: 0.9,
      fillOpacity: 0.75
    }).addTo(markersLayer);

    marker.bindPopup(`
      <div style="font-family:sans-serif; color:#0f172a;">
        <b style="font-size:14px; color:${getMagColor(q.mag)}">Magnitud ${q.mag.toFixed(1)} Mw</b><br>
        <span>${q.place}</span><br>
        <small>Profundidad: ${Math.round(q.depth)} km</small><br>
        <small>Falla: ${q.fault}</small>
      </div>
    `);

    marker.on('click', () => {
      openDetail(q.id);
    });
  });
}

function filterList(list) {
  if (currentFilter === 'all') return list;
  if (currentFilter === '3') return list.filter(q => q.mag >= 3.0);
  if (currentFilter === '4') return list.filter(q => q.mag >= 4.0);
  if (currentFilter === '5') return list.filter(q => q.mag >= 5.0);
  if (currentFilter === 'region' && currentRegion) {
    return list.filter(q => q.place.toLowerCase().includes(currentRegion.toLowerCase()) || q.nearestCity.toLowerCase().includes(currentRegion.toLowerCase()));
  }
  return list;
}

window.openDetail = function(id) {
  const q = earthquakes.find(item => item.id === id);
  if (!q) return;

  const content = document.getElementById('modal-content');
  content.innerHTML = `
    <div style="display:flex; align-items:center; gap:14px; margin-bottom:12px;">
      <div class="mag-badge ${getMagClass(q.mag)}">${q.mag.toFixed(1)}</div>
      <div>
        <h2 style="font-size:18px; margin:0;">Magnitud ${q.mag.toFixed(1)} Mw</h2>
        <p style="color:var(--text-muted); font-size:12px; margin:2px 0 0;">${timeAgo(q.time)} • ${new Date(q.time).toLocaleString('es-PA')}</p>
      </div>
    </div>

    <p style="font-size:15px; font-weight:600; margin-bottom:12px;">${q.place}</p>

    <div class="detail-grid">
      <div class="detail-box">
        <div class="lbl">PROFUNDIDAD</div>
        <div class="val">${Math.round(q.depth)} km (${q.depth < 25 ? 'Somero' : 'Profundo'})</div>
      </div>
      <div class="detail-box">
        <div class="lbl">CIUDAD CERCANA</div>
        <div class="val">${q.nearestCity} (~${q.distToCity} km)</div>
      </div>
      <div class="detail-box">
        <div class="lbl">COORDINADAS</div>
        <div class="val">${q.lat.toFixed(3)}°N, ${q.lon.toFixed(3)}°O</div>
      </div>
      <div class="detail-box">
        <div class="lbl">REPORTE OFICIAL</div>
        <div class="val">Red Sísmica Panamá</div>
      </div>
    </div>

    <div class="detail-box" style="margin-bottom:16px;">
      <div class="lbl">SISTEMA DE FALLA ASOCIADO</div>
      <div class="val" style="color:var(--primary);">${q.fault}</div>
      <p style="font-size:11px; color:var(--text-muted); margin-top:4px;">${q.faultInfo}</p>
    </div>

    <a href="${q.url}" target="_blank" class="btn-action">Ver Boletín Oficial Completo</a>
  `;

  document.getElementById('modal-overlay').classList.add('open');
};

window.closeModal = function() {
  document.getElementById('modal-overlay').classList.remove('open');
};

function triggerAlert(event) {
  // Vibración en el teléfono Android
  if (navigator.vibrate) {
    navigator.vibrate([200, 100, 200]);
  }

  // Alerta de audio
  if (soundEnabled) {
    try {
      const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, audioCtx.currentTime); // Nota D5
      gain.gain.setValueAtTime(0.3, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.8);
      osc.connect(gain);
      gain.connect(audioCtx.destination);
      osc.start();
      osc.stop(audioCtx.currentTime + 0.8);
    } catch (e) {}
  }
}
