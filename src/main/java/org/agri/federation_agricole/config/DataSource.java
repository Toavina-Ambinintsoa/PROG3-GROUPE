package org.agri.federation_agricole.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DataSource {

    public Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/agri_test_db", "postgres", "toky");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
