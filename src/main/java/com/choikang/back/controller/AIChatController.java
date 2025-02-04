package com.choikang.back.controller;

import com.choikang.back.dto.*;
import com.choikang.back.entity.AIChat;
import com.choikang.back.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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

    @PostMapping("/save")
    public ResponseEntity<?> toggleChatSave(@RequestBody AIChatSaveToggleRequest request) {
        try {
            System.out.println("세이브 메서드");
            if (request.getAiChatId() == null) {
                throw new IllegalArgumentException("ai_chat_id is required and cannot be null");
            }

            AIChatSaveToggleResponse response = aiChatService.toggleFavorite(
                    request.getMemberId(),
                    request.getAiChatId()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", response.getMessage(),
                    "category", response.getCategory(),
                    "ai_chat_id", response.getAiChatId()
            ));
        } catch (IllegalArgumentException e) {
            e.printStackTrace(); // 로그 출력
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 로그 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "알 수 없는 서버 에러가 발생했습니다"));
        }
    }

    @PostMapping("/latest")
    public ResponseEntity<?> getLatestChatId(@RequestBody Map<String, Integer> request) {
        try {
            Integer memberId = request.get("member_id");
            if (memberId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "member_id는 필수입니다"));
            }

            Integer latestChatId = aiChatService.getLatestChatId(memberId);

            if (latestChatId == null) {
                return ResponseEntity.ok(Map.of("message", "대화 내역이 없습니다", "latest_ai_chat_id", null));
            }

            return ResponseEntity.ok(Map.of("latest_ai_chat_id", latestChatId));
        } catch (Exception e) {
            e.printStackTrace(); // 디버깅 로그
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "알 수 없는 서버 에러가 발생했습니다"));
        }
    }

    @PostMapping("/inquiry")
    public ResponseEntity<?> getChatHistory(@RequestBody AIChatRequest request) {
        try {
            Integer memberId = request.getMember_id();
            Integer aiChatId = request.getAi_chat_id();

            if (memberId == null || aiChatId == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "member_id와 ai_chat_id는 필수입니다"));
            }

            List<AIChatDTO> chatHistory = aiChatService.getChatHistory(memberId, aiChatId);

            return ResponseEntity.ok(chatHistory);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "알 수 없는 서버 에러가 발생했습니다"));
        }
    }

    @PostMapping("/category/details")
    public ResponseEntity<?> getCategoryDetails(@RequestBody Map<String, Object> requestBody) {
        try {
            // 1. 요청 바디에서 필요한 필드를 추출
            //    예: { "member_id" : 1, "catagory" : "물" }
            Long memberId = null;
            String category = null;

            if (requestBody.containsKey("member_id")) {
                memberId = Long.valueOf(requestBody.get("member_id").toString());
            }
            if (requestBody.containsKey("catagory")) {
                category = requestBody.get("catagory").toString();
            }

            // (필요에 따라 null 체크, 유효성 검사 추가 가능)
            if (memberId == null || category == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "필수 파라미터가 누락되었습니다."));
            }

            // 2. 서비스 호출: DB에서 조건에 맞는 AIChat 목록 조회
            List<AIChat> aiChatList = aiChatService.findByMemberIdAndCategory(memberId, category);

            // 3. 응답용 리스트 만들기
            //    [{ ai_chat_id: xx, ai_response_text: xxx }, { ... }, ...]
            List<Map<String, Object>> responseList = new ArrayList<>();
            for (AIChat chat : aiChatList) {
                Map<String, Object> map = new HashMap<>();
                map.put("ai_chat_id", chat.getAiChatId());
                map.put("ai_response_text", chat.getAiResponseText());
                responseList.add(map);
            }

            // 4. 정상 응답(200) 리턴
            return ResponseEntity.ok(responseList);

        } catch (Exception e) {
            // 5. 알 수 없는 에러 시 500 응답
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "알 수 없는 서버 에러가 발생했습니다"));
        }
    }





}
