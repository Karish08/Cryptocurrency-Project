package com.crypto.scheduler;

import com.crypto.model.Address;
import com.crypto.repository.AddressRepository;
import com.crypto.service.BlockchainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceUpdateScheduler {
    
    private final AddressRepository addressRepository;
    private final BlockchainService blockchainService;
    
    // Update balances every hour
    @Scheduled(fixedRate = 3600000)
    public void updateAllBalances() {
        log.info("Starting scheduled balance update for all addresses");
        
        List<Address> allAddresses = addressRepository.findAll();
        int updated = 0;
        
        for (Address address : allAddresses) {
            try {
                blockchainService.fetchAddressBalance(address);
                updated++;
                
                // Add small delay to avoid rate limiting
                Thread.sleep(1000);
                
            } catch (Exception e) {
                log.error("Failed to update balance for address {}: {}", 
                        address.getAddress(), e.getMessage());
            }
        }
        
        log.info("Completed balance update for {} out of {} addresses", 
                updated, allAddresses.size());
    }
}