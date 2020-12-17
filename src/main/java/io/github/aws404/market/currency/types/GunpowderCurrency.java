package io.github.aws404.market.currency.types;

import io.github.aws404.market.MarketRegistry;
import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.market.currency.SimpleCurrency;
import io.github.aws404.util.gui.GuiBase;
import io.github.aws404.util.input.SignInput;
import io.github.gunpowder.api.GunpowderMod;
import io.github.gunpowder.api.module.currency.dataholders.StoredBalance;
import io.github.gunpowder.api.module.currency.modelhandlers.BalanceHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.math.BigDecimal;
import java.util.function.Consumer;

public class GunpowderCurrency implements SimpleCurrency {

    private static final ItemStack DISPLAY = GuiBase.createItem(Items.GOLD_NUGGET, new LiteralText("Dollars"), null);

    private BalanceHandler handler;
    private final Builder builder;

    private GunpowderCurrency(Builder builder) {
        this.builder = builder;
    }

    @Override
    public ItemStack getDisplayItem(Number amount) {
        ItemStack display = DISPLAY.copy();
        display.setCustomName(new LiteralText("$" + amount.doubleValue()));
        return display;
    }

    @Override
    public Number get(ServerPlayerEntity player) {
        checkHandler();
        return handler.getUser(player.getUuid()).getBalance();
    }

    @Override
    public void give(ServerPlayerEntity player, Number amount) {
        checkHandler();
        StoredBalance balance = handler.getUser(player.getUuid());
        balance.setBalance(balance.getBalance().add(BigDecimal.valueOf(amount.doubleValue())));
    }

    @Override
    public void take(ServerPlayerEntity player, Number amount) {
        checkHandler();
        StoredBalance balance = handler.getUser(player.getUuid());
        balance.setBalance(balance.getBalance().subtract(BigDecimal.valueOf(amount.doubleValue())));
    }

    @Override
    public Text asText(Number amount) {
        return new LiteralText("$" + amount);
    }

    @Override
    public Text asText() {
        return new LiteralText("Dollars");
    }

    @Override
    public String getNumberAsString(Number amount) {
        return Double.toString(amount.doubleValue());
    }

    @Override
    public String serialise() {
        return "";
    }

    @Override
    public MarketRegistry.CurrencyBuilder getBuilder() {
        return builder;
    }

    private void checkHandler() {
        if (handler == null) {
            handler = GunpowderMod.getInstance().getRegistry().getModelHandler(BalanceHandler.class);
        }
    }

    public static class Builder extends MarketRegistry.CurrencyBuilder {

        public final GunpowderCurrency INSTANCE = new GunpowderCurrency(this);

        @Override
        public void createSelector(ServerPlayerEntity playerEntity, Consumer<CurrencyInstance> type) {
            SignInput.createSignNumberHandler("Dollars ($)", "", playerEntity, (playerEntity1, amount) -> type.accept(new CurrencyInstance(INSTANCE, amount)));
        }

        @Override
        public ItemStack getDisplayItem() {
            return DISPLAY;
        }

        @Override
        public SimpleCurrency deSerialise(String serialised) {
            return INSTANCE;
        }

    }
}
