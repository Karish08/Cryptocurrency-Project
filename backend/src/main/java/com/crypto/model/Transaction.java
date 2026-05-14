package com.crypto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions", 
       indexes = {
           @Index(name = "idx_tx_hash", columnList = "tx_hash"),
           @Index(name = "idx_address_id", columnList = "address_id"),
           @Index(name = "idx_timestamp", columnList = "timestamp")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    @JsonIgnore
    private Address address;
    
    @Column(name = "tx_hash", nullable = false, length = 255)
    private String txHash;
    
    @Column(nullable = false, length = 50)
    private String blockchain;
    
    @Column(name = "from_address", length = 255)
    private String fromAddress;
    
    @Column(name = "to_address", length = 255)
    private String toAddress;
    
    @Column(precision = 30, scale = 8)
    private BigDecimal value;
    
    @Column(name = "value_usd", precision = 30, scale = 2)
    private BigDecimal valueUsd;
    
    @Column(name = "token_symbol", length = 20)
    private String tokenSymbol;
    
    @Column(name = "token_address", length = 255)
    private String tokenAddress;
    
    private Long timestamp;
    
    @Column(length = 20)
    private String status;
    
    @Column(name = "gas_used", precision = 20)
    private BigDecimal gasUsed;
    
    @Column(name = "gas_price", precision = 20)
    private BigDecimal gasPrice;
    
    @Column(name = "block_number")
    private Long blockNumber;
    
    @Column(name = "confirmations")
    private Integer confirmations;
    
    @Column(length = 500)
    private String note;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}