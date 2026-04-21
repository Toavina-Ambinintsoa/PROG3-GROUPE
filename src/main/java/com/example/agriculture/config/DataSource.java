package com.example.agriculture.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DataSource {
    private final String jdbcURl = System.getenv("JDBC_URl"); //
    private final String user = System.getenv("USER");
    private final String password = System.getenv("PASSWORD");

    @Bean
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(jdbcURl, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
