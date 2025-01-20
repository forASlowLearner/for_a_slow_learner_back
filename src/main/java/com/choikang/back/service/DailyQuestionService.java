package com.choikang.back.service;

import com.choikang.back.dto.DailyQuizResponse;
import com.choikang.back.entity.DailyQuestion;
import com.choikang.back.entity.Member;
import com.choikang.back.repository.DailyQuestionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyQuestionService {

    private final ChatClient chatClient;
    private final DailyQuestionRepository dailyQuestionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public DailyQuestionService(ChatClient chatClient, DailyQuestionRepository dailyQuestionRepository) {
        this.chatClient = chatClient;
        this.dailyQuestionRepository = dailyQuestionRepository;
    }

    public int checkIfQuizIsDone(Integer memberId) {
        List<DailyQuestion> dailyQuestions = dailyQuestionRepository.findByMember_MemberIdAndDailyQuestionDate(memberId, LocalDate.now());
        return dailyQuestions.isEmpty() ? 0 : 1; // 데이터가 없으면 0, 있으면 1 반환
    }

    public DailyQuizResponse generateDailyQuiz(Integer memberId) throws Exception {
        // 1) GPT 시스템 메시지
        String systemPrompt = """
        경계선 지능인에게 도움이 될만한 수준의 난이도의 생활상식, 교양, 예의 등 
        일상적인 카테고리에 대해 O, X로 정답이 명확히 나뉘는 퀴즈를 생성해줘.
        퀴즈는 매번 다르게 랜덤으로 생성해줘.
        JSON 형태로 응답하고 반드시 아래 형식을 따라줘:
        {
          "question": "...",
          "answer": "o/x"
        }
    """;

        // 2) 사용자 메시지 (빈 값 확인 후 기본 메시지 설정)
        String userPrompt = ""; // 사용자 입력 값
        if (userPrompt == null || userPrompt.trim().isEmpty()) {
            userPrompt = "퀴즈를 생성해 주세요.";
        }

        // 3) GPT 요청
        String gptResponse = chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt) // 수정된 부분
                .call()
                .content();

        // 4) GPT 응답 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(gptResponse);
        String question = rootNode.path("question").asText();
        String answer = rootNode.path("answer").asText();

        // 5) Member 엔티티 조회 (EntityManager 사용)
//        Member member = entityManager.find(Member.class, memberId);
//        if (member == null) {
//            throw new RuntimeException("Member not found");
//        }
        // 1) 임시 회원 생성
        Member testMember = new Member();
        testMember.setMemberId(1);
        testMember.setNickName("TempUser");
        testMember.setBirthday(LocalDate.of(1995, 8, 15));
        testMember.setEmail("tempuser@example.com");
        testMember.setSlower(false);
        testMember.setScore(10);
        testMember.setSignUpDate(LocalDate.now());

        // 6) DB 저장
        DailyQuestion dailyQuestion = new DailyQuestion();
        dailyQuestion.setDailyQuestion(question);
        dailyQuestion.setDailyQuestionAnswer("o".equalsIgnoreCase(answer) ? 1 : 0);
        dailyQuestion.setDailyQuestionDate(LocalDate.now());
        dailyQuestion.setMember(testMember);
        DailyQuestion savedQuestion = dailyQuestionRepository.save(dailyQuestion);

        // 7) 응답 DTO 구성
        return new DailyQuizResponse(savedQuestion.getDailyQuestionId(), question, answer);
    }

}
