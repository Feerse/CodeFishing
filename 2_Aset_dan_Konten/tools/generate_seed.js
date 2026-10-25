/*
 * Membuat seed DFD dari konten prototipe yang sudah ada.
 * Jalankan: node 2_Aset_dan_Konten/tools/generate_seed.js
 * Sumber: app/src/main/assets/content/{stages,ikan_config,misi}.json
 * Keluaran: 2_Aset_dan_Konten/konten/seed/*.json dan app/src/main/assets/seed/*.json
 */
const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '../..');
const SRC = path.join(ROOT, 'app/src/main/assets/content');
const OUT_SRC = path.join(ROOT, '2_Aset_dan_Konten/konten/seed');
const OUT_ASSET = path.join(ROOT, 'app/src/main/assets/seed');

const stagesLama = JSON.parse(fs.readFileSync(path.join(SRC, 'stages.json'), 'utf8')).stages;
const ikanLama = JSON.parse(fs.readFileSync(path.join(SRC, 'ikan_config.json'), 'utf8')).ikan;
const misiLama = JSON.parse(fs.readFileSync(path.join(SRC, 'misi.json'), 'utf8')).misi;

const ZONA = ['Epipelagik', 'Epipelagik', 'Epipelagik', 'Pelagik', 'Pelagik', 'Pelagik', 'Batial', 'Batial', 'Batial', 'Abisal'];

const stageRows = stagesLama.map((s, i) => ({
  id: i + 1,
  nomor: i + 1,
  nama: s.topic,
  zona: ZONA[i],
  konsep: s.topic,
  tipe: s.boss ? 'boss' : 'materi',
  jumlah_soal: 5,
  skor_lulus: 70,
  xp_reward: s.boss ? 100 : 50,
  ikan_target_id: i + 1,
  urutan: i + 1,
}));

const materiRows = stagesLama.slice(0, 9).map((s, i) => ({
  id: i + 1,
  stage_id: i + 1,
  judul: s.topic,
  isi_markdown: s.text,
  contoh_kode: s.code,
  output_contoh: s.out,
  trik_nelayan: [
    'Tulis nama perintah dengan huruf kecil. Python membedakan huruf besar dan kecil.',
    'Nama variabel yang jelas membuat kode mudah dibaca saat kembali ke pelabuhan.',
    'Gunakan garis bawah untuk memisahkan kata, bukan spasi atau tanda minus.',
    'Teks selalu diapit tanda kutip, bilangan bulat ditulis tanpa titik.',
    'Angka desimal memakai titik. Bool hanya True dan False, diawali huruf besar.',
    'Operator perbandingan menghasilkan True atau False, bukan angka.',
    'Jorokkan blok di bawah if dan else dengan empat spasi.',
    'Pastikan penghitung berubah, jika tidak while akan berputar selamanya.',
    'Cek kondisi di dalam loop pada setiap putaran; jangan lupa mengubah penghitung.',
  ][i],
  urutan: 1,
}));

const IKAN = [
  ['Teri', 'Stolephorus indicus', 'biasa', 'String & print()', 1, 15, 0.08, 'common'],
  ['Bandeng', 'Chanos chanos', 'biasa', 'Variabel & assignment', 2, 20, 0.8, 'common'],
  ['Bawal', 'Colossoma macropomum', 'biasa', 'Nama variabel', 3, 20, 1.2, 'common'],
  ['Mujair', 'Oreochromis mossambicus', 'biasa', 'str & int', 4, 25, 0.9, 'common'],
  ['Nila', 'Oreochromis niloticus', 'biasa', 'float & bool', 5, 25, 0.7, 'common'],
  ['Lele', 'Clarias batrachus', 'biasa', 'Operator', 6, 30, 1.1, 'common'],
  ['Patin', 'Pangasius hypophthalmus', 'biasa', 'if & else', 7, 30, 2.4, 'common'],
  ['Gurame', 'Osphronemus goramy', 'epik', 'while', 8, 45, 1.8, 'common'],
  ['Sepat', 'Trichopodus pectoralis', 'epik', 'if di dalam loop', 9, 45, 0.4, 'common'],
  ['Gabus', 'Channa striata', 'legenda', 'Boss Ujian Pasifik', 10, 100, 3.2, 'common'],
  ['Kerapu', 'Epinephelus coioides', 'langka', 'Jawaban benar beruntun 3', null, 40, 2.0, 'rare'],
  ['Barakuda', 'Sphyraena barracuda', 'langka', 'Jawaban benar beruntun 4', null, 50, 4.5, 'rare'],
  ['Tenggiri', 'Scomberomorus commerson', 'langka', 'Jawaban benar beruntun 5', null, 55, 5.2, 'rare'],
  ['Arwana', 'Scleropages formosus', 'legenda', 'Selesaikan seluruh stage', null, 90, 3.0, 'legend'],
  ['Hiu Merah', 'Carcharhinus longimanus', 'mitos', 'Selesaikan seluruh stage tanpa salah', null, 150, 120.0, 'mythic'],
];

