package com.crypto.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Long id;
    private String address;
    private String blockchain;
    private String label;
    private String notes;
    private boolean favorite;
    private boolean verified;
    private BigDecimal totalBalance;
    private BigDecimal totalBalanceUsd;
    private LocalDateTime lastBalanceCheck;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<CategoryResponse> categories;
    private Set<BalanceResponse> balances;
}