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
import java.util.function.Supplier;

public final class Registries {

    private Registries() {}

    private static TopologicalRegistryBuilder<ITileDataFactory> tileDataFactoryBuilder = TopologicalRegistryBuilder.create();
    private static TopologicalRegistryBuilder<IHandleProvider> handleProviderBuilder = TopologicalRegistryBuilder.create();

    private static IForgeRegistry<ITileDataSerializer> tileDataSerializers = null;
    private static IForgeRegistry<IUniqueObjectSerializer> uniqueObjectSerializers = null;
    private static ImmutableOrderedRegistry<ITileDataFactory> tileDataFactories = null;
    private static ImmutableOrderedRegistry<IHandleProvider> handleProviders = null;

    static {
        addDefaultOrdered();
    }

    public static IForgeRegistry<IUniqueObjectSerializer> getUniqueObjectSerializers() {
        Preconditions
                .checkState(uniqueObjectSerializers != null, "Attempted to retrieve UniqueObjectSerializerRegistry before registries were created!");
        return uniqueObjectSerializers;
    }

    public static void onCreateRegistries() {
        BuildingGadgets.LOG.trace("Creating ForgeRegistries");
        tileDataSerializers = new RegistryBuilder<ITileDataSerializer>()
                .setType(ITileDataSerializer.class)
                .setName(Reference.TileDataSerializerReference.REGISTRY_ID_TILE_DATA_SERIALIZER)
                .create();
        uniqueObjectSerializers = new RegistryBuilder<IUniqueObjectSerializer>()
                .setType(IUniqueObjectSerializer.class)
                .setName(Reference.UniqueObjectSerializerReference.REGISTRY_ID_UNIQUE_OBJECT_SERIALIZER)
                .create();
        BuildingGadgets.LOG.trace("Finished Creating ForgeRegistries");
    }

    public static void registerTileDataSerializers() {
        BuildingGadgets.LOG.trace("Registering TemplateItem Serializers");
        MappedRegistry<ITileDataSerializer> template_serializer = FabricRegistryBuilder.createSimple(ITileDataSerializer.class, BuildingGadgets.id("template_serializer")).attribute(RegistryAttribute.MODDED).buildAndRegister();
        Registry.register(template_serializer, BuildingGadgets.id("dummy_data_serializer"), SerialisationSupport.dummyDataSerializer());
        Registry.register(template_serializer, BuildingGadgets.id("nbt_tile_data_serializer"), SerialisationSupport.nbtTileDataSerializer());
        BuildingGadgets.LOG.trace("Finished Registering TemplateItem Serializers");
    }

    public static void registerUniqueObjectSerializers() {
        BuildingGadgets.LOG.trace("Registering UniqueObject Serializers");
        MappedRegistry<IUniqueObjectSerializer> uniqueObjectSerializer = FabricRegistryBuilder.createSimple(IUniqueObjectSerializer.class, BuildingGadgets.id("unique_object_serializer")).attribute(RegistryAttribute.MODDED).buildAndRegister();
        Registry.register(uniqueObjectSerializer, BuildingGadgets.id("item_serializer"), SerialisationSupport.uniqueItemSerializer());
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

    public static boolean handleIMC(InterModComms.IMCMessage message) {
        BuildingGadgets.LOG.debug("Received IMC message using Method {} from {}.", message.getMethod(), message.getSenderModId());
        if (message.getMethod().equals(Reference.TileDataFactoryReference.IMC_METHOD_TILEDATA_FACTORY)) {
            BuildingGadgets.LOG.debug("Recognized ITileDataFactory registration message. Registering.");
            Preconditions.checkState(tileDataFactoryBuilder != null,
                    "Attempted to register ITileDataFactory, after the Registry has been built!");
            TopologicalRegistryBuilder<ITileDataFactory> builder = message.<Supplier<TopologicalRegistryBuilder<ITileDataFactory>>>getMessageSupplier().get().get();
            tileDataFactoryBuilder.merge(builder);
            BuildingGadgets.LOG.trace("Registered {} from {} to the ITileDataFactory registry.", builder, message.getSenderModId());
            return true;
        } else if (message.getMethod().equals(Reference.HandleProviderReference.IMC_METHOD_HANDLE_PROVIDER)) {
            BuildingGadgets.LOG.debug("Recognized IHandleProvider registration message. Registering.");
            Preconditions.checkState(handleProviderBuilder != null,
                    "Attempted to register IHandleProvider, after the Registry has been built!");
            TopologicalRegistryBuilder<IHandleProvider> builder = message.<Supplier<TopologicalRegistryBuilder<IHandleProvider>>>getMessageSupplier().get().get();
            handleProviderBuilder.merge(builder);
            BuildingGadgets.LOG.trace("Registered {} from {} to the IHandleProvider registry.", builder, message.getSenderModId());
            return true;
        }
        return false;
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

        public static IForgeRegistry<ITileDataSerializer> getTileDataSerializers() {
            Preconditions
                    .checkState(tileDataSerializers != null, "Attempted to retrieve TileDataSerializerRegistry before registries were created!");
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
