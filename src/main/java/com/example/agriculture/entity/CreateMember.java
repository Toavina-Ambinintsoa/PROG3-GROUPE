package com.example.agriculture.entity;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class CreateMember extends MemberInformation {
    private String collectivityId;
    private List<Integer> refereesIds;
    private boolean registrationFeePaid;
    private boolean membershipDuesPaid;
}
