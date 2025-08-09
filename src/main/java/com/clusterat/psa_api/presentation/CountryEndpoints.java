package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateCountryCommand;
import com.clusterat.psa_api.application.dto.CountryApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateCountryCommandHandler;
import com.clusterat.psa_api.application.interfaces.ICountryRepository;
import com.clusterat.psa_api.domain.entities.CountryEntity;
import com.clusterat.psa_api.presentation.dto.CountryPresentationDTO;
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
@RequestMapping("/api/v1/countries")
@Tag(name = "Country Management", description = "API endpoints for managing countries")
public class CountryEndpoints {

    private final ICountryRepository countryRepository;
    private final CreateCountryCommandHandler createCountryCommandHandler;
    private static final Logger log = LoggerFactory.getLogger(CountryEndpoints.class);

    public CountryEndpoints(ICountryRepository countryRepository, CreateCountryCommandHandler createCountryCommandHandler) {
        this.countryRepository = countryRepository;
        this.createCountryCommandHandler = createCountryCommandHandler;
    }

    @Operation(summary = "Get all countries", description = "Retrieve a list of all countries in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved countries",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CountryApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public CompletableFuture<ResponseEntity<List<CountryApplicationDTO.Response>>> getCountries() {
        MDC.put("operation", "getCountries");
        log.info("Starting to retrieve all countries");
        
        return countryRepository.GetAllAsync()
                .thenApply(countries -> {
                    List<CountryApplicationDTO.Response> response = countries.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(country -> new CountryApplicationDTO.Response(
                                    country.getId(),
                                    country.getName(),
                                    country.getShortName(),
                                    country.getIsoCode()))
                            .toList();
                    
                    log.info("Successfully retrieved {} countries", response.size());
                    MDC.clear();
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving countries", throwable);
                    MDC.clear();
                    return ResponseEntity.internalServerError().build();
                });
    }

    @Operation(summary = "Get country by ID", description = "Retrieve a specific country by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved country",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CountryApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Country not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> getCountryById(
            @Parameter(description = "Country ID", required = true) @PathVariable("id") int id) {
        MDC.put("operation", "getCountryById");
        MDC.put("countryId", String.valueOf(id));
        log.info("Starting to retrieve country by id: {}", id);
        
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> future = new CompletableFuture<>();
        
        countryRepository.GetByIdAsync(id)
                .thenApply(countryOpt -> {
                    try {
                        if (countryOpt.isPresent()) {
                            CountryEntity country = countryOpt.get();
                            CountryApplicationDTO.Response response = new CountryApplicationDTO.Response(
                                    country.getId(),
                                    country.getName(),
                                    country.getShortName(),
                                    country.getIsoCode());
                            
                            log.info("Successfully retrieved country: {}", country.getId());
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("Country not found with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving country by id: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving country by id: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @Operation(summary = "Get country by ISO code", description = "Retrieve a country by its ISO country code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved country",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CountryApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Country not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/iso/{isoCode}")
    public CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> getCountryByIsoCode(
            @Parameter(description = "ISO country code", required = true) @PathVariable("isoCode") String isoCode) {
        MDC.put("operation", "getCountryByIsoCode");
        MDC.put("isoCode", isoCode);
        log.info("Starting to retrieve country by ISO code: {}", isoCode);
        
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> future = new CompletableFuture<>();
        
        countryRepository.GetByIsoCodeAsync(isoCode)
                .thenApply(countryOpt -> {
                    try {
                        if (countryOpt.isPresent()) {
                            CountryEntity country = countryOpt.get();
                            CountryApplicationDTO.Response response = new CountryApplicationDTO.Response(
                                    country.getId(),
                                    country.getName(),
                                    country.getShortName(),
                                    country.getIsoCode());
                            
                            log.info("Successfully retrieved country by ISO code: {}", isoCode);
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("Country not found with ISO code: {}", isoCode);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving country by ISO code: {}", isoCode, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving country by ISO code: {}", isoCode, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @Operation(summary = "Create new country", description = "Create a new country in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created country",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CountryApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> createCountry(
            @Parameter(description = "Country creation data", required = true)
            @Valid @RequestBody CountryPresentationDTO.CreateRequest request) {
        MDC.put("operation", "createCountry");
        MDC.put("isoCode", request.isoCode());
        log.info("Starting to create country with ISO code: {}", request.isoCode());
        
        CreateCountryCommand command = new CreateCountryCommand(
                request.name(),
                request.shortName(),
                request.isoCode());
        
        return createCountryCommandHandler.handle(command)
                .thenApply(country -> {
                    CountryApplicationDTO.Response response = new CountryApplicationDTO.Response(
                            country.getId(),
                            country.getName(),
                            country.getShortName(),
                            country.getIsoCode());
                    
                    log.info("Successfully created country with id: {}", country.getId());
                    MDC.clear();
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error creating country with ISO code: {}", request.isoCode(), throwable);
                    MDC.clear();
                    return ResponseEntity.badRequest().build();
                });
    }

    @Operation(summary = "Update country", description = "Update an existing country's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated country",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CountryApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "Country not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> updateCountry(
            @Parameter(description = "Country ID", required = true) @PathVariable("id") int id,
            @Parameter(description = "Country update data", required = true)
            @Valid @RequestBody CountryPresentationDTO.UpdateRequest request) {
        MDC.put("operation", "updateCountry");
        MDC.put("countryId", String.valueOf(id));
        log.info("Starting to update country: {}", id);
        
        CompletableFuture<ResponseEntity<CountryApplicationDTO.Response>> future = new CompletableFuture<>();
        
        countryRepository.GetByIdAsync(id)
                .thenCompose(countryOpt -> {
                    try {
                        if (countryOpt.isPresent()) {
                            CountryEntity existingCountry = countryOpt.get();
                            existingCountry.setName(request.name());
                            existingCountry.setShortName(request.shortName());
                            existingCountry.setIsoCode(request.isoCode());
                            
                            return countryRepository.UpdateAsync(existingCountry)
                                    .thenApply(updatedCountry -> {
                                        try {
                                            CountryApplicationDTO.Response response = new CountryApplicationDTO.Response(
                                                    updatedCountry.getId(),
                                                    updatedCountry.getName(),
                                                    updatedCountry.getShortName(),
                                                    updatedCountry.getIsoCode());
                                            
                                            log.info("Successfully updated country: {}", id);
                                            MDC.clear();
                                            future.complete(ResponseEntity.ok(response));
                                        } catch (Exception e) {
                                            log.error("Error building response for updated country: {}", id, e);
                                            MDC.clear();
                                            future.complete(ResponseEntity.internalServerError().build());
                                        }
                                        return null;
                                    });
                        } else {
                            log.warn("Country not found for update with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                            return CompletableFuture.completedFuture(null);
                        }
                    } catch (Exception e) {
                        log.error("Error updating country: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error updating country: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @Operation(summary = "Delete country", description = "Delete a country from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted country"),
            @ApiResponse(responseCode = "404", description = "Country not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteCountry(
            @Parameter(description = "Country ID", required = true) @PathVariable("id") int id) {
        MDC.put("operation", "deleteCountry");
        MDC.put("countryId", String.valueOf(id));
        log.info("Starting to delete country: {}", id);
        
        return countryRepository.DeleteAsync(id)
                .thenApply(deletedCountry -> {
                    log.info("Successfully deleted country: {}", id);
                    MDC.clear();
                    return ResponseEntity.noContent().<Void>build();
                })
                .exceptionally(throwable -> {
                    log.error("Error deleting country: {}", id, throwable);
                    MDC.clear();
                    return ResponseEntity.notFound().build();
                });
    }
}