# 4. Logika Mode Expert (Sandbox)

Kode: `app/src/main/java/com/example/codefishing/expert/`
- `SandboxModel.kt`: `Dunia`, `Biaya` (gerak 1, lempar 2, lihat 0), `HasilJalan`, antarmuka `CodeRunner`
- `Scoring.kt`: bintang (3 bila ticks <= par, 2 bila <= ceil(par x 1,4), selain itu 1)

Tugas utama: isi implementasi `CodeRunner` (interpreter subset Python yang berjalan offline) dan penjalan petak laut. Layar Sandbox (tim Frontend) hanya memanggil antarmuka ini.
Misi ada di `2_Aset_dan_Konten/konten/misi.json`. Aturan: `docs/ATURAN_GAME.md`.
