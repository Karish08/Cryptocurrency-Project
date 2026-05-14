package com.crypto.repository;

import com.crypto.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Page<Transaction> findByAddressId(Long addressId, Pageable pageable);
    
    Optional<Transaction> findByTxHashAndBlockchain(String txHash, String blockchain);
    
    List<Transaction> findTop10ByAddressIdOrderByTimestampDesc(Long addressId);
    
    boolean existsByTxHashAndBlockchain(String txHash, String blockchain);

    List<Transaction> findTop10ByAddressUserIdOrderByTimestampDesc(Long userId);
}