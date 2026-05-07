package org.agri.federation_agricole.service;

import org.agri.federation_agricole.entity.Collectivity;
import org.agri.federation_agricole.entity.CollectivityTransaction;
import org.agri.federation_agricole.entity.Collectivityinformation;
import org.agri.federation_agricole.entity.Contribution;
import org.agri.federation_agricole.entity.CreateCollectivity;
import org.agri.federation_agricole.entity.CreateContribution;
import org.agri.federation_agricole.entity.FinancialAccount;
import org.agri.federation_agricole.exception.BadRequestException;
import org.agri.federation_agricole.exception.UnAuthorizeException;
import org.agri.federation_agricole.repository.CollectivityRepository;
import org.agri.federation_agricole.repository.ContributionRepository;
import org.agri.federation_agricole.repository.FinancialAccountRepository;
import org.agri.federation_agricole.repository.MemberRepository;
import org.agri.federation_agricole.repository.TransactionRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CollectivityService {
    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;
    private final FinancialAccountRepository financialAccountRepository;
    private final TransactionRepository transactionRepository;

    public CollectivityService(CollectivityRepository collectivityRepository,
                               MemberRepository memberRepository,
                               ContributionRepository contributionRepository,
                               FinancialAccountRepository financialAccountRepository,
                               TransactionRepository transactionRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
        this.contributionRepository = contributionRepository;
        this.financialAccountRepository = financialAccountRepository;
        this.transactionRepository = transactionRepository;
    }

    public @Nullable Object getCollectities() {
        List<Collectivity> collectivities = new ArrayList<>();
        try {
            collectivities = collectivityRepository.getCollectivites();
            for (Collectivity collectivity : collectivities) {
                collectivity.setMembers(memberRepository.getCollectivityMemberById(collectivity.getId()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return collectivities;
    }

    public @Nullable List<Collectivity> saveCollectivities(List<CreateCollectivity> collectivities) {
        for (CreateCollectivity c : collectivities) {
            if (c.getMembers() == null || c.getMembers().size() < 10) {
                throw new BadRequestException(
                        "Collectivity must have at least 10 members, otherwise actual is "
                                + (c.getMembers() == null ? 0 : c.getMembers().size())
                );
            }
            if (!c.isFederationApproval()) {
                throw new UnAuthorizeException("Creation not approved by federation");
            }
        }
        List<Collectivity> cols = collectivityRepository.saveCollectivities(collectivities);
        for (Collectivity c : cols) {
            c.setStructure(memberRepository.getStructureByCollectivityId(c.getId()));
            c.setMembers(memberRepository.getCollectivityMemberById(c.getId()));
        }
        return cols;
    }

    public @Nullable Object getCollectityById(String id) {
        if (id == null) {
            throw new BadRequestException("id is null");
        }
        return collectivityRepository.getCollectivityById(id);
    }

    public @Nullable Object setInformations(String id, Collectivityinformation collectivityinformation) {
        if (id == null) {
            throw new BadRequestException("id is null");
        }
        if (collectivityinformation == null) {
            throw new BadRequestException("collectivityinformation is null");
        }
        if (collectivityinformation.getName() == null) {
            throw new BadRequestException("name is null");
        }
        return collectivityRepository.setInformations(id, collectivityinformation);
    }

    public List<Contribution> getCollectivityContribution(String collectivityId) {
        if (collectivityId == null) {
            throw new BadRequestException("collectivityId is null");
        }
        return contributionRepository.getCollectivityContribution(collectivityId);
    }

    public List<Contribution> saveCollectivityContributions(String collectivityId, List<CreateContribution> contributions) {
        if (collectivityId == null) {
            throw new BadRequestException("collectivityId is null");
        }
        if (contributions == null || contributions.isEmpty()) {
            throw new BadRequestException("contributions list is empty");
        }
        return contributionRepository.saveContributions(collectivityId, contributions);
    }

    public List<CollectivityTransaction> getCollectivityTransactions(String collectivityId, LocalDate from, LocalDate to) {
        if (collectivityId == null) {
            throw new BadRequestException("collectivityId is null");
        }
        if (from == null || to == null) {
            throw new BadRequestException("from and to dates are required");
        }
        if (from.isAfter(to)) {
            throw new BadRequestException("from date must be before or equal to to date");
        }
        return transactionRepository.getTransactionsByPeriod(collectivityId, from, to);
    }

    public List<FinancialAccount> getCollectivityFinancialAccounts(String collectivityId, LocalDate at) {
        if (collectivityId == null) {
            throw new BadRequestException("collectivityId is null");
        }
        if (at == null) {
            throw new BadRequestException("at date is required");
        }
        return financialAccountRepository.getFinancialAccountsWithBalance(collectivityId, at);
    }
}