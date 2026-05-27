package Instagram.ChatRealTime.Services;

import Instagram.ChatRealTime.Dto.Request.MessegeRequest;
import Instagram.ChatRealTime.Repositories.MessageRepository;
import Instagram.ChatRealTime.model.Message;
import Instagram.ChatRealTime.model.User;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final GroupChatService groupChatService;

    public MessageService(MessageRepository messageRepository, GroupChatService groupChatService) {
        this.messageRepository = messageRepository;
        this.groupChatService = groupChatService;
    }

    //    Lấy tin nhắn cũ
    public List<Message> getMessageHistory(UUID userIdSend, UUID userIdTo){
        return messageRepository.findMessagesBetweenUsers(userIdSend,userIdTo);
    }
    //Lưu tin nhắn
    public Message saveMessage(Message message){
        return messageRepository.save(message);
    }
}
