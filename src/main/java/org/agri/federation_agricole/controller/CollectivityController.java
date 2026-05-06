package org.agri.federation_agricole.controller;

import org.agri.federation_agricole.entity.Collectivityinformation;
import org.agri.federation_agricole.entity.CreateCollectivity;
import org.agri.federation_agricole.entity.CreateContribution;
import org.agri.federation_agricole.service.CollectivityService;
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

    public CollectivityController(CollectivityService collectivityService) {
        this.collectivityService = collectivityService;
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
}