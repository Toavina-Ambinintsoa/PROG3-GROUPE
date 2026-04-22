package com.example.agriculture.entity;

import com.example.agriculture.entity.Enum.PaymentMode;

import java.time.LocalDate;

public class MemberPayment {
    private String id;
    private int amount;
    private PaymentMode paymentMode;
    private FinancialAccount accountCredited;
    private LocalDate creationDate;
}
