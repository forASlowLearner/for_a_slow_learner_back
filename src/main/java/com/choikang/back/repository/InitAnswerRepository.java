package com.choikang.back.repository;

import com.choikang.back.entity.InitAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InitAnswerRepository extends JpaRepository<InitAnswer, Integer> {

}
