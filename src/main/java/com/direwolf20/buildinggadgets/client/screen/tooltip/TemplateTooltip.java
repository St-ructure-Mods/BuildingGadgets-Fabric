package com.direwolf20.buildinggadgets.client.screen.tooltip;

import com.direwolf20.buildinggadgets.client.EventUtil;
import com.direwolf20.buildinggadgets.common.component.BGComponent;
import com.direwolf20.buildinggadgets.common.tainted.building.view.BuildContext;
import com.direwolf20.buildinggadgets.common.tainted.inventory.IItemIndex;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryHelper;
import com.direwolf20.buildinggadgets.common.tainted.inventory.MatchResult;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.MaterialList;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.direwolf20.buildinggadgets.common.tainted.template.TemplateHeader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class TemplateTooltip implements ClientTooltipComponent {

    ItemStack itemStack;
    Minecraft mc = Minecraft.getInstance();
    int count = 0;

    public TemplateTooltip(TemplateData data) {
        itemStack = data.getStack();
    }

    @Override
    public int getHeight() {
        if(this.getCount() > 0 && Screen.hasShiftDown()) {
            return (((count - 1) / EventUtil.STACKS_PER_LINE) + 1) * 21;
        }
        return 0;
    }

    @Override
    public int getWidth(Font font) {
        if(this.getCount() > 0 && Screen.hasShiftDown()) {
            return Math.min(EventUtil.STACKS_PER_LINE, count) * 18;
        }
        return 0;
    }

    private int getCount() {
        BGComponent.TEMPLATE_PROVIDER_COMPONENT.maybeGet(mc.level).ifPresent((ITemplateProvider provider) -> BGComponent.TEMPLATE_KEY_COMPONENT.maybeGet(itemStack).ifPresent((ITemplateKey templateKey) -> {
            Template template = provider.getTemplateForKey(templateKey);
            IItemIndex index = InventoryHelper.index(itemStack, mc.player);

            BuildContext buildContext = BuildContext.builder()
                    .stack(itemStack)
                    .player(mc.player)
                    .build(mc.level);

            TemplateHeader header = template.getHeaderAndForceMaterials(buildContext);
            MaterialList list = header.getRequiredItems();
            if (list == null)
                list = MaterialList.empty();

            MatchResult match;

            try (Transaction transaction = Transaction.openOuter()) {
                match = index.match(list, transaction);
            }
            count = match.isSuccess() ? match.getChosenOption().entrySet().size() : match.getChosenOption().entrySet().size() + 1;
        }));
        return count;
    }

    @Override
    public void renderImage(Font font, int xin, int yin, PoseStack poseStack, ItemRenderer itemRenderer, int k, TextureManager textureManager) {
        if (!Screen.hasShiftDown())
            return;

        //This method will draw items on the tooltip
        Minecraft mc = Minecraft.getInstance();

        if (mc.level == null || mc.player == null)
            return;

        BGComponent.TEMPLATE_PROVIDER_COMPONENT.maybeGet(mc.level).ifPresent((ITemplateProvider provider) -> BGComponent.TEMPLATE_KEY_COMPONENT.maybeGet(itemStack).ifPresent((ITemplateKey templateKey) -> {
            Template template = provider.getTemplateForKey(templateKey);
            IItemIndex index = InventoryHelper.index(itemStack, mc.player);
            BuildContext buildContext = BuildContext.builder()
                    .stack(itemStack)
                    .player(mc.player)
                    .build(mc.level);
            TemplateHeader header = template.getHeaderAndForceMaterials(buildContext);
            MaterialList list = header.getRequiredItems();
            if (list == null)
                list = MaterialList.empty();

            MatchResult match;

            try (Transaction transaction = Transaction.openOuter()) {
                match = index.match(list, transaction);
            }

            Multiset<ItemVariant> existing = match.getFoundItems();
            List<Multiset.Entry<ItemVariant>> sortedEntries = ImmutableList.sortedCopyOf(EventUtil.ENTRY_COMPARATOR, match.getChosenOption().entrySet());

            int by = yin;
            int j = 0;
            int totalMissing = 0;
            //add missing offset because the Stack is 16 by 16 as a render, not 9 by 9
            //needs to be 8 instead of 7, so that there is a one pixel padding to the text, just as there is between stacks
            by += 8;
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            for (Multiset.Entry<ItemVariant> entry : sortedEntries) {
                int x = xin + (j % EventUtil.STACKS_PER_LINE) * 18;
                int y = yin + (j / EventUtil.STACKS_PER_LINE) * 20;
                totalMissing += renderRequiredBlocks(poseStack, entry.getElement().toStack(), font, itemRenderer, x, y, existing.count(entry.getElement()), entry.getCount());
                j++;
            }
        }));
    }

    private int renderRequiredBlocks(PoseStack matrices, ItemStack itemStack, Font font, ItemRenderer render, int x, int y, int count, int req) {

        String s1 = req == Integer.MAX_VALUE ? "\u221E" : Integer.toString(req);
        int w1 = font.width(s1);

        boolean hasReq = req > 0;


        // TODO: fix this, this isn't correct
        render.renderAndDecorateItem(itemStack, x, y);
        render.renderGuiItemDecorations(font, itemStack, x, y);

        matrices.pushPose();
        matrices.translate(x + 8 - w1 / 4f, y + (hasReq ? 12 : 14), 500f + render.blitOffset);
        matrices.scale(.5f, .5f, 0);
        MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        font.drawInBatch(s1, 0, 0, 0xFFFFFF, true, matrices.last().pose(), irendertypebuffer$impl, false, 0, 15728880);
        matrices.popPose();


        int missingCount = 0;
        if (hasReq) {
            if (count < req) {
                String fs = Integer.toString(req - count);
                String s2 = "(" + fs + ")";
                int w2 = font.width(s2);

                matrices.pushPose();
                matrices.translate(x + 8 - w2 / 4f, y + 17, 500f + render.blitOffset);
                matrices.scale(.5f, .5f, 0);
                font.drawInBatch(s2, 0, 0, 0xFF0000, true, matrices.last().pose(), irendertypebuffer$impl, false, 0, 15728880);
                matrices.popPose();

                missingCount = (req - count);
            }
        }

        irendertypebuffer$impl.endBatch();
        return missingCount;
    }
}
