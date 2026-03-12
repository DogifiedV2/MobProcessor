package com.dog.mobprocessor.client;

import com.dog.mobprocessor.container.MobProcessorMenu;
import com.dog.mobprocessor.container.MobProcessorSlot;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MobProcessorScreen extends AbstractContainerScreen<MobProcessorMenu> {

    private static final int BG_COLOR = 0xFFC6C6C6;
    private static final int BORDER_HIGHLIGHT = 0xFFFFFFFF;
    private static final int BORDER_SHADOW = 0xFF555555;
    private static final int SLOT_BORDER_DARK = 0xFF373737;
    private static final int SLOT_INNER = 0xFF8B8B8B;
    private static final int SLOT_LOCKED = 0xFF4A4A4A;

    private static final int PROGRESS_OVERLAY = 0x6044CC44;
    private static final int PROGRESS_OVERLAY_PAUSED = 0x60CC4444;

    public MobProcessorScreen(MobProcessorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 200;
        this.imageHeight = 166;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderProgressOverlays(poseStack);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    private void renderProgressOverlays(PoseStack poseStack) {
        poseStack.pushPose();
        poseStack.translate(0, 0, 300);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int unlockedSlots = this.menu.getUnlockedEggSlotCount();
        for (int i = 0; i < unlockedSlots; i++) {
            Slot slot = this.menu.slots.get(i);
            if (!slot.hasItem()) continue;

            int maxTime = this.menu.getMaxCycleTime(i);
            if (maxTime <= 0) continue;

            int progress = this.menu.getCycleProgress(i);
            int fillHeight = (int) ((float) progress / maxTime * 16);
            fillHeight = Math.min(fillHeight, 16);
            if (fillHeight <= 0) continue;

            int slotX = this.leftPos + slot.x;
            int slotY = this.topPos + slot.y;
            int fillColor = this.menu.getPauseState() > 0 ? PROGRESS_OVERLAY_PAUSED : PROGRESS_OVERLAY;

            fill(poseStack, slotX, slotY + 16 - fillHeight, slotX + 16, slotY + 16, fillColor);
        }

        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        int titleWidth = this.font.width(this.title);
        this.font.draw(poseStack, this.title, (170 - titleWidth) / 2f, this.titleLabelY, 0x404040);
        this.font.draw(poseStack, this.playerInventoryTitle, 8, this.inventoryLabelY, 0x404040);

        int pauseState = this.menu.getPauseState();
        if (pauseState > 0) {
            Component pauseText = pauseState == 1
                    ? new TextComponent("Redstone Paused").withStyle(ChatFormatting.RED)
                    : new TextComponent("Too Many Items").withStyle(ChatFormatting.GOLD);
            int pauseWidth = this.font.width(pauseText);
            this.font.draw(poseStack, pauseText, (170 - pauseWidth) / 2f, 60, 0xFFFFFF);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        fill(poseStack, x, y, x + imageWidth, y + imageHeight, BG_COLOR);

        hLine(poseStack, x, x + imageWidth - 1, y, BORDER_HIGHLIGHT);
        vLine(poseStack, x, y, y + imageHeight - 1, BORDER_HIGHLIGHT);
        hLine(poseStack, x + 1, x + imageWidth - 1, y + imageHeight - 1, BORDER_SHADOW);
        vLine(poseStack, x + imageWidth - 1, y + 1, y + imageHeight - 1, BORDER_SHADOW);

        int unlockedSlots = this.menu.getUnlockedEggSlotCount();

        for (int i = 0; i < this.menu.slots.size(); i++) {
            Slot slot = this.menu.slots.get(i);
            if (i < MobProcessorMenu.MAX_EGG_SLOTS) {
                if (i < unlockedSlots) {
                    drawSlotBackground(poseStack, x + slot.x - 1, y + slot.y - 1);
                } else {
                    drawLockedSlotBackground(poseStack, x + slot.x - 1, y + slot.y - 1);
                }
            } else {
                drawSlotBackground(poseStack, x + slot.x - 1, y + slot.y - 1);
            }
        }
    }

    @Override
    protected void renderTooltip(PoseStack poseStack, int mouseX, int mouseY) {
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null) {
            int slotIndex = this.hoveredSlot.index;

            if (this.hoveredSlot instanceof MobProcessorSlot eggSlot && eggSlot.isLocked()) {
                List<Component> tooltip = new ArrayList<>();
                String requiredTier = getRequiredStorageTier(slotIndex);
                tooltip.add(new TextComponent("Requires " + requiredTier).withStyle(ChatFormatting.RED));
                this.renderTooltip(poseStack, tooltip, java.util.Optional.empty(), mouseX, mouseY);
                return;
            }

            if (this.hoveredSlot.hasItem() && this.hoveredSlot instanceof MobProcessorSlot) {
                ItemStack stack = this.hoveredSlot.getItem();
                List<Component> tooltip = this.getTooltipFromItem(stack);

                long itemsProduced = this.menu.getItemsProduced(slotIndex);
                String formattedCount = NumberFormat.getIntegerInstance().format(itemsProduced);
                tooltip.add(new TextComponent("Items produced: " + formattedCount).withStyle(ChatFormatting.GRAY));

                this.renderTooltip(poseStack, tooltip, stack.getTooltipImage(), mouseX, mouseY);
                return;
            }
        }
        super.renderTooltip(poseStack, mouseX, mouseY);
    }

    private String getRequiredStorageTier(int slotIndex) {
        if (slotIndex < 15) return "Storage Upgrade I";
        if (slotIndex < 21) return "Storage Upgrade II";
        return "Storage Upgrade III";
    }

    private void drawSlotBackground(PoseStack poseStack, int x, int y) {
        fill(poseStack, x, y, x + 18, y + 1, SLOT_BORDER_DARK);
        fill(poseStack, x, y + 1, x + 1, y + 17, SLOT_BORDER_DARK);
        fill(poseStack, x + 1, y + 17, x + 18, y + 18, BORDER_HIGHLIGHT);
        fill(poseStack, x + 17, y + 1, x + 18, y + 17, BORDER_HIGHLIGHT);
        fill(poseStack, x + 1, y + 1, x + 17, y + 17, SLOT_INNER);
    }

    private void drawLockedSlotBackground(PoseStack poseStack, int x, int y) {
        fill(poseStack, x, y, x + 18, y + 1, SLOT_BORDER_DARK);
        fill(poseStack, x, y + 1, x + 1, y + 17, SLOT_BORDER_DARK);
        fill(poseStack, x + 1, y + 17, x + 18, y + 18, BORDER_HIGHLIGHT);
        fill(poseStack, x + 17, y + 1, x + 18, y + 17, BORDER_HIGHLIGHT);
        fill(poseStack, x + 1, y + 1, x + 17, y + 17, SLOT_LOCKED);
    }
}
