package com.crypto.service.impl;

import com.crypto.model.Address;
import com.crypto.model.AddressBalance;
import com.crypto.model.Transaction;
import com.crypto.repository.AddressBalanceRepository;
import com.crypto.repository.AddressRepository;
import com.crypto.repository.TransactionRepository;
import com.crypto.service.BlockchainService;
import com.crypto.client.EtherscanClient;
import com.crypto.client.BlockchainInfoClient;
import com.crypto.client.CoinGeckoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlockchainServiceImpl implements BlockchainService {
    
    private final AddressBalanceRepository balanceRepository;
    private final TransactionRepository transactionRepository;
    private final AddressRepository addressRepository;
    private final EtherscanClient etherscanClient;
    private final BlockchainInfoClient blockchainInfoClient;
    private final CoinGeckoClient coinGeckoClient;
    
    private static final Map<String, String> RPC_ENDPOINTS = new HashMap<>() {{
        put("ethereum", "https://mainnet.infura.io/v3/YOUR-PROJECT-ID");
        put("binance", "https://bsc-dataseed.binance.org/");
        put("polygon", "https://polygon-rpc.com");
        put("avalanche", "https://api.avax.network/ext/bc/C/rpc");
        put("arbitrum", "https://arb1.arbitrum.io/rpc");
        put("optimism", "https://mainnet.optimism.io");
    }};
    
    @Override
    @Async
    public CompletableFuture<Void> fetchAddressBalanceAsync(Address address) {
        return CompletableFuture.runAsync(() -> fetchAddressBalance(address));
    }
    
    @Override
    @Transactional
    public void fetchAddressBalance(Address address) {
        try {
            log.info("Fetching balance for address: {} on {}", address.getAddress(), address.getBlockchain());
            
            BigDecimal balance = BigDecimal.ZERO;
            BigDecimal priceUsd = getPriceUsd(address.getBlockchain());
            
            switch (address.getBlockchain().toLowerCase()) {
                case "ethereum":
                    balance = fetchEthereumBalance(address.getAddress());
                    break;
                case "bitcoin":
                    balance = fetchBitcoinBalance(address.getAddress());
                    break;
                case "binance":
                    balance = fetchBscBalance(address.getAddress());
                    break;
                case "polygon":
                    balance = fetchPolygonBalance(address.getAddress());
                    break;
                case "solana":
                    balance = fetchSolanaBalance(address.getAddress());
                    break;
                default:
                    log.warn("Unsupported blockchain: {}", address.getBlockchain());
                    return;
            }
            
            // Save or update native balance
            saveBalance(address, "NATIVE", null, balance, priceUsd);
            
            // Update address totals
            address.setTotalBalance(balance);
            address.setTotalBalanceUsd(balance.multiply(priceUsd));
            address.setLastBalanceCheck(LocalDateTime.now());
            addressRepository.save(address);
            
            log.info("Updated balance for address {}: {} {} (${})", 
                    address.getAddress(), balance, address.getBlockchain(), 
                    balance.multiply(priceUsd));
            
            // Fetch token balances for EVM chains
            if (isEvmChain(address.getBlockchain())) {
                fetchTokenBalances(address);
            }
            
            // Fetch recent transactions
            fetchRecentTransactions(address);
            
        } catch (Exception e) {
            log.error("Error fetching balance for address {}: {}", address.getAddress(), e.getMessage());
        }
    }
    
    @Override
    public void fetchTokenBalances(Address address) {
        // Implementation for ERC20 tokens
        log.info("Fetching token balances for address: {}", address.getAddress());
    }
    
    @Override
    public void fetchRecentTransactions(Address address) {
        // Implementation for recent transactions
        log.info("Fetching recent transactions for address: {}", address.getAddress());
    }
    
    @Override
    @Cacheable(value = "prices", key = "#blockchain")
    public BigDecimal getPriceUsd(String blockchain) {
        try {
            return coinGeckoClient.getPrice(blockchain);
        } catch (Exception e) {
            log.error("Error fetching price for {}: {}", blockchain, e.getMessage());
            // Return default prices
            Map<String, BigDecimal> defaultPrices = new HashMap<>() {{
                put("ethereum", BigDecimal.valueOf(2000));
                put("bitcoin", BigDecimal.valueOf(40000));
                put("binance", BigDecimal.valueOf(300));
                put("polygon", BigDecimal.valueOf(0.8));
                put("solana", BigDecimal.valueOf(60));
                put("avalanche", BigDecimal.valueOf(15));
            }};
            return defaultPrices.getOrDefault(blockchain.toLowerCase(), BigDecimal.ONE);
        }
    }
    
    private BigDecimal fetchEthereumBalance(String address) {
        try {
            Web3j web3j = Web3j.build(new HttpService(RPC_ENDPOINTS.get("ethereum")));
            EthGetBalance ethBalance = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .send();
            
            BigInteger balanceWei = ethBalance.getBalance();
            return Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER);
            
        } catch (Exception e) {
            log.error("Error fetching Ethereum balance: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    private BigDecimal fetchBitcoinBalance(String address) {
        try {
            return blockchainInfoClient.getBalance(address);
        } catch (Exception e) {
            log.error("Error fetching Bitcoin balance: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    private BigDecimal fetchBscBalance(String address) {
        try {
            Web3j web3j = Web3j.build(new HttpService(RPC_ENDPOINTS.get("binance")));
            EthGetBalance balance = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .send();
            
            BigInteger balanceWei = balance.getBalance();
            return Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER);
            
        } catch (Exception e) {
            log.error("Error fetching BSC balance: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    private BigDecimal fetchPolygonBalance(String address) {
        try {
            Web3j web3j = Web3j.build(new HttpService(RPC_ENDPOINTS.get("polygon")));
            EthGetBalance balance = web3j
                    .ethGetBalance(address, DefaultBlockParameterName.LATEST)
                    .send();
            
            BigInteger balanceWei = balance.getBalance();
            return Convert.fromWei(balanceWei.toString(), Convert.Unit.ETHER);
            
        } catch (Exception e) {
            log.error("Error fetching Polygon balance: {}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    private BigDecimal fetchSolanaBalance(String address) {
        // Implement Solana balance fetch
        return BigDecimal.ZERO;
    }
    
    private boolean isEvmChain(String blockchain) {
        return List.of("ethereum", "binance", "polygon", "avalanche", "arbitrum", "optimism")
                .contains(blockchain.toLowerCase());
    }
    
    private void saveBalance(Address address, String tokenSymbol, String tokenAddress, 
                            BigDecimal balance, BigDecimal priceUsd) {
        AddressBalance balanceEntity = balanceRepository
                .findByAddressIdAndTokenSymbolAndTokenAddress(address.getId(), tokenSymbol, tokenAddress)
                .orElse(AddressBalance.builder()
                        .address(address)
                        .tokenSymbol(tokenSymbol)
                        .tokenAddress(tokenAddress)
                        .build());
        
        balanceEntity.setBalance(balance);
        balanceEntity.setBalanceUsd(balance.multiply(priceUsd));
        balanceEntity.setLastUpdated(LocalDateTime.now());
        
        // Calculate BTC and ETH equivalents
        BigDecimal btcPrice = getPriceUsd("bitcoin");
        BigDecimal ethPrice = getPriceUsd("ethereum");
        
        if (btcPrice.compareTo(BigDecimal.ZERO) > 0) {
            balanceEntity.setBalanceBtc(balance.multiply(priceUsd).divide(btcPrice, 8, RoundingMode.HALF_UP));
        }
        
        if (ethPrice.compareTo(BigDecimal.ZERO) > 0) {
            balanceEntity.setBalanceEth(balance.multiply(priceUsd).divide(ethPrice, 8, RoundingMode.HALF_UP));
        }
        
        balanceRepository.save(balanceEntity);
    }
}