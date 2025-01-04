package com.choikang.back.entity;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import java.time.LocalDate;

import lombok.*;

@Entity
@Table(name = "ai_chat")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
@AllArgsConstructor(access = AccessLevel.PRIVATE)  // 빌더용 생성자
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

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;
}
