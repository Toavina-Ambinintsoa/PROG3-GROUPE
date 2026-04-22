package com.example.agriculture.entity;

import com.example.agriculture.entity.Enum.Frequency;

import java.time.LocalDate;

public class CreateMembershipFee {
    private LocalDate eligibleFrom;
    private Frequency frequency;
    private Double amount;
    private String label;
}
