package com.chat_application.ChatApplication.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

    private static final String DEFAULT_FIREBASE_CONFIG_PATH = "firebase/webblog-6eee4-firebase-adminsdk-3ja5x-89dda28363.json";
    private static final String DEFAULT_STORAGE_BUCKET = "webblog-6eee4.appspot.com";
    private static volatile boolean initialized = false;

    @Value("${firebase.config.path:}")
    private String firebaseConfigPath;

    @Value("${firebase.storage.bucket:}")
    private String storageBucket;

    /**
     * Khởi tạo Firebase nếu chưa được khởi tạo và file config tồn tại.
     * Method này sẽ không throw exception nếu file không tồn tại.
     * @return true nếu Firebase đã được khởi tạo thành công, false nếu không
     */
    public boolean initializeIfAvailable() {
        // Kiểm tra nếu FirebaseApp đã được khởi tạo
        if (!FirebaseApp.getApps().isEmpty()) {
            if (!initialized) {
                log.info("FirebaseApp already initialized");
                initialized = true;
            }
            return true;
        }

        if (initialized) {
            return false; // Đã thử khởi tạo nhưng thất bại
        }

        log.info("Attempting to initialize FirebaseApp...");
        
        InputStream serviceAccount = null;
        String configPath = firebaseConfigPath != null && !firebaseConfigPath.isEmpty() 
            ? firebaseConfigPath 
            : DEFAULT_FIREBASE_CONFIG_PATH;
        
        try {
            // Thử load từ classpath trước (khi chạy từ JAR)
            serviceAccount = getClass().getClassLoader().getResourceAsStream(configPath);
            
            if (serviceAccount != null) {
                log.info("Firebase config loaded from classpath: {}", configPath);
            } else {
                // Nếu không tìm thấy trong classpath, thử load từ file system (khi chạy từ IDE)
                log.debug("Firebase config not found in classpath, trying file system...");
                try {
                    String fileSystemPath = "src/main/resources/" + configPath;
                    serviceAccount = new FileInputStream(fileSystemPath);
                    log.info("Firebase config loaded from file system: {}", fileSystemPath);
                } catch (IOException e) {
                    // Thử load từ đường dẫn tuyệt đối nếu được cung cấp qua environment variable
                    if (firebaseConfigPath != null && !firebaseConfigPath.isEmpty()) {
                        try {
                            serviceAccount = new FileInputStream(firebaseConfigPath);
                            log.info("Firebase config loaded from absolute path: {}", firebaseConfigPath);
                        } catch (IOException e2) {
                            log.warn("Firebase configuration file not found. Firebase features will be disabled.");
                            log.warn("To enable Firebase, please add the config file. See README.md in firebase folder for details.");
                            initialized = true; // Đánh dấu đã thử
                            return false;
                        }
                    } else {
                        log.warn("Firebase configuration file not found. Firebase features will be disabled.");
                        log.warn("To enable Firebase, please add the config file. See README.md in firebase folder for details.");
                        initialized = true; // Đánh dấu đã thử
                        return false;
                    }
                }
            }
            
            String bucket = storageBucket != null && !storageBucket.isEmpty() 
                ? storageBucket 
                : DEFAULT_STORAGE_BUCKET;
            
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket(bucket)
                .build();

            FirebaseApp.initializeApp(options);
            log.info("FirebaseApp initialized successfully with storage bucket: {}", bucket);
            initialized = true;
            return true;
            
        } catch (IOException e) {
            log.warn("Failed to initialize FirebaseApp: {}. Firebase features will be disabled.", e.getMessage());
            log.warn("To enable Firebase, please add the config file. See README.md in firebase folder for details.");
            initialized = true; // Đánh dấu đã thử
            return false;
        } finally {
            if (serviceAccount != null) {
                try {
                    serviceAccount.close();
                } catch (IOException e) {
                    log.warn("Failed to close Firebase config stream", e);
                }
            }
        }
    }
}
