package com.chat_application.ChatApplication.Services.post;

import com.chat_application.ChatApplication.Dto.Request.BlogCreateReq;
import com.chat_application.ChatApplication.Dto.Request.BlogUpdateReq;
import com.chat_application.ChatApplication.Dto.Response.ApiResponse;
import com.chat_application.ChatApplication.Dto.Response.PostResponse;
import com.chat_application.ChatApplication.Dto.Response.PostResponseWithoutUser;
import com.chat_application.ChatApplication.Dto.Response.PostResponseWithUser;
import com.chat_application.ChatApplication.Entities.Post;
import com.chat_application.ChatApplication.Entities.User;

import java.util.List;

public interface IPostService {
    ApiResponse<List<Post>> findAll();
    ApiResponse<List<Post>> findAllByOneUser(User user);
    ApiResponse<List<Post>> findAllByUserList(List<User> users);
    ApiResponse<Post> add(Post post);
    ApiResponse<String> delete(int id);
    ApiResponse<List<Post>> findAllByCaption(String caption);
    ApiResponse<Post> updateCaption(int postId, String caption);

    ApiResponse<Post> updateVisible(int postId, boolean visible);
    List<PostResponseWithoutUser> postOfUsername(String username);
    List<PostResponseWithUser> postOfUsernameWithUser(String username);
    Post getPostById(int id);

    int allPost();
    int allPostInMonth();
    int allPostInDay();

    List<PostResponse> getAllForAdmin();

    boolean changeVisible(int id);

    ApiResponse<List<Post>> findAllPost();
    
    // Admin APIs
    ApiResponse<Post> createBlog(BlogCreateReq req);
    ApiResponse<Post> updateBlog(BlogUpdateReq req);
    String generatePresignedUrl();
}
