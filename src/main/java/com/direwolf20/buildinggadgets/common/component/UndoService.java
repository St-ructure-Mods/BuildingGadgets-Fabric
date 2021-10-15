package com.direwolf20.buildinggadgets.common.component;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.save.Undo;
import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.*;

public final class UndoService implements Component, ServerTickingComponent {

    private final Map<UUID, Deque<UndoData>> histories;
    private int tick;

    public UndoService() {
        this.histories = new HashMap<>();
    }

    public void insertUndo(UUID uuid, Undo undo) {
        Deque<UndoData> deque = histories.computeIfAbsent(uuid, $ -> new LinkedList<>());
        deque.push(new UndoData(System.currentTimeMillis() + BuildingGadgets.getConfig().gadgets.undoExpiry, undo));

        // Remove too many elements
        while (deque.size() > BuildingGadgets.getConfig().gadgets.undoSize) {
            deque.removeLast();
        }
    }

    public Optional<Undo> getUndo(UUID uuid) {
        Deque<UndoData> deque = histories.get(uuid);

        if (deque != null) {
            UndoData poll = deque.poll();

            if (poll != null) {
                return Optional.ofNullable(poll.undo());
            }
        }

        return Optional.empty();
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        histories.clear();

        for (String key : tag.getAllKeys()) {
            LinkedList<UndoData> history = new LinkedList<>();

            for (Tag d : tag.getList(key, 0)) {
                CompoundTag data = (CompoundTag) d;
                history.add(new UndoData(data.getLong("Expiry"), Undo.deserialize(data.getCompound("Undo"))));
            }

            histories.put(UUID.fromString(key), history);
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        histories.forEach((uuid, history) -> {
            ListTag list = new ListTag();

            for (UndoData data : history) {
                CompoundTag inner = new CompoundTag();
                inner.putLong("Expiry", data.expiry());
                inner.put("Undo", data.undo().serialize());
                list.add(inner);
            }

            tag.put(uuid.toString(), list);
        });
    }

    @Override
    public void serverTick() {
        // Only check every 30 seconds
        if (++tick % 600 == 0) {
            long now = System.currentTimeMillis();

            histories.entrySet().removeIf(entry -> {
                entry.getValue().removeIf(data -> data.expiry >= now);
                return entry.getValue().isEmpty();
            });
        }
    }

    private record UndoData(long expiry, Undo undo) {
    }
}
