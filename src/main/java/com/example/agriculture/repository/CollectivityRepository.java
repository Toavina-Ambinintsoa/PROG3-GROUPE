package com.example.agriculture.repository;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.Collectivity;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class CollectivityRepository {
    DataSource ds;

    public CollectivityRepository(DataSource ds) {
        this.ds = ds;
    }

    List<Collectivity> CreateCollectivity(List<Collectivity> collectivity) {
        String query = """
                    INSERT INTO collectivity v
                """;
        try (Connection conn = ds.getConnection()){
            PreparedStatement prstmt = conn.prepareStatement(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
