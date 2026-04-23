package com.example.agriculture.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DataSource {
    private final String jdbcURl = "jdbc:postgresql://localhost:5432/agri_db";
    private final String user = "postgres";
    private final String password = "toavina";

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(jdbcURl, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
