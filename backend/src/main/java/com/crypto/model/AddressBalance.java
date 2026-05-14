package com.crypto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "address_balances", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"address_id", "token_symbol", "token_address"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AddressBalance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    @JsonIgnore
    private Address address;
    
    @Column(name = "token_symbol", nullable = false, length = 20)
    private String tokenSymbol;
    
    @Column(name = "token_name", length = 50)
    private String tokenName;
    
    @Column(name = "token_address", length = 255)
    private String tokenAddress;
    
    @Column(name = "token_decimals")
    private Integer tokenDecimals;
    
    @Column(precision = 30, scale = 8)
    private BigDecimal balance;
    
    @Column(name = "balance_usd", precision = 30, scale = 2)
    private BigDecimal balanceUsd;
    
    @Column(name = "balance_btc", precision = 30, scale = 8)
    private BigDecimal balanceBtc;
    
    @Column(name = "balance_eth", precision = 30, scale = 8)
    private BigDecimal balanceEth;
    
    @LastModifiedDate
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}