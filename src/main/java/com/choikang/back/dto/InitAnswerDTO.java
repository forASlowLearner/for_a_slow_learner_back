package com.choikang.back.dto;

import lombok.Data;

@Data
public class InitAnswerDTO {
    private Integer initAnswerId;
    private boolean firstAns;
    private boolean secondAns;
    private boolean thirdAns;
    private boolean fourthAns;
    private boolean fifthAns;
    private Integer memberId;
}
