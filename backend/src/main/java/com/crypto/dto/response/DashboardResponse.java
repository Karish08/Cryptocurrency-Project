package com.crypto.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Long totalAddresses;
    private Long totalCategories;
    private BigDecimal totalBalanceUsd;
    private BigDecimal totalBalanceBtc;
    private Long favoriteCount;
    private Long verifiedCount;
    
    private Map<String, Long> blockchainDistribution;
    private Map<String, BigDecimal> balanceDistribution;
    private List<AddressResponse> recentAddresses;
    private List<TransactionResponse> recentTransactions;
    private List<CategoryResponse> topCategories;
    
    private BigDecimal dailyChange;
    private BigDecimal weeklyChange;
    private BigDecimal monthlyChange;
}