package org.agri.federation_agricole.service;

import org.agri.federation_agricole.entity.CreateMember;
import org.agri.federation_agricole.entity.Member;
import org.agri.federation_agricole.repository.MemberRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public @Nullable List<Member> createMembers(List<CreateMember> members) {
        try {
            return memberRepository.createMembers(members);
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
