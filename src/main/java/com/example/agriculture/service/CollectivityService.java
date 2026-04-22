package com.example.agriculture.service;

import com.example.agriculture.entity.Collectivity;
import com.example.agriculture.entity.CreateCollectivity;
import com.example.agriculture.entity.Member;
import com.example.agriculture.repository.CollectivityRepository;
import com.example.agriculture.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Federation approval or structure missing");
            }

            // 404 - membres introuvables
            List<Member> members = memberRepository.findAllByIds(c.getMembers());
            if (members.size() != c.getMembers().size()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "One or more members not found");
            }

            // 400 - règle A : au moins 10 membres
            if (c.getMembers().size() < 10) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "At least 10 members required");
            }

            // 400 - règle A : au moins 5 membres avec ancienneté > 6 mois
            long seniorCount = members.stream()
                    .filter(m -> m.getAdhesionDate().isBefore(LocalDate.now().minusMonths(6)))
                    .count();
            if (seniorCount < 5) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "At least 5 members with 6 months seniority required");
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
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "One or more structure members not found");
            }
        }

        return collectivityRepository.saveCollectivity(collectivities);
    }
}
