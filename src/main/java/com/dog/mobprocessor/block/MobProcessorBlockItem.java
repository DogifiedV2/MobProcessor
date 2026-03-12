package com.dog.mobprocessor.block;

import com.dog.mobprocessor.block.entity.MobProcessorBlockEntity;
import com.dog.mobprocessor.item.AugmentEggItem;
import com.dog.mobprocessor.item.UpgradeItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MobProcessorBlockItem extends BlockItem {

    public MobProcessorBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        CompoundTag blockEntityTag = getBlockEntityData(stack);
        if (blockEntityTag == null) return;

        List<String> mobNames = new ArrayList<>();
        if (blockEntityTag.contains("EggInventory")) {
            ItemStackHandler handler = new ItemStackHandler(MobProcessorBlockEntity.MAX_EGG_SLOTS);
            handler.deserializeNBT(blockEntityTag.getCompound("EggInventory"));
            for (int i = 0; i < MobProcessorBlockEntity.MAX_EGG_SLOTS; i++) {
                ItemStack egg = handler.getStackInSlot(i);
                if (!egg.isEmpty() && egg.getItem() instanceof AugmentEggItem) {
                    EntityType<?> type = AugmentEggItem.getEntityType(egg);
                    if (type != null) {
                        mobNames.add(type.getDescription().getString());
                    }
                }
            }
        }

        List<String> upgradeNames = new ArrayList<>();
        if (blockEntityTag.contains("UpgradeInventory")) {
            ItemStackHandler upgradeHandler = new ItemStackHandler(MobProcessorBlockEntity.UPGRADE_SLOT_COUNT);
            upgradeHandler.deserializeNBT(blockEntityTag.getCompound("UpgradeInventory"));
            for (int i = 0; i < MobProcessorBlockEntity.UPGRADE_SLOT_COUNT; i++) {
                ItemStack upgrade = upgradeHandler.getStackInSlot(i);
                if (!upgrade.isEmpty() && upgrade.getItem() instanceof UpgradeItem upgradeItem) {
                    String tierNumeral = switch (upgradeItem.getTier()) {
                        case 1 -> "I";
                        case 2 -> "II";
                        case 3 -> "III";
                        default -> String.valueOf(upgradeItem.getTier());
                    };
                    upgradeNames.add(upgradeItem.getUpgradeType().getDisplayName() + " " + tierNumeral);
                }
            }
        }

        if (!mobNames.isEmpty() || !upgradeNames.isEmpty()) {
            StringBuilder line = new StringBuilder();
            if (!mobNames.isEmpty()) {
                line.append(String.join(", ", mobNames));
            }
            if (!upgradeNames.isEmpty()) {
                if (!mobNames.isEmpty()) line.append(" | ");
                line.append(String.join(", ", upgradeNames));
            }
            tooltip.add(new TextComponent(line.toString()).withStyle(ChatFormatting.GRAY));
        }
    }
}
