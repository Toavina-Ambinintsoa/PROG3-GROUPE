package org.agri.federation_agricole.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Collectivityinformation;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectivityOverallStatisticsDTO {
    private CollectivityInformationDTO collectivityInformation;
    private int newMembersNumber;
    private double overallMemberCurrentDuePercentage;
}