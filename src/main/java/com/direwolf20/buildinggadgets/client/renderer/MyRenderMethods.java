package com.direwolf20.buildinggadgets.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.List;

public class MyRenderMethods {
    // TODO: Replace with native method
    public static void renderModelBrightnessColorQuads(PoseStack.Pose matrixEntry, VertexConsumer builder, float red, float green, float blue, float alpha, List<BakedQuad> listQuads, int combinedLightsIn, int combinedOverlayIn) {
        for (BakedQuad bakedquad : listQuads) {
            float f = 1f;
            float f1 = 1f;
            float f2 = 1f;
            if (bakedquad.isTinted()) {
                f = red;
                f1 = green;
                f2 = blue;
            }
            builder.putBulkData(matrixEntry, bakedquad, f, f1, f2, combinedLightsIn, combinedOverlayIn);
        }
    }
}
