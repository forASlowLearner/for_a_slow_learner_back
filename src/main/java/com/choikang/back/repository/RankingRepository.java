package com.choikang.back.repository;

import com.choikang.back.entity.MemberRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<MemberRank, Long> {
    List<MemberRank> findTop10ByOrderByScoreDesc();
    Optional<MemberRank> findByMemberId(Integer memberId);
}
