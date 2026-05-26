//package com.chat_application.ChatApplication;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class ChatApplication {
//    public static void main(String[] args) {
//        SpringApplication.run(ChatApplication.class, args);
//    }
//}
package com.chat_application.ChatApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ChatApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ChatApplication.class, args);

        // ✅ Lấy thông tin server
        if (context instanceof ServletWebServerApplicationContext webCtx) {
            int port = webCtx.getWebServer().getPort();
            String contextPath = webCtx.getServletContext().getContextPath();
            if (contextPath == null || contextPath.isEmpty()) {
                contextPath = "/";
            }

            System.out.println("========================================");
            System.out.println("🚀 ChatApplication is running!");
            System.out.println("👉 Access URLs:");
            System.out.println("   Local:      http://localhost:" + port + contextPath);
            System.out.println("   External:   http://" + getHostAddress() + ":" + port + contextPath);
            System.out.println("========================================");
        }
    }

    private static String getHostAddress() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "localhost";
        }
    }
}
