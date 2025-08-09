package com.clusterat.psa_api.presentation;

import com.clusterat.psa_api.application.commands.CreateAddressCommand;
import com.clusterat.psa_api.application.dto.AddressApplicationDTO;
import com.clusterat.psa_api.application.handlers.CreateAddressCommandHandler;
import com.clusterat.psa_api.application.interfaces.IAddressRepository;
import com.clusterat.psa_api.domain.entities.AddressEntity;
import com.clusterat.psa_api.presentation.dto.AddressPresentationDTO;
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
@RequestMapping("/api/v1/addresses")
public class AddressEndpoints {

    private final IAddressRepository addressRepository;
    private final CreateAddressCommandHandler createAddressCommandHandler;
    private static final Logger log = LoggerFactory.getLogger(AddressEndpoints.class);

    public AddressEndpoints(IAddressRepository addressRepository, CreateAddressCommandHandler createAddressCommandHandler) {
        this.addressRepository = addressRepository;
        this.createAddressCommandHandler = createAddressCommandHandler;
    }

    @GetMapping
    public CompletableFuture<ResponseEntity<List<AddressApplicationDTO.Response>>> getAddresses() {
        MDC.put("operation", "getAddresses");
        log.info("Starting to retrieve all addresses");
        
        return addressRepository.GetAllAsync()
                .thenApply(addresses -> {
                    List<AddressApplicationDTO.Response> response = addresses.stream()
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .map(address -> new AddressApplicationDTO.Response(
                                    address.getId(),
                                    address.getStreet(),
                                    address.getNumber(),
                                    address.getComplement(),
                                    address.getNeighborhood(),
                                    address.getCity().getId()))
                            .toList();
                    
                    log.info("Successfully retrieved {} addresses", response.size());
                    MDC.clear();
                    return ResponseEntity.ok(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving addresses", throwable);
                    MDC.clear();
                    return ResponseEntity.<List<AddressApplicationDTO.Response>>internalServerError().build();
                });
    }

    @GetMapping("/{id}")
    public CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> getAddressById(@PathVariable("id") int id) {
        MDC.put("operation", "getAddressById");
        MDC.put("addressId", String.valueOf(id));
        log.info("Starting to retrieve address by id: {}", id);
        
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> future = new CompletableFuture<>();
        
        addressRepository.GetByIdAsync(id)
                .thenApply(addressOpt -> {
                    try {
                        if (addressOpt.isPresent()) {
                            AddressEntity address = addressOpt.get();
                            AddressApplicationDTO.Response response = new AddressApplicationDTO.Response(
                                    address.getId(),
                                    address.getStreet(),
                                    address.getNumber(),
                                    address.getComplement(),
                                    address.getNeighborhood(),
                                    address.getCity().getId());
                            
                            log.info("Successfully retrieved address: {}", address.getId());
                            MDC.clear();
                            future.complete(ResponseEntity.ok(response));
                        } else {
                            log.warn("Address not found with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                        }
                    } catch (Exception e) {
                        log.error("Error retrieving address by id: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                    }
                    return null;
                })
                .exceptionally(throwable -> {
                    log.error("Error retrieving address by id: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @PostMapping
    public CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> createAddress(@Valid @RequestBody AddressPresentationDTO.CreateRequest request) {
        MDC.put("operation", "createAddress");
        MDC.put("cityId", String.valueOf(request.cityId()));
        log.info("Starting to create address for city: {}", request.cityId());
        
        CreateAddressCommand command = new CreateAddressCommand(
                request.street(),
                request.number(),
                request.complement(),
                request.neighborhood(),
                request.cityId());
        
        return createAddressCommandHandler.handle(command)
                .thenApply(address -> {
                    AddressApplicationDTO.Response response = new AddressApplicationDTO.Response(
                            address.getId(),
                            address.getStreet(),
                            address.getNumber(),
                            address.getComplement(),
                            address.getNeighborhood(),
                            address.getCity().getId());
                    
                    log.info("Successfully created address with id: {}", address.getId());
                    MDC.clear();
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .exceptionally(throwable -> {
                    log.error("Error creating address for city: {}", request.cityId(), throwable);
                    MDC.clear();
                    return ResponseEntity.badRequest().build();
                });
    }

    @PutMapping("/{id}")
    public CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> updateAddress(@PathVariable("id") int id, @Valid @RequestBody AddressPresentationDTO.UpdateRequest request) {
        MDC.put("operation", "updateAddress");
        MDC.put("addressId", String.valueOf(id));
        MDC.put("cityId", String.valueOf(request.cityId()));
        log.info("Starting to update address: {}", id);
        
        CompletableFuture<ResponseEntity<AddressApplicationDTO.Response>> future = new CompletableFuture<>();
        
        addressRepository.GetByIdAsync(id)
                .thenCompose(addressOpt -> {
                    try {
                        if (addressOpt.isPresent()) {
                            AddressEntity existingAddress = addressOpt.get();
                            existingAddress.setStreet(request.street());
                            existingAddress.setNumber(request.number());
                            existingAddress.setComplement(request.complement());
                            existingAddress.setNeighborhood(request.neighborhood());
                            
                            return addressRepository.UpdateAsync(existingAddress)
                                    .thenApply(updatedAddress -> {
                                        try {
                                            AddressApplicationDTO.Response response = new AddressApplicationDTO.Response(
                                                    updatedAddress.getId(),
                                                    updatedAddress.getStreet(),
                                                    updatedAddress.getNumber(),
                                                    updatedAddress.getComplement(),
                                                    updatedAddress.getNeighborhood(),
                                                    updatedAddress.getCity().getId());
                                            
                                            log.info("Successfully updated address: {}", id);
                                            MDC.clear();
                                            future.complete(ResponseEntity.ok(response));
                                        } catch (Exception e) {
                                            log.error("Error building response for updated address: {}", id, e);
                                            MDC.clear();
                                            future.complete(ResponseEntity.internalServerError().build());
                                        }
                                        return null;
                                    });
                        } else {
                            log.warn("Address not found for update with id: {}", id);
                            MDC.clear();
                            future.complete(ResponseEntity.notFound().build());
                            return CompletableFuture.completedFuture(null);
                        }
                    } catch (Exception e) {
                        log.error("Error updating address: {}", id, e);
                        MDC.clear();
                        future.complete(ResponseEntity.internalServerError().build());
                        return CompletableFuture.completedFuture(null);
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Error updating address: {}", id, throwable);
                    MDC.clear();
                    future.complete(ResponseEntity.internalServerError().build());
                    return null;
                });
                
        return future;
    }

    @DeleteMapping("/{id}")
    public CompletableFuture<ResponseEntity<Void>> deleteAddress(@PathVariable("id") int id) {
        MDC.put("operation", "deleteAddress");
        MDC.put("addressId", String.valueOf(id));
        log.info("Starting to delete address: {}", id);
        
        return addressRepository.DeleteAsync(id)
                .thenApply(deletedAddress -> {
                    log.info("Successfully deleted address: {}", id);
                    MDC.clear();
                    return ResponseEntity.noContent().<Void>build();
                })
                .exceptionally(throwable -> {
                    log.error("Error deleting address: {}", id, throwable);
                    MDC.clear();
                    return ResponseEntity.notFound().build();
                });
    }
}