package com.direwolf20.buildinggadgets.client.cache;

import com.direwolf20.buildinggadgets.common.network.bidirection.PacketSetRemoteInventoryCache;
import com.direwolf20.buildinggadgets.common.tainted.inventory.InventoryLinker;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Multiset;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class RemoteInventoryCache {

    private final Stopwatch timer = Stopwatch.createStarted();
    private final boolean isCopyPaste;

    private boolean forceUpdate;
    private InventoryLinker.InventoryLink location;
    private Multiset<ItemVariant> cache;

    public RemoteInventoryCache(boolean isCopyPaste) {
        this.isCopyPaste = isCopyPaste;
    }

    public void setCache(Multiset<ItemVariant> cache) {
        this.cache = cache;
    }

    public void forceUpdate() {
        forceUpdate = true;
    }

    public boolean maintainCache(ItemStack gadget) {
        InventoryLinker.InventoryLink loc = InventoryLinker.getDataFromStack(gadget);

        if (isCacheOld(loc)) {
            updateCache(loc);
        }

        return loc != null;
    }

    public Multiset<ItemVariant> getCache() {
        return cache;
    }

    private void updateCache(InventoryLinker.InventoryLink loc) {
        location = loc;

        if (loc == null) {
            cache = null;
        } else {
            PacketSetRemoteInventoryCache.send(isCopyPaste, loc);
        }
    }

    private boolean isCacheOld(@Nullable InventoryLinker.InventoryLink loc) {
        if (forceUpdate || !Objects.equals(location, loc) || timer.elapsed(TimeUnit.MILLISECONDS) >= 5000) {
            timer.reset();
            timer.start();
            forceUpdate = false;
            return true;
        } else {
            return false;
        }
    }
}
