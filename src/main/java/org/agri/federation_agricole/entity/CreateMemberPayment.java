package org.agri.federation_agricole.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Enum.PaymentMode;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMemberPayment {
    private int amount;
    private String membershipFeeIdentifier;
    private String accountCreditedIdentifier;
    private PaymentMode paymentMode;
}