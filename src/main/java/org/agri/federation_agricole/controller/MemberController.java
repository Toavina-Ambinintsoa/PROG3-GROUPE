package org.agri.federation_agricole.controller;

import org.agri.federation_agricole.entity.CreateMember;
import org.agri.federation_agricole.entity.Member;
import org.agri.federation_agricole.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public ResponseEntity<?> createMembers(@RequestBody List<CreateMember> members) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(memberService.createMembers(members));
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
