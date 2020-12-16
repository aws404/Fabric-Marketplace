package io.github.aws404.market.orders;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.market.guis.ConfirmationScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.stream.Collector;

public abstract class MarketListing {
    protected static final Gson GSON = new Gson();

    public int id;
    protected GameProfile seller;
    protected ArrayList<CurrencyInstance> claimableItems = new ArrayList<>();
    public Timestamp timestamp;

    public MarketListing(int id, GameProfile seller, String claimableItems, Timestamp timestamp) {
        this.id = id;
        this.seller = seller;
        this.timestamp = timestamp;

        if (claimableItems != null && !claimableItems.isEmpty()) {
            for (JsonElement jsonElement : GSON.fromJson(claimableItems, JsonArray.class)) {
                this.claimableItems.add(CurrencyInstance.deSerialise(jsonElement.getAsJsonObject()));
            }
        }
    }

    public abstract String getIdentifier();
    public abstract CurrencyInstance getInput();
    public abstract CurrencyInstance getOutput();
    public abstract void refresh();
    public abstract MarketListing createCounterOrder(ConfirmationScreen screen);
    public abstract ItemStack getDisplayStack(ServerPlayerEntity playerEntity);
    public abstract boolean hasTotalOutput(ServerPlayerEntity playerEntity);
    public abstract void takeTotalOutput(ServerPlayerEntity playerEntity);
    public abstract String serialiseOrder();
    public abstract void cancel();
    public abstract void connectOrders(MarketInstance instance, MarketListing order);

    /**
     * @return true if this trade is available to be purchased should be shown in the main page of the market
     */
    public abstract boolean isAvailable();

    /**
     * @return true if the trade should be visible in a players personal trade menu, such as items still being claimable
     */
    public abstract boolean isActiveTrade();

    public GameProfile getSeller() {
        return seller;
    }

    public CurrencyInstance getClaimableInSlot(int slot) {
        return claimableItems.get(slot);
    }

    public ArrayList<CurrencyInstance> getClaimable() {
        return claimableItems;
    }

    public void addToClaimable(CurrencyInstance currency) {
        for (CurrencyInstance claimableItem : claimableItems) {
            if (CurrencyInstance.currenciesMatch(claimableItem, currency)) {
                claimableItem.add(currency);
                return;
            }
        }
        claimableItems.add(currency);
    }

    public void removeClaimable(int slot) {
        claimableItems.remove(slot);
    }

    public String serialiseClaimable() {
        return GSON.toJson(claimableItems.stream().collect(Collector.of(JsonArray::new, (jsonArray, currencyInstance) -> jsonArray.add(currencyInstance.serialise()), (jsonElements, jsonElements2) -> {
            jsonElements.addAll(jsonElements2);
            return jsonElements;
        })));
    }
}
