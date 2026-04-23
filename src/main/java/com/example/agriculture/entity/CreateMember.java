package com.example.agriculture.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMember extends MemberInformation {
    private String collectivityIdentifier;
    private List<Integer> referees;
    private boolean registrationFeePaid;
    private boolean membershipDuesPaid;
}
