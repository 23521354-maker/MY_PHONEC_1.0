# BÁO CÁO DỰ ÁN MY PHONEC
**Ứng dụng Android đa năng: Phân tích thiết bị di động & Công cụ hỗ trợ build PC**

---

## 1. GIỚI THIỆU TỔNG QUAN

**My PhoneC** là một ứng dụng Android được phát triển bằng **Kotlin + Jetpack Compose**, tích hợp **Firebase** làm hạ tầng đám mây, hướng đến hai nhóm người dùng:

- **Người dùng phổ thông**: muốn kiểm tra "sức khoẻ" chiếc điện thoại của mình (thông tin phần cứng, cảm biến, màn hình, pin, hiệu năng).
- **Người dùng quan tâm tới máy tính**: muốn xây dựng cấu hình PC, so sánh linh kiện, tính toán bottleneck CPU/GPU.

### 1.1. Mục tiêu của dự án
1. Xây dựng một ứng dụng **2-trong-1** (PHONE + PC) trong cùng một app.
2. **Giải quyết điểm yếu cố hữu** của các app dạng "phone info / benchmark": người dùng thường gỡ app sau khi dùng → mất hết dữ liệu. My PhoneC khắc phục bằng cách **đăng nhập Google + đồng bộ kết quả benchmark, thông tin thiết bị lên Firestore**, gắn liền với tài khoản Google của người dùng. Khi cài lại app, dữ liệu được khôi phục tự động.
3. Tích hợp **AI (Gemini qua Firebase AI Logic)** để gợi ý cấu hình PC theo nhu cầu thực tế, thay vì chỉ là form chọn linh kiện đơn thuần.
4. Triển khai **bảo mật phía client** thông qua **Firebase App Check** (Play Integrity ở bản release, Debug provider ở môi trường phát triển).

### 1.2. Thông tin kỹ thuật
| Mục | Giá trị |
|---|---|
| Ngôn ngữ | Kotlin |
| UI framework | Jetpack Compose + Material 3 |
| Kiến trúc | MVVM (Model – View – ViewModel) |
| Min SDK / Target SDK | 24 / 35 |
| Backend | Firebase (Authentication, Firestore, App Check, AI Logic, Analytics) |
| Bất đồng bộ | Kotlin Coroutines + StateFlow |
| Lưu trữ cục bộ | Jetpack DataStore (Preferences) |
| Xác thực | Google Sign-In qua Credential Manager API |
| Điều hướng | Navigation Compose |

---

## 2. KIẾN TRÚC ỨNG DỤNG

### 2.1. Sơ đồ kiến trúc tổng quát (MVVM + Repository)

```
┌──────────────────────────────────────────────────────────────┐
│                         UI LAYER                              │
│   (Jetpack Compose Screens: MyPhoneScreen, PCToolsScreen,     │
│    BenchmarkScreen, BuildRigScreen, BottleneckScreen, ...)    │
└──────────────────────────┬───────────────────────────────────┘
                           │ collectAsState / events
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                      VIEWMODEL LAYER                          │
│  AuthViewModel · BenchmarkViewModel · PCBuilderViewModel      │
│  BottleneckViewModel · CompareViewModel · UserProfileVM ...   │
│  (Quản lý StateFlow, business logic, gọi Repository)          │
└──────────────────────────┬───────────────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────────────┐
│                     REPOSITORY LAYER                          │
│  FirebaseRepository  · UserBenchmarkRepository                │
│  LeaderboardRepository · BuildAiRepository · SessionManager   │
└──────────┬─────────────────────────────────────┬─────────────┘
           │                                     │
           ▼                                     ▼
   ┌───────────────┐                   ┌──────────────────┐
   │ Local DataStore│                  │   FIREBASE CLOUD │
   │ (Session)      │                  │  Auth · Firestore│
   └───────────────┘                   │  AI · App Check  │
                                       └──────────────────┘
```

### 2.2. Cấu trúc package (`com.example.myphonec`)

