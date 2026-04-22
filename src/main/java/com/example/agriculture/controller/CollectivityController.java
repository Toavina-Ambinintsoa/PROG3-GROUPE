package com.example.agriculture.controller;

import com.example.agriculture.entity.*;
import com.example.agriculture.exception.BadRequestException;
import com.example.agriculture.exception.NotFoundException;
import com.example.agriculture.service.CollectivityService;
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

    /// TODO: adding try catch to control errors

    @PatchMapping("/{id}/identity")
    @ResponseStatus(HttpStatus.OK)
    public Collectivity assignIdentity(
            @PathVariable int id,
            @RequestBody AssignCollectivityIdentity payload) {
        return collectivityService.assignIdentity(id, payload);
    }

    @GetMapping("/{id}/membershipFees")
    public ResponseEntity<MembershipFee> getMembershipFee(
            @PathVariable String id
    ){
        try {
            return ResponseEntity
                    .status(200)
                    .body(collectivityService.getMembershipFee(id));
        }catch (Exception e){
            throw new BadRequestException("Not implemented yet");
        }

    }

    @PostMapping("/{id}/membershipFees")
    public ResponseEntity<MembershipFee> createMembershipFee(
            @PathVariable String id,
            @RequestBody List<CreateMembershipFee> payload
    ){
        try {
            return ResponseEntity
                    .status(201)
                    .body(collectivityService.createMembershipFee(id, payload));
        }catch (BadRequestException e){
            throw new RuntimeException("Not implemented yet");
        }
    }

    @GetMapping("/{id}/transaction")
    public ResponseEntity<?> getTransactions(
            @PathVariable String id,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
            )
    {
        try {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(collectivityService.getTransactions(id, from, to));
        }catch (Exception e){
            throw new RuntimeException("Not implemented yet");
        }
    }
}
