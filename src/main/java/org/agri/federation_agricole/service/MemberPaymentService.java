package org.agri.federation_agricole.service;

import org.agri.federation_agricole.entity.CreateMemberPayment;
import org.agri.federation_agricole.entity.MemberPayment;
import org.agri.federation_agricole.repository.PaymentRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberPaymentService {
    private final PaymentRepository paymentRepository;

    public MemberPaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public @Nullable List<MemberPayment> createPayments(String memberId, List<CreateMemberPayment> payments) {
        try {
            return paymentRepository.createPayments(memberId, payments);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}