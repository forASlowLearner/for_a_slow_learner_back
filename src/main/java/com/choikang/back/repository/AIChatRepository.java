package com.choikang.back.repository;

import com.choikang.back.entity.AIChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AIChatRepository extends JpaRepository<AIChat, Integer> {

}
