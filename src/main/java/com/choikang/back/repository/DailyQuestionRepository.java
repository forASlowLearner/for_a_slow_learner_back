package com.choikang.back.repository;

import com.choikang.back.entity.DailyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyQuestionRepository extends JpaRepository<DailyQuestion, Integer> {

    //Optional<DailyQuestion> findByMember_MemberIdAndDailyQuestionDate(Integer memberId, LocalDate date);

    List<DailyQuestion> findByMember_MemberIdAndDailyQuestionDate(Integer memberId, LocalDate date);

}