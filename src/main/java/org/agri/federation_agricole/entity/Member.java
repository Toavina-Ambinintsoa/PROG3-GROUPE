package org.agri.federation_agricole.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Enum.Gender;
import org.agri.federation_agricole.entity.Enum.Occupation;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    private String id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String email;
    private String phone;
    private String profession;
    private LocalDate registrationDate;
    private Occupation occupation;
    private List<Member> referees;
}
