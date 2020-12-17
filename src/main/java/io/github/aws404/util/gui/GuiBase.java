package io.github.aws404.util.gui;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import io.github.aws404.mixin.ServerPlayerEntityAccessor;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class GuiBase {

    protected final Inventory inventory;
    public final ServerPlayerEntity player;
    protected HashMap<Integer, Button> buttons = new HashMap<>();
    protected ScreenHandler screenHandler;

    protected GuiBase(ServerPlayerEntity player, Inventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    public void tick(ServerPlayerEntity playerEntity) {

    }

    public void onClick(int slot, SlotActionType actionType, int button, ServerPlayerEntity playerEntity) {
        if (buttons.containsKey(slot)) {
            buttons.get(slot).action.accept(playerEntity, actionType == SlotActionType.QUICK_MOVE);
        }
        generateButtons();
    }

    public void registerButton(int slot, Button button) {
        buttons.put(slot, button);
    }

    public abstract NamedScreenHandlerFactory getScreenHandlerFactory();

    public Inventory getInventory() {
        return inventory;
    }

    public void updateStacks(DefaultedList<ItemStack> stacks) {
        for(int i = 0; i < stacks.size(); ++i) {
            setStackInSlot(i, stacks.get(i));
        }
    }

    public void setStackInSlot(int i, ItemStack stack) {
        screenHandler.setStackInSlot(i, stack);
    }

    public void generateButtons() {
        buttons.forEach((integer, button) -> inventory.setStack(integer, button.getStack()));
    }

    public int open() {
        generateButtons();

        if (player.currentScreenHandler != player.playerScreenHandler) {
            player.closeHandledScreen();
        }

        ((ServerPlayerEntityAccessor) player).invokeIncrementScreenHandlerSyncId();

        screenHandler = getScreenHandlerFactory().createMenu(((ServerPlayerEntityAccessor) player).getScreenHandlerSyncId(), player.inventory, player);
        player.networkHandler.sendPacket(new OpenScreenS2CPacket(screenHandler.syncId, screenHandler.getType(), getScreenHandlerFactory().getDisplayName()));
        screenHandler.addListener(player);
        player.currentScreenHandler = screenHandler;
        return screenHandler.syncId;
    }

    protected static void setLore(ItemStack stack, List<Text> lore) {
        ListTag listTag = new ListTag();
        for (Text current : lore) {
            listTag.add(StringTag.of(Text.Serializer.toJson(new LiteralText("").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.WHITE)).append(current))));
        }
        stack.getOrCreateSubTag("display").put("Lore", listTag);
    }

    public static ArrayList<Text> getLore(ItemStack stack) {
        ArrayList<Text> lore = new ArrayList<>();
        for (Tag tag : stack.getOrCreateSubTag("display").getList("Lore", 8)) {
            lore.add(Text.Serializer.fromJson(tag.asString()));
        }
        return lore;
    }

    public static ItemStack createItem(Item item, Text displayName, List<Text> lore, int damage, int count) {
        ItemStack stack = new ItemStack(item, count);
        stack.setCustomName(new LiteralText("").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.WHITE)).append(displayName));
        if (lore != null) {
            setLore(stack, lore);
        }
        stack.setDamage(damage);
        stack.addHideFlag(ItemStack.TooltipSection.UNBREAKABLE);
        stack.addHideFlag(ItemStack.TooltipSection.ADDITIONAL);
        stack.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
        stack.addHideFlag(ItemStack.TooltipSection.MODIFIERS);
        stack.putSubTag("Unbreakable", ByteTag.of(true));
        return stack;
    }

    public static ItemStack createItem(Item item, Text displayName, List<Text> lore) {
        return createItem(item, displayName, lore, 0, 1);
    }

    public static ItemStack createSkullItem(GameProfile profile, Text displayName, List<Text> lore) {
        ItemStack stack = new ItemStack(Items.PLAYER_HEAD);
        stack.setCustomName(new LiteralText("").setStyle(Style.EMPTY.withItalic(false).withColor(Formatting.WHITE)).append(displayName));
        if (lore != null) {
            setLore(stack, lore);
        }
        stack.addHideFlag(ItemStack.TooltipSection.UNBREAKABLE);
        stack.addHideFlag(ItemStack.TooltipSection.ADDITIONAL);
        stack.addHideFlag(ItemStack.TooltipSection.ENCHANTMENTS);
        stack.addHideFlag(ItemStack.TooltipSection.MODIFIERS);
        stack.putSubTag("Unbreakable", ByteTag.of(true));
        stack.putSubTag("SkullOwner", NbtHelper.fromGameProfile(new CompoundTag(), profile));
        return stack;
    }

    protected static MutableText createOptionText(String title, String value) {
        return new LiteralText("")
                .append(new LiteralText(title + ": ").formatted(Formatting.GRAY))
                .append(new LiteralText(value).formatted(Formatting.WHITE));
    }

    protected static List<MutableText> createBulletList(Formatting formatting, Pair<String, String>... bullets) {
        return Arrays.stream(bullets).map(bullet -> createBulletOptionText(formatting, bullet)).collect(Collectors.toList());
    }

    protected static MutableText createBulletOptionText(Formatting formatting, Pair<String, String> bullet) {
        return new LiteralText("")
                .append(new LiteralText("- ").formatted(formatting))
                .append(new LiteralText(bullet.getFirst() + ": ").formatted(Formatting.GRAY))
                .append(new LiteralText(bullet.getSecond()).formatted(Formatting.WHITE));
    }

    protected static MutableText createBulletText(Formatting formatting, String bullet) {
        return  new LiteralText("")
                .append(new LiteralText("- ").formatted(formatting))
                .append(new LiteralText(bullet).formatted(Formatting.WHITE));
    }

    protected static class Button {
        public final BiConsumer<ServerPlayerEntity, Boolean> action;
        private final Supplier<ItemStack> stack;

        public Button(Consumer<ServerPlayerEntity> action, ItemStack stack) {
            this.action = (player, shift) -> action.accept(player);
            this.stack = () -> stack;
        }

        public Button(Consumer<ServerPlayerEntity> action, Supplier<ItemStack> stack) {
            this.action = (player, shift) -> action.accept(player);
            this.stack = stack;
        }

        public Button(BiConsumer<ServerPlayerEntity, Boolean> action, ItemStack stack) {
            this.action = action;
            this.stack = () -> stack;
        }

        public ItemStack getStack() {
            return stack.get();
        }
    }

}
