package com.choikang.back.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
