package com.example.agriculture.service;

import com.example.agriculture.entity.*;
import com.example.agriculture.exception.BadRequestException;
import com.example.agriculture.exception.ConflictException;
import com.example.agriculture.exception.NotFoundException;
import com.example.agriculture.repository.*;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class CollectivityService {

    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final MembershipFeeRepository membershipFeeRepository;
    private final TransactionRepository transactionRepository;
    private final FinancialAccountRepository financialAccountRepository;

    public CollectivityService(CollectivityRepository collectivityRepository,
                               MemberRepository memberRepository,
                               MembershipFeeRepository membershipFeeRepository,
                               TransactionRepository transactionRepository, FinancialAccountRepository financialAccountRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
        this.membershipFeeRepository = membershipFeeRepository;
        this.transactionRepository = transactionRepository;
        this.financialAccountRepository = financialAccountRepository;
    }

    public List<Collectivity> createCollectivities(List<CreateCollectivity> collectivities) throws SQLException {

        for (CreateCollectivity c : collectivities) {

            if (!c.getFederationApproval() || c.getStructure() == null) {
                throw new BadRequestException("Federation approval or structure missing");
            }

            List<Member> members = memberRepository.findAllByIds(c.getMembers());
            if (members.size() != c.getMembers().size()) {
                throw new NotFoundException("One or more members not found");
            }

            if (c.getMembers().size() < 10) {
                throw new BadRequestException("At least 10 members required");
            }

            long seniorCount = members.stream()
                    .filter(m -> m.getAdhesionDate() != null &&
                            m.getAdhesionDate().isBefore(LocalDate.now().minusMonths(6)))
                    .count();
            if (seniorCount < 5) {
                throw new BadRequestException("At least 5 members with 6 months seniority required");
            }

            List<Integer> structureIds = List.of(
                    c.getStructure().getPresident(),
                    c.getStructure().getVicePresident(),
                    c.getStructure().getTreasurer(),
                    c.getStructure().getSecretary()
            );
            List<Member> structureMembers = memberRepository.findAllByIds(structureIds);
            if (structureMembers.size() != structureIds.size()) {
                throw new NotFoundException("One or more structure members not found");
            }
        }

        return collectivityRepository.saveCollectivity(collectivities);
    }

    public Collectivity assignIdentity(String collectivityId, AssignCollectivityIdentity payload) {

        if (!collectivityRepository.collectivityExists(collectivityId)) {
            throw new NotFoundException("Collectivité introuvable : id=" + collectivityId);
        }

        if (payload.getName() != null) {
            if (collectivityRepository.hasName(collectivityId)) {
                throw new ConflictException(
                        "Le nom de la collectivité id=" + collectivityId +
                                " est déjà attribué et ne peut plus être modifié.");
            }
            if (collectivityRepository.nameAlreadyExists(payload.getName())) {
                throw new ConflictException(
                        "Le nom \"" + payload.getName() + "\" est déjà utilisé par une autre collectivité.");
            }
        }

        if (payload.getNumber() != null) {
            if (collectivityRepository.hasNumber(collectivityId)) {
                throw new ConflictException(
                        "Le numéro de la collectivité id=" + collectivityId +
                                " est déjà attribué et ne peut plus être modifié.");
            }
            if (collectivityRepository.numberAlreadyExists(payload.getNumber())) {
                throw new ConflictException(
                        "Le numéro " + payload.getNumber() + " est déjà utilisé par une autre collectivité.");
            }
        }

        return collectivityRepository.assignIdentity(collectivityId, payload.getName(), payload.getNumber());
    }

    public List<MembershipFee> getMembershipFee(String collectivityId) {
        if (!collectivityRepository.collectivityExists(collectivityId)) {
            throw new NotFoundException("Collectivité introuvable : id=" + collectivityId);
        }
        return membershipFeeRepository.findByCollectivityId(collectivityId);
    }

    public List<MembershipFee> createMembershipFee(String collectivityId,
                                                    List<CreateMembershipFee> fees) {
        if (!collectivityRepository.collectivityExists(collectivityId)) {
            throw new NotFoundException("Collectivité introuvable : id=" + collectivityId);
        }

        for (CreateMembershipFee fee : fees) {
            if (fee.getFrequency() == null) {
                throw new BadRequestException("Fréquence non reconnue pour la cotisation : " + fee.getLabel());
            }
            if (fee.getAmount() < 0) {
                throw new BadRequestException("Le montant de la cotisation ne peut pas être négatif.");
            }
        }

        return membershipFeeRepository.save(collectivityId, fees);
    }

    public List<CollectivityTransaction> getTransactions(String collectivityId,
                                                         LocalDate from,
                                                         LocalDate to) {
        if (!collectivityRepository.collectivityExists(collectivityId)) {
            throw new NotFoundException("Collectivité introuvable : id=" + collectivityId);
        }
        if (from == null || to == null) {
            throw new BadRequestException("Les paramètres 'from' et 'to' sont obligatoires.");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("La date 'from' ne peut pas être après la date 'to'.");
        }
        return transactionRepository.findByCollectivityIdAndPeriod(collectivityId, from, to);
    }

    public Collectivity getById(String id) {
        if (!collectivityRepository.collectivityExists(id)) {
            throw new NotFoundException("Collectivité introuvable : id=" + id);
        }

        return collectivityRepository.findById(id);
    }

    public List<FinancialAccount> getFinancialAccounts(String id, LocalDate at) {

        if (!collectivityRepository.collectivityExists(id)) {
            throw new NotFoundException("Collectivité introuvable : id=" + id);
        }

        // 🔹 récupérer tous les comptes
        List<FinancialAccount> accounts =
                financialAccountRepository.findByCollectivityId(id);

        // 🔥 calcul du solde à une date donnée
        for (FinancialAccount account : accounts) {

            Double balance = transactionRepository
                    .sumAmountByAccountUntilDate(account.getId(), at);

            account.setAmount(balance != null ? balance : 0);
        }

        return accounts;
    }
}
