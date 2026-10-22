# AppBooking

App Android đặt phòng khách sạn của **Nhóm 2**, viết bằng **Java**, build bằng **Gradle (Groovy DSL)**.
App dùng chung backend với website WebBooking:

| Thành phần | Địa chỉ |
|---|---|
| Backend (Spring Boot, Render) | https://webbooking-c5iu.onrender.com |
| Website (React, Vercel) | https://ript-1307-03-2026-nhom2-kthp.vercel.app |
| Repo backend + web | https://github.com/NguyenDuyTu275/WebBooking |

## Tính năng

- Đăng ký (mã OTP gửi qua email), đăng nhập, đăng xuất
- Danh sách khách sạn, tìm theo tên / thành phố không cần gõ dấu ("ha noi" ra "Hà Nội")
- Chi tiết khách sạn: ảnh, mô tả, các phòng, đánh giá của khách
- Đặt phòng: chọn ngày nhận – trả phòng, tự tính số đêm và tổng tiền
- Thanh toán chuyển khoản:
  - **Mã QR** VietQR (quét bằng app ngân hàng bất kỳ)
  - **App ngân hàng**: chọn ngân hàng, app ngân hàng mở ra; các app có nhãn "Tự điền" điền sẵn STK, số tiền, nội dung
  - Sepay báo tiền về cho backend, app tự kiểm tra và báo **Thanh toán thành công**
- Đơn của tôi: trạng thái đơn và thanh toán, thanh toán tiếp hoặc hủy đơn
- Chat hỗ trợ với nhân viên (tự cập nhật vài giây một lần)

## Công nghệ

- Java 17, Android SDK 37 (chạy từ Android 8.0 – minSdk 26)
- Gradle 9.5 + Android Gradle Plugin 9.3 (Groovy: `settings.gradle`, `build.gradle`, `app/build.gradle`)
- Giao diện: XML layout + View Binding, Material 3, RecyclerView
- Mạng: Retrofit 2 + OkHttp + Gson; ảnh: Glide
- Kiểm thử: JUnit 4

```
app/src/main/java/com/nhom2/appbooking/
├── data/          ApiClient, ApiService (Retrofit), SessionManager (JWT), model/
├── ui/            auth/, home/, hotel/, booking/, payment/, bookings/, chat/, account/
└── util/          Formatters, TextSearch, BankAppLinks, ImageUrls (thuần Java, có unit test)
```

## Mở bằng Android Studio

1. Cài **Android Studio** (đã kèm Android SDK và JDK).
2. **File → Open** → chọn thư mục project → chờ **Gradle Sync** xong.
3. Chọn máy ảo (Device Manager) hoặc điện thoại bật USB debugging → bấm **Run ▶**.

Mặc định app gọi backend trên Render nên chạy được ngay.

## Chạy với backend trên máy tính

Tạo / sửa file `local.properties` (không commit):

```properties
api.baseUrl=http://localhost:8080
```

rồi chuyển cổng 8080 của máy ảo / điện thoại sang máy tính:

```bash
adb reverse tcp:8080 tcp:8080
```

> Không dùng `http://10.0.2.2:8080`: Android 17 chặn app (targetSdk 37) truy cập địa chỉ mạng nội bộ
> nếu không xin quyền riêng, nên request bị treo. `localhost` + `adb reverse` không bị chặn và
> dùng được cả với điện thoại thật cắm cáp USB. Bản debug cho phép `http` tới `localhost`.

## Build và test bằng dòng lệnh

```bash
./gradlew testDebugUnitTest   # unit test
./gradlew assembleDebug       # APK: app/build/outputs/apk/debug/app-debug.apk
```

## CI

Mỗi lần push / Pull Request, GitHub Actions chạy unit test và build APK debug.
Tải APK ở tab **Actions** → lần chạy → mục **Artifacts** → `AppBooking-debug-apk`.

## Ghi chú

- Mở app ngân hàng dùng deeplink của VietQR (`dl.vietqr.io`). Chỉ kiểm chứng được trên điện thoại thật có cài
  app ngân hàng; trên máy ảo link sẽ mở trong trình duyệt.
- Các app không có nhãn "Tự điền" (vd TPBank, VCB, Techcombank) chỉ được mở lên: app tự sao chép nội dung
  `DH <mã đơn>` để khách dán khi chuyển khoản (Sepay dựa vào nội dung này để xác nhận).
- Chức năng quản trị (admin) dùng trên website.
