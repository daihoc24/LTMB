package Instagram.ChatRealTime.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentResponse {
    int id;
    Integer postId;
    UUID userId;
    String content;
    String username;
    String avatar;
    Integer preComment;
    boolean visible;
    Timestamp createdAt;
    Timestamp updatedAt;
}
