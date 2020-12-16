package io.github.aws404.market.currency.types;

import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.market.currency.SimpleCurrency;
import io.github.aws404.market.guis.OrderListScreen;
import io.github.aws404.util.gui.GuiBase;
import io.github.aws404.market.guis.ListSimpleOrderScreen;
import io.github.aws404.market.MarketRegistry;
import io.github.aws404.util.input.SignInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class EmeraldCurrency extends MarketRegistry.CurrencyBuilder {
    public static final Currency INSTANCE = new Currency();

    private static final Text NAME = new LiteralText("Emeralds").formatted(Formatting.GREEN);
    private static final ItemStack DISPLAY_ITEM = GuiBase.createItem(Items.EMERALD, NAME, null);

    @Override
    public Currency deSerialise(String serialised) {
        return INSTANCE;
    }

    @Override
    public void createSelector(ServerPlayerEntity playerEntity, OrderListScreen screen, String type) {
        SignInput.createSignNumberHandler("Emeralds", "", playerEntity, (playerEntity1, amount) -> {
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
        return DISPLAY_ITEM;
    }

    public static class Currency implements SimpleCurrency {

        @Override
        public ItemStack getDisplayItem(Number amount) {
            ItemStack dummy = DISPLAY_ITEM.copy();
            dummy.setCount(amount.intValue());
            dummy.setCustomName(new LiteralText(amount.intValue() + " ").append(NAME).formatted(Formatting.GREEN).setStyle(Style.EMPTY.withItalic(false)));
            return dummy;
        }

        @Override
        public Integer get(ServerPlayerEntity player) {
            return player.inventory.remove(stack -> stack.getItem() == Items.EMERALD, 0, player.playerScreenHandler.method_29281());
        }

        @Override
        public void give(ServerPlayerEntity player, Number amount) {
            player.giveItemStack(new ItemStack(Items.EMERALD, amount.intValue()));
        }

        @Override
        public void take(ServerPlayerEntity player, Number amount) {
            player.inventory.remove(stack -> stack.getItem() == Items.EMERALD, amount.intValue(), player.playerScreenHandler.method_29281());
        }

        @Override
        public String serialise() {
            return "";
        }

        @Override
        public Text asText(Number amount) {
            return new LiteralText(getNumberAsString(amount) + " Emeralds").formatted(Formatting.GREEN);
        }

        @Override
        public Text asText() {
            return NAME;
        }

        @Override
        public String getNumberAsString(Number amount) {
            return Integer.toString(amount.intValue());
        }

        @Override
        public String getCode() {
            return "emeralds";
        }
    }

}
