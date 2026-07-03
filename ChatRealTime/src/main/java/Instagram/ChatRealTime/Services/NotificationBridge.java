package Instagram.ChatRealTime.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationBridge {
    private static final Logger log = LoggerFactory.getLogger(NotificationBridge.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String CORE_URL = "http://127.0.0.1:8080/chat-application/v1/notification/add";

    public void notify(String userId, String title, String message) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(CORE_URL)
                    .queryParam("userId", userId)
                    .queryParam("title", title)
                    .queryParam("message", message)
                    .toUriString();
            restTemplate.postForLocation(url, null);
        } catch (Exception exception) {
            log.warn("Khong tao duoc thong bao cho user {}: {}", userId, exception.getMessage());
            // Không làm hỏng thao tác bình luận/tin nhắn nếu push tạm thời lỗi.
        }
    }

    public void comment(String actorId, int postId, int parentCommentId) {
        try {
            String url = UriComponentsBuilder.fromHttpUrl(
                            "http://127.0.0.1:8080/chat-application/v1/notification/comment-event")
                    .queryParam("actorId", actorId)
                    .queryParam("postId", postId)
                    .queryParam("parentCommentId", parentCommentId)
                    .toUriString();
            restTemplate.postForLocation(url, null);
        } catch (Exception exception) {
            log.warn("Khong tao duoc thong bao binh luan (postId={}): {}", postId, exception.getMessage());
        }
    }
}
