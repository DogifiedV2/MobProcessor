package com.dog.mobprocessor.item;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class AugmentEggItem extends Item {

    public static final String ENTITY_TYPE_TAG = "EntityType";

    public AugmentEggItem() {
        super(new Properties().tab(com.dog.mobprocessor.init.ModCreativeTab.TAB));
    }

    public static ItemStack createForEntity(EntityType<?> entityType) {
        ItemStack stack = new ItemStack(com.dog.mobprocessor.init.ModItems.AUGMENT_EGG.get());
        stack.getOrCreateTag().putString(ENTITY_TYPE_TAG, EntityType.getKey(entityType).toString());
        return stack;
    }

    @Nullable
    public static EntityType<?> getEntityType(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(ENTITY_TYPE_TAG)) return null;
        ResourceLocation entityId = ResourceLocation.tryParse(tag.getString(ENTITY_TYPE_TAG));
        if (entityId == null) return null;
        return ForgeRegistries.ENTITIES.getValue(entityId);
    }

    @Override
    public Component getName(ItemStack stack) {
        EntityType<?> type = getEntityType(stack);
        if (type != null) {
            return new TranslatableComponent("item.mobprocessor.augment_egg.named", type.getDescription());
        }
        return super.getName(stack);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> items) {
        if (this.allowdedIn(tab)) {
            SpawnEggLookup.getCache().keySet().stream()
                    .sorted(Comparator.comparing(type -> EntityType.getKey(type).toString()))
                    .forEach(type -> items.add(createForEntity(type)));
        }
    }
}
