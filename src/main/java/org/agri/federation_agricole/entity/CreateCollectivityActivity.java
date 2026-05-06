package org.agri.federation_agricole.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Enum.ActivityType;
import org.agri.federation_agricole.entity.Enum.Occupation;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCollectivityActivity {
    private String label;
    private ActivityType activityType;

    /**
     * Occupations concerned by this activity (mandatory attendance).
     * If null/empty, all members are concerned.
     */
    private List<Occupation> memberOccupationConcerned;

    /**
     * Recurrence rule for recurring activities.
     * Mutually exclusive with executiveDate.
     */
    private MonthlyRecurrenceRule recurrenceRule;

    /**
     * Specific one-shot date.
     * Mutually exclusive with recurrenceRule.
     */
    private LocalDate executiveDate;
}