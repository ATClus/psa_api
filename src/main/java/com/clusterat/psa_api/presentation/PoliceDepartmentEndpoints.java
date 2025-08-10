package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreatePoliceDepartmentCommand;
import com.clusterat.psa_api.application.dto.PoliceDepartmentApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreatePoliceDepartmentCommandHandler;
import com.clusterat.psa_api.application.interfaces.IPoliceDepartmentRepository;
import com.clusterat.psa_api.domain.entities.PoliceDepartmentEntity;
import com.clusterat.psa_api.presentation.dto.PoliceDepartmentPresentationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/police-departments")
@Tag(name = "Police Department Management", description = "API endpoints for managing police departments and law enforcement facilities")
public class PoliceDepartmentEndpoints {

    private final IPoliceDepartmentRepository policeDepartmentRepository;
    private final CreatePoliceDepartmentCommandHandler createPoliceDepartmentCommandHandler;
    private static final Logger log = LoggerFactory.getLogger(PoliceDepartmentEndpoints.class);

    public PoliceDepartmentEndpoints(IPoliceDepartmentRepository policeDepartmentRepository, CreatePoliceDepartmentCommandHandler createPoliceDepartmentCommandHandler) {
        this.policeDepartmentRepository = policeDepartmentRepository;
        this.createPoliceDepartmentCommandHandler = createPoliceDepartmentCommandHandler;
    }

