package com.direwolf20.buildinggadgets.common.compat;

import com.direwolf20.buildinggadgets.common.network.Target;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class NoWorldCompat implements ITemplateProvider {
    @Override
    public UUID getId(ITemplateKey key) {
        return null;
    }

    @Override
    public Template getTemplateForKey(ITemplateKey key) {
        return null;
    }

    @Override
    public void setTemplate(ITemplateKey key, Template template) {

    }

    @Override
    public boolean requestUpdate(ITemplateKey key) {
        return false;
    }

    @Override
    public boolean requestUpdate(ITemplateKey key, Target target) {
        return false;
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key, Level level) {
        return false;
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key, Target target) {
        return false;
    }

    @Override
    public void registerUpdateListener(IUpdateListener listener) {

    }

    @Override
    public void removeUpdateListener(IUpdateListener listener) {

    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {

    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {

    }
}
