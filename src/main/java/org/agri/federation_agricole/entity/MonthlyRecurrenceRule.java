package org.agri.federation_agricole.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRecurrenceRule {
    /**
     * Week position in a month: 1 = 1st week, 2 = 2nd, ... 5 = 5th.
     */
    private int weekOrdinal;

    /**
     * Day of week: MO, TU, WE, TH, FR, SA, SU
     */
    private String dayOfWeek;
}