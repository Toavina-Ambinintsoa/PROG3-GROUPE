package org.agri.federation_agricole.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectivityInformationDTO {
    private String id;
    private Integer number;
    private String name;
}