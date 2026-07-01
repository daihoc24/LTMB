package Instagram.ChatRealTime.Dto.Request;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class MessageReponse {
    int id;
    boolean visible; // true là trạng thái bị block con false là trang thái bình thường
    String content;
    Timestamp createdAt;
    @NonNull
    UUID userIdSend;
    Long groupChatId; // chứa id nhóm chat
    String avatar;

}

