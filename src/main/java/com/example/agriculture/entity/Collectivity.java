package com.example.agriculture.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Collectivity {
    private String id;
    private String name;
    private Integer number;

    private String location;
    private CollectivityStructure structure;
    private List<Member> members;

    public Collectivity(String collectivityId, String location, CollectivityStructure structure, List<Member> members) {
        this.id = collectivityId;
        this.location = location;
        this.structure = structure;
        this.members = members;
    }
}
