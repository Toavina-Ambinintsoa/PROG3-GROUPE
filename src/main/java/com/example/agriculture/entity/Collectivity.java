package com.example.agriculture.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class Collectivity {
    private int id;
    private String location;
    private CollectivityStructure structure;
    private List<Member> memberList;
}
