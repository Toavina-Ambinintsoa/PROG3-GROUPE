package com.example.agriculture.service;

import com.example.agriculture.entity.*;
import com.example.agriculture.exception.BadRequestException;
import com.example.agriculture.exception.ConflictException;
import com.example.agriculture.exception.NotFoundException;
import com.example.agriculture.repository.CollectivityRepository;
import com.example.agriculture.repository.MemberRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class CollectivityService {

    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;

    public CollectivityService(CollectivityRepository collectivityRepository,
                               MemberRepository memberRepository) {
        this.collectivityRepository = collectivityRepository;
        this.memberRepository = memberRepository;
    }

    public List<Collectivity> createCollectivities(List<CreateCollectivity> collectivities) throws SQLException {

        for (CreateCollectivity c : collectivities) {

            // 400 - autorisation fédération manquante ou structure nulle
            if (!c.getFederationApproval() || c.getStructure() == null) {
                throw new BadRequestException("Federation approval or structure missing");
            }

            // 404 - membres introuvables
            List<Member> members = memberRepository.findAllByIds(c.getMembers());
            if (members.size() != c.getMembers().size()) {
                throw new NotFoundException("One or more members not found");
            }

            // 400 - règle A : au moins 10 membres
            if (c.getMembers().size() < 10) {
                throw new BadRequestException("At least 10 members required");
            }

            // 400 - règle A : au moins 5 membres avec ancienneté > 6 mois
            long seniorCount = members.stream()
                    .filter(m -> m.getAdhesionDate().isBefore(LocalDate.now().minusMonths(6)))
                    .count();
            if (seniorCount < 5) {
                throw new BadRequestException("At least 5 members with 6 months seniority required");
            }

            // 404 - membres de la structure introuvables
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


    public Collectivity assignIdentity(int collectivityId, AssignCollectivityIdentity payload) {

        // 404 - collectivité introuvable
        if (!collectivityRepository.collectivityExists(collectivityId)) {
            throw new NotFoundException("Collectivité introuvable : id=" + collectivityId);
        }

        if (payload.getName() != null) {
            // 409 - nom déjà attribué, immuable
            if (collectivityRepository.hasName(collectivityId)) {
                throw new ConflictException(
                        "Le nom de la collectivité id=" + collectivityId +
                                " est déjà attribué et ne peut plus être modifié."
                );
            }
            // 409 - nom déjà utilisé par une autre collectivité
            if (collectivityRepository.nameAlreadyExists(payload.getName())) {
                throw new ConflictException(
                        "Le nom \"" + payload.getName() + "\" est déjà utilisé par une autre collectivité."
                );
            }
        }

        if (payload.getNumber() != null) {
            // 409 - numéro déjà attribué, immuable
            if (collectivityRepository.hasNumber(collectivityId)) {
                throw new ConflictException(
                        "Le numéro de la collectivité id=" + collectivityId +
                                " est déjà attribué et ne peut plus être modifié."
                );
            }
            // 409 - numéro déjà utilisé par une autre collectivité
            if (collectivityRepository.numberAlreadyExists(payload.getNumber())) {
                throw new ConflictException(
                        "Le numéro " + payload.getNumber() + " est déjà utilisé par une autre collectivité."
                );
            }
        }

        return collectivityRepository.assignIdentity(collectivityId, payload.getName(), payload.getNumber());
    }


    /// TODO: implementing

    public MembershipFee getMembershipFee(String id) {
        throw new RuntimeException("Not implemented yet");
    }

    public @Nullable MembershipFee createMembershipFee(String id, List<CreateMembershipFee> payload) {
        throw new RuntimeException("Not implemented yet");
    }

    public @Nullable Object getTransactions(String id, LocalDate from, LocalDate to) {
        throw new RuntimeException("Not implemented yet");
    }
}