package io.github.aws404.util.gui.hopper;

import io.github.aws404.util.gui.GuiBase;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public abstract class HopperBase extends GuiBase {

    public final NamedScreenHandlerFactory factory;

    public HopperBase(ServerPlayerEntity player, Text name) {
        super(player, new SimpleInventory(5));
        factory = new CustomHopperScreenHandlerFactory(name, this);
    }

    public NamedScreenHandlerFactory getScreenHandlerFactory() {
        return factory;
    }
}
