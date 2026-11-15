package com.example.codefishing.expert

/** Satu perintah game yang bisa dipakai di Sandbox. */
data class PanduanPerintah(
    val nama: String,
    val biaya: String,
    val ringkas: String,
    val hasil: String? = null,
    val contoh: String? = null,
    val sisip: String? = null,
    val kursorMundur: Int = 0,
)

/** Satu topik sintaks Python yang didukung interpreter Sandbox. */
data class PanduanSintaks(
    val judul: String,
    val ringkas: String,
    val contoh: String? = null,
    val sisip: String? = null,
    val kursorMundur: Int = 0,
)

/** Pasangan gejala error dan saran perbaikannya. */
data class PanduanMasalah(val masalah: String, val solusi: String)

/** Tombol pintasan yang menyisipkan potongan kode di editor. */
data class TombolSisip(
    val label: String,
    val potongan: String,
    val kursorMundur: Int = 0,
    val deskripsi: String,
)

/**
 * Isi Guidebook Mode Expert. Disimpan di kode agar dekat dengan interpreter;
 * contoh kodenya diuji di PanduanExpertTest supaya tidak melenceng dari perilaku game.
 */
object PanduanExpert {
    val petunjukPapan: List<String> = listOf(
        "Kapal mulai di (0, 0) — kiri bawah. x bertambah ke Timur, y bertambah ke Utara.",
        "Petak selalu N × N. gerak() di luar tepi mengembalikan False dan tetap memakan 1 tick.",
        "lempar() memakan 1 tick; lihat() gratis. Periksa isi petak dulu sebelum melempar.",
        "Sampah tidak dihitung ikan dan bisa menggagalkan misi tertentu.",
        "Bintang: 3 jika tick ≤ par, 2 jika tick ≤ 1,5 × par, selain itu 1.",
        "Batas aman: 1000 tick dan 10.000 langkah.",
    )

    val perintah: List<PanduanPerintah> = listOf(
        PanduanPerintah(
            nama = "gerak(arah)",
            biaya = "1 tick",
            ringkas = "Geser kapal satu petak ke Utara, Selatan, Timur, atau Barat.",
            hasil = "True jika berhasil. False jika menabrak tepi — tetap dihitung 1 tick.",
            contoh = "gerak(Timur)",
            sisip = "gerak()",
            kursorMundur = 1,
        ),
        PanduanPerintah(
            nama = "lempar()",
            biaya = "1 tick",
            ringkas = "Lempar kail di petak tempat kapal berada.",
            hasil = "Nama ikan (mis. \"nila\"), \"sampah\", atau None jika petak kosong.",
            contoh = "hasil = lempar()\nprint(hasil)",
            sisip = "lempar()",
        ),
        PanduanPerintah(
            nama = "lihat()",
            biaya = "0 tick",
            ringkas = "Baca isi petak tanpa memakan tick. Aman dipanggil sesering apa pun.",
            hasil = "\"nila\", \"sampah\", atau \"kosong\".",
            contoh = "if lihat() == \"nila\":\n    lempar()",
            sisip = "lihat()",
        ),
        PanduanPerintah(
            nama = "get_x() / get_y()",
            biaya = "0 tick",
            ringkas = "Posisi kapal saat ini. Petak (0, 0) ada di kiri bawah.",
            hasil = "Bilangan bulat.",
            contoh = "print(get_x(), get_y())",
            sisip = "get_x()",
            kursorMundur = 1,
        ),
        PanduanPerintah(
            nama = "ukuran()",
            biaya = "0 tick",
            ringkas = "Lebar petak. Petak selalu N × N, jadi nilainya sama untuk x dan y.",
            hasil = "Bilangan bulat N.",
            contoh = "print(ukuran())",
            sisip = "ukuran()",
            kursorMundur = 1,
        ),
        PanduanPerintah(
            nama = "jumlah()",
            biaya = "0 tick",
            ringkas = "Banyak ikan yang masih tersisa di petak — bukan yang sudah tertangkap.",
            hasil = "Bilangan bulat.",
            contoh = "print(jumlah())",
            sisip = "jumlah()",
            kursorMundur = 1,
        ),
        PanduanPerintah(
            nama = "print(nilai)",
            biaya = "0 tick",
            ringkas = "Tulis nilai ke panel OUTPUT di bawah tombol Jalankan.",
            hasil = "None.",
            contoh = "print(\"Halo\", 3)",
            sisip = "print()",
            kursorMundur = 1,
        ),
        PanduanPerintah(
            nama = "range(...)",
            biaya = "0 tick",
            ringkas = "Deret angka untuk for. range(5) = 0..4; range(2, 5) = 2..4; range(5, 0, -1) = 5..1.",
            hasil = "Daftar angka.",
            contoh = "for i in range(3):\n    lempar()",
            sisip = "range()",
            kursorMundur = 1,
        ),
        PanduanPerintah(
            nama = "len(nilai)",
            biaya = "0 tick",
            ringkas = "Panjang teks atau daftar.",
            hasil = "Bilangan bulat.",
            contoh = "print(len(\"ikan\"))",
            sisip = "len()",
            kursorMundur = 1,
        ),
        PanduanPerintah(
            nama = "str() / int() / float() / bool()",
            biaya = "0 tick",
            ringkas = "Ubah tipe data. int(\"12\") menjadi angka 12.",
            hasil = "Nilai dengan tipe yang diminta.",
            contoh = "print(int(\"12\") + 1)",
            sisip = "int()",
            kursorMundur = 1,
        ),
        PanduanPerintah(
            nama = "Utara / Selatan / Timur / Barat",
            biaya = "0 tick",
            ringkas = "Konstanta arah untuk gerak().",
            hasil = "Nama arah.",
            contoh = "gerak(Utara)",
            sisip = "Utara",
        ),
        PanduanPerintah(
            nama = "True / False / None",
            biaya = "0 tick",
            ringkas = "Nilai boolean dan nilai kosong. None adalah hasil lempar() di petak kosong.",
            hasil = "True, False, atau None.",
            contoh = "hasil = lempar()\nif hasil == None:\n    print(\"tidak ada ikan\")",
            sisip = "None",
        ),
    )

