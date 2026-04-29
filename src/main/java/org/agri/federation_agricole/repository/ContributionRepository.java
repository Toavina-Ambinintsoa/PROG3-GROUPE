package org.agri.federation_agricole.repository;

import org.agri.federation_agricole.config.DataSource;
import org.agri.federation_agricole.entity.Contribution;
import org.agri.federation_agricole.entity.Enum.Frequency;
import org.agri.federation_agricole.entity.Enum.Status;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ContributionRepository {
    private final DataSource dataSource;

    public ContributionRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Contribution> getCollectivityContribution(String collectivityId){
        String query = """
                select id, label, status, frequency, eligible_since, amount
                from contributions where collectivity_id = ?
                """;
        List<Contribution> contributions = new ArrayList<>();
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, collectivityId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
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
}
