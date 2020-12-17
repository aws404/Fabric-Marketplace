package io.github.aws404.market.currency;

import io.github.aws404.market.MarketRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public interface SimpleCurrency {
    ItemStack getDisplayItem(Number amount);

    Number get(ServerPlayerEntity player);

    void give(ServerPlayerEntity player, Number amount);

    void take(ServerPlayerEntity player, Number amount);

    Text asText(Number amount);

    Text asText();

    String getNumberAsString(Number amount);

    String serialise();

    MarketRegistry.CurrencyBuilder getBuilder();
}
