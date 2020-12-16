package io.github.aws404.util.gui.container;

import io.github.aws404.util.gui.CustomHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class CustomContainerScreenHandler extends GenericContainerScreenHandler implements CustomHandler {

    public final ContainerBase guiBase;

    public CustomContainerScreenHandler(ContainerBase guiBase, ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, Inventory inventory, int rows) {
        super(type, syncId, playerInventory, inventory, rows);
        this.guiBase = guiBase;
    }

    @Override
    public ItemStack onSlotClick(int slotNo, int button, SlotActionType actionType, PlayerEntity playerEntity) {
        guiBase.onClick(slotNo, actionType, button, (ServerPlayerEntity) playerEntity);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isNotRestricted(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void tick(ServerPlayerEntity playerEntity) {
        guiBase.tick(playerEntity);
    }
}
