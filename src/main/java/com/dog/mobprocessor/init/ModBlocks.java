package com.dog.mobprocessor.init;

import com.dog.mobprocessor.MobProcessor;
import com.dog.mobprocessor.block.MobProcessorBlock;
import com.dog.mobprocessor.block.MobProcessorBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MobProcessor.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MobProcessor.MOD_ID);

    public static final RegistryObject<Block> MOB_PROCESSOR =
            BLOCKS.register("mob_processor", MobProcessorBlock::new);

    public static final RegistryObject<Item> MOB_PROCESSOR_ITEM =
            ITEMS.register("mob_processor", () -> new MobProcessorBlockItem(
                    MOB_PROCESSOR.get(),
                    new Item.Properties()
                            .stacksTo(1)
                            .tab(ModCreativeTab.TAB)
            ));
}