| Nhóm chức năng | File chính |
|---|---|
| Entry point & điều hướng | `MainActivity.kt`, `MyApplication.kt` |
| Xác thực & phiên đăng nhập | `LOGIN.kt`, `AuthViewModel.kt`, `SessionManager.kt` |
| Thông tin điện thoại | `MyPhoneScreen.kt`, `DeviceInfoViewModel.kt`, `DeviceDetailsScreen.kt`, `SystemDetailsScreen.kt`, `ProcessorViewModel.kt`, `ProcessorInfoScreen.kt` |
| Cảm biến / Màn hình / Pin / Hiệu năng | `SensorsViewModel.kt`, `SensorsScreen.kt`, `ScreenTestScreen.kt`, `BatteryViewModel.kt`, `BatteryHealthScreen.kt`, `PerformanceViewModel.kt`, `PerformanceScreen.kt` |
| Benchmark & Bảng xếp hạng | `BenchmarkViewModel.kt`, `BenchmarkScreen.kt`, `BenchmarkRenderer.kt`, `BenchmarkedDevice.kt`, `LeaderboardRepository.kt`, `LeaderboardViewModel.kt`, `LeaderboardScreen.kt`, `UserBenchmarkRepository.kt`, `UserProfileViewModel.kt` |
| Build PC | `BUILD PC.kt`, `PCBuilderViewModel.kt`, `PCBuildModels.kt`, `PCToolsScreen.kt` |
| So sánh linh kiện | `Compare.kt`, `CompareViewModel.kt`, `CompareModels.kt` |
| Tính bottleneck | `bottleneck calculator.kt`, `BottleneckViewModel.kt`, `BottleneckModels.kt` |
| Mô hình dữ liệu phần cứng | `HardwareModels.kt` (CPU, GPU, Motherboard, RAM, PSU) |
| AI Logic | `BuildAiRepository.kt` |
| Quản trị (admin) | `AdminViewModel.kt`, `AdminScreen.kt` |
| Cầu Firebase | `FirebaseRepository.kt` |

### 2.3. Luồng khởi động (MainActivity)
1. `setupAppCheck()` — cài đặt App Check **trước khi** gọi bất kỳ dịch vụ Firebase nào (DEBUG dùng `DebugAppCheckProviderFactory`, RELEASE dùng `PlayIntegrityAppCheckProviderFactory`).
2. `MainScreen()` khởi tạo các Repository (singleton-like qua `remember`) và inject vào ViewModel thông qua `AppViewModelFactory`.
3. `AuthViewModel` đọc phiên cũ từ DataStore → quyết định `startDestination`:
   - Nếu đã đăng nhập / khách → `phone`.
   - Nếu chưa → `login`.
4. Bottom navigation gồm 2 tab chính: **PHONE** và **PC**, các màn hình con được điều hướng bằng `NavController` với hiệu ứng slide.

---

## 3. CÁC CHỨC NĂNG CHÍNH

### 3.1. Tab PHONE — Phân tích thiết bị di động

| Chức năng | Màn hình | Cách hoạt động |
|---|---|---|
| **Tổng quan thiết bị** | `MyPhoneScreen` | Hiển thị model, chipset, RAM/Storage tổng/khả dụng, ảnh đại diện user. |
| **Chi tiết phần cứng** | `DeviceDetailsScreen`, `SystemDetailsScreen` | Đọc `Build.MANUFACTURER`, `Build.MODEL`, `Build.BOARD`, `Build.HARDWARE`, kernel, security patch, bootloader, build ID, độ phân giải, mật độ điểm ảnh… |
| **Thông tin CPU** | `ProcessorInfoScreen` | Đọc `/proc/cpuinfo` và `Runtime.availableProcessors()` để xác định kiến trúc, số nhân, tần số. |
| **Cảm biến** | `SensorsScreen` | Dùng `SensorManager` liệt kê toàn bộ cảm biến (gia tốc, con quay, từ trường, ánh sáng, tiệm cận…) và cho phép test trực tiếp. |
| **Test màn hình** | `ScreenTestScreen` | Hiển thị các màu thuần (đỏ, lục, lam, trắng, đen) để phát hiện điểm chết, ám màu, hở sáng. |
| **Sức khoẻ pin** | `BatteryHealthScreen` | Dùng `BatteryManager` để đọc dung lượng, trạng thái sạc, nhiệt độ, công nghệ pin, điện thế. |
| **Hiệu năng thực tế** | `PerformanceScreen` | Đo FPS, mức sử dụng RAM trong thời gian thực. |
| **Benchmark + Upload** | `BenchmarkScreen` | Chạy benchmark 30 giây (đếm ngược 3s → render → đo FPS), tính score = `avgFPS * 1500`, gán tier (Flagship / High-End / Midrange / Entry). Khi đăng nhập, kết quả tự động upload lên Firestore. |
| **Bảng xếp hạng** | `LeaderboardScreen` | Lấy top 20 thiết bị có điểm cao nhất từ collection `leaderboard_scores`, real-time qua `addSnapshotListener`. |
| **Hồ sơ người dùng** | `UserProfileViewModel` | Hiển thị các thiết bị mà chính user đã từng benchmark (lịch sử cá nhân). |

