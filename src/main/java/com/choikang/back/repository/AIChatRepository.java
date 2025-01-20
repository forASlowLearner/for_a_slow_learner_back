package com.choikang.back.repository;

import com.choikang.back.entity.AIChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIChatRepository extends JpaRepository<AIChat, Integer> {
    // 회원 ID를 기준으로 가장 최신 ai_chat_id 조회
    AIChat findTopByMember_MemberIdOrderByAiChatIdDesc(Integer memberId);
    // 회원 ID와 AI_CHAT_ID를 기준으로 데이터를 조회 (최대 10개)
    List<AIChat> findTop10ByMember_MemberIdAndAiChatIdLessThanEqualOrderByAiChatIdDesc(
            Integer memberId,
            Integer aiChatId
    );

    // 회원 ID와 AI_CHAT_ID를 기준으로 데이터를 조회 (최대 10개, ai_chat_id 기준 오름차순 정렬)
    List<AIChat> findTop10ByMember_MemberIdAndAiChatIdLessThanEqualOrderByAiChatIdAsc(
            Integer memberId,
            Integer aiChatId
    );

    // 이 때, 'Member' 엔티티의 PK가 memberId이므로
    // "member" -> "Member" 엔티티
    // "MemberId" -> 그 엔티티의 PK 프로퍼티 이름
    List<AIChat> findAllByMemberMemberIdAndCategory(Long memberId, String category);

    // DISTINCT를 이용해 중복 없이 카테고리만 가져오기
    @Query("SELECT DISTINCT a.category FROM AIChat a WHERE a.member.memberId = :memberId")
    List<String> findDistinctCategoriesByMemberId(@org.springframework.data.repository.query.Param("memberId") Integer memberId);

}
