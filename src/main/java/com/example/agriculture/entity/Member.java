package com.example.agriculture.entity;

import com.example.agriculture.entity.Enum.Gender;
import com.example.agriculture.entity.Enum.MemberOccupation;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class Member {
    private int id;
    private Gender gender;
    private MemberOccupation memberoccupation;
}