### 3.2. Tab PC — Công cụ hỗ trợ build máy tính

#### a) **Build PC** (`PCBuilderViewModel`)
- Tải danh sách CPU, GPU, Motherboard, RAM, PSU từ Firestore.
- Người dùng chọn linh kiện thủ công **HOẶC** dùng AI gợi ý.
- **Compatibility Engine** (logic phía client):
  - Kiểm tra **socket** CPU vs Motherboard.
  - Kiểm tra **loại RAM** (DDR4/DDR5) Motherboard có hỗ trợ không.
  - Tính `requiredPower = CPU.tdp + GPU.tdp + 100W`. So sánh với PSU.watt (cảnh báo "Near Limit" nếu dư < 50W).
  - Trả về `buildScore` 0/100 và trạng thái `COMPATIBLE / INCOMPATIBLE / INCOMPLETE`.
- **AI Suggest**: ghép prompt động chứa toàn bộ catalog Firestore + yêu cầu user (ngân sách, mục đích, độ phân giải, hãng, khả năng nâng cấp) → Gemini trả về JSON → tự động fill form và chạy lại Compatibility Engine.
- **Chat Assistant**: chat đa lượt với Gemini để tư vấn cấu hình.

#### b) **So sánh linh kiện** (`CompareViewModel`)
- Chọn 2 CPU hoặc 2 GPU bất kỳ → so sánh trực quan các thông số: score, TDP, số nhân/luồng, base/boost clock, VRAM, băng thông…

#### c) **Tính Bottleneck** (`BottleneckViewModel`)
- Thuật toán dựa trên trọng số CPU theo độ phân giải:
  - 1080p → CPU weight 3.5, 1440p → 3.2, 4K → 2.6 (vì độ phân giải càng cao GPU càng quyết định).
- Tính `cpuPower = cpu.score * cpuWeight`, `gpuPower = gpu.score`.
- `rawPercent = |cpuPower - gpuPower| / average * 100`.
- Soften × 0.35 và clamp [0, 45]%.
- Phân loại 5 mức từ **EXCELLENT BALANCE** → **SEVERE BOTTLENECK**, kèm hướng (CPU bottleneck / GPU bottleneck) và mức tải mô phỏng.

### 3.3. Trang Admin (`AdminViewModel`, `AdminScreen`)
- Cho phép admin import file JSON danh sách CPU/GPU vào Firestore (chunk 400 record/batch).
- Có thể clear toàn bộ collection.
- Đây là công cụ quản trị catalog phần cứng cho hai chức năng Build PC và Bottleneck.

---

## 4. FIREBASE — VAI TRÒ & CÁCH HOẠT ĐỘNG

Đây là phần **quan trọng nhất** giải quyết "điểm yếu cố hữu" của các app phân tích thiết bị: **dữ liệu không bị mất khi gỡ app** vì được lưu vào tài khoản Google của user trên Firestore.

### 4.1. Firebase App Check
**Mục đích**: chống request giả mạo từ bot/script bên ngoài app thật.

