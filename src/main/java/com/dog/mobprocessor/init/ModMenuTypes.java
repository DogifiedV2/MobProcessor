package com.dog.mobprocessor.init;

import com.dog.mobprocessor.MobProcessor;
import com.dog.mobprocessor.container.MobProcessorMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.CONTAINERS, MobProcessor.MOD_ID);

    public static final RegistryObject<MenuType<MobProcessorMenu>> MOB_PROCESSOR =
            MENU_TYPES.register("mob_processor", () -> IForgeMenuType.create(MobProcessorMenu::fromNetwork));
}
