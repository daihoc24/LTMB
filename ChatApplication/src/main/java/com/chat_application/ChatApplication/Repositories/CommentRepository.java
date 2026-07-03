package com.chat_application.ChatApplication.Repositories;

import com.chat_application.ChatApplication.Entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Integer> { }
