package com.example.agriculture.repository;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.*;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class CollectivityRepository {

    private final DataSource dataSource;
    private MemberRepository memberRepository;

    public CollectivityRepository(DataSource dataSource, MemberRepository memberRepository) {
        this.dataSource = dataSource;
        this.memberRepository = memberRepository;
    }

    public List<Collectivity> saveCollectivity(List<CreateCollectivity> collectivities) throws SQLException {

        String sql = "INSERT INTO collectivity (location) VALUES (?) RETURNING id";

        List<Collectivity> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (CreateCollectivity c : collectivities) {
                    stmt.setString(1, c.getLocation());
                    ResultSet rs = stmt.executeQuery();
                    rs.next();
                    int collectivityId = rs.getInt("id");

                    CollectivityStructure structure = saveStructure(conn, collectivityId, c.getCollectivityStructure());
                    saveMembers(conn, collectivityId, c.getMemberIds());

                    List<Member> members = memberRepository.findAllByIds(c.getMemberIds());

                    result.add(new Collectivity(collectivityId, c.getLocation(), structure, members));
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }

        return result;
    }

    private CollectivityStructure saveStructure(Connection conn, int collectivityId, CreateCollectivityStructure structure) throws SQLException {
        String sql = """
            INSERT INTO collectivity_structure
            (collectivity_id, president_id, vice_president_id, treasurer_id, secretary_id)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            stmt.setInt(2, structure.getPresidentId());
            stmt.setInt(3, structure.getVicePresidentId());
            stmt.setInt(4, structure.getTreasurerId());
            stmt.setInt(5, structure.getSecretaryId());
            stmt.executeUpdate();
        }

        List<Member> structureMembers = memberRepository.findAllByIds(List.of(
                structure.getPresidentId(),
                structure.getVicePresidentId(),
                structure.getTreasurerId(),
                structure.getSecretaryId()
        ));

        Map<Integer, Member> memberMap = structureMembers.stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        return new CollectivityStructure(
                memberMap.get(structure.getPresidentId()),
                memberMap.get(structure.getVicePresidentId()),
                memberMap.get(structure.getTreasurerId()),
                memberMap.get(structure.getSecretaryId())
        );
    }

    private void saveMembers(Connection conn, int collectivityId, List<Integer> members) throws SQLException {
        String sql = "INSERT INTO collectivity_member (collectivity_id, member_id) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int memberId : members) {
                stmt.setInt(1, collectivityId);
                stmt.setInt(2, memberId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }
}