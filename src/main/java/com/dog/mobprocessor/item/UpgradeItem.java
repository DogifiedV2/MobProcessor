package com.dog.mobprocessor.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradeItem extends Item {

    private final UpgradeType upgradeType;
    private final int tier;
    private final String effectDescription;

    public UpgradeItem(UpgradeType upgradeType, int tier, String effectDescription) {
        super(new Properties().stacksTo(1).tab(com.dog.mobprocessor.init.ModCreativeTab.TAB));
        this.upgradeType = upgradeType;
        this.tier = tier;
        this.effectDescription = effectDescription;
    }

    public UpgradeType getUpgradeType() {
        return upgradeType;
    }

    public int getTier() {
        return tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(new TextComponent(effectDescription).withStyle(ChatFormatting.GRAY));
    }
}
