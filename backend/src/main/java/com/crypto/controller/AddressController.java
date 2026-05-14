package com.crypto.controller;

import com.crypto.dto.request.AddressRequest;
import com.crypto.dto.response.AddressResponse;
import com.crypto.dto.response.ErrorResponse;
import com.crypto.dto.response.PagedResponse;
import com.crypto.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")

@RequiredArgsConstructor
@Tag(name = "Addresses", description = "Address management endpoints")
public class AddressController {
    
    private final AddressService addressService;
    
    @GetMapping
    @Operation(summary = "Get all addresses with pagination")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<AddressResponse>> getAddresses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String blockchain,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean favorite) {
        
        Sort sort = sortDirection.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AddressResponse> addresses = addressService.getUserAddresses(
            pageable, blockchain, categoryId, favorite);
        
        return ResponseEntity.ok(PagedResponse.of(addresses));
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> getAddress(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }
    
    @PostMapping
    @Operation(summary = "Create new address")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> createAddress(
            @Valid @RequestBody AddressRequest addressRequest) {
        return ResponseEntity.ok(addressService.createAddress(addressRequest));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update address")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest addressRequest) {
        return ResponseEntity.ok(addressService.updateAddress(id, addressRequest));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.ok().body(new ErrorResponse("Address deleted successfully"));
    }
    
    @PostMapping("/{id}/refresh")
    @Operation(summary = "Refresh address balance")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> refreshBalance(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.refreshAddressBalance(id));
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search addresses")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<AddressResponse>> searchAddresses(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AddressResponse> addresses = addressService.searchAddresses(q, pageable);
        return ResponseEntity.ok(PagedResponse.of(addresses));
    }
    
    @PostMapping("/{id}/verify")
    @Operation(summary = "Verify address ownership")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> verifyAddress(@PathVariable Long id) {
        addressService.initiateVerification(id);
        return ResponseEntity.ok().body(new ErrorResponse("Verification initiated"));
    }
    
    @PostMapping("/{id}/verify/confirm")
    @Operation(summary = "Confirm address verification")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<AddressResponse> confirmVerification(
            @PathVariable Long id,
            @RequestParam String code) {
        return ResponseEntity.ok(addressService.confirmVerification(id, code));
    }
    
    @GetMapping("/favorites")
    @Operation(summary = "Get favorite addresses")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AddressResponse>> getFavorites() {
        return ResponseEntity.ok(addressService.getFavoriteAddresses());
    }
}