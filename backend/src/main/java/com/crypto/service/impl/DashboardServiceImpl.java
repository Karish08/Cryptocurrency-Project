package com.crypto.service.impl;

import com.crypto.dto.response.AddressResponse;
import com.crypto.dto.response.DashboardResponse;
import com.crypto.dto.response.TransactionResponse;
import com.crypto.model.User;
import com.crypto.repository.AddressRepository;
import com.crypto.repository.CategoryRepository;
import com.crypto.repository.TransactionRepository;
import com.crypto.repository.UserRepository;
import com.crypto.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.crypto.dto.response.CategoryResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {
    
    private final AddressRepository addressRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    
    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Override
    @Cacheable(value = "dashboard", key = "#root.methodName")
    public DashboardResponse getDashboardStats() {
        User currentUser = getCurrentUser();
        Long userId = currentUser.getId();
        
        // Basic counts
        Long totalAddresses = addressRepository.countByUserId(userId);
        Long totalCategories = categoryRepository.countByUserId(userId);
        Long favoriteCount = addressRepository.countByUserIdAndFavoriteTrue(userId);
        Long verifiedCount = addressRepository.countByUserIdAndVerifiedTrue(userId);
        
        // Total balance
        BigDecimal totalBalanceUsd = addressRepository.sumTotalBalanceUsdByUserId(userId)
                .orElse(BigDecimal.ZERO);
        BigDecimal totalBalanceBtc = addressRepository.sumTotalBalanceBtcByUserId(userId)
                .orElse(BigDecimal.ZERO);
        
        // Blockchain distribution
        Map<String, Long> blockchainDistribution = addressRepository
                .getBlockchainDistribution(userId)
                .stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));
        
        // Balance distribution by category
        Map<String, BigDecimal> balanceDistribution = addressRepository
                .getBalanceDistributionByCategory(userId)
                .stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (BigDecimal) obj[1]
                ));
        
        // Recent addresses
        List<AddressResponse> recentAddresses = addressRepository
                .findTop5ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToAddressResponse)
                .collect(Collectors.toList());
        
        // Recent transactions
        List<TransactionResponse> recentTransactions = transactionRepository
                .findTop10ByAddressUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(this::convertToTransactionResponse)
                .collect(Collectors.toList());
        
        // Top categories by address count
        List<CategoryResponse> topCategories = categoryRepository
                .findTop5ByUserIdOrderByAddressCountDesc(userId)
                .stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
        
        // Calculate changes
        BigDecimal dailyChange = calculateBalanceChange(userId, ChronoUnit.DAYS);
        BigDecimal weeklyChange = calculateBalanceChange(userId, ChronoUnit.WEEKS);
        BigDecimal monthlyChange = calculateBalanceChange(userId, ChronoUnit.MONTHS);
        
        return DashboardResponse.builder()
                .totalAddresses(totalAddresses)
                .totalCategories(totalCategories)
                .totalBalanceUsd(totalBalanceUsd)
                .totalBalanceBtc(totalBalanceBtc)
                .favoriteCount(favoriteCount)
                .verifiedCount(verifiedCount)
                .blockchainDistribution(blockchainDistribution)
                .balanceDistribution(balanceDistribution)
                .recentAddresses(recentAddresses)
                .recentTransactions(recentTransactions)
                .topCategories(topCategories)
                .dailyChange(dailyChange)
                .weeklyChange(weeklyChange)
                .monthlyChange(monthlyChange)
                .build();
    }
    
    private BigDecimal calculateBalanceChange(Long userId, ChronoUnit period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime previous = now.minus(1, period);
        
        BigDecimal currentBalance = addressRepository.sumTotalBalanceUsdByUserId(userId)
                .orElse(BigDecimal.ZERO);
        BigDecimal previousBalance = addressRepository.sumTotalBalanceUsdByUserIdAndDateRange(userId, previous, now)
                .orElse(BigDecimal.ZERO);
        
        if (previousBalance.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return currentBalance.subtract(previousBalance)
                .divide(previousBalance, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }
    
    private AddressResponse convertToAddressResponse(com.crypto.model.Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .address(address.getAddress())
                .blockchain(address.getBlockchain())
                .label(address.getLabel())
                .favorite(address.isFavorite())
                .verified(address.isVerified())
                .totalBalanceUsd(address.getTotalBalanceUsd())
                .createdAt(address.getCreatedAt())
                .build();
    }
    
    private TransactionResponse convertToTransactionResponse(com.crypto.model.Transaction transaction) {
        return TransactionResponse.builder()
                .txHash(transaction.getTxHash())
                .blockchain(transaction.getBlockchain())
                .fromAddress(transaction.getFromAddress())
                .toAddress(transaction.getToAddress())
                .value(transaction.getValue())
                .valueUsd(transaction.getValueUsd())
                .tokenSymbol(transaction.getTokenSymbol())
                .timestamp(transaction.getTimestamp())
                .status(transaction.getStatus())
                .blockNumber(transaction.getBlockNumber())
                .confirmations(transaction.getConfirmations())
                .build();
    }
    
    private CategoryResponse convertToCategoryResponse(com.crypto.model.Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .color(category.getColor())
                .createdAt(category.getCreatedAt())
                .addressCount(category.getAddresses() != null ? category.getAddresses().size() : 0)
                .build();
    }
}