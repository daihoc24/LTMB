package com.chat_application.ChatApplication.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportPostResponse {
    int id, reportedPostId;
    byte context;
    String reporterId, reporterUsername, caption;
    Timestamp createdAt;
}