```kotlin
// MainActivity.kt
private fun setupAppCheck() {
    val firebaseAppCheck = Firebase.appCheck
    if (BuildConfig.DEBUG) {
        firebaseAppCheck.installAppCheckProviderFactory(
            DebugAppCheckProviderFactory.getInstance())
    } else {
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance())
    }
}
```

- **Debug provider**: dùng cho emulator / máy thật bật cờ debug. Token debug phải được khai báo trong Firebase Console.
- **Play Integrity provider**: dùng cho APK release. Google Play Integrity API ký token chứng nhận "đây là app My PhoneC thật, được cài từ Play Store / chữ ký gốc, đang chạy trên thiết bị Android không bị root/tampered".
- Mọi request đến Firestore, Auth, AI Logic đều bị Firebase **từ chối** nếu không kèm App Check token hợp lệ → ngăn lạm dụng quota AI và ghi rác vào Firestore.

### 4.2. Firebase Authentication (Google Sign-In)
**Mục đích**: gắn dữ liệu (benchmark, profile) với một danh tính duy nhất, bền vững theo tài khoản Google.

Luồng đăng nhập (`AuthViewModel.signInWithGoogle`):
1. Mở `CredentialManager` với `GetGoogleIdOption(serverClientId)` → user chọn tài khoản Google.
2. Lấy được `GoogleIdTokenCredential` chứa `idToken`.
3. Đổi token lấy `AuthCredential` qua `GoogleAuthProvider.getCredential(idToken, null)`.
4. Gọi `FirebaseAuth.signInWithCredential(...)` → nhận `FirebaseUser` (uid, displayName, email, photoUrl).
5. Gọi `FirebaseRepository.saveOrUpdateUser(...)` ghi/cập nhật document vào collection `users/{uid}`.
6. `SessionManager` lưu phiên vào DataStore để lần mở app sau **không cần đăng nhập lại**.

App cũng hỗ trợ chế độ **Guest** (`onContinueAsGuest`) — vẫn dùng được tab PHONE/PC nhưng không upload, không có lịch sử cá nhân.

### 4.3. Cloud Firestore — Cấu trúc dữ liệu

| Collection | Document ID | Trường chính | Mục đích |
|---|---|---|---|
| `users` | `{uid}` | `uid, name, email, photoUrl, createdAt` | Hồ sơ người dùng |
| `user_benchmarks` | auto | `uid, userName, deviceModel, chipset, score, fps, testedAt` | **Lịch sử benchmark cá nhân** — nguồn để khôi phục lại sau khi cài lại app |
| `leaderboard_scores` | `{uid}_{deviceModel}` | giống trên, chỉ giữ điểm cao nhất theo thiết bị | Bảng xếp hạng global |
| `cpus` | slug-name | `name, socket, cores, threads, score, tdp,...` | Catalog CPU |
| `gpus` | slug-name | `name, score, tdp, vram,...` | Catalog GPU |
| `motherboards` | slug-name | `name, socket, ramType,...` | Catalog Mainboard |
| `ram` | slug-name | `name, type, capacity, speed,...` | Catalog RAM |
| `psu` | slug-name | `name, watt, certification,...` | Catalog nguồn |

**Hai cơ chế ghi/đọc đáng chú ý:**

1. **Leaderboard chống ghi đè điểm thấp** (`FirebaseRepository.saveBenchmarkResult`):
   ```kotlin
   if (!existingDoc.exists() || existingDoc.score < newScore) {
       leaderboardRef.set(benchmarkData, SetOptions.merge()).await()
   }
   ```
   → chỉ cập nhật nếu điểm mới cao hơn.

2. **Real-time listener** dùng `callbackFlow + addSnapshotListener` ở `getTopScores()` và `getUserBenchmarks(uid)` → bảng xếp hạng và lịch sử cá nhân tự refresh khi có dữ liệu mới mà không cần reload.

3. **Bulk import** dùng `WriteBatch` chunked 400 records để vượt qua giới hạn 500 op/batch của Firestore.

### 4.4. Firebase AI Logic (Gemini)
**Mục đích**: tích hợp Gemini mà **không lộ API key** ở client (Firebase đứng làm proxy + áp App Check).

