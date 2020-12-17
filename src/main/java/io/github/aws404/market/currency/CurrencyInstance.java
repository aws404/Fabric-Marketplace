package io.github.aws404.market.currency;

import com.google.gson.JsonObject;
import io.github.aws404.market.MarketRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Objects;

public class CurrencyInstance {

    private final SimpleCurrency type;
    private Number amount;

    public CurrencyInstance(SimpleCurrency type, Number amount) {
        this.type = type;
        this.amount = amount;
    }

    public boolean has(ServerPlayerEntity player) {
        return type.get(player).doubleValue() >= amount.doubleValue();
    }

    public void give(ServerPlayerEntity player) {
        type.give(player, amount);
    }

    public void take(ServerPlayerEntity player) {
        type.take(player, amount);
    }

    public Text asText() {
        return type.asText(amount);
    }

    public JsonObject serialise() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type.getBuilder().getIdentifier().toString());
        object.addProperty("amount", amount);
        object.addProperty("data", type.serialise());
        return object;
    }

    public ItemStack getDisplayItem() {
        return type.getDisplayItem(amount);
    }

    public void add(CurrencyInstance addition) {
        if (currenciesMatch(this, addition)) {
            this.amount = this.amount.doubleValue() + addition.amount.doubleValue();
        }
    }

    public boolean canBuy(CurrencyInstance question) {
        if (currenciesMatch(this, question)) {
            return amount.doubleValue() >= question.amount.doubleValue();
        }
        return false;
    }

    public Number getAmount() {
        return amount;
    }

    public SimpleCurrency getType() {
        return type;
    }

    public String amountAsString() {
        return type.getNumberAsString(amount);
    }

    public static boolean currenciesMatch(CurrencyInstance left, CurrencyInstance right) {
        return left.type.getBuilder().getIdentifier().equals(right.type.getBuilder().getIdentifier());
    }

    public CurrencyInstance copy() {
        return new CurrencyInstance(type, amount);
    }

    public static CurrencyInstance deSerialise(JsonObject object) {
        SimpleCurrency type = MarketRegistry.CURRENCY_TYPES.get(new Identifier(object.get("type").getAsString())).deSerialise(object.get("data").getAsString());
        return new CurrencyInstance(type, object.get("amount").getAsDouble());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencyInstance that = (CurrencyInstance) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, amount);
    }
}
