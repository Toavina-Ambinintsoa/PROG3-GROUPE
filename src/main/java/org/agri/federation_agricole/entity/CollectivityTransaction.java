package org.agri.federation_agricole.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Enum.PaymentMode;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectivityTransaction {
    private String id;
    private LocalDate creationDate;
    private int amount;
    private PaymentMode paymentMode;
    private FinancialAccount accountCredited;
    private Member memberDebited;
}