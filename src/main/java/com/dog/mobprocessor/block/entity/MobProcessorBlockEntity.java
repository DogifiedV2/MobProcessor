package com.dog.mobprocessor.block.entity;

import com.dog.mobprocessor.config.MobProcessorConfig;
import com.dog.mobprocessor.container.MobProcessorMenu;
import com.dog.mobprocessor.init.ModBlockEntities;
import com.dog.mobprocessor.item.AugmentEggItem;
import com.dog.mobprocessor.item.UpgradeItem;
import com.dog.mobprocessor.item.UpgradeType;
import com.dog.mobprocessor.loot.LootTableRoller;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobProcessorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int MAX_EGG_SLOTS = 27;
    public static final int BASE_EGG_SLOTS = 9;
    public static final int UPGRADE_SLOT_COUNT = 3;

    private static final Direction[] BELOW_CAPABILITY_DIRECTIONS = {null, Direction.UP, Direction.DOWN};
    private static final Direction[] HORIZONTAL_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

    private final ItemStackHandler eggInventory = new ItemStackHandler(MAX_EGG_SLOTS) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot >= getUnlockedEggSlotCount()) {
                return false;
            }
            if (!(stack.getItem() instanceof AugmentEggItem)) {
                return false;
            }
            EntityType<?> incomingType = AugmentEggItem.getEntityType(stack);
            if (incomingType == null) return false;
            for (int i = 0; i < getUnlockedEggSlotCount(); i++) {
                if (i == slot) continue;
                ItemStack existing = getStackInSlot(i);
                if (existing.isEmpty()) continue;
                EntityType<?> existingType = AugmentEggItem.getEntityType(existing);
                if (existingType != null && existingType == incomingType) {
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onContentsChanged(int slot) {
            slotCycleProgress[slot] = 0;
            setChanged();
        }
    };

    private final ItemStackHandler upgradeInventory = new ItemStackHandler(UPGRADE_SLOT_COUNT) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (!(stack.getItem() instanceof UpgradeItem incomingUpgrade)) {
                return false;
            }
            UpgradeType incomingType = incomingUpgrade.getUpgradeType();
            for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
                if (i == slot) continue;
                ItemStack existing = getStackInSlot(i);
                if (!existing.isEmpty() && existing.getItem() instanceof UpgradeItem existingUpgrade) {
                    if (existingUpgrade.getUpgradeType() == incomingType) {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            handleStorageDowngrade();
        }
    };

    private LazyOptional<IItemHandler> combinedHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> eggHandler = LazyOptional.empty();
    private LazyOptional<IItemHandler> upgradeHandler = LazyOptional.empty();

    private final int[] slotCycleProgress = new int[MAX_EGG_SLOTS];
    private final Map<ResourceLocation, Long> itemsProducedPerMob = new HashMap<>();
    private int pauseState = 0;

    private Map<ResourceLocation, Integer> mobCycleOverrideCache;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            if (index < 27) return slotCycleProgress[index];
            if (index < 54) return getEffectiveCycleTimeForSlot(index - 27);
            if (index < 81) return (int) (getItemsProducedForSlot(index - 54) & 0xFFFF);
            if (index == 81) return pauseState;
            if (index < 109) return (int) ((getItemsProducedForSlot(index - 82) >> 16) & 0xFFFF);
            if (index == 109) return getUnlockedEggSlotCount();
            if (index == 110) return getLootingLevel();
            if (index == 111) return getSpeedMultiplier();
            if (index == 112) return hasPlayerKillUpgrade() ? 1 : 0;
            return 0;
        }

        @Override
        public void set(int index, int value) {
            if (index < 27) slotCycleProgress[index] = value;
            else if (index == 81) pauseState = value;
        }

        @Override
        public int getCount() {
            return MobProcessorMenu.DATA_SLOT_COUNT;
        }
    };

    public MobProcessorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOB_PROCESSOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MobProcessorBlockEntity blockEntity) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        if (level.hasNeighborSignal(pos)) {
            blockEntity.pauseState = 1;
            return;
        }

        if (blockEntity.tooManyNearbyItems(serverLevel, pos)) {
            blockEntity.pauseState = 2;
            blockEntity.spawnPausedParticles(serverLevel, pos);
            return;
        }

        blockEntity.pauseState = 0;
        boolean anyProcessing = false;
        int unlockedSlots = blockEntity.getUnlockedEggSlotCount();

        for (int slot = 0; slot < unlockedSlots; slot++) {
            if (!blockEntity.eggInventory.getStackInSlot(slot).isEmpty()) {
                anyProcessing = true;
            }
            blockEntity.processSlot(serverLevel, pos, slot);
        }

        if (anyProcessing) {
            blockEntity.spawnActiveParticles(serverLevel, pos);
        }
    }

    private void processSlot(ServerLevel serverLevel, BlockPos pos, int slot) {
        ItemStack eggStack = eggInventory.getStackInSlot(slot);
        if (eggStack.isEmpty()) {
            slotCycleProgress[slot] = 0;
            return;
        }

        if (!(eggStack.getItem() instanceof AugmentEggItem)) return;

        EntityType<?> entityType = AugmentEggItem.getEntityType(eggStack);
        if (entityType == null) return;

        int effectiveCycleTime = getEffectiveCycleTime(entityType);

        slotCycleProgress[slot]++;

        if (slotCycleProgress[slot] >= effectiveCycleTime) {
            slotCycleProgress[slot] = 0;

            List<ItemStack> loot = LootTableRoller.rollEntityLoot(serverLevel, pos, entityType, getLootingLevel(), hasPlayerKillUpgrade());
            ResourceLocation mobId = EntityType.getKey(entityType);
            for (ItemStack lootItem : loot) {
                itemsProducedPerMob.merge(mobId, (long) lootItem.getCount(), Long::sum);
                outputItem(serverLevel, pos, lootItem);
            }

            setChanged();
        }
    }

    private int getEffectiveCycleTime(EntityType<?> entityType) {
        int baseCycleTime = getCycleTimeForMob(entityType);
        int speedMultiplier = getSpeedMultiplier();
        return Math.max(1, baseCycleTime / speedMultiplier);
    }

    private int getEffectiveCycleTimeForSlot(int slot) {
        ItemStack eggStack = eggInventory.getStackInSlot(slot);
        if (eggStack.isEmpty() || !(eggStack.getItem() instanceof AugmentEggItem)) {
            int baseCycleTime = MobProcessorConfig.DEFAULT_CYCLE_TIME_TICKS.get();
            return Math.max(1, baseCycleTime / getSpeedMultiplier());
        }
        EntityType<?> entityType = AugmentEggItem.getEntityType(eggStack);
        if (entityType == null) {
            int baseCycleTime = MobProcessorConfig.DEFAULT_CYCLE_TIME_TICKS.get();
            return Math.max(1, baseCycleTime / getSpeedMultiplier());
        }
        return getEffectiveCycleTime(entityType);
    }

    private int getCycleTimeForMob(EntityType<?> entityType) {
        if (mobCycleOverrideCache == null) {
            rebuildOverrideCache();
        }
        ResourceLocation entityId = EntityType.getKey(entityType);
        Integer override = mobCycleOverrideCache.get(entityId);
        return override != null ? override : MobProcessorConfig.DEFAULT_CYCLE_TIME_TICKS.get();
    }

    private void rebuildOverrideCache() {
        mobCycleOverrideCache = new HashMap<>();
        List<? extends String> overrides = MobProcessorConfig.MOB_CYCLE_OVERRIDES.get();
        for (String entry : overrides) {
            String[] parts = entry.split("=", 2);
            if (parts.length != 2) continue;
            try {
                ResourceLocation mobId = ResourceLocation.tryParse(parts[0].trim());
                if (mobId == null) continue;
                int ticks = Integer.parseInt(parts[1].trim());
                mobCycleOverrideCache.put(mobId, ticks);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public int getLootingLevel() {
        for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
            ItemStack stack = upgradeInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade
                    && upgrade.getUpgradeType() == UpgradeType.LOOTING) {
                return upgrade.getTier();
            }
        }
        return 0;
    }

    public int getSpeedMultiplier() {
        for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
            ItemStack stack = upgradeInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade
                    && upgrade.getUpgradeType() == UpgradeType.SPEED) {
                return upgrade.getTier() + 1;
            }
        }
        return 1;
    }

    public boolean hasPlayerKillUpgrade() {
        for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
            ItemStack stack = upgradeInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade
                    && upgrade.getUpgradeType() == UpgradeType.PLAYER_KILL) {
                return true;
            }
        }
        return false;
    }

    public int getUnlockedEggSlotCount() {
        for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
            ItemStack stack = upgradeInventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem upgrade
                    && upgrade.getUpgradeType() == UpgradeType.STORAGE) {
                return BASE_EGG_SLOTS + upgrade.getTier() * 6;
            }
        }
        return BASE_EGG_SLOTS;
    }

    private void handleStorageDowngrade() {
        if (level == null || level.isClientSide()) return;

        int unlockedSlots = getUnlockedEggSlotCount();
        for (int slot = unlockedSlots; slot < MAX_EGG_SLOTS; slot++) {
            ItemStack overflow = eggInventory.getStackInSlot(slot);
            if (!overflow.isEmpty()) {
                eggInventory.setStackInSlot(slot, ItemStack.EMPTY);
                slotCycleProgress[slot] = 0;
                ejectToWorld((ServerLevel) level, worldPosition, overflow);
            }
        }
    }

    private boolean tooManyNearbyItems(ServerLevel level, BlockPos pos) {
        double radius = MobProcessorConfig.DETECTION_RADIUS.get();
        AABB scanArea = new AABB(pos).inflate(radius);
        int itemCount = level.getEntitiesOfClass(ItemEntity.class, scanArea).size();
        return itemCount >= MobProcessorConfig.MAX_NEARBY_ITEMS.get();
    }

    private void outputItem(ServerLevel level, BlockPos pos, ItemStack stack) {
        ItemStack remaining = tryInsertIntoInventoryBelow(level, pos, stack);
        if (remaining.isEmpty()) return;

        remaining = tryInsertIntoEntityInventoriesBelow(level, pos, remaining);
        if (remaining.isEmpty()) return;

        ejectToWorld(level, pos, remaining);
    }

    private ItemStack tryInsertIntoInventoryBelow(ServerLevel level, BlockPos pos, ItemStack stack) {
        BlockPos belowPos = pos.below();
        BlockEntity belowEntity = level.getBlockEntity(belowPos);
        if (belowEntity == null) return stack;

        for (Direction direction : BELOW_CAPABILITY_DIRECTIONS) {
            LazyOptional<IItemHandler> capability = belowEntity.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction);
            if (capability.isPresent()) {
                IItemHandler handler = capability.orElseThrow(IllegalStateException::new);
                return ItemHandlerHelper.insertItemStacked(handler, stack.copy(), false);
            }
        }

        return stack;
    }

    private ItemStack tryInsertIntoEntityInventoriesBelow(ServerLevel level, BlockPos pos, ItemStack stack) {
        AABB scanArea = new AABB(pos.below()).inflate(0.5);
        List<net.minecraft.world.entity.Entity> entities = level.getEntities(null, scanArea);

        for (net.minecraft.world.entity.Entity entity : entities) {
            LazyOptional<IItemHandler> capability = entity.getCapability(
                    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
            if (capability.isPresent()) {
                IItemHandler handler = capability.orElseThrow(IllegalStateException::new);
                stack = ItemHandlerHelper.insertItemStacked(handler, stack.copy(), false);
                if (stack.isEmpty()) return ItemStack.EMPTY;
            }
        }

        return stack;
    }

    private void ejectToWorld(ServerLevel level, BlockPos pos, ItemStack stack) {
        Direction direction = HORIZONTAL_DIRECTIONS[level.random.nextInt(HORIZONTAL_DIRECTIONS.length)];

        double spawnX = pos.getX() + 0.5 + direction.getStepX() * 0.6;
        double spawnY = pos.getY() + 0.5;
        double spawnZ = pos.getZ() + 0.5 + direction.getStepZ() * 0.6;

        ItemEntity itemEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, stack);
        itemEntity.setDeltaMovement(
                direction.getStepX() * 0.1,
                0.15,
                direction.getStepZ() * 0.1
        );

        level.addFreshEntity(itemEntity);
    }

    private void spawnActiveParticles(ServerLevel level, BlockPos pos) {
        if (level.getGameTime() % 20 != 0) return;
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                1, 0.2, 0.0, 0.2, 0.01);
    }

    private void spawnPausedParticles(ServerLevel level, BlockPos pos) {
        if (level.getGameTime() % 10 != 0) return;
        level.sendParticles(ParticleTypes.FLAME,
                pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                2, 0.3, 0.0, 0.3, 0.01);
    }

    private long getItemsProducedForSlot(int slot) {
        ItemStack eggStack = eggInventory.getStackInSlot(slot);
        if (eggStack.isEmpty() || !(eggStack.getItem() instanceof AugmentEggItem)) {
            return 0;
        }
        EntityType<?> entityType = AugmentEggItem.getEntityType(eggStack);
        if (entityType == null) return 0;
        ResourceLocation mobId = EntityType.getKey(entityType);
        return itemsProducedPerMob.getOrDefault(mobId, 0L);
    }

    public boolean isEmpty() {
        for (int i = 0; i < MAX_EGG_SLOTS; i++) {
            if (!eggInventory.getStackInSlot(i).isEmpty()) return false;
        }
        for (int i = 0; i < UPGRADE_SLOT_COUNT; i++) {
            if (!upgradeInventory.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    public void saveToItemTag(CompoundTag tag) {
        saveAdditional(tag);
    }

    public ItemStackHandler getEggInventory() {
        return eggInventory;
    }

    public ItemStackHandler getUpgradeInventory() {
        return upgradeInventory;
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("container.mobprocessor.mob_processor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new MobProcessorMenu(windowId, playerInventory, this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        combinedHandler = LazyOptional.of(() -> new CombinedInvWrapper(eggInventory, upgradeInventory));
        eggHandler = LazyOptional.of(() -> eggInventory);
        upgradeHandler = LazyOptional.of(() -> upgradeInventory);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        combinedHandler.invalidate();
        eggHandler.invalidate();
        upgradeHandler.invalidate();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (side == Direction.UP) return eggHandler.cast();
            if (side == Direction.DOWN) return combinedHandler.cast();
            return upgradeHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("EggInventory", eggInventory.serializeNBT());
        tag.put("UpgradeInventory", upgradeInventory.serializeNBT());
        tag.putIntArray("SlotCycleProgress", slotCycleProgress);

        CompoundTag producedTag = new CompoundTag();
        itemsProducedPerMob.forEach((mobId, count) -> producedTag.putLong(mobId.toString(), count));
        tag.put("ItemsProducedPerMob", producedTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("EggInventory")) {
            eggInventory.deserializeNBT(tag.getCompound("EggInventory"));
        }
        if (tag.contains("UpgradeInventory")) {
            upgradeInventory.deserializeNBT(tag.getCompound("UpgradeInventory"));
        }
        if (tag.contains("SlotCycleProgress")) {
            int[] loaded = tag.getIntArray("SlotCycleProgress");
            System.arraycopy(loaded, 0, slotCycleProgress, 0, Math.min(loaded.length, MAX_EGG_SLOTS));
        }
        if (tag.contains("ItemsProducedPerMob")) {
            itemsProducedPerMob.clear();
            CompoundTag producedTag = tag.getCompound("ItemsProducedPerMob");
            for (String key : producedTag.getAllKeys()) {
                ResourceLocation mobId = ResourceLocation.tryParse(key);
                if (mobId != null) {
                    itemsProducedPerMob.put(mobId, producedTag.getLong(key));
                }
            }
        }
    }
}
