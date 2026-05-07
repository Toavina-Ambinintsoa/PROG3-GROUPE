package org.agri.federation_agricole.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDescriptionDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String occupation;
}