package org.agri.federation_agricole.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollectivity {
    private String location;
    private boolean federationApproval;
    private List<String> members;
    private CreateStructure structure;
}
