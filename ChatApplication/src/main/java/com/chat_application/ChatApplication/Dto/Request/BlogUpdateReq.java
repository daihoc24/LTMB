package com.chat_application.ChatApplication.Dto.Request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BlogUpdateReq {
    @NotBlank(message = "ID_CANNOT_BE_BLANK")
    String id;
    String authId;
    @NotBlank(message = "TITLE_CANNOT_BE_BLANK")
    String title;
    String image;
    @NotBlank(message = "SHORT_DESCRIPTION_CANNOT_BE_BLANK")
    String shortDescription;
    @NotBlank(message = "CONTENT_CANNOT_BE_BLANK")
    String content;
    String categoryId;
    int status;
}

