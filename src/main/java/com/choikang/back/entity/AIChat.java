package com.choikang.back.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import java.time.LocalDate;

import lombok.*;

@Entity
@Table(name = "ai_chat")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AIChat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int aiChatId;

    private String userQuestion;

    private String aiResponseText;

    private LocalDate questionDate;

    private boolean isSavedQuestion;

    private String category;

    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // 외래 키 매핑
    private Member member;

    public void setIsSavedQuestion(boolean newValue) {
        System.out.println("세이브메서드 들어와짐? ");
        this.isSavedQuestion = newValue;
    }
}
