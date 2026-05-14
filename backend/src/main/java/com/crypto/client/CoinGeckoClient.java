package com.crypto.client;

import java.math.BigDecimal;

public interface CoinGeckoClient {
    BigDecimal getPrice(String blockchain);
}
