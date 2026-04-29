package org.agri.federation_agricole.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Enum.AccountType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinancialAccount {
    private String id;
    private String collectivityId;
    private AccountType accountType;
    private Integer initialBalance;
    private String holder;
    private String phone;
}
