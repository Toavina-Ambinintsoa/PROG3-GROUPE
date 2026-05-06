package org.agri.federation_agricole.controller;

import org.agri.federation_agricole.entity.CreateMember;
import org.agri.federation_agricole.entity.CreateMemberPayment;
import org.agri.federation_agricole.service.MemberPaymentService;
import org.agri.federation_agricole.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;
    private final MemberPaymentService memberPaymentService;

    public MemberController(MemberService memberService, MemberPaymentService memberPaymentService) {
        this.memberService = memberService;
        this.memberPaymentService = memberPaymentService;
    }

    @PostMapping
    public ResponseEntity<?> createMembers(@RequestBody List<CreateMember> members) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(memberService.createMembers(members));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<?> createPayments(
            @PathVariable String id,
            @RequestBody List<CreateMemberPayment> payments) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(memberPaymentService.createPayments(id, payments));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}