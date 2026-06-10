package com.dsm.services;

import com.dsm.dto.request.RequestDTO.LocationRequest;
import com.dsm.dto.response.ResponseDTO.LocationResponse;
import com.dsm.entities.Location;
import com.dsm.entities.Stock;
import com.dsm.exception.DuplicateResourceException;
import com.dsm.exception.ResourceNotFoundException;
import com.dsm.repositories.LocationRepository;
import com.dsm.repositories.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationService {
    private final LocationRepository locationRepository;
    private final StockRepository stockRepository;

    public LocationResponse addLocation(LocationRequest request) {
        String code = clean(request.getCode());

        if (locationRepository.getLocationByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Location With This Name Already Exists.");
        }
        if (code != null && locationRepository.getLocationByCode(code).isPresent()) {
            throw new DuplicateResourceException("Location With This Code Already Exists.");
        }

        Location location = Location.builder()
                .name(request.getName())
                .code(code)
                .description(clean(request.getDescription()))
                .build();
        return toResponse(locationRepository.save(location));
    }
    @Transactional(readOnly = true)
    public LocationResponse getLocationById(String id) {
        return toResponse(findById(id));
    }
    @Transactional(readOnly = true)
    public List<LocationResponse> getAllLocations() {
        return locationRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<LocationResponse> getLocationByName(String name) {
        return locationRepository.getLocationsByName(name).stream().map(this::toResponse).collect(Collectors.toList());
    }
    public LocationResponse updateLocation(String id, LocationRequest request) {
        Location location = findById(id);
        String code = clean(request.getCode());

        if (!location.getName().equals(request.getName()) && locationRepository.getLocationByName(request.getName()).isPresent()) {
            throw new DuplicateResourceException("Location With This Name Already Exists.");
        }
        if (code != null && (location.getCode() == null || !location.getCode().equals(code)) && locationRepository.getLocationByCode(code).isPresent()) {
            throw new DuplicateResourceException("Location With This Code Already Exists.");
        }

        location.setName(request.getName());
        location.setCode(code);
        location.setDescription(clean(request.getDescription()));
        Location saved = locationRepository.save(location);

        for (Stock stock : stockRepository.getStocksByLocationId(saved.getId())) {
            stock.setLocation(saved.getName());
            stockRepository.save(stock);
        }

        return toResponse(saved);
    }
    public void deleteLocation(String id) {
        Location location = findById(id);

        for (Stock stock : stockRepository.getStocksByLocationId(location.getId())) {
            stock.setLocationId(null);
            stock.setLocation(null);
            stockRepository.save(stock);
        }

        locationRepository.deleteById(id);
    }
    public Location findById(String id) {
        return locationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Location With ID " + id + " Was Not Found."));
    }
    private LocationResponse toResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .name(location.getName())
                .code(location.getCode())
                .description(location.getDescription())
                .stockCount(stockRepository.getStocksByLocationId(location.getId()).size())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .build();
    }
    private String clean(String value) {
        return value != null && !value.isBlank() ? value.trim() : null;
    }

}
