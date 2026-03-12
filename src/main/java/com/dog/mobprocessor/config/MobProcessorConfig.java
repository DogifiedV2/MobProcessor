package com.dog.mobprocessor.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class MobProcessorConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue DEFAULT_CYCLE_TIME_TICKS;
    public static final ForgeConfigSpec.IntValue MAX_NEARBY_ITEMS;
    public static final ForgeConfigSpec.DoubleValue DETECTION_RADIUS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_CYCLE_OVERRIDES;
    public static final ForgeConfigSpec.ConfigValue<String> AUGMENT_EGG_CENTER_ITEM;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("processing");
        DEFAULT_CYCLE_TIME_TICKS = builder
                .comment("Default number of ticks per processing cycle (20 ticks = 1 second)")
                .defineInRange("defaultCycleTimeTicks", 100, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("anti_lag");
        MAX_NEARBY_ITEMS = builder
                .comment("Maximum number of item entities nearby before processing pauses")
                .defineInRange("maxNearbyItems", 2000, 1, Integer.MAX_VALUE);
        DETECTION_RADIUS = builder
                .comment("Radius to scan for nearby item entities")
                .defineInRange("detectionRadius", 16.0, 1.0, 64.0);
        builder.pop();

        builder.push("mob_overrides");
        MOB_CYCLE_OVERRIDES = builder
                .comment("Per-mob cycle time overrides. Format: \"minecraft:zombie=120\"")
                .defineListAllowEmpty(List.of("mobCycleOverrides"), List::of, entry -> {
                    if (!(entry instanceof String s)) return false;
                    String[] parts = s.split("=", 2);
                    if (parts.length != 2) return false;
                    try {
                        Integer.parseInt(parts[1].trim());
                        return true;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                });
        builder.pop();

        builder.push("recipe");
        AUGMENT_EGG_CENTER_ITEM = builder
                .comment("Registry name of the item required in the center of the Augment Egg recipe")
                .define("augmentEggCenterItem", "minecraft:diamond_block");
        builder.pop();

        SPEC = builder.build();
    }
}