    val sintaks: List<PanduanSintaks> = listOf(
        PanduanSintaks(
            judul = "Indentasi 4 spasi",
            ringkas = "Blok setelah tanda ':' harus menjorok. Pakai 4 spasi dan jangan campur 2 dengan 4.",
            contoh = "if lihat() == \"nila\":\n    lempar()",
        ),
        PanduanSintaks(
            judul = "Variabel",
            ringkas = "Simpan nilai dengan nama = nilai. Tidak ada += atau -=; tulis ulang nama variabelnya.",
            contoh = "x = 0\nx = x + 1\nprint(x)",
        ),
        PanduanSintaks(
            judul = "if / elif / else",
            ringkas = "Jalankan blok berbeda sesuai kondisi. Awali dengan 4 spasi di dalam blok.",
            contoh = "if lihat() == \"nila\":\n    lempar()\nelif lihat() == \"sampah\":\n    print(\"hindari\")\nelse:\n    print(\"kosong\")",
            sisip = "if :",
            kursorMundur = 1,
        ),
        PanduanSintaks(
            judul = "for + range",
            ringkas = "Ulangi blok sebanyak angka di range(). Cocok untuk menyapu baris atau kolom.",
            contoh = "for i in range(4):\n    lempar()\n    gerak(Timur)",
            sisip = "for i in range():",
            kursorMundur = 2,
        ),
        PanduanSintaks(
            judul = "while",
            ringkas = "Ulangi blok selama kondisi benar. Pastikan variabel di kondisi berubah agar loop berhenti.",
            contoh = "i = 0\nwhile i < 3:\n    lempar()\n    i = i + 1",
            sisip = "while :",
            kursorMundur = 1,
        ),
        PanduanSintaks(
            judul = "def / return",
            ringkas = "Buat fungsi sendiri, beri parameter, lalu panggil. Misi tertentu mewajibkan def.",
            contoh = "def panen(arah):\n    lempar()\n    gerak(arah)\n\npanen(Timur)",
            sisip = "def ():",
            kursorMundur = 3,
        ),
        PanduanSintaks(
            judul = "break / continue / pass",
            ringkas = "break keluar dari loop, continue melompat ke putaran berikutnya, pass tidak melakukan apa-apa.",
            contoh = "for i in range(5):\n    if i == 2:\n        continue\n    if i == 4:\n        break\n    print(i)",
        ),
        PanduanSintaks(
            judul = "Operator dan teks",
            ringkas = "Aritmetika + - * / // % **, perbandingan == != < <= > >=, logika and or not. Teks bisa digabung dengan + dan diulang dengan *.",
            contoh = "print(7 // 2)\nprint(2 ** 3)\nprint(\"ikan\" * 2)",
        ),
        PanduanSintaks(
            judul = "Komentar",
            ringkas = "Baris yang diawali # tidak dijalankan. Berguna untuk menandai langkah.",
            contoh = "# komentar tidak dijalankan\nlempar()",
            sisip = "# ",
        ),
        PanduanSintaks(
            judul = "Membandingkan isi petak",
            ringkas = "Gunakan lihat() bersama if agar kail hanya dilempar saat isinya ikan yang dicari.",
            contoh = "if lihat() == \"nila\":\n    lempar()\nelif lihat() == \"sampah\":\n    print(\"hindari sampah\")\nelse:\n    print(\"petak kosong\")",
        ),
    )

