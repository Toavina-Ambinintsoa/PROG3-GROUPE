package com.example.agriculture.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class Member extends MemberInformation {
    private int id;
    private List<Member> referees;
}
