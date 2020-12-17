package io.github.aws404.market.orders;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import io.github.aws404.InitFabricMarketplace;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.MarketRegistry;
import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.market.guis.ConfirmationScreen;
import io.github.aws404.market.guis.ListSimpleOrderScreen;
import io.github.aws404.util.gui.GuiBase;
import io.github.aws404.util.gui.container.ContainerBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BuyOrder extends MarketListing {

    private static final ItemStack DISPLAY_ITEM = GuiBase.createItem(Items.OAK_SIGN, new LiteralText("Simple Buy Order"), null);

    private CurrencyInstance input;
    private CurrencyInstance output;
    public int maximumTransactions;
    public int completedTransactions;

    public static BuyOrder createUnlisted(ServerPlayerEntity playerEntity, CurrencyInstance input, CurrencyInstance output, int maximumTransactions) {
        return new BuyOrder(
                playerEntity.getGameProfile(),
                input,
                output,
                maximumTransactions,
                0,
                "",
                -1,
                null
        );
    }

    private BuyOrder(GameProfile player, CurrencyInstance input, CurrencyInstance output, int maximumTransactions, int completedTransactions, String claimableItems, int id, Timestamp timestamp) {
        super(id, player, claimableItems, timestamp);
        this.input = input;
        this.output = output;
        this.maximumTransactions = maximumTransactions;
        this.completedTransactions = completedTransactions;
    }

    public boolean isAvailable() {
        LocalDateTime expire = LocalDateTime.ofEpochSecond(timestamp.getTime(), timestamp.getNanos(), InitFabricMarketplace.ZONE_OFFSET).plus(Duration.ofMinutes(InitFabricMarketplace.CONFIG.orderExpireTime));
        Duration remaining = Duration.ofSeconds(LocalDateTime.now().until(expire, ChronoUnit.SECONDS));
        return maximumTransactions > completedTransactions && remaining.getSeconds() > 0;
    }

    public CurrencyInstance getInput() {
        return input;
    }

    public CurrencyInstance getOutput() {
        return output;
    }

    @Override
    public String getIdentifier() {
        return "buy_order";
    }

    public void refresh() {
        if (id == -1) {
            return;
        }
        BuyOrder refreshed = (BuyOrder) MarketInstance.MARKET_INSTANCE.getBuyOrder(id);
        this.id = refreshed.id;
        this.seller = refreshed.seller;
        this.input = refreshed.input;
        this.output = refreshed.output;
        this.completedTransactions = refreshed.completedTransactions;
        this.maximumTransactions = refreshed.maximumTransactions;
        this.claimableItems = refreshed.claimableItems;
        this.timestamp = refreshed.timestamp;
    }

    @Override
    public BuyOrder createCounterOrder(ConfirmationScreen screen) {
        return new BuyOrder(
                screen.player.getGameProfile(),
                output,
                input,
                screen.transactions,
                0,
                "",
                -1,
                null
        );
    }

    @Override
    public ItemStack getDisplayStack(ServerPlayerEntity playerEntity) {
        ArrayList<Text> lore = GuiBase.getLore(getOutput().getDisplayItem());
        lore.add(LiteralText.EMPTY);
        lore.add(new LiteralText("")
                .append(new LiteralText("- ").formatted(Formatting.GOLD))
                .append(new LiteralText("Price: "))
                .append(getInput().asText())
        );

        LocalDateTime expire = LocalDateTime.ofEpochSecond(timestamp.getTime(), timestamp.getNanos(), InitFabricMarketplace.ZONE_OFFSET).plus(Duration.ofMinutes(InitFabricMarketplace.CONFIG.orderExpireTime));
        Duration remaining = Duration.ofSeconds(LocalDateTime.now().until(expire, ChronoUnit.SECONDS));
        int daysRemaining = (int) Math.floor(remaining.toDays());
        remaining = remaining.minusDays(daysRemaining);
        int hoursRemaining = (int) Math.floor(remaining.toHours());
        remaining = remaining.minusHours(hoursRemaining);
        int minuteRemaining = (int) Math.floor(remaining.toMinutes());

        String remainingString = (daysRemaining > 0 ? daysRemaining + "d " : "") + (hoursRemaining > 0 ? hoursRemaining + "h " : "") + minuteRemaining + "m";

        lore.add(new LiteralText("")
                .append(new LiteralText("- ").formatted(Formatting.GOLD))
                .append(new LiteralText("Remaining: "))
                .append(new LiteralText(remainingString).formatted(Formatting.GRAY))
        );
        /* Might add this in some capacity at some point. Just redundant to show both
        lore.add(new LiteralText("")
                .append(new LiteralText("- ").formatted(Formatting.GOLD))
                .append(new LiteralText("Expires: "))
                .append(new LiteralText(expire.format(TIME_FORMATTER)).formatted(Formatting.GRAY))
        );
         */
        lore.add(LiteralText.EMPTY);
        lore.add(new LiteralText(String.format("[%s/%s Transactions Complete]", completedTransactions, maximumTransactions)).formatted(Formatting.GRAY));

        if (seller == playerEntity.getGameProfile()) {
            lore.add(new LiteralText("This is your listing, click to modify.").formatted(Formatting.GRAY));
        }

        Text name = new LiteralText("")
                .append(new LiteralText("Selling ").formatted(Formatting.GOLD))
                .append(getOutput().asText());
        return ContainerBase.createItem(getOutput().getDisplayItem().getItem(), name, lore);
    }

    @Override
    public boolean hasTotalOutput(ServerPlayerEntity playerEntity) {
        return output.getType().get(playerEntity).doubleValue() >= output.getAmount().doubleValue() * maximumTransactions;
    }

    @Override
    public void takeTotalOutput(ServerPlayerEntity playerEntity) {
        output.getType().take(playerEntity, output.getAmount().doubleValue() * maximumTransactions);
    }

    @Override
    public String serialiseOrder() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("input", input.serialise());
        jsonObject.add("output", output.serialise());
        jsonObject.addProperty("completed", completedTransactions);
        jsonObject.addProperty("maximum", maximumTransactions);
        return jsonObject.toString();
    }

    @Override
    public void cancel() {
        int remainingTransactions = maximumTransactions - completedTransactions;
        for (int i = 0; i < remainingTransactions; i++) {
            addToClaimable(getOutput());
        }
        completedTransactions = maximumTransactions;
    }

    @Override
    public void connectOrders(MarketInstance instance, MarketListing order) {
        if (!(order instanceof BuyOrder)) {
            return;
        }

        for (int i = ((BuyOrder) order).completedTransactions; i < ((BuyOrder) order).maximumTransactions; i++) {
            if (!(isAvailable() && order.isAvailable())) {
                break;
            }

            ((BuyOrder) order).completedTransactions++;
            this.completedTransactions++;
            this.addToClaimable(order.getOutput().copy());
            order.addToClaimable(this.getOutput().copy());
        }

        instance.writeUpdatesToOrder(this);
        instance.writeUpdatesToOrder(order);

        ServerPlayerEntity buyer = instance.getServer().getPlayerManager().getPlayer(getSeller().getId());
        ServerPlayerEntity seller = instance.getServer().getPlayerManager().getPlayer(order.getSeller().getId());

        if (buyer != null) {
            buyer.sendMessage(new LiteralText("Someone has bought some of your ").append(getOutput().asText()).append(" from the market!"), false);
        }
        if (seller != null) {
            seller.sendMessage(new LiteralText("Someone has bought some of your ").append(order.getOutput().asText()).append(" from the market!"), false);
        }

    }

    public boolean isActiveTrade() {
        return (maximumTransactions > completedTransactions || claimableItems.size() >= 1);
    }

    public static class Builder extends MarketRegistry.OrderTypeBuilder {
        @Override
        public BuyOrder fromSet(MarketInstance instance, ResultSet set) {
            try {
                JsonObject serialised = GSON.fromJson(set.getString("SERIALISED"), JsonObject.class);

                CurrencyInstance input = CurrencyInstance.deSerialise(serialised.getAsJsonObject("input"));
                CurrencyInstance output = CurrencyInstance.deSerialise(serialised.getAsJsonObject("output"));

                UUID uuid = UUID.fromString(set.getString("SELLER"));
                GameProfile player;
                ServerPlayerEntity onlinePlayer = instance.getServer().getPlayerManager().getPlayer(uuid);
                if (onlinePlayer == null) {
                    try {
                        player = CompletableFuture.supplyAsync(() -> new GameProfile(uuid, InitFabricMarketplace.MOJANG_SERVER_UTILS.getNameFromUUID(uuid))).get();
                    } catch (InterruptedException | ExecutionException e) {
                        player = new GameProfile(uuid, "Seller");
                        e.printStackTrace();
                    }
                } else {
                    player = onlinePlayer.getGameProfile();
                }

                return new BuyOrder(
                        player,
                        input,
                        output,
                        serialised.get("maximum").getAsInt(),
                        serialised.get("completed").getAsInt(),
                        set.getString("CLAIMABLE"),
                        set.getInt("ID"),
                        set.getTimestamp("TIMESTAMP")
                );
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public void openListGui(ServerPlayerEntity playerEntity) {
            new ListSimpleOrderScreen(playerEntity).open();
        }

        @Override
        public ItemStack getDisplayItem() {
            return DISPLAY_ITEM;
        }
    }
}
