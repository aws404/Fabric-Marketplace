package io.github.aws404.util.gui.hopper;

import io.github.aws404.util.gui.CustomHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class CustomHopperScreenHandler extends HopperScreenHandler implements CustomHandler {

    private final HopperBase guiBase;

    public CustomHopperScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, HopperBase guiBase) {
        super(syncId, playerInventory, inventory);
        this.guiBase = guiBase;
    }

    @Override
    public ItemStack onSlotClick(int slotNo, int button, SlotActionType actionType, PlayerEntity playerEntity) {
        guiBase.onClick(slotNo, actionType, button, (ServerPlayerEntity) playerEntity);
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean isNotRestricted(PlayerEntity player) {
        return true;
    }

    @Override
    public void tick(ServerPlayerEntity playerEntity) {
        guiBase.tick(playerEntity);
    }
}
