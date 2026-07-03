package com.chat_application.ChatApplication.Controllers.v1;

import com.chat_application.ChatApplication.Dto.Response.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/v1/category")
@CrossOrigin
public class CategoryController {

    @PostMapping
    public ApiResponse<List<Map<String, Object>>> getCategories() {
        // Trả về danh sách category mặc định (có thể mở rộng sau)
        List<Map<String, Object>> categories = new ArrayList<>();
        
        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", 1);
        category1.put("name", "General");
        categories.add(category1);
        
        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", 2);
        category2.put("name", "News");
        categories.add(category2);
        
        Map<String, Object> category3 = new HashMap<>();
        category3.put("id", 3);
        category3.put("name", "Technology");
        categories.add(category3);
        
        return ApiResponse.<List<Map<String, Object>>>builder()
                .result(categories)
                .message("Get categories successfully")
                .build();
    }
}

