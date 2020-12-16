package io.github.aws404.market.guis;

import io.github.aws404.util.gui.container.ContainerBase;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.market.orders.MarketListing;
import io.github.aws404.market.tasks.CancelTask;
import io.github.aws404.market.tasks.ClaimTask;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;

public class ClaimingScreen extends ContainerBase {

    private static final ItemStack RETURN = createItem(Items.BARRIER, new LiteralText("Return to Personal Trades"), null);

    private final MarketListing order;
    private int tick = 0;

    public ClaimingScreen(ServerPlayerEntity player, MarketListing order) {
        super(player, new LiteralText("Trade Order"), 1);
        this.order = order;

        inventory.setStack(1, DIVIDER);
        registerButton(8, new Button(playerEntity -> new PersonalTrades(playerEntity, 1).open(), RETURN));

        ItemStack stack = order.getOutput().getDisplayItem();

        registerButton(0, new Button(playerEntity -> MarketInstance.MARKET_INSTANCE.send(new CancelTask(playerEntity, order)), () -> createItem(stack.getItem(), stack.getName(),
                Arrays.asList(
                        new LiteralText(""),
                        new LiteralText("Price: ").append(order.getInput().asText()),
                        new LiteralText(""),
                        order.isAvailable() ? new LiteralText("Click to Cancel Trade").formatted(Formatting.RED) : new LiteralText("Trade Completed!").formatted(Formatting.GREEN)
                )
        )));

        for (int i = 0; i < 6; i++) {
            int finalI = i;
            registerButton(2 + i, new Button(playerEntity -> {
                MarketInstance.MARKET_INSTANCE.send(new ClaimTask(playerEntity, order, finalI));
            }, () -> {
                ArrayList<CurrencyInstance> claimable = order.getClaimable();
                if (finalI >= claimable.size()) {
                    return ItemStack.EMPTY;
                } else {
                    return claimable.get(finalI).getDisplayItem();
                }
            }));
        }
    }

    @Override
    public void tick(ServerPlayerEntity server) {
        tick++;
        if (tick >= 20) {
            tick = 0;
            generateButtons();
        }
    }

    public void generateButtons() {
        order.refresh();
        super.generateButtons();
    }

}
