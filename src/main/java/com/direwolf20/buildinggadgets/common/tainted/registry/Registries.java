package com.direwolf20.buildinggadgets.common.tainted.registry;

import com.direwolf20.buildinggadgets.common.BuildingGadgets;
import com.direwolf20.buildinggadgets.common.tainted.building.tilesupport.ITileDataSerializer;
import com.direwolf20.buildinggadgets.common.tainted.template.SerialisationSupport;
import com.direwolf20.buildinggadgets.common.util.ref.Reference;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;

public final class Registries {

    private static final MappedRegistry<ITileDataSerializer> tileDataSerializers = FabricRegistryBuilder.createSimple(ITileDataSerializer.class, Reference.TileDataSerializerReference.REGISTRY_ID_TILE_DATA_SERIALIZER).attribute(RegistryAttribute.MODDED).buildAndRegister();

    public static void registerTileDataSerializers() {
        BuildingGadgets.LOG.trace("Registering TemplateItem Serializers");
        Registry.register(tileDataSerializers, Reference.TileDataSerializerReference.DUMMY_SERIALIZER_RL, SerialisationSupport.dummyDataSerializer());
        Registry.register(tileDataSerializers, Reference.TileDataSerializerReference.NBT_TILE_ENTITY_DATA_SERIALIZER_RL, SerialisationSupport.nbtTileDataSerializer());
        BuildingGadgets.LOG.trace("Finished Registering TemplateItem Serializers");
    }

    public static MappedRegistry<ITileDataSerializer> getTileDataSerializers() {
        return tileDataSerializers;
    }
}
