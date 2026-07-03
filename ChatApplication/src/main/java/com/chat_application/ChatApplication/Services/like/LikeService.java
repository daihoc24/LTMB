package com.chat_application.ChatApplication.Services.like;


import com.chat_application.ChatApplication.Dto.Response.ApiResponse;
import com.chat_application.ChatApplication.Entities.Like;
import com.chat_application.ChatApplication.Entities.Post;
import com.chat_application.ChatApplication.Repositories.LikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.chat_application.ChatApplication.Services.NotificationService;

import java.util.List;

@Service
public class LikeService implements ILikeService {
    @Autowired
    private LikeRepository repository;
    @Autowired
    private NotificationService notificationService;

    @Override
    public ApiResponse<List<Like>> findAll() {
        return ApiResponse.<List<Like>>builder()
                .code(200)
                .message("Get list like successfully")
                .result(repository.findAll())
                .build();
    }

    @Override
    public ApiResponse<Like> add(Like like) {
        if(repository.existsByUserAndPost(like.getUser(), like.getPost())) return null;
        Like saved = repository.save(like);
        if (!like.getUser().getId().equals(like.getPost().getUser().getId())) {
            try {
                notificationService.addNotification(
                        like.getPost().getUser().getId().toString(),
                        "Lượt thích mới",
                        like.getUser().getUsername() + " đã thích bài viết của bạn"
                );
            } catch (Exception ignored) { }
        }
        return ApiResponse.<Like>builder()
                .message("add like successfully")
                .result(saved)
                .build();
    }

    @Transactional
    @Override
    public ApiResponse<Like> delete(Like like) {
        repository.deleteByUserAndPost(like.getUser(), like.getPost());
        return ApiResponse.<Like>builder()
                .message("delete like successfully")
                .build();
    }

    @Override
    public ApiResponse<Integer> quantityLike(Post post) {
        return ApiResponse.<Integer>builder()
                .message("get quantity like of post successfully")
                .result(repository.quantityPost(post))
                .build();
    }

    @Override
    public ApiResponse<Boolean> isLike(Like like) {
        return ApiResponse.<Boolean>builder()
                .message("check is like successfully")
                .result(repository.existsByUserAndPost(like.getUser(), like.getPost()))
                .build();
    }
}
