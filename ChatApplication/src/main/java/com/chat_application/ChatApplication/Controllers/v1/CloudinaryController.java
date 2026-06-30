package com.chat_application.ChatApplication.Controllers.v1;

import com.chat_application.ChatApplication.Dto.Response.ApiResponse;
import com.chat_application.ChatApplication.Entities.Media;
import com.chat_application.ChatApplication.Services.cloudinary.ICloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/v1/cloudinary")
public class CloudinaryController {
    @Autowired
    private ICloudinaryService service;

    // Các endpoint upload dùng multipart/form-data
    @PostMapping(value = "/one", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadImage(@RequestParam(value = "fileUpload") MultipartFile file,
                                           @RequestParam String userId,
                                           @RequestParam int postId) throws IOException {
        return service.uploadMedia(file, userId, postId);
    }

    @PostMapping(value = "/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadImages(@RequestParam(value = "fileUpload") MultipartFile[] files,
                                            @RequestParam String userId,
                                            @RequestParam int postId) throws IOException {
        System.out.println(Arrays.toString(files));
        System.out.println(userId);
        System.out.println(postId);
        return service.uploadMediaList(Arrays.asList(files), userId, postId);
    }

    @DeleteMapping("/delete")
    public ApiResponse<String> delete(@RequestParam String src) throws Exception {
        return service.deleteMedia(src);
    }

    @DeleteMapping("/deleteAll")
    public ApiResponse<String> deleteAll(@RequestParam String src) {
        return service.deleteFolderMedia(src);
    }

    // Endpoint này nhận JSON (application/json) nên KHÔNG dùng multipart/form-data
    @PostMapping("/getAllMultiple")
    public ApiResponse<List<List<String>>> getAllMultiple(@RequestBody List<String> folders) throws Exception {
        return service.getAllMultipleMediaFromFolder(folders);
    }
}
