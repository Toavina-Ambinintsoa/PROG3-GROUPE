package org.agri.federation_agricole.repository;

import org.agri.federation_agricole.config.DataSource;
import org.agri.federation_agricole.entity.Contribution;
import org.agri.federation_agricole.entity.CreateContribution;
import org.agri.federation_agricole.entity.Enum.Frequency;
import org.agri.federation_agricole.entity.Enum.Status;
import org.agri.federation_agricole.exception.BadRequestException;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ContributionRepository {
    private final DataSource dataSource;

    public ContributionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Contribution> getCollectivityContribution(String collectivityId) {
        String query = """
                select id, label, status, frequency, eligible_since, amount
                from contributions where collectivity_id = ?
                """;
        List<Contribution> contributions = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, collectivityId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Contribution contribution = new Contribution();
                contribution.setId(resultSet.getString("id"));
                contribution.setLabel(resultSet.getString("label"));
                contribution.setStatus(Status.valueOf(resultSet.getString("status")));
                contribution.setEligibleSince(resultSet.getDate("eligible_since").toLocalDate());
                contribution.setAmount(resultSet.getInt("amount"));
                contribution.setFrequency(Frequency.valueOf(resultSet.getString("frequency")));
                contributions.add(contribution);
            }
            return contributions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Contribution> saveContributions(String collectivityId, List<CreateContribution> createContributions) {
        String insertQuery = """
                INSERT INTO contributions (id, collectivity_id, label, status, frequency, eligible_since, amount)
                VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id
                """;
        List<String> savedIds = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            for (CreateContribution c : createContributions) {
                if (c.getAmount() < 0) {
                    throw new BadRequestException("Amount must be >= 0");
                }
                if (c.getFrequency() == null) {
                    throw new BadRequestException("Frequency is required");
                }
                String id = getNextId(collectivityId);
                PreparedStatement ps = conn.prepareStatement(insertQuery);
                ps.setString(1, id);
                ps.setString(2, collectivityId);
                ps.setString(3, c.getLabel());
                ps.setString(4, Status.ACTIVE.name());
                ps.setString(5, c.getFrequency().name());
                ps.setDate(6, Date.valueOf(c.getEligibleFrom()));
                ps.setInt(7, c.getAmount());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    savedIds.add(rs.getString("id"));
                }
            }
            conn.commit();
        } catch (BadRequestException e) {
            throw e;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return getContributionsByIds(savedIds);
    }

    private List<Contribution> getContributionsByIds(List<String> ids) {
        if (ids.isEmpty()) return new ArrayList<>();
        String query = """
                select id, label, status, frequency, eligible_since, amount
                from contributions where id = ?
                """;
        List<Contribution> contributions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            for (String id : ids) {
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    Contribution contribution = new Contribution();
                    contribution.setId(rs.getString("id"));
                    contribution.setLabel(rs.getString("label"));
                    contribution.setStatus(Status.valueOf(rs.getString("status")));
                    contribution.setEligibleSince(rs.getDate("eligible_since").toLocalDate());
                    contribution.setAmount(rs.getInt("amount"));
                    contribution.setFrequency(Frequency.valueOf(rs.getString("frequency")));
                    contributions.add(contribution);
                }
            }
            return contributions;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getLastContributionId(String collectivityId) {
        String query = "SELECT id FROM contributions WHERE collectivity_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, collectivityId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
            String[] parts = collectivityId.split("-");
            return parts[0] + "-CT0";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getNextId(String collectivityId) {
        String lastId = getLastContributionId(collectivityId);
        String[] parts = lastId.split("-CT");
        if (parts.length != 2) {
            String[] colParts = collectivityId.split("-");
            return colParts[0] + "-CT1";
        }
        int num = Integer.parseInt(parts[1]);
        num += 1;
        return parts[0] + "-CT" + num;
    }
}