package com.clusterat.psa_api.application.handlers;

import com.clusterat.psa_api.application.commands.CreteUserCommand;
import com.clusterat.psa_api.application.interfaces.IUserRepository;
import com.clusterat.psa_api.domain.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class CreateUserCommandHandler {
    private final IUserRepository userRepository;

    @Autowired
    public CreateUserCommandHandler(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CompletableFuture<UserEntity> handle(CreteUserCommand command) {
        UserEntity newUser = new UserEntity(command.cognitoId());
        return userRepository.AddAsync(newUser);
    }
}
