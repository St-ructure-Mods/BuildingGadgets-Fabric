/**
 * This class was adapted from code written by Vazkii for the PSI mod: https://github.com/Vazkii/Psi
 * Psi is Open Source and distributed under the
 * Psi License: http://psi.vazkii.us/license.php
 */
package com.direwolf20.buildinggadgets.client.screen;

import com.direwolf20.buildinggadgets.client.KeyBindings;
import com.direwolf20.buildinggadgets.client.OurSounds;
import com.direwolf20.buildinggadgets.client.screen.components.GuiIconActionable;
import com.direwolf20.buildinggadgets.client.screen.components.GuiSliderInt;
import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.items.*;
import com.direwolf20.buildinggadgets.common.items.modes.BuildingModes;
import com.direwolf20.buildinggadgets.common.items.modes.ExchangingModes;
import com.direwolf20.buildinggadgets.common.network.C2S.*;
import com.direwolf20.buildinggadgets.common.util.GadgetUtils;
import com.direwolf20.buildinggadgets.common.util.lang.GuiTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.MessageTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.RadialTranslation;
import com.direwolf20.buildinggadgets.common.util.lang.Styles;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ModeRadialMenu extends Screen {
    public enum ScreenPosition {
        RIGHT, LEFT, BOTTOM, TOP
    }

    private static final ImmutableList<ResourceLocation> signsCopyPaste = ImmutableList.of(
            new ResourceLocation(Reference.MODID, "textures/gui/mode/copy.png"),
            new ResourceLocation(Reference.MODID, "textures/gui/mode/paste.png")
    );

    private int timeIn = 0;
    private int slotSelected = -1;
    private int segments;
    private final List<Button> conditionalButtons = new ArrayList<>();

    public ModeRadialMenu(ItemStack stack) {
        super(new TextComponent(""));

        if (stack.getItem() instanceof AbstractGadget)
            setSocketable(stack);
    }

    public void setSocketable(ItemStack stack) {
        if (stack.getItem() instanceof GadgetBuilding)
            segments = BuildingModes.values().length;
        else if (stack.getItem() instanceof GadgetExchanger)
            segments = ExchangingModes.values().length;
        else if (stack.getItem() instanceof GadgetCopyPaste)
            segments = GadgetCopyPaste.ToolMode.values().length;
    }

    @Override
    public void init() {
        conditionalButtons.clear();
        ItemStack tool = getGadget();
        boolean isDestruction = tool.getItem() instanceof GadgetDestruction;
        ScreenPosition right = isDestruction ? ScreenPosition.TOP : ScreenPosition.RIGHT;
        ScreenPosition left = isDestruction ? ScreenPosition.BOTTOM : ScreenPosition.LEFT;

        if (isDestruction) {
            addRenderableWidget(new PositionedIconActionable(RadialTranslation.DESTRUCTION_OVERLAY, "destroy_overlay", right, send -> {
                if (send) {
                    PacketChangeRange.send();
                }

                return GadgetDestruction.getOverlay(getGadget());
            }));

            addRenderableWidget(new PositionedIconActionable(RadialTranslation.FLUID_ONLY, "fluid_only", right, send -> {
                if (send) {
                    PacketToggleFluidOnly.send();
                }

                return GadgetDestruction.getIsFluidOnly(getGadget());
            }));
        } else {
            addRenderableWidget(new PositionedIconActionable(RadialTranslation.ROTATE, "rotate", left, false, send -> {
                if (send) {
                    PacketRotateMirror.send(PacketRotateMirror.Operation.ROTATE);
                }

                return false;
            }));
            addRenderableWidget(new PositionedIconActionable(RadialTranslation.MIRROR, "mirror", left, false, send -> {
                if (send) {
                    PacketRotateMirror.send(PacketRotateMirror.Operation.MIRROR);
                }

                return false;
            }));
        }
        if (!(tool.getItem() instanceof GadgetCopyPaste)) {
            if (!isDestruction || BuildingGadgets.getConfig().gadgets.gadgetDestruction.nonFuzzyEnabled) {
                Button button = new PositionedIconActionable(RadialTranslation.FUZZY, "fuzzy", right, send -> {
                    if (send)
                        PacketToggleFuzzy.send();

                    return AbstractGadget.getFuzzy(getGadget());
                });
                addRenderableWidget(button);
                conditionalButtons.add(button);
            }
            if (!isDestruction) {
                Button button = new PositionedIconActionable(RadialTranslation.CONNECTED_SURFACE, "connected_area", right, send -> {
                    if (send)
                        PacketToggleConnectedArea.send();

                    return AbstractGadget.getConnectedArea(getGadget());
                });
                addRenderableWidget(button);
                conditionalButtons.add(button);
            }
            if (!isDestruction) {
                int widthSlider = 82;
                GuiSliderInt sliderRange = new GuiSliderInt(width / 2 - widthSlider / 2, height / 2 + 72, widthSlider, 14, GuiTranslation.SINGLE_RANGE.componentTranslation().append(new TextComponent(": ")), 1, BuildingGadgets.getConfig().gadgets.maxRange,
                        GadgetUtils.getToolRange(tool), Color.DARK_GRAY, (slider, integer) -> {
                    slider.setValueInt(Mth.clamp(slider.getValueInt() + integer, 1, BuildingGadgets.getConfig().gadgets.maxRange));
                });
                //sliderRange.precision = 1;
                sliderRange.getComponents().forEach(this::addRenderableWidget);
            }
        } else {
            // Copy Paste specific
            addRenderableWidget(new PositionedIconActionable(RadialTranslation.OPEN_GUI, "copypaste_opengui", right, send -> {
                if (!send)
                    return false;

                assert Minecraft.getInstance().player != null;

                Minecraft.getInstance().player.closeContainer();
                if (GadgetCopyPaste.getToolMode(tool) == GadgetCopyPaste.ToolMode.COPY)
                    Minecraft.getInstance().setScreen(new CopyGUI(tool));
                else
                    Minecraft.getInstance().setScreen(new PasteGUI(tool));
                return true;
            }));
            addRenderableWidget(new PositionedIconActionable(RadialTranslation.OPEN_MATERIAL_LIST, "copypaste_materiallist", right, send -> {
                if (!send)
                    return false;

                assert Minecraft.getInstance().player != null;

                Minecraft.getInstance().player.closeContainer();
                Minecraft.getInstance().setScreen(new MaterialListGUI(tool));
                return true;
            }));
        }
        addRenderableWidget(new PositionedIconActionable(RadialTranslation.RAYTRACE_FLUID, "raytrace_fluid", right, send -> {
            if (send)
                PacketToggleRayTraceFluid.send();

            return AbstractGadget.shouldRayTraceFluid(getGadget());
        }));
        if (tool.getItem() instanceof GadgetBuilding) {
            addRenderableWidget(new PositionedIconActionable(RadialTranslation.PLACE_ON_TOP, "building_place_atop", right, send -> {
                if (send)
                    PacketToggleBlockPlacement.send();

                return GadgetBuilding.shouldPlaceAtop(getGadget());
            }));
        }
        addRenderableWidget(new PositionedIconActionable(RadialTranslation.ANCHOR, "anchor", left, send -> {
            if (send)
                PacketAnchor.send();

            ItemStack stack = getGadget();
            if (stack.getItem() instanceof GadgetCopyPaste || stack.getItem() instanceof GadgetDestruction)
                return ((AbstractGadget) stack.getItem()).getAnchor(stack) != null;

            return GadgetUtils.getAnchor(stack).isPresent();
        }));

        if (!(tool.getItem() instanceof GadgetExchanger)) {
            addRenderableWidget(new PositionedIconActionable(RadialTranslation.UNDO, "undo", left, false, send -> {
                if (send)
                    PacketUndo.send();
                return false;
            }));
        }

        updateButtons(tool);
    }

    private void updateButtons(ItemStack tool) {
        int posRight = 0;
        int posLeft = 0;
        int dim = 24;
        int padding = 10;
        boolean isDestruction = tool.getItem() instanceof GadgetDestruction;
        ScreenPosition right = isDestruction ? ScreenPosition.BOTTOM : ScreenPosition.RIGHT;
        for (GuiEventListener widget : children()) {
            if (!(widget instanceof PositionedIconActionable button))
                continue;

            if (!button.visible) continue;
            int offset;
            boolean isRight = button.position == right;
            if (isRight) {
                posRight += dim + padding;
                offset = 70;
            } else {
                posLeft += dim + padding;
                offset = -70 - dim;
            }
            button.setWidth(dim);
            //button.setHeight(dim);
            if (isDestruction)
                button.y = height / 2 + (isRight ? 10 : -button.getHeight() - 10);
            else
                button.x = width / 2 + offset;
        }
        posRight = resetPos(tool, padding, posRight);
        posLeft = resetPos(tool, padding, posLeft);
        for (GuiEventListener widget : children()) {
            if (!(widget instanceof PositionedIconActionable))
                continue;

            PositionedIconActionable button = (PositionedIconActionable) widget;
            if (!button.visible) continue;
            boolean isRight = button.position == right;
            int pos = isRight ? posRight : posLeft;
            if (isDestruction)
                button.x = pos;
            else
                button.y = pos;

            if (isRight)
                posRight += dim + padding;
            else
                posLeft += dim + padding;
        }
    }

    private int resetPos(ItemStack tool, int padding, int pos) {
        return tool.getItem() instanceof GadgetDestruction ? width / 2 - (pos - padding) / 2 : height / 2 - (pos - padding) / 2;
    }

    private ItemStack getGadget() {
        assert Minecraft.getInstance().player != null;
        return AbstractGadget.getGadget(Minecraft.getInstance().player);
    }

    @Override
    public void render(PoseStack matrices, int mx, int my, float partialTicks) {
        float stime = 5F;
        float fract = Math.min(stime, timeIn + partialTicks) / stime;
        int x = width / 2;
        int y = height / 2;

        int radiusMin = 26;
        int radiusMax = 60;
        double dist = new Vec3(x, y, 0).distanceTo(new Vec3(mx, my, 0));
        boolean inRange = false;
        if (segments != 0) {
            inRange = dist > radiusMin && dist < radiusMax;
            for (GuiEventListener button : children()) {
                if (button instanceof PositionedIconActionable)
                    ((PositionedIconActionable) button).setFaded(inRange);
            }
        }

        matrices.pushPose();
        matrices.translate((1 - fract) * x, (1 - fract) * y, 0);
        matrices.scale(fract, fract, fract);
        super.render(matrices, mx, my, partialTicks);
        matrices.popPose();

        if (segments == 0)
            return;

        RenderSystem.disableTexture();

        float angle = mouseAngle(x, y, mx, my);

        RenderSystem.enableBlend();
//        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        float totalDeg = 0;
        float degPer = 360F / segments;

        List<NameDisplayData> nameData = new ArrayList<>();

        ItemStack tool = getGadget();
        if (tool.isEmpty())
            return;

        slotSelected = -1;

        List<ResourceLocation> signs;
        int modeIndex;
        if (tool.getItem() instanceof GadgetBuilding) {
            modeIndex = GadgetBuilding.getToolMode(tool).ordinal();
            signs = Arrays.stream(BuildingModes.values()).map(e -> new ResourceLocation(Reference.MODID, e.getIcon())).collect(Collectors.toList());
        } else if (tool.getItem() instanceof GadgetExchanger) {
            modeIndex = GadgetExchanger.getToolMode(tool).ordinal();
            signs = Arrays.stream(ExchangingModes.values()).map(e -> new ResourceLocation(Reference.MODID, e.getIcon())).collect(Collectors.toList());
        } else {
            modeIndex = GadgetCopyPaste.getToolMode(tool).ordinal();
            signs = signsCopyPaste;
        }

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuilder();

        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        boolean shouldCenter = (segments + 2) % 4 == 0;
        int indexBottom = segments / 4;
        int indexTop = indexBottom + segments / 2;
        for (int seg = 0; seg < segments; seg++) {
            boolean mouseInSector = isCursorInSlice(angle, totalDeg, degPer, inRange);
            float radius = Math.max(0F, Math.min((timeIn + partialTicks - seg * 6F / segments) * 40F, radiusMax));


            float gs = 0.25F;
            if (seg % 2 == 0)
                gs += 0.1F;

            float r = gs;
            float g = gs + (seg == modeIndex ? 1F : 0.0F);
            float b = gs;
            float a = 0.4F;
            if (mouseInSector) {
                slotSelected = seg;
                r = g = b = 1F;
            }

            RenderSystem.setShaderColor(r, g, b, a);


            for (float i = degPer; i >= 0; i--) {
                float rad = (float) ((i + totalDeg) / 180F * Math.PI);


                double xp = x + Math.cos(rad) * radius;
                double yp = y + Math.sin(rad) * radius;
                if ((int) i == (int) (degPer / 2))
                    nameData.add(new NameDisplayData((int) xp, (int) yp, mouseInSector, shouldCenter && (seg == indexBottom || seg == indexTop)));

                bufferBuilder.vertex(x + Math.cos(rad) * radius / 2.3F, y + Math.sin(rad) * radius / 2.3F, 0).color(r, g, b, a).endVertex();
                bufferBuilder.vertex(xp, yp, 0).color(r, g, b, a).endVertex();
            }

            totalDeg += degPer;
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }

        tessellator.end();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();

        for (int i = 0; i < nameData.size(); i++) {
            matrices.pushPose();
            NameDisplayData data = nameData.get(i);
            int xp = data.getX();
            int yp = data.getY();

            String name;
            if (tool.getItem() instanceof GadgetBuilding)
                name = I18n.get(BuildingModes.values()[i].getTranslationKey());
            else if (tool.getItem() instanceof GadgetExchanger)
                name = I18n.get(ExchangingModes.values()[i].getTranslationKey());
            else
                name = GadgetCopyPaste.ToolMode.values()[i].getTranslation().format();

            int xsp = xp - 4;
            int ysp = yp;
            int width = font.width(name);

            if (xsp < x)
                xsp -= width - 8;
            if (ysp < y)
                ysp -= 9;

            Color color = i == modeIndex ? Color.GREEN : Color.WHITE;
            if (data.isSelected())
                font.drawShadow(matrices, name, xsp + (data.isCentralized() ? width / 2f - 4 : 0), ysp, color.getRGB());

            double mod = 0.7;
            int xdp = (int) ((xp - x) * mod + x);
            int ydp = (int) ((yp - y) * mod + y);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, 1);
            RenderSystem.setShaderTexture(0, signs.get(i));
            blit(matrices, xdp - 8, ydp - 8, 0, 0, 16, 16, 16, 16);

            matrices.popPose();
        }

        float s = 2.25F * fract;

        PoseStack stack = RenderSystem.getModelViewStack();
        stack.pushPose();
        stack.scale(s, s, s);
        stack.translate(x / s - (tool.getItem() instanceof GadgetCopyPaste ? 8 : 8.5), y / s - 8, 0);
        this.itemRenderer.renderAndDecorateItem(tool, 0, 0);
        stack.popPose();
    }

    private boolean isCursorInSlice(float angle, float totalDeg, float degPer, boolean inRange) {
        return inRange && angle > totalDeg && angle < totalDeg + degPer;
    }

    private void changeMode() {
        if (slotSelected >= 0) {
            Item gadget = getGadget().getItem();

            // This should logically never fail but implementing a way to ensure that would
            // be a pretty solid idea for the next guy to touch this code.
            String mode;
            if (gadget instanceof GadgetBuilding)
                mode = I18n.get(BuildingModes.values()[slotSelected].getTranslationKey());
            else if (gadget instanceof GadgetExchanger)
                mode = I18n.get(ExchangingModes.values()[slotSelected].getTranslationKey());
            else
                mode = GadgetCopyPaste.ToolMode.values()[slotSelected].getTranslation().format();

            assert Minecraft.getInstance().player != null;
            Minecraft.getInstance().player.displayClientMessage(MessageTranslation.MODE_SET.componentTranslation(mode).setStyle(Styles.AQUA), true);

            PacketToggleMode.send(slotSelected);
            OurSounds.BEEP.playSound();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        changeMode();
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void tick() {
        if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), KeyBindingHelper.getBoundKeyOf(KeyBindings.menuSettings).getValue())) {
            onClose();
            changeMode();
        }

        ImmutableSet<KeyMapping> set = ImmutableSet.of(Minecraft.getInstance().options.keyUp, Minecraft.getInstance().options.keyLeft, Minecraft.getInstance().options.keyDown, Minecraft.getInstance().options.keyRight, Minecraft.getInstance().options.keyShift, Minecraft.getInstance().options.keySprint, Minecraft.getInstance().options.keyJump);
        for (KeyMapping k : set)
            KeyMapping.set(KeyBindingHelper.getBoundKeyOf(k), k.isDown());

        timeIn++;
        ItemStack tool = getGadget();
        boolean builder = tool.getItem() instanceof GadgetBuilding;
        if (!builder && !(tool.getItem() instanceof GadgetExchanger))
            return;

        boolean current;
        boolean changed = false;
        for (int i = 0; i < conditionalButtons.size(); i++) {
            Button button = conditionalButtons.get(i);
            if (builder)
                current = GadgetBuilding.getToolMode(tool) == BuildingModes.SURFACE;
            else
                current = i == 0 || GadgetExchanger.getToolMode(tool) == ExchangingModes.SURFACE;

            if (button.visible != current) {
                button.visible = current;
                changed = true;
            }
        }
        if (changed)
            updateButtons(tool);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static float mouseAngle(int x, int y, int mx, int my) {
        Vector2f baseVec = new Vector2f(1F, 0F);
        Vector2f mouseVec = new Vector2f(mx - x, my - y);

        float ang = (float) (Math.acos(baseVec.dot(mouseVec) / (baseVec.length() * mouseVec.length())) * (180F / Math.PI));
        return my < y ? 360F - ang : ang;
    }

    private void sendRangeUpdate(int valueNew) {
        if (valueNew != GadgetUtils.getToolRange(getGadget()))
            PacketChangeRange.send(valueNew);
    }

    private record NameDisplayData(int x, int y, boolean selected, boolean centralize) {

        private int getX() {
            return x();
        }

        private int getY() {
            return y();
        }

        private boolean isSelected() {
            return selected();
        }

        private boolean isCentralized() {
            return centralize();
        }
    }

    private static class PositionedIconActionable extends GuiIconActionable {
        private final ScreenPosition position;

        PositionedIconActionable(RadialTranslation message, String icon, ScreenPosition position, boolean isSelectable, Predicate<Boolean> action) {
            super(0, 0, icon, message.componentTranslation(), isSelectable, action);

            this.position = position;
        }

        PositionedIconActionable(RadialTranslation message, String icon, ScreenPosition position, Predicate<Boolean> action) {
            this(message, icon, position, true, action);
        }
    }

    private static class Vector2f {
        public final float x;
        public final float y;

        public Vector2f(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public final float dot(Vector2f v1) {
            return (this.x * v1.x + this.y * v1.y);
        }

        public final float length() {
            return (float) Math.sqrt(this.x * this.x + this.y * this.y);
        }
    }
}
