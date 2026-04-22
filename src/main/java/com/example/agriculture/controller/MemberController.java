package com.example.agriculture.controller;

import com.example.agriculture.entity.CreateMember;
import com.example.agriculture.entity.CreateMemberPayment;
import com.example.agriculture.entity.Member;
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
    @ResponseStatus(HttpStatus.CREATED)
    public List<Member> createMembers(@RequestBody List<CreateMember> requests) {
        return memberService.createMembers(requests);
    }

    @PostMapping("/{id}/{payment}")
    public ResponseEntity<?> createPayment(
            @PathVariable String id,
            @RequestBody CreateMemberPayment payment
    )
    {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(memberService.createMemberPayment(id, payment));
        }catch (Exception e){
            throw new RuntimeException("Not implemented yet");
        }
    }
}
