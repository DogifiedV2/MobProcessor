package com.dog.mobprocessor.compat.jei;

import com.dog.mobprocessor.MobProcessor;
import com.dog.mobprocessor.config.MobProcessorConfig;
import com.dog.mobprocessor.init.ModItems;
import com.dog.mobprocessor.item.AugmentEggItem;
import com.dog.mobprocessor.item.SpawnEggLookup;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.registries.ForgeRegistries;

public class AugmentEggRecipeCategory implements IRecipeCategory<AugmentEggJeiRecipe> {

    public static final ResourceLocation UID = new ResourceLocation(MobProcessor.MOD_ID, "augment_egg");
    public static final RecipeType<AugmentEggJeiRecipe> RECIPE_TYPE = RecipeType.create(MobProcessor.MOD_ID, "augment_egg", AugmentEggJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public AugmentEggRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(116, 54);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.AUGMENT_EGG.get()));
    }

    @Override
    public RecipeType<AugmentEggJeiRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent("jei.mobprocessor.augment_egg");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @SuppressWarnings("removal")
    @Override
    public Class<? extends AugmentEggJeiRecipe> getRecipeClass() {
        return AugmentEggJeiRecipe.class;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AugmentEggJeiRecipe recipe, IFocusGroup focuses) {
        Item centerItem = getCenterItem();
        SpawnEggItem spawnEgg = SpawnEggLookup.getEggForEntity(recipe.entityType());

        if (spawnEgg == null) return;

        ItemStack eggStack = new ItemStack(spawnEgg);
        ItemStack outputStack = AugmentEggItem.createForEntity(recipe.entityType());

        int gridX = 0;
        int gridY = 0;

        builder.addSlot(RecipeIngredientRole.INPUT, gridX + 18, gridY)
                .addItemStack(eggStack);
        builder.addSlot(RecipeIngredientRole.INPUT, gridX, gridY + 18)
                .addItemStack(eggStack);
        builder.addSlot(RecipeIngredientRole.INPUT, gridX + 18, gridY + 18)
                .addItemStack(new ItemStack(centerItem));
        builder.addSlot(RecipeIngredientRole.INPUT, gridX + 36, gridY + 18)
                .addItemStack(eggStack);
        builder.addSlot(RecipeIngredientRole.INPUT, gridX + 18, gridY + 36)
                .addItemStack(eggStack);

        builder.addSlot(RecipeIngredientRole.OUTPUT, gridX + 94, gridY + 18)
                .addItemStack(outputStack);
    }

    private Item getCenterItem() {
        String itemId = MobProcessorConfig.AUGMENT_EGG_CENTER_ITEM.get();
        ResourceLocation resourceLocation = ResourceLocation.tryParse(itemId);
        if (resourceLocation == null) return Items.DIAMOND_BLOCK;
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
        return item != null ? item : Items.DIAMOND_BLOCK;
    }
}
