package org.agri.federation_agricole.controller;

import org.agri.federation_agricole.service.StatisticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Statistics endpoints.
 * Note: the OAS v0.0.5 spec deliberately uses "/collectivites" (missing 'i')
 * for these two routes — kept as-is to match the specification exactly.
 */
@RestController
@RequestMapping("/collectivities")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * GET /collectivites/{id}/statistics
     * Returns earned amount and potential unpaid amount per active member
     * of the given collectivity over the requested period.
     */
    @GetMapping("/{id}/statistics")
    public ResponseEntity<?> getLocalStatistics(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(statisticsService.getLocalStatistics(id, from, to));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * GET /collectivites/statistics
     * Returns for every collectivity:
     *  - number of new members in the period
     *  - overall percentage of members current on their dues
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getOverallStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(statisticsService.getOverallStatistics(from, to));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}