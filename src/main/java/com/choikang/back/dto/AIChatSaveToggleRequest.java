package com.choikang.back.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIChatSaveToggleRequest {
    private Integer memberId;        // 회원 ID
    @JsonProperty("ai_chat_id")
    private Integer aiChatId;
}