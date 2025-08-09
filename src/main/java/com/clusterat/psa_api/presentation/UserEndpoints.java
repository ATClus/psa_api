package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreteUserCommand;
import com.clusterat.psa_api.application.dto.UserApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateUserCommandHandler;
import com.clusterat.psa_api.application.interfaces.IUserRepository;
import com.clusterat.psa_api.domain.entities.UserEntity;
import com.clusterat.psa_api.presentation.dto.UserPresentationDTO;
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
@RequestMapping("/api/v1/users")
public class UserEndpoints {

    private final IUserRepository userRepository;
    private final CreateUserCommandHandler createUserCommandHandler;
    private static final Logger log = LoggerFactory.getLogger(UserEndpoints.class);

    public UserEndpoints(IUserRepository userRepository, CreateUserCommandHandler createUserCommandHandler) {
        this.userRepository = userRepository;
        this.createUserCommandHandler = createUserCommandHandler;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<UserApplicationDTO.Response>>> getUsers() {
        MDC.put("operation", "getUsers");
        log.info("Starting to retrieve all users");
        
        return userRepository.GetAllAsync()
                .thenApply(users -> {
                    List<UserApplicationDTO.Response> response = users.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(user -> new UserApplicationDTO.Response(user.getId(), user.getCognitoId()))
                            .toList();
                    
                    log.info("Successfully retrieved {} users", response.size());
                    MDC.clear();
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving users", throwable);
                    MDC.clear();
                    return ResponseEntity.internalServerError().build();
                });
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> getUserById(@PathVariable("id") int id) {
        MDC.put("operation", "getUserById");
        MDC.put("userId", String.valueOf(id));
        log.info("Starting to retrieve user by id: {}", id);
        
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> future = new CompletableFuture<>();
        
        userRepository.GetByIdAsync(id)
                .thenApply(userOpt -> {
                    try {
                        if (userOpt.isPresent()) {
                            UserEntity user = userOpt.get();
                            UserApplicationDTO.Response response = new UserApplicationDTO.Response(user.getId(), user.getCognitoId());
                            
                            log.info("Successfully retrieved user: {}", user.getId());
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("User not found with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving user by id: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving user by id: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @GetMapping("/cognito/{cognitoId}")
    public CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> getUserByCognitoId(@PathVariable("cognitoId") int cognitoId) {
        MDC.put("operation", "getUserByCognitoId");
        MDC.put("cognitoId", String.valueOf(cognitoId));
        log.info("Starting to retrieve user by cognito id: {}", cognitoId);
        
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> future = new CompletableFuture<>();
        
        userRepository.GetByCognitoIdAsync(cognitoId)
                .thenApply(userOpt -> {
                    try {
                        if (userOpt.isPresent()) {
                            UserEntity user = userOpt.get();
                            UserApplicationDTO.Response response = new UserApplicationDTO.Response(user.getId(), user.getCognitoId());
                            
                            log.info("Successfully retrieved user by cognito id: {}", cognitoId);
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("User not found with cognito id: {}", cognitoId);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving user by cognito id: {}", cognitoId, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving user by cognito id: {}", cognitoId, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> createUser(@Valid @RequestBody UserPresentationDTO.CreateRequest request) {
        MDC.put("operation", "createUser");
        MDC.put("cognitoId", String.valueOf(request.cognitoId()));
        log.info("Starting to create user with cognito id: {}", request.cognitoId());
        
        CreteUserCommand command = new CreteUserCommand(request.cognitoId());
        return createUserCommandHandler.handle(command)
                .thenApply(user -> {
                    UserApplicationDTO.Response response = new UserApplicationDTO.Response(user.getId(), user.getCognitoId());
                    
                    log.info("Successfully created user with id: {}", user.getId());
                    MDC.clear();
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error creating user with cognito id: {}", request.cognitoId(), throwable);
                    MDC.clear();
                    return ResponseEntity.badRequest().build();
                });
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> updateUser(@PathVariable("id") int id, @Valid @RequestBody UserPresentationDTO.CreateRequest request) {
        MDC.put("operation", "updateUser");
        MDC.put("userId", String.valueOf(id));
        MDC.put("cognitoId", String.valueOf(request.cognitoId()));
        log.info("Starting to update user: {}", id);
        
        CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> future = new CompletableFuture<>();
        
        userRepository.GetByIdAsync(id)
                .thenCompose(userOpt -> {
                    try {
                        if (userOpt.isPresent()) {
                            UserEntity existingUser = userOpt.get();
                            existingUser.setCognitoId(request.cognitoId());
                            return userRepository.UpdateAsync(existingUser)
                                    .thenApply(updatedUser -> {
                                        try {
                                            UserApplicationDTO.Response response = new UserApplicationDTO.Response(updatedUser.getId(), updatedUser.getCognitoId());
                                            
                                            log.info("Successfully updated user: {}", id);
                                            MDC.clear();
                                            future.complete(ResponseEntity.ok(response));
                                        } catch (Exception e) {
                                            log.error("Error building response for updated user: {}", id, e);
                                            MDC.clear();
                                            future.complete(ResponseEntity.internalServerError().build());
                                        }
                                        return null;
                                    });
                        } else {
                            log.warn("User not found for update with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                            return CompletableFuture.completedFuture(null);
                        }
                    } catch (Exception e) {
                        log.error("Error updating user: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error updating user: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteUser(@PathVariable("id") int id) {
        MDC.put("operation", "deleteUser");
        MDC.put("userId", String.valueOf(id));
        log.info("Starting to delete user: {}", id);
        
        return userRepository.DeleteAsync(id)
                .thenApply(deletedUser -> {
                    log.info("Successfully deleted user: {}", id);
                    MDC.clear();
                    return ResponseEntity.noContent().<Void>build();
                })
                .exceptionally(throwable -> {
                    log.error("Error deleting user: {}", id, throwable);
                    MDC.clear();
                    return ResponseEntity.notFound().build();
                });
    }
}
