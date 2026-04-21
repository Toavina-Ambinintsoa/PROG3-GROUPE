package com.example.agriculture.repository;

import org.springframework.stereotype.Repository;

import java.sql.Connection;

@Repository
public class CollectivityRepository {
    Connection connection;

    public CollectivityRepository(Connection connection) {
        this.connection = connection;
    }




}
