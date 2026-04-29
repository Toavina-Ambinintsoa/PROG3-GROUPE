package org.agri.federation_agricole.repository;

import org.agri.federation_agricole.config.DataSource;
import org.agri.federation_agricole.entity.Collectivity;
import org.agri.federation_agricole.entity.Collectivityinformation;
import org.agri.federation_agricole.entity.CreateCollectivity;
import org.agri.federation_agricole.exception.UnAuthorizeException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class CollectivityRepository {
    private final DataSource dataSource;

    public CollectivityRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public @Nullable List<Collectivity> getCollectivites() throws SQLException {
        String query = """
                select 
                    c.id, c.number, c.name, c.locality, c.specialization
                from collectivities c;
                """;
        List<Collectivity> collectivities = new ArrayList<>();
        try(Connection connection = dataSource.getConnection()){
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                Collectivity c = new Collectivity();
                c.setId(resultSet.getString("id"));
                c.setNumber(resultSet.getInt("number"));
                c.setName(resultSet.getString("name"));
                c.setLocation(resultSet.getString("locality"));
                c.setSpecialization(resultSet.getString("specialization"));
                collectivities.add(c);
            }
            return collectivities;
        }catch(SQLException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public @Nullable Collectivity getCollectivityById(String id) {
        String  query = """
                select
                    c.id, c.number, c.name, c.locality, c.specialization
                from collectivities c
                where c.id = ?;
        """;
        Collectivity c = new Collectivity();
        try (Connection conn = dataSource.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                c.setId(resultSet.getString("id"));
                c.setNumber(resultSet.getInt("number"));
                c.setName(resultSet.getString("name"));
                c.setLocation(resultSet.getString("locality"));
                c.setSpecialization(resultSet.getString("specialization"));
            }
            return c;
        }catch(SQLException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<Collectivity> saveCollectivities(List<CreateCollectivity> collectivities) {
        List<Collectivity> saved = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            for (CreateCollectivity c : collectivities) {
                Collectivity col = saveCollectivity(conn, c);
                saveCollectivityMembers(conn, col.getId(), c.getMembers());
                saved.add(col);
            }
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }
        return saved;
    }

    private Collectivity saveCollectivity(Connection conn, CreateCollectivity c) throws SQLException {
        String id = getNextId();
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO collectivities (id, locality) VALUES (?, ?)"
        );
        ps.setString(1, id);
        ps.setString(2, c.getLocation());
        ps.executeUpdate();

        Collectivity col = new Collectivity();
        col.setId(id);
        col.setLocation(c.getLocation());
        return col;
    }

    private void saveCollectivityMembers(Connection conn, String collectivityId, List<String> memberIds) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO collectivity_members (collectivity_id, member_id) VALUES (?, ?)"
        );
        for (String memberId : memberIds) {
            ps.setString(1, collectivityId);
            ps.setString(2, memberId);
            ps.addBatch();
        }
        ps.executeBatch();
    }

    public String getLastId(){
        String lastId = null;
        String query = "SELECT id FROM collectivities order by id DESC LIMIT 1;";
        try (Connection conn = dataSource.getConnection()){
            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()){
                lastId = resultSet.getString("id");
            }
            return lastId;
        }catch (SQLException e){
            throw new RuntimeException(e);
        }
    }

    String getNextId(){
        String lastId = getLastId();
        String[] parts = lastId.split("-");
        if(parts.length != 2){
            throw new UnAuthorizeException("Invalid last id format");
        }
        int id = Integer.parseInt(parts[1]);
        id += 1;
        return parts[0]+"-"+id;
    }

    public @Nullable Collectivity setInformations(String id, Collectivityinformation collectivityinformation) {
        String query = """
                UPDATE collectivities set name = ?, number = ? where id = ?
        """;
        try(Connection conn = dataSource.getConnection()){
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, collectivityinformation.getName());
            preparedStatement.setInt(2, collectivityinformation.getNumber());
            preparedStatement.setString(3, id);
            preparedStatement.executeUpdate();
            return getCollectivityById(id);
        }catch(SQLException e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
