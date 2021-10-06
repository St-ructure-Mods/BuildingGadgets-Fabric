package com.direwolf20.buildinggadgets.common.mixins;

import com.direwolf20.buildinggadgets.common.events.ItemPickupCallback;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityMixin {

    @Inject(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;take(Lnet/minecraft/world/entity/Entity;I)V"))
    private void playerTouch(Player player, CallbackInfo callbackInfo) {
        ItemPickupCallback.EVENT.invoker().onPickup((ItemEntity) (Object) this, player);
    }
}
