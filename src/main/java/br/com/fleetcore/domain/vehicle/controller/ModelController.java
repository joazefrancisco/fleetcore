package br.com.fleetcore.domain.vehicle.controller;

import br.com.fleetcore.domain.vehicle.dto.ModelDetails;
import br.com.fleetcore.domain.vehicle.dto.ModelResponse;
import br.com.fleetcore.domain.vehicle.dto.ModelSummary;
import br.com.fleetcore.domain.vehicle.dto.CreateModelRequest;
import br.com.fleetcore.domain.vehicle.dto.UpdateModelRequest;
import br.com.fleetcore.domain.vehicle.enums.ModelStatusFilter;
import br.com.fleetcore.domain.vehicle.service.ModelService;
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
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/models")
@RequiredArgsConstructor
@Tag(name = "Models", description = "Vehicle model management")
public class ModelController {

    private final ModelService modelService;

    @PostMapping
    @Operation(summary = "Create new model", description = "Creates a new vehicle model with the provided name and brand")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Model created successfully",
                    content = @Content(schema = @Schema(implementation = ModelResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Brand not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Model already exists with the same name or brand is inactive",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ModelResponse> create(
            @RequestBody @Valid CreateModelRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(modelService.create(request));
    }

    @GetMapping
    @Operation(summary = "List models", description = """
            Returns a paginated list of vehicle models.
            The status filter accepts ACTIVE, INACTIVE, or ALL.
            When omitted, the service defaults to ACTIVE.
            The brandId filter allows filtering by a specific brand.
            Pagination supports page, size, and sort parameters.
            Default sort is name,asc.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Model list returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status or brandId parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<ModelSummary>> findAll(
            @Parameter(description = "Status filter: ACTIVE, INACTIVE, or ALL. Defaults to ACTIVE when not provided.",
                    example = "ACTIVE"
            )
            @RequestParam(required = false) ModelStatusFilter status,
            @Parameter(description = "Brand ID to filter models", example = "1")
            @RequestParam(required = false) Long brandId,
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(modelService.findAll(status, brandId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get model by ID", description = "Returns complete details of a specific model, including inactive models")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Model found",
                    content = @Content(schema = @Schema(implementation = ModelDetails.class))),
            @ApiResponse(responseCode = "404", description = "Model not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ModelDetails> findById(
            @Parameter(description = "Model ID", example = "1", required = true)
            @PathVariable Long id) {

        return ResponseEntity.ok(modelService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update model", description = "Updates the name of an existing active model. Brand cannot be changed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Model updated successfully",
                    content = @Content(schema = @Schema(implementation = ModelDetails.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Model not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: model with same name already exists, model is inactive, or concurrent update",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ModelDetails> update(
            @Parameter(description = "Model ID", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody @Valid UpdateModelRequest request) {

        return ResponseEntity.ok(modelService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update model status", description = "Activates or deactivates an existing model")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid parameter value",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Model not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: concurrent update",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> updateStatus(
            @Parameter(description = "Model ID", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "New model status (true = active, false = inactive)", example = "true", required = true)
            @RequestParam boolean active) {

        modelService.updateStatus(id, active);

        return ResponseEntity.noContent().build();
    }
}
