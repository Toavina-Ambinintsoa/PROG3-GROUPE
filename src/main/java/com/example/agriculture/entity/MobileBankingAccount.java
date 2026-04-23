package com.example.agriculture.entity;

import com.example.agriculture.entity.Enum.MobileBankingService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class MobileBankingAccount extends FinancialAccount {
    private String holderName;
    private MobileBankingService mobileBankingService;
    private String mobileNumber;
}
