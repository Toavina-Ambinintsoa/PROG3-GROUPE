package com.example.agriculture.entity;

import com.example.agriculture.entity.Enum.Status;
import com.example.agriculture.entity.Enum.Frequency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class MembershipFee {
    private String id;
    private LocalDate eligibleFrom;
    private Frequency frequency;
    private double amount;
    private String label;
    private Status status;
}
