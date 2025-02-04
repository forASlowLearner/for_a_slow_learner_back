package com.choikang.back.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIChatRequest {
    private Integer member_id;
    private Integer ai_chat_id;
    private String user_question;
}