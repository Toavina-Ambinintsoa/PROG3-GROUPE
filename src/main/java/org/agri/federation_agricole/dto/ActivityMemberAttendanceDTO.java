package org.agri.federation_agricole.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agri.federation_agricole.entity.Enum.AttendanceStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMemberAttendanceDTO {
    private String id;
    private MemberDescriptionDTO memberDescription;
    private AttendanceStatus attendanceStatus;
}