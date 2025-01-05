package com.choikang.back.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RankingResponseDTO {
    private String nickname;
    private Integer score;
    private Integer rank;

    @Builder
    public RankingResponseDTO(String nickname, Integer score, Integer rank) {
        this.nickname = nickname;
        this.score = score;
        this.rank = rank;
    }
}