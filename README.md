# VMN Máy Tính Đa Năng (Kotlin & Jetpack Compose)

Ứng dụng máy tính đa năng thuần Android được xây dựng bằng **Kotlin** và **Jetpack Compose** theo phong cách thiết kế **Liquid Glass**, phát triển từ ứng dụng web **VMN Máy Tính Đa Năng v2.0** của **VMN Studios 95**[span_0](start_span)[span_0](end_span).

---

## Tính năng nổi bật

1. **Máy tính chuỗi (`StandardCalculatorTab`)**[span_1](start_span)[span_1](end_span)
   * Bộ phân tích cú pháp đệ quy xuống (`MathExpressionEngine`) hỗ trợ đầy đủ phép toán `+`, `-`, `×`, `÷`, `%`, dấu ngoặc `(...)` và nhân ngầm định (ví dụ: `2(3+4)`)[span_2](start_span)[span_2](end_span).
   * Tự động xử lý và làm sạch sai số dấu phẩy động chuẩn IEEE-754[span_3](start_span)[span_3](end_span).
2. **Tính tiền theo cân nặng (`WeightCalculatorTab`)**[span_4](start_span)[span_4](end_span)
   * Hỗ trợ quy đổi đa đơn vị: `kg`, `lạng` (tùy chỉnh tỉ lệ quy đổi sang kg), `g`, `yến`, `tạ`, `tấn`[span_5](start_span)[span_5](end_span).
   * Tính thành tiền tự động theo đơn giá/kg chuẩn[span_6](start_span)[span_6](end_span).
3. **Tính tiền theo giờ làm (`HoursCalculatorTab`)**[span_7](start_span)[span_7](end_span)
   * Nhập linh hoạt số tiếng, số phút lẻ, tiền công/tiếng và hệ số tăng ca (`1`, `1.5`, `2`...)[span_8](start_span)[span_8](end_span).
4. **Tính tiền theo số lít (`LiterCalculatorTab`)**[span_9](start_span)[span_9](end_span)
   * Hỗ trợ tính toán theo `lít`, `ml` và `can` (tùy chỉnh dung tích can chuẩn)[span_10](start_span)[span_10](end_span).
5. **Tùy biến nâng cao (`AdvancedSettingsTab`)**[span_11](start_span)[span_11](end_span)
   * Cho phép thay đổi tên ứng dụng, dòng mô tả ngắn, đơn giá mặc định và các hệ số quy đổi (`1 lạng = ? kg`, `1 can = ? lít`)[span_12](start_span)[span_12](end_span).
   * Hỗ trợ khôi phục cấu hình mặc định chỉ với một chạm[span_13](start_span)[span_13](end_span).
6. **Lịch sử tính toán tự động (`HistoryTab`)**[span_14](start_span)[span_14](end_span)
   * Tự động lưu trữ tối đa **250 phép tính** gần nhất vào bộ nhớ cục bộ (`SharedPreferences`) kèm phân loại và dấu thời gian[span_15](start_span)[span_15](end_span).
   * Hỗ trợ xóa từng mục riêng lẻ hoặc xóa toàn bộ lịch sử[span_16](start_span)[span_16](end_span).
7. **Cài đặt giao diện & Đồng bộ dữ liệu (`SettingsTab`)**[span_17](start_span)[span_17](end_span)
   * **2 chế độ giao diện**: *Dễ thương (kèm bộ icon con vật 🐶🐻🦊🐸🐼🦉🐨)* và *Giao diện bình thường*[span_18](start_span)[span_18](end_span).
   * **4 kiểu phông chữ**: Bo tròn dễ thương, Gọn hiện đại, Mặc định hệ thống, Chữ đều (Mono)[span_19](start_span)[span_19](end_span).
   * **Tỷ lệ cỡ chữ**: Tùy chỉnh linh hoạt từ `85%` đến `125%`[span_20](start_span)[span_20](end_span).
   * **Sao lưu & Khôi phục**: Xuất và nạp tệp cấu hình kèm lịch sử định dạng `.json` thông qua Storage Access Framework (`CreateDocument` / `OpenDocument`)[span_21](start_span)[span_21](end_span).
8. **Giới thiệu & Cộng đồng (`AboutTab`)**[span_22](start_span)[span_22](end_span)
   * Hiển thị thông tin phiên bản, sao chép nhanh liên kết GitHub và Email liên hệ của nhà phát triển[span_23](start_span)[span_23](end_span).

---

## Cấu trúc dự án

```text
VMN_May_Tinh_Da_Nang_Android/
├── app/
│   └── src/main/
│       ├── java/com/vmnstudios95/maytinhdanang/
│       │   ├── MainActivity.kt                  # Điểm khởi chạy chính, điều hướng HorizontalPager & Toast
│       │   ├── data/
│       │   │   └── LocalStateStorage.kt         # Quản lý SharedPreferences & xuất/nhập JSON qua SAF
│       │   ├── engine/
│       │   │   └── MathExpressionEngine.kt      # Tokenizer, Recursive Descent Parser & định dạng tiền tệ VNĐ
│       │   ├── model/
│       │   │   └── AppModels.kt                 # Các model dữ liệu, cấu hình tab & serialize JSON
│       │   └── ui/
│       │       ├── components/
│       │       │   └── GlassComponents.kt       # Hệ thống UI components hiệu ứng Liquid Glass
│       │       ├── screens/
│       │       │   └── CalculatorTabs.kt        # Giao diện và logic xử lý cho 8 tab chức năng
│       │       └── theme/
│       │           └── Theme.kt                 # Bảng màu Cute/Normal, Typography & tỷ lệ cỡ chữ
│       └── res/                                 # Tài nguyên chuỗi (strings.xml) và giao diện (themes.xml)
├── build.gradle.kts
└── settings.gradle.kts
```[span_24](start_span)[span_24](end_span)

---

## Yêu cầu hệ thống & Công nghệ sử dụng

* **Ngôn ngữ**: Kotlin `1.9.22`[span_25](start_span)[span_25](end_span)
* **Giao diện**: Jetpack Compose (Compose BOM `2024.02.00`), Material 3[span_26](start_span)[span_26](end_span)
* **SDK**: `minSdk = 24` (Android 7.0) | `compileSdk = 34` | `targetSdk = 34`[span_27](start_span)[span_27](end_span)
* **Công cụ build**: Android Gradle Plugin `8.2.2`, Gradle `8.4`, JDK `17`[span_28](start_span)[span_28](end_span)

---

## Hướng dẫn cài đặt & Build

1. **Clone hoặc giải nén mã nguồn**:[span_29](start_span)[span_29](end_span)
   ```bash
   git clone [https://github.com/vuminhngocpt/VMN_May_Tinh_Da_Nang_Kotlin.git](https://github.com/vuminhngocpt/VMN_May_Tinh_Da_Nang_Kotlin.git)
