package io.github.aws404.market.currency.types;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.aws404.market.currency.SimpleCurrency;
import io.github.aws404.market.guis.OrderListScreen;
import io.github.aws404.util.gui.GuiBase;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.guis.ListSimpleOrderScreen;
import io.github.aws404.market.guis.SelectItemScreen;
import io.github.aws404.market.MarketRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class ItemCurrency extends MarketRegistry.CurrencyBuilder {

    private static final ItemStack DISPLAY = GuiBase.createItem(Items.APPLE, new LiteralText("Item"), null);

    @Override
    public Currency deSerialise(String serialised) {
        try {
            return new Currency(ItemStack.fromTag(StringNbtReader.parse(serialised)));
        } catch (CommandSyntaxException e) {
            MarketInstance.LOGGER.error("There was an error parsing the item tag '{}'", serialised);
            return new Currency(new ItemStack(Items.STONE));
        }
    }

    @Override
    public void createSelector(ServerPlayerEntity playerEntity, OrderListScreen screen, String type) {
        new SelectItemScreen(playerEntity, screen, type).open();
    }

    @Override
    public ItemStack getDisplayItem() {
        return DISPLAY;
    }

    public static class Currency implements SimpleCurrency {
        private final ItemStack stack;

        public Currency(ItemStack stack) {
            this.stack = stack.copy();
            stack.setCount(1);
        }

        @Override
        public ItemStack getDisplayItem(Number amount) {
            ItemStack dummy = stack.copy();
            dummy.setCount(amount.intValue());
            dummy.setCustomName(new LiteralText(amount.intValue() + " ").append(dummy.getName()).setStyle(Style.EMPTY.withItalic(false)));
            return dummy;
        }

        @Override
        public Integer get(ServerPlayerEntity player) {
            return player.inventory.remove(stack -> stack.isItemEqual(stack) && ItemStack.areTagsEqual(stack, stack), 0, player.playerScreenHandler.method_29281());
        }

        @Override
        public void give(ServerPlayerEntity player, Number amount) {
            ItemStack dummy = stack.copy();
            dummy.setCount(amount.intValue());
            player.giveItemStack(dummy);
        }

        @Override
        public void take(ServerPlayerEntity player, Number amount) {
            player.inventory.remove(stack -> stack.isItemEqual(stack) && ItemStack.areTagsEqual(stack, stack), amount.intValue(), player.playerScreenHandler.method_29281());
        }

        @Override
        public String serialise() {
            return stack.toTag(new CompoundTag()).asString();
        }

        @Override
        public Text asText(Number amount) {
            return new LiteralText(getNumberAsString(amount)).append(" ").append(stack.getName());
        }

        @Override
        public Text asText() {
            return stack.getName();
        }

        @Override
        public String getNumberAsString(Number amount) {
            return Integer.toString(amount.intValue());
        }

        @Override
        public String getCode() {
            return "item";
        }
    }
}
