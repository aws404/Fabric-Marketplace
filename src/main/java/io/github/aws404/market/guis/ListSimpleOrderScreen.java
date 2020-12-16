package io.github.aws404.market.guis;

import com.mojang.datafixers.util.Pair;
import io.github.aws404.util.gui.container.ContainerBase;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.market.guis.templates.SelectorScreen;
import io.github.aws404.market.orders.BuyOrder;
import io.github.aws404.market.MarketRegistry;
import io.github.aws404.market.tasks.ListTask;
import io.github.aws404.util.input.SignInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;

public class ListSimpleOrderScreen extends ContainerBase implements OrderListScreen {

    private static final ItemStack CONFIRM = createItem(Items.GREEN_DYE, new LiteralText("Confirm Trade"), null);
    private static final ItemStack NO_SELECTED_INPUT = createItem(Items.RED_STAINED_GLASS_PANE, new LiteralText("Select something to sell"), null);
    private static final ItemStack NO_SELECTED_OUTPUT = createItem(Items.RED_STAINED_GLASS_PANE, new LiteralText("Select the price"), null);

    public CurrencyInstance input = null;
    public CurrencyInstance output = null;
    private int maximum = 1;

    public ListSimpleOrderScreen(ServerPlayerEntity player) {
        super(player, new LiteralText("What would you like to sell?"), 3);

        registerButton(10, new Button(playerEntity -> {
            new SelectorScreen<>(player, "Currency", MarketRegistry.getCurrencyBuilders(), currencyBuilder -> currencyBuilder.createSelector(playerEntity, this, "input")).open();
        }, () -> {
            if (input == null) {
                return NO_SELECTED_INPUT;
            } else {
                ItemStack stack = input.getDisplayItem().copy();
                ArrayList<Text> lore = getLore(stack);
                lore.add(new LiteralText(""));
                lore.add(new LiteralText("Click to Change").formatted(Formatting.GRAY));
                Text name = new LiteralText("")
                        .append(new LiteralText("Selling ").formatted(Formatting.GOLD))
                        .append(input.asText());
                return createItem(stack.getItem(), name, lore);
            }
        }));

        registerButton(11, new Button(playerEntity -> SignInput.createSignNumberHandler("Transaction Count", Integer.toString(maximum), playerEntity, (playerEntity1, s) -> {
            this.maximum = s.intValue();
            open();
        }), () -> {
            ArrayList<Text> lore = new ArrayList<>();
            lore.add(createBulletOptionText(Formatting.YELLOW, Pair.of("Number of Transactions", Integer.toString(maximum))));
            lore.add(LiteralText.EMPTY);
            if (input != null) {
                lore.add(new LiteralText("")
                        .append(new LiteralText("- ").formatted(Formatting.YELLOW))
                        .append(new LiteralText("Total Input: ").formatted(Formatting.GRAY))
                        .append(new LiteralText(input.getType().getNumberAsString(input.getAmount().doubleValue() * maximum)).append(" ").append(input.getType().asText()).formatted(Formatting.WHITE))
                        .append(new LiteralText(" (")
                                .append(input.amountAsString())
                                .append(" x ")
                                .append(Integer.toString(maximum))
                                .append(" Transactions")
                                .append(")")
                        ).formatted(Formatting.GRAY)
                );
            } else {
                lore.add(createBulletText(Formatting.YELLOW, "No input set"));
            }
            if (output != null) {
                lore.add(new LiteralText("")
                        .append(new LiteralText("- ").formatted(Formatting.YELLOW))
                        .append(new LiteralText("Total Price: ").formatted(Formatting.GRAY))
                        .append(new LiteralText(output.getType().getNumberAsString(output.getAmount().doubleValue() * maximum)).append(" ").append(output.getType().asText()).formatted(Formatting.WHITE))
                        .append(new LiteralText(" (")
                                .append(output.amountAsString())
                                .append(" x ")
                                .append(Integer.toString(maximum))
                                .append(" Transactions")
                                .append(")")
                        ).formatted(Formatting.GRAY)
                );
            } else {
                lore.add(createBulletText(Formatting.YELLOW, "No price set"));
            }
            lore.add(LiteralText.EMPTY);
            lore.add(new LiteralText("Click to Change Transaction Count").formatted(Formatting.GRAY));
            return createItem(Items.ITEM_FRAME, new LiteralText("Transaction Information").formatted(Formatting.YELLOW), lore);
        }));

        registerButton(12, new Button(playerEntity -> {
            new SelectorScreen<>(player, "Currency", MarketRegistry.getCurrencyBuilders(), currencyBuilder -> currencyBuilder.createSelector(playerEntity, this, "output")).open();
        }, () -> {
            if (output == null) {
                return NO_SELECTED_OUTPUT;
            } else {
                ItemStack stack = output.getDisplayItem().copy();
                ArrayList<Text> lore = getLore(stack);
                lore.add(new LiteralText(""));
                lore.add(new LiteralText("Click to Change").formatted(Formatting.GRAY));
                Text name = new LiteralText("")
                        .append(new LiteralText("Price ").formatted(Formatting.GOLD))
                        .append(output.asText());
                return createItem(stack.getItem(), name, lore);
            }
        }));

        registerButton(15, new Button(playerEntity -> {
            if (input != null && output != null) {
                listItem(playerEntity);
            }
        }, () -> {
            if (input != null && output != null) {
                return CONFIRM;
            } else {
                ArrayList<Text> lore = new ArrayList<>();
                if (input == null) lore.add(createBulletText(Formatting.RED, "Order Input"));
                if (output == null) lore.add(createBulletText(Formatting.RED, "Order Price"));

                lore.add(new LiteralText(""));
                lore.add(new LiteralText("All required information"));
                lore.add(new LiteralText("must be set before proceeding."));

                return createItem(Items.GRAY_DYE, new LiteralText("Missing Information").formatted(Formatting.RED), lore);
            }
        }));
    }

    public void listItem(ServerPlayerEntity playerEntity) {
        if (input.getType().get(playerEntity).doubleValue() >= input.getAmount().doubleValue() * maximum) {
            playerEntity.closeHandledScreen();
            BuyOrder order = BuyOrder.createUnlisted(playerEntity, output, input, maximum);
            MarketInstance.MARKET_INSTANCE.send(new ListTask(playerEntity, order));
            new PersonalTrades(playerEntity, 1).open();
        } else {
            playerEntity.sendMessage(new LiteralText("You only have ")
                    .append(input.getType().getNumberAsString(input.getType().get(playerEntity)))
                    .append(" ")
                    .append(input.getType().asText())
                    .append(", needed ")
                    .append(input.getType().getNumberAsString(input.getAmount().doubleValue() * maximum))
                    .append(".")
                    .formatted(Formatting.RED)
            , false);
        }
    }

    @Override
    public void setInput(CurrencyInstance currencyInstance) {
        input = currencyInstance;
    }

    @Override
    public void setOutput(CurrencyInstance currencyInstance) {
        output = currencyInstance;
    }

    @Override
    public CurrencyInstance getInput() {
        return input;
    }

    @Override
    public CurrencyInstance getOutput() {
        return output;
    }
}
