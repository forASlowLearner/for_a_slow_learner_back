package com.choikang.back.service;

import com.choikang.back.dto.MemberDTO;
import com.choikang.back.entity.Member;
import com.choikang.back.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
