package com.direwolf20.buildinggadgets.client.cache;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.network.Target;
import com.direwolf20.buildinggadgets.common.network.bidirection.PacketRequestTemplate;
import com.direwolf20.buildinggadgets.common.network.bidirection.SplitPacketUpdateTemplate;
import com.direwolf20.buildinggadgets.common.tainted.Tainted;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateKey;
import com.direwolf20.buildinggadgets.common.tainted.template.ITemplateProvider;
import com.direwolf20.buildinggadgets.common.tainted.template.Template;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Tainted(reason = "Uses template system")
public final class CacheTemplateProvider implements ITemplateProvider {
    private final Cache<UUID, Template> cache;
    private final Set<IUpdateListener> updateListeners;

    public CacheTemplateProvider() {
        this.cache = CacheBuilder
                .newBuilder()
                .expireAfterAccess(1, TimeUnit.MINUTES)
                .build();
        this.updateListeners = Collections.newSetFromMap(new WeakHashMap<>());
    }

    @Override
    @NotNull
    public Template getTemplateForKey(@NotNull ITemplateKey key) {
        UUID id = getId(key);

        try {
            return cache.get(id, () -> {
                requestUpdate(id, new Target(PacketFlow.SERVERBOUND, null));
                return new Template();
            });
        } catch (ExecutionException e) {
            BuildingGadgets.LOG.error("Failed to access Cache! Returning new Template, this is certainly going to cause unexpected behaviour!", e);
            return new Template();
        }
    }

    @Override
    public void setTemplate(ITemplateKey key, Template template) {
        UUID id = getId(key);
        cache.put(id, template);
        notifyListeners(key, template, l -> l::onTemplateUpdate);
    }

    @Override
    public boolean requestUpdate(ITemplateKey key) {
        return requestUpdate(key, new Target(PacketFlow.SERVERBOUND, null));
    }

    @Override
    public boolean requestUpdate(ITemplateKey key, Target target) {
        return requestUpdate(key.getOrComputeId(UUID::randomUUID), target);
    }

    private boolean requestUpdate(UUID id, Target target) {
        PacketRequestTemplate.sendToTarget(target, id);
        return true;
    }

    @Override
    public boolean requestRemoteUpdate(ITemplateKey key, Target target) {
        UUID id = getId(key);
        Template template = cache.getIfPresent(id);
        if (template != null) {
            notifyListeners(key, template, l -> l::onTemplateUpdateSend);
            SplitPacketUpdateTemplate.sendToTarget(target, id, template);
        }
        return template != null;
    }

    public boolean requestRemoteUpdate(ITemplateKey key, Level level) {
        return requestRemoteUpdate(key, new Target(PacketFlow.SERVERBOUND, null));
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
        return key.getOrComputeId(UUID::randomUUID);
    }

    /**
     * Although public, do not use this method willingly. The cache is already purged on each
     * onPlayerLoggedOut event.
     */
    public void clear() {
        this.cache.invalidateAll();
        this.cache.cleanUp();
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

    }

    @Override
    public void writeToNbt(CompoundTag tag) {

    }
}
