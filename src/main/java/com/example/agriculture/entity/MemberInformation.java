package com.example.agriculture.entity;

import com.example.agriculture.entity.Enum.Gender;
import com.example.agriculture.entity.Enum.MemberOccupation;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Data
@Component
public class MemberInformation {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String profession;
    private String phoneNumber;
    private String email;
    private MemberOccupation memberOccupation;
}
