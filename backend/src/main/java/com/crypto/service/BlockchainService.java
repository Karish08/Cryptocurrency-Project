package com.crypto.service;

import com.crypto.model.Address;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface BlockchainService {
    CompletableFuture<Void> fetchAddressBalanceAsync(Address address);
    void fetchAddressBalance(Address address);
    void fetchTokenBalances(Address address);
    void fetchRecentTransactions(Address address);
    BigDecimal getPriceUsd(String blockchain);
}
