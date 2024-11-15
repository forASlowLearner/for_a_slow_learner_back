package com.choikang.back.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity
public class InitAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer initAnswerId;

    private boolean firstAns;

    private boolean secondAns;

    private boolean thirdAns;

    private boolean fourthAns;

    private boolean fifthAns;

    @OneToOne
    @JoinColumn(name = "memberId")
    private Member member;
}
