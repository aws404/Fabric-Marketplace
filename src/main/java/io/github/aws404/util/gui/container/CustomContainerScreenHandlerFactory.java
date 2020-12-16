package io.github.aws404.util.gui.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;

public class CustomContainerScreenHandlerFactory implements NamedScreenHandlerFactory {
    public final Text name;
    private final int rows;
    private final ScreenHandlerType<?> type;
    public final ContainerBase guiType;

    public CustomContainerScreenHandlerFactory(Text name, ContainerBase gui, int rows) {
        this.name = name;
        this.guiType = gui;
        int rows1 = rows;
        switch (rows) {
            case 1:
                type = ScreenHandlerType.GENERIC_9X1;
                break;
            case 2:
                type = ScreenHandlerType.GENERIC_9X2;
                break;
            case 3:
                type = ScreenHandlerType.GENERIC_9X3;
                break;
            case 4:
                type = ScreenHandlerType.GENERIC_9X4;
                break;
            case 5:
                type = ScreenHandlerType.GENERIC_9X5;
                break;
            case 6:
                type = ScreenHandlerType.GENERIC_9X6;
                break;
            default:
                rows1 = 3;
                type = ScreenHandlerType.GENERIC_9X3;
        }
        this.rows = rows1;
    }

    public Text getDisplayName() {
        return this.name;
    }

    public ScreenHandler createMenu(int i, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new CustomContainerScreenHandler(guiType, type, i, playerInventory, guiType.getInventory(), rows);
    }
}
