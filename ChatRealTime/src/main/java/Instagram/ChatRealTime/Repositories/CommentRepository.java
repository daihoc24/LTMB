package Instagram.ChatRealTime.Repositories;

import Instagram.ChatRealTime.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // Lấy tất cả comment còn hiển thị (visible = true) của 1 post, cũ -> mới
    @Query("SELECT c FROM Comment c WHERE c.postId = :postId AND c.visible = true ORDER BY c.createdAt ASC")
    List<Comment> findVisibleByPostId(@Param("postId") Integer postId);
}
