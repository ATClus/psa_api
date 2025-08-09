package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateStateCommand;
import com.clusterat.psa_api.application.dto.StateApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateStateCommandHandler;
import com.clusterat.psa_api.application.interfaces.IStateRepository;
import com.clusterat.psa_api.domain.entities.StateEntity;
import com.clusterat.psa_api.presentation.dto.StatePresentationDTO;
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
@RequestMapping("/api/v1/states")
public class StateEndpoints {

    private final IStateRepository stateRepository;
    private final CreateStateCommandHandler createStateCommandHandler;
    private static final Logger log = LoggerFactory.getLogger(StateEndpoints.class);

    public StateEndpoints(IStateRepository stateRepository, CreateStateCommandHandler createStateCommandHandler) {
        this.stateRepository = stateRepository;
        this.createStateCommandHandler = createStateCommandHandler;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<StateApplicationDTO.Response>>> getStates() {
        MDC.put("operation", "getStates");
        log.info("Starting to retrieve all states");
        
        return stateRepository.GetAllAsync()
                .thenApply(states -> {
                    List<StateApplicationDTO.Response> response = states.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(state -> new StateApplicationDTO.Response(
                                    state.getId(),
                                    state.getName(),
                                    state.getShortName(),
                                    state.getRegion(),
                                    state.getIbgeCode(),
                                    state.getCountry().getId()))
                            .toList();
                    
                    log.info("Successfully retrieved {} states", response.size());
                    MDC.clear();
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving states", throwable);
                    MDC.clear();
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> getStateById(@PathVariable("id") int id) {
        MDC.put("operation", "getStateById");
        MDC.put("stateId", String.valueOf(id));
        log.info("Starting to retrieve state by id: {}", id);
        
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> future = new CompletableFuture<>();
        
        stateRepository.GetByIdAsync(id)
                .thenApply(stateOpt -> {
                    try {
                        if (stateOpt.isPresent()) {
                            StateEntity state = stateOpt.get();
                            StateApplicationDTO.Response response = new StateApplicationDTO.Response(
                                    state.getId(),
                                    state.getName(),
                                    state.getShortName(),
                                    state.getRegion(),
                                    state.getIbgeCode(),
                                    state.getCountry().getId());
                            
                            log.info("Successfully retrieved state: {}", state.getId());
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("State not found with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving state by id: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving state by id: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @GetMapping("/ibge/{ibgeCode}")
    public CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> getStateByIbgeCode(@PathVariable("ibgeCode") String ibgeCode) {
        MDC.put("operation", "getStateByIbgeCode");
        MDC.put("ibgeCode", ibgeCode);
        log.info("Starting to retrieve state by IBGE code: {}", ibgeCode);
        
        CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> future = new CompletableFuture<>();
        
        stateRepository.GetByIbgeCodeAsync(ibgeCode)
                .thenApply(stateOpt -> {
                    try {
                        if (stateOpt.isPresent()) {
                            StateEntity state = stateOpt.get();
                            StateApplicationDTO.Response response = new StateApplicationDTO.Response(
                                    state.getId(),
                                    state.getName(),
                                    state.getShortName(),
                                    state.getRegion(),
                                    state.getIbgeCode(),
                                    state.getCountry().getId());
                            
                            log.info("Successfully retrieved state by IBGE code: {}", ibgeCode);
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("State not found with IBGE code: {}", ibgeCode);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving state by IBGE code: {}", ibgeCode, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving state by IBGE code: {}", ibgeCode, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> createState(@Valid @RequestBody StatePresentationDTO.CreateRequest request) {
        MDC.put("operation", "createState");
        MDC.put("countryId", String.valueOf(request.countryId()));
        MDC.put("ibgeCode", request.ibgeCode());
        log.info("Starting to create state with IBGE code: {} for country: {}", request.ibgeCode(), request.countryId());
        
        CreateStateCommand command = new CreateStateCommand(
                request.name(),
                request.shortName(),
                request.region(),
                request.ibgeCode(),
                request.countryId());
        
        return createStateCommandHandler.handle(command)
                .thenApply(state -> {
                    StateApplicationDTO.Response response = new StateApplicationDTO.Response(
                            state.getId(),
                            state.getName(),
                            state.getShortName(),
                            state.getRegion(),
                            state.getIbgeCode(),
                            state.getCountry().getId());
                    
                    log.info("Successfully created state with id: {}", state.getId());
                    MDC.clear();
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error creating state with IBGE code: {} for country: {}", request.ibgeCode(), request.countryId(), throwable);
                    MDC.clear();
                    return ResponseEntity.badRequest().build();
                });
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteState(@PathVariable("id") int id) {
        MDC.put("operation", "deleteState");
        MDC.put("stateId", String.valueOf(id));
        log.info("Starting to delete state: {}", id);
        
        return stateRepository.DeleteAsync(id)
                .thenApply(deletedState -> {
                    log.info("Successfully deleted state: {}", id);
                    MDC.clear();
                    return ResponseEntity.noContent().<Void>build();
                })
                .exceptionally(throwable -> {
                    log.error("Error deleting state: {}", id, throwable);
                    MDC.clear();
                    return ResponseEntity.notFound().build();
                });
    }
}