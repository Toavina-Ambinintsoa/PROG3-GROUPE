package com.example.agriculture.entity;

import com.example.agriculture.entity.Enum.Bank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class BankAccount extends FinancialAccount {
    private String holderName;
    private Bank bankName;
    private String bankCode;        // 5 digits
    private String bankBranchCode;  // 5 digits
    private String bankAccountNumber; // 11 digits
    private String bankAccountKey;  // 2 digits (clé RIB)
}
