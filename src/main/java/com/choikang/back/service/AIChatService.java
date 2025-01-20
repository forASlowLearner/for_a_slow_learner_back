package com.choikang.back.service;

import com.choikang.back.dto.AIChatDTO;
import com.choikang.back.dto.AIChatResponse;
import com.choikang.back.dto.AIChatSaveToggleResponse;
import com.choikang.back.entity.AIChat;
import com.choikang.back.entity.Member;
import com.choikang.back.repository.AIChatRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .isSavedQuestion(false)
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

    public AIChatSaveToggleResponse toggleFavorite(Integer memberId, Integer aiChatId) {
        System.out.println("토글 들어와짐?");

        System.out.println("Received aiChatId: " + aiChatId);
        if (aiChatId == null) {
            throw new IllegalArgumentException("aiChatId cannot be null");
        }

        // 1) AIChat 엔티티 조회
        AIChat aiChat = aiChatRepository.findById(aiChatId)
                .orElseThrow(() -> new RuntimeException("AIChat not found"));


        aiChat.setIsSavedQuestion(!aiChat.isSavedQuestion());

        // 3) DB 업데이트
        AIChat updatedChat = aiChatRepository.save(aiChat);

        // 4) 응답 DTO 구성
        AIChatSaveToggleResponse response = new AIChatSaveToggleResponse();
        if (aiChat.isSavedQuestion()==true) {
            response.setMessage("즐겨찾기 등록에 성공했습니다");
        } else {
            response.setMessage("즐겨찾기 해제에 성공했습니다");
        }
        response.setCategory(updatedChat.getCategory());
        response.setAiChatId(updatedChat.getAiChatId());

        return response;
    }

    public Integer getLatestChatId(Integer memberId) {
        // 가장 최근 ai_chat_id 조회
        AIChat latestChat = aiChatRepository.findTopByMember_MemberIdOrderByAiChatIdDesc(memberId);

        // 대화가 없는 경우 null 반환
        return latestChat != null ? latestChat.getAiChatId() : null;
    }

    public List<AIChatDTO> getChatHistory(Integer memberId, Integer aiChatId) {
        // 데이터베이스에서 조건에 맞는 대화 내역 조회
        List<AIChat> chats = aiChatRepository.findTop10ByMember_MemberIdAndAiChatIdLessThanEqualOrderByAiChatIdAsc(
                memberId,
                aiChatId
        );

        // 조회 결과를 DTO 리스트로 변환
        List<AIChatDTO> responseList = new ArrayList<>();
        for (AIChat chat : chats) {
            AIChatDTO dto = new AIChatDTO();
            dto.setAi_chat_id(chat.getAiChatId());
            dto.setUser_question(chat.getUserQuestion());
            dto.setAi_response_text(chat.getAiResponseText());
            dto.setIs_saved_question(chat.isSavedQuestion() ? 1 : 0);
            responseList.add(dto);
        }

        return responseList;
    }

    // memberId와 category(문자열)을 이용해 AIChat 목록을 조회합니다.
    public List<AIChat> findByMemberIdAndCategory(Long memberId, String category) {
        return aiChatRepository.findAllByMemberMemberIdAndCategory(memberId, category);
    }


    // 새로 추가: 특정 회원의 (중복 없는) 카테고리 목록 조회
    public List<String> getAllDistinctCategories(Integer memberId) {
        return aiChatRepository.findDistinctCategoriesByMemberId(memberId);
    }
}
