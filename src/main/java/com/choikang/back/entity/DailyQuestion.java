package com.choikang.back.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class DailyQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dailyQuestionId;

    private String dailyQuestion;

    private boolean dailyQuestionAnswer;

    @ManyToOne
    @JoinColumn(name = "memberId")
    private Member member;
}
