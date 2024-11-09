package com.choikang.back.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int memberId;

    private String nickName;

    private LocalDate birthday;

    private String email;

    private boolean isSlower;

    private int score;

    private LocalDate signUpDate;
}
