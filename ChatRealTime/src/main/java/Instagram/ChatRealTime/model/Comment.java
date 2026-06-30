package Instagram.ChatRealTime.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "Comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    boolean visible;
    int preComment;
    String context;
    Timestamp updatedAt;
    Timestamp createdAt;

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @Column(name = "post_id", nullable = false)
    Integer postId;

    @PrePersist
    public void onCreate() {
        Timestamp now = Timestamp.from(Instant.now());
        createdAt = now;
        updatedAt = now;
        // Mặc định comment mới là đang hiển thị -> visible = true
        visible = true;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Timestamp.from(Instant.now());
    }
}
