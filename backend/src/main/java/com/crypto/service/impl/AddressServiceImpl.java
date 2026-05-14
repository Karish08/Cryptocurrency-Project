package com.crypto.service.impl;

import com.crypto.dto.request.AddressRequest;
import com.crypto.dto.response.AddressResponse;
import com.crypto.dto.response.BalanceResponse;
import com.crypto.dto.response.CategoryResponse;
import com.crypto.exception.ResourceNotFoundException;
import com.crypto.model.Address;
import com.crypto.model.AddressBalance;
import com.crypto.model.Category;
import com.crypto.model.User;
import com.crypto.repository.AddressRepository;
import com.crypto.repository.CategoryRepository;
import com.crypto.repository.UserRepository;
import com.crypto.service.AddressService;
import com.crypto.service.BlockchainService;
import com.crypto.util.AddressValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    
    private final AddressRepository addressRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BlockchainService blockchainService;
    private final AddressValidator addressValidator;
    
    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Override
    @Cacheable(value = "addresses", key = "#pageable + #blockchain + #categoryId + #favorite")
    public Page<AddressResponse> getUserAddresses(Pageable pageable, String blockchain, 
                                                  Long categoryId, Boolean favorite) {
        User currentUser = getCurrentUser();
        
        Page<Address> addresses;
        
        if (blockchain != null) {
            addresses = addressRepository.findByUserIdAndBlockchain(currentUser.getId(), blockchain, pageable);
        } else if (categoryId != null) {
            addresses = addressRepository.findByCategoryIdAndUserId(categoryId, currentUser.getId(), pageable);
        } else if (favorite != null) {
            addresses = addressRepository.findByUserIdAndFavorite(currentUser.getId(), favorite, pageable);
        } else {
            addresses = addressRepository.findByUserId(currentUser.getId(), pageable);
        }
        
        return addresses.map(this::convertToResponse);
    }
    
    @Override
    @Cacheable(value = "addresses", key = "#id")
    public AddressResponse getAddressById(Long id) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        return convertToResponse(address);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "addresses", allEntries = true)
    public AddressResponse createAddress(AddressRequest addressRequest) {
        User currentUser = getCurrentUser();
        
        // Validate address format
        if (!addressValidator.isValid(addressRequest.getAddress(), addressRequest.getBlockchain())) {
            throw new IllegalArgumentException("Invalid address format for blockchain: " + addressRequest.getBlockchain());
        }
        
        // Check if address already exists
        addressRepository.findByAddressAndBlockchainAndUserId(
                addressRequest.getAddress(), addressRequest.getBlockchain(), currentUser.getId())
                .ifPresent(a -> {
                    throw new IllegalArgumentException("Address already exists");
                });
        
        Address address = Address.builder()
                .address(addressRequest.getAddress())
                .blockchain(addressRequest.getBlockchain())
                .label(addressRequest.getLabel())
                .notes(addressRequest.getNotes())
                .favorite(addressRequest.getFavorite() != null ? addressRequest.getFavorite() : false)
                .user(currentUser)
                .build();
        
        // Add categories
        if (addressRequest.getCategoryIds() != null && !addressRequest.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : addressRequest.getCategoryIds()) {
                categoryRepository.findByIdAndUserId(categoryId, currentUser.getId())
                        .ifPresent(categories::add);
            }
            address.setCategories(categories);
        }
        
        Address savedAddress = addressRepository.save(address);
        
        // Fetch initial balance asynchronously
        blockchainService.fetchAddressBalanceAsync(savedAddress);
        
        log.info("Address created: {} for user: {}", savedAddress.getAddress(), currentUser.getUsername());
        
        return convertToResponse(savedAddress);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "addresses", key = "#id", allEntries = true)
    public AddressResponse updateAddress(Long id, AddressRequest addressRequest) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        address.setLabel(addressRequest.getLabel());
        address.setNotes(addressRequest.getNotes());
        address.setFavorite(addressRequest.getFavorite() != null ? addressRequest.getFavorite() : address.isFavorite());
        
        // Update categories
        if (addressRequest.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : addressRequest.getCategoryIds()) {
                categoryRepository.findByIdAndUserId(categoryId, currentUser.getId())
                        .ifPresent(categories::add);
            }
            address.setCategories(categories);
        }
        
        Address updatedAddress = addressRepository.save(address);
        
        log.info("Address updated: {} for user: {}", updatedAddress.getAddress(), currentUser.getUsername());
        
        return convertToResponse(updatedAddress);
    }
    
    @Override
    @Transactional
    @CacheEvict(value = "addresses", allEntries = true)
    public void deleteAddress(Long id) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        addressRepository.delete(address);
        
        log.info("Address deleted: {} for user: {}", address.getAddress(), currentUser.getUsername());
    }
    
    @Override
    @Transactional
    public AddressResponse refreshAddressBalance(Long id) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        blockchainService.fetchAddressBalance(address);
        
        return convertToResponse(addressRepository.findById(id).orElse(address));
    }
    
    @Override
    public Page<AddressResponse> searchAddresses(String searchTerm, Pageable pageable) {
        User currentUser = getCurrentUser();
        return addressRepository.searchAddresses(currentUser.getId(), searchTerm, pageable)
                .map(this::convertToResponse);
    }
    
    @Override
    public List<AddressResponse> getFavoriteAddresses() {
        User currentUser = getCurrentUser();
        return addressRepository.findByUserIdAndFavoriteTrue(currentUser.getId()).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void initiateVerification(Long id) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        String verificationCode = UUID.randomUUID().toString().substring(0, 8);
        address.setVerificationCode(verificationCode);
        addressRepository.save(address);
        
        // Send verification code via email or display
        log.info("Verification initiated for address: {} with code: {}", address.getAddress(), verificationCode);
    }
    
    @Override
    @Transactional
    public AddressResponse confirmVerification(Long id, String code) {
        User currentUser = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with id: " + id));
        
        if (code.equals(address.getVerificationCode())) {
            address.setVerified(true);
            address.setVerificationCode(null);
            addressRepository.save(address);
            log.info("Address verified: {}", address.getAddress());
        } else {
            throw new IllegalArgumentException("Invalid verification code");
        }
        
        return convertToResponse(address);
    }
    
    private AddressResponse convertToResponse(Address address) {
        Set<CategoryResponse> categoryResponses = address.getCategories().stream()
                .map(category -> CategoryResponse.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .color(category.getColor())
                        .createdAt(category.getCreatedAt())
                        .build())
                .collect(Collectors.toSet());
        
        Set<BalanceResponse> balanceResponses = address.getBalances().stream()
                .map(balance -> BalanceResponse.builder()
                        .tokenSymbol(balance.getTokenSymbol())
                        .tokenName(balance.getTokenName())
                        .tokenAddress(balance.getTokenAddress())
                        .balance(balance.getBalance())
                        .balanceUsd(balance.getBalanceUsd())
                        .balanceBtc(balance.getBalanceBtc())
                        .balanceEth(balance.getBalanceEth())
                        .lastUpdated(balance.getLastUpdated())
                        .build())
                .collect(Collectors.toSet());
        
        return AddressResponse.builder()
                .id(address.getId())
                .address(address.getAddress())
                .blockchain(address.getBlockchain())
                .label(address.getLabel())
                .notes(address.getNotes())
                .favorite(address.isFavorite())
                .verified(address.isVerified())
                .totalBalance(address.getTotalBalance())
                .totalBalanceUsd(address.getTotalBalanceUsd())
                .lastBalanceCheck(address.getLastBalanceCheck())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .categories(categoryResponses)
                .balances(balanceResponses)
                .build();
    }
}