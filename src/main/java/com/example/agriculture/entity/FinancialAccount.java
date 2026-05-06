package com.example.agriculture.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = CashAccount.class,          name = "CASH"),
    @JsonSubTypes.Type(value = MobileBankingAccount.class, name = "MOBILE_BANKING"),
    @JsonSubTypes.Type(value = BankAccount.class,          name = "BANK_TRANSFER")
})
public abstract class FinancialAccount {
    private String id;
    private double amount;
}
