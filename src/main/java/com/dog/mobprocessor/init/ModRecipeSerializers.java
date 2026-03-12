package com.dog.mobprocessor.init;

import com.dog.mobprocessor.MobProcessor;
import com.dog.mobprocessor.recipe.AugmentEggRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MobProcessor.MOD_ID);

    public static final RegistryObject<RecipeSerializer<?>> AUGMENT_EGG =
            RECIPE_SERIALIZERS.register("augment_egg",
                    () -> new SimpleRecipeSerializer<>(AugmentEggRecipe::new));
}
