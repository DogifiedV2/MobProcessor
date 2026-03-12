package com.dog.mobprocessor.init;

import com.dog.mobprocessor.MobProcessor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTab {

    public static final CreativeModeTab TAB = new CreativeModeTab(MobProcessor.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ModBlocks.MOB_PROCESSOR_ITEM.get());
        }
    };
}
