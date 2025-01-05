package com.choikang.back.controller;

import com.choikang.back.dto.RankingResponseDTO;
import com.choikang.back.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
public class RankController {

    private final RankingService rankingService;

    @GetMapping("/top10")
    public ResponseEntity<List<RankingResponseDTO>> getTop10Rankings() {
        return ResponseEntity.ok(rankingService.getTop10Ranking());
    }

    @GetMapping("/members/{memberId}")
    public ResponseEntity<RankingResponseDTO> getMemberRanking(@PathVariable Integer memberId) {
        return ResponseEntity.ok(rankingService.getMemberRanking(memberId));
    }
}