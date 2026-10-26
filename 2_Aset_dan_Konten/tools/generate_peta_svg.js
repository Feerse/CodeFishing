#!/usr/bin/env node
/*
 * Membuat peta perairan vertikal (SVG) untuk layar Peta.
 * Jalankan dari root repo: node 2_Aset_dan_Konten/tools/generate_peta_svg.js
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..', '..');
const W = 1000;
const H = 2800;

const peta = [];
const add = (s) => peta.push(s);

function palm(x, y, k = 1) {
  return `<g transform="translate(${x} ${y}) scale(${k})">
    <path d="M0 0Q6 -24 2 -44" stroke="#8A5A33" stroke-width="5" fill="none" stroke-linecap="round"/>
    <path d="M2 -44Q-22 -58 -38 -40Q-18 -46 2 -44ZM2 -44Q26 -60 42 -42Q20 -48 2 -44ZM2 -44Q-8 -66 -22 -68Q-10 -56 2 -44ZM2 -44Q12 -66 28 -66Q14 -56 2 -44Z" fill="#2F8F55"/>
    <path d="M2 -44Q-14 -54 -26 -48" stroke="#23683F" stroke-width="2" fill="none" stroke-linecap="round"/>
  </g>`;
}

function island(x, y, k = 1) {
  return `<g transform="translate(${x} ${y}) scale(${k})">
    <ellipse cx="0" cy="20" rx="132" ry="42" fill="#7FC9E0" opacity=".13"/>
    <ellipse cx="0" cy="10" rx="112" ry="34" fill="#7A5C37"/>
    <ellipse cx="0" cy="0" rx="104" ry="29" fill="#B88F57"/>
    <path d="M-74 4Q-52 -34 -16 -30Q16 -56 54 -20Q82 -16 84 6Q40 22 -6 18Q-46 24 -74 4Z" fill="#2C7A53"/>
    <path d="M-34 -12Q-8 -42 24 -24Q42 -22 38 -6Q8 6 -26 2Q-42 2 -34 -12Z" fill="#23683F"/>
    ${palm(-26, -8, 1.05)}
    ${palm(24, -2, .8)}
  </g>`;
}

function iceberg(x, y, k = 1) {
  return `<g transform="translate(${x} ${y}) scale(${k})">
    <ellipse cx="0" cy="26" rx="92" ry="15" fill="#BFE6F2" opacity=".16"/>
    <path d="M-68 24L-48 -32L-14 -12L10 -58L50 -8L78 24Z" fill="#DCEEF5"/>
    <path d="M-68 24L-48 -32L-14 -12L-6 24Z" fill="#8DB7CA"/>
    <path d="M10 -58L50 -8L78 24L26 24Z" fill="#B5D4E2"/>
    <path d="M-14 -12L-2 24L-6 24Z" fill="#6E9BAF" opacity=".6"/>
  </g>`;
}

function rock(x, y, k = 1) {
  return `<g transform="translate(${x} ${y}) scale(${k})">
    <ellipse cx="0" cy="13" rx="44" ry="10" fill="#7FC9E0" opacity=".13"/>
    <path d="M-38 13L-24 -24L-7 -9L9 -40L33 13Z" fill="#5B6C77"/>
    <path d="M-38 13L-24 -24L-7 -9L-2 13Z" fill="#45545E"/>
  </g>`;
}

function fishShadow(x, y, r, k = 1) {
  return `<g transform="translate(${x} ${y}) rotate(${r}) scale(${k})" opacity=".32">
    <ellipse rx="24" ry="8" fill="#030E14"/>
    <path d="M20 0L37 -10V10Z" fill="#030E14"/>
  </g>`;
}

function wave(y, opacity, phase = 0) {
  let d = `M ${-120 + phase} ${y}`;
  for (let x = -120 + phase; x < W + 120; x += 150) {
    d += ` Q ${x + 38} ${y - 17} ${x + 75} ${y} T ${x + 150} ${y}`;
  }
  return `<path d="${d}" fill="none" stroke="#7FC9E0" stroke-opacity="${opacity}" stroke-width="2.1" stroke-linecap="round"/>`;
}

const stageTitik = [
  [220, 340], [430, 540], [720, 730], [820, 940], [600, 1120],
  [360, 1310], [230, 1490], [450, 1670], [700, 1860], [820, 2080],
];

add(`<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}" viewBox="0 0 ${W} ${H}">`);
add(`<defs>
  <linearGradient id="laut" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#123F55"/>
    <stop offset=".24" stop-color="#0D3448"/>
    <stop offset=".55" stop-color="#0B2F42"/>
    <stop offset=".78" stop-color="#0A2433"/>
    <stop offset="1" stop-color="#07171F"/>
  </linearGradient>
  <linearGradient id="es" x1="0" y1="0" x2="0" y2="1">
    <stop offset="0" stop-color="#EAF7FB"/>
    <stop offset="1" stop-color="#A9CDDB"/>
  </linearGradient>
  <radialGradient id="glow">
    <stop offset="0" stop-color="#7FC9E0" stop-opacity=".24"/>
    <stop offset="1" stop-color="#7FC9E0" stop-opacity="0"/>
  </radialGradient>
  <radialGradient id="vignette">
    <stop offset=".55" stop-color="#000000" stop-opacity="0"/>
    <stop offset="1" stop-color="#000000" stop-opacity=".46"/>
  </radialGradient>
</defs>`);

add(`<rect width="${W}" height="${H}" fill="url(#laut)"/>`);

// Cahaya permukaan dan arus laut.
add(`<path d="M0 0H1000V430Q760 380 540 430T0 390Z" fill="#8FD4E8" opacity=".05"/>`);
add(`<path d="M0 260Q150 215 310 255T640 250T1000 300" fill="none" stroke="#BFE6F2" stroke-opacity=".13" stroke-width="26"/>`);
add(`<path d="M0 720Q190 650 370 710T740 700T1000 750" fill="none" stroke="#7FC9E0" stroke-opacity=".05" stroke-width="36"/>`);

// Pola ombak dua lapis.
for (let y = 80; y < H; y += 145) add(wave(y, 0.105, (Math.round(y / 145) % 2) * 52));
for (let y = 150; y < H; y += 210) add(wave(y, 0.055, (Math.round(y / 210) % 2) * 76));

// Rhumb lines ringan seperti peta laut.
const pusatKompas = [812, 175];
for (let i = 0; i < 12; i++) {
  const a = (Math.PI * 2 * i) / 12;
  add(`<line x1="${pusatKompas[0]}" y1="${pusatKompas[1]}" x2="${pusatKompas[0] + Math.cos(a) * 1250}" y2="${pusatKompas[1] + Math.sin(a) * 1250}" stroke="#8FB2B8" stroke-opacity=".035" stroke-width="1.4"/>`);
}

// Bayangan ikan di bawah permukaan.
[
  [70, 610, -8, 1], [890, 760, 12, 1.1], [120, 1010, 6, .8], [900, 1280, -10, 1],
  [90, 1570, 14, .9], [870, 1760, 8, 1.05], [130, 2180, -6, .9], [860, 2260, 12, .8],
  [470, 2460, 0, .8], [710, 2280, 170, .7], [300, 2650, 10, .7],
].forEach((f) => add(fishShadow(...f)));

// Pulau-pulau di sisi rute.
[
  [84, 470, 1.0], [930, 610, .9], [92, 930, .72], [934, 1200, 1.05],
  [108, 1440, .82], [930, 1640, .9], [92, 1960, .78],
].forEach((p) => add(island(...p)));

// Karang dan bongkahan batu.
[
  [300, 800, .8], [720, 870, .7], [120, 1210, .65], [850, 1420, .8],
  [320, 1780, .62], [760, 1980, .7], [170, 2330, .65],
].forEach((p) => add(rock(...p)));

// Gunung es menuju perairan Antartika.
[
  [880, 1930, .85], [150, 2050, .75], [900, 2320, 1.0], [180, 2410, .95],
  [780, 2510, 1.15], [340, 2580, .9], [590, 2640, .8], [120, 2710, .85],
].forEach((p) => add(iceberg(...p)));

// Mercusuar kecil di sisi timur.
add(`<g transform="translate(878 388)">
  <path d="M-24 70L-16 -44H16L24 70Z" fill="#E8F4F6"/>
  <path d="M-18 18H18L21 38H-21Z" fill="#F26A2E"/>
  <path d="M-18 -18H18L20 0H-20Z" fill="#F26A2E"/>
  <rect x="-22" y="-58" width="44" height="16" rx="4" fill="#245A70"/>
  <circle cx="0" cy="-50" r="10" fill="#FFD166"/>
  <path d="M0 -60V-150M0 -60L150 -110M0 -60L-150 -110" stroke="#FFD166" stroke-opacity=".2" stroke-width="18" stroke-linecap="round"/>
</g>`);

// Kompas dekoratif.
add(`<g transform="translate(${pusatKompas[0]} ${pusatKompas[1]})">
  <circle r="54" fill="#081B26" opacity=".62" stroke="#8FB2B8" stroke-opacity=".45" stroke-width="2"/>
  <circle r="40" fill="none" stroke="#8FB2B8" stroke-opacity=".28" stroke-width="1.5"/>
  <polygon points="0,-38 8,-8 38,0 8,8 0,38 -8,8 -38,0 -8,-8" fill="#F26A2E" opacity=".9"/>
  <polygon points="0,-38 8,-8 0,0 -8,-8" fill="#FFD166"/>
  <circle r="5" fill="#081B26"/>
</g>`);

// Cahaya lembut di setiap titik stage agar node Compose mudah dibaca.
stageTitik.forEach(([x, y]) => {
  add(`<circle cx="${x}" cy="${y}" r="120" fill="url(#glow)"/>`);
});

// Perairan Antartika: batas, lapisan es, dan celah es.
add(`<line x1="0" y1="2385" x2="1000" y2="2385" stroke="#BFE6F2" stroke-opacity=".5" stroke-width="3" stroke-dasharray="5 18" stroke-linecap="round"/>`);
add(`<path d="M0 2480Q120 2418 260 2462T520 2440T760 2488T1000 2450V2800H0Z" fill="url(#es)"/>`);
add(`<path d="M0 2580Q180 2510 330 2560T690 2540T1000 2590V2800H0Z" fill="#DCEEF5"/>`);
add(`<path d="M0 2690Q170 2640 330 2672T700 2660T1000 2700V2800H0Z" fill="#BFE6F2"/>`);
for (let i = 0; i < 9; i++) {
  const x = 50 + i * 115;
  add(`<path d="M${x} 2800L${x + 28 + (i % 3) * 12} ${2580 + (i % 4) * 22}" stroke="#8DB7CA" stroke-opacity=".55" stroke-width="3" stroke-linecap="round"/>`);
}
add(`<ellipse cx="190" cy="2520" rx="70" ry="12" fill="#EAF7FB" opacity=".8"/>`);
add(`<ellipse cx="810" cy="2560" rx="86" ry="14" fill="#EAF7FB" opacity=".72"/>`);
add(`<ellipse cx="520" cy="2610" rx="54" ry="9" fill="#EAF7FB" opacity=".62"/>`);

// Vignette dan garis tepi peta.
add(`<rect width="${W}" height="${H}" fill="url(#vignette)"/>`);
add(`<rect x="14" y="14" width="${W - 28}" height="${H - 28}" rx="22" fill="none" stroke="#8FB2B8" stroke-opacity=".14" stroke-width="2"/>`);

add('</svg>');

const svg = peta.join('\n') + '\n';
const tujuan = [
  path.join(ROOT, '2_Aset_dan_Konten', 'assets-source', '01_Peta_Perairan', 'peta_perairan.svg'),
  path.join(ROOT, 'app', 'src', 'main', 'assets', 'peta', 'peta_perairan.svg'),
];
for (const file of tujuan) {
  fs.mkdirSync(path.dirname(file), { recursive: true });
  fs.writeFileSync(file, svg);
  console.log(`Tulis ${path.relative(ROOT, file)} (${svg.length} byte)`);
}
