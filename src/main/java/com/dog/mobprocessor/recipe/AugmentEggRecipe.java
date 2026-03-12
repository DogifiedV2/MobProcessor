package com.dog.mobprocessor.recipe;

import com.dog.mobprocessor.config.MobProcessorConfig;
import com.dog.mobprocessor.init.ModRecipeSerializers;
import com.dog.mobprocessor.item.AugmentEggItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class AugmentEggRecipe extends CustomRecipe {

    public AugmentEggRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        for (int xOff = 0; xOff <= container.getWidth() - 3; xOff++) {
            for (int yOff = 0; yOff <= container.getHeight() - 3; yOff++) {
                if (matchesAt(container, xOff, yOff)) return true;
            }
        }
        return false;
    }

    private boolean matchesAt(CraftingContainer container, int xOff, int yOff) {
        Item centerItem = getCenterItem();
        EntityType<?> eggType = null;

        for (int x = 0; x < container.getWidth(); x++) {
            for (int y = 0; y < container.getHeight(); y++) {
                ItemStack stack = container.getItem(x + y * container.getWidth());
                int relX = x - xOff;
                int relY = y - yOff;

                if (relX >= 0 && relX < 3 && relY >= 0 && relY < 3) {
                    int patternPos = relX + relY * 3;

                    if (patternPos == 4) {
                        if (!stack.is(centerItem)) return false;
                    } else if (patternPos == 1 || patternPos == 3 || patternPos == 5 || patternPos == 7) {
                        if (!(stack.getItem() instanceof SpawnEggItem spawnEgg)) return false;
                        EntityType<?> type = spawnEgg.getType(stack.getTag());
                        if (eggType == null) {
                            eggType = type;
                        } else if (type != eggType) {
                            return false;
                        }
                    } else {
                        if (!stack.isEmpty()) return false;
                    }
                } else {
                    if (!stack.isEmpty()) return false;
                }
            }
        }

        return eggType != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        for (int xOff = 0; xOff <= container.getWidth() - 3; xOff++) {
            for (int yOff = 0; yOff <= container.getHeight() - 3; yOff++) {
                if (matchesAt(container, xOff, yOff)) {
                    int eggIndex = (xOff + 1) + yOff * container.getWidth();
                    ItemStack eggStack = container.getItem(eggIndex);
                    if (eggStack.getItem() instanceof SpawnEggItem spawnEgg) {
                        return AugmentEggItem.createForEntity(spawnEgg.getType(eggStack.getTag()));
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.AUGMENT_EGG.get();
    }

    private Item getCenterItem() {
        String itemId = MobProcessorConfig.AUGMENT_EGG_CENTER_ITEM.get();
        ResourceLocation resourceLocation = ResourceLocation.tryParse(itemId);
        if (resourceLocation == null) return Items.DIAMOND_BLOCK;
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
        return item != null ? item : Items.DIAMOND_BLOCK;
    }
}
