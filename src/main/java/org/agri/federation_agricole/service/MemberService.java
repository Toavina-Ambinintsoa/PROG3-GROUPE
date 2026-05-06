package org.agri.federation_agricole.service;

import org.agri.federation_agricole.entity.CreateMember;
import org.agri.federation_agricole.entity.Member;
import org.agri.federation_agricole.exception.BadRequestException;
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
        for (CreateMember member : members) {
            if (!member.isRegistrationFeePaid()) {
                throw new BadRequestException("Member registration fee not paid");
            }
            if (!member.isMembershipDuesPaid()) {
                throw new BadRequestException("Member membership dues not paid");
            }
            if (member.getRefereesId() == null || member.getRefereesId().size() < 2) {
                throw new BadRequestException("Member must have at least 2 referees");
            }
        }
        try {
            return memberRepository.createMembers(members);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
