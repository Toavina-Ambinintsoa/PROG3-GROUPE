package com.example.agriculture.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class CreateCollectivityStructure {
    private int president;
    private int vicePresident;
    private int treasurer;
    private int secretary;
}
