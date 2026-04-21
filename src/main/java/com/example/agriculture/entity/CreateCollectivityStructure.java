package com.example.agriculture.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class CreateCollectivityStructure {
    private int presidentId;
    private int vicePresidentId;
    private int treasurerId;
    private int secretaryId;
}
