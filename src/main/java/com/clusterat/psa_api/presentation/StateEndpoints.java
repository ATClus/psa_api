package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateStateCommand;
import com.clusterat.psa_api.application.dto.StateApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateStateCommandHandler;
import com.clusterat.psa_api.application.interfaces.IStateRepository;
import com.clusterat.psa_api.domain.entities.StateEntity;
import com.clusterat.psa_api.presentation.dto.StatePresentationDTO;
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
@RequestMapping("/api/v1/states")
@Tag(name = "State Management", description = "API endpoints for managing states and provinces")
public class StateEndpoints {

    private final IStateRepository stateRepository;
    private final CreateStateCommandHandler createStateCommandHandler;
    private static final Logger log = LoggerFactory.getLogger(StateEndpoints.class);

    public StateEndpoints(IStateRepository stateRepository, CreateStateCommandHandler createStateCommandHandler) {
        this.stateRepository = stateRepository;
        this.createStateCommandHandler = createStateCommandHandler;
    }

    @Operation(summary = "Get all states", description = "Retrieve a list of all states in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved states",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StateApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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

    @Operation(summary = "Get state by ID", description = "Retrieve a specific state by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved state",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StateApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "State not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> getStateById(
            @Parameter(
                description = "Unique identifier of the state",
                required = true,
                example = "1",
                schema = @Schema(type = "integer", minimum = "1")
            ) @PathVariable("id") int id) {
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

    @Operation(summary = "Get state by IBGE code", description = "Retrieve a state by its Brazilian IBGE geographic code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved state",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StateApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "404", description = "State not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/ibge/{ibgeCode}")
    public CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> getStateByIbgeCode(
            @Parameter(
                description = "Brazilian IBGE (Instituto Brasileiro de Geografia e Estatística) geographic code for the state",
                required = true,
                example = "11",
                schema = @Schema(type = "string", pattern = "^[0-9]{1,2}$")
            ) @PathVariable("ibgeCode") String ibgeCode) {
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

    @Operation(summary = "Create new state", description = "Create a new state or province in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created state",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StateApplicationDTO.Response.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public CompletableFuture<ResponseEntity<StateApplicationDTO.Response>> createState(
            @RequestBody(
                description = "State creation request payload with all required fields",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = StatePresentationDTO.CreateRequest.class),
                    examples = {
                        @ExampleObject(
                            name = "createStateExample",
                            summary = "Create State Request",
                            description = "Example request to create a new Brazilian state",
                            value = "{\n  \"name\": \"São Paulo\",\n  \"shortName\": \"SP\",\n  \"region\": \"SUDESTE\",\n  \"ibgeCode\": \"35\",\n  \"countryId\": 1\n}"
                        )
                    }
                )
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody StatePresentationDTO.CreateRequest request) {
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

    @Operation(summary = "Delete state", description = "Delete a state from the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted state"),
            @ApiResponse(responseCode = "404", description = "State not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteState(
            @Parameter(
                description = "Unique identifier of the state to delete",
                required = true,
                example = "1",
                schema = @Schema(type = "integer", minimum = "1")
            ) @PathVariable("id") int id) {
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