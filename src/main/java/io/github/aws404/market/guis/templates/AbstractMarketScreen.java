package io.github.aws404.market.guis.templates;

import com.mojang.datafixers.util.Pair;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.MarketRegistry;
import io.github.aws404.market.guis.ClaimingScreen;
import io.github.aws404.market.guis.ConfirmationScreen;
import io.github.aws404.market.guis.MainMenu;
import io.github.aws404.market.guis.SearchScreen;
import io.github.aws404.market.orders.MarketListing;
import io.github.aws404.market.tasks.CancelTask;
import io.github.aws404.util.gui.container.ContainerBase;
import io.github.aws404.util.input.SignInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractMarketScreen extends ContainerBase {
    protected static final ItemStack LIST_ITEM = createItem(Items.OAK_SIGN, new LiteralText("List an Item"), null);
    protected static final ItemStack SEARCH = createItem(Items.COMPASS, new LiteralText("Search"), null);
    protected static final ItemStack LEFT_ARROW = createItem(Items.ARROW, new LiteralText("<<<<"), null);
    protected static final ItemStack RIGHT_ARROW = createItem(Items.ARROW, new LiteralText(">>>>"), null);

    protected final int page;
    private List<Button> orderButtons = new ArrayList<>();

    private int tick = 200;

    protected AbstractMarketScreen(ServerPlayerEntity player, String name, int page) {
        super(player, new LiteralText(name + ": Page " + page), 6);
        this.page = page;
        for (int i = 36; i < 45; i++) {
            registerButton(i, new Button(playerEntity -> { }, DIVIDER));
        }

        registerButton(48, new Button(playerEntity -> new SelectorScreen<>(playerEntity, "Type", MarketRegistry.ORDER_TYPES.stream().collect(Collectors.toList()), marketOrder -> marketOrder.openListGui(playerEntity)).open(), LIST_ITEM));
        registerButton(49, new Button(playerEntity -> SignInput.createSignStringHandler("Market Search", "", player, (playerEntity1, s) -> new SearchScreen(playerEntity1, s, 1).open()), SEARCH));
        registerButton(50, getRightButton());

        if (page > 1) {
            registerButton(45, new Button(playerEntity -> {
                playerEntity.closeHandledScreen();
                new MainMenu(player, page - 1).open();
            }, LEFT_ARROW));
        }
    }

    protected abstract Button getRightButton();
    protected abstract ArrayList<MarketListing> getBuyOrders();

    protected void onOrderClick(MarketListing order, boolean shift) {
        if (shift && player.hasPermissionLevel(2)) {
            MarketInstance.MARKET_INSTANCE.send(new CancelTask(player, order));
        } else if (order.getSeller() == player.getGameProfile()) {
            new ClaimingScreen(player, order).open();
        } else {
            if (order.getInput().has(player)) {
                new ConfirmationScreen(player, order).open();
            } else {
                player.sendMessage(new LiteralText("You do not have enough items for that!"), false);
            }
        }
    }

    @Override
    public void tick(ServerPlayerEntity server) {
        if (tick <= 0) {
            generateButtons();
            tick = 200;
        }
        tick--;
        super.tick(server);
    }

    @Override
    public void onClick(int slot, SlotActionType actionType, int button, ServerPlayerEntity playerEntity) {
        super.onClick(slot, actionType, button, playerEntity);
        if (slot < orderButtons.size()) {
            orderButtons.get(slot).action.accept(playerEntity, actionType == SlotActionType.QUICK_MOVE);
        }
    }

    @Override
    public void generateButtons() {
        super.generateButtons();
        ArrayList<MarketListing> orders = getBuyOrders();
        orderButtons = orders.stream().map(order -> {
            ItemStack stack = order.getDisplayStack(player);
            if (player.hasPermissionLevel(2)) {
                appendInfo(order, stack);
            }
            return new Button((playerEntity, shift) -> onOrderClick(order, shift), stack);
        }).collect(Collectors.toList());

        for (int j = 35 * (page - 1); j < 35 * page; j++) {
            if (j < orderButtons.size()) {
                inventory.setStack(j, orderButtons.get(j).getStack());
            } else {
                inventory.setStack(j, ItemStack.EMPTY);
            }
        }

        if (orders.size() >= 45) {
            registerButton(53, new Button(playerEntity -> {
                playerEntity.closeHandledScreen();
                new MainMenu(player, page + 1).open();
            }, RIGHT_ARROW));
        }
    }

    private static void appendInfo(MarketListing order, ItemStack stack) {
        List<Text> lore = getLore(stack);
        lore.add(LiteralText.EMPTY);
        lore.add(new LiteralText("Moderator Information:").formatted(Formatting.GOLD));
        lore.add(createBulletOptionText(Formatting.GOLD, Pair.of("Order ID", Integer.toString(order.id))));
        lore.add(createBulletOptionText(Formatting.GOLD, Pair.of("Seller", String.format("%s (%s)", order.getSeller().getName(), order.getSeller().getId()))));
        lore.add(new LiteralText("Shift Click To Cancel").formatted(Formatting.RED));
        setLore(stack, lore);
    }
}
