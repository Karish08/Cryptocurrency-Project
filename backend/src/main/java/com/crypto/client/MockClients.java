package com.crypto.client;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
class MockBlockchainInfoClient implements BlockchainInfoClient {
    @Override
    public BigDecimal getBalance(String address) {
        return BigDecimal.ZERO;
    }
}

@Component
class MockCoinGeckoClient implements CoinGeckoClient {
    @Override
    public BigDecimal getPrice(String blockchain) {
        return BigDecimal.ONE;
    }
}

@Component
class MockEtherscanClient implements EtherscanClient {
}
