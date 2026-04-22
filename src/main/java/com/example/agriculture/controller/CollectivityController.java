package com.example.agriculture.controller;

import com.example.agriculture.entity.Collectivity;
import com.example.agriculture.entity.CreateCollectivity;
import com.example.agriculture.service.CollectivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/collectivities")
public class CollectivityController {

    private final CollectivityService collectivityService;

    public CollectivityController(CollectivityService collectivityService) {
        this.collectivityService = collectivityService;
    }

    @PostMapping
    public ResponseEntity<List<Collectivity>> createCollectivities(
            @RequestBody List<CreateCollectivity> collectivities) throws SQLException {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(collectivityService.createCollectivities(collectivities));
    }
}
