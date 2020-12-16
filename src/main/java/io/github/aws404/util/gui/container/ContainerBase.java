package io.github.aws404.util.gui.container;

import io.github.aws404.util.gui.GuiBase;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public abstract class ContainerBase extends GuiBase {
    protected static final ItemStack DIVIDER = createItem(Items.GRAY_STAINED_GLASS_PANE, new LiteralText(""), null);

    private final CustomContainerScreenHandlerFactory screenHandlerFactory;

    public ContainerBase(ServerPlayerEntity player, Text title, int rows, DefaultedList<ItemStack> items) {
        super(player, new SimpleInventory(items.toArray(new ItemStack[0])));
        screenHandlerFactory = new CustomContainerScreenHandlerFactory(title, this, rows);
    }

    public ContainerBase(ServerPlayerEntity player, Text title, int rows) {
        super(player, new SimpleInventory(rows * 9));
        screenHandlerFactory = new CustomContainerScreenHandlerFactory(title, this, rows);
    }

    public NamedScreenHandlerFactory getScreenHandlerFactory() {
        return screenHandlerFactory;
    }
}
