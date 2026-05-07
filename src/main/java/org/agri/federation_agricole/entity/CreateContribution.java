package org.agri.federation_agricole.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Enum.Frequency;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateContribution {
    private LocalDate eligibleFrom;
    private Frequency frequency;
    private int amount;
    private String label;
}