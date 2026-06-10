package com.dsm.controllers;

import com.dsm.dto.request.RequestDTO.LocationRequest;
import com.dsm.dto.response.ResponseDTO.LocationResponse;
import com.dsm.services.LocationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Location Management")
public class LocationController {
    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationResponse> createNewLocation(@Valid @RequestBody LocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.addLocation(request));
    }
    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getLocationById(@PathVariable String id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }
    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAllLocations(@RequestParam(required = false) String criteria) {
        if (criteria != null && !criteria.isBlank()) return ResponseEntity.ok(locationService.getLocationByName(criteria));
        return ResponseEntity.ok(locationService.getAllLocations());
    }
    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> updateLocation(@PathVariable String id, @Valid @RequestBody LocationRequest request) {
        return ResponseEntity.ok(locationService.updateLocation(id, request));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable String id) {
        locationService.deleteLocation(id);
        return ResponseEntity.noContent().build();
    }

}
