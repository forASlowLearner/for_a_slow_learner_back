package com.choikang.back.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIChatResponse {
    private String message;         // GPT 최종 답변
    private int ai_chat_id;         // DB에 저장된 aiChatId
    private String category;        // GPT가 제시한 키워드
    private String summary_message; // GPT가 제공한 요약(15글자 이하)
}
