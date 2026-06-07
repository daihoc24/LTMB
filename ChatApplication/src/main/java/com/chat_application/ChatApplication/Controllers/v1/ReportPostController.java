package com.chat_application.ChatApplication.Controllers.v1;

import com.chat_application.ChatApplication.Dto.Request.ReportPostRequest;
import com.chat_application.ChatApplication.Dto.Response.ReportPostResponse;
import com.chat_application.ChatApplication.Services.ReportPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/reportPost")
@CrossOrigin(origins = "http://localhost:3000")
public class ReportPostController {
    @Autowired
    private ReportPostService reportService;

    @PostMapping("/getReportPort")
    public List<ReportPostResponse> getReportPort(){
        return reportService.getReportPort();
    }

    @PostMapping("/sendReportPort")
    public boolean sendReportPort(@RequestBody ReportPostRequest req){
        return reportService.sendReportPort(req);
    }
}
