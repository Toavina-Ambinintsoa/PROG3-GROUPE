package com.example.agriculture.entity;

import lombok.Data;

import java.util.List;

@Data
public class CreateMember extends MemberInformation {
    private String collectivityId;
    private List<Integer> refereesIds;
    private boolean registrationFeePaid;
    private boolean membershipDuesPaid;
}
