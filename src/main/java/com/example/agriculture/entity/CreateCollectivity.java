package com.example.agriculture.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class CreateCollectivity {
    private String location;
    private List<Integer> memberIds;
    private Boolean federationApproval;
    private CreateCollectivityStructure collectivityStructure;
}
