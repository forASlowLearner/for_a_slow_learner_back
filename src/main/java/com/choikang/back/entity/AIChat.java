package com.choikang.back.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import lombok.Data;

@Data
@Table(name = "ai_chat")
@Entity
public class AIChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int aiChatId;

    private String userQuestion;

    private String aiResponseText;

    private LocalDate questionDate;

    private boolean isSavedQuestion;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;
}
