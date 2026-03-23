package com.dog.mobprocessor.compat.jei;

import com.dog.mobprocessor.MobProcessor;
import com.dog.mobprocessor.init.ModBlocks;
import com.dog.mobprocessor.init.ModItems;
import com.dog.mobprocessor.item.AugmentEggItem;
import com.dog.mobprocessor.item.SpawnEggLookup;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

@JeiPlugin
public class MobProcessorJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MobProcessor.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ModItems.AUGMENT_EGG.get(), (stack, context) -> {
            EntityType<?> entityType = AugmentEggItem.getEntityType(stack);
            if (entityType == null) return IIngredientSubtypeInterpreter.NONE;
            return EntityType.getKey(entityType).toString();
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new AugmentEggRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<AugmentEggJeiRecipe> recipes = SpawnEggLookup.getCache().keySet().stream()
                .map(AugmentEggJeiRecipe::new)
                .toList();

        registration.addRecipes(AugmentEggRecipeCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.CRAFTING_TABLE), AugmentEggRecipeCategory.RECIPE_TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MOB_PROCESSOR_ITEM.get()), AugmentEggRecipeCategory.RECIPE_TYPE);
    }
}
