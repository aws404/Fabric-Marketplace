package io.github.aws404.market.currency.types;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.MarketRegistry;
import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.market.currency.SimpleCurrency;
import io.github.aws404.util.gui.GuiBase;
import io.github.aws404.util.gui.hopper.HopperBase;
import io.github.aws404.util.input.SignInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

public class ItemCurrency implements SimpleCurrency {

    private static final ItemStack DISPLAY = GuiBase.createItem(Items.APPLE, new LiteralText("Item"), null);
    public static final ItemStack RETURN = GuiBase.createItem(Items.BARRIER, new LiteralText("Return to List Menu"), null);
    public static final ItemStack NO_SELECTED = GuiBase.createItem(Items.GRAY_DYE, new LiteralText("No Item Selected"), Collections.singletonList(new LiteralText("- Click an item in your inventory below")));

    private final ItemStack stack;
    private final Builder builder;

    public ItemCurrency(Builder builder, ItemStack stack) {
        this.builder = builder;
        this.stack = stack.copy();
        this.stack.setCount(1);
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
    public MarketRegistry.CurrencyBuilder getBuilder() {
        return builder;
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

    public static class Builder extends MarketRegistry.CurrencyBuilder {

        @Override
        public ItemCurrency deSerialise(String serialised) {
            try {
                return new ItemCurrency(this, ItemStack.fromTag(StringNbtReader.parse(serialised)));
            } catch (CommandSyntaxException e) {
                MarketInstance.LOGGER.error("There was an error parsing the item tag '{}'", serialised);
                return new ItemCurrency(this, new ItemStack(Items.STONE));
            }
        }

        @Override
        public void createSelector(ServerPlayerEntity playerEntity, Consumer<CurrencyInstance> type) {
            new SelectItemScreen(playerEntity, type).open();
        }

        @Override
        public ItemStack getDisplayItem() {
            return DISPLAY;
        }

        public class SelectItemScreen extends HopperBase {

            private ItemStack selectedItem;
            private int amount = 1;

            public SelectItemScreen(ServerPlayerEntity player, Consumer<CurrencyInstance> type) {
                super(player, new LiteralText("Select an item in your inventory"));

                registerButton(0, new GuiBase.Button(player1 -> type.accept(null), RETURN));
                registerButton(2, new GuiBase.Button(playerEntity -> {
                    if (selectedItem != null) {
                        type.accept(new CurrencyInstance(new ItemCurrency(Builder.this, selectedItem), amount));
                    }
                }, () -> {
                    if (selectedItem == null) {
                        return NO_SELECTED;
                    } else {
                        ArrayList<Text> lore = GuiBase.getLore(selectedItem);
                        lore.add(new LiteralText("- Amount: " + amount));
                        lore.add(new LiteralText(""));
                        lore.add(new LiteralText("Click to Confirm").formatted(Formatting.GREEN));
                        return GuiBase.createItem(selectedItem.getItem(), selectedItem.getName(), lore, selectedItem.getDamage(), amount);
                    }
                }));
                registerButton(4, new GuiBase.Button(playerEntity -> SignInput.createSignNumberHandler("Item Amount", "", playerEntity, (playerEntity1, s) -> {
                    this.amount = Math.max(1, s.intValue());
                    this.open();
                }), () -> GuiBase.createItem(Items.ITEM_FRAME, new LiteralText("Amount"), Collections.singletonList(new LiteralText("- " + amount)))));
            }

            @Override
            public void onClick(int slot, SlotActionType actionType, int button, ServerPlayerEntity playerEntity) {
                if (slot >= 5) {
                    ItemStack stack = screenHandler.getSlot(slot).getStack().copy();
                    if (!stack.isEmpty()) {
                        stack.setCount(1);
                        selectedItem = stack;
                    }
                    generateButtons();
                } else {
                    super.onClick(slot, actionType, button, playerEntity);
                }
            }
        }
    }
}
