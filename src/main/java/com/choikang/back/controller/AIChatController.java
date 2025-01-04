package com.choikang.back.controller;

import com.choikang.back.dto.AIChatRequest;
import com.choikang.back.dto.AIChatResponse;
import com.choikang.back.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping("/send")
    public ResponseEntity<AIChatResponse> sendChat(@RequestBody AIChatRequest request) {
        try {
            AIChatResponse response = aiChatService.processUserQuestion(
                    request.getMember_id(),
                    request.getUser_question()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
