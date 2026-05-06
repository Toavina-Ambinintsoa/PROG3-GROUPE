package com.example.agriculture.controller;

import com.example.agriculture.entity.CreateMember;
import com.example.agriculture.entity.CreateMemberPayment;
import com.example.agriculture.entity.Member;
import com.example.agriculture.entity.MemberPayment;
import com.example.agriculture.exception.BadRequestException;
import com.example.agriculture.exception.NotFoundException;
import com.example.agriculture.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<?> createMembers(@RequestBody List<CreateMember> requests) {
        try {
            List<Member> created = memberService.createMembers(requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/payment")
    public ResponseEntity<?> createPayments(
            @PathVariable String id,
            @RequestBody List<CreateMemberPayment> payments) {
        try {
            List<MemberPayment> result = memberService.createMemberPayments(id, payments);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
