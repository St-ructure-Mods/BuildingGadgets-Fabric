package com.direwolf20.buildinggadgets.client.screen.components;

import com.direwolf20.buildinggadgets.client.BuildingGadgetsClient;
import com.direwolf20.buildinggadgets.client.screen.GuiMod;
import com.direwolf20.buildinggadgets.common.network.C2S.PacketChangeRange;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

import java.awt.*;
import java.util.Collection;
import java.util.function.BiConsumer;

public class GuiSliderInt extends AbstractSliderButton {
    private final int colorBackground;
    private final int colorSliderBackground;
    private final int colorSlider;
    private final BiConsumer<GuiSliderInt, Integer> increment;
    private double value;
    private final double minVal;
    private final double maxVal;

    public GuiSliderInt(int xPos, int yPos, int width, int height, Component prefix, double minVal, double maxVal,
                        double currentVal, Color color,
                        BiConsumer<GuiSliderInt, Integer> increment) {

        super(xPos, yPos, width, height, prefix, currentVal);

        colorBackground = GuiMod.getColor(color, 200).getRGB();
        colorSliderBackground = GuiMod.getColor(color.darker(), 200).getRGB();
        colorSlider = GuiMod.getColor(color.brighter().brighter(), 200).getRGB();
        this.minVal = minVal;
        this.maxVal = maxVal;

        this.increment = increment;
    }

    //copied from AbstractSliderButton because it was private and i couldnt be bother accesswidening it
    public void setValue(int value) {
        double e = this.value;
        this.value = Mth.clamp(value, minVal, maxVal);
        if (e != this.value) {
            this.applyValue();
        }

        this.updateMessage();
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        setValue(getValueInt());
    }

    public int getValueInt() {
        return (int) value;
    }

    @Override
    protected void updateMessage() {

    }

    @Override
    public void applyValue() {
        playSound();
        PacketChangeRange.send((int) value);
    }

    private void playSound() {
        BuildingGadgetsClient.playSound(SoundEvents.DISPENSER_FAIL, 2F);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partial) {
        if (!visible)
            return;

        Minecraft mc = Minecraft.getInstance();
        isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
        fill(matrices, x, y, x + width, y + height, colorBackground);
        renderBg(matrices, mc, mouseX, mouseY);
        renderText(matrices, mc, this);
    }

    private void renderText(PoseStack matrices, Minecraft mc, AbstractWidget component) {
        int color = ! active ? 10526880 : (isHovered ? 16777120 : - 1);
        String buttonText = component.getMessage().getString();
        int strWidth = mc.font.width(buttonText);
        int ellipsisWidth = mc.font.width("...");
        if (strWidth > component.getWidth() - 6 && strWidth > ellipsisWidth)
            buttonText = mc.font.plainSubstrByWidth(buttonText, component.getWidth() - 6 - ellipsisWidth).trim() + "...";

        drawCenteredString(matrices, mc.font, buttonText, component.x + component.getWidth() / 2, component.y + (component.getHeight() - 8) / 2, color);
    }

    @Override
    public void playDownSound(SoundManager p_playDownSound_1_) { }

    @Override
    protected void renderBg(PoseStack matrices, Minecraft mc, int mouseX, int mouseY) {
        if (!visible)
            return;

        drawBorderedRect(matrices, (int) (x + (value * (width - 8))), y, 8, height);
    }

    private void drawBorderedRect(PoseStack matrices, int x, int y, int width, int height) {
        fill(matrices, x, y, x + width, y + height, colorSliderBackground);
        fill(matrices, ++ x, ++ y, x + width - 2, y + height - 2, colorSlider);
    }

    public Collection<AbstractWidget> getComponents() {
        return ImmutableSet.of(
                this,
                new GuiButtonIncrement(this, x - height, y, width, height, new TextComponent("-"), b -> increment.accept(this, - 1)),
                new GuiButtonIncrement(this, x + width, y, width, height, new TextComponent("+"), b -> increment.accept(this, 1)
        ));
    }

    private static class GuiButtonIncrement extends Button {
        private final GuiSliderInt parent;

        public GuiButtonIncrement(GuiSliderInt parent, int x, int y, int width, int height, Component buttonText, OnPress action) {
            super(x, y, width, height, buttonText, action);
            this.parent = parent;
        }

        @Override
        public void render(PoseStack matrices, int mouseX, int mouseY, float partial) {
            if (!visible)
                return;

            Minecraft mc = Minecraft.getInstance();
            isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            fill(matrices, x, y, x + width, y + height, parent.colorBackground);
            parent.drawBorderedRect(matrices, x, y, width, height);
            parent.renderText(matrices, mc, this);
        }

        @Override
        public void playDownSound(SoundManager p_playDownSound_1_) { }
    }
}
