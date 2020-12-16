package io.github.aws404.util.gui.hopper;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class CustomHopperScreenHandlerFactory implements NamedScreenHandlerFactory {

    private final HopperBase base;
    private final Text text;

    public CustomHopperScreenHandlerFactory(Text name, HopperBase base) {
        this.base = base;
        this.text = name;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new CustomHopperScreenHandler(syncId, inv, base.getInventory(), base);
    }

    @Override
    public Text getDisplayName() {
        return text;
    }
}
