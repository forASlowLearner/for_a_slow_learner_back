package com.choikang.back.service;

import com.choikang.back.dto.MemberDTO;
import com.choikang.back.entity.Member;
import com.choikang.back.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class MemberService {
    @Autowired
    private MemberRepository memberRepository;

    public void saveMember(MemberDTO memberDTO){
        Member member = Member.builder()
                .email(memberDTO.getEmail())
                .nickName(null)
                .birthday(null)
                .isSlower(false)
                .score(0)
                .build();

        memberRepository.save(member);
    }

    // 최초 가입 질문 저장: memberId를 통해 회원을 조회 후 이름(nickName)과 생일(birthday) 업데이트
    public void saveUserInfo(int memberId, String name, LocalDate birthday) throws Exception {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            member.setNickName(name);
            member.setBirthday(birthday);
            memberRepository.save(member);
        } else {
            throw new Exception("회원이 존재하지 않습니다.");
        }
    }

    // 최초 가입 질문 점수 저장: 회원의 score 갱신 및 isSlower 필드 처리
    public void submitAnswers(int memberId, int score) throws Exception {
        Optional<Member> optionalMember = memberRepository.findById(memberId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            Member updatedMember = member.toBuilder()
                    .score(score)
                    .isSlower(score <= 5) // 점수가 5 이하면 true, 그렇지 않으면 false
                    .build();
            memberRepository.save(updatedMember);
        } else {
            throw new Exception("회원이 존재하지 않습니다.");
        }
    }

}
