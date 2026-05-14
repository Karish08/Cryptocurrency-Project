package com.crypto.repository;

import com.crypto.model.AddressBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressBalanceRepository extends JpaRepository<AddressBalance, Long> {
    
    List<AddressBalance> findByAddressId(Long addressId);
    
    Optional<AddressBalance> findByAddressIdAndTokenSymbolAndTokenAddress(
            Long addressId, String tokenSymbol, String tokenAddress);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM AddressBalance b WHERE b.address.id = :addressId")
    void deleteByAddressId(@Param("addressId") Long addressId);
}