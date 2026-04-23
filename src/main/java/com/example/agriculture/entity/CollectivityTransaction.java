package com.example.agriculture.entity;

import com.example.agriculture.entity.Enum.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectivityTransaction {
    private String id;
    private LocalDate creationDate;
    private double amount;
    private PaymentMode paymentMode;
    private FinancialAccount accountCredited;
    private Member memberDebited;
}
