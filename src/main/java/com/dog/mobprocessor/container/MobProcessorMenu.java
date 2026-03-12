package com.dog.mobprocessor.container;

import com.dog.mobprocessor.block.entity.MobProcessorBlockEntity;
import com.dog.mobprocessor.init.ModBlocks;
import com.dog.mobprocessor.init.ModMenuTypes;
import com.dog.mobprocessor.item.AugmentEggItem;
import com.dog.mobprocessor.item.UpgradeItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class MobProcessorMenu extends AbstractContainerMenu {

    public static final int MAX_EGG_SLOTS = MobProcessorBlockEntity.MAX_EGG_SLOTS;
    public static final int UPGRADE_SLOT_COUNT = MobProcessorBlockEntity.UPGRADE_SLOT_COUNT;
    public static final int DATA_SLOT_COUNT = 113;

    private static final int EGG_GRID_START_X = 8;
    private static final int EGG_GRID_START_Y = 18;
    private static final int EGG_GRID_COLS = 9;
    private static final int EGG_GRID_ROWS = 3;

    private static final int UPGRADE_SLOT_X = 176;
    private static final int UPGRADE_SLOT_START_Y = 18;
    private static final int UPGRADE_SLOT_SPACING = 18;

    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;
    private static final int PLAYER_HOTBAR_Y = 142;

    private final ContainerData data;
    private final ContainerLevelAccess containerLevelAccess;

    public static MobProcessorMenu fromNetwork(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        return new MobProcessorMenu(windowId, playerInventory,
                new ItemStackHandler(MAX_EGG_SLOTS),
                new ItemStackHandler(UPGRADE_SLOT_COUNT),
                new SimpleContainerData(DATA_SLOT_COUNT),
                ContainerLevelAccess.NULL);
    }

    public MobProcessorMenu(int windowId, Inventory playerInventory, MobProcessorBlockEntity blockEntity) {
        this(windowId, playerInventory,
                blockEntity.getEggInventory(),
                blockEntity.getUpgradeInventory(),
                blockEntity.getContainerData(),
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()));
    }

    private MobProcessorMenu(int windowId, Inventory playerInventory, IItemHandler eggHandler,
                              IItemHandler upgradeHandler, ContainerData data, ContainerLevelAccess access) {
        super(ModMenuTypes.MOB_PROCESSOR.get(), windowId);
        this.data = data;
        this.containerLevelAccess = access;

        for (int row = 0; row < EGG_GRID_ROWS; row++) {
            for (int col = 0; col < EGG_GRID_COLS; col++) {
                int slotIndex = col + row * EGG_GRID_COLS;
                addSlot(new MobProcessorSlot(eggHandler, slotIndex,
                        EGG_GRID_START_X + col * 18, EGG_GRID_START_Y + row * 18,
                        this::getUnlockedEggSlotCount));
            }
        }

        for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
            addSlot(new UpgradeSlot(upgradeHandler, i,
                    UPGRADE_SLOT_X, UPGRADE_SLOT_START_Y + i * UPGRADE_SLOT_SPACING));
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9,
                        PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, PLAYER_HOTBAR_Y));
        }

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack slotStack = slot.getItem();
        ItemStack originalStack = slotStack.copy();

        int firstPlayerSlot = MAX_EGG_SLOTS + UPGRADE_SLOT_COUNT;
        int lastPlayerSlot = this.slots.size();

        if (slotIndex < MAX_EGG_SLOTS) {
            if (!moveItemStackTo(slotStack, firstPlayerSlot, lastPlayerSlot, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex < firstPlayerSlot) {
            if (!moveItemStackTo(slotStack, firstPlayerSlot, lastPlayerSlot, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (slotStack.getItem() instanceof UpgradeItem) {
                if (!moveItemStackTo(slotStack, MAX_EGG_SLOTS, MAX_EGG_SLOTS + UPGRADE_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (slotStack.getItem() instanceof AugmentEggItem) {
                int unlockedSlots = getUnlockedEggSlotCount();
                if (!moveItemStackTo(slotStack, 0, unlockedSlots, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (slotStack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (slotStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, slotStack);
        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.containerLevelAccess, player, ModBlocks.MOB_PROCESSOR.get());
    }

    public int getCycleProgress(int slot) {
        return data.get(slot);
    }

    public int getMaxCycleTime(int slot) {
        return data.get(27 + slot);
    }

    public long getItemsProduced(int slot) {
        int low = data.get(54 + slot) & 0xFFFF;
        int high = data.get(82 + slot) & 0xFFFF;
        return ((long) high << 16) | low;
    }

    public int getPauseState() {
        return data.get(81);
    }

    public int getUnlockedEggSlotCount() {
        return data.get(109);
    }

    public int getLootingLevel() {
        return data.get(110);
    }

    public int getSpeedMultiplier() {
        return data.get(111);
    }

    public boolean hasPlayerKillUpgrade() {
        return data.get(112) == 1;
    }
}