const PLACEHOLDER = [
  ['Kakap Merah', 'Lutjanus campechanus', 'biasa'],
  ['Tongkol', 'Euthynnus affinis', 'biasa'],
  ['Cakalang', 'Katsuwonus pelamis', 'biasa'],
  ['Sarden', 'Sardinella lemuru', 'biasa'],
  ['Kembung', 'Rastrelliger kanagurta', 'biasa'],
  ['Layang', 'Decapterus macrosoma', 'biasa'],
  ['Kue', 'Kuhlia rupestris', 'biasa'],
  ['Baronang', 'Siganus guttatus', 'biasa'],
  ['Belanak', 'Mugil cephalus', 'biasa'],
  ['Tawes', 'Barbonymus gonionotus', 'biasa'],
  ['Nilem', 'Osteochilus hasselti', 'biasa'],
  ['Mas', 'Cyprinus carpio', 'biasa'],
  ['Betok', 'Anabas testudineus', 'biasa'],
  ['Sidat', 'Anguilla bicolor', 'langka'],
  ['Kakap Putih', 'Lates calcarifer', 'langka'],
  ['Kuwe', 'Caranx sexfasciatus', 'langka'],
  ['Bawal Hitam', 'Parastromateus niger', 'langka'],
  ['Bawal Bintang', 'Trachinotus blochii', 'langka'],
  ['Kerong-kerong', 'Terapon jarbua', 'langka'],
  ['Gulamah', 'Nibea albiflora', 'langka'],
  ['Manyung', 'Arius thalassinus', 'langka'],
  ['Pari Manta', 'Mobula birostris', 'epik'],
  ['Hiu Tutul', 'Rhincodon typus', 'epik'],
  ['Hiu Martil', 'Sphyrna lewini', 'epik'],
  ['Mola-mola', 'Mola mola', 'epik'],
  ['Napoleon', 'Cheilinus undulatus', 'epik'],
  ['Ikan Pedang', 'Xiphias gladius', 'epik'],
  ['Marlin Hitam', 'Makaira indica', 'epik'],
  ['Tuna Sirip Biru', 'Thunnus thynnus', 'legenda'],
  ['Coelacanth', 'Latimeria chalumnae', 'legenda'],
  ['Hiu Greenland', 'Somniosus microcephalus', 'legenda'],
  ['Oarfish', 'Regalecus glesne', 'mitos'],
  ['Hiu Putih Raksasa', 'Carcharodon carcharias', 'mitos'],
];

const ikanRows = [];
IKAN.forEach((row, i) => {
  const [nama, ilmiah, tier, konsep, stageId, xp, berat, key] = row;
  ikanRows.push({
    id: i + 1,
    nama,
    nama_ilmiah: ilmiah,
    tier,
    konsep_python: konsep,
    deskripsi: `${nama} ditemukan di perairan CodeFishing. Selesaikan ${stageId ? `Stage ${stageId}` : 'tantangan khusus'} untuk menambahkannya ke koleksi.`,
    asal_stage_id: stageId,
    syarat_unlock: stageId ? `Selesaikan Stage ${stageId}` : 'Tantangan khusus',
    nilai_xp: xp,
    bobot_kg: berat,
    gambar: `ikan_${nama.toLowerCase().replace(/ /g, '_')}`,
    urutan: i + 1,
  });
});
PLACEHOLDER.forEach((row, i) => {
  const [nama, ilmiah, tier] = row;
  ikanRows.push({
    id: 16 + i,
    nama,
    nama_ilmiah: ilmiah,
    tier,
    konsep_python: 'Spesies dalam riset',
    deskripsi: `${nama} masih dalam riset tim Material Collecting. Ilustrasinya akan ditambahkan pada pembaruan berikutnya.`,
    asal_stage_id: null,
    syarat_unlock: 'Segera hadir — menunggu aset',
    nilai_xp: 10,
    bobot_kg: Math.round((0.2 + (i % 7) * 0.45) * 10) / 10,
    gambar: 'tanda_tanya_ikan',
    urutan: 16 + i,
  });
});

function conv(q, stageId, urutan) {
  return {
    stage_asal_id: stageId,
    tipe: 'token',
    teks: q.q,
    kode_template: q.code,
    pembahasan: `Jawaban benar: ${q.o[q.ok].t}. Hasilnya: ${q.o[q.ok].out}.`,
    poin: 25,
    urutan,
    pilihan: q.o.map((o, i) => ({ teks: o.t, benar: i === q.ok ? 1 : 0 })),
  };
}

