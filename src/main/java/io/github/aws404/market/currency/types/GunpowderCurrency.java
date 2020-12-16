package io.github.aws404.market.currency.types;

import io.github.aws404.market.MarketRegistry;
import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.market.currency.SimpleCurrency;
import io.github.aws404.market.guis.OrderListScreen;
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

public class GunpowderCurrency implements SimpleCurrency {

    public static final GunpowderCurrency INSTANCE = new GunpowderCurrency();

    private static final ItemStack DISPLAY = GuiBase.createItem(Items.GOLD_NUGGET, new LiteralText("Dollars"), null);
    private BalanceHandler handler;

    private GunpowderCurrency() {
    }

    public static class Builder extends MarketRegistry.CurrencyBuilder {

        @Override
        public void createSelector(ServerPlayerEntity playerEntity, OrderListScreen screen, String type) {
            SignInput.createSignNumberHandler("Dollars ($)", "", playerEntity, (playerEntity1, amount) -> {
                switch (type) {
                    case "output":
                        screen.setOutput(new CurrencyInstance(INSTANCE, amount));
                        break;
                    case "input":
                        screen.setInput(new CurrencyInstance(INSTANCE, amount));
                        break;
                }
                screen.open();
            });
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
    public String getCode() {
        return "gunpowder";
    }

    private void checkHandler() {
        if (handler == null) {
            handler = GunpowderMod.getInstance().getRegistry().getModelHandler(BalanceHandler.class);
        }
    }
}
