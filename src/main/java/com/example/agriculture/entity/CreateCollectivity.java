package com.example.agriculture.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCollectivity {
    private String location;
    private List<Integer> members;
    private Boolean federationApproval;
    private CreateCollectivityStructure structure;
    private String specialty;
}
