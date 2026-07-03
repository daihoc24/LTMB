package com.chat_application.ChatApplication.Controllers.v1;

import com.chat_application.ChatApplication.Dto.Response.NotificationResponse;
import com.chat_application.ChatApplication.Services.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/notification")
public class NotificationController {

    NotificationService notificationService;

    @PostMapping
    public List<NotificationResponse> getAllNotifyByUseId(@RequestParam String userId){
        List<NotificationResponse> notifications = notificationService.getAllNotifyByUseId(userId);
         return notifications;
    }

    @PostMapping("/add")
    public void addNotification(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String message
    ) {
        notificationService.addNotification(userId, title, message);
    }

    @PostMapping("/comment-event")
    public void commentEvent(
            @RequestParam String actorId,
            @RequestParam int postId,
            @RequestParam(defaultValue = "0") int parentCommentId
    ) {
        notificationService.addCommentNotification(actorId, postId, parentCommentId);
    }
}
