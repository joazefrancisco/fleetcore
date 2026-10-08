package br.com.fleetcore.domain.vehicle.controller;

import br.com.fleetcore.domain.vehicle.dto.BrandDetails;
import br.com.fleetcore.domain.vehicle.dto.BrandResponse;
import br.com.fleetcore.domain.vehicle.dto.BrandSummary;
import br.com.fleetcore.domain.vehicle.dto.CreateBrandRequest;
import br.com.fleetcore.domain.vehicle.dto.UpdateBrandRequest;
import br.com.fleetcore.domain.vehicle.enums.BrandStatusFilter;
import br.com.fleetcore.domain.vehicle.service.BrandService;
import br.com.fleetcore.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Brands", description = "Vehicle brand management")
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    @Operation(summary = "Create new brand", description = "Creates a new vehicle brand with the provided name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Brand created successfully",
                    content = @Content(schema = @Schema(implementation = BrandResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Brand already exists with the same name",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BrandResponse> create(
            @RequestBody @Valid CreateBrandRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.create(request));
    }

    @GetMapping
    @Operation(summary = "List brands", description = "Lists all brands with pagination and status filter support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand list returned successfully",
                    content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<BrandSummary>> findAll(
            @Parameter(description = "Status filter (ACTIVE, INACTIVE, ALL). If not provided, returns all brands.")
            @RequestParam(required = false) BrandStatusFilter status,
            Pageable pageable) {

        return ResponseEntity.ok(brandService.findAll(status, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID", description = "Returns complete details of a specific brand")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand found",
                    content = @Content(schema = @Schema(implementation = BrandDetails.class))),
            @ApiResponse(responseCode = "404", description = "Brand not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BrandDetails> findById(
            @Parameter(description = "Brand ID", example = "1", required = true)
            @PathVariable Long id) {

        return ResponseEntity.ok(brandService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update brand", description = "Updates the name of an existing brand")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Brand updated successfully",
                    content = @Content(schema = @Schema(implementation = BrandDetails.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Brand not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: brand with same name already exists or concurrent update",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BrandDetails> update(
            @Parameter(description = "Brand ID", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody @Valid UpdateBrandRequest request) {

        return ResponseEntity.ok(brandService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update brand status", description = "Activates or deactivates an existing brand")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Brand not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: concurrent update",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> updateStatus(
            @Parameter(description = "Brand ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "New brand status (true = active, false = inactive)", example = "true", required = true)
            @RequestParam boolean active) {

        brandService.updateStatus(id, active);

        return ResponseEntity.noContent().build();
    }
}