```kotlin
// BuildAiRepository.kt
private val model: GenerativeModel by lazy {
    Firebase.ai.generativeModel("gemini-3-flash-preview")
}
```

- **`suggestBuild(prompt)`**: gọi 1 lượt với timeout 20s → trả `Result<String>` chứa JSON cấu hình PC.
- **`chatWithAi(history, message)`**: chat đa lượt với 10 tin nhắn gần nhất, timeout 15s.
- **`mapFirebaseAiError`**: dịch lỗi sang tiếng Việt thân thiện (App Check denied, quota 429, network error...).

App Check + Firebase AI là một combo bảo mật quan trọng: **không ai gọi được endpoint AI của bạn từ ngoài app**, tránh bị lạm dụng quota Gemini (vốn rất tốn tiền).

### 4.5. Quy ước bảo mật khuyến nghị (Firestore Rules)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Catalog phần cứng: ai cũng đọc được, chỉ admin ghi
    match /cpus/{id} { allow read: if true; allow write: if false; }
    match /gpus/{id} { allow read: if true; allow write: if false; }
    match /motherboards/{id} { allow read: if true; allow write: if false; }
    match /ram/{id} { allow read: if true; allow write: if false; }
    match /psu/{id} { allow read: if true; allow write: if false; }

    // Hồ sơ: chỉ chủ sở hữu được đọc/sửa
    match /users/{uid} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }

    // Benchmark cá nhân: user chỉ ghi document có uid của mình
    match /user_benchmarks/{doc} {
      allow read:  if request.auth != null && resource.data.uid == request.auth.uid;
      allow create: if request.auth != null && request.resource.data.uid == request.auth.uid;
    }

    // Leaderboard: ai cũng đọc, chỉ chủ sở hữu được tạo
    match /leaderboard_scores/{doc} {
      allow read:  if true;
      allow write: if request.auth != null && request.resource.data.uid == request.auth.uid;
    }
  }
}
```

---

## 5. LUỒNG NGHIỆP VỤ TIÊU BIỂU (END-TO-END)

### 5.1. Luồng "Benchmark → Upload → Bảng xếp hạng"
```
[User mở app]
   │
   ▼
[App Check token được cấp]
   │
   ▼
[Login Google] ──► FirebaseAuth ──► Lấy uid ──► saveOrUpdateUser() vào /users
   │                                                     │
   ▼                                                     ▼
[Vào BenchmarkScreen]                          [SessionManager lưu phiên local]
   │
   ▼
[Đếm ngược 3s → Render 30s → BenchmarkRenderer thu thập FPS]
   │
   ▼
[calculateResult: score = avgFps × 1500, tier]
   │
   ▼
[uploadResult]
   │
   ├──► Thêm document vào /user_benchmarks (lịch sử)
   └──► Set/merge document /leaderboard_scores/{uid}_{deviceModel}
            (chỉ ghi đè nếu điểm mới > điểm cũ)
   │
   ▼
[LeaderboardScreen của tất cả user khác cập nhật real-time qua snapshotListener]
```

### 5.2. Luồng "Người dùng cài lại app"
```
1. User gỡ app → mọi DataStore local bị xoá.
2. User cài lại app → mở lên → AuthViewModel thấy DataStore rỗng → vào màn login.
3. User đăng nhập lại bằng cùng Google account → FirebaseAuth trả về CÙNG uid.
4. UserProfileViewModel query /user_benchmarks where uid == currentUid
   → toàn bộ lịch sử benchmark hiện trở lại.
5. LeaderboardScreen vẫn thấy điểm số cũ của user trong /leaderboard_scores.
```
→ **Đây chính là điểm khác biệt giải quyết "điểm yếu cố hữu" mà bạn đã đặt làm mục tiêu của dự án.**

### 5.3. Luồng "AI gợi ý cấu hình PC"
```
[BuildRigScreen — user nhập budget, mục đích, resolution...]
   │
   ▼
[PCBuilderViewModel.suggestBuildWithAi()]
   │
   ▼
