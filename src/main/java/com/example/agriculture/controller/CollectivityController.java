package com.example.agriculture.controller;

import com.example.agriculture.entity.*;
import com.example.agriculture.exception.BadRequestException;
import com.example.agriculture.exception.ConflictException;
import com.example.agriculture.exception.NotFoundException;
import com.example.agriculture.service.CollectivityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/collectivities")
public class CollectivityController {

    private final CollectivityService collectivityService;

    public CollectivityController(CollectivityService collectivityService) {
        this.collectivityService = collectivityService;
    }

    @PostMapping
    public ResponseEntity<?> createCollectivises(
            @RequestBody List<CreateCollectivity> collectivities) throws SQLException {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(collectivityService.createCollectivities(collectivities));
        }catch (BadRequestException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/informations")
    public ResponseEntity<?> assignIdentity(
            @PathVariable int id,
            @RequestBody AssignCollectivityIdentity payload) {
        try {
            return ResponseEntity.ok(collectivityService.assignIdentity(id, payload));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/membershipFees")
    public ResponseEntity<?> getMembershipFees(@PathVariable String id) {
        try {
            return ResponseEntity.ok(collectivityService.getMembershipFee(id));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/membershipFees")
    public ResponseEntity<?> createMembershipFees(
            @PathVariable String id,
            @RequestBody List<CreateMembershipFee> fees) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(collectivityService.createMembershipFee(id, fees));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getTransactions(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        try {
            return ResponseEntity.ok(collectivityService.getTransactions(id, from, to));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/financialAccounts")
    public ResponseEntity<?> getFinancialAccounts(
            @PathVariable String id,
            @RequestParam @DateTimeFormat( iso = DateTimeFormat.ISO.DATE) LocalDate at
    ) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(collectivityService.getFinancialAccounts(id, at));
        } catch (Exception e) {
            throw new RuntimeException("Not implemented yet");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectivityById(@PathVariable String id) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(collectivityService.getById(id));
        }catch (Exception e){
            throw new RuntimeException("Not implemented yet");
        }
    }
}
