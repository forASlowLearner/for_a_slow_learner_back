package com.choikang.back.service;

import com.choikang.back.dto.RankingResponseDTO;
import com.choikang.back.entity.MemberRank;
import com.choikang.back.repository.RankingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;

    public List<RankingResponseDTO> getTop10Ranking() {
        return rankingRepository.findTop10ByOrderByScoreDesc()
                .stream()
                .map(rank -> RankingResponseDTO.builder()
                        .nickname(rank.getNickname())
                        .score(rank.getScore())
                        .rank(rank.getRank())
                        .build())
                .collect(Collectors.toList());
    }

    public RankingResponseDTO getMemberRanking(Integer memberId) {
        MemberRank rank = rankingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return RankingResponseDTO.builder()
                .nickname(rank.getNickname())
                .score(rank .getScore())
                .rank(rank.getRank())
                .build();
    }
}