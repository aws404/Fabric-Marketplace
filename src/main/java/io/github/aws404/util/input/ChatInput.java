package io.github.aws404.util.input;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class ChatInput {

    public static final HashMap<ServerPlayerEntity, ChatInput> HANDLERS = new HashMap<>();

    private final ServerPlayerEntity player;
    private final BiConsumer<ServerPlayerEntity, String> consumer;
    private final String original;

    public static void createChatInputHandler(String name, String value, ServerPlayerEntity player, BiConsumer<ServerPlayerEntity, String> consumer) {
        HANDLERS.put(player, new ChatInput(name, value, player, consumer));
    }

    private ChatInput(String name, String value, ServerPlayerEntity player, BiConsumer<ServerPlayerEntity, String> consumer) {
        this.player = player;
        this.consumer = consumer;
        this.original = value == null ? "" : value;

        Style style = Style.EMPTY.withColor(Formatting.GOLD);
        if (!original.isEmpty()) {
            style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new LiteralText("Click to auto-fill '" + value + "'")))
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, value));
        }
        player.sendMessage(new LiteralText("Type a new value for " + name + ", or 'cancel' to cancel:").setStyle(style), false);
    }

    public void onInput(String input) {
        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(new LiteralText("Cancelled!").formatted(Formatting.RED), false);
            consumer.accept(player, original);
        } else {
            consumer.accept(player, input);
        }

        HANDLERS.remove(player);
    }
}
