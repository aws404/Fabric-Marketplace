package io.github.aws404.market.currency.types;

import io.github.aws404.market.MarketRegistry;
import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.market.currency.SimpleCurrency;
import io.github.aws404.market.guis.ListSimpleOrderScreen;
import io.github.aws404.market.guis.OrderListScreen;
import io.github.aws404.util.gui.GuiBase;
import io.github.aws404.util.input.SignInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ExperienceLevelsCurrency extends MarketRegistry.CurrencyBuilder {

    private static final ItemStack DISPLAY = GuiBase.createItem(Items.EXPERIENCE_BOTTLE, new LiteralText("Experience Levels").formatted(Formatting.GREEN), null);
    private static final Currency INSTANCE = new Currency();

    @Override
    public SimpleCurrency deSerialise(String serialised) {
        return INSTANCE;
    }

    @Override
    public void createSelector(ServerPlayerEntity playerEntity, OrderListScreen screen, String type) {
        SignInput.createSignNumberHandler("Experience Levels", "", playerEntity, (playerEntity1, amount) -> {
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

    public static class Currency implements SimpleCurrency {

        @Override
        public ItemStack getDisplayItem(Number amount) {
            ItemStack stack = DISPLAY.copy();
            stack.setCustomName(asText(amount));
            return stack;
        }

        @Override
        public Number get(ServerPlayerEntity player) {
            return player.experienceLevel;
        }

        @Override
        public void give(ServerPlayerEntity player, Number amount) {
            player.addExperienceLevels(amount.intValue());
        }

        @Override
        public void take(ServerPlayerEntity player, Number amount) {
            player.addExperienceLevels(-amount.intValue());
        }

        @Override
        public Text asText(Number amount) {
            return new LiteralText(getNumberAsString(amount) + " Experience Levels");
        }

        @Override
        public Text asText() {
            return new LiteralText("Experience Levels");
        }

        @Override
        public String getNumberAsString(Number amount) {
            return Integer.toString(amount.intValue());
        }

        @Override
        public String serialise() {
            return "";
        }

        @Override
        public String getCode() {
            return "experience_levels";
        }
    }
}
