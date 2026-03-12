package com.dog.mobprocessor.item;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SpawnEggLookup {

    private static Map<EntityType<?>, SpawnEggItem> cache;

    @Nullable
    public static SpawnEggItem getEggForEntity(EntityType<?> entityType) {
        SpawnEggItem vanilla = SpawnEggItem.byId(entityType);
        if (vanilla != null) return vanilla;
        return getCache().get(entityType);
    }

    public static Map<EntityType<?>, SpawnEggItem> getCache() {
        if (cache == null) {
            rebuildCache();
        }
        return cache;
    }

    public static void rebuildCache() {
        cache = new HashMap<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof SpawnEggItem spawnEgg) {
                try {
                    EntityType<?> type = spawnEgg.getType(null);
                    if (type != null) {
                        cache.putIfAbsent(type, spawnEgg);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }
}
