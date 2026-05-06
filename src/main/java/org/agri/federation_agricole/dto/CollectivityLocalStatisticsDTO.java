package org.agri.federation_agricole.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Member;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectivityLocalStatisticsDTO {
    private Member memberDescription;
    private long earnedAmount;
    private long unpaidAmount;
}