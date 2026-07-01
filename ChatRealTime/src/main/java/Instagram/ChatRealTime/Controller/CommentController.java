package Instagram.ChatRealTime.Controller;

import Instagram.ChatRealTime.Dto.Request.CommentRequest;
import Instagram.ChatRealTime.Dto.Response.CommentResponse;
import Instagram.ChatRealTime.Services.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<?> createComment(@RequestBody CommentRequest request) {
        try {
            CommentResponse response = commentService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> listComments(@RequestParam Integer postId) {
        if (postId == null) {
            return ResponseEntity.badRequest().body("postId is required");
        }
        List<CommentResponse> comments = commentService.listByPost(postId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateComment(@PathVariable Integer id, @RequestBody CommentRequest request) {
        try {
            Optional<CommentResponse> updated = commentService.update(id, request);
            return updated.<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer id) {
        boolean deleted = commentService.delete(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Comment not found");
    }
}
