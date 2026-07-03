package Instagram.ChatRealTime.Services;

import Instagram.ChatRealTime.Dto.Request.CommentRequest;
import Instagram.ChatRealTime.Dto.Response.CommentResponse;
import Instagram.ChatRealTime.Repositories.CommentRepository;
import Instagram.ChatRealTime.model.Comment;
import Instagram.ChatRealTime.model.User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final NotificationBridge notificationBridge;

    public CommentService(CommentRepository commentRepository, UserService userService, NotificationBridge notificationBridge) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.notificationBridge = notificationBridge;
    }

    public CommentResponse create(CommentRequest request) {
        validateRequest(request);

        Comment comment = Comment.builder()
                .context(request.getContent().trim())
                .preComment(request.getPreComment() == null ? 0 : request.getPreComment())
                .postId(request.getPostId())
                .userId(UUID.fromString(request.getUserId()))
                .createdAt(Timestamp.from(Instant.now()))
                .updatedAt(Timestamp.from(Instant.now()))
                // visible = true (1) khi comment đang hiển thị
                .visible(true)
                .build();

        Comment saved = commentRepository.save(comment);
        notificationBridge.comment(
                request.getUserId(),
                request.getPostId(),
                request.getPreComment() == null ? 0 : request.getPreComment()
        );
        return toResponse(saved);
    }

    public List<CommentResponse> listByPost(Integer postId) {
        // Lấy tất cả comment đang hiển thị theo post (visible = true), mặc định đã sort theo createdAt
        List<Comment> comments = commentRepository.findVisibleByPostId(postId);
        return comments.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public Optional<CommentResponse> update(Integer id, CommentRequest request) {
        if (request.getContent() != null && !StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("Content must not be empty");
        }
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isEmpty()) {
            return Optional.empty();
        }
        Comment comment = commentOpt.get();
        if (request.getContent() != null) {
            comment.setContext(request.getContent().trim());
        }
        if (request.getPreComment() != null) {
            comment.setPreComment(request.getPreComment());
        }
        comment.setUpdatedAt(Timestamp.from(Instant.now()));
        Comment updated = commentRepository.save(comment);
        return Optional.of(toResponse(updated));
    }

    public boolean delete(Integer id) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isEmpty()) {
            return false;
        }
        // Soft-delete: đổi visible = false để ẩn comment
        Comment comment = commentOpt.get();
        comment.setVisible(false);
        comment.setUpdatedAt(Timestamp.from(Instant.now()));
        commentRepository.save(comment);
        return true;
    }

    private void validateRequest(CommentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is required");
        }
        if (!StringUtils.hasText(request.getContent())) {
            throw new IllegalArgumentException("Content must not be empty");
        }
        if (request.getPostId() == null) {
            throw new IllegalArgumentException("postId is required");
        }
        if (!StringUtils.hasText(request.getUserId())) {
            throw new IllegalArgumentException("userId is required");
        }
        // Ensure UUID format is valid
        UUID.fromString(request.getUserId());
    }

    private CommentResponse toResponse(Comment comment) {
        User user = null;
        try {
            user = userService.findByUserById(comment.getUserId());
        } catch (Exception ignored) {}

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContext())
                .preComment(comment.getPreComment())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .username(user != null ? user.getUsername() : null)
                .avatar(user != null ? user.getAvatar() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .visible(comment.isVisible())
                .build();
    }
}
