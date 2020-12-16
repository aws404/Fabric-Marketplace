package io.github.aws404.market.guis.templates;

import io.github.aws404.util.gui.container.ContainerBase;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.List;
import java.util.function.Consumer;

public class SelectorScreen<T extends GuiSelectable> extends ContainerBase {

    private final List<T> options;
    private final Consumer<T> action;

    public SelectorScreen(ServerPlayerEntity player, String type, List<T> options, Consumer<T> action) {
        super(player, new LiteralText("Select: ").append(type), getRows(options));

        this.options = options;
        this.action = action;

        if (options.size() == 1) {
            action.accept(options.get(0));
        } else {
            for (int i = 0; i < options.size(); i++) {
                inventory.setStack(i, options.get(i).getDisplayItem());
            }
        }
    }

    private static int getRows(List<? extends GuiSelectable> options) {
        return (int) Math.ceil(options.size() / 9f);
    }

    @Override
    public int open() {
        if (options.size() == 1) {
            action.accept(options.get(0));
            return -1;
        } else {
            return super.open();
        }
    }

    @Override
    public void onClick(int slot, SlotActionType actionType, int button, ServerPlayerEntity playerEntity) {
        if (options.size() > slot) {
            action.accept(options.get(slot));
        }
    }
}
