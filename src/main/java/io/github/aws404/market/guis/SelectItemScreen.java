package io.github.aws404.market.guis;

import io.github.aws404.market.currency.types.ItemCurrency;
import io.github.aws404.util.gui.GuiBase;
import io.github.aws404.util.gui.hopper.HopperBase;
import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.util.input.SignInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Collections;

public class SelectItemScreen extends HopperBase {

    public static final ItemStack RETURN = GuiBase.createItem(Items.BARRIER, new LiteralText("Return to List Menu"), null);
    public static final ItemStack NO_SELECTED = GuiBase.createItem(Items.GRAY_DYE, new LiteralText("No Item Selected"), Collections.singletonList(new LiteralText("- Click an item in your inventory below")));

    private ItemStack selectedItem;
    private int amount = 1;

    public SelectItemScreen(ServerPlayerEntity player, OrderListScreen screen, String type) {
        super(player, new LiteralText("Select an item in your inventory"));

        registerButton(0, new GuiBase.Button(player1 -> screen.open(), RETURN));
        registerButton(2, new GuiBase.Button(playerEntity -> {
            if (selectedItem != null) {
                switch (type) {
                    case "input":
                        screen.setInput(new CurrencyInstance(new ItemCurrency.Currency(selectedItem), amount));
                        break;
                    case "output":
                        screen.setOutput(new CurrencyInstance(new ItemCurrency.Currency(selectedItem), amount));
                        break;
                }
                playerEntity.closeHandledScreen();
                screen.open();
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
