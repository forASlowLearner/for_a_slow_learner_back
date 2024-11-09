package com.choikang.back.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import java.time.LocalDate;


@Table(name = "ai_chat")
@Entity
public class AIChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int aiChatId;

    private int memberId;

    private String userQuestion;

    private String aiResponseText;

    private LocalDate questionDate;

    private Boolean isSavedQuestion;
}
