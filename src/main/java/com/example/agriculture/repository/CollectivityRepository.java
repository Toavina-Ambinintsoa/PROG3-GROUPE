package com.example.agriculture.repository;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.*;
import com.example.agriculture.entity.Enum.MemberOccupation;
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
    private final MemberRepository memberRepository;

    public CollectivityRepository(DataSource dataSource, MemberRepository memberRepository) {
        this.dataSource = dataSource;
        this.memberRepository = memberRepository;
    }

    public List<Collectivity> saveCollectivity(List<CreateCollectivity> collectivities) throws SQLException {

        String sql = "INSERT INTO collectivity (location, specialty) VALUES (?, ?) RETURNING id, created_at";

        List<Collectivity> result = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (CreateCollectivity c : collectivities) {
                    stmt.setString(1, c.getLocation());
                    stmt.setString(2, c.getSpecialty());
                    ResultSet rs = stmt.executeQuery();
                    rs.next();
                    String collectivityId = rs.getString("id");

                    CollectivityStructure structure = saveStructure(conn, Integer.parseInt(collectivityId), c.getStructure());
                    saveMembers(conn, Integer.parseInt(collectivityId), c.getMembers());

                    List<Member> members = memberRepository.findAllByIds(c.getMembers());

                    result.add(new Collectivity(collectivityId, c.getLocation(), c.getSpecialty(), rs.getDate("created_at").toLocalDate(), structure, members));
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
            stmt.setInt(2, structure.getPresident());
            stmt.setInt(3, structure.getVicePresident());
            stmt.setInt(4, structure.getTreasurer());
            stmt.setInt(5, structure.getSecretary());
            stmt.executeUpdate();
        }

        List<Member> structureMembers = memberRepository.findAllByIds(List.of(
                structure.getPresident(),
                structure.getVicePresident(),
                structure.getTreasurer(),
                structure.getSecretary()
        ));

        Map<Integer, Member> memberMap = structureMembers.stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        return new CollectivityStructure(
                memberMap.get(structure.getPresident()),
                memberMap.get(structure.getVicePresident()),
                memberMap.get(structure.getTreasurer()),
                memberMap.get(structure.getSecretary())
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

    public boolean collectivityExists(int collectivityId) {
        String sql = "SELECT COUNT(id) FROM collectivity WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean nameAlreadyExists(String name) {
        String sql = "SELECT COUNT(id) FROM collectivity WHERE name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean numberAlreadyExists(int number) {
        String sql = "SELECT COUNT(id) FROM collectivity WHERE number = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, number);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasName(int collectivityId) {
        String sql = "SELECT name FROM collectivity WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                return name != null && !name.isBlank();
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean hasNumber(int collectivityId) {
        String sql = "SELECT number FROM collectivity WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getObject("number") != null;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Collectivity assignIdentity(int collectivityId, String name, Integer number) {
        StringBuilder sql = new StringBuilder("UPDATE collectivity SET ");
        List<Object> params = new ArrayList<>();

        if (name != null) {
            sql.append("name = ?, ");
            params.add(name);
        }
        if (number != null) {
            sql.append("number = ?, ");
            params.add(number);
        }

        String query = sql.toString().replaceAll(", $", "") + " WHERE id = ?";
        params.add(collectivityId);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return findById(collectivityId);
    }

    public Collectivity findById(int collectivityId) {
        String sql = """
                SELECT c.id, c.location, c.name, c.number, c.specialty, c.created_at,
                       p.id  AS p_id,  p.first_name AS p_fn,  p.last_name AS p_ln,  p.occupation AS p_occ,
                       vp.id AS vp_id, vp.first_name AS vp_fn, vp.last_name AS vp_ln, vp.occupation AS vp_occ,
                       t.id  AS t_id,  t.first_name  AS t_fn,  t.last_name  AS t_ln,  t.occupation  AS t_occ,
                       s.id  AS s_id,  s.first_name  AS s_fn,  s.last_name  AS s_ln,  s.occupation  AS s_occ
                FROM collectivity c
                LEFT JOIN member p  ON c.president_id      = p.id
                LEFT JOIN member vp ON c.vice_president_id = vp.id
                LEFT JOIN member t  ON c.treasurer_id      = t.id
                LEFT JOIN member s  ON c.secretary_id      = s.id
                WHERE c.id = ?
                """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Collectivity mapRow(ResultSet rs) throws SQLException {
        Collectivity c = new Collectivity();
        c.setId(String.valueOf(rs.getInt("id")));
        c.setLocation(rs.getString("location"));
        c.setName(rs.getString("name"));
        c.setNumber(rs.getObject("number") != null ? rs.getInt("number") : null);
        c.setSpecialty(rs.getString("specialty"));
        c.setCreatedAt(rs.getDate("created_at").toLocalDate());

        CollectivityStructure structure = new CollectivityStructure();
        structure.setPresident(mapMember(rs, "p_"));
        structure.setVicePresident(mapMember(rs, "vp_"));
        structure.setTreasurer(mapMember(rs, "t_"));
        structure.setSecretary(mapMember(rs, "s_"));
        c.setStructure(structure);

        return c;
    }

    private Member mapMember(ResultSet rs, String prefix) throws SQLException {
        Member m = new Member();
        m.setId(rs.getInt(prefix + "id"));
        m.setFirstName(rs.getString(prefix + "fn"));
        m.setLastName(rs.getString(prefix + "ln"));
        m.setOccupation(MemberOccupation.valueOf(rs.getString(prefix + "occ")));
        return m;
    }
}