    val masalah: List<PanduanMasalah> = listOf(
        PanduanMasalah(
            masalah = "IndentationError: Blok setelah ':' harus dijorokkan.",
            solusi = "Tambahkan 4 spasi di awal baris pertama blok.",
        ),
        PanduanMasalah(
            masalah = "IndentationError: Jorokkan blok dengan jumlah spasi yang konsisten.",
            solusi = "Gunakan 4 spasi untuk setiap tingkat; jangan campur 2 dan 4 spasi.",
        ),
        PanduanMasalah(
            masalah = "NameError: Nama '...' belum didefinisikan.",
            solusi = "Periksa ejaan. Fungsi game: gerak, lempar, lihat, get_x, get_y, ukuran, jumlah.",
        ),
        PanduanMasalah(
            masalah = "SyntaxError.",
            solusi = "Periksa kurung (), titik dua :, tanda kutip, dan '=' versus '=='.",
        ),
        PanduanMasalah(
            masalah = "BatasTick: Batas 1000 tick tercapai.",
            solusi = "Persingkat loop dan hindari gerakan bolak-balik yang sia-sia.",
        ),
        PanduanMasalah(
            masalah = "RuntimeError: Loop while terlalu lama.",
            solusi = "Pastikan variabel di kondisi loop berubah di dalam loop.",
        ),
        PanduanMasalah(
            masalah = "Kail mengangkat sampah.",
            solusi = "Panggil lihat() lebih dulu; lempar() hanya jika isinya ikan.",
        ),
        PanduanMasalah(
            masalah = "Ada ikan lain yang ikut tertangkap.",
            solusi = "Misi ini hanya mengizinkan satu spesies; saring dengan lihat().",
        ),
        PanduanMasalah(
            masalah = "Program belum memakai def.",
            solusi = "Bungkus minimal satu langkah ke dalam fungsi buatanmu, lalu panggil fungsinya.",
        ),
        PanduanMasalah(
            masalah = "Target belum tercapai.",
            solusi = "Cek apakah kapal sudah menyapu semua petak dan semua target misi terpenuhi.",
        ),
    )

    val belumDidukung: List<String> = listOf(
        "List dan dict: [1, 2] atau {\"kunci\": nilai}",
        "f-string: f\"...\"",
        "Method objek: teks.upper(), daftar.append()",
        "import, class, lambda, try/except",
        "input() dan akses file",
        "Operator +=, -=, *=, /=",
        "Teks multi-baris dengan tiga kutip",
        "Fungsi dengan nilai bawaan atau *args",
    )

    val tombolSisip: List<TombolSisip> = listOf(
        TombolSisip("Tab", "    ", 0, "Sisipkan indentasi empat spasi"),
        TombolSisip("gerak()", "gerak()", 1, "Sisipkan gerak dengan kursor di dalam kurung"),
        TombolSisip("lempar()", "lempar()", 0, "Sisipkan lempar()"),
        TombolSisip("lihat()", "lihat()", 0, "Sisipkan lihat()"),
        TombolSisip("if", "if :", 1, "Sisipkan if siap diisi kondisi"),
        TombolSisip("elif", "elif :", 1, "Sisipkan elif siap diisi kondisi"),
        TombolSisip("else:", "else:", 0, "Sisipkan else:"),
        TombolSisip("for", "for i in range():", 2, "Sisipkan for dengan kursor di dalam range"),
        TombolSisip("while", "while :", 1, "Sisipkan while siap diisi kondisi"),
        TombolSisip("def", "def ():", 3, "Sisipkan def dengan kursor setelah nama fungsi"),
        TombolSisip("return", "return ", 0, "Sisipkan return"),
        TombolSisip("print()", "print()", 1, "Sisipkan print dengan kursor di dalam kurung"),
        TombolSisip("Timur", "Timur", 0, "Sisipkan arah Timur"),
        TombolSisip("Utara", "Utara", 0, "Sisipkan arah Utara"),
        TombolSisip("Selatan", "Selatan", 0, "Sisipkan arah Selatan"),
        TombolSisip("Barat", "Barat", 0, "Sisipkan arah Barat"),
        TombolSisip("==", " == ", 0, "Sisipkan operator perbandingan =="),
        TombolSisip("=", " = ", 0, "Sisipkan operator penetapan ="),
        TombolSisip("\"\"", "\"\"", 1, "Sisipkan tanda kutip dengan kursor di tengah"),
        TombolSisip("#", "# ", 0, "Sisipkan komentar"),
        TombolSisip("jumlah()", "jumlah()", 1, "Sisipkan jumlah dengan kursor di dalam kurung"),
    )
}