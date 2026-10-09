package com.supertotem;

import com.supertotem.entity.FlashbangEntity;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;

public final class ModEntities {
    public static final ResourceKey<EntityType<?>> FLASHBANG_KEY = ResourceKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(SuperTotemMod.MOD_ID, "flashbang"));

    public static EntityType<FlashbangEntity> FLASHBANG;
    public static Item FLASHBANG_SPAWN_EGG;

    private ModEntities() {}

    public static void register() {
        FLASHBANG = Registry.register(BuiltInRegistries.ENTITY_TYPE, FLASHBANG_KEY,
                EntityType.Builder.of(FlashbangEntity::new, MobCategory.MONSTER)
                        .sized(0.6F, 1.7F)          // hitbox de creeper
                        .clientTrackingRange(8)
                        .build(FLASHBANG_KEY));

        FabricDefaultAttributeRegistry.register(FLASHBANG, FlashbangEntity.createAttributes());

        SpawnPlacements.register(FLASHBANG, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ModEntities::canSpawn);

        // Se anade a todos los biomas (overworld, nether, end, The Beginning...); canSpawn decide cuando.
        BiomeModifications.addSpawn(BiomeSelectors.all(), MobCategory.MONSTER, FLASHBANG, 30, 1, 1);

        // Huevo generador (aparece en la pestana de huevos del creativo)
        ResourceKey<Item> eggKey = ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(SuperTotemMod.MOD_ID, "flashbang_spawn_egg"));
        FLASHBANG_SPAWN_EGG = Registry.register(BuiltInRegistries.ITEM, eggKey,
                new SpawnEggItem(FLASHBANG, new Item.Properties().setId(eggKey)));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS)
                .register(entries -> entries.accept(FLASHBANG_SPAWN_EGG));
    }

    /** Dia 20-49: solo Overworld. Dia 50+: cualquier dimension (incluida The Beginning). */
    private static boolean canSpawn(EntityType<FlashbangEntity> type, ServerLevelAccessor level,
                                    EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        long day = PermadeathDays.day();
        if (day < 20) return false;
        if (day < 50 && level.getLevel().dimension() != Level.OVERWORLD) return false;
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }
}
