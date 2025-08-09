package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateCityCommand;
import com.clusterat.psa_api.application.dto.CityApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateCityCommandHandler;
import com.clusterat.psa_api.application.interfaces.ICityRepository;
import com.clusterat.psa_api.domain.entities.CityEntity;
import com.clusterat.psa_api.presentation.dto.CityPresentationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@RequestMapping("/api/v1/cities")
@Tag(name = "City Management", description = "API endpoints for managing cities and their geographic information")
public class CityEndpoints {

    private final ICityRepository cityRepository;
    private final CreateCityCommandHandler createCityCommandHandler;
    private static final Logger log = LoggerFactory.getLogger(CityEndpoints.class);

    public CityEndpoints(ICityRepository cityRepository, CreateCityCommandHandler createCityCommandHandler) {
        this.cityRepository = cityRepository;
        this.createCityCommandHandler = createCityCommandHandler;
    }

    @Operation(summary = "Get all cities", description = "Retrieve a list of all cities in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved cities",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CityApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public CompletableFuture<ResponseEntity<List<CityApplicationDTO.Response>>> getCities() {
        MDC.put("operation", "getCities");
        log.info("Starting to retrieve all cities");
        
        return cityRepository.GetAllAsync()
                .thenApply(cities -> {
                    List<CityApplicationDTO.Response> response = cities.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(city -> new CityApplicationDTO.Response(
                                    city.getId(),
                                    city.getName(),
                                    city.getShortName(),
                                    city.getIbgeCode(),
                                    city.getState().getId()))
                            .toList();
                    
                    log.info("Successfully retrieved {} cities", response.size());
                    MDC.clear();
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving cities", throwable);
                    MDC.clear();
                    return ResponseEntity.internalServerError().build();
                });
    }

    @Operation(summary = "Get city by ID", description = "Retrieve a specific city by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved city",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CityApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "City not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> getCityById(
            @Parameter(description = "City ID", required = true) @PathVariable("id") int id) {
        MDC.put("operation", "getCityById");
        MDC.put("cityId", String.valueOf(id));
        log.info("Starting to retrieve city by id: {}", id);
        
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> future = new CompletableFuture<>();
        
        cityRepository.GetByIdAsync(id)
                .thenApply(cityOpt -> {
                    try {
                        if (cityOpt.isPresent()) {
                            CityEntity city = cityOpt.get();
                            CityApplicationDTO.Response response = new CityApplicationDTO.Response(
                                    city.getId(),
                                    city.getName(),
                                    city.getShortName(),
                                    city.getIbgeCode(),
                                    city.getState().getId());
                            
                            log.info("Successfully retrieved city: {}", city.getId());
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("City not found with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving city by id: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving city by id: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @Operation(summary = "Get city by IBGE code", description = "Retrieve a city by its Brazilian Institute of Geography and Statistics (IBGE) code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved city",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CityApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "City not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/ibge/{ibgeCode}")
    public CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> getCityByIbgeCode(
            @Parameter(description = "IBGE code of the city", required = true) @PathVariable("ibgeCode") String ibgeCode) {
        MDC.put("operation", "getCityByIbgeCode");
        MDC.put("ibgeCode", ibgeCode);
        log.info("Starting to retrieve city by IBGE code: {}", ibgeCode);
        
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> future = new CompletableFuture<>();
        
        cityRepository.GetByIbgeCodeAsync(ibgeCode)
                .thenApply(cityOpt -> {
                    try {
                        if (cityOpt.isPresent()) {
                            CityEntity city = cityOpt.get();
                            CityApplicationDTO.Response response = new CityApplicationDTO.Response(
                                    city.getId(),
                                    city.getName(),
                                    city.getShortName(),
                                    city.getIbgeCode(),
                                    city.getState().getId());
                            
                            log.info("Successfully retrieved city by IBGE code: {}", ibgeCode);
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("City not found with IBGE code: {}", ibgeCode);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving city by IBGE code: {}", ibgeCode, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving city by IBGE code: {}", ibgeCode, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @Operation(summary = "Create new city", description = "Create a new city in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created city",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CityApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> createCity(
            @Parameter(description = "City creation data", required = true)
            @Valid @RequestBody CityPresentationDTO.CreateRequest request) {
        MDC.put("operation", "createCity");
        MDC.put("stateId", String.valueOf(request.stateId()));
        MDC.put("ibgeCode", request.ibgeCode());
        log.info("Starting to create city with IBGE code: {} for state: {}", request.ibgeCode(), request.stateId());
        
        CreateCityCommand command = new CreateCityCommand(
                request.name(),
                request.shortName(),
                request.ibgeCode(),
                request.stateId());
        
        return createCityCommandHandler.handle(command)
                .thenApply(city -> {
                    CityApplicationDTO.Response response = new CityApplicationDTO.Response(
                            city.getId(),
                            city.getName(),
                            city.getShortName(),
                            city.getIbgeCode(),
                            city.getState().getId());
                    
                    log.info("Successfully created city with id: {}", city.getId());
                    MDC.clear();
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error creating city with IBGE code: {} for state: {}", request.ibgeCode(), request.stateId(), throwable);
                    MDC.clear();
                    return ResponseEntity.badRequest().build();
                });
    }

    @Operation(summary = "Update city", description = "Update an existing city's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated city",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CityApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "City not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> updateCity(
            @Parameter(description = "City ID", required = true) @PathVariable("id") int id,
            @Parameter(description = "City update data", required = true)
            @Valid @RequestBody CityPresentationDTO.UpdateRequest request) {
        MDC.put("operation", "updateCity");
        MDC.put("cityId", String.valueOf(id));
        MDC.put("stateId", String.valueOf(request.stateId()));
        log.info("Starting to update city: {}", id);
        
        CompletableFuture<ResponseEntity<CityApplicationDTO.Response>> future = new CompletableFuture<>();
        
        cityRepository.GetByIdAsync(id)
                .thenCompose(cityOpt -> {
                    try {
                        if (cityOpt.isPresent()) {
                            CityEntity existingCity = cityOpt.get();
                            existingCity.setName(request.name());
                            existingCity.setShortName(request.shortName());
                            existingCity.setIbgeCode(request.ibgeCode());
                            
                            return cityRepository.UpdateAsync(existingCity)
                                    .thenApply(updatedCity -> {
                                        try {
                                            CityApplicationDTO.Response response = new CityApplicationDTO.Response(
                                                    updatedCity.getId(),
                                                    updatedCity.getName(),
                                                    updatedCity.getShortName(),
                                                    updatedCity.getIbgeCode(),
                                                    updatedCity.getState().getId());
                                            
                                            log.info("Successfully updated city: {}", id);
                                            MDC.clear();
                                            future.complete(ResponseEntity.ok(response));
                                        } catch (Exception e) {
                                            log.error("Error building response for updated city: {}", id, e);
                                            MDC.clear();
                                            future.complete(ResponseEntity.internalServerError().build());
                                        }
                                        return null;
                                    });
                        } else {
                            log.warn("City not found for update with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                            return CompletableFuture.completedFuture(null);
                        }
                    } catch (Exception e) {
                        log.error("Error updating city: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error updating city: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @Operation(summary = "Delete city", description = "Delete a city from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted city"),
            @ApiResponse(responseCode = "404", description = "City not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteCity(
            @Parameter(description = "City ID", required = true) @PathVariable("id") int id) {
        MDC.put("operation", "deleteCity");
        MDC.put("cityId", String.valueOf(id));
        log.info("Starting to delete city: {}", id);
        
        return cityRepository.DeleteAsync(id)
                .thenApply(deletedCity -> {
                    log.info("Successfully deleted city: {}", id);
                    MDC.clear();
                    return ResponseEntity.noContent().<Void>build();
                })
                .exceptionally(throwable -> {
                    log.error("Error deleting city: {}", id, throwable);
                    MDC.clear();
                    return ResponseEntity.notFound().build();
                });
    }
}