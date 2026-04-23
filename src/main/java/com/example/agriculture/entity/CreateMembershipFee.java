package com.example.agriculture.entity;

import com.example.agriculture.entity.Enum.Frequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMembershipFee {
    private LocalDate eligibleFrom;
    private Frequency frequency;
    private Double amount;
    private String label;
}
