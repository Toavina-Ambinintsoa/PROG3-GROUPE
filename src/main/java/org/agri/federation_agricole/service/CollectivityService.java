package org.agri.federation_agricole.service;

import org.agri.federation_agricole.entity.Collectivity;
import org.agri.federation_agricole.entity.Collectivityinformation;
import org.agri.federation_agricole.entity.Contribution;
import org.agri.federation_agricole.entity.CreateCollectivity;
import org.agri.federation_agricole.exception.BadRequestException;
import org.agri.federation_agricole.exception.UnAuthorizeException;
import org.agri.federation_agricole.repository.CollectivityRepository;
import org.agri.federation_agricole.repository.ContributionRepository;
import org.agri.federation_agricole.repository.MemberRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CollectivityService {
    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final ContributionRepository contributionRepository;

    public CollectivityService(CollectivityRepository collectivityRepository, MemberRepository memberRepository, ContributionRepository contributionRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
        this.contributionRepository = contributionRepository;
    }


    public @Nullable Object getCollectities(){
        List<Collectivity> collectivities = new ArrayList<>();
        try {
            collectivities =  collectivityRepository.getCollectivites();
            for (Collectivity collectivity : collectivities) {
                collectivity.setMembers(memberRepository.getCollectivityMemberById(collectivity.getId()));
            }
        }catch(SQLException e){
            throw new RuntimeException(e);
        }
        return collectivities;
    }

    public @Nullable List<Collectivity> saveCollectivities(List<CreateCollectivity> collectivities) {
        for (CreateCollectivity c : collectivities) {
            if (c.getMembers().size() < 2) {
                throw new RuntimeException("Not enough members");
            }
            if (!c.isFederationApproval()) {
                throw new UnAuthorizeException("Creation not approved");
            }
        }
        List<Collectivity> cols = collectivityRepository.saveCollectivities(collectivities);
        for (Collectivity c: cols) {
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
        if (collectivityinformation.getName() == null){
            throw new BadRequestException("name is null");
        }
        return collectivityRepository.setInformations(id, collectivityinformation);
    }

    public List<Contribution> getCollectivityContribution(String collectivityId){
        if (collectivityId == null) {
            throw new BadRequestException("collectivityId is null");
        }
        return contributionRepository.getCollectivityContribution(collectivityId);
    }

    public @Nullable Object getCollectivityFinancialAccounts(String id, DateTimeFormat.ISO at) {
        throw new RuntimeException("Not implemented yet");
    }
}
