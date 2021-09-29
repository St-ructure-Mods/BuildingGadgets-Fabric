package com.direwolf20.buildinggadgets.common.tainted.save;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.util.ref.Reference.SaveReference;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public enum SaveManager {
    INSTANCE;
    private final SaveTemplateProvider templateProvider;
    private TemplateSave templateSave;
    private final List<UndoSaveContainer> undoSaves;

    SaveManager() {
        this.templateProvider = new SaveTemplateProvider(this::getTemplateSave);
        this.undoSaves = new LinkedList<>();
    }

    public Supplier<UndoWorldSave> registerUndoSave(Function<ServerLevel, UndoWorldSave> ctrFun) {
        UndoSaveContainer container = new UndoSaveContainer(Objects.requireNonNull(ctrFun));
        this.undoSaves.add(container);
        return container::getCurrentSave;
    }

    public void onServerStarted() {
        BuildingGadgets.LOG.debug("Loading World Saves.");
        ServerLifecycleEvents.SERVER_STARTED.register((server -> {
            ServerLevel world = server.getLevel(ServerLevel.OVERWORLD);
            for (UndoSaveContainer c : undoSaves) {
                c.acquire(world);
            }
            templateSave = getTemplateSave(world, SaveReference.TEMPLATE_SAVE_TEMPLATES);
        }));

        BuildingGadgets.LOG.debug("Finished Loading saves");
    }

    public void onServerStopped() {
        BuildingGadgets.LOG.debug("Clearing save caches");
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            for (UndoSaveContainer c : undoSaves) {
                c.release();
            }
            templateSave = null;
        });
        BuildingGadgets.LOG.debug("Finished clearing save caches");
    }

    public static UndoWorldSave getUndoSave(ServerLevel world, long maxLengthSupplier, String name) {
        return world.getDataStorage().computeIfAbsent(UndoWorldSave::loads, () -> new UndoWorldSave(maxLengthSupplier), name);
    }

    private static TemplateSave getTemplateSave(ServerLevel world, String name) {
        return world.getDataStorage().computeIfAbsent(TemplateSave::loads, TemplateSave::new, name);
    }

//    private static <T extends SavedData> T get(ServerLevel world, Function<CompoundTag, T> loader, Supplier<T> supplier, String name) {
//        return world.getDataStorage().computeIfAbsent(loader, supplier, name);
//    }

    public SaveTemplateProvider getTemplateProvider() {
        return templateProvider;
    }

    public TemplateSave getTemplateSave() {
        return templateSave;
    }

    private static final class UndoSaveContainer {
        private final Function<ServerLevel, UndoWorldSave> constructor;
        @Nullable
        private UndoWorldSave currentSave;

        private UndoSaveContainer(Function<ServerLevel, UndoWorldSave> constructor) {
            this.constructor = constructor;
            this.currentSave = null;
        }

        private void acquire(ServerLevel world) {
            this.currentSave = constructor.apply(world);
        }

        @Nullable
        private UndoWorldSave getCurrentSave() {
            return currentSave;
        }

        private void release() {
            currentSave = null;
        }
    }
}
