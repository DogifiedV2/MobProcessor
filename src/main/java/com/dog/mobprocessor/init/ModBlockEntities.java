package com.dog.mobprocessor.init;

import com.dog.mobprocessor.MobProcessor;
import com.dog.mobprocessor.block.entity.MobProcessorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MobProcessor.MOD_ID);

    public static final RegistryObject<BlockEntityType<MobProcessorBlockEntity>> MOB_PROCESSOR =
            BLOCK_ENTITIES.register("mob_processor", () ->
                    BlockEntityType.Builder.of(
                            MobProcessorBlockEntity::new,
                            ModBlocks.MOB_PROCESSOR.get()
                    ).build(null));
}
