package com.choikang.back.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "daily_question")
public class DailyQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dailyQuestionId;

    private String dailyQuestion;

    private Integer dailyQuestionAnswer;

    private LocalDate dailyQuestionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // 외래 키 매핑
    private Member member;
}