function baru(teks, kode, benar, salah, pembahasan) {
  return {
    teks,
    kode_template: kode,
    pembahasan,
    pilihan: [{ teks: benar, benar: 1 }, ...salah.map((s) => ({ teks: s, benar: 0 }))],
  };
}

const TAMBAHAN = {
  1: [
    baru('Papan skor pelabuhan harus menampilkan kata Python.', 'print(___)', '"Python"', ['Python', '"Python', 'echo("Python")'], 'Teks harus diapit tanda kutip dan dicetak dengan print().'),
    baru('Dua baris teks dicetak berurutan: Laut lalu Tenang.', 'print("Laut")\n___("Tenang")', 'print', ['echo', 'out', 'write'], 'print() dapat dipanggil berulang untuk mencetak baris baru.'),
  ],
  2: [
    baru('Nelayan menyimpan 10 umpan ke variabel umpan.', 'umpan ___ 10', '=', ['==', '=>', ':'], 'Tanda = memasukkan nilai ke variabel; == membandingkan.'),
    baru('Nama ikan diganti dari teri menjadi gurame. Tampilkan isi variabel nama.', 'nama = "teri"\nnama = "gurame"\nprint(___)', 'nama', ['"nama"', 'nama()', '"teri"'], 'print(nama) membaca nilai terbaru yang disimpan di variabel.'),
  ],
  3: [
    baru('Nelayan mencatat berat ikan. Pilih nama variabel yang valid.', '___ = 2.5', 'berat_ikan', ['berat ikan', 'berat-ikan', '2berat'], 'Nama variabel memakai huruf kecil dan garis bawah, tidak diawali angka.'),
    baru('Pilih nama variabel yang boleh diawali garis bawah.', '___ = 12', '_jumlah', ['2jumlah', 'jumlah ikan', 'jumlah-kan'], 'Garis bawah boleh di awal nama variabel.'),
    baru('Jumlahkan dua keranjang: lama 5 dan baru 7.', 'lama = 5\nbaru = 7\nhasil = ___\nprint(hasil)', 'lama + baru', ['lama - baru', '"lama + baru"', 'lama == baru'], 'Operator + menjumlahkan nilai kedua variabel.'),
  ],
  4: [
    baru('Papan menampilkan "100" sebagai teks. Tipe apa nilainya?', 'print(type(___))', '"100"', ['100', '100.0', 'True'], 'Nilai dalam tanda kutip bertipe str.'),
    baru('Hitung total ikan dari dua ember berisi 3 dan 4.', 'print(___)', '3 + 4', ['"3" + "4"', '3 + "4"', '"3 + 4"'], 'int + int menghasilkan int 7.'),
    baru('Pilih nilai yang bertipe int.', 'print(type(___))', '25', ['"25"', '25.0', 'True'], '25 adalah bilangan bulat, jadi bertipe int.'),
  ],
  5: [
    baru('Suhu air tercatat 27.5 derajat. Pilih nilai float.', 'print(type(___))', '27.5', ['27', '"27.5"', 'True'], 'float memakai titik desimal.'),
    baru('Periksa apakah 5 lebih besar dari 3. Hasilnya bertipe bool.', 'print(type(___))', '5 > 3', ['"5 > 3"', '5 + 3', '"True"'], 'Perbandingan menghasilkan True atau False bertipe bool.'),
  ],
  6: [
    baru('Dua bubu masing-masing berisi 4 ikan. Hitung totalnya.', 'print(2 ___ 4)', '*', ['+', '-', '=='], '2 * 4 = 8.'),
    baru('Periksa apakah 10 sama dengan 12.', 'print(10 ___ 12)', '==', ['!=', '<', '>'], '== memeriksa kesamaan dan menghasilkan False.'),
    baru('Bagi 20 ikan ke 4 keranjang.', 'print(20 ___ 4)', '/', ['//', '%', '*'], 'Operator / menghasilkan pembagian, yaitu 5.0.'),
  ],
  7: [
    baru('Ikan lulus jika beratnya minimal 2 kg.', 'berat = 2.5\nif berat ___ 2:\n    print("Lulus")', '>=', ['<=', '==', '!='], 'Minimal berarti lebih besar atau sama dengan (>=).'),
    baru('Ketika kondisi if salah, jalankan blok alternatif.', 'if n > 5:\n    print("A")\n___:\n    print("B")', 'else', ['if', 'elif', 'for'], 'else menangani kondisi yang tidak masuk if.'),
    baru('Nilai 75 masuk kategori B. Lengkapi elif.', 'nilai = 75\nif nilai >= 80:\n    print("A")\nelif nilai >= 70:\n    print(___)', '"B"', ['"A"', '"C"', 'nilai'], '75 tidak memenuhi if, tetapi memenuhi elif >= 70.'),
  ],
  8: [
    baru('Putar loop tepat 4 kali.', 'n = 0\nwhile n < ___:\n    n = n + 1', '4', ['3', '5', 'n'], 'Kondisi n < 4 bernilai benar untuk n = 0,1,2,3.'),
    baru('Setelah loop selesai, berapa nilai n?', 'n = 0\nwhile n < 5:\n    n = n + 1\nprint(___)', 'n', ['5', '0', '"n"'], 'Setiap putaran menambah n; setelah 5 putaran n bernilai 5.'),
  ],
  9: [
    baru('Jumlahkan bilangan 1 sampai 3 dengan for.', 'total = 0\nfor i in range(1, 4):\n    total = total + i\nprint(total)', '6', ['3', '10', '0'], '1 + 2 + 3 = 6.'),
    baru('Penghitung berhenti saat mencapai 3. Berapa hasil akhirnya?', 'n = 0\nwhile n < 3:\n    n = n + 1\nprint(n)', '3', ['2', '4', '0'], 'Loop berhenti ketika n < 3 bernilai salah, yaitu n = 3.'),
  ],
};

