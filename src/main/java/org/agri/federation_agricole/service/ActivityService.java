package org.agri.federation_agricole.service;

import org.agri.federation_agricole.dto.ActivityMemberAttendanceDTO;
import org.agri.federation_agricole.entity.CollectivityActivity;
import org.agri.federation_agricole.entity.CreateActivityMemberAttendance;
import org.agri.federation_agricole.entity.CreateCollectivityActivity;
import org.agri.federation_agricole.exception.BadRequestException;
import org.agri.federation_agricole.repository.ActivityRepository;
import org.agri.federation_agricole.repository.CollectivityRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final CollectivityRepository collectivityRepository;

    public ActivityService(ActivityRepository activityRepository,
                           CollectivityRepository collectivityRepository) {
        this.activityRepository = activityRepository;
        this.collectivityRepository = collectivityRepository;
    }

    public List<CollectivityActivity> getActivities(String collectivityId) {
        if (collectivityId == null) {
            throw new BadRequestException("collectivityId is required");
        }
        if (collectivityRepository.getCollectivityById(collectivityId) == null) {
            throw new BadRequestException("Collectivity not found");
        }
        return activityRepository.getActivitiesByCollectivityId(collectivityId);
    }

    public List<CollectivityActivity> saveActivities(String collectivityId,
                                                     List<CreateCollectivityActivity> createList) {
        if (collectivityId == null) {
            throw new BadRequestException("collectivityId is required");
        }
        if (createList == null || createList.isEmpty()) {
            throw new BadRequestException("Activities list must not be empty");
        }
        if (collectivityRepository.getCollectivityById(collectivityId) == null) {
            throw new BadRequestException("Collectivity not found");
        }
        return activityRepository.saveActivities(collectivityId, createList);
    }

    public List<ActivityMemberAttendanceDTO> saveAttendance(String collectivityId,
                                                            String activityId,
                                                            List<CreateActivityMemberAttendance> createList) {
        if (collectivityId == null || activityId == null) {
            throw new BadRequestException("collectivityId and activityId are required");
        }
        if (createList == null || createList.isEmpty()) {
            throw new BadRequestException("Attendance list must not be empty");
        }
        if (collectivityRepository.getCollectivityById(collectivityId) == null) {
            throw new BadRequestException("Collectivity not found");
        }
        if (activityRepository.getActivityById(activityId) == null) {
            throw new BadRequestException("Activity not found");
        }
        return activityRepository.saveAttendance(collectivityId, activityId, createList);
    }

    public List<ActivityMemberAttendanceDTO> getAttendance(String collectivityId,
                                                           String activityId) {
        if (collectivityId == null || activityId == null) {
            throw new BadRequestException("collectivityId and activityId are required");
        }
        if (collectivityRepository.getCollectivityById(collectivityId) == null) {
            throw new BadRequestException("Collectivity not found");
        }
        if (activityRepository.getActivityById(activityId) == null) {
            throw new BadRequestException("Activity not found");
        }
        return activityRepository.getAttendanceByActivityId(collectivityId, activityId);
    }
}