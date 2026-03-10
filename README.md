# 📱 CodePocket

> Notepad khusus untuk Code Snippet & Terminal Command — 100% Offline, Dark Mode, Export/Import JSON.

---

## ✨ Fitur Lengkap

| Fitur | Detail |
|-------|--------|
| 💾 Simpan Snippet | Title, code, kategori, deskripsi, tag |
| 📋 Copy Cepat | Tombol copy di setiap card |
| 🗂️ Kategori | Bash, Python, JavaScript, HTML, Git, Custom |
| 🔍 Search | Cari berdasarkan title, code, tag, deskripsi |
| ✏️ Edit / Delete | Full CRUD lewat popup menu |
| 💾 Auto Save | Room DB lokal, tidak perlu internet |
| 🌙 Dark Mode | Toggle dari toolbar |
| 🏷️ Tag Snippet | `#bash` `#git` `#python` dll |
| 📤 Export JSON | Export semua snippet ke file `.json` |
| 📥 Import JSON | Import snippet dari file `.json` |
| ☑️ Multi-Copy | Long press → pilih banyak → copy sekaligus |
| 📌 Pin | Pin snippet penting ke atas daftar |
| 📱 Offline 100% | Database Room lokal, tidak butuh internet |

---

## 🏗️ Struktur Proyek

```
CodePocket/
├── app/
│   ├── build.gradle
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/codepocket/app/
│       │   ├── MainActivity.kt           ← Main screen
│       │   ├── AddEditActivity.kt        ← Add/Edit snippet
│       │   ├── SnippetViewModel.kt       ← ViewModel (LiveData)
│       │   ├── adapter/
│       │   │   └── SnippetAdapter.kt     ← RecyclerView adapter
│       │   ├── database/
│       │   │   ├── AppDatabase.kt        ← Room database
│       │   │   ├── SnippetDao.kt         ← DAO queries
│       │   │   └── SnippetRepository.kt  ← Repository pattern
│       │   ├── model/
│       │   │   └── Snippet.kt            ← Data class + Categories
│       │   └── utils/
│       │       ├── ImportExportUtil.kt   ← JSON export/import
│       │       └── Prefs.kt              ← SharedPreferences (dark mode)
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml
│           │   ├── activity_add_edit.xml
│           │   └── item_snippet.xml
│           ├── drawable/                 ← Vector icons + backgrounds
│           ├── menu/
│           │   ├── menu_snippet_item.xml
│           │   └── menu_export_import.xml
│           └── values/
│               ├── colors.xml
│               ├── strings.xml
│               └── themes.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

---

## 🚀 Cara Build di Android Studio

### Prasyarat
- Android Studio Hedgehog (2023.1+) atau lebih baru
- JDK 8+
- Android SDK API 24+ (Android 7.0)

### Langkah Build

1. **Buka project** di Android Studio
   ```
   File → Open → pilih folder CodePocket/
   ```

2. **Tunggu Gradle sync** selesai (butuh internet pertama kali untuk download dependencies)

3. **Jalankan** di emulator atau device:
   - Klik tombol ▶️ Run
   - Atau: `Build → Run App` (Shift+F10)

4. **Generate APK:**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   APK akan ada di: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📦 Dependencies

```gradle
// Room Database
implementation 'androidx.room:room-runtime:2.6.1'
implementation 'androidx.room:room-ktx:2.6.1'
kapt 'androidx.room:room-compiler:2.6.1'

// Gson (JSON export/import)
implementation 'com.google.code.gson:gson:2.10.1'

// ViewModel + LiveData
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'

// Coroutines
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3'

// Material Design
implementation 'com.google.android.material:material:1.11.0'
```

---

## 📁 Format Export JSON

```json
{
  "version": "1.0",
  "exportedAt": 1710000000000,
  "snippets": [
    {
      "id": 1,
      "title": "Update & Upgrade",
      "code": "pkg update && pkg upgrade -y",
      "category": "Bash",
      "description": "Update semua package",
      "tags": ["bash", "termux", "update"],
      "createdAt": 1710000000000,
      "updatedAt": 1710000000000,
      "isPinned": false
    }
  ]
}
```

---

## 🎯 Cara Penggunaan

| Aksi | Cara |
|------|------|
| Tambah snippet | Tap FAB `+` |
| Copy code | Tap ikon 📋 di card |
| Edit snippet | Tap card atau `⋮ → Edit` |
| Hapus snippet | `⋮ → Hapus` |
| Pin snippet | `⋮ → Pin` |
| Multi select | Long press card, pilih beberapa |
| Multi copy | Pilih beberapa → tap ikon copy di bar atas |
| Export | Toolbar `⋮ → Export Snippets` |
| Import | Toolbar `⋮ → Import Snippets` |
| Dark mode | Tap ikon 🌙 di toolbar |
| Filter kategori | Tap chip kategori (Bash, Python, dll) |
| Search | Ketik di kotak search |

---

## 🔧 Kustomisasi

### Tambah Kategori Baru
Edit `model/Snippet.kt` di bagian object `Categories`:
```kotlin
const val RUST = "Rust"
val ALL = listOf(BASH, PYTHON, JAVASCRIPT, HTML, GIT, RUST, CUSTOM)

fun getIcon(category: String) = when(category) {
    RUST -> "🦀"
    // ...
}
```

Lalu tambahkan Chip di `activity_main.xml` dan listener di `MainActivity.kt`.

---

## 📱 Minimum Requirements

- **Android:** 7.0 (API 24)
- **RAM:** 50MB
- **Storage:** ~10MB + data snippet
- **Internet:** Tidak diperlukan (100% offline)
