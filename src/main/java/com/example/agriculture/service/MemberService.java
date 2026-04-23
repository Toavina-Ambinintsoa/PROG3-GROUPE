package com.example.agriculture.service;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.*;
import com.example.agriculture.entity.Enum.MemberOccupation;
import com.example.agriculture.exception.BadRequestException;
import com.example.agriculture.exception.NotFoundException;
import com.example.agriculture.repository.*;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberPaymentRepository memberPaymentRepository;
    private final MembershipFeeRepository membershipFeeRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final TransactionRepository transactionRepository;
    private final DataSource dataSource;

    public MemberService(MemberRepository memberRepository,
                         MemberPaymentRepository memberPaymentRepository,
                         MembershipFeeRepository membershipFeeRepository,
                         FinancialAccountRepository financialAccountRepository,
                         TransactionRepository transactionRepository,
                         DataSource dataSource) {
        this.memberRepository = memberRepository;
        this.memberPaymentRepository = memberPaymentRepository;
        this.membershipFeeRepository = membershipFeeRepository;
        this.financialAccountRepository = financialAccountRepository;
        this.transactionRepository = transactionRepository;
        this.dataSource = dataSource;
    }

    public List<Member> createMembers(List<CreateMember> requests) {
        List<Member> created = new ArrayList<>();
        for (CreateMember cm : requests) {
            created.add(createOneMember(cm));
        }
        return created;
    }

    private Member createOneMember(CreateMember cm) {
        // 404
        if (!memberRepository.collectivityExists(cm.getCollectivityIdentifier())) {
            throw new NotFoundException("Collectivité introuvable : " + cm.getCollectivityIdentifier());
        }
        // 400 - frais d'adhésion
        if (!cm.isRegistrationFeePaid()) {
            throw new BadRequestException("Les frais d'adhésion (50 000 MGA) doivent être réglés.");
        }
        // 400 - cotisations annuelles
        if (!cm.isMembershipDuesPaid()) {
            throw new BadRequestException("Les cotisations annuelles obligatoires doivent être réglées.");
        }
        // 400 - règles de parrainage B-2
        validateReferees(cm.getReferees(), cm.getCollectivityIdentifier());

        Member member = new Member();
        member.setFirstName(cm.getFirstName());
        member.setLastName(cm.getLastName());
        member.setBirthDate(cm.getBirthDate());
        member.setGender(cm.getGender());
        member.setAddress(cm.getAddress());
        member.setProfession(cm.getProfession());
        member.setPhoneNumber(cm.getPhoneNumber());
        member.setEmail(cm.getEmail());
        member.setOccupation(MemberOccupation.JUNIOR);

        Member saved = memberRepository.save(member, cm.getCollectivityIdentifier());

        memberRepository.saveReferees(saved.getId(), cm.getReferees());

        List<Member> resolvedReferees = new ArrayList<>();
        for (int refereeId : cm.getReferees()) {
            Member referee = memberRepository.findById(refereeId);
            if (referee != null) resolvedReferees.add(referee);
        }
        saved.setReferees(resolvedReferees);

        return saved;
    }

    private void validateReferees(List<Integer> refereeIds, String targetCollectivityId) {
        if (refereeIds == null || refereeIds.size() < 2) {
            throw new BadRequestException("Le candidat doit être parrainé par au moins 2 membres confirmés.");
        }

        int insideCount  = 0;
        int outsideCount = 0;

        for (int refereeId : refereeIds) {
            if (!memberRepository.memberExists(refereeId)) {
                throw new NotFoundException("Parrain introuvable : id=" + refereeId);
            }
            if (!memberRepository.isSeniorMember(refereeId)) {
                throw new BadRequestException(
                        "Le parrain id=" + refereeId +
                                " n'est pas un membre confirmé avec 90 jours d'ancienneté.");
            }
            String refereeCollectivity = memberRepository.getCollectivityIdOfMember(refereeId);
            if (targetCollectivityId.equals(refereeCollectivity)) {
                insideCount++;
            } else {
                outsideCount++;
            }
        }

        if (insideCount < outsideCount) {
            throw new BadRequestException(
                    "Parrains de la collectivité cible (" + insideCount +
                            ") doit être >= parrains extérieurs (" + outsideCount + ").");
        }
    }

    public List<MemberPayment> createMemberPayments(String memberId,
                                                    List<CreateMemberPayment> payments) {
        int mId;
        try {
            mId = Integer.parseInt(memberId);
        } catch (NumberFormatException e) {
            throw new BadRequestException("L'identifiant membre doit être un entier : " + memberId);
        }

        if (!memberRepository.memberExists(mId)) {
            throw new NotFoundException("Membre introuvable : id=" + memberId);
        }
        if (payments == null || payments.isEmpty()) {
            throw new BadRequestException("La liste de paiements ne peut pas être vide.");
        }

        String collectivityId = memberRepository.getCollectivityIdOfMember(mId);

        List<MemberPayment> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (CreateMemberPayment payment : payments) {
                    // Validate
                    if (!membershipFeeRepository.exists(payment.getMembershipFeeIdentifier())) {
                        throw new NotFoundException(
                                "Cotisation introuvable : id=" + payment.getMembershipFeeIdentifier());
                    }
                    if (!financialAccountRepository.exists(payment.getAccountCreditedIdentifier())) {
                        throw new NotFoundException(
                                "Compte introuvable : id=" + payment.getAccountCreditedIdentifier());
                    }
                    if (payment.getAmount() <= 0) {
                        throw new BadRequestException("Le montant du paiement doit être positif.");
                    }
                    if (payment.getPaymentMode() == null) {
                        throw new BadRequestException("Le mode de paiement est obligatoire.");
                    }

                    MemberPayment saved = memberPaymentRepository.save(
                            conn,
                            mId,
                            payment.getAmount(),
                            payment.getPaymentMode(),
                            payment.getMembershipFeeIdentifier(),
                            payment.getAccountCreditedIdentifier()
                    );
                    result.add(saved);

                    financialAccountRepository.creditAccount(
                            conn,
                            payment.getAccountCreditedIdentifier(),
                            payment.getAmount()
                    );

                    transactionRepository.save(
                            conn,
                            collectivityId,
                            mId,
                            payment.getAmount(),
                            payment.getPaymentMode(),
                            payment.getAccountCreditedIdentifier()
                    );
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof RuntimeException re) throw re;
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de base de données : " + e.getMessage(), e);
        }

        return result;
    }
}