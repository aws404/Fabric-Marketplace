package io.github.aws404.util.input;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.SignEditorOpenS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class SignInput {

    public static final HashMap<ServerPlayerEntity, SignInput> HANDLERS = new HashMap<>();

    protected final ServerPlayerEntity player;
    private final BiConsumer<ServerPlayerEntity, String> consumer;
    protected final BlockPos signPos;

    public static void createSignStringHandler(String name, String value, ServerPlayerEntity player, BiConsumer<ServerPlayerEntity, String> consumer) {
        HANDLERS.put(player, new SignInput(name, value, player, consumer));
    }

    public static void createSignNumberHandler(String name, String value, ServerPlayerEntity player, BiConsumer<ServerPlayerEntity, Number> consumer) {
        HANDLERS.put(player, new SignNumberInput(name, value, player, consumer));
    }

    private SignInput(String name, String value, ServerPlayerEntity player, BiConsumer<ServerPlayerEntity, String> consumer) {
        this.player = player;
        this.consumer = consumer;
        BlockPos playerPos = player.getBlockPos();
        BlockPos dummyPos = new BlockPos(playerPos.getX(), 254, playerPos.getZ());
        this.signPos = dummyPos;

        SignBlockEntity sign = new SignBlockEntity();
        sign.setTextOnRow(0, new LiteralText(value));
        sign.setTextOnRow(1, new LiteralText("^^^^^^^^^^^^^^^"));
        sign.setTextOnRow(2, new LiteralText("Set value for"));
        sign.setTextOnRow(3, new LiteralText(name));
        sign.setEditor(player);
        sign.setPos(dummyPos);

        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(dummyPos, Blocks.OAK_SIGN.getDefaultState()));
        player.networkHandler.sendPacket(sign.toUpdatePacket());
        player.networkHandler.sendPacket(new SignEditorOpenS2CPacket(dummyPos));
    }

    public void onInput(BlockPos pos, String[] input) {
        if (pos.equals(signPos)) {
            consumer.accept(player, input[0]);
        } else {
            player.sendMessage(new LiteralText("There was an error with input! Please try again."), false);
        }

        player.networkHandler.sendPacket(new BlockUpdateS2CPacket(player.world, signPos));
        HANDLERS.remove(player);
    }

    public static class SignNumberInput extends SignInput {
        private final BiConsumer<ServerPlayerEntity, Number> consumer;
        private SignNumberInput(String name, String value, ServerPlayerEntity player, BiConsumer<ServerPlayerEntity, Number> consumer) {
            super(name, value, player, null);
            this.consumer = consumer;
        }

        @Override
        public void onInput(BlockPos pos, String[] input) {
            if (pos.isWithinDistance(signPos, 1)) {
                try {
                    consumer.accept(player, Double.valueOf(input[0]));
                } catch (NumberFormatException e) {
                    player.sendMessage(new LiteralText("That is not a number! Try again.").formatted(Formatting.RED), false);
                    player.networkHandler.sendPacket(new SignEditorOpenS2CPacket(signPos));
                    return;
                }
            } else {
                player.sendMessage(new LiteralText("There was an error with input! Please try again.").formatted(Formatting.RED), false);
            }

            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(player.world, signPos));
            HANDLERS.remove(player);
        }
    }
}
