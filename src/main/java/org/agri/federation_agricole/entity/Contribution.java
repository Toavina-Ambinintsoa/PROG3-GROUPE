package org.agri.federation_agricole.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Enum.Frequency;
import org.agri.federation_agricole.entity.Enum.Status;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contribution {
    private String id;
    private String label;
    private Status status;
    private Frequency frequency;
    private LocalDate eligibleSince;
    private int amount;
}
