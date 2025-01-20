package com.choikang.back.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIChatSaveToggleResponse {
    private String message;        // 결과 메시지
    private String category;       // AIChat 엔티티의 category
    private int aiChatId;          // AIChat ID
}