package io.github.aws404.market.guis;

import com.mojang.datafixers.util.Pair;
import io.github.aws404.util.gui.container.ContainerBase;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.orders.MarketListing;
import io.github.aws404.market.tasks.ListTask;
import io.github.aws404.util.input.SignInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;

public class ConfirmationScreen extends ContainerBase {
    private static final ItemStack CONFIRM = createItem(Items.GREEN_DYE, new LiteralText("Confirm Trade").formatted(Formatting.GREEN), null);
    private static final ItemStack DENY = createItem(Items.RED_DYE, new LiteralText("Deny Trade").formatted(Formatting.RED), null);

    public int transactions = 1;

    public ConfirmationScreen(ServerPlayerEntity purchaser, MarketListing order) {
        super(purchaser, new LiteralText(order.getSeller().getName() + "          " + purchaser.getGameProfile().getName()), 3);

        buttons.put(4, new Button(playerEntity -> {
        }, DIVIDER));
        buttons.put(13, new Button(playerEntity -> {
            SignInput.createSignNumberHandler("Max. Transactions", Integer.toString(transactions), playerEntity, (playerEntity1, number) -> {
                this.transactions = number.intValue();
                this.open();
            });
        }, () -> {
            ArrayList<Text> lore = new ArrayList<>();
            lore.add(createBulletOptionText(Formatting.YELLOW, Pair.of("Number of Transactions", Integer.toString(transactions))));
            lore.add(LiteralText.EMPTY);
            lore.add(new LiteralText("")
                    .append(new LiteralText("- ").formatted(Formatting.YELLOW))
                    .append(new LiteralText("Total Input: ").formatted(Formatting.GRAY))
                    .append(new LiteralText(order.getOutput().getType().getNumberAsString(order.getOutput().getAmount().doubleValue() * transactions)).append(" ").append(order.getOutput().getType().asText()).formatted(Formatting.WHITE))
                    .append(new LiteralText(" (")
                            .append(order.getOutput().amountAsString())
                            .append(" x ")
                            .append(Integer.toString(transactions))
                            .append(" Transactions")
                            .append(")")
                    ).formatted(Formatting.GRAY)
            );
            lore.add(new LiteralText("")
                    .append(new LiteralText("- ").formatted(Formatting.YELLOW))
                    .append(new LiteralText("Total Price: ").formatted(Formatting.GRAY))
                    .append(new LiteralText(order.getInput().getType().getNumberAsString(order.getInput().getAmount().doubleValue() * transactions)).append(" ").append(order.getInput().getType().asText()).formatted(Formatting.WHITE))
                    .append(new LiteralText(" (")
                            .append(order.getInput().amountAsString())
                            .append(" x ")
                            .append(Integer.toString(transactions))
                            .append(" Transactions")
                            .append(")")
                    ).formatted(Formatting.GRAY)
            );

            lore.add(LiteralText.EMPTY);
            lore.add(new LiteralText("Click to Change Transaction Count").formatted(Formatting.GRAY));
            return createItem(Items.ITEM_FRAME, new LiteralText("Transaction Information").formatted(Formatting.YELLOW), lore);
        }));
        buttons.put(22, new Button(playerEntity -> {
        }, DIVIDER));

        buttons.put(11, new Button(playerEntity -> {
        }, () -> {
            ItemStack display = order.getOutput().getDisplayItem();
            return createItem(display.getItem(), order.getOutput().asText(), getLore(display));
        }));
        buttons.put(15, new Button(playerEntity -> {
        }, () -> {
            ItemStack display = order.getInput().getDisplayItem();
            return createItem(display.getItem(), order.getInput().asText(), getLore(display));
        }));

        buttons.put(18, new Button(playerEntity -> {
            playerEntity.closeHandledScreen();
            new MainMenu(player, 1).open();
        }, DENY));
        buttons.put(26, new Button(playerEntity -> {
            if (order.getInput().getType().get(playerEntity).doubleValue() >= order.getInput().getAmount().doubleValue() * transactions) {
                MarketListing counterOrder = order.createCounterOrder(this);
                MarketInstance.MARKET_INSTANCE.send(new ListTask(playerEntity, counterOrder));
                new PersonalTrades(playerEntity, 1).open();
            } else {
                playerEntity.sendMessage(new LiteralText("You only have ")
                                .append(order.getInput().getType().getNumberAsString(order.getInput().getType().get(playerEntity)))
                                .append(" ")
                                .append(order.getInput().getType().asText())
                                .append(", needed ")
                                .append(order.getInput().getType().getNumberAsString(order.getInput().getAmount().doubleValue() * transactions))
                                .append(".")
                                .formatted(Formatting.RED)
                        , false);
            }
        }, CONFIRM));
    }
}
