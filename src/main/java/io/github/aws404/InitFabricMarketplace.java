package io.github.aws404;

import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.MarketRegistry;
import io.github.aws404.util.MojangServerUtils;
import io.github.aws404.util.gui.CustomHandler;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.Identifier;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.TimeZone;

public class InitFabricMarketplace implements ModInitializer {

	public static final ZoneOffset ZONE_OFFSET = ZoneOffset.ofTotalSeconds(TimeZone.getDefault().getRawOffset() / 1000);
	public static MojangServerUtils MOJANG_SERVER_UTILS;
	public static MarketplaceConfig CONFIG;

	@Override
	public void onInitialize() {
		CONFIG = AutoConfig.register(MarketplaceConfig.class, JanksonConfigSerializer::new).getConfig();
		MarketRegistry.CURRENCY_PREDICATE = currencyBuilder -> {
			Identifier id = MarketRegistry.CURRENCY_TYPES.getId(currencyBuilder);
			return Arrays.stream(CONFIG.currencyBlacklist).map(Identifier::new).noneMatch(id::equals);
		};

		MarketRegistry.ORDER_PREDICATE = currencyBuilder -> {
			Identifier id = MarketRegistry.ORDER_TYPES.getId(currencyBuilder);
			return Arrays.stream(CONFIG.orderTypeBlacklist).map(Identifier::new).noneMatch(id::equals);
		};


		ServerLifecycleEvents.SERVER_STARTED.register(minecraftServer -> {
			MOJANG_SERVER_UTILS = new MojangServerUtils(minecraftServer);
			MarketRegistry.listLoaded();
			MarketInstance.startMarket(minecraftServer);
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(minecraftServer -> MarketInstance.MARKET_INSTANCE.stop());

		CommandRegistrationCallback.EVENT.register((commandDispatcher, b) -> MarketCommand.register(commandDispatcher));

		ServerTickEvents.START_SERVER_TICK.register(minecraftServer -> {
			minecraftServer.getPlayerManager().getPlayerList().forEach(playerEntity -> {
				if (playerEntity.currentScreenHandler instanceof CustomHandler) {
					((CustomHandler) playerEntity.currentScreenHandler).tick(playerEntity);
				}
			});
		});
	}
}
