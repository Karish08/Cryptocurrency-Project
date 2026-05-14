package com.crypto.repository;

import com.crypto.model.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    Page<Address> findByUserId(Long userId, Pageable pageable);
    Page<Address> findByUserIdAndFavorite(Long userId, Boolean favorite, Pageable pageable);
    Page<Address> findByUserIdAndBlockchain(Long userId, String blockchain, Pageable pageable);
    Page<Address> findByUserIdAndBlockchainAndFavorite(Long userId, String blockchain, Boolean favorite, Pageable pageable);
    
    Optional<Address> findByIdAndUserId(Long id, Long userId);
    
    Optional<Address> findByAddressAndBlockchainAndUserId(String address, String blockchain, Long userId);
    
    List<Address> findByUserIdAndFavoriteTrue(Long userId);
    
    @Query("SELECT a FROM Address a JOIN a.categories c WHERE c.id = :categoryId AND a.user.id = :userId")
    List<Address> findByCategoryIdAndUserId(@Param("categoryId") Long categoryId, @Param("userId") Long userId);
    
    @Query("SELECT a FROM Address a JOIN a.categories c WHERE c.id = :categoryId AND a.user.id = :userId")
    Page<Address> findByCategoryIdAndUserId(@Param("categoryId") Long categoryId, @Param("userId") Long userId, Pageable pageable);
    
    long countByUserIdAndFavoriteTrue(Long userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND " +
           "(LOWER(a.address) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.label) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.notes) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Address> searchAddresses(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    long countByUserIdAndVerifiedTrue(Long userId);

    @Query("SELECT SUM(a.totalBalanceUsd) FROM Address a WHERE a.user.id = :userId")
    Optional<java.math.BigDecimal> sumTotalBalanceUsdByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(a.totalBalance) FROM Address a WHERE a.user.id = :userId AND a.blockchain = 'Bitcoin'")
    Optional<java.math.BigDecimal> sumTotalBalanceBtcByUserId(@Param("userId") Long userId);

    @Query("SELECT a.blockchain, COUNT(a) FROM Address a WHERE a.user.id = :userId GROUP BY a.blockchain")
    List<Object[]> getBlockchainDistribution(@Param("userId") Long userId);

    @Query("SELECT c.name, SUM(a.totalBalanceUsd) FROM Address a JOIN a.categories c WHERE a.user.id = :userId GROUP BY c.name")
    List<Object[]> getBalanceDistributionByCategory(@Param("userId") Long userId);

    List<Address> findTop5ByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT SUM(a.totalBalanceUsd) FROM Address a WHERE a.user.id = :userId AND a.createdAt BETWEEN :startDate AND :endDate")
    Optional<java.math.BigDecimal> sumTotalBalanceUsdByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
}