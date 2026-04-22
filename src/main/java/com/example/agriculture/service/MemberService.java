package com.example.agriculture.service;

import com.example.agriculture.entity.CreateMember;
import com.example.agriculture.entity.Enum.MemberOccupation;
import com.example.agriculture.entity.Member;
import com.example.agriculture.exception.BadRequestException;
import com.example.agriculture.exception.NotFoundException;
import com.example.agriculture.repository.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public List<Member> createMembers(List<CreateMember> requests) {
        List<Member> created = new ArrayList<>();
        for (CreateMember cm : requests) {
            created.add(createOneMember(cm));
        }
        return created;
    }

    private Member createOneMember(CreateMember cm) {
        if (!memberRepository.collectivityExists(cm.getCollectivityId())) {
            throw new NotFoundException(
                    "Collectivité introuvable : " + cm.getCollectivityId()
            );
        }
        if (!cm.isRegistrationFeePaid()) {
            throw new BadRequestException(
                    "Les frais d'adhésion (50 000 MGA) doivent être réglés."
            );
        }
        if (!cm.isMembershipDuesPaid()) {
            throw new BadRequestException(
                    "Les cotisations annuelles obligatoires de la collectivité doivent être réglées."
            );
        }
        validateReferees(cm.getRefereesIds(), cm.getCollectivityId());
        Member member = new Member();
        member.setFirstName(cm.getFirstName());
        member.setLastName(cm.getLastName());
        member.setBirthDate(cm.getBirthDate());
        member.setGender(cm.getGender());
        member.setAddress(cm.getAddress());
        member.setProfession(cm.getProfession());
        member.setPhoneNumber(cm.getPhoneNumber());
        member.setEmail(cm.getEmail());
        member.setMemberOccupation(MemberOccupation.JUNIOR_MEMBER);

        List<Member> resolvedReferees = new ArrayList<>();
        for (int refereeId : cm.getRefereesIds()) {
            resolvedReferees.add(memberRepository.findById(refereeId));
        }
        member.setReferees(resolvedReferees);
        return memberRepository.save(member, cm.getCollectivityId());
    }

    private void validateReferees(List<Integer> refereeIds, String targetCollectivityId) {

        if (refereeIds == null || refereeIds.size() < 2) {
            throw new BadRequestException(
                    "Le candidat doit être parrainé par au moins 2 membres confirmés."
            );
        }

        int insideCount = 0;
        int outsideCount = 0;

        for (int refereeId : refereeIds) {

            if (!memberRepository.memberExists(refereeId)) {
                throw new NotFoundException("Parrain introuvable : id=" + refereeId);
            }

            if (!memberRepository.isSeniorMember(refereeId)) {
                throw new BadRequestException(
                        "Le parrain id=" + refereeId + " n'est pas un membre confirmé (SENIOR)."
                );
            }

            String refereeCollectivityId = memberRepository.getCollectivityIdOfMember(refereeId);
            if (targetCollectivityId.equals(refereeCollectivityId)) {
                insideCount++;
            } else {
                outsideCount++;
            }
        }

        if (insideCount < outsideCount) {
            throw new BadRequestException(
                    "Le nombre de parrains de la collectivité cible (" + insideCount +
                            ") doit être >= au nombre de parrains extérieurs (" + outsideCount + ")."
            );
        }
    }
}