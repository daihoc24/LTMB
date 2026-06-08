package com.chat_application.ChatApplication.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCreateReq {
    String fullName;
    
    @NotBlank(message = "EMAIL_CANNOT_BE_BLANK")
    @Email(message = "EMAIL_INVALID")
    String email;
    
    String phoneNumber;
    
    @NotBlank(message = "USERNAME_CANNOT_BE_BLANK")
    @Pattern(regexp = "^[a-zA-Z0-9._-]{3,}$", message = "USERNAME_INVALID")
    String username;
    
    @NotBlank(message = "PASSWORD_CANNOT_BE_BLANK")
    @Size(min = 6, message = "PASSWORD_INVALID")
    String password;
    
    @NotBlank(message = "CONFIRM_PASSWORD_CANNOT_BE_BLANK")
    String confirmPassword;
}

