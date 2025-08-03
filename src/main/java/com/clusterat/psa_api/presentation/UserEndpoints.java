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
        log.info("Working on getting users list");
        return userRepository.GetAllAsync()
                .thenApply(users -> {
                    List<UserApplicationDTO.Response> response = users.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(user -> new UserApplicationDTO.Response(user.getId(), user.getCognitoId()))
                            .toList();
                    return ResponseEntity.ok(response);
                });
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> getUserById(@PathVariable("id") int id) {
        log.info("Working on getting user by id {}", id);
        return userRepository.GetByIdAsync(id)
                .thenApply(userOpt -> {
                    if (userOpt.isPresent()) {
                        UserEntity user = userOpt.get();
                        UserApplicationDTO.Response response = new UserApplicationDTO.Response(user.getId(), user.getCognitoId());
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                });
    }

    @GetMapping("/cognito/{cognitoId}")
    public CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> getUserByCognitoId(@PathVariable("cognitoId") int cognitoId) {
        log.info("Working on getting user by cognito id {}", cognitoId);
        return userRepository.GetByCognitoIdAsync(cognitoId)
                .thenApply(userOpt -> {
                    if (userOpt.isPresent()) {
                        UserEntity user = userOpt.get();
                        UserApplicationDTO.Response response = new UserApplicationDTO.Response(user.getId(), user.getCognitoId());
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.notFound().build();
                    }
                });
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> createUser(@Valid @RequestBody UserPresentationDTO.CreateRequest request) {
        log.info("Working on create user {}", request);
        CreteUserCommand command = new CreteUserCommand(request.cognitoId());
        return createUserCommandHandler.handle(command)
                .thenApply(user -> {
                    UserApplicationDTO.Response response = new UserApplicationDTO.Response(user.getId(), user.getCognitoId());
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                });
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<UserApplicationDTO.Response>> updateUser(@PathVariable("id") int id, @Valid @RequestBody UserPresentationDTO.CreateRequest request) {
        log.info("Working on update user {}", request);
        return userRepository.GetByIdAsync(id)
                .thenCompose(userOpt -> {
                    if (userOpt.isPresent()) {
                        UserEntity existingUser = userOpt.get();
                        existingUser.setCognitoId(request.cognitoId());
                        return userRepository.UpdateAsync(existingUser)
                                .thenApply(updatedUser -> {
                                    UserApplicationDTO.Response response = new UserApplicationDTO.Response(updatedUser.getId(), updatedUser.getCognitoId());
                                    return ResponseEntity.ok(response);
                                });
                    } else {
                        return CompletableFuture.completedFuture(ResponseEntity.notFound().build());
                    }
                });
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteUser(@PathVariable("id") int id) {
        log.info("Working on delete user {}", id);
        return userRepository.DeleteAsync(id)
                .thenApply(deletedUser -> ResponseEntity.noContent().<Void>build())
                .exceptionally(throwable -> ResponseEntity.notFound().build());
    }
}
