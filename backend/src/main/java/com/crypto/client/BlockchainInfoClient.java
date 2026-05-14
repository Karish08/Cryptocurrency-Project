package com.crypto.client;

import java.math.BigDecimal;

public interface BlockchainInfoClient {
    BigDecimal getBalance(String address);
}
