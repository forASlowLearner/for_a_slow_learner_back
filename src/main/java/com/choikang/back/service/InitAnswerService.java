package com.choikang.back.service;

import com.choikang.back.dto.InitAnswerDTO;
import com.choikang.back.entity.InitAnswer;
import com.choikang.back.repository.InitAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InitAnswerService {
    private final InitAnswerRepository initAnswerRepository;

    public InitAnswer saveInitAnswer(InitAnswerDTO initAnswerDTO) {
        InitAnswer initAnswer = InitAnswer.builder()
                .firstAns(initAnswerDTO.isFifthAns())
                .secondAns(initAnswerDTO.isSecondAns())
                .thirdAns(initAnswerDTO.isThirdAns())
                .fourthAns(initAnswerDTO.isFourthAns())
                .fifthAns(initAnswerDTO.isFifthAns())
                //member 객체를 설정하는 로직 추가하기
                .build();

        return initAnswerRepository.save(initAnswer);
    }
}
