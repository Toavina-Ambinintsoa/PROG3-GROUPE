package org.agri.federation_agricole.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Enum.Gender;
import org.agri.federation_agricole.entity.Enum.Occupation;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMember {
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String profession;
    private String phone;
    private String email;
    private LocalDate registrationDate;

    private Occupation occupation;
    private String collectivityId;
    private List<String> refereesId;
    private boolean registrationFeePaid;
    private boolean membershipDuesPaid;
}
