package com.direwolf20.buildinggadgets.common.tainted.registry;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataFactory;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.TileSupport;
import com.direwolf20.buildinggadgets.common.tainted.template.SerialisationSupport;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import com.google.common.base.Preconditions;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

public final class Registries {

    private Registries() {
    }

    private static TopologicalRegistryBuilder<ITileDataFactory> tileDataFactoryBuilder = TopologicalRegistryBuilder.create();

    private static final MappedRegistry<ITileDataSerializer> tileDataSerializers = FabricRegistryBuilder.createSimple(ITileDataSerializer.class, Reference.TileDataSerializerReference.REGISTRY_ID_TILE_DATA_SERIALIZER).attribute(RegistryAttribute.MODDED).buildAndRegister();
    private static ImmutableOrderedRegistry<ITileDataFactory> tileDataFactories = null;

    static {
        addDefaultOrdered();
    }

    public static void registerTileDataSerializers() {
        BuildingGadgets.LOG.trace("Registering TemplateItem Serializers");
        Registry.register(tileDataSerializers, Reference.TileDataSerializerReference.DUMMY_SERIALIZER_RL, SerialisationSupport.dummyDataSerializer());
        Registry.register(tileDataSerializers, Reference.TileDataSerializerReference.NBT_TILE_ENTITY_DATA_SERIALIZER_RL, SerialisationSupport.nbtTileDataSerializer());
        BuildingGadgets.LOG.trace("Finished Registering TemplateItem Serializers");
    }

    public static void createOrderedRegistries() {
        BuildingGadgets.LOG.trace("Creating Ordered Registries");
        Preconditions.checkState(tileDataFactoryBuilder != null, "Cannot create Ordered Registries twice!");
        tileDataFactories = tileDataFactoryBuilder.build();
        tileDataFactoryBuilder = null;
        BuildingGadgets.LOG.trace("Finished Creating Ordered Registries");
    }

    private static void addDefaultOrdered() {
        tileDataFactoryBuilder
                .addMarker(Reference.MARKER_BEFORE_RL)
                .addMarker(Reference.MARKER_AFTER_RL)
                .addValue(Reference.TileDataFactoryReference.DATA_PROVIDER_FACTORY_RL, TileSupport.dataProviderFactory())
                .addDependency(Reference.MARKER_AFTER_RL, Reference.TileDataFactoryReference.DATA_PROVIDER_FACTORY_RL)
                .addDependency(Reference.MARKER_BEFORE_RL, Reference.MARKER_AFTER_RL);
    }

    public static final class TileEntityData {
        private TileEntityData() {
        }

        public static ImmutableOrderedRegistry<ITileDataFactory> getTileDataFactories() {
            Preconditions
                    .checkState(tileDataFactories != null, "Attempted to retrieve TileDataFactoryRegistry before it was created!");
            return tileDataFactories;
        }

        public static MappedRegistry<ITileDataSerializer> getTileDataSerializers() {
            return tileDataSerializers;
        }
    }
}
