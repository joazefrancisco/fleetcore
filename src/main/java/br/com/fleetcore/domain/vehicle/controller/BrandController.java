package br.com.fleetcore.domain.vehicle.controller;

import br.com.fleetcore.domain.vehicle.dto.BrandDetails;
import br.com.fleetcore.domain.vehicle.dto.BrandResponse;
import br.com.fleetcore.domain.vehicle.dto.BrandSummary;
import br.com.fleetcore.domain.vehicle.dto.CreateBrandRequest;
import br.com.fleetcore.domain.vehicle.dto.UpdateBrandRequest;
import br.com.fleetcore.domain.vehicle.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ResponseEntity<BrandResponse> create(
            @RequestBody @Valid CreateBrandRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<BrandSummary>> findAll(
            @RequestParam(required = false) Boolean active,
            Pageable pageable) {

        return ResponseEntity.ok(brandService.findAll(active, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDetails> findById(
            @PathVariable Long id) {

        return ResponseEntity.ok(brandService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandDetails> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateBrandRequest request) {

        return ResponseEntity.ok(brandService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        brandService.updateStatus(id, active);

        return ResponseEntity.noContent().build();
    }
}