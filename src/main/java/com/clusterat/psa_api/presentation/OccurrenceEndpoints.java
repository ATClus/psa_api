package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateOccurrenceCommand;
import com.clusterat.psa_api.application.dto.OccurrenceApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateOccurrenceCommandHandler;
import com.clusterat.psa_api.application.interfaces.IOccurrenceRepository;
import com.clusterat.psa_api.domain.entities.OccurrenceEntity;
import com.clusterat.psa_api.presentation.dto.OccurrencePresentationDTO;
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
@RequestMapping("/api/v1/occurrences")
public class OccurrenceEndpoints {

    private final IOccurrenceRepository occurrenceRepository;
    private final CreateOccurrenceCommandHandler createOccurrenceCommandHandler;
    private static final Logger log = LoggerFactory.getLogger(OccurrenceEndpoints.class);

    public OccurrenceEndpoints(IOccurrenceRepository occurrenceRepository, CreateOccurrenceCommandHandler createOccurrenceCommandHandler) {
        this.occurrenceRepository = occurrenceRepository;
        this.createOccurrenceCommandHandler = createOccurrenceCommandHandler;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<OccurrenceApplicationDTO.Response>>> getOccurrences() {
        MDC.put("operation", "getOccurrences");
        log.info("Starting to retrieve all occurrences");
        
        return occurrenceRepository.GetAllAsync()
                .thenApply(occurrences -> {
                    List<OccurrenceApplicationDTO.Response> response = occurrences.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(occurrence -> new OccurrenceApplicationDTO.Response(
                                    occurrence.getId(),
                                    occurrence.getName(),
                                    occurrence.getDescription(),
                                    occurrence.getDateStart(),
                                    occurrence.getDateEnd(),
                                    occurrence.getDateUpdate(),
                                    occurrence.isActive(),
                                    occurrence.getIntensity(),
                                    occurrence.getAddress().getId(),
                                    occurrence.getUser().getId()))
                            .toList();
                    
                    log.info("Successfully retrieved {} occurrences", response.size());
                    MDC.clear();
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving occurrences", throwable);
                    MDC.clear();
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> getOccurrenceById(@PathVariable("id") int id) {
        MDC.put("operation", "getOccurrenceById");
        MDC.put("occurrenceId", String.valueOf(id));
        log.info("Starting to retrieve occurrence by id: {}", id);
        
        CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> future = new CompletableFuture<>();
        
        occurrenceRepository.GetByIdAsync(id)
                .thenApply(occurrenceOpt -> {
                    try {
                        if (occurrenceOpt.isPresent()) {
                            OccurrenceEntity occurrence = occurrenceOpt.get();
                            OccurrenceApplicationDTO.Response response = new OccurrenceApplicationDTO.Response(
                                    occurrence.getId(),
                                    occurrence.getName(),
                                    occurrence.getDescription(),
                                    occurrence.getDateStart(),
                                    occurrence.getDateEnd(),
                                    occurrence.getDateUpdate(),
                                    occurrence.isActive(),
                                    occurrence.getIntensity(),
                                    occurrence.getAddress().getId(),
                                    occurrence.getUser().getId());
                            
                            log.info("Successfully retrieved occurrence: {}", occurrence.getId());
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("Occurrence not found with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving occurrence by id: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving occurrence by id: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @GetMapping("/active")
    public CompletableFuture<ResponseEntity<List<OccurrenceApplicationDTO.Response>>> getActiveOccurrences() {
        MDC.put("operation", "getActiveOccurrences");
        log.info("Starting to retrieve active occurrences");
        
        return occurrenceRepository.GetByActiveAsync(true)
                .thenApply(occurrences -> {
                    List<OccurrenceApplicationDTO.Response> response = occurrences.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(occurrence -> new OccurrenceApplicationDTO.Response(
                                    occurrence.getId(),
                                    occurrence.getName(),
                                    occurrence.getDescription(),
                                    occurrence.getDateStart(),
                                    occurrence.getDateEnd(),
                                    occurrence.getDateUpdate(),
                                    occurrence.isActive(),
                                    occurrence.getIntensity(),
                                    occurrence.getAddress().getId(),
                                    occurrence.getUser().getId()))
                            .toList();
                    
                    log.info("Successfully retrieved {} active occurrences", response.size());
                    MDC.clear();
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving active occurrences", throwable);
                    MDC.clear();
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/inactive")
    public CompletableFuture<ResponseEntity<List<OccurrenceApplicationDTO.Response>>> getInactiveOccurrences() {
        MDC.put("operation", "getInactiveOccurrences");
        log.info("Starting to retrieve inactive occurrences");
        
        return occurrenceRepository.GetByActiveAsync(false)
                .thenApply(occurrences -> {
                    List<OccurrenceApplicationDTO.Response> response = occurrences.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(occurrence -> new OccurrenceApplicationDTO.Response(
                                    occurrence.getId(),
                                    occurrence.getName(),
                                    occurrence.getDescription(),
                                    occurrence.getDateStart(),
                                    occurrence.getDateEnd(),
                                    occurrence.getDateUpdate(),
                                    occurrence.isActive(),
                                    occurrence.getIntensity(),
                                    occurrence.getAddress().getId(),
                                    occurrence.getUser().getId()))
                            .toList();
                    
                    log.info("Successfully retrieved {} inactive occurrences", response.size());
                    MDC.clear();
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving inactive occurrences", throwable);
                    MDC.clear();
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/user/{userId}")
    public CompletableFuture<ResponseEntity<List<OccurrenceApplicationDTO.Response>>> getOccurrencesByUserId(@PathVariable("userId") int userId) {
        MDC.put("operation", "getOccurrencesByUserId");
        MDC.put("userId", String.valueOf(userId));
        log.info("Starting to retrieve occurrences for user: {}", userId);
        
        return occurrenceRepository.GetByUserIdAsync(userId)
                .thenApply(occurrences -> {
                    List<OccurrenceApplicationDTO.Response> response = occurrences.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(occurrence -> new OccurrenceApplicationDTO.Response(
                                    occurrence.getId(),
                                    occurrence.getName(),
                                    occurrence.getDescription(),
                                    occurrence.getDateStart(),
                                    occurrence.getDateEnd(),
                                    occurrence.getDateUpdate(),
                                    occurrence.isActive(),
                                    occurrence.getIntensity(),
                                    occurrence.getAddress().getId(),
                                    occurrence.getUser().getId()))
                            .toList();
                    
                    log.info("Successfully retrieved {} occurrences for user: {}", response.size(), userId);
                    MDC.clear();
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving occurrences for user: {}", userId, throwable);
                    MDC.clear();
                    return ResponseEntity.internalServerError().build();
                });
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> createOccurrence(@Valid @RequestBody OccurrencePresentationDTO.CreateRequest request) {
        MDC.put("operation", "createOccurrence");
        MDC.put("addressId", String.valueOf(request.addressId()));
        MDC.put("userId", String.valueOf(request.userId()));
        MDC.put("intensity", request.intensity().toString());
        log.info("Starting to create occurrence for address: {} by user: {} with intensity: {}", request.addressId(), request.userId(), request.intensity());
        
        CreateOccurrenceCommand command = new CreateOccurrenceCommand(
                request.name(),
                request.description(),
                request.dateStart(),
                request.dateEnd(),
                request.dateUpdate(),
                request.active(),
                request.intensity(),
                request.addressId(),
                request.userId());
        
        return createOccurrenceCommandHandler.handle(command)
                .thenApply(occurrence -> {
                    OccurrenceApplicationDTO.Response response = new OccurrenceApplicationDTO.Response(
                            occurrence.getId(),
                            occurrence.getName(),
                            occurrence.getDescription(),
                            occurrence.getDateStart(),
                            occurrence.getDateEnd(),
                            occurrence.getDateUpdate(),
                            occurrence.isActive(),
                            occurrence.getIntensity(),
                            occurrence.getAddress().getId(),
                            occurrence.getUser().getId());
                    
                    log.info("Successfully created occurrence with id: {}", occurrence.getId());
                    MDC.clear();
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error creating occurrence for address: {} by user: {}", request.addressId(), request.userId(), throwable);
                    MDC.clear();
                    return ResponseEntity.badRequest().build();
                });
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> updateOccurrence(@PathVariable("id") int id, @Valid @RequestBody OccurrencePresentationDTO.UpdateRequest request) {
        MDC.put("operation", "updateOccurrence");
        MDC.put("occurrenceId", String.valueOf(id));
        MDC.put("addressId", String.valueOf(request.addressId()));
        MDC.put("userId", String.valueOf(request.userId()));
        log.info("Starting to update occurrence: {}", id);
        
        CompletableFuture<ResponseEntity<OccurrenceApplicationDTO.Response>> future = new CompletableFuture<>();
        
        occurrenceRepository.GetByIdAsync(id)
                .thenCompose(occurrenceOpt -> {
                    try {
                        if (occurrenceOpt.isPresent()) {
                            OccurrenceEntity existingOccurrence = occurrenceOpt.get();
                            existingOccurrence.setName(request.name());
                            existingOccurrence.setDescription(request.description());
                            existingOccurrence.setDateStart(request.dateStart());
                            existingOccurrence.setDateEnd(request.dateEnd());
                            existingOccurrence.setDateUpdate(request.dateUpdate());
                            existingOccurrence.setActive(request.active());
                            existingOccurrence.setIntensity(request.intensity());
                            
                            return occurrenceRepository.UpdateAsync(existingOccurrence)
                                    .thenApply(updatedOccurrence -> {
                                        try {
                                            OccurrenceApplicationDTO.Response response = new OccurrenceApplicationDTO.Response(
                                                    updatedOccurrence.getId(),
                                                    updatedOccurrence.getName(),
                                                    updatedOccurrence.getDescription(),
                                                    updatedOccurrence.getDateStart(),
                                                    updatedOccurrence.getDateEnd(),
                                                    updatedOccurrence.getDateUpdate(),
                                                    updatedOccurrence.isActive(),
                                                    updatedOccurrence.getIntensity(),
                                                    updatedOccurrence.getAddress().getId(),
                                                    updatedOccurrence.getUser().getId());
                                            
                                            log.info("Successfully updated occurrence: {}", id);
                                            MDC.clear();
                                            future.complete(ResponseEntity.ok(response));
                                        } catch (Exception e) {
                                            log.error("Error building response for updated occurrence: {}", id, e);
                                            MDC.clear();
                                            future.complete(ResponseEntity.internalServerError().build());
                                        }
                                        return null;
                                    });
                        } else {
                            log.warn("Occurrence not found for update with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                            return CompletableFuture.completedFuture(null);
                        }
                    } catch (Exception e) {
                        log.error("Error updating occurrence: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error updating occurrence: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteOccurrence(@PathVariable("id") int id) {
        MDC.put("operation", "deleteOccurrence");
        MDC.put("occurrenceId", String.valueOf(id));
        log.info("Starting to delete occurrence: {}", id);
        
        return occurrenceRepository.DeleteAsync(id)
                .thenApply(deletedOccurrence -> {
                    log.info("Successfully deleted occurrence: {}", id);
                    MDC.clear();
                    return ResponseEntity.noContent().<Void>build();
                })
                .exceptionally(throwable -> {
                    log.error("Error deleting occurrence: {}", id, throwable);
                    MDC.clear();
                    return ResponseEntity.notFound().build();
                });
    }
}