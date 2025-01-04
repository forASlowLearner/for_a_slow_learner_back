package com.choikang.back.service;

import com.choikang.back.dto.AIChatResponse;
import com.choikang.back.entity.AIChat;
import com.choikang.back.entity.Member;
import com.choikang.back.repository.AIChatRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class AIChatService {
    @Autowired
    private ChatClient chatClient;

    @Autowired
    private AIChatRepository aiChatRepository;



    /**
     * 사용자의 질문을 받아 GPT API에 전송하고,
     * "JSON 형태"로 결과를 받아서 DB 저장 및 응답 DTO를 구성
     */
    public AIChatResponse processUserQuestion(Integer memberId, String userQuestion) throws Exception {

        // 1) 시스템 메시지(역할/규칙) + JSON 형식 응답 요구
        String systemPrompt = """
    경계선 지능 장애가 의심되는 사용자의 질문에 대해서 
    기초적인 생활상식에 대한 답변을 제공해줘.
    그리고 사용자의 안전과 평화를 고려한 방향의 답변으로 제공해줘.

    아래 JSON 형태로 응답해줘 (key는 반드시 answer, keyword, summary 세 개):
    {
      "answer": "...",
      "keyword": "...",
      "summary": "..."
    }

    (추가 요구사항)
    1) summary는 한글로 15글자 이내
    2) keyword는 질문/답변에서 중요한 단어 한 개
    """;

        // 2) 사용자 메시지 (실제 질문)
        String userPrompt = userQuestion;

        // 3) GPT API 호출 → 응답
        String gptResponse = chatClient
                .prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();  // 자유 텍스트 or JSON

        // 4) JSON 파싱 (answer, keyword, summary)
        String answer = "";
        String keyword = "";
        String summary = "";

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(gptResponse);
            answer = root.path("answer").asText();
            keyword = root.path("keyword").asText();
            summary = root.path("summary").asText();
        } catch (Exception e) {
            // JSON 파싱 실패 시, 전체 응답을 answer에 넣고, 나머지는 비워둠
            answer = gptResponse;
            keyword = "";
            summary = "";
        }

        // 1) 임시 회원 생성
        Member testMember = new Member();
        testMember.setMemberId(1);
        testMember.setNickName("TempUser");
        testMember.setBirthday(LocalDate.of(1995, 8, 15));
        testMember.setEmail("tempuser@example.com");
        testMember.setSlower(false);
        testMember.setScore(10);
        testMember.setSignUpDate(LocalDate.now());

        // 5) DB 저장 (빌더 패턴 사용)
        //   - Lombok의 @Builder, @AllArgsConstructor(access = PRIVATE), @NoArgsConstructor(access = PROTECTED) 등을
        //     엔티티에 설정해둔 상태를 가정
        AIChat aiChat = AIChat.builder()
                .userQuestion(userQuestion)
                .aiResponseText(answer)
                .questionDate(LocalDate.now())
                .isSavedQuestion(true)
                .category(keyword)
                .summary(summary)
                .member(testMember)   // 실제로 memberRepository를 통해 찾은 회원 엔티티가 있을 경우 세팅
                .build();

        AIChat saved = aiChatRepository.save(aiChat);

        // 6) 응답 DTO 구성
        AIChatResponse response = new AIChatResponse();
        response.setMessage(answer);
        response.setAi_chat_id(saved.getAiChatId());
        response.setCategory(keyword);
        response.setSummary_message(summary);

        return response;
    }
}
