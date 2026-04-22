package com.example.agriculture.entity;

import com.example.agriculture.entity.Enum.Gender;
import com.example.agriculture.entity.Enum.MemberOccupation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Member extends MemberInformation {
    private Integer id;
    private List<Member> referees;

    public Member(String firstName, String lastName, LocalDate birthDate, Gender gender, String address, String profession, String phoneNumber, String email, MemberOccupation memberOccupation, LocalDate adhesionDate) {
        super(firstName, lastName, birthDate, gender, address, profession, phoneNumber, email, memberOccupation, adhesionDate);
    }
}
