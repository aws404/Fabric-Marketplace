package io.github.aws404.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ServerPlayerEntity.class)
public interface ServerPlayerEntityAccessor {

    @Invoker("incrementScreenHandlerSyncId")
    void invokeIncrementScreenHandlerSyncId();

    @Accessor("screenHandlerSyncId")
    int getScreenHandlerSyncId();
}
