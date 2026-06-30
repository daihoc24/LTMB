package com.chat_application.ChatApplication.Controllers.v1;

import com.chat_application.ChatApplication.Configuration.FirebaseConfig;
import com.chat_application.ChatApplication.Dto.Request.*;
import com.chat_application.ChatApplication.Dto.Response.ApiResponse;
import com.chat_application.ChatApplication.Dto.Response.InfoUserResp;
import com.chat_application.ChatApplication.Dto.Response.UserResponse;
import com.chat_application.ChatApplication.Services.UserService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/users")
public class UserController {
    UserService userService;
    FirebaseConfig firebaseConfig;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreateReq req) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createUser(req))
                .build();
    }

    @PostMapping("/updateInfo")
    ApiResponse<InfoUserResp> updateInfoUser(@RequestBody InfoUserReq req) {
        return ApiResponse.<InfoUserResp>builder()
                .result(userService.updateInfoUser(req))
                .build();
    }

    @PostMapping("/updateAvat")
    public ResponseEntity<String> updateProfilePicture(@RequestParam("file") MultipartFile file,
                                                       @RequestParam("username") String username,
                                                       HttpServletRequest request) {
        try {
            System.out.println("=== UPDATE AVATAR REQUEST ===");
            System.out.println("Username: " + username);
            System.out.println("File name: " + (file != null ? file.getOriginalFilename() : "null"));
            System.out.println("File size: " + (file != null ? file.getSize() : "null"));
            System.out.println("File content type: " + (file != null ? file.getContentType() : "null"));
            System.out.println("File is empty: " + (file != null ? file.isEmpty() : "null"));
            
            // Kiểm tra xem file có rỗng hay không
            if (file == null || file.isEmpty()) {
                System.out.println("ERROR: File is null or empty");
                return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
            }

            System.out.println("Uploading image...");
            String imageUrl = uploadImage(file); // Gọi hàm upload hình ảnh
            
            // Fallback: Nếu Firebase không có, lưu vào local server
            if (imageUrl == null) {
                System.out.println("Firebase not available, using local storage fallback...");
                imageUrl = uploadImageToLocal(file, request);
                if (imageUrl == null) {
                    System.out.println("ERROR: Failed to upload image");
                    return new ResponseEntity<>("Failed to upload image", HttpStatus.INTERNAL_SERVER_ERROR);
                }
                System.out.println("Image saved to local storage: " + imageUrl);
            } else {
                System.out.println("Image uploaded successfully to Firebase. URL: " + imageUrl);
            }

            AvatUserReq avatUserReq = AvatUserReq.builder()
                    .username(username)
                    .file(imageUrl)
                    .build();

            System.out.println("Updating user avatar in database...");
            // Lưu imageUrl vào cơ sở dữ liệu của user với userId tương ứng
            userService.updateAvatUser(avatUserReq); // Gọi hàm updateAvatUser trong UserService
            System.out.println("User avatar updated successfully");
            System.out.println("=== END UPDATE AVATAR REQUEST ===");

            return new ResponseEntity<>(imageUrl, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("=== EXCEPTION IN updateProfilePicture ===");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== END EXCEPTION ===");
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String uploadImage(MultipartFile file) {
        try {
            System.out.println("=== UPLOAD IMAGE TO FIREBASE ===");
            System.out.println("Original filename: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize() + " bytes");
            System.out.println("Content type: " + file.getContentType());
            
            // Thử khởi tạo Firebase nếu chưa được khởi tạo
            if (FirebaseApp.getApps().isEmpty()) {
                System.out.println("Firebase not initialized, attempting to initialize...");
                if (!firebaseConfig.initializeIfAvailable()) {
                    System.err.println("ERROR: Firebase is not initialized. Please add Firebase configuration file.");
                    System.err.println("See README.md in src/main/resources/firebase/ for instructions.");
                    System.err.println("File should be placed at: src/main/resources/firebase/webblog-6eee4-firebase-adminsdk-3ja5x-89dda28363.json");
                    return null;
                }
            }
            
            // Generate a random file name
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            System.out.println("Generated filename: " + fileName);

            // Get bucket from Firebase Storage
            System.out.println("Getting Firebase Storage bucket...");
            Bucket bucket = StorageClient.getInstance().bucket();
            System.out.println("Bucket name: " + bucket.getName());

            // Upload file to Firebase Storage
            System.out.println("Uploading file to Firebase Storage...");
            byte[] fileBytes = file.getBytes();
            System.out.println("File bytes length: " + fileBytes.length);
            
            Blob blob = bucket.create(fileName, fileBytes, file.getContentType());
            System.out.println("File uploaded successfully. Blob name: " + blob.getName());

            // Get the public download URL
            String fileUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucket.getName(),
                    URLEncoder.encode(fileName, StandardCharsets.UTF_8));
            System.out.println("Generated file URL: " + fileUrl);
            System.out.println("=== END UPLOAD IMAGE ===");

            return fileUrl; // Trả về URL
        } catch (IOException e) {
            System.err.println("=== IO EXCEPTION IN uploadImage ===");
            System.err.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== END IO EXCEPTION ===");
            return null; // Trả về null khi có lỗi
        } catch (Exception e) {
            System.err.println("=== EXCEPTION IN uploadImage ===");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== END EXCEPTION ===");
            return null;
        }
    }

    /**
     * Fallback method: Lưu file vào local server khi Firebase không có
     */
    private String uploadImageToLocal(MultipartFile file, HttpServletRequest request) {
        try {
            System.out.println("=== UPLOAD IMAGE TO LOCAL STORAGE ===");
            System.out.println("Original filename: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize() + " bytes");
            
            // Tạo thư mục uploads nếu chưa có
            String uploadDir = "src/main/resources/static/uploads/avatars";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created directory: " + uploadDir);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(fileName);
            
            // Lưu file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("File saved to: " + filePath.toAbsolutePath());
            
            // Tạo URL để truy cập file từ request
            String scheme = request.getScheme(); // http hoặc https
            String serverName = request.getServerName(); // localhost hoặc IP
            int serverPort = request.getServerPort(); // 8080
            String contextPath = request.getContextPath(); // /chat-application
            
            // Build full URL
            String imageUrl = String.format("%s://%s:%d%s/uploads/avatars/%s", 
                scheme, serverName, serverPort, contextPath, fileName);
            System.out.println("Image URL: " + imageUrl);
            System.out.println("=== END UPLOAD IMAGE TO LOCAL STORAGE ===");
            
            return imageUrl;
        } catch (IOException e) {
            System.err.println("=== EXCEPTION IN uploadImageToLocal ===");
            System.err.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("=== END EXCEPTION ===");
            return null;
        }
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUser(@PathVariable String id) {

        return ApiResponse.<UserResponse>builder()
                .result(userService.get(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<UserResponse> update(@PathVariable String id, @RequestBody UserReq request) {

        return ApiResponse.<UserResponse>builder()
                .result(userService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable String id) {
        userService.delete(id);

        return ApiResponse.<Void>builder()
                .message("'" + id + "' was deleted")
                .build();
    }

    @PostMapping("/delete")
    ApiResponse<Void> deleteUser() {
        // Xóa user hiện tại (từ token)
        var user = userService.getMyInfo();
        userService.delete(user.getId().toString());
        return ApiResponse.<Void>builder()
                .message("User was deleted")
                .build();
    }

    @GetMapping("/my-info")
    ApiResponse<UserResponse> getMyInfo() {
        return ApiResponse.<UserResponse>builder().result(userService.getMyInfo()).build();
    }

    @PostMapping("/alluser")
    List<UserResponse> allUser() {
        return userService.allUser();
    }

    @PostMapping("/alluserNum")
    int alluserNum() {
        return userService.alluserNum();
    }

    @PostMapping("/alluserInMonth")
    int alluserInMonth() {
        return userService.alluserInMonth();
    }

    @PostMapping("/alluserInDay")
    int alluserInDay() {
        return userService.alluserInDay();
    }

    @PostMapping("/lockAccount")
    void allUser(@RequestBody UsernameRequest request) {
        userService.lockAccount(request);
    }

    @PostMapping("/createAdmin")
    ApiResponse<UserResponse> createAdmin(@RequestBody @Valid AdminCreateReq req) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.createAdmin(req))
                .message("Create admin successfully")
                .build();
    }

    @PostMapping("/requireActivateAccount")
    ApiResponse<Void> requireActivateAccount(@RequestParam String emailorUsername) {
        userService.requireActivateAccount(emailorUsername);
        return ApiResponse.<Void>builder()
                .message("Activation email sent successfully")
                .build();
    }

}
