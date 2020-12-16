package io.github.aws404;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.github.aws404.market.MarketInstance;
import io.github.aws404.market.guis.ListSimpleOrderScreen;
import io.github.aws404.market.guis.MainMenu;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class MarketCommand {
    private static final SimpleCommandExceptionType MARKET_OFFLINE = new SimpleCommandExceptionType(new LiteralMessage("The market is not online. Try again later."));
    private static final SimpleCommandExceptionType MARKET_ONLINE = new SimpleCommandExceptionType(new LiteralMessage("The market is not offline. Try again later."));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("market")
                .then(CommandManager.literal("list")
                        .executes(context -> {
                            checkOnline();
                            new ListSimpleOrderScreen(context.getSource().getPlayer()).open();
                            return 1;
                        })
                )
                .then(CommandManager.literal("stop")
                    .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            checkOnline();
                            MarketInstance.MARKET_INSTANCE.stop();
                            context.getSource().sendFeedback(new LiteralText("Market manually stopped!"), true);
                            return 1;
                        })
                )
                .then(CommandManager.literal("start")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(context -> {
                            checkOffline();
                            MarketInstance.startMarket(context.getSource().getMinecraftServer());
                            context.getSource().sendFeedback(new LiteralText("Market manually started!"), true);
                            return 1;
                        })
                )
                .executes(context -> {
                    checkOnline();
                    new MainMenu(context.getSource().getPlayer(), 1).open();
                    return 1;
                })
        );
    }

    public static void checkOnline() throws CommandSyntaxException {
        if (MarketInstance.MARKET_INSTANCE == null || !MarketInstance.MARKET_INSTANCE.online) {
            throw MARKET_OFFLINE.create();
        }
    }

    public static void checkOffline() throws CommandSyntaxException {
        if (MarketInstance.MARKET_INSTANCE != null && MarketInstance.MARKET_INSTANCE.online) {
            throw MARKET_ONLINE.create();
        }
    }
}
