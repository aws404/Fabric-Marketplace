package io.github.aws404.market;

import com.mojang.serialization.Lifecycle;
import io.github.aws404.market.currency.CurrencyInstance;
import io.github.aws404.market.currency.SimpleCurrency;
import io.github.aws404.market.currency.types.ExperienceCurrency;
import io.github.aws404.market.currency.types.ExperienceLevelsCurrency;
import io.github.aws404.market.currency.types.GunpowderCurrency;
import io.github.aws404.market.currency.types.ItemCurrency;
import io.github.aws404.market.guis.templates.GuiSelectable;
import io.github.aws404.market.orders.BuyOrder;
import io.github.aws404.market.orders.MarketListing;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

import java.sql.ResultSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MarketRegistry {
    public static Predicate<CurrencyBuilder> CURRENCY_PREDICATE = (currency) -> true;
    public static Predicate<OrderTypeBuilder> ORDER_PREDICATE = (orderType) -> true;

    private static final RegistryKey<Registry<OrderTypeBuilder>> ORDER_TYPES_KEY = RegistryKey.ofRegistry(new Identifier("order_types"));
    private static final RegistryKey<Registry<CurrencyBuilder>> CURRENCY_TYPES_KEY = RegistryKey.ofRegistry(new Identifier("currency_types"));

    public static final Registry<OrderTypeBuilder> ORDER_TYPES = new SimpleRegistry<>(ORDER_TYPES_KEY, Lifecycle.experimental());
    public static final Registry<CurrencyBuilder> CURRENCY_TYPES = new SimpleRegistry<>(CURRENCY_TYPES_KEY, Lifecycle.experimental());

    public static final OrderTypeBuilder BUY_ORDER = Registry.register(MarketRegistry.ORDER_TYPES, new Identifier("buy_order"), new BuyOrder.Builder());;

    public static final CurrencyBuilder ITEM = Registry.register(MarketRegistry.CURRENCY_TYPES, new Identifier("item"), new ItemCurrency.Builder());
    public static final CurrencyBuilder EXPERIENCE = Registry.register(MarketRegistry.CURRENCY_TYPES, new Identifier("experience"), new ExperienceCurrency.Builder());
    public static final CurrencyBuilder EXPERIENCE_LEVELS = Registry.register(MarketRegistry.CURRENCY_TYPES, new Identifier("experience_levels"), new ExperienceLevelsCurrency.Builder());

    public static final CurrencyBuilder GUNPOWDER = FabricLoader.getInstance().isModLoaded("gunpowder-currency") ? Registry.register(MarketRegistry.CURRENCY_TYPES, new Identifier("gunpowder"), new GunpowderCurrency.Builder()) : null;

    public static void listLoaded() {
        Set<Identifier> builders = getOrderTypeBuilders().stream().map(ORDER_TYPES::getId).collect(Collectors.toSet());
        MarketInstance.LOGGER.info("Loaded {} order types: {}", builders.size(), builders.stream().map(Identifier::toString).collect(Collectors.joining(", ")));

        Set<Identifier> currencies = getCurrencyBuilders().stream().map(CURRENCY_TYPES::getId).collect(Collectors.toSet());
        MarketInstance.LOGGER.info("Loaded {} currencies: {}", currencies.size(), currencies.stream().map(Identifier::toString).collect(Collectors.joining(", ")));
    }

    public static List<CurrencyBuilder> getCurrencyBuilders() {
        return CURRENCY_TYPES.stream().filter(CURRENCY_PREDICATE).collect(Collectors.toList());
    }

    public static List<OrderTypeBuilder> getOrderTypeBuilders() {
        return ORDER_TYPES.stream().filter(ORDER_PREDICATE).collect(Collectors.toList());
    }

    public abstract static class OrderTypeBuilder implements GuiSelectable {
        public abstract MarketListing fromSet(MarketInstance instance, ResultSet set);
        public abstract void openListGui(ServerPlayerEntity playerEntity);
        public Identifier getIdentifier() {
            return ORDER_TYPES.getId(this);
        }
    }

    public abstract static class CurrencyBuilder implements GuiSelectable {
        public abstract SimpleCurrency deSerialise(String serialised);
        public abstract void createSelector(ServerPlayerEntity playerEntity, Consumer<CurrencyInstance> type);
        public Identifier getIdentifier() {
            return CURRENCY_TYPES.getId(this);
        }
    }
}