    @Operation(summary = "Get all police departments", description = "Retrieve a list of all police departments in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved police departments",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PoliceDepartmentApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public CompletableFuture<ResponseEntity<List<PoliceDepartmentApplicationDTO.Response>>> getPoliceDepartments() {
        MDC.put("operation", "getPoliceDepartments");
        log.info("Starting to retrieve all police departments");
        
        return policeDepartmentRepository.GetAllAsync()
                .thenApply(policeDepartments -> {
                    List<PoliceDepartmentApplicationDTO.Response> response = policeDepartments.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(policeDepartment -> new PoliceDepartmentApplicationDTO.Response(
                                    policeDepartment.getId(),
                                    policeDepartment.getOverpassId(),
                                    policeDepartment.getName(),
                                    policeDepartment.getShortName(),
                                    policeDepartment.getOperator(),
                                    policeDepartment.getOwnership(),
                                    policeDepartment.getPhone(),
                                    policeDepartment.getEmail(),
                                    policeDepartment.getLatitude(),
                                    policeDepartment.getLongitude(),
                                    policeDepartment.getAddress().getId()))
                            .toList();
                    
                    log.info("Successfully retrieved {} police departments", response.size());
                    MDC.clear();
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving police departments", throwable);
                    MDC.clear();
                    return ResponseEntity.internalServerError().build();
                });
    }

    @Operation(summary = "Get police department by ID", description = "Retrieve a specific police department by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved police department",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PoliceDepartmentApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Police department not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> getPoliceDepartmentById(
            @Parameter(description = "Police Department ID", required = true) @PathVariable("id") int id) {
        MDC.put("operation", "getPoliceDepartmentById");
        MDC.put("policeDepartmentId", String.valueOf(id));
        log.info("Starting to retrieve police department by id: {}", id);
        
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> future = new CompletableFuture<>();
        
        policeDepartmentRepository.GetByIdAsync(id)
                .thenApply(policeDepartmentOpt -> {
                    try {
                        if (policeDepartmentOpt.isPresent()) {
                            PoliceDepartmentEntity policeDepartment = policeDepartmentOpt.get();
                            PoliceDepartmentApplicationDTO.Response response = new PoliceDepartmentApplicationDTO.Response(
                                    policeDepartment.getId(),
                                    policeDepartment.getOverpassId(),
                                    policeDepartment.getName(),
                                    policeDepartment.getShortName(),
                                    policeDepartment.getOperator(),
                                    policeDepartment.getOwnership(),
                                    policeDepartment.getPhone(),
                                    policeDepartment.getEmail(),
                                    policeDepartment.getLatitude(),
                                    policeDepartment.getLongitude(),
                                    policeDepartment.getAddress().getId());
                            
                            log.info("Successfully retrieved police department: {}", policeDepartment.getId());
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("Police department not found with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving police department by id: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving police department by id: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @Operation(summary = "Get police department by Overpass ID", description = "Retrieve a police department by its OpenStreetMap Overpass API identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved police department",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PoliceDepartmentApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Police department not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/overpass/{overpassId}")
    public CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> getPoliceDepartmentByOverpassId(
            @Parameter(description = "OpenStreetMap Overpass ID of the police department", required = true) @PathVariable("overpassId") String overpassId) {
        MDC.put("operation", "getPoliceDepartmentByOverpassId");
        MDC.put("overpassId", overpassId);
        log.info("Starting to retrieve police department by overpass id: {}", overpassId);
        
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> future = new CompletableFuture<>();
        
        policeDepartmentRepository.GetByOverpassIdAsync(overpassId)
                .thenApply(policeDepartmentOpt -> {
                    try {
                        if (policeDepartmentOpt.isPresent()) {
                            PoliceDepartmentEntity policeDepartment = policeDepartmentOpt.get();
                            PoliceDepartmentApplicationDTO.Response response = new PoliceDepartmentApplicationDTO.Response(
                                    policeDepartment.getId(),
                                    policeDepartment.getOverpassId(),
                                    policeDepartment.getName(),
                                    policeDepartment.getShortName(),
                                    policeDepartment.getOperator(),
                                    policeDepartment.getOwnership(),
                                    policeDepartment.getPhone(),
                                    policeDepartment.getEmail(),
                                    policeDepartment.getLatitude(),
                                    policeDepartment.getLongitude(),
                                    policeDepartment.getAddress().getId());
                            
                            log.info("Successfully retrieved police department by overpass id: {}", overpassId);
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("Police department not found with overpass id: {}", overpassId);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving police department by overpass id: {}", overpassId, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving police department by overpass id: {}", overpassId, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @Operation(summary = "Create new police department", description = "Create a new police department in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created police department",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PoliceDepartmentApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> createPoliceDepartment(
            @RequestBody(
                description = "Police department creation request payload with all required fields",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PoliceDepartmentPresentationDTO.CreateRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "createPoliceDepartmentExample",
                            summary = "Create Police Department Request",
                            description = "Example request to create a new police department",
                            value = "{\n  \"overpassId\": \"way/123456789\",\n  \"name\": \"1º Distrito Policial\",\n  \"shortName\": \"1º DP\",\n  \"operator\": \"Polícia Civil\",\n  \"ownership\": \"public\",\n  \"phone\": \"+5511999999999\",\n  \"email\": \"contato@policia.sp.gov.br\",\n  \"latitude\": \"-23.550520\",\n  \"longitude\": \"-46.633309\",\n  \"addressId\": 1\n}"
                        )
                    }
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PoliceDepartmentPresentationDTO.CreateRequest request) {
        MDC.put("operation", "createPoliceDepartment");
        MDC.put("addressId", String.valueOf(request.addressId()));
        MDC.put("overpassId", request.overpassId());
        log.info("Starting to create police department with overpass id: {} for address: {}", request.overpassId(), request.addressId());
        
        CreatePoliceDepartmentCommand command = new CreatePoliceDepartmentCommand(
                request.overpassId(),
                request.name(),
                request.shortName(),
                request.operator(),
                request.ownership(),
                request.phone(),
                request.email(),
                request.latitude(),
                request.longitude(),
                request.addressId());
        
        return createPoliceDepartmentCommandHandler.handle(command)
                .thenApply(policeDepartment -> {
                    PoliceDepartmentApplicationDTO.Response response = new PoliceDepartmentApplicationDTO.Response(
                            policeDepartment.getId(),
                            policeDepartment.getOverpassId(),
                            policeDepartment.getName(),
                            policeDepartment.getShortName(),
                            policeDepartment.getOperator(),
                            policeDepartment.getOwnership(),
                            policeDepartment.getPhone(),
                            policeDepartment.getEmail(),
                            policeDepartment.getLatitude(),
                            policeDepartment.getLongitude(),
                            policeDepartment.getAddress().getId());
                    
                    log.info("Successfully created police department with id: {}", policeDepartment.getId());
                    MDC.clear();
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error creating police department with overpass id: {} for address: {}", request.overpassId(), request.addressId(), throwable);
                    MDC.clear();
                    return ResponseEntity.badRequest().build();
                });
    }

    @Operation(summary = "Update police department", description = "Update an existing police department's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated police department",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = PoliceDepartmentApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Police department not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> updatePoliceDepartment(
            @Parameter(
                description = "Unique identifier of the police department to update",
                required = true,
                example = "1",
                schema = @Schema(type = "integer", minimum = "1")
            ) @PathVariable("id") int id,
            @RequestBody(
                description = "Police department update request payload with all fields",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PoliceDepartmentPresentationDTO.UpdateRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "updatePoliceDepartmentExample",
                            summary = "Update Police Department Request",
                            description = "Example request to update an existing police department",
                            value = "{\n  \"overpassId\": \"way/987654321\",\n  \"name\": \"2º Distrito Policial\",\n  \"shortName\": \"2º DP\",\n  \"operator\": \"Polícia Civil\",\n  \"ownership\": \"public\",\n  \"phone\": \"+5511888888888\",\n  \"email\": \"contato2@policia.sp.gov.br\",\n  \"latitude\": \"-23.561414\",\n  \"longitude\": \"-46.656271\",\n  \"addressId\": 2\n}"
                        )
                    }
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody PoliceDepartmentPresentationDTO.UpdateRequest request) {
        MDC.put("operation", "updatePoliceDepartment");
        MDC.put("policeDepartmentId", String.valueOf(id));
        MDC.put("addressId", String.valueOf(request.addressId()));
        log.info("Starting to update police department: {}", id);
        
        CompletableFuture<ResponseEntity<PoliceDepartmentApplicationDTO.Response>> future = new CompletableFuture<>();
        
        policeDepartmentRepository.GetByIdAsync(id)
                .thenCompose(policeDepartmentOpt -> {
                    try {
                        if (policeDepartmentOpt.isPresent()) {
                            PoliceDepartmentEntity existingPoliceDepartment = policeDepartmentOpt.get();
                            existingPoliceDepartment.setOverpassId(request.overpassId());
                            existingPoliceDepartment.setName(request.name());
                            existingPoliceDepartment.setShortName(request.shortName());
                            existingPoliceDepartment.setOperator(request.operator());
                            existingPoliceDepartment.setOwnership(request.ownership());
                            existingPoliceDepartment.setPhone(request.phone());
                            existingPoliceDepartment.setEmail(request.email());
                            existingPoliceDepartment.setLatitude(request.latitude());
                            existingPoliceDepartment.setLongitude(request.longitude());
                            
                            return policeDepartmentRepository.UpdateAsync(existingPoliceDepartment)
                                    .thenApply(updatedPoliceDepartment -> {
                                        try {
                                            PoliceDepartmentApplicationDTO.Response response = new PoliceDepartmentApplicationDTO.Response(
                                                    updatedPoliceDepartment.getId(),
                                                    updatedPoliceDepartment.getOverpassId(),
                                                    updatedPoliceDepartment.getName(),
                                                    updatedPoliceDepartment.getShortName(),
                                                    updatedPoliceDepartment.getOperator(),
                                                    updatedPoliceDepartment.getOwnership(),
                                                    updatedPoliceDepartment.getPhone(),
                                                    updatedPoliceDepartment.getEmail(),
                                                    updatedPoliceDepartment.getLatitude(),
                                                    updatedPoliceDepartment.getLongitude(),
                                                    updatedPoliceDepartment.getAddress().getId());
                                            
                                            log.info("Successfully updated police department: {}", id);
                                            MDC.clear();
                                            future.complete(ResponseEntity.ok(response));
                                        } catch (Exception e) {
                                            log.error("Error building response for updated police department: {}", id, e);
                                            MDC.clear();
                                            future.complete(ResponseEntity.internalServerError().build());
                                        }
                                        return null;
                                    });
                        } else {
                            log.warn("Police department not found for update with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                            return CompletableFuture.completedFuture(null);
                        }
                    } catch (Exception e) {
                        log.error("Error updating police department: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error updating police department: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @Operation(summary = "Delete police department", description = "Delete a police department from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted police department"),
            @ApiResponse(responseCode = "404", description = "Police department not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deletePoliceDepartment(
            @Parameter(description = "Police Department ID", required = true) @PathVariable("id") int id) {
        MDC.put("operation", "deletePoliceDepartment");
        MDC.put("policeDepartmentId", String.valueOf(id));
        log.info("Starting to delete police department: {}", id);
        
        return policeDepartmentRepository.DeleteAsync(id)
                .thenApply(deletedPoliceDepartment -> {
                    log.info("Successfully deleted police department: {}", id);
                    MDC.clear();
                    return ResponseEntity.noContent().<Void>build();
                })
                .exceptionally(throwable -> {
                    log.error("Error deleting police department: {}", id, throwable);
                    MDC.clear();
                    return ResponseEntity.notFound().build();
                });
    }
}