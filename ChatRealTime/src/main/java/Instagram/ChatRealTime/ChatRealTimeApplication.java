//package Instagram.ChatRealTime;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class ChatRealTimeApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(ChatRealTimeApplication.class, args);
//	}
//
//}
package Instagram.ChatRealTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ChatRealTimeApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(ChatRealTimeApplication.class, args);

		// ✅ Ép kiểu để lấy ServletContext (đúng cách)
		if (context instanceof ServletWebServerApplicationContext webCtx) {
			int port = webCtx.getWebServer().getPort();
			String contextPath = webCtx.getServletContext().getContextPath();
			if (contextPath == null || contextPath.isEmpty()) {
				contextPath = "/";
			}

			System.out.println("========================================");
			System.out.println("🚀 ChatRealTime Application is running!");
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
