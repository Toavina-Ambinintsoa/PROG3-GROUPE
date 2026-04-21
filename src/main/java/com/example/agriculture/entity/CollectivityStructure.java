package com.example.agriculture.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class CollectivityStructure {
    private Member president;
    private Member vicePresident;
    private Member tresorier;
    private Member secretary;
}
