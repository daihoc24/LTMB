# InstagramKotlin

Ứng dụng Android native Kotlin + Jetpack Compose thay thế dần frontend React Native `InstagramClone`.

## Yêu cầu

- Android Studio có JDK 17.
- Android SDK Platform 34.
- Android Emulator API 24 trở lên.

## Mở và chạy

1. Mở thư mục `D:\LTMB\InstagramKotlin` bằng Android Studio.
2. Chờ Gradle Sync hoàn tất.
3. Khởi động emulator.
4. Chọn cấu hình `app` và nhấn Run.

Build từ terminal Windows:

```powershell
cd D:\LTMB\InstagramKotlin
.\gradlew.bat assembleDebug
```

APK debug được tạo tại `app\build\outputs\apk\debug\app-debug.apk`.

## Lưu ý applicationId

Project giữ `applicationId = com.instagramclone` để thay thế ứng dụng cũ. Vì vậy React Native APK và Kotlin APK không nên cài đồng thời; hãy gỡ bản đang cài trước khi chuyển bản.

## Cấu hình local

- HTTP cleartext chỉ được cho phép trong debug đối với `10.0.2.2` và `localhost`.
- Debug dùng `127.0.0.1:8080/8082` qua `adb reverse` để tránh Windows Firewall chặn Android Emulator.
- Không commit `local.properties`, secret hoặc API key.

## Trạng thái U-01

U-01 cung cấp app shell, Material 3 theme, Hilt và 5 tab placeholder: Home, Search, Post, Notifications và Profile. Các màn hình nghiệp vụ sẽ thay placeholder ở unit tiếp theo.

## Networking và session (U-02)

- Core API debug: `http://10.0.2.2:8080/chat-application/`.
- Khi app mở, session core đọc encrypted JWT. Có token thì gọi `GET v1/users/my-info`; không có token thì mở Auth.
- Mất mạng/server lỗi giữ token và hiển thị Retry; HTTP 401/403 hoặc Logout xóa token.
- JWT được mã hóa AES/GCM bằng Android Keystore và bị loại khỏi Android backup.
- Login/registration ở U-03 sẽ gọi `SessionRepository.establishSession(token)` sau khi nhận JWT.

## Authentication và registration (U-03)

- Sign In dùng `POST v1/auth/token`, sau đó U-02 lưu token và gọi `my-info`.
- Registration: Email → OTP → Password → Birthday → Username → Terms.
- OTP được gửi qua `POST v1/verification/send-code` và xác minh bằng GET `v1/verification/verify`.
- Tạo tài khoản bằng `POST v1/users`, sau đó quay lại Sign In.
- Password/OTP không được lưu xuống disk; response OTP từ backend không được hiển thị hoặc tự điền.

## Camera và chọn ảnh (U-08)

- Tab Post cho phép chụp ảnh bằng CameraX hoặc chọn tối đa 10 ảnh bằng Android Photo Picker.
- Ảnh camera nằm trong cache riêng và được chia sẻ bằng content URI qua FileProvider; chụp lại/hủy sẽ dọn file tạm.
- App chỉ xin quyền CAMERA khi người dùng mở camera và không xin quyền đọc bộ nhớ.
- Đây là luồng chọn media tối thiểu; caption, upload và tạo bài viết thuộc U-04.

## Feed và đăng bài (U-04)

- Home tải bài viết hiển thị từ backend, ghép ảnh Cloudinary theo folder và sắp xếp bài mới trước.
- Nút làm mới xử lý trạng thái loading, rỗng và lỗi mạng.
- Tab Post nhận caption cùng tối đa 10 ảnh từ U-08, tạo bài viết và upload multipart bằng stream.
- Đăng thành công sẽ dọn draft/media tạm và yêu cầu Home tải lại.
- Like/comment/chỉnh sửa bài viết được để cho U-05.

## Social, tìm kiếm và profile (U-05 MVP)

- Feed hỗ trợ like/unlike, số lượt thích, xem/gửi và xóa bình luận của chính mình.
- Search tìm user theo username hoặc post theo caption; kết quả user hỗ trợ follow/unfollow.
- Profile cá nhân hiển thị avatar, username, email, follower/following và sửa username/privacy.
- Comment REST dùng service 8082; social/profile dùng core API 8080.
- QR, avatar upload, profile người khác dạng lưới, sửa comment và quản lý post nâng cao là gap U-09 nếu đồ án yêu cầu trình diễn.

## Notifications (U-06 MVP)

- Tab Notifications tải danh sách theo user hiện tại, có loading/error/empty/refresh.
- Push OneSignal chưa bật trong Kotlin vì project không có native push configuration đáng tin cậy; đây là gap U-09.

## Direct chat (U-07 MVP)

- Nút tin nhắn trên Home mở danh sách người đang follow và lịch sử chat trực tiếp.
- REST 8082 tải contacts/history; SockJS-STOMP gửi qua `/app/chat` và nghe `/topic/messages`.
- Broadcast backend thiếu receiver ID nên client reload history của hội thoại hiện tại thay vì chèn payload trực tiếp.
- Có nút kết nối lại thủ công. Group chat và auto-reconnect đầy đủ chuyển U-09 vì backend group-list đang disabled.

## Tích hợp cuối (U-09)

- Chủ bài viết có thể sửa caption hoặc ẩn bài ngay trên feed.
- Debug chỉ cho phép cleartext tới `10.0.2.2`/localhost; release dùng URL placeholder an toàn và cần cấu hình server thật trước khi phát hành.
- APK demo cần đồng thời chạy backend 8080, realtime 8082 và Android emulator.
- Checklist demo đầy đủ nằm trong tài liệu AI-DLC nội bộ và không được commit.
