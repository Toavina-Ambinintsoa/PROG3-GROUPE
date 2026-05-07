package org.agri.federation_agricole.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Collectivity {
    private String id;
    private Integer number;
    private String name;
    private String location;
    private String specialization;
    private Structure structure;
    private List<Member> members;
}
