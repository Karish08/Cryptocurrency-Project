package com.crypto.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {
    private String tokenSymbol;
    private String tokenName;
    private String tokenAddress;
    private BigDecimal balance;
    private BigDecimal balanceUsd;
    private BigDecimal balanceBtc;
    private BigDecimal balanceEth;
    private LocalDateTime lastUpdated;
}