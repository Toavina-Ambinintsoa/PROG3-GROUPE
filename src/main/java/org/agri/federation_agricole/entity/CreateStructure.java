package org.agri.federation_agricole.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStructure {
    private String president;
    private String vice_president;
    private String secretary;
    private String treasurer;
}
