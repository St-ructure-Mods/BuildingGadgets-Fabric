package com.direwolf20.buildinggadgets.common.mixins.client;

import com.direwolf20.buildinggadgets.client.EventUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenRenderTooltip {

    @Inject(method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V", at=@At("TAIL"))
    protected void renderTooltip(PoseStack poseStack, ItemStack itemStack, int i, int j, CallbackInfo ci) {
        EventUtil.onDrawTooltip(poseStack, itemStack, i, j);
    }
}
