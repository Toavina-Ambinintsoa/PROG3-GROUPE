package org.agri.federation_agricole.controller;

import org.agri.federation_agricole.entity.Collectivityinformation;
import org.agri.federation_agricole.entity.CreateCollectivity;
import org.agri.federation_agricole.entity.CreateContribution;
import org.agri.federation_agricole.entity.CreateCollectivityActivity;
import org.agri.federation_agricole.entity.CreateActivityMemberAttendance;
import org.agri.federation_agricole.service.ActivityService;
import org.agri.federation_agricole.service.CollectivityService;
import org.agri.federation_agricole.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/collectivities")
public class CollectivityController {
    private final CollectivityService collectivityService;
    private final StatisticsService statisticsService;
    private final ActivityService activityService;

    public CollectivityController(CollectivityService collectivityService,
                                  StatisticsService statisticsService,
                                  ActivityService activityService) {
        this.collectivityService = collectivityService;
        this.statisticsService = statisticsService;
        this.activityService = activityService;
    }

    @GetMapping
    public ResponseEntity<?> getCollectivities() {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(collectivityService.getCollectities());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectivitiesById(@PathVariable String id) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(collectivityService.getCollectityById(id));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> saveCollectivities(@RequestBody List<CreateCollectivity> collectivities) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(collectivityService.saveCollectivities(collectivities));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PutMapping("/{id}/informations")
    public ResponseEntity<?> setInformations(@PathVariable String id, @RequestBody Collectivityinformation collectivityinformation) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(collectivityService.setInformations(id, collectivityinformation));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/{id}/membershipFees")
    public ResponseEntity<?> getMembershipFees(@PathVariable String id) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(collectivityService.getCollectivityContribution(id));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @PostMapping("/{id}/membershipFees")
    public ResponseEntity<?> createMembershipFees(
            @PathVariable String id,
            @RequestBody List<CreateContribution> contributions) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(collectivityService.saveCollectivityContributions(id, contributions));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactions(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(collectivityService.getCollectivityTransactions(id, from, to));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/{id}/financialAccounts")
    public ResponseEntity<?> getFinancialAccounts(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate at) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(collectivityService.getCollectivityFinancialAccounts(id, at));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Bonus 1 — Activities (E) and Attendance (F)
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/activities")
    public ResponseEntity<?> getActivities(@PathVariable String id) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(activityService.getActivities(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<?> createActivities(
            @PathVariable String id,
            @RequestBody List<CreateCollectivityActivity> activities) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(activityService.saveActivities(id, activities));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/activities/{activityId}/attendance")
    public ResponseEntity<?> createAttendance(
            @PathVariable String id,
            @PathVariable String activityId,
            @RequestBody List<CreateActivityMemberAttendance> attendances) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(activityService.saveAttendance(id, activityId, attendances));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @GetMapping("/{id}/activities/{activityId}/attendance")
    public ResponseEntity<?> getAttendance(
            @PathVariable String id,
            @PathVariable String activityId) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(activityService.getAttendance(id, activityId));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}