package com.crypto.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String txHash;
    private String blockchain;
    private String fromAddress;
    private String toAddress;
    private BigDecimal value;
    private BigDecimal valueUsd;
    private String tokenSymbol;
    private Long timestamp;
    private String status;
    private Long blockNumber;
    private Integer confirmations;
}