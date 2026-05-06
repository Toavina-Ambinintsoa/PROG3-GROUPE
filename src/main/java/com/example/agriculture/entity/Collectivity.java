package com.example.agriculture.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Collectivity {
    private String id;
    private String name;
    private Integer number;

    private String location;
    private CollectivityStructure structure;
    private List<Member> members;

    private String specialty;
    private LocalDate createdAt;

    public Collectivity(String collectivityId, String location, String specialty, LocalDate createdAt, CollectivityStructure structure, List<Member> members) {
        this.id = collectivityId;
        this.location = location;
        this.specialty = specialty;
        this.createdAt = createdAt;
        this.structure = structure;
        this.members = members;
    }
}
