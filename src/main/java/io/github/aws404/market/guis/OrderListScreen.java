package io.github.aws404.market.guis;

import io.github.aws404.market.currency.CurrencyInstance;

public interface OrderListScreen {
    int open();

    void setInput(CurrencyInstance currencyInstance);
    void setOutput(CurrencyInstance currencyInstance);
    CurrencyInstance getInput();
    CurrencyInstance getOutput();
}
