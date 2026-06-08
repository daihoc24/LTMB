package com.chat_application.ChatApplication.Dto.Response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
/**
 * Response DTO for Post with full User information.
 * This is used when we need to return posts along with complete user details.
 */
public class PostResponseWithUser {
    int id;
    boolean visible;
    String caption;
    Timestamp createdAt;
    Timestamp updatedAt;
    UserResponse user; // Full user information
}