[buildAiPrompt() — ghép prompt KÈM toàn bộ catalog Firestore đã load sẵn]
   │
   ▼
[BuildAiRepository.suggestBuild(prompt)]
   │
   ▼
[Firebase AI Logic ──(App Check token)──► Gemini 3 Flash Preview]
   │
   ▼
[Trả JSON {cpu, gpu, motherboard, ram, psu, reason}]
   │
   ▼
[autoFillFromRecommendation() — match tên với catalog local]
   │
   ▼
[runCompatibilityEngine() — kiểm tra socket, ram type, PSU watt]
   │
   ▼
[UI hiển thị build score + cảnh báo tương thích]
```

---

## 6. ĐIỂM NỔI BẬT VỀ KỸ THUẬT

1. **Reactive UI hoàn toàn**: tất cả ViewModel dùng `MutableStateFlow` → Composable `collectAsState()` → UI tự render lại khi state đổi, không cần `notifyDataSetChanged`.
2. **Unidirectional Data Flow**: dữ liệu chảy từ Repository → ViewModel → UI; sự kiện chảy ngược lại qua các hàm public của ViewModel.
3. **Dependency Injection thủ công** qua `AppViewModelFactory` (không dùng Hilt/Dagger để giữ project gọn).
4. **Resilient Firestore listeners**: `callbackFlow + awaitClose` đảm bảo subscription được tháo gỡ khi ViewModel bị huỷ → không leak.
5. **App Check tách Debug/Release** đúng quy chuẩn của Google → vừa dev được trên emulator, vừa an toàn khi phát hành.
6. **Compatibility Engine viết tay**: kiểm tra socket / RAM type / PSU power, có 3 trạng thái OK / WARNING / ERROR — minh hoạ kiến thức phần cứng PC.
7. **Bottleneck algorithm** có trọng số theo độ phân giải, được "soften" và "clamp" để tránh báo bottleneck cực đoan — sản phẩm gần thực tế.
8. **AI prompt engineering**: prompt build bao gồm toàn bộ catalog → Gemini không thể "bịa" linh kiện không có trong DB; kết quả luôn map lại được vào local catalog.
9. **Bảo mật nhiều lớp**: App Check + Firebase Auth + Firestore Rules + Lưu Server Client ID (không phải API key Gemini) ở client.

---

## 7. HẠN CHẾ & HƯỚNG PHÁT TRIỂN

| Hạn chế hiện tại | Hướng cải thiện |
|---|---|
| Catalog phần cứng phụ thuộc admin import thủ công | Tích hợp Cloud Function pull từ API như TechPowerUp |
| Mật khẩu signing key đang để trong `build.gradle.kts` | Chuyển sang biến môi trường / `local.properties` |
| Chưa có offline cache cho Firestore | Bật `setPersistenceEnabled(true)` |
| Chưa unit test cho thuật toán bottleneck/compatibility | Viết unit test cho ViewModel với MockK |
| Score benchmark đơn giản (avgFPS × 1500) | Thêm bài test GPU thực sự (OpenGL / Vulkan) |
| Chưa có cơ chế xoá tài khoản (GDPR) | Bổ sung `deleteAccount()` xoá document `/users/{uid}` + dữ liệu liên quan |

---

## 8. KẾT LUẬN

My PhoneC là một dự án Android end-to-end, kết hợp:
- **Phần native Android**: đọc thông tin phần cứng, cảm biến, pin, màn hình, đo FPS.
- **Phần cloud (Firebase)**: Auth, Firestore, App Check, AI Logic — giải quyết bài toán bền vững dữ liệu và bảo mật.
- **Phần thuật toán**: Compatibility Engine, Bottleneck Calculator.
- **Phần AI**: Gemini hỗ trợ gợi ý cấu hình PC theo ngữ cảnh.

Dự án minh hoạ rõ một kiến trúc Android hiện đại (MVVM + Compose + Coroutines + StateFlow), khả năng tích hợp dịch vụ đám mây, và tư duy giải quyết vấn đề thực tế (chống mất dữ liệu khi gỡ app, chống lạm dụng AI bằng App Check).
