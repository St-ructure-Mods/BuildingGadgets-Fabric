package com.direwolf20.buildinggadgets.common.entities;

import com.direwolf20.buildinggadgets.common.util.ref.Reference.EntityReference;
import dev.architectury.registry.level.entity.EntityRendererRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class OurEntities {

    private static final EntityType<ConstructionBlockEntity> CONSTRUCTION = Registry.register(Registry.ENTITY_TYPE, EntityReference.CONSTRUCTION_BLOCK_ENTITY_RL,
            EntityType.Builder.<ConstructionBlockEntity>of(ConstructionBlockEntity::new, MobCategory.MISC)
                    // .setTrackingRange(64)
                    .updateInterval(1)
                    // .setShouldReceiveVelocityUpdates(false)
                    // .setCustomClientFactory(((spawnEntity, world) -> new ConstructionBlockEntity(ConstructionBlockEntity.TYPE, world)))
                    .build(""));

    private OurEntities() {
    }

    public static void registerClient() {
        EntityRendererRegistry.register(CONSTRUCTION, ConstructionBlockEntityRender::new);
    }
}
