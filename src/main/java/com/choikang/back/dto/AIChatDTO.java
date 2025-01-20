package com.choikang.back.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIChatDTO {
    private int ai_chat_id;              // AI Chat의 고유 ID
    private String user_question;        // 사용자가 한 질문
    private String ai_response_text;     // AI의 응답
    private int is_saved_question;       // 즐겨찾기 여부 (0 또는 1)
}
