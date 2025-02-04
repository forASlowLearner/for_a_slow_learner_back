package com.choikang.back.controller;

import com.choikang.back.entity.AIChat;
import com.choikang.back.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {
    private final AIChatService aiChatService;


    @PostMapping("/details")
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

    @GetMapping("/all/{member_id}")
    public ResponseEntity<?> getAllCategories(@PathVariable("member_id") Integer memberId) {
        try {
            // 1. 서비스 호출
            List<String> categories = aiChatService.getAllDistinctCategories(memberId);

            // 2. [{ "catagory": "xxx" }, ... ] 형태로 변환
            List<Map<String, String>> responseList = new ArrayList<>();
            for (String c : categories) {
                Map<String, String> map = new HashMap<>();
                // 문제에서 "catagory" 라는 키를 사용하므로 그대로 준다. (원래는 "category"가 맞춤법)
                map.put("catagory", c);
                responseList.add(map);
            }

            // 3. 정상 응답
            return ResponseEntity.ok(responseList);

        } catch (Exception e) {
            // 4. 알 수 없는 에러 -> 500
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "알 수 없는 서버 에러가 발생했습니다"));
        }
    }

}
