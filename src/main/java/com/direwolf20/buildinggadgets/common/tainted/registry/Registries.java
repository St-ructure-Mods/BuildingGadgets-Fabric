package com.direwolf20.buildinggadgets.common.tainted.registry;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IHandleProvider;
import com.direwolf20.buildinggadgets.common.tainted.inventory.handle.IObjectHandle;
import com.direwolf20.buildinggadgets.common.tainted.inventory.materials.objects.IUniqueObjectSerializer;
import com.direwolf20.buildinggadgets.common.tainted.template.SerialisationSupport;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Registries {

    private Registries() {}

    private static TopologicalRegistryBuilder<ITileDataFactory> tileDataFactoryBuilder = TopologicalRegistryBuilder.create();
    private static TopologicalRegistryBuilder<IHandleProvider> handleProviderBuilder = TopologicalRegistryBuilder.create();

    private static final MappedRegistry<ITileDataSerializer> tileDataSerializers = FabricRegistryBuilder.createSimple(ITileDataSerializer.class, Reference.TileDataSerializerReference.REGISTRY_ID_TILE_DATA_SERIALIZER).attribute(RegistryAttribute.MODDED).buildAndRegister();
    private static final MappedRegistry<IUniqueObjectSerializer> uniqueObjectSerializers = FabricRegistryBuilder.createSimple(IUniqueObjectSerializer.class, Reference.UniqueObjectSerializerReference.REGISTRY_ID_UNIQUE_OBJECT_SERIALIZER).attribute(RegistryAttribute.MODDED).buildAndRegister();
    private static ImmutableOrderedRegistry<ITileDataFactory> tileDataFactories = null;
    private static ImmutableOrderedRegistry<IHandleProvider> handleProviders = null;

    static {
        addDefaultOrdered();
    }

    public static MappedRegistry<IUniqueObjectSerializer> getUniqueObjectSerializers() {
        return uniqueObjectSerializers;
    }

    public static void registerTileDataSerializers() {
        BuildingGadgets.LOG.trace("Registering TemplateItem Serializers");
        Registry.register(tileDataSerializers, Reference.TileDataSerializerReference.DUMMY_SERIALIZER_RL, SerialisationSupport.dummyDataSerializer());
        Registry.register(tileDataSerializers, Reference.TileDataSerializerReference.NBT_TILE_ENTITY_DATA_SERIALIZER_RL, SerialisationSupport.nbtTileDataSerializer());
        BuildingGadgets.LOG.trace("Finished Registering TemplateItem Serializers");
    }

    public static void registerUniqueObjectSerializers() {
        BuildingGadgets.LOG.trace("Registering UniqueObject Serializers");
        Registry.register(uniqueObjectSerializers, Reference.UniqueObjectSerializerReference.SIMPLE_UNIQUE_ITEM_ID_RL, SerialisationSupport.uniqueItemSerializer());
        BuildingGadgets.LOG.trace("Finished Registering UniqueObject Serializers");
    }

    public static void createOrderedRegistries() {
        BuildingGadgets.LOG.trace("Creating Ordered Registries");
        Preconditions.checkState(tileDataFactoryBuilder != null, "Cannot create Ordered Registries twice!");
        tileDataFactories = tileDataFactoryBuilder.build();
        tileDataFactoryBuilder = null;
        handleProviders = handleProviderBuilder.build();
        handleProviderBuilder = null;
        BuildingGadgets.LOG.trace("Finished Creating Ordered Registries");
    }

    private static void addDefaultOrdered() {
        tileDataFactoryBuilder
                .addMarker(Reference.MARKER_BEFORE_RL)
                .addMarker(Reference.MARKER_AFTER_RL)
                .addValue(Reference.TileDataFactoryReference.DATA_PROVIDER_FACTORY_RL, TileSupport.dataProviderFactory())
                .addDependency(Reference.MARKER_AFTER_RL, Reference.TileDataFactoryReference.DATA_PROVIDER_FACTORY_RL)
                .addDependency(Reference.MARKER_BEFORE_RL, Reference.MARKER_AFTER_RL);
        handleProviderBuilder
                .addMarker(Reference.MARKER_BEFORE_RL)
                .addMarker(Reference.MARKER_AFTER_RL)
                .addDependency(Reference.MARKER_BEFORE_RL, Reference.MARKER_AFTER_RL);
    }

    public static final class TileEntityData {
        private TileEntityData() {}

        public static ImmutableOrderedRegistry<ITileDataFactory> getTileDataFactories() {
            Preconditions
                    .checkState(tileDataFactories != null, "Attempted to retrieve TileDataFactoryRegistry before it was created!");
            return tileDataFactories;
        }

        public static MappedRegistry<ITileDataSerializer> getTileDataSerializers() {
            return tileDataSerializers;
        }
    }

    public static final class HandleProvider {
        private HandleProvider() {}

        public static ImmutableOrderedRegistry<IHandleProvider> getHandleProviders() {
            Preconditions
                    .checkState(tileDataFactories != null, "Attempted to retrieve HandleProviderRegistry before it was created!");
            return handleProviders;
        }

        public static boolean indexCapProvider(ICapabilityProvider provider, Map<Class<?>, Map<Object, List<IObjectHandle>>> indexMap) {
            Set<Class<?>> evaluatedClasses = new HashSet<>();
            boolean indexed = false;
            for (IHandleProvider handleProvider : getHandleProviders().getValuesInOrder()) {
                indexed |= handleProvider.index(provider, indexMap, evaluatedClasses);
            }
            return indexed;
        }
    }
}
