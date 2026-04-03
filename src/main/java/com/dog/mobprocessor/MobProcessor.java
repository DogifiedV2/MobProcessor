package com.dog.mobprocessor;

import com.dog.mobprocessor.client.MobProcessorScreen;
import com.dog.mobprocessor.config.MobProcessorConfig;
import com.dog.mobprocessor.init.ModBlockEntities;
import com.dog.mobprocessor.init.ModBlocks;
import com.dog.mobprocessor.init.ModItems;
import com.dog.mobprocessor.init.ModMenuTypes;
import com.dog.mobprocessor.init.ModRecipeSerializers;
import com.dog.mobprocessor.item.AugmentEggItem;
import com.dog.mobprocessor.loot.LootSimulationGuard;
import net.minecraft.client.gui.screens.MenuScreens;
import com.dog.mobprocessor.item.SpawnEggLookup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MobProcessor.MOD_ID)
public class MobProcessor {

    public static final String MOD_ID = "mobprocessor";

    public MobProcessor() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.ITEMS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenuTypes.MENU_TYPES.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MobProcessorConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(MobProcessor::onEntityJoinLevel);
    }

    private static void onEntityJoinLevel(EntityJoinWorldEvent event) {
        if (LootSimulationGuard.isActive()) {
            event.setCanceled(true);
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() ->
                    MenuScreens.register(ModMenuTypes.MOB_PROCESSOR.get(), MobProcessorScreen::new)
            );
        }

        @SubscribeEvent
        public static void onItemColors(ColorHandlerEvent.Item event) {
            event.getItemColors().register((stack, tintIndex) -> {
                if (tintIndex == 0) return 0xFFFFFF;
                EntityType<?> entityType = AugmentEggItem.getEntityType(stack);
                if (entityType == null) return 0xFFFFFF;
                SpawnEggItem spawnEgg = SpawnEggLookup.getEggForEntity(entityType);
                if (spawnEgg == null) return 0xFFFFFF;
                return spawnEgg.getColor(tintIndex - 1);
            }, ModItems.AUGMENT_EGG.get());
        }
    }
}
