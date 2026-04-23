package com.example.agriculture.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class CreateMember extends MemberInformation {
    private String collectivityIdentifier;
    private List<Integer> referees;
    private boolean registrationFeePaid;
    private boolean membershipDuesPaid;
}