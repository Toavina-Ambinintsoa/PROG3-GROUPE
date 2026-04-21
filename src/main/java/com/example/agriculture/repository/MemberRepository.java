package com.example.agriculture.repository;

import com.example.agriculture.config.DataSource;
import com.example.agriculture.entity.Member;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class MemberRepository {
    private DataSource dataSource;

    public MemberRepository(DataSource dataSource){
        this.dataSource = dataSource;
    }


}