// Soal boss lama dipindahkan ke stage yang sesuai agar tidak ada soal yatim.
const bossLama = stagesLama[9].qs;
const pinjam = {
  1: [bossLama[0]],
  2: [bossLama[1]],
  5: [bossLama[2]],
  8: [bossLama[3]],
  9: [bossLama[4]],
};

const soalRows = [];
let soalId = 1;
const perStage = {};
for (let s = 1; s <= 9; s++) {
  const qs = [];
  for (const q of stagesLama[s - 1].qs) qs.push(conv(q, s, qs.length + 1));
  for (const q of pinjam[s] || []) qs.push(conv(q, s, qs.length + 1));
  for (const q of TAMBAHAN[s]) qs.push({ ...q, stage_asal_id: s, tipe: 'token', poin: 25, urutan: qs.length + 1 });
  while (qs.length < 5) qs.push({ ...TAMBAHAN[s][0], stage_asal_id: s, tipe: 'token', poin: 25, urutan: qs.length + 1 });
  perStage[s] = [];
  for (const q of qs.slice(0, 5)) {
    const row = {
      id: soalId++,
      stage_asal_id: s,
      tipe: q.tipe || 'token',
      teks: q.teks,
      kode_template: q.kode_template,
      jawaban_benar: q.jawaban_benar || null,
      pembahasan: q.pembahasan,
      poin: q.poin || 25,
      urutan: perStage[s].length + 1,
      pilihan: q.pilihan.map((p, i) => ({ id: soalId * 10 + i, soal_id: soalId - 1, teks: p.teks, benar: p.benar, urutan: i + 1 })),
    };
    perStage[s].push(row.id);
    soalRows.push(row);
  }
}

const komposisi = [];
let komposisiId = 1;
for (let s = 1; s <= 9; s++) {
  perStage[s].forEach((qid, i) => komposisi.push({ id: komposisiId++, stage_id: s, soal_id: qid, urutan: i + 1 }));
}
// Boss memakai lima soal dari stage berbeda.
[1, 2, 5, 8, 9].forEach((s, i) => komposisi.push({ id: komposisiId++, stage_id: 10, soal_id: perStage[s][0], urutan: i + 1 }));

const misiRows = misiLama.map((m, i) => {
  const par = { m1: 2, m2: 14, m3: 48, m4: 36, m5: 40, m6: 36, bebas: 999 }[m.id];
  return {
    id: i + 1,
    nomor: i + 1,
    judul: m.n,
    deskripsi: m.gt,
    tipe: m.goal.t,
    target_ikan: m.goal.n || 0,
    target_baris: m.id === 'm2' ? 4 : null,
    par_tick: par,
    xp_reward: m.id === 'bebas' ? 0 : 120,
    urutan: i + 1,
    // Data runtime sandbox (tidak masuk tabel misi):
    kode: m.id,
    grid: m.N,
    cells: m.cells,
    kode_awal: m.code,
    hint: m.hint,
    goal: m.goal,
  };
});

function writeJson(dir, name, data) {
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(path.join(dir, name), JSON.stringify(data, null, 1) + '\n', 'utf8');
}

for (const dir of [OUT_SRC, OUT_ASSET]) {
  writeJson(dir, 'stage.json', stageRows);
  writeJson(dir, 'materi.json', materiRows);
  writeJson(dir, 'soal.json', soalRows);
  writeJson(dir, 'komposisi_kuis.json', komposisi);
  writeJson(dir, 'ikan.json', ikanRows);
  writeJson(dir, 'misi.json', misiRows);
}

console.log(`Seed dibuat: ${stageRows.length} stage, ${materiRows.length} materi, ${soalRows.length} soal, ${komposisi.length} komposisi, ${ikanRows.length} ikan, ${misiRows.length} misi.`);
