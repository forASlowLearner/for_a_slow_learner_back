package com.choikang.back.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {
    private int memberId;

    private String nickName;

    private LocalDate birthday;

    private String email;

    private boolean isSlower;

    private int score;

    private LocalDate signUpDate;
}
