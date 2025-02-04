package com.choikang.back.controller;

import com.choikang.back.entity.DailyQuestion;
import com.choikang.back.entity.Member;
import com.choikang.back.repository.DailyQuestionRepository;
import com.choikang.back.service.DailyQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.choikang.back.dto.DailyQuizResponse;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/quiz")
public class DailyQuestionController {

    private final DailyQuestionService dailyQuestionService;
    private final DailyQuestionRepository dailyQuestionRepository;


    @Autowired
    public DailyQuestionController(DailyQuestionService dailyQuestionService,DailyQuestionRepository dailyQuestionRepository) {
        this.dailyQuestionService = dailyQuestionService;
        this.dailyQuestionRepository = dailyQuestionRepository;
    }

    @GetMapping("/{memberId}")
    public DailyQuizResponse getDailyQuiz(@PathVariable Integer memberId) throws Exception {
        return dailyQuestionService.generateDailyQuiz(memberId);
    }

    @GetMapping("/is-done/{memberId}")
    public QuizCompletionResponse checkIfQuizIsDone(@PathVariable Integer memberId) {
        int completed = dailyQuestionService.checkIfQuizIsDone(memberId);
        return new QuizCompletionResponse(completed);
    }

    private static class QuizCompletionResponse {
        private final int completed;

        public QuizCompletionResponse(int completed) {
            this.completed = completed;
        }

        public int getCompleted() {
            return completed;
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> submitAnswer(@RequestBody Map<String, Object> requestBody) {
        // 요청 데이터에서 필요한 값 추출
        Object questionId = requestBody.get("question_id");
        Object isCorrect = requestBody.get("is_correct");

        // 요청 데이터 검증
        if (questionId == null || isCorrect == null) {
            throw new IllegalArgumentException("Invalid request format: question_id or is_correct is missing.");
        }

        // 요청 데이터가 올바르다면 성공 메시지 반환
        return ResponseEntity.ok(Map.of("message", "정답 제출이 완료되었습니다."));
    }

    // 요청 DTO
    public static class AnswerSubmissionRequest {
        private Integer questionId;
        private String isCorrect;

        public Integer getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Integer questionId) {
            this.questionId = questionId;
        }

        public String getIsCorrect() {
            return isCorrect;
        }

        public void setIsCorrect(String isCorrect) {
            this.isCorrect = isCorrect;
        }
    }

    // 응답 DTO
    public static class AnswerSubmissionResponse {
        private String message;

        public AnswerSubmissionResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
