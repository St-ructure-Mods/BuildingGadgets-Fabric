package com.direwolf20.buildinggadgets.common.tainted.save;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.network.Target;
import com.direwolf20.buildinggadgets.common.network.bidirection.PacketRequestTemplate;
import com.direwolf20.buildinggadgets.common.network.bidirection.SplitPacketUpdateTemplate;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Function;

public final class SaveTemplateProvider implements ITemplateProvider {

    private final TemplateSave save;
    private final Set<IUpdateListener> updateListeners;

    public SaveTemplateProvider() {
        this.save = new TemplateSave();
        this.updateListeners = Collections.newSetFromMap(new WeakHashMap<>());
    }

    public TemplateSave getSave() {
        return save;
    }

    @Override
    public Template getTemplateForKey(ITemplateKey key) {
        UUID id = getId(key);
        return getSave().getTemplate(id);
    }

    @Override
    public void setTemplate(ITemplateKey key, Template template) {
        getSave().setTemplate(key.getOrComputeId(this::getFreeId), template);
        notifyListeners(key, template, l -> l::onTemplateUpdate);
    }

    @Override
    public boolean requestUpdate(ITemplateKey key) {
        return false;
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key, Level level) {
        UUID id = getId(key);
        Template template = getSave().getTemplate(id);
        notifyListeners(key, template, l -> l::onTemplateUpdateSend);

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            SplitPacketUpdateTemplate.Server.send(id, template, player);
        }

        return true;
    }

    @Override
    public void registerUpdateListener(IUpdateListener listener) {
        updateListeners.add(listener);
    }

    @Override
    public void removeUpdateListener(IUpdateListener listener) {
        updateListeners.remove(listener);
    }

    @Override
    public UUID getId(ITemplateKey key) {
        return key.getOrComputeId(this::getFreeId);
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key, Target target) {
        UUID id = getId(key);
        Template template = getSave().getTemplate(id);
        SplitPacketUpdateTemplate.sendToTarget(target, id, template);
        return true;
    }

    @Override
    public boolean requestUpdate(ITemplateKey key, Target target) {
        UUID id = getId(key);
        PacketRequestTemplate.sendToTarget(target, id);
        return true;
    }

    private UUID getFreeId() {
        return getSave().getFreeUUID();
    }

    private void notifyListeners(ITemplateKey key, Template template, Function<IUpdateListener, TriConsumer<ITemplateProvider, ITemplateKey, Template>> function) {
        for (IUpdateListener listener : updateListeners) {
            try {
                function.apply(listener).accept(this, key, template);
            } catch (Exception e) {
                BuildingGadgets.LOG.error("Update listener threw an exception!", e);
            }
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        save.load(tag);
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        save.save(tag);
    }
}
