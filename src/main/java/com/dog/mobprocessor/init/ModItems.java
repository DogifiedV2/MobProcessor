package com.dog.mobprocessor.init;

import com.dog.mobprocessor.MobProcessor;
import com.dog.mobprocessor.item.AugmentEggItem;
import com.dog.mobprocessor.item.UpgradeItem;
import com.dog.mobprocessor.item.UpgradeType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MobProcessor.MOD_ID);

    public static final RegistryObject<Item> AUGMENT_EGG = ITEMS.register("augment_egg",
            AugmentEggItem::new);

    public static final RegistryObject<Item> LOOTING_UPGRADE_1 = ITEMS.register("looting_upgrade_1",
            () -> new UpgradeItem(UpgradeType.LOOTING, 1, "Looting I on loot rolls"));
    public static final RegistryObject<Item> LOOTING_UPGRADE_2 = ITEMS.register("looting_upgrade_2",
            () -> new UpgradeItem(UpgradeType.LOOTING, 2, "Looting II on loot rolls"));
    public static final RegistryObject<Item> LOOTING_UPGRADE_3 = ITEMS.register("looting_upgrade_3",
            () -> new UpgradeItem(UpgradeType.LOOTING, 3, "Looting III on loot rolls"));

    public static final RegistryObject<Item> SPEED_UPGRADE_1 = ITEMS.register("speed_upgrade_1",
            () -> new UpgradeItem(UpgradeType.SPEED, 1, "2x processing speed"));
    public static final RegistryObject<Item> SPEED_UPGRADE_2 = ITEMS.register("speed_upgrade_2",
            () -> new UpgradeItem(UpgradeType.SPEED, 2, "3x processing speed"));
    public static final RegistryObject<Item> SPEED_UPGRADE_3 = ITEMS.register("speed_upgrade_3",
            () -> new UpgradeItem(UpgradeType.SPEED, 3, "4x processing speed"));

    public static final RegistryObject<Item> STORAGE_UPGRADE_1 = ITEMS.register("storage_upgrade_1",
            () -> new UpgradeItem(UpgradeType.STORAGE, 1, "+6 augment slots (15 total)"));
    public static final RegistryObject<Item> STORAGE_UPGRADE_2 = ITEMS.register("storage_upgrade_2",
            () -> new UpgradeItem(UpgradeType.STORAGE, 2, "+12 augment slots (21 total)"));
    public static final RegistryObject<Item> STORAGE_UPGRADE_3 = ITEMS.register("storage_upgrade_3",
            () -> new UpgradeItem(UpgradeType.STORAGE, 3, "+18 augment slots (27 total)"));

    public static final RegistryObject<Item> PLAYER_KILL_UPGRADE = ITEMS.register("player_kill_upgrade",
            () -> new UpgradeItem(UpgradeType.PLAYER_KILL, 1, "Simulates player kills for loot tables"));